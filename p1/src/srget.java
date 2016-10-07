import java.util.*;
import java.io.*;
import java.net.*;
public class srget {
	public static void main(String[] args) throws IOException{
		//We are going to use main to check certain parts and throw args into new function
		System.out.println(Arrays.toString(args));
		if(!args[0].equals("-o")){
			System.out.println("Invalid Parameters");
			System.exit(0);
		}
		else{
			String[] destArr = args[args.length-1].split(":");
			String servName = new String();
			//port may not be initialised depending on the input
			int port = 80;
			if(destArr.length < 3){
				servName = args[args.length-1];
			}
			else{
				port = Integer.parseInt(destArr[1]);
				servName += destArr[0] + destArr[2];
			}
			
			//Create Downloader
			Downloader d  = new Downloader(servName,port);
			d.connect();
			
			
			d.close();
		}
	}
}
