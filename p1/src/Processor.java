import java.util.*;
import java.nio.charset.*;

public abstract class Processor {
	Charset c = Charset.forName("US-ASCII"); 
	String headerStr;
	String errorMsg;
	StringBuilder header;
	boolean errorRecv;
	boolean doneReading;
	boolean headerFound;

	public Processor(){
		headerStr = new String();
		header = new StringBuilder();
		
		setErrorStatus(false);
		setHeaderFound(false);
		setDoneReading(false);
	}
	public byte[] process(byte[] data, int end){
		if(!isHeaderFound()){
			header.append(new String(Arrays.copyOfRange(data, 0, end),c));
			data = checkHeader(data, end);
		}
		else{
			data = processData(data,0,end);
		}
		return data;
	}
		
	public byte[] checkHeader(byte[] data, int end){
		String tempth = header.toString();
		if(tempth.contains("\r\n\r\n")){
			String header = tempth.split("\r\n\r\n")[0];
			setHeader(header);
			checkResponse(header);
			setHeaderFound(true);
			
			int bodyStart = findBody(tempth,header,data,end);
			data = processData(data, bodyStart, end);
		}
		return data;
	}
	private void checkResponse(String h){
		errorMsg = HeadProc.checkError(h.split("\r\n")[0]);
		if(errorMsg != null){
			System.out.println(errorMsg);
		}
	}
	
	private int findBody(String tempth, String headers, byte[] data, int end){
		int diff = tempth.getBytes(c).length - headers.getBytes(c).length;
		int bodyStart = end - diff;
		return bodyStart+4;
	}
	
	
	abstract byte[] processData(byte[] data, int start, int end);
	
	
	
	
	//Set methods
	public void setHeaderFound(boolean s){
		headerFound  = s;
	}

	public void setDoneReading(boolean b){
		doneReading = b;
	}
	public void setErrorStatus(boolean b){
		errorRecv = b;
	}
	public void setErrorMsg(String err){
		errorMsg = err;
	}
	public void setHeader(String s){
		headerStr = s;
	}
	
	//Get method start here
	public boolean getErrorStatus(){
		return errorRecv;
	}
	public boolean isHeaderFound(){
		return headerFound;
	}
	public boolean isDoneReading(){
		return doneReading;
	}
	public String getErrorMsg(){
		return errorMsg;
	}
	public String getHeader(){
		return headerStr;
	}

}
