import java.util.ArrayList;
import java.util.concurrent.*;


public class ExtendedThreadPool extends ThreadPoolExecutor {
	private ArrayList<String> chunkQueue; 
	private ArrayList<String> holder;
	
	public ExtendedThreadPool(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ArrayList<String> chunkQueue, ArrayList<String> holder) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		this.chunkQueue = chunkQueue;
		this.holder = holder;
	}
	
	protected void afterExecute(Runnable r, Throwable t) {
     super.afterExecute(r, t);
     WorkerThread wt = (WorkerThread) r;
     if (wt.isErrorWhileDownloading()) {
    	 System.out.println("Error downloading " + wt.getThreadName() + ", throwing it back");
         holder.add(wt.getThreadName());
     }
     else{
    	 chunkQueue.remove(wt.getThreadName());
     }
   }

}
