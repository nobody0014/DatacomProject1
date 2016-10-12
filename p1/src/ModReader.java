import java.io.*;

//Modified reader for easy purpose and shit
public class ModReader {
	ProcessNormal norm;
	String fn;
	File f;
	FileOutputStream out;
	String errorMsg;
	private  boolean fileCreated;
	boolean readingDone;
	boolean foundError;
	public ModReader(String fileName){
		fn = fileName;
		fileCreated = false;
		norm = new ProcessNormal();
	}
	public boolean writeIntoFile(byte[] data, int end) throws IOException{
		
		boolean stop = true;
		if(norm.getError()){
			setErrorMsg(norm.getErrorMsg());
			setError(true);
			stop = true;
		}
		else if(!norm.getDoneReading()){
			data = norm.process(data, end);
			if(data.length != 0){
				if(!fileCreated){
					createFile();
					creteFileOutputStream();
					fileCreated = true;
				}
				out.write(data);
			}
			stop  = false;
		}
		if(norm.getDoneReading()){
			stop = true;
		}
		return stop;
	}
	public void createFile(){
		f = new File(fn);
	}
	public void creteFileOutputStream(){
		try{
			out = new FileOutputStream(f);
		}
		catch(Exception e){
			System.out.println("Apparently the file is not created yet");
		}
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
}
