import java.io.*;
import java.util.*;
import java.nio.charset.*;

//Modified reader for easy purpose and shit
public class ModReader {
	Charset c = Charset.forName("US-ASCII");
	String fn;
	StringBuilder header;
	File f;
	FileOutputStream out;
	boolean headerFound;
	int currentProg;
	long contentLength;
	long cfl;
	long[] progress = {10,20,30,40,50,60,70,80,90,100};
	
	public ModReader(String fileName) throws IOException{
		fn = fileName;
		headerFound = false;
		out = new FileOutputStream(fn);
		f = new File(fn);
		contentLength = Integer.MAX_VALUE;
		header = new StringBuilder();
		currentProg = 0;
		cfl = 0;
	}

	//Writing into file using fileoutputstream which use byte[]
	//header detection is done here
	public boolean write(byte[] data, int end) throws IOException{
		
		if(!headerFound){
			header.append(new String(Arrays.copyOfRange(data, 0, end),c));
			String tempth = header.toString();
			if(tempth.contains("\r\n\r\n")){
				String[] split = tempth.split("\r\n\r\n");
				headerFound = true;
				extractContentLength(split[0]);
				
				//get the size of the body part that came trough
//				System.out.println(tempth.getBytes(c).length + " " +  split[0].getBytes(c).length);
				int diff = tempth.getBytes(c).length - split[0].getBytes(c).length;
				//get the start of the body
				int bodyStart = end - diff;
//				System.out.println(bodyStart);
				for(int i = bodyStart; i < data.length; i++){
					if(data[i] == 13 || data[i] == 10){bodyStart++;}
					else{break;}
				}
				System.out.println(bodyStart);
				byte[] body = Arrays.copyOfRange(data, bodyStart, end);
				cfl += body.length;
				out.write(body);
//				System.out.println(Arrays.toString(Arrays.copyOfRange(data, split[0].length()+4, end)));
//				System.out.println("Header Done");
//				System.exit(-1);
			}
		}
		else{
			if(end + cfl > contentLength){
				System.out.println(cfl);
				int l = (int) (contentLength - cfl);
				cfl += l;
				System.out.println(cfl);
				System.out.println(l);
				out.write(data,0,l);	
			}
			else{
				out.write(data,0,end);
				cfl += end;
			}
			if((float)cfl/(float)contentLength * 100 > progress[currentProg] ){
				System.out.println("Downloading: " + progress[currentProg] + "%");
				currentProg++;
			}
		}
		return checkEnd();
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
		if(cfl >= contentLength){
			return true;
		}
		return false;
	}
	public void close() throws IOException{
		out.close();
	}
}
