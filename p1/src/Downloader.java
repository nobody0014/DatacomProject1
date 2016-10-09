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
	
	//Constructors
	public Downloader(String[] hostInfo,String absUrl){
		domain = hostInfo[0];
		path = hostInfo[1];
		this.absUrl = absUrl;
		if(hostInfo.length == 3){
			port  = Integer.parseInt(hostInfo[2]);
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
	public void readInput(String fileName) throws IOException{
		mod = new ModReader(fileName);
		in = new DataInputStream(client.getInputStream());
		byte[] currentData = new byte[8192];
		int currentByte = 0;
		
		while(currentByte != -1){	
			currentByte = in.read(currentData);
			if(mod.write(Arrays.copyOfRange(currentData, 0,currentByte))){
				break;
			}
		}
	}

	//Make new socket and connect it, time out if doesnt work
	public void connect(){
		try{
			client = new Socket();
			client.connect(new InetSocketAddress(domain, port), 3000);
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
