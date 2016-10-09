import java.util.*;

public class HeadProc {
	String NL = "\r\n";
	//processed host and return only url, path, and port
	public String[] procHost(String servName){
		String[] arr = servName.split(":");
		String[] toReturn;
		if(arr.length == 2){
			toReturn = new String[2];
			String host = arr[1];
			//magic
			toReturn[0] = host.substring(2, host.length()).split("/")[0];
			toReturn[1] = host.substring(toReturn[0].length()+2, host.length());
		}
		else{
			toReturn = new String[3];
			toReturn[0] = arr[1];
			toReturn[2] = arr[2].split("/")[0];
			toReturn[1] = arr[2].substring(toReturn[2].length(), arr[2].length());
		}
		if(toReturn[1].equals("")){
			toReturn[1] = "/";
		}
		System.out.println(Arrays.toString(toReturn));
		return toReturn;
	}
	
	
	//ask the function to make http request according to the absURL (port is for the server port)
	public String makeDownloadReq(String path, String domain){
		String req = "GET " + path + " HTTP/1.1" + NL;
		req += "Host: " + domain  + NL;
		return req + NL;
	}
}
