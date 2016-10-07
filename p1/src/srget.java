import java.util.*;
import java.io.*;
import java.net.*;
public class srget {
	public static void main(String[] args) throws IOException{
		System.out.println(Arrays.toString(args));
		checkArgs(args);
		HeadProc h = new HeadProc();
		String[] hostinfo = h.procHost(args[args.length-1]);
		Downloader d = createD(hostinfo);
		d.connect();
		
		
		d.close();
	}
	
	
	
	//A function to check if args inputted in are valid, quit if not
	public static void checkArgs(String[] args){
		if(!args[0].equals("-o")){
			System.out.println("Invalid Parameters");
			System.exit(0);
		}
	}
	//Create Downloader
	public static Downloader createD(String[] hostinfo){
		Downloader d;
		if(hostinfo.length == 2){
			d = new Downloader(hostinfo[0],hostinfo[1]);
		}
		else{
			d = new Downloader(hostinfo[0],hostinfo[1],hostinfo[2]);
		}
		return d;
	}
}
