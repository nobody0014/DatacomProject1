import java.io.*;

//Modified reader for easy purpose and shit
public class ModReader {
	ProcessNormal norm;
	String fn;
	String mn;
	File f;
	File m;
	FileOutputStream outf;
	FileOutputStream outm;
	String errorMsg;
	private  boolean fileCreated;
	boolean CLExist;
	boolean readingDone;
	boolean foundError;
	public ModReader(String fileName){
		fn = fileName + "CRDOWNLOAD";
		mn = fileName + "META";
		fileCreated = false;
		CLExist = false;
		norm = new ProcessNormal();
	}
	public boolean writeIntoFile(byte[] data, int end) throws IOException{
		boolean stop = true;
		if(!norm.getDoneReading()){
			data = norm.process(data, end);
			if(data.length != 0){
				if(!fileCreated){
					createFile();
					creteFileOutputStream();
					fileCreated = true;
					if(norm.getCL() != 0){CLExist = true;}
				}
				outf.write(data);
			}
			stop  = false;
		}
		if(norm.getError()){
			setErrorMsg(norm.getErrorMsg());
			setError(true);
			stop = true;
			outf.close();
		}
		if(norm.getDoneReading()){
			stop = true;
			m.delete();
			System.out.println(f.renameTo(new File(fn.substring(0, fn.length()-10))));
			outf.close();
		}
		return stop;
	}
	public void writeError() throws IOException{
		//A function that is used to write meta file if the download suddenly file
		if(norm.getHeaderFound()){
			String toWrite = norm.getHeader() + "\r\n\r\n" + "Bytes Read: " + norm.getCfl(); 
			outm.write(toWrite.getBytes());
			System.out.println("Error in download, writing meta file and ending the download");
		}
		else{
			System.out.println("Can't even get the header, internet connection is probably bad");
		}
	}
	public void createFile(){
		f = new File(fn);
		m = new File(mn);
	}
	public void creteFileOutputStream(){
		try{
			outf = new FileOutputStream(f);
			outm = new FileOutputStream(m);
		}
		catch(Exception e){
			System.out.println("Apparently the file is not created yet");
		}
	}
	public void deleteMetaFile(){
		m.delete();
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
}
