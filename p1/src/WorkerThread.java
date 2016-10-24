import java.io.*;
import java.net.*;
import java.util.Arrays;

public class WorkerThread implements Runnable{
	private long byteStart;
	private long currentByte;
	private int chunkSize;
	private boolean isCTE;
	private boolean contentLengthExists;
	
	private String request;
	private String domain;
	private String path;
	private int port;
	
	private Processor dataProcessor;
	private FileWriter fw;
	
	private Socket client;
	private DataInputStream in;
	private DataOutputStream out;
	
	
	private String ThreadName;
	
	@Override
	public void run() {
//		System.out.println("Worker Name: " + ThreadName);
		setUp();
//		System.out.println("Finish setting up the work");
		connectToHost();
//		System.out.println("Connected to host");
		sendRequest();
//		System.out.println("Finish Sending request");
		download();
	}
	public  WorkerThread(String fn, String domain, String path, int port, String threadName){
		this.ThreadName = threadName;
		this.fw = new FileWriter(fn);
		this.domain = domain;
		this.path = path;
		this.port = port;
		this.request = createRequest(domain, path);
		this.contentLengthExists = false;
	}
	
	public  WorkerThread(long byteStart, int chunkSize, String fn, String domain, String path, int port, String threadName){
		this(fn,domain,path,port,threadName);
		
		this.byteStart = byteStart;
		this.currentByte = byteStart;
		this.chunkSize = chunkSize;	
		this.contentLengthExists = true;
		this.request = createRequest(domain,path,byteStart, byteStart+chunkSize);
	}
	public  WorkerThread(long byteStart, String fn, String domain, String path, int port, String threadName){
		this(fn,domain,path,port,threadName);
		
		this.byteStart = byteStart;
		this.currentByte = byteStart;
		this.contentLengthExists = true;
		this.request = createRequest(domain,path,byteStart);
	}
	
	public  WorkerThread(boolean isCTE, String fn, String domain, String path, int port, String threadName){
		this(fn,domain,path,port,threadName);
		
		this.contentLengthExists = false;
		this.isCTE = isCTE;
	}
	
	
	public void setUp(){
		if(isCTE()){
			dataProcessor = new ProcessCTE();
		}
		else{
			if(chunkSize != 0){
				dataProcessor = new ProcessNormal(chunkSize);
			}
			else{
				dataProcessor = new ProcessNormal();
			}
		}
	}
	
	
	public void download(){
		int byteRead = 0;
		byte[] currentData = new byte[8192];
		while(true){
			try{byteRead = in.read(currentData);}
			catch(Exception e){
				System.out.println("Possible timeout from the server");
				break;
			}
			if(dataProcessor.getErrorStatus()){
				System.out.println(dataProcessor.getErrorMsg());
				break;
			}
			if(byteRead != -1){
				byte[] dataToWrite = dataProcessor.process(currentData, byteRead);
				if(!contentLengthExists || isCTE){
					fw.write(dataToWrite, currentByte, currentByte + dataToWrite.length);
				}
				else{
					if(chunkSize != 0 && dataToWrite.length + currentByte > byteStart+chunkSize){
						dataToWrite = Arrays.copyOfRange(dataToWrite, 0, (int) (byteStart+chunkSize - currentByte));
					}
					fw.write(dataToWrite, currentByte);
				}
				currentByte += dataToWrite.length;
			}
			else{
				break;
			}
		}
	}
	
	public void sendRequest(){
		try{
			if(chunkSize != 0){
				out.write(this.request.getBytes());
			}
			else{
				out.write(this.request.getBytes());
			}
		}catch(Exception e){
			System.out.println("Error sending request to the Server");
		}
		
	}
	public String createRequest(String domain, String path){
		return HeadProc.makeDownloadReq(path, domain);
	}
	public String createRequest(String domain, String path, long byteStart){
		return HeadProc.makeDownloadReq(path, domain,byteStart);
	}
	public String createRequest(String domain, String path, long byteStart, long byteEnd){
		return HeadProc.makeDownloadReq(path, domain,byteStart,byteEnd);
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
	public boolean isCTE(){
		return isCTE;
	}
	
	public String getThreadName(){
		return ThreadName;
	}
	
}
