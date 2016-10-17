import java.io.*;
import java.util.Arrays;

//Modified reader for easy purpose and shit
public class ModReader {
	private ProcessNormal norm;
	private String fn;
	private String mn;
	private String byteStart;
	private String errorMsg;
	private String resumeHead;
	
	private File f;
	private File m;
	private RandomAccessFile raff;
	private RandomAccessFile rafe;
	private boolean CLExist;
	private boolean foundError;
	private boolean firstTimeWriteError;
	private boolean isResumable;
	private int rafeStart;
	
	
	public ModReader(String fileName){
		fn = fileName + "CRDOWNLOAD";
		mn = fileName + "META";
		firstTimeWriteError = true;
		CLExist = false;
		norm = new ProcessNormal();
	}
	public boolean write(boolean isHead,byte[] data, int end) throws IOException{
		if(isHead){return writeHead(data,end);}
		else{return writeIntoFile(data,end);}
	}
	public boolean writeIntoFile(byte[] data, int end) throws IOException{
		boolean stop = true;
		if(!norm.getDoneReading()){
			data = norm.process(data, end);
			if(data.length != 0){
				if(norm.getCL() != 0){CLExist = true;}
				rafWrite(data,norm.getCfl()-data.length,raff);
				setResumable((norm.getCL() != 0) && !norm.getCTE());
				writeError();
			}
			stop  = false;
		}
		if(norm.getError()){
			setErrorMsg(norm.getErrorMsg());
			setError(true);
			stop = true;
			raff.close();
			rafe.close();
		}
		if(norm.getDoneReading()){
			stop = true;
			renameFinalFile();
			deleteMetaFile();
			raff.close();
			rafe.close();
		}
		return stop;
	}
	public boolean writeHead(byte[] data, int end) throws IOException{
		boolean stop = false;
		resumeHead += new String(Arrays.copyOfRange(data, 0, end));
		if(resumeHead.contains("\r\n\r\n")){
			setResumable(processResumeHead());
			stop = true;
		}
		return stop;
	}
	//check if we can resume
	public boolean processResumeHead() throws IOException{
		boolean r = false;
		String[] metaSections = splitHead(getOldFile(), "\r\n\r\n");
		if(metaSections.length > 1){
			String bs = metaSections[1].split(": ")[1];
			setByteStart(bs.substring(0, bs.length()-2));
			norm.setCfl(bs);
		}
		else{
			setByteStart("0");
		}
		String[] split = splitHead(resumeHead,"\r\n");
		if(norm.checkError(split[0])){
			System.out.println("Error while requesting for HEAD");
			r = false;
		}
		else if(!findRange(resumeHead)){
			System.out.println("The server do not accept resumable");
			r = false;
		}
		else{
			r = compareHead(splitHead(metaSections[0], "\r\n"),split);
		}
		return r;
	}
	
