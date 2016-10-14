import java.io.*;
import java.util.*;
import java.nio.charset.*;

//Modified reader for easy purpose and shit
public class ProcessNormal {
	Charset c = Charset.forName("US-ASCII"); 
	String errorMsg;
	StringBuilder header;
	String headerStr;
	ProcessCTE cte;
	
	boolean isCTE; //To check if the file transfer is CTE
	boolean headerFound; // Done --> technically this is used for determining CTE or normal download
	boolean doneReading;
	boolean errorRecv;
	boolean clExist;
	
	int cp; //Done --> current progress --> a percentage (alr times 100) of how much file is downloaded (not usuable when CTE)

	long contentLength; //Done --> this is used only when it's normal download (chunkSize for CTE)
	long cfl; //Done --> Keep the current File length --> usually use it to ending downloading for normal download (but ill also add it during CTE)
	long[] progress = {10,20,30,40,50,60,70,80,90,100}; // --> this is used in conjuncture with the cp
	
	
	
	//Contructor
	public ProcessNormal(){
		errorMsg = new String();
		headerStr = new String();
		header = new StringBuilder();
		setError(false);
		setCTE(false);
		setHeaderFound(false);
		setDoneReading(false);
		setDefaultCL();
		setDefaultCP();
		setDefaultCfl();
	}

	//Write methods (only write will be called from other class)
	public byte[] process(byte[] data, int end) throws IOException{
		if(getCTE()){
			data = cte.processCTE(data,0,end);
		}else{
			data  = processNormal(data,end);
		}
		addCfl(data.length);
		checkEnd();
		return data;
	}
	
	//This is just for normal writing and also to find the main header
	private byte[] processNormal(byte[] data, int end) throws IOException{
		if(!getHeaderFound()){
			header.append(new String(Arrays.copyOfRange(data, 0, end),c));
			data = checkHeader(data, end);
		}
		else{
			data = processData(data,end);
			if(getCLExist()){
				printProgress();
			}
		}
		return data;
	}

	
	private byte[] processData(byte[] data, int end) throws IOException{
		if(end + getCfl() >= getCL()){
			end = (int) (getCL() - getCfl()); //Change ending for us to read the byte file instead of reading till the end, we read till the contentlength
		}
		return Arrays.copyOfRange(data, 0, end);
	}
	private byte[] processExtraBody(byte[] data, int start, int end) throws IOException{
		if(getCTE()){
			data = cte.processCTE(data,start, end); //write= takes the start and the end 
		}
		else{
			//write actually takes in offset and length so ill have to minus to get the length
			data = Arrays.copyOfRange(data, start, end);
		}
		return data;
	}
	//End of writing methods
	
	
	
	//Finding methods
	//This part of the code is used to determine where the body start with respect to the data input
	private int findBody(String tempth, String headers, byte[] data, int end){
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
	private void extractHeader(String h) throws IOException{
		if(h.contains("Transfer-Encoding: chunked")){
			setCTE(true);
			cte = new ProcessCTE();
		}
		else{
			extractCL(h.split("\r\n\r\n")[0]);
		}
		setHeaderFound(true);
	}
	
	//Extract the content length for normal downloading
	private void extractCL(String h){
		String[] hSplit = h.split("\r\n");
		for(String s : hSplit){
			if(s.contains("Content-Length")){
				String[] cl = s.split(":");
				setCL(Long.parseLong(cl[1].trim()) + getCfl());
				setCLExist(true);
			}
		}
		if(getCLExist()){
			System.out.println("Content Length: " + getCL());
		}
		else{
			System.out.println("Content Length does not exist");
		}
		
	}
	//End extraction Methods
	
	//Check Methods
	
	//Use only when header is not found yet
	//Do necessary functions once headers are found.
	private byte[] checkHeader(byte[] data, int end) throws IOException{
		String tempth = header.toString();
		if(tempth.contains("\r\n\r\n")){
			//These following lines are used to take out the header and extract contentLength
			//split[0] is the whole headers lines, split[1] is the body part 
			String[] split = tempth.split("\r\n\r\n");
			setHeader(split[0]);
			checkResponse(split[0]);
			extractHeader(split[0]);
			setHeaderFound(true);
			
			//find where he body start and write into the file.
			int bodyStart = findBody(tempth,split[0],data,end);
			data = processExtraBody(data, bodyStart, end);
		}
		return data;
	}
	
	private void checkResponse(String h){
		String[] headers = h.split("\r\n");
		setError(checkError(headers[0]));
	}
	
	public boolean checkError(String response){
		int responseCode = Integer.parseInt(response.split(" ")[1]);
		if(responseCode >= 300 && responseCode <= 600) {
			if(responseCode >= 300 && responseCode <= 399){
				setErrorMsg("The file requested has been either redirected and since the client is not programmed to handle it yet we will quit");
			}
			else if(responseCode >= 400 && responseCode <= 499){
				setErrorMsg("The request contains incorrect syntax or the the file name was incorrect");
			}
			else if(responseCode >= 500 && responseCode <= 599){
				setErrorMsg("The server probably cant find the file or the file has been moved or removed");
			}
			return true;
		}
		return false;
	}
	
	
	private void printProgress(){
//		for(int i = 0; i < 101; i++) {
//		      try{
//		        Thread.sleep(100);
//		      }catch (Exception ex){
//		        ex.printStackTrace();
//		      }
//		      System.out.printf("%s \r", Integer.toString(i) + "%");
//		}
		float perc = (float)getCfl()/(float)getCL() * 100 ;
		
		if(perc >= progress[getCP()]){
			String progress = "Progress: " + (int) perc + "%";
			System.out.println(progress);
			incCP();
		}
	}
	
	//We always let content length be max vlue first until we discover 
	//the contentLength sent back form the header or the file ends with /r/n/r/n
	private void checkEnd(){
		if(getCTE()){
			setDoneReading(cte.getDoneReading());
		}
		else if(getCLExist() && getCfl() >= getCL()){
			setDoneReading(true);
		}
	}
	
	//Add method start here
	//Add to the current file length, we dont really want to use f.length
	public void addCfl(int l){
		if(getCL() != 0){
			cfl += l;
		}
	}
	//just in case, most likely wont happen
	public void addCfl(long l){
		if(getCL() != 0){
			cfl += l;
		}
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
	public void setCTE(boolean s){
		isCTE = s;
	}
	public void setDoneReading(boolean b){
		doneReading = b;
	}
	public void setError(boolean b){
		errorRecv = b;
	}
	public void setErrorMsg(String err){
		errorMsg = err;
	}
	public void setCLExist(boolean b){
		clExist = b;
	}
	public void setHeader(String s){
		headerStr = s;
	}
	public void setCfl(String s){
		cfl = Long.parseLong(s.trim());
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
	
	public boolean getDoneReading(){
		return doneReading;
	}
	public boolean getError(){
		return errorRecv;
	}
	public String getErrorMsg(){
		return errorMsg;
	}
	public boolean getCLExist(){
		return clExist;
	}
	public String getHeader(){
		return headerStr;
	}

}
