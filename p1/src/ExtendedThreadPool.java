import java.util.ArrayList;
import java.util.concurrent.*;


public class ExtendedThreadPool extends ThreadPoolExecutor {
	private ArrayList<String> chunkQueue; 
	
	public ExtendedThreadPool(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ArrayList<String> chunkQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		this.chunkQueue = chunkQueue;
	}
	protected void afterExecute(WorkerThread r, Throwable t) {
     super.afterExecute(r, t);
     if (t == null) {
    	 chunkQueue.remove(r.getThreadName());
     }
     else{
    	 this.getQueue().add(r);
     }
   }
	

}
