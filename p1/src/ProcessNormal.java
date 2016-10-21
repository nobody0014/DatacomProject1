import java.util.*;
import java.nio.charset.*;

//Modified reader for easy purpose and shit
public class ProcessNormal extends Processor{
	Charset c = Charset.forName("US-ASCII"); 
	
	boolean contentLengthExists;
	
	long contentLength; 
	long currentFileLength; 	
	
	public ProcessNormal(){
		super();
		setContentLengthExists(false);
		setDefaultContentLength();
	}
	
	public ProcessNormal(int cl){
		super();
		setDefaultCurrentFileLength();
		setContentLength(cl);
		setContentLengthExists(true);
	}

	//Write methods (only write will be called from other class)
	public byte[] process(byte[] data, int end){
		data = super.process(data, end);
		addCfl(data.length);
		checkEnd();
		return data;
	}
	public byte[] processData(byte[] data, int start, int end){
		if(end + getCurrentFileLength() >= getContentLength()){
			end = (int) (getContentLength() - getCurrentFileLength());
		}
		return Arrays.copyOfRange(data, start, end);
	}
	public void checkEnd(){
		if(isContentLengthExists() && getCurrentFileLength() >= getContentLength()){
			setDoneReading(true);
		}
	}
	
	//Add method start here
	public void addCfl(int l){
		if(getContentLength() != 0 && isHeaderFound()){
			currentFileLength += l;
		}
	}
	public void addCfl(long l){
		if(getContentLength() != 0  && isHeaderFound()){
			currentFileLength += l;
		}
	}
	
	//Set methods
	public void setContentLength(long l){
		contentLength = l;
	}
	public void setContentLength(int l){
		contentLength = l;
	}
	public void setContentLengthExists(boolean b){
		contentLengthExists = b;
	}
	public void setCurrentFileLength(String s){
		currentFileLength = Long.parseLong(s.trim());
	}
	
	
	//Set default methods are used to initialised certain global variables
	public void setDefaultCurrentFileLength(){
		currentFileLength = 0;
	}
	public void setDefaultContentLength(){
		contentLength = Long.MAX_VALUE;
	}
	
	
	//Get method start here
	
	public long getCurrentFileLength(){
		return currentFileLength;
	}
	public long getContentLength(){
		return contentLength;
	}
	public boolean isContentLengthExists(){
		return contentLengthExists;
	}

}
