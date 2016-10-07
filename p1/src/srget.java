import java.util.*;
import java.io.*;
import java.net.*;
public class srget {
	public static void main(String[] args) throws IOException{
		System.out.println(Arrays.toString(args));
		checkArgs(args);
		Downloader d = new Downloader(args[args.length-1]);
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
}
