import java.io.*;
import java.util.*;
import java.nio.charset.*;
import java.nio.file.*;

//Modified reader for easy purpose and shit
public class ModReader {
	Charset c = Charset.forName("US-ASCII"); 
	String fn;
	StringBuilder header;
	StringBuilder chunkSizeStr;
	File f;
	FileOutputStream out;
	
	boolean isCTE; //To check if the file transfer is CTE
	boolean headerFound; // Done --> technically this is used for determining CTE or normal download
	boolean chunkSizeFound;
	boolean doneReading;
	
	int dontRead; // --> use for when reading chunk, so when the chunk ends, we are suppose to skip \r\n which is 2 bytes total
	int cp; //Done --> current progress --> a percentage (alr times 100) of how much file is downloaded (not usuable when CTE)
	long chunkSize; //Making --> this is used when it's Chunk-Transfer-Encoding
	long chunkReadSoFar;
	
	long contentLength; //Done --> this is used only when it's normal download (chunkSize for CTE)
	long cfl; //Done --> Keep the current File length --> usually use it to ending downloading for normal download (but ill also add it during CTE)
	long[] progress = {10,20,30,40,50,60,70,80,90,100}; // --> this is used in conjuncture with the cp
	
	
	
	//Contructor
	public ModReader(String fileName) throws IOException{
		header = new StringBuilder();
		chunkSizeStr = new StringBuilder();
		fn = fileName;
		f = new File(fn);
		out = new FileOutputStream(fn);

		setHeaderFound(false);
		setDoneReading(false);
		setDontRead(0);
		setChunkReadSoFar(0);
		setChunkSize(Integer.MAX_VALUE);
		setDefaultCL();
		setDefaultCP();
		setDefaultCfl();
	}

	
	
	//Write methods (only write will be called from other class)
	
	//Writing into file using fileoutputstream which use byte[]
	public boolean write(byte[] data, int end) throws IOException{
		//different write methods for different file encoding (normal and CTE)
		if(getCTE()){
			writeCTE(data,0,end);
		}
		else{
			writeNormal(data,end);
		}
		printMileStone();
		return checkEnd();
	}
	
	//This is just for normal writing and also to find the main header
	public void writeNormal(byte[] data, int end) throws IOException{
		if(!getHeaderFound()){
			header.append(new String(Arrays.copyOfRange(data, 0, end),c));
			//Check for completed headers and do necessary operations to extract headers and write body
			checkHeader(data, end);
		}
		else{
			writeData(data,end);
			addCfl(end);
		}
	}
	//Writing for chunk encoding
	public void writeCTE(byte[] data, int start, int end) throws IOException{
		//The counter that keeps how much of toWrite we have to write into the file.
		int c = 0;
		//Create a tempt static byte array to keep bytes that should be written into the file
		byte[] toWrite = new byte[8192];
		for(int i = start; i < end; i++){
			//dontRead is a variable asking you to not read this byte.
			if(getDontRead() == 0){
				if(!getChunkSizeFound()){
					chunkSizeStr.append((char)data[i]);
					extractChunkSize();
				}
				else{
					toWrite[c] = data[i];
					incChunkReadSoFar(); 
					checkChunkReadDone();
					c++;
				}
			}
			else{
				decDontRead();
			}
		}	
		//We know that toWrite contains all the stuff thats needed and that the offset is 0 and the length is c, so yup
		out.write(toWrite,0,c);
	}
	//I got tired of making big function so i am trying to make it easy to read the big functions
	//This is only used o write into a file (it assumes we already seperate the header)
	public void writeData(byte[] data, int end) throws IOException{
		if(end + getCfl() > contentLength){
			end = (int) (contentLength - cfl); //Change ending for us to read the byte file instead of reading till the end, we read till the contentlength
		}
		out.write(data,0,end);
	}
	public void writeExtraBody(byte[] data, int start, int end) throws IOException{
		if(getCTE()){
			writeCTE(data,start, end); //writecte takes the start and the end 
		}
		else{
			//write actually takes in offset and length so ill have to minus to get the length
			out.write(data, start, end-start);
			addCfl(end-start);
		}
	}
	
	//End of writing methods
	
	
	
	//Finding methods
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
	
	//End finding
	
	
	//Extraction methods (shit loads of em)
	//Done when all the header is found
	public void extractHeader(String h) throws IOException{
		if(h.contains("Transfer-Encoding: chunked")){
			setCTE(true);
		}
		else{
			extractCL(h.split("\r\n\r\n")[0]);
		}
		setHeaderFound(true);
	}
	
	//Extract the content length for normal downloading
	public void extractCL(String h){
		String[] hSplit = h.split("\r\n");
		for(String s : hSplit){
			if(s.contains("Content-Length")){
				String[] cl = s.split(":");
				contentLength = Long.parseLong(cl[1].trim());
			}
		}	
		System.out.println("Content Length: " + contentLength);
	}
	
