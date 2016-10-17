import java.io.*;
import java.net.*;

public class Downloader {
	
	//global var that we will use
	Socket client;
	DataInputStream in;
	DataOutputStream out;
	ModReader mod;
	
	
	URL hostInfo;
	String domain;
	String path;
	String fn;
	
	boolean isResume;
	
	HeadProc h;
	
	int port = 80;
	
	public Downloader(String host,String fileName) throws IOException{
		setUpHostInformation(host);
		setUpFiles(fileName);
	}
	//Called to set up anythin host related
	public void setUpHostInformation(String host){
		h = new HeadProc();
		try{
			h.procHost(host);
			hostInfo = h.procHost(host);
		}catch (Exception e){
			System.out.println("Unaccetable host, Quiting program");
			System.exit(0);
		}
		domain = hostInfo.getHost();
		if(hostInfo.getPath() == null || hostInfo.getPath().equals("")){path = "/";}
		else{path = hostInfo.getPath();}
		if(hostInfo.getPort() != -1){port = hostInfo.getPort();}
	}
	//Called during the constructor, set up necesarry file infomation and set if the file is resumable
	public void setUpFiles(String fileName){
		fn = fileName;
		mod = new ModReader(fn);
		mod.createFile();
		setResume(mod.checkDoResume());
	}
	
	//This sendReq will have to check if the file is resumable,else it will call sendReq(req)
	public void sendReq() throws IOException{
		if(isResume()){doResumeProcess();}
		else{sendReq(h.makeDownloadReq(path, domain));}
	}
	//Simple sending request that was made by HeadProc
	public void sendReq(String req) throws IOException{
		int reqS = req.length();
		int sent = out.size();
		while(sent < reqS){
			out.write(req.getBytes(),sent,req.length());
			sent  = out.size();
		}
	}
	//Do the necessary request for getting information for resumable
	public void doResumeProcess() throws IOException{
		String req = h.makeHeadReq(path, domain);
		sendReq(req);
		download(true);
		close();
		connect();
		req = h.makeDownloadReq(path, domain,mod.getByteStart());
		sendReq(req);
	}
	
	
	//Reading input do 2 things, read the incoming files and writing into another file
	//Write into other is done by initializing mod and using it
	//Ending detection and header seperation will be done by mod
	public void download(boolean isHead) throws IOException{
		int currentByte = 0;
		byte[] currentData = new byte[8192];
		while(currentByte != -1){
			try{currentByte = in.read(currentData);}
			catch(Exception e){
				System.out.println("Possible timeout from the server");
				break;
			}
			if(currentByte == -1){
				if(mod.getError()){System.out.println(mod.getErrorMsg());}
				else{System.out.println("Possible disconnection from the server");}
				break;
			}
			if(mod.write(isHead,currentData, currentByte)){
				if(mod.getError()){System.out.println(mod.getErrorMsg());}
				else{System.out.println("Finish Downloading");}
				break;
			}
		}
	}
	//Make new socket and connect it, time out if doesnt work
	public void connect(){
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
	public void close() throws IOException{
		client.close();
	}
	public void setResume(boolean b){
		isResume = b;
	}
	public boolean isResume(){
		return isResume;
	}
	
}
