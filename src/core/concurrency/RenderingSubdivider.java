package core.concurrency;

import core.world.camera.Camera;
import core.world.light.Light;
import core.world.ray.Ray;
import core.world.shapes.BoundingVol;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static core.Globals.HEIGHT;
import static core.Globals.WIDTH;

/**
 * Created by Gabriel Jadderson on 09-08-2017.
 */
public class RenderingSubdivider
{
	
	public static ConcurrentHashMap<Integer, int[]> pixelMap; //this will hold x and y coords
	public static ConcurrentHashMap<Integer, Ray> rayMap; //this will hold rays
	public static ArrayList<ConcurrentNode> nodes;
	
	/**
	 * @param totalPixels should be WIDTH * HEIGHT;
	 */
	public RenderingSubdivider(final int cols, final int rows, final int totalPixels, Camera camera, ArrayList<BoundingVol> boundingList, Light globalLight)
	{
		final int chunks = rows * cols;
		final int pixelsPerChunk = (int) Math.floor((double) totalPixels / (double) chunks);
		
		
		//pixelMap = new ConcurrentHashMap<>();
		//rayMap = new ConcurrentHashMap<>();
		
		nodes = generateNodes(camera);
		
		
		//int[][] tilePixelMap = new int[chunks][pixelsPerChunk];
		ArrayList<ArrayList<ConcurrentNode>> tilePixelMap = new ArrayList<>();
		for (int i = 0; i < chunks; i++)
		{
			tilePixelMap.add(new ArrayList<>()); //populate the list
		}
		
		
		System.out.println("total nodes " + nodes.size());
		
		int tileCount = 0;
		int px = 0;
		for (int j = 0; j < nodes.size(); j++)
		{
			if (px == pixelsPerChunk)
			{
				px = 0;
				if (tileCount < chunks)
				{
					tileCount++; //increment tile count
				}
			} else
			{
				tilePixelMap.get(tileCount).add(nodes.get(j));
				//tilePixelMap[k][px] = j;
				px++;
			}
		}
		
		
		ArrayList<RenderingTile> renderingTiles = new ArrayList<>();
		for (int j = 0; j < chunks; j++)
		{
			renderingTiles.add(new RenderingTile(j, tilePixelMap.get(j), boundingList, globalLight));
		}
		
		CountDownLatch countDownLatch = new CountDownLatch(chunks);
		renderingTiles.forEach(x -> {
			doNewThread(() -> x.start(countDownLatch));
		});
		
		
		//wait for all threads to finished to prevent the main thread from shutting down before all threads are done.
		try
		{
			countDownLatch.await(2L, TimeUnit.DAYS);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void doNewThread(Runnable runnable)
	{
		new Thread(() -> runnable.run()).start();
	}
	
	private ArrayList<ConcurrentNode> generateNodes(Camera camera)
	{
		ArrayList<ConcurrentNode> nodes = new ArrayList<>();
		int pixelNumber = 0;
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				Ray ray = camera.calculateRayAt(x / (double) WIDTH, y / (double) HEIGHT);
				nodes.add(new ConcurrentNode(ray, x, y, pixelNumber));
				pixelNumber++;
			}
		}
		System.out.println("population done");
		return nodes;
	}
	
	
}
