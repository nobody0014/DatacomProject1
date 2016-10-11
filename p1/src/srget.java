import java.util.*;
import java.io.*;
import java.net.*;
public class srget {
	public static void main(String[] args) throws IOException{
		System.out.println(Arrays.toString(args));
		checkArgs(args);
		HeadProc h = new HeadProc();
		URL hostinfo = h.procHost(args[args.length-1]);
		Downloader d = new Downloader(hostinfo,args[1]);
		String makereq = h.makeDownloadReq(d.path, d.domain);
		System.out.print(makereq);
		System.out.println("connecting");
		
		d.connect();
		System.out.println("Sending req");
		
		
		d.sendReq(makereq);
		System.out.println("Sent req");
		System.out.println("Reading Input");
		
		d.download(); //This function will both read and then write input immediately
		
		System.out.println("Done reading");
		System.out.println("Taking input");
		
		
		
		d.close();
	}
	
	
	
	//A function to check if args inputted in are valid, quit if not
	public static void checkArgs(String[] args){
		if(!args[0].equals("-o")){
			System.out.println("Invalid Parameters");
			System.exit(0);
		}
	}
}
