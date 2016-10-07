import java.util.*;
import java.io.*;
import java.net.*;
public class Downloader {
	
	//global var that we will use
	Socket client;
	String absUrl;
	String domain;
	String path;
	HeadProc h;
	//standard http port is 80
	int port = 80;
	
	
	
	//Constructors
	public Downloader(String absUrl){
		h = new HeadProc();
		String[] hostInfo = h.procHost(absUrl);
		domain = hostInfo[0];
		path = hostInfo[1];
		if(hostInfo.length == 3){
			port  = Integer.parseInt(hostInfo[2]);
		}
	}

	
	//Make new socket and connect it, time out if doesnt work
	public void connect(){
		try{
			client = new Socket();
			client.connect(new InetSocketAddress(domain, port), 10000);
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
