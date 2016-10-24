import java.util.ArrayList;
import java.util.concurrent.*;


public class ExtendedThreadPool extends ThreadPoolExecutor {
	private ArrayList<String> chunkQueue; 
	
	public ExtendedThreadPool(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ArrayList<String> chunkQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		this.chunkQueue = chunkQueue;
	}
	protected void afterExecute(Runnable r, Throwable t) {
     super.afterExecute(r, t);
     if (t == null) {
    	 WorkerThread wt = (WorkerThread) r;
//    	 System.out.println(chunkQueue.toString());
    	 chunkQueue.remove(wt.getThreadName());
     }
     else{
    	 this.execute(r);
     }
   }

}
