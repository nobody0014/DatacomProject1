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
	public void write(byte[] data, int start, int end){
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
	public void writeError(ArrayList<String> chunks, int startByte){
		try{
			String s = arraysToString(chunks);
			raf.setLength(startByte  + s.getBytes().length);
			raf.seek(startByte);
			raf.write(s.getBytes());
		}catch(Exception e){
			System.out.println("Writing failed" );
		}
	}
	public void writeError(ArrayList<String> chunks, long startByte){
		try{
			String s = arraysToString(chunks);
			raf.setLength(startByte  + s.getBytes().length);
			raf.seek(startByte);
			raf.write(s.getBytes());
		}catch(Exception e){
			System.out.println("Writing failed" );
		}
	}
	public String arraysToString(ArrayList<String> chunks){
		StringBuilder s = new StringBuilder();
		for(String c : chunks){
			s.append(new String(c + " "));
		}
		return s.toString();
	}
	
	private void createRandomAccessFile(){
		try{
			raf = new RandomAccessFile(f, "rw");
		}catch(Exception e){
			System.out.println("Error while making random access file");
		}
	}
	
	
}
