import java.io.*;
import java.util.*;

//Modified reader for easy purpose and shit
public class ModReader {
	String fn;
	File f;
	FileOutputStream out;
	RandomAccessFile raf;
	boolean headerFound;
	long contentLength;
	
	public ModReader(String fileName) throws IOException{
		fn = fileName;
		headerFound = false;
		out = new FileOutputStream(fn);
		f = new File(fn);
		raf = new RandomAccessFile(f, "r");
		contentLength = Integer.MAX_VALUE;
	}

	//Writing into file using fileoutputstream which use byte[]
	//header detection is done here
	public boolean write(byte[] data) throws IOException{
		if(!headerFound){
			String endDetector = new String(data);
			if(endDetector.contains("\r\n\r\n")){
				String[] split = endDetector.split("\r\n\r\n");
				out.write(split[1].getBytes());
				extractContentLength(split[0]);
				headerFound = true;
			}
		}
		else{
			if(data.length + f.length() >= contentLength){
				int l = (int) (contentLength - f.length());
				out.write(data,0,l);	
			}
			else{
				out.write(data);
			}
		}
		return checkEnd();
	}

	//Done when all the header is found
	public void extractContentLength(String header) throws IOException{
		String[] hSplit = header.split("\r\n");
		for(String s : hSplit){
			if(s.contains("Content-Length")){
				String[] cl = s.split(":");
				contentLength = Integer.parseInt(cl[1].trim());
			}
		}
	}

	//We always let content length be max vlue first until we discover the contentLength sent back form the header or the file ends with /r/n/r/n
	public boolean checkEnd() throws IOException{
		if(f.length() >= contentLength){
			return true;
		}
		return false;
	}
	public void close() throws IOException{
		out.close();
	}
}
