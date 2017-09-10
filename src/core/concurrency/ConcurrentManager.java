package core.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Gabriel Jadderson on 09-08-2017.
 */
public class ConcurrentManager
{
	
	public void doAsNew(int threads, Runnable runnable)
	{
		ExecutorService es = newUniqueExecutor(threads);
		es.submit(() -> runnable.run());
		es.shutdown();
	}
	
	
	public static void doNew(Runnable runnable)
	{
		
		ExecutorService es = newUniqueExecutor(1);
		es.submit(() -> runnable.run());
		es.shutdown();
	}
	
	
	public void doAsNewThread(String name, Runnable runnable)
	{
		new Thread(() -> runnable.run()).start();
	}
	
	public void doAsNewThreadTim(String name, Runnable runnable)
	{
		//runnable.run();
		new Thread(() -> {
			long startTime = System.nanoTime();
			runnable.run();
			long estimatedTime = System.nanoTime() - startTime;
			System.err.println("[CET] " + name + " took " + estimatedTime / 1e09 + "ms");
		}).start();
	}
	
	public static void doNewThread(Runnable runnable)
	{
		new Thread(() -> runnable.run()).start();
	}
	
	public static ExecutorService newUniqueExecutor(int threads)
	{
		return Executors.newFixedThreadPool(threads);
	}
	
}