	//Extract the chuncksize
	public void extractChunkSize(){
		String cStr = chunkSizeStr.toString();
		//if the reading of the chunk length is done, convert it to long and set chunkSize
		//In addition, it also has to check for the ending of the file
		if(cStr.contains("\r\n")){
			setChunkSize(convertStringToHexLong(cStr.substring(0, cStr.length()-2)));
			setChunkSizeFound(true);
			checkEnd();
		}
	}
	
	
	//End extraction Methods
	
	//Check Methods
	
	//Use only when header is not found yet
	//Do necessary functions once headers are found.
	public void checkHeader(byte[] data, int end) throws IOException{
		String tempth = header.toString();
		if(tempth.contains("\r\n\r\n")){
			//These following lines are used to take out the header and extract contentLength
			//split[0] is the whole headers lines, split[1] is the body part 
			String[] split = tempth.split("\r\n\r\n");
			checkResponse(split[0]);
			extractHeader(split[0]);
			setHeaderFound(true);
			
			//find where he body start and write into the file.
			int bodyStart = findBody(tempth,split[0],data,end);
			writeExtraBody(data, bodyStart, end);
		}
	}
	
	
	
	//Restart the chunk reading profile
	public void checkChunkReadDone(){
		if(getChunkSize() == getChunkReadSoFar()){
			chunkSizeStr = new StringBuilder();
			setChunkReadSoFar(0);
			setChunkSizeFound(false);
			setDontRead(2);
		}
	}
	public void checkResponse(String h) throws IOException{
		String[] headers = h.split("\r\n");
		checkError(headers[0]);
	}
	
	//Quit the program if the server return error
	public void checkError(String response) throws IOException{
		int responseCode = Integer.parseInt(response.split(" ")[1]);
		if(responseCode >= 100 && responseCode <= 199){
			System.out.println("Request has been received and the process is continuing");
		}
		else if(responseCode >= 200 && responseCode <= 299){
			System.out.println("Request recived by the server, understood and accepted");
		}
		else {
			if(responseCode >= 300 && responseCode <= 399){
				System.out.println("The file requested has been either redirected and since the client is not programmed to handle it yet we will quit");
			}
			else if(responseCode >= 400 && responseCode <= 499){
				System.out.println("The request contains incorrect syntax or the the file name was incorrect");
			}
			else if(responseCode >= 500 && responseCode <= 599){
				System.out.println("The server probably cant find the file or the file has been moved or removed");
			}
			//If not sucessful, we exit
			suddenExit();
		}
	}
	
	public long convertStringToHexLong(String hexStr){
		return Long.parseLong(hexStr,16);
	}
	
	//We always let content length be max vlue first until we discover 
	//the contentLength sent back form the header or the file ends with /r/n/r/n
	public boolean checkEnd(){
		if(getCTE()){
			if(getChunkSize() == 0){
				setDoneReading(true);
				return getDoneReading();
			}
		}
		else if(getCfl() >= contentLength){
			setDoneReading(true);
			return getDoneReading();
		}
		return getDoneReading();
	}
	//Use it for when the server send us unsuccessful response
	public void suddenExit() throws IOException{
		deleteFile();
		close();
		System.out.println("Deleting the downloaded file and ending the program");
		System.exit(0);
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
	//Decrementing dontRead
	public void decDontRead(){
		dontRead--;
	}
	public void addChunkReadSoFar(int i){
		chunkReadSoFar += i; 
	}
	public void addChunkReadSoFar(long i){
		chunkReadSoFar += i; 
	}
	public void incChunkReadSoFar(){
		chunkReadSoFar++;
	}
	
	//Print method, this is just used to print to progress for every 10% to the user.
	public void printMileStone(){
		if((float)getCfl()/(float)getCL() * 100 > progress[getCP()] ){
			System.out.println("Downloading: " + progress[getCP()] + "%");
			incCP();
		}
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
	public void setCTE(boolean s){
		isCTE = true;
	}
	public void setChunkSize(int l){
		chunkSize = l;
	}
	public void setChunkSize(long l){
		chunkSize = l;
	}
	public void setChunkSizeFound(boolean s){
		chunkSizeFound = s;
	}
	public void setChunkReadSoFar(long i){
		chunkReadSoFar = i; 
	}
	public void setChunkReadSoFar(int i){
		chunkReadSoFar = i; 
	}
	public void setDontRead(int l){
		dontRead = l;
	}
	public void setDoneReading(boolean b){
		doneReading = b;
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
	
	
	//Get method start here
	public boolean getCTE(){
		return isCTE;
	}
	public long getChunkSize(){
		return chunkSize;
	}
	public boolean getHeaderFound(){
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
	public boolean getChunkSizeFound(){
		return chunkSizeFound;
	}
	public long getChunkReadSoFar(){
		return chunkReadSoFar;
	}
	public int getDontRead(){
		return dontRead;
	}
	
	public boolean getDoneReading(){
		return doneReading;
	}
}
