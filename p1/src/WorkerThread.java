import java.io.*;
import java.net.*;

public class WorkerThread implements Runnable{
	private int byteStart;
	private int chunkSize;
	private boolean isCTE;
	private boolean contentLengthExists;
	
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
		setUp();
		connectToHost();
		sendRequest();
		download();
	}
	public  WorkerThread(String fn, String domain, String path, int port, String threadName){
		this.ThreadName = threadName;
		this.fw = new FileWriter(fn);
		this.domain = domain;
		this.path = path;
		this.port = port;
	}
	
	public  WorkerThread(int byteStart, int chunkSize, String fn, String domain, String path, int port, String threadName){
		this(fn,domain,path,port,threadName);
		
		this.byteStart = byteStart;
		this.chunkSize = chunkSize;		
	}
	
	public  WorkerThread(boolean isCTE, String fn, String domain, String path, int port, String threadName){
		this(fn,domain,path,port,threadName);
		
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
		int currentByte = 0;
		byte[] currentData = new byte[8192];
		while(true){
			try{currentByte = in.read(currentData);}
			catch(Exception e){
				System.out.println("Possible timeout from the server");
				break;
			}
			if(dataProcessor.getErrorStatus()){
				System.out.println(dataProcessor.getErrorMsg());
				break;
			}
			if(currentByte != -1){
				byte[] dataToWrite = dataProcessor.process(currentData, currentByte);
				if(contentLengthExists || isCTE){
					fw.write(dataToWrite, 0, dataToWrite.length);
				}
				else{
					fw.write(dataToWrite, byteStart);
				}
				byteStart += dataToWrite.length;
			}
			else{
				break;
			}
		}
		System.out.println(ThreadName + " has completed its download");
	}
	
	public void sendRequest(){
		try{
			if(chunkSize != 0){
				out.write(HeadProc.makeDownloadReq(path, domain,byteStart, byteStart+chunkSize).getBytes());
			}
			else{
				out.write(HeadProc.makeDownloadReq(path, domain).getBytes());
			}
		}catch(Exception e){
			System.out.println("Error sending request to the Server");
		}
		
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
	public boolean isCTE(){
		return isCTE;
	}
	
	public String getThreadName(){
		return ThreadName;
	}
}
