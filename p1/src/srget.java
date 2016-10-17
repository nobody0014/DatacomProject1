import java.io.*;
public class srget {
	public static void main(String[] args) throws IOException{
//		System.out.println(Arrays.toString(args));
		checkArgs(args);
		
		Downloader d = new Downloader(args[args.length-1],args[1]);
		
		System.out.println("Connecting...");
		d.connect();
		System.out.println("Connection established");
		
		
		System.out.println("Sending req...");
		d.sendReq(); //Check for resumable and set up necessary areas for download
		System.out.println("Sent req");
		
		
		d.download(false); //tell the program to download the body of the file 
						
		d.close();
	}
	
	
	
	//A function to check if args inputted in are valid, quit if not
	public static void checkArgs(String[] args){
		if(!args[0].equals("-o")){
			System.out.println("Invalid Parameters: no tag \"-o\"");
			System.exit(0);
		}
		if(!args[args.length-1].contains("http")){
			System.out.println("Invalid Parameters: no http in the url");
			System.exit(0);
		}
	}
}
