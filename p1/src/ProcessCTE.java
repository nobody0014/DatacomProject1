import java.io.*;
import java.util.*;
import java.nio.charset.*;

//Modified reader for easy purpose and shit
public class ProcessCTE{
	Charset c = Charset.forName("US-ASCII"); 
	StringBuilder chunkSizeStr;
	boolean chunkSizeFound;
	boolean doneReading;
	int dontRead; // --> use for when reading chunk, so when the chunk ends, we are suppose to skip \r\n which is 2 bytes total
	long chunkSize; //Making --> this is used when it's Chunk-Transfer-Encoding
	long chunkReadSoFar;
	
	//Contructor
	public ProcessCTE(){
		chunkSizeStr = new StringBuilder();
		setDoneReading(false);
		setDontRead(0);
		setChunkReadSoFar(0);
		setChunkSize(Integer.MAX_VALUE);
	}
	//Write methods (only write will be called from other class)
	
	//Writing for chunk encoding
	public byte[] processCTE(byte[] data, int start, int end) throws IOException{
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
		return Arrays.copyOfRange(data,start,start+c);
	}	
	
	//Extract the chuncksize
	private void extractChunkSize(){
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
	
	//Restart the chunk reading profile
	private void checkChunkReadDone(){
		if(getChunkSize() == getChunkReadSoFar()){
			chunkSizeStr = new StringBuilder();
			setChunkReadSoFar(0);
			setChunkSizeFound(false);
			setDontRead(2);
		}
	}
	
	private long convertStringToHexLong(String hexStr){
		return Long.parseLong(hexStr,16);
	}
	
	//We always let content length be max vlue first until we discover 
	//the contentLength sent back form the header or the file ends with /r/n/r/n
	private void checkEnd(){
		if(getChunkSize() == 0){
			setDoneReading(true);
		}
	}
	
	//Add method start here
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
	//Set methods
	//Called to set the contentlength, can be both long and int
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
	
	//Get method start here
	public long getChunkSize(){
		return chunkSize;
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
