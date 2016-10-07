import java.util.*;
import java.io.*;
import java.net.*;
public class Downloader {
	
	//global var that we will use
	Socket client;	
	String servName;
	String path;
	
	//standard http port is 80
	int port = 80;
	
	//Constructors
	public Downloader(String servName, String path){
		this.servName = servName;
		this.path = path;
	}
	public Downloader(String servName, String path,int port){
		this(servName,path);
		this.port = port;
	}
	public Downloader(String servName, String path,String port){
		this(servName,path);
		this.port = Integer.parseInt(port);
	}
	
	//Make new socket and connect it, time out if doesnt work
	public void connect(){
		try{
			client = new Socket();
			client.connect(new InetSocketAddress(servName, port), 10000);
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
