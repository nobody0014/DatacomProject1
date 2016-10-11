import java.util.*;
import java.net.*;
import java.io.*;

public class HeadProc {
	String NL = "\r\n";
	//processed host and return only url, path, and port
	public URL procHost(String servName) throws IOException{
		return new URL(servName);
	}
	
	
	//ask the function to make http request according to the absURL (port is for the server port)
	public String makeDownloadReq(String path, String domain){
		String req = "GET " + path + " HTTP/1.1" + NL;
		req += "Host: " + domain  + NL;
		return req + NL;
	}
}
