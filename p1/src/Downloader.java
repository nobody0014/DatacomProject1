import java.util.*;
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
	HeadProc h;
	int port = 80;
	
	public Downloader(URL hostInfo,String fileName) throws IOException{
		fn = fileName;
		domain = hostInfo.getHost();
		path = hostInfo.getPath();	
		mod = new ModReader(fn);
		if(hostInfo.getPort() != -1){
			port  = hostInfo.getPort();
		}
		this.hostInfo = hostInfo;
	}
	
	//Sending request out (this is a rather simple one)
	public void sendReq(String req) throws IOException{
		
		int reqS = req.length();
		int sent = out.size();
		while(sent < reqS){
			out.write(req.getBytes(),sent,req.length());
			sent  = out.size();
		}
	}
	
	//Reading input do 2 things, read the incoming files and writing into another file
	//Write into other is done by initializing mod and using it
	//Ending detection and header seperation will be done by mod
	public void download() throws IOException{
		int currentByte = 0;
		byte[] currentData = new byte[8192];
		while(currentByte != -1){
			try{
				currentByte = in.read(currentData);
			}catch(Exception e){
				System.out.println("Connection Timeout");
			}
			//If just so that the server close and stop sending, end this connection
			checkSuddenDis(currentByte);
			if(mod.write(currentData,currentByte)){break;}
		}
	}
	public void checkSuddenDis(int currentByte) throws IOException{
		if(currentByte == -1){
			System.out.println(currentByte);
			mod.deleteFile();
			mod.close();
			System.out.println("Sudden disconnection from server, deleting the file and ending the program");
			System.exit(0);
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
	
}
