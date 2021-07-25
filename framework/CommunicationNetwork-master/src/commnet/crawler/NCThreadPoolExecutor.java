package commnet.crawler;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NCThreadPoolExecutor {

	private int poolSize = 4;

	private int maxPoolSize = 24;

	private long keepAliveTime = 60;

	private ThreadPoolExecutor threadPool = null;

	private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

	public NCThreadPoolExecutor() {
		threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, queue);
	}

	public void runTask(Runnable task) {
		threadPool.execute(task);
	}

	public void shutDown() {
		threadPool.shutdown();
	}

	/**
	 * Wait Thread execute tasks
	 */
	public void waitFinish() {
		try {
			threadPool.awaitTermination(20, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
