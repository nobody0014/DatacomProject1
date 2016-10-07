import java.util.*;
import java.io.*;
import java.net.*;
public class HeadProc {
	
	
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
		return toReturn;
	}
}
