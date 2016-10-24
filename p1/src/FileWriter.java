import java.util.*;
import java.io.*;

public class FileWriter {
	private File f;
	private RandomAccessFile raf;
	public FileWriter(String fn){
		f = new File(fn);
		createRandomAccessFile();
	}
	
	public void write(byte[] data, int start){
		try{
			raf.seek(start);
			raf.write(data);
		}catch(Exception e){
			System.out.println("Writing failed" );
		}
	}
	public void write(byte[] data, long start){
		try{
			raf.seek(start);
			raf.write(data);
		}catch(Exception e){
			System.out.println("Writing failed" );
		}
	}
	public void write(byte[] data, int start, int end){
		try{
			raf.seek(start);
			raf.setLength(raf.length() + end);
			raf.write(data);
		}catch(Exception e){
			System.out.println("Writing failed" );
		}
	}
	public void write(byte[] data, long start, long end){
		try{
			raf.seek(start);
			raf.setLength(raf.length() + end);
			raf.write(data);
		}catch(Exception e){
			System.out.println("Writing failed" );
		}
	}
	public void write(byte[] data, int start, long end){
		try{
			raf.seek(start);
			raf.setLength(raf.length() + end);
			raf.write(data);
		}catch(Exception e){
			System.out.println("Writing failed" );
		}
	}
	
	public void writeHead(byte[] data){
		try{
			raf.setLength(data.length);
			raf.seek(0);
			raf.write(data);
		}catch(Exception e){
			System.out.println("Writing failed" );
		}
	}
	public void writeError(ArrayList<String> chunks, ArrayList<String> chunkInfor, int startByte){
		String[] statusStrings = arraysToString(chunks,chunkInfor);
		String s = statusStrings[0];
		if(!statusStrings[1].equals("false")){
			try{
				raf.setLength(startByte  + s.getBytes().length);
				raf.seek(startByte);
				raf.write(s.getBytes());
			}catch(Exception e){
				System.out.println("Writing Error failed" );
			}
		}
		
	}
	public void writeError(ArrayList<String> chunks, ArrayList<String> chunkInfor, long startByte) {
		String[] statusStrings = arraysToString(chunks,chunkInfor);
		String s = statusStrings[0];
		if(statusStrings[1].equals("false")){
			try{
				raf.setLength(startByte  + s.getBytes().length);
				raf.seek(startByte);
				raf.write(s.getBytes());
			}catch(Exception e){
				System.out.println("Writing Error failed" );
			}
		}
	}
		
	public String[] arraysToString(ArrayList<String> chunks, ArrayList<String> chunkInfor){
		String[] toReturn = new String[2];
		StringBuilder sb = new StringBuilder();
		boolean errorGettingChunk = false;
		try{
			for(String c : chunks){
				sb.append(new String(c + " "));
			}
			for(String c: chunkInfor){
				sb.append(new String(c + " "));
			}
			toReturn[0] = sb.substring(0, sb.length()-1);
		}catch(Exception e){errorGettingChunk = true;}
		toReturn[1] = String.valueOf(errorGettingChunk);
		return toReturn;
	}
	
	private void createRandomAccessFile(){
		try{
			raf = new RandomAccessFile(f, "rw");
		}catch(Exception e){
			System.out.println("Error while making random access file");
		}
	}
	
	
}
