import java.io.*;

public class srget {
	public static void main(String[] args) throws IOException, InterruptedException{
		
		int connections = checkArgs(args);
		System.out.println("Creating the Master Thread...");
		MasterThread master = new MasterThread(args[args.length-1], args[1], connections);
		System.out.println("Master Thread created, Begining the download process...");
		master.beginDownload();
		System.out.println("Download Process Completed...");
	}
	
	//A function to check if args inputted in are valid, quit if not
	public static int checkArgs(String[] args){
		
		if(!args[0].equals("-o")){
			System.out.println("Invalid Parameters: no tag \"-o\"");
			System.exit(0);
		}
		if(!args[args.length-1].contains("http")){
			System.out.println("Invalid Parameters: no http in the url");
			System.exit(0);
		}
		if(args.length > 3){
			if(args[2].equals("-c") && args.length == 4){
				return 5;
			}
			else if(args[2].equals("-c") && args.length == 5){
				try{
					return Integer.parseInt(args[3]);
				}
				catch (Exception e){
					System.out.println("Worker number is not an integer, setting the number to default");
					return 5;
				}
			}
			else{
				System.out.println("Invalid Parameters");
				System.exit(0);
			}
		}
		
		return 5;
	}
}
