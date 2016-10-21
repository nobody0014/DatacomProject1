import java.util.*;

//Modified reader for easy purpose and shit
public class ProcessCTE extends Processor{
	StringBuilder chunkSizeStr;
	boolean chunkSizeFound;
	int dontRead; 
	long chunkSize; 
	long chunkReadSoFar;
	
	public ProcessCTE(){
		super();
		chunkSizeStr = new StringBuilder();
		setDoneReading(false);
		setDontRead(0);
		setChunkReadSoFar(0);
		setChunkSize(Integer.MAX_VALUE);
	}

	public byte[] process(byte[] data, int end){
		data = super.process(data,end);
		checkEnd();
		return data;
	}
	
	public byte[] processData(byte[] data, int start, int end){
		int c = 0;
		byte[] toWrite = new byte[8192];
		for(int i = start; i < end; i++){
			if(getDontRead() == 0){
				if(!isChunkSizeFound()){
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
		return Arrays.copyOfRange(data,start,start+c);
	}	
	
	//Extract the chuncksize
	private void extractChunkSize(){
		String cStr = chunkSizeStr.toString();
		if(cStr.contains("\r\n")){
			setChunkSize(convertStringToHexLong(cStr.substring(0, cStr.length()-2)));
			setChunkSizeFound(true);
			checkEnd();
		}
	}
	
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
	
	public void checkEnd(){
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
	//Get method start here
	public long getChunkSize(){
		return chunkSize;
	}
	public long getChunkReadSoFar(){
		return chunkReadSoFar;
	}
	public int getDontRead(){
		return dontRead;
	}
	public boolean isChunkSizeFound(){
		return chunkSizeFound;
	}
}
