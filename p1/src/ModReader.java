import java.io.*;
import java.util.Arrays;

//Modified reader for easy purpose and shit
public class ModReader {
	String fn;
	String endDetector;
	File f;
	FileOutputStream out;
	RandomAccessFile raf;
	
	boolean headerFound;
	public ModReader(String fileName) throws IOException{
		fn = fileName;
		headerFound = false;
		out = new FileOutputStream(fn);
		f = new File(fn);
		endDetector = new String();
		raf = new RandomAccessFile(f, "r");
	}
	public boolean write(byte[] data) throws IOException{
		System.out.print(new String(data));
		if(!headerFound){
			endDetector += new String(data);
			if(endDetector.contains("\r\n\r\n")){
				headerFound = true;
				String[] split = endDetector.split("\r\n\r\n");
				out.write(split[1].getBytes());
				endDetector = new String();
			}
		}
		else{
			out.write(data);
		}
		return checkEnd();
	}
	public boolean checkEnd() throws IOException{
		byte[] checkEnd = new byte[4];
		raf.seek(f.length() - 4);
		raf.read(checkEnd, 0, 4);
		String check = new String(checkEnd);
		System.out.println(Arrays.toString(checkEnd));
		if(check.equals("\r\n\r\n")){
			return true;
		}
		return false;
	}
	public void close() throws IOException{
		out.close();
	}
}
