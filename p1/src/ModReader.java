import java.io.*;
import java.util.*;
import java.nio.charset.*;
import java.nio.file.*;

//Modified reader for easy purpose and shit
public class ModReader {
	Charset c = Charset.forName("US-ASCII"); 
	String fn;
	StringBuilder header;
	File f;
	FileOutputStream out;
	boolean headerFound; // Done
	int cp; //Done
	long contentLength; //Done
	long cfl; //Done 
	long[] progress = {10,20,30,40,50,60,70,80,90,100};
	
	public ModReader(String fileName) throws IOException{
		header = new StringBuilder();
		fn = fileName;
		f = new File(fn);
		out = new FileOutputStream(fn);
		setHeaderFound(false);
		setDefaultCL();
		setDefaultCP();
		setDefaultCfl();
	}

	//Writing into file using fileoutputstream which use byte[]
	//header detection is done here
	public boolean write(byte[] data, int end) throws IOException{
		if(!headerFound){
			header.append(new String(Arrays.copyOfRange(data, 0, end),c));
			
			//Check for completed headers and do necessary operations to extract headers and write body
			checkHeader(data, end);
		}
		else{
			writeData(data,end);
			addCfl(end);
		}
		printMileStone();
		return checkEnd();
	}
	
	
	//Use only when header is not found yet
	//Do necessary functions once headers are found.
	public void checkHeader(byte[] data, int end) throws IOException{
		String tempth = header.toString();
		if(tempth.contains("\r\n\r\n")){
			//These following lines are used to take out the header and extract contentLength
			//split[0] is the whole headers lines, split[1] is the body part 
			String[] split = tempth.split("\r\n\r\n");
			setHeaderFound(true);
			extractContentLength(split[0]);
			
			//find where he body start and write into the file.
			int bodyStart = findBody(tempth,split[0],data,end);
			out.write(data, bodyStart, end-bodyStart);;
			addCfl(end-bodyStart);
		}
	}
	//This part of the code is used to determine where the body start with respect to the data input
	public int findBody(String tempth, String headers, byte[] data, int end){
		//get the size of the body part that came trough along with the header
		int diff = tempth.getBytes(c).length - headers.getBytes(c).length;
		//get the start of the body
		int bodyStart = end - diff;
		//Shave off the 2 crlf if there are some at the start (most probably will)
		for(int i = bodyStart; i < data.length; i++){
			if(data[i] == 13 || data[i] == 10){bodyStart++;}
			else{break;}
		}
		return bodyStart;
	}
	
	
	//I got tired of making big function so i am trying to make it easy to read the big functions
	//This is only used o write into a file (it assumes we already seperate the header)
	public void writeData(byte[] data, int end) throws IOException{
		if(end + getCfl() > contentLength){
			end = (int) (contentLength - cfl); //Change ending for us to read the byte file instead of reading till the end, we read till the contentlength
			out.write(data,0,end);				
		}
		else{
			out.write(data,0,end);
		}
	}
	
	
	
	//Quit the program if the server return error
	public void checkResponse(String h) throws IOException{
		String[] headers = h.split("\r\n");
		if(!headers[0].contains("OK")){
			System.out.println("Either no file was found or the file was moved");
			System.out.println("Quiting the system...");
			System.exit(0);
		}
		System.out.println("Receving positive response from server, writing the file");
	}
	

	//Done when all the header is found
	public void extractContentLength(String h) throws IOException{
		String[] hSplit = h.split("\r\n");
		for(String s : hSplit){
			if(s.contains("Content-Length")){
				String[] cl = s.split(":");
				contentLength = Long.parseLong(cl[1].trim());
			}
		}
		System.out.println("Content Length : " + contentLength);
	}
	//We always let content length be max vlue first until we discover 
	//the contentLength sent back form the header or the file ends with /r/n/r/n
	public boolean checkEnd() throws IOException{
		if(getCfl() >= contentLength){
			return true;
		}
		return false;
	}
	//This is used when file download is incomplete and we cant continue downloading, delete the file
	public void deleteFile() throws IOException, NoSuchFileException{
		f.delete();
	}
	//Close the fileoutputstream
	public void close() throws IOException{
		out.close();
	}
	
	
	
	
	
	//Add method start here
	//Add to the current file length, we dont really want to use f.length
	public void addCfl(int l){
		cfl += l;
	}
	//just in case, most likely wont happen
	public void addCfl(long l){
		cfl += l;
	}
	//Increment cp, cp is what we used to track the progress of the download
	public void incCP(){
		cp++;
	}
	
	//Set methods
	//Called to set the contentlength, can be both long and int
	public void setCL(long l){
		contentLength = l;
	}
	public void setCL(int l){
		contentLength = l;
	}
	public void setHeaderFound(boolean s){
		headerFound  = s;
	}
	
	//Set default methods are used to initialised certain global variables
	public void setDefaultCfl(){
		cfl = 0;
	}
	public void setDefaultCP(){
		cp = 0;
	}
	//Default CL is used to initialised contentLength before we are getting the header.
	public void setDefaultCL(){
		contentLength = Integer.MAX_VALUE;
	}
	
	//Print method, this is just used to print to progress for every 10% to the user.
	public void printMileStone(){
		if((float)getCfl()/(float)getCL() * 100 > progress[getCP()] ){
			System.out.println("Downloading: " + progress[getCP()] + "%");
			incCP();
		}
	}
	
	//Get method start here
	public boolean foundHeader(){
		return headerFound;
	}
	public long getCfl(){
		return cfl;
	}
	public int getCP(){
		return cp;
	}
	public long getCL(){
		return contentLength;
	}
	
}
