import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MasterThread {
	
	private int number_of_workers;
	private ArrayList<String> chunkQueue; 
	
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
		Queue<Runnable> jobQueue = createJobs();
		ExtendedThreadPool exe = new ExtendedThreadPool(number_of_workers, number_of_workers, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(jobQueue.size()),chunkQueue);		
		for(Runnable r : jobQueue){
			exe.execute(r);
			while(exe.getActiveCount() == exe.getMaximumPoolSize()){
				if(rc.isResumable()){editMetaFile();}
				printProgress();
			}
		}
		exe.shutdown();
		while(!exe.isTerminated()){
			if(rc.isResumable())
				{editMetaFile();}
			printProgress();
		}
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
	
	public BlockingQueue<Runnable> createJobs(){
		String downloadingFileName = fn + "CRDOWNLOAD";
		int chunkSize = Integer.parseInt(chunkQueue.get(chunkQueue.size()-2));
		BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(chunkQueue.size());
		for(int i = 0; i < chunkQueue.size()-3; i++){
			Runnable worker = new WorkerThread(chunkSize*i, chunkSize,downloadingFileName,domain,path,port,chunkQueue.get(i));
			workQueue.add(worker);
		}
		workQueue.add(new WorkerThread( chunkSize * (chunkQueue.size()-3),downloadingFileName,domain,path,port,"C" + (chunkQueue.size()-3)));
		return workQueue;
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
	public void printProgress(){
		if(rc.contentLengthExists()){System.out.printf("%s \r", (((rc.getTotalChunks()-chunkQueue.size()+1)/(float)rc.getTotalChunks())*100) + "%");}
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
