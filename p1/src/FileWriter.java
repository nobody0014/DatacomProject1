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
	public void writeError(ArrayList<String> chunks, int startByte){
		String s = arraysToString(chunks);
		try{
			raf.setLength(startByte  + s.getBytes().length);
			raf.seek(startByte);
			raf.write(s.getBytes());
		}catch(Exception e){
			System.out.println("Writing Error failed" );
		}
	}
	public void writeError(ArrayList<String> chunks, long startByte) {
		String s = arraysToString(chunks);
		try{
			raf.setLength(startByte  + s.getBytes().length);
			raf.seek(startByte);
			raf.write(s.getBytes());
		}catch(Exception e){
			System.out.println("Writing Error failed" );
		}
	}
		
	public String arraysToString(ArrayList<String> chunks){
		StringBuilder s = new StringBuilder();
		try{
			for(String c : chunks){
				s.append(new String(c + " "));
			}
		}catch(Exception e){}//Do nothin, error caused by iterating while trying to delete stuff, basically threads clashing}
		
		return s.substring(0, s.length()-1);
	}
	
	private void createRandomAccessFile(){
		try{
			raf = new RandomAccessFile(f, "rw");
		}catch(Exception e){
			System.out.println("Error while making random access file");
		}
	}
	
	
}
