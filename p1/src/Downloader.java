import java.util.*;
import java.io.*;
import java.net.*;
public class Downloader {
	
	//global var that we will use
	Socket client;
	DataInputStream in;
	DataOutputStream out;
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
	public void readInput() throws IOException{
		String response  = readHeader();
		System.out.println(response);
	}
	public String readHeader() throws IOException{
		String response = read();
		String[] splitted  = response.split("\r\n\r\n");
		//usually the first if statement would not happen since it would mean that we have read the entire header and body within 8192 bytes
		//Which when we use split, would give us array of length 3 (1 header, 1 body, 1 empty)
		if(splitted.length == 3){
			return response;
		}
		else{
			return response  + readBody();
		}
	}
	public String readBody() throws IOException{
		//This is called when the file is more than 8192 bytes, which happens most of the time.
		String response = read();
		return response;
	}
	public String read() throws IOException{
		in = new DataInputStream(client.getInputStream());
		byte[] currentData = new byte[8192];
		int currentByte = 0;
		int last = 0;
		int totalSoFar = 0;
		String response = "";
		currentByte = in.read(currentData,last,8192-totalSoFar);
		last = totalSoFar;
		while(currentByte != -1 && currentByte != 0){			
			totalSoFar += currentByte;
			String incoming = new String(Arrays.copyOfRange(currentData, last, totalSoFar)); 
			response += incoming;
			if(response.contains("\r\n\r\n")){
				break;
			}
			if(totalSoFar >= 8192){
				currentData = new byte[8192];
				totalSoFar = 0;
			}
			last = totalSoFar;
			currentByte = in.read(currentData,last,8192-totalSoFar);
		}
		return response;
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