	//Store start byte and return the old head --> just the head
	public String getOldFile() throws IOException{
		FileInputStream fis = new FileInputStream(m);
		byte[] data = new byte[(int) m.length()];
		fis.read(data);
		fis.close();
		String oldFile = new String(data, "UTF-8");
		return oldFile;
	}
	//Compare the requested head and the kept head, check for modification
	public boolean compareHead(String[] oldHead, String[] newHead){
		String oldETag = "";
		String oldDate = "";
		String oldCL = "";
		boolean foundOldETag = false;
		boolean foundOldDate = false;
		boolean foundOldCL = false;
		String newETag = "";
		String newDate = "";
		String newCL = "";
		boolean foundNewETag = false;
		boolean foundNewDate = false;
		boolean foundNewCL = false;
		boolean r = false;
		
		for(String s : oldHead){
			if(!foundOldETag && s.contains("ETag")){
				oldETag = s.split(": ")[1];
				foundOldETag = true;
			}
			else if (!foundOldDate && s.contains("Last-Modified")){
				oldDate = s.split(": ")[1];
				foundOldDate = true;
			}
			else if (!foundOldCL && s.contains("Content-Length")){
				oldCL = s.split(": ")[1];
				foundOldCL = true;
			}
		}
		for(String s : newHead){
			if(!foundNewETag && s.contains("ETag")){
				newETag = s.split(": ")[1];
				foundNewETag = true;
			}
			else if (!foundNewDate && s.contains("Last-Modified")){
				newDate = s.split(": ")[1];
				foundNewDate = true;
			}
			else if (!foundNewCL && s.contains("Content-Length")){
				newCL = s.split(": ")[1];
				foundNewCL = true;
			}
		}
		
		//First we check  both ETags 
		//Second, if we wasnt able to find both ETags we resort to Last-Modified 
		//Pretty much if u was not able to find Both ETags and Last-Modifieds then we go and check with CL
		if(norm.checkError(oldHead[0])){
			setByteStart("0");
			r = false;
		}
		else if((foundOldETag && foundNewETag) && (newETag.equals(oldETag))){
			r = true;
		}
		else if(!(foundOldETag && foundNewETag) && (foundOldDate && foundNewDate) && (newDate.equals(oldDate))) {
			r = true;
		}
		else if(!(foundOldETag && foundNewETag) && !(foundOldDate && foundNewDate) && (foundOldCL && foundNewCL) && (newCL.equals(oldCL))) {
			r = true;
		}
		return r;
		
	}
	public boolean findRange(String head){
		return head.contains("Accept-Ranges");
	}
	public String[] splitHead(String head, String regex){
		return head.split(regex);
	}
	
	
	//A function that is used to write meta file if the download suddenly file
	public void writeError() throws IOException{
		if(isResumable()){
			StringBuilder toWrite = new StringBuilder();
			if(firstTimeWriteError){
				firstTimeWriteError = false;
						
				toWrite.append(norm.getHeader() + "\r\n\r\n");
				toWrite.append("Bytes Read: ");
				rafeStart = toWrite.toString().getBytes().length;
				
				
				toWrite.append(norm.getCfl() + "\r\n"); 
				rafWrite(toWrite.toString().getBytes(), 0, rafe);
			}
			else{
				toWrite.append(norm.getCfl() + "\r\n");
				rafWrite(toWrite.toString().getBytes(),rafeStart,rafe);
			} 
		}
		else if(!isResumable() && m.exists()){
			deleteMetaFile();
		}
	}
	
	
	public void rafWrite(byte[] data, long startFile, RandomAccessFile r){
		try{
			r.seek(startFile);
			r.setLength(data.length+startFile);
			r.write(data);
		}catch(Exception e){
			System.out.println("Writing failed" );
		}
	}
	
	public boolean checkDoResume(){
		if(f.isFile() && m.isFile()){
			byteStart = "";
			resumeHead = "";
			createFileOutputStream(true);
			return true;
		}
		else{
			createFileOutputStream(false);
		}
		return false;
	}
	public void createFile(){
		f = new File(fn);
		m = new File(mn);
	}
	public void createFileOutputStream(boolean b){
		try{
			raff = new RandomAccessFile(f, "rw");
			rafe = new RandomAccessFile(m, "rw");
		}
		catch(Exception e){
			System.out.println("Apparently the file is not created yet");
		}
	}
	public void renameFinalFile(){
		f.renameTo(new File(fn.substring(0, fn.length()-10)));
	}
	public void deleteMetaFile(){
		m.delete();
	}
	public void setResumable(boolean b){
		isResumable = b;
	}
	public void setByteStart(String s){
		byteStart = s;
	}
	public void setError(boolean b){
		foundError = b;
	}
	public void setErrorMsg(String s){
		errorMsg = s;
	}
	public boolean getError(){
		return foundError;
	}
	public String getErrorMsg(){
		return errorMsg;
	}
	public boolean CLExist(){
		return CLExist;
	}
	public String getByteStart(){
		return byteStart;
	}
	public boolean isResumable(){
		return isResumable;
	}
}
