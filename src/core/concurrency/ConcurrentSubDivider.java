package core.concurrency;

import core.world.camera.Camera;
import core.world.light.Light;
import core.world.ray.Ray;
import core.world.shapes.BoundingVol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static core.Globals.HEIGHT;
import static core.Globals.WIDTH;

/**
 * Created by Gabriel Jadderson on 09-07-2017.
 */
public class ConcurrentSubDivider
{
	
	public static ConcurrentHashMap<Integer, int[]> pixelMap; //this will hold x and y coords
	public static ConcurrentHashMap<Integer, Ray> rayMap; //this will hold a ray
	
	private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 2);
	
	public ConcurrentSubDivider(int subdivisons, Camera camera, ArrayList<BoundingVol> boundingList, Light globalLight)
	{
		
		pixelMap = new ConcurrentHashMap<>();
		rayMap = new ConcurrentHashMap<>();
		
		generateAndStoreRayPerPixel(camera);
		
		final int subdivisionCount = (WIDTH * HEIGHT) / subdivisons;
		
		List<ConcurrentDivision> divisions = Collections.synchronizedList(new ArrayList<ConcurrentDivision>());
		
		//ArrayList<Integer> pixelList = new ArrayList<>();
		//ArrayList<int[]> pixelListArray = new ArrayList<>();
		/*
		int[][] pixels = new int[subdivisons][subdivisionCount];
		int c = 0;
		for (int i = 0; i < subdivisons; i++)
		{
			for (int j = 0; j < subdivisionCount; j++)
			{
				pixels[i][j] = c;
				c++;
			}
		}
		*/
		
		//randomize order of pixels to be rendered and create subdivided array
		ArrayList<Integer> randomInts = new ArrayList<>();
		for (int i = 0; i < WIDTH * HEIGHT; i++)
		{
			randomInts.add(i);
		}
		//Collections.shuffle(randomInts);
		
		int[][] pixels = new int[subdivisons][subdivisionCount];
		int q = 0;
		for (int i = 0; i < subdivisons; i++)
		{
			for (int j = 0; j < subdivisionCount; j++)
			{
				pixels[i][j] = randomInts.get(q);
				q++;
			}
		}
		
		//start asigning values
		for (int i = 0; i < subdivisons; i++)
		{
			divisions.add(new ConcurrentDivision(i, pixels[i], boundingList, globalLight, getRelevantRayMap(pixels[i]), getRelevantPixelMap(pixels[i])));
		}
		
		
		//start assigning values
		
		
		//do calculations
		CountDownLatch latch = new CountDownLatch(subdivisons);
		//CountDownLatch latch = new CountDownLatch(subdivisons * subdivisionCount);
		
		divisions.forEach(x ->
		{
			executorService.submit(() ->
			{
				x.init(latch);
			});
		});
		
		
		//make sure we end
		try
		{
			latch.await();
			executorService.shutdownNow();
			executorService.awaitTermination(2L, TimeUnit.SECONDS);
			clean(pixels, randomInts, divisions);
			pixels = null;
			divisions = null;
			randomInts = null;
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		
	}
	
	private ConcurrentHashMap<Integer, int[]> getRelevantPixelMap(int[] list)
	{
		ConcurrentHashMap<Integer, int[]> map = new ConcurrentHashMap<>();
		for (Integer i : list)
		{
			map.put(i, this.pixelMap.get(i));
		}
		return map;
	}
	
	private ConcurrentHashMap<Integer, Ray> getRelevantRayMap(int[] list)
	{
		ConcurrentHashMap<Integer, Ray> map = new ConcurrentHashMap<>();
		for (Integer i : list)
		{
			map.put(i, this.rayMap.get(i));
		}
		return map;
	}
	
	
	private void generateAndStoreRayPerPixel(Camera camera)
	{
		int pixelNumber = 0;
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				//  System.out.println(pixelNumber); //TODO: DELETE ME. used for debug can be deleted
				
				int[] arr = new int[2];
				arr[0] = x;
				arr[1] = y;
				
				Ray ray = camera.calculateRayAt(x / (double) WIDTH, y / (double) HEIGHT);
				
				pixelMap.put(pixelNumber, arr);
				rayMap.put(pixelNumber, ray);
				pixelNumber++;
			}
		}
		System.out.println("population done");
	}
	
	private void clean(int[][] pixels, ArrayList<?> arrayLists, List<?> list)
	{
		this.pixelMap = null;
		this.rayMap = null;
		arrayLists.clear();
		list.clear();
		for (int i = 0; i < pixels.length; i++)
		{
			for (int j = 0; j < pixels[i].length; j++)
			{
				pixels[i][j] = 0;
			}
		}
		System.gc();
	}
}
