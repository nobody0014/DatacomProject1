import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MasterThread {
	
	private int number_of_workers;
	private ArrayList<String> chunkQueue; 
	private ArrayList<String> chunkInfor;
	
	private ResumableChecker rc;
		
	private URL hostInfo;
	private String domain;
	private String path;
	private int port = 80; 
	
	private String fn;
	private FileWriter mw;
	
	private Socket client;
	private DataInputStream in;
	private DataOutputStream out;
	
	
	private long headSize;
	private boolean headWritten;
	
	
	public MasterThread(String host,String fileName,int wn) {
		setUpHostInformation(host);
		fn = fileName;
		rc  = new ResumableChecker(domain,path,port,fn);
		this.number_of_workers = wn;
	}
	
	public void beginDownload(){
		setUpFileInformation();
		setUpChunks();
		File f = new File(fn + "CRDOWNLOAD");
		if(rc.contentLengthExists()){
			try{
				RandomAccessFile raf = new RandomAccessFile(f, "rw");
				raf.setLength(rc.getContentLength());
				raf.close();
			}catch (Exception e){
				System.out.println("Cant access file");
			}
		}
		if(rc.isResumable()){
			createMetaFile();
		}
		else{
			System.out.println("Download Type is not resumable, setting number of workers to 1");
			number_of_workers = 1;
		}
		ArrayList<String> holder = new ArrayList<>();
		for(String s: chunkQueue){holder.add(s);}
		ExtendedThreadPool exe = new ExtendedThreadPool(number_of_workers, number_of_workers, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(chunkQueue.size()),chunkQueue,holder);		
		while(!holder.isEmpty() || exe.getActiveCount()>0){
			if(!holder.isEmpty()){
				exe.execute(createJob(holder.get(0)));
				holder.remove(0);
			}
			while(exe.getActiveCount() == exe.getMaximumPoolSize()){
				if(rc.isResumable()){printProgress();editMetaFile();}
			}
			while(holder.isEmpty() && exe.getActiveCount()>0){if(rc.isResumable()){printProgress();editMetaFile();}}
		}
		exe.shutdown();
		completeTheDownload();
		System.out.println("Finish Renaming file");
	}
	
	
	public void completeTheDownload(){
		if(rc.isResumable()){
			File m = new File(fn + "META");
			m.delete();
		}
		File f  = new File(fn + "CRDOWNLOAD");
		f.renameTo(new File(fn));
		f.delete();
	}
	public Runnable createJob(String s){
		int chunkSize = Integer.parseInt(chunkInfor.get(0));
		int chunkNum = Integer.parseInt(s.substring(1,s.length()));
		if(chunkNum < rc.getContentLength()/rc.getChunkSize() + 1){
			return new WorkerThread(chunkNum*chunkSize,chunkSize,fn + "CRDOWNLOAD",domain,path,port,s);
		}
		else{
			return new WorkerThread(chunkNum*chunkSize,fn + "CRDOWNLOAD",domain,path,port,s);
		}	
	}
	public void createMetaFile(){
		if(!headWritten){
			mw = new FileWriter(fn + "META");
			String metaHeader = rc.getHead() + "\r\n\r\n";
			mw.writeHead(metaHeader.getBytes());
			headWritten = true;
		}
		headSize = rc.getHead().getBytes().length;
	}
	public void editMetaFile(){
		mw.writeError(chunkQueue, chunkInfor,headSize);
	}
	public void setUpHostInformation(String host){
		try{
			hostInfo = HeadProc.procHost(host);
		}catch (Exception e){
			System.out.println("Unaccetable host, Quiting program");
			System.exit(0);
		}
		domain = hostInfo.getHost();
		if(hostInfo.getPath() == null || hostInfo.getPath().equals("")){path = "/";}
		else{path = hostInfo.getPath();}
		if(hostInfo.getPort() != -1){port = hostInfo.getPort();}
	}
	
	public void setUpFileInformation(){
		connectToHost();
		rc.downloadResumeHead(client, in, out, HeadProc.makeHeadReq(path, domain));
		closeConnection();
		if(checkFileMoved()){
			searchCorrectUrl(0);
		}
		rc.checkDownloadType();
		rc.checkDoResume();
	}
	public boolean checkFileMoved(){
		String error = HeadProc.checkError(rc.getHead());
		if(error != null && error.contains("3")){
			return true;
		}
		return false;
	}
	private void searchCorrectUrl(int timeSearched){
		if(timeSearched < 6){
			String newUrl = extractLocation(rc.getHead());
			setUpHostInformation(newUrl);
			if(!newUrl.equals("")){
				connectToHost();
				rc.resetHead();
				rc.downloadResumeHead(client, in, out, HeadProc.makeHeadReq(path, domain));
				closeConnection();
				if(checkFileMoved()){
					searchCorrectUrl(timeSearched+1);
				}
			}
		}
		else{
			System.out.println("Cant search for the url, quiting the program");
			System.exit(0);
		}
	}
	private String extractLocation(String headers){
		String[] h = headers.split("\r\n");
		String newUrl = "";
		for(String s : h){
			if(s.contains("Location")){
				newUrl = s.split(": ")[1];
			}
		}
		return newUrl;
	}
	
	public void setUpChunks(){
		chunkQueue = rc.getChunk(number_of_workers);
		chunkInfor = new ArrayList<String>();
		chunkInfor.add(chunkQueue.get(chunkQueue.size()-2));
		chunkInfor.add(chunkQueue.get(chunkQueue.size()-1));
		chunkQueue.remove(chunkQueue.size()-1);
		chunkQueue.remove(chunkQueue.size()-1);
	}
	public void printProgress(){
		if(rc.contentLengthExists()){
			String toPrint = (((rc.getTotalChunks()-chunkQueue.size())/(float)rc.getTotalChunks())*100) + "";
			if(toPrint.length() > 4){
				System.out.printf("%s \r", toPrint.substring(0, 4) + "%");
			}
			else{
				System.out.printf("%s \r", toPrint + "%");
			}
		}
	}
	
	public void connectToHost(){
		try{
			client = new Socket();
			client.connect(new InetSocketAddress(domain, port), 3000);
			client.setSoTimeout(5000);
			out = new DataOutputStream(client.getOutputStream());
			in = new DataInputStream(client.getInputStream());
			
		}
		catch (Exception e){
			System.out.println("Unable to get response from server.\nQuiting");
			System.exit(0);
		}
	}
	public void closeConnection(){
		try{
			client.close();
			in.close();
			out.close();
		}catch(Exception e){
			System.out.println("Unable to close clients, ggwp hell is going down");
		}
	}
	
}
