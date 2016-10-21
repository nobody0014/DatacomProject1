import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MasterThread {
	
	private int number_of_workers = 1;
	private ArrayList<String> chunkQueue; 
	
	private ResumableChecker rc;
		
	private URL hostInfo;
	private String domain;
	private String path;
	private String fn;
	private int port = 80; 
	
	private FileWriter mw;
	
	private Socket client;
	private DataInputStream in;
	private DataOutputStream out;
	
	private long headSize;
	private boolean headWritten;
	
	public MasterThread(String host,String fileName,int wn) {
		setUpHostInformation(host);
		rc  = new ResumableChecker(domain,path,port,fn);
		this.number_of_workers = wn;
	}
	public void beginDownload(){
		setUpFileInformation();
		setUpChunks();
		if(!rc.doResumable()){
			File f = new File(fn + "CRDOWNLOAD");
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
		ExtendedThreadPool exe = new ExtendedThreadPool(number_of_workers, number_of_workers, 0, TimeUnit.MILLISECONDS, createJobs(),chunkQueue);
		while(!exe.isTerminated()){
			try{
				Thread.sleep(1000);
				if(rc.isResumable()){
					editMetaFile();
				}
			}catch(Exception e){
				System.out.println("Cant put master into sleep");
			}
		}
		renameCompletedFile();
	}
	public void renameCompletedFile(){
		if(rc.isResumable()){
			File m = new File(fn + "META");
			m.delete();
		}
		File f  = new File(fn + "CRDOWNLOAD");
		f.renameTo(new File(fn));
	}
	
	public BlockingQueue<Runnable> createJobs(){
		int chunkSize = Integer.parseInt(chunkQueue.get(chunkQueue.size()-1));
		BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(chunkQueue.size()-1);
		for(int i = 0; i < chunkQueue.size()-1; i++){
			Runnable worker = new WorkerThread(chunkSize*(i+1), chunkSize,fn,domain,path,port,chunkQueue.get(i));
			workQueue.add(worker);
		}
		return workQueue;
	}
	public void createMetaFile(){
		if(!headWritten){
			mw = new FileWriter(fn + "META");
			String metaHeader = rc.getHead() + "\r\n\r\n";
			mw.writeHead(metaHeader.getBytes());
			headWritten = true;
		}
	}
	public void editMetaFile(){
		mw.writeError(chunkQueue, headSize);
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
		rc.checkDownloadType();
		rc.checkDoResume();
	}
	
	public void setUpChunks(){
		chunkQueue = rc.getChunk(number_of_workers);
	}
	
	public void connectToHost(){
		try{
			client = new Socket();
			client.connect(new InetSocketAddress(domain, port), 3000);
			client.setSoTimeout(5000);
			out = new DataOutputStream(client.getOutputStream());
			in = new DataInputStream(client.getInputStream());
			
			System.out.println("Connection established");
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
