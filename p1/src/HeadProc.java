import java.net.*;
import java.io.*;

public class HeadProc {
	static String NL = "\r\n";
	//processed host and return only url, path, and port
	public static URL procHost(String servName) throws IOException{
		return new URL(servName);
	}
	
	//Followings makeDownloadReq makes the GET request
	public static String makeDownloadReq(String path, String domain){
		String req = "GET " + path + " HTTP/1.1" + NL;
		req += "Host: " + domain  + NL;
		req += "Connection: close" + NL;
		return req + NL;
	}
	
	public static String makeDownloadReq(String path, String domain,long startByte){
		String req = "GET " + path + " HTTP/1.1" + NL;
		req += "Host: " + domain  + NL;
		req += "Connection: close" + NL;
		req += "Range: bytes=" + startByte + "-"+ NL;
		return req + NL;
	}
	public static String makeDownloadReq(String path, String domain,long startByte, long endByte){
		String req = "GET " + path + " HTTP/1.1" + NL;
		req += "Host: " + domain  + NL;
		req += "Connection: close" + NL;
		req += "Range: bytes=" + startByte + "-" + endByte  + NL;
		return req + NL;
	}
	
	//Make HEAD request that will be used for checking for resumable
	public static String makeHeadReq(String path, String domain){
		String req = "HEAD " + path + " HTTP/1.1" + NL;
		req += "Host: " + domain  + NL;
		req += "Connection: close" + NL;
		req += "Range:" + NL;
		return req + NL;
	}
	public static String checkError(String response){
		int responseCode = Integer.parseInt(response.split(" ")[1]);
		if(responseCode >= 300 && responseCode <= 600) {
			String errorMsg = "";
			if(responseCode >= 300 && responseCode <= 399){
				errorMsg = "3:The file requested has been redirected";
			}
			else if(responseCode >= 400 && responseCode <= 499){
				errorMsg = "4:The request contains incorrect syntax or the the file name was incorrect";
			}
			else if(responseCode >= 500 && responseCode <= 599){
				errorMsg = "5:The server probably cant find the file or the file has been moved or removed";
			}
			return errorMsg;
		}
		return null;
	}
	
}
