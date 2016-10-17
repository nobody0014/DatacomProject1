import java.net.*;
import java.io.*;

public class HeadProc {
	String NL = "\r\n";
	//processed host and return only url, path, and port
	public URL procHost(String servName) throws IOException{
		return new URL(servName);
	}
	
	//Followings makeDownloadReq makes the GET request
	public String makeDownloadReq(String path, String domain){
		String req = "GET " + path + " HTTP/1.1" + NL;
		req += "Host: " + domain  + NL;
		req += "Connection: close" + NL;
		return req + NL;
	}
	
	public String makeDownloadReq(String path, String domain,String startByte){
		String req = "GET " + path + " HTTP/1.1" + NL;
		req += "Host: " + domain  + NL;
		req += "Connection: close" + NL;
		req += "Range: bytes=" + startByte + "-"  + NL;
		return req + NL;
	}
	public String makeDownloadReq(String path, String domain,String startByte, String endByte){
		String req = "GET " + path + " HTTP/1.1" + NL;
		req += "Host: " + domain  + NL;
		req += "Connection: close" + NL;
		req += "Range: bytes=" + startByte + "-" + endByte  + NL;
		return req + NL;
	}
	
	//Make HEAD request that will be used for checking for resumable
	public String makeHeadReq(String path, String domain){
		String req = "HEAD " + path + " HTTP/1.1" + NL;
		req += "Host: " + domain  + NL;
		req += "Connection: close" + NL;
		req += "Range:" + NL;
		return req + NL;
	}
}
