import java.util.*;
import java.io.*;
import java.net.*;

public class Downloader {
	
	//global var that we will use
	Socket client;
	DataInputStream in;
	DataOutputStream out;
	ModReader mod;
	String domain;
	String absUrl;
	String path;
	HeadProc h;
	int port = 80;
	
	public Downloader(URL hostInfo,String absUrl){
		domain = hostInfo.getHost();
		path = hostInfo.getPath();
		this.absUrl = absUrl;
		if(hostInfo.getPort() != -1){
			port  = hostInfo.getPort();
		}
	}
	
	//Sending request out (this is a rather simple one)
	public void sendReq(String req) throws IOException{
		out = new DataOutputStream(client.getOutputStream());
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
	public void readInput(String fileName) throws IOException{
		mod = new ModReader(fileName);
		in = new DataInputStream(client.getInputStream());
		int currentByte = 0;
		byte[] currentData = new byte[8192];
		while(currentByte != -1){
			try{
				currentByte = in.read(currentData);
			}catch(Exception e){
				System.out.println("Connection Timeout");
			}
			if(mod.write(currentData,currentByte)){
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
