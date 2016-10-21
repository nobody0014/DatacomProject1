import java.io.*;
import java.net.*;
import java.util.*;

public class ResumableChecker {
	private String fn;
	private String mn;
	private String resumeHead;
	private String resumeFile;
	
	private File f;
	private File m;
	
	private boolean filesExist;
	private boolean sameHead;
	private boolean clExists;
	private boolean isCTE;
	private boolean rangeFound;
	
	private long chunkSize;
	private long contentLength;
	
	public ResumableChecker(String domain, String path, int port, String fileName){
		fn = fileName + "CRDOWNLOAD";
		mn = fileName + "META";
		resumeHead = "";
	}
	
	public ArrayList<String> getChunk(int connection_numbers){
		ArrayList<String> chunks;
		if(isResumable()){
			String chunkString = splitHead(resumeFile, "\r\n\r\n")[1];
			chunks = (ArrayList<String>) Arrays.asList(splitHead(chunkString," "));
			chunkSize = Long.parseLong(chunks.get(chunks.size()-1));
		}
		else{
			chunks = splitFileChunks(connection_numbers);
		}
		return chunks;
	}
	private ArrayList<String> splitFileChunks(int cn){
		ArrayList<String> chunks = new ArrayList<>();
		if(contentLength < 1000000){
			chunkSize = 10000;
		}
		else{
			chunkSize = 1000000;
		}
		for(int i = 0; i < contentLength/chunkSize;i++){
			chunks.add(new String("C" + 1));
		}
		if(contentLength%chunkSize != 0){
			chunks.add(new String("C" +(contentLength/chunkSize +1)));
		}
		chunks.add(String.valueOf(chunkSize));
		return chunks;
	}
	
	
	private boolean checkFileExists(){
		f = new File(fn);
		m = new File(mn);
		if(f.isFile() && m.isFile()){
			filesExist =  true;
		}
		else{
			f.delete();
			m.delete();
			filesExist = false;
		}
		return filesExist;
	}
	
	public void checkDoResume(){
		if(checkFileExists()){
			openResumeFile();
			sameHead = processResumeHead();
		}
	}
	
	public void checkDownloadType(){
		if(resumeHead.contains("Content-Length")){
			extractCL();
			rangeFound = resumeHead.contains("Range");
			clExists = true;
		}
		else{
			if(resumeHead.contains("Transfer-Encoding: chunked")){
				isCTE = true;
			}
			else{
				clExists = false;
			}
		}
	}
	private void extractCL(){
		String[] head = splitHead(resumeHead,"\r\n");
		for(String s : head){
			if(s.contains("Content-Length")){
				contentLength = Long.parseLong(s.split(": ")[1]);
				break;
			}
		}
	}
	//Store start byte and return the old head --> just the head
	private void openResumeFile(){
		try{
			FileInputStream fis = new FileInputStream(m);
			byte[] data = new byte[8192];
			int currentByte = 0;
			while(currentByte != -1){
				currentByte = fis.read(data);
				resumeFile += new String(Arrays.copyOfRange(data, 0, currentByte), "UTF-8"); 
				data = new byte[8192];
			}
			fis.close();
		}
		catch(Exception e){
			System.out.println("Can't get Meta file even though the Meta file exists, possible corruption, will do redownloading");
			filesExist = false;
		}
	}
	public void downloadResumeHead(Socket client, DataInputStream in, DataOutputStream out, String headReq){
		try{
			out.write(headReq.getBytes());
			download(in);
		}
		catch(Exception e){
			System.out.println("Unable to write to the server to get Head, Probably not connected, Exiting...");
			System.exit(0);
		}
	}
	private void download(DataInputStream in) throws IOException{
		int currentByte = 0;
		byte[] currentData = new byte[8192];
		while(currentByte != -1){
			try{currentByte = in.read(currentData);}
			catch(Exception e){
				System.out.println("Possible timeout from the server");
				break;
			}
			if(writeHead(currentData,currentByte)){break;}
		}
	}
	private boolean writeHead(byte[] data, int end) throws IOException{
		boolean stop = false;
		resumeHead += new String(Arrays.copyOfRange(data, 0, end));
		if(resumeHead.contains("\r\n\r\n")){
			stop = true;
		}
		return stop;
	}
	
	//check if we can resume
	private boolean processResumeHead() {
		boolean r = false;
		String[] metaSections = splitHead(resumeFile, "\r\n\r\n");
		String[] split = splitHead(resumeHead,"\r\n");
		if(HeadProc.checkError(split[0]) != null){
			System.out.println("Error while requesting for HEAD");
			r = false;
		}
		else if(!rangeFound){
			System.out.println("The server do not accept resumable");
			r = false;
		}
		else{
			r = compareHead(splitHead(metaSections[0], "\r\n"),split);
		}
		return r;
	}
	//Compare the requested head and the kept head, check for modification
	private boolean compareHead(String[] oldHead, String[] newHead){
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
		
		if((foundOldETag && foundNewETag) && (newETag.equals(oldETag))){
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
	
	
	private String[] splitHead(String head, String regex){
		return head.split(regex);
	}

	
	
	public long getContentLength(){
		return contentLength;
	}
	public long getChunkSize(){
		return chunkSize;
	}
	public boolean isCTE(){
		return isCTE;
	}
	public boolean doResumable(){
		return sameHead && filesExist;
	}
	public boolean isResumable(){
		return rangeFound && clExists;
	}
	public String getHead(){
		return resumeHead;
	}
}
