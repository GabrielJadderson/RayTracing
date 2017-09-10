package core.concurrency;

import core.Globals;
import core.gui.Pixel;
import core.world.light.Light;
import core.world.math.VecMath;
import core.world.ray.Ray;
import core.world.ray.RayInfo;
import core.world.shapes.BoundingVol;
import core.world.shapes.IShape;
import core.world.shapes.OclusionObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static core.Globals.EPSILON;

/**
 * Created by Gabriel Jadderson on 09-07-2017.
 */
public class ConcurrentDivision
{
	
	private ConcurrentHashMap<Integer, int[]> pixelMap; //this will hold x and y coords
	
	private ConcurrentHashMap<Integer, Ray> rayMap; //this will hold a ray
	
	private ConcurrentHashMap<Integer, Pixel> renderMap;
	
	private List<Integer> PID;
	//private int[] pixels;
	
	private CountDownLatch pixelLatch;
	
	private ExecutorService executorService;
	
	private ArrayList<BoundingVol> BL;
	private Light GL;
	
	private int id = 0;
	
	public ConcurrentDivision(int unique_id, int[] pixelIDs, ArrayList<BoundingVol> boundingList, Light globalLight, ConcurrentHashMap<Integer, Ray> rayMap, ConcurrentHashMap<Integer, int[]> pixelMap)
	{
		this.id = unique_id;
		this.pixelMap = pixelMap;
		this.rayMap = rayMap;
		this.GL = globalLight;
		this.BL = boundingList;
		
		renderMap = new ConcurrentHashMap<>();
		pixelLatch = new CountDownLatch(pixelIDs.length - 1);
		
		//executorService = Executors.newFixedThreadPool(1);
		executorService = Executors.newSingleThreadExecutor();
		
		
		PID = Collections.synchronizedList(new ArrayList<>());
		for (int i : pixelIDs)
		{
			PID.add(i);
		}
	}
	
	public void init(CountDownLatch latch)
	{
		
		this.PID.forEach(x ->
		{
			executorService.submit(() -> trace(this.pixelLatch, x, this.BL, this.GL, this.renderMap));
		});
		
		try
		{
			this.pixelLatch.await();
			//push to renderer
			pushToRenderer();
			latch.countDown(); //once all is done, then increment
			executorService.shutdown();
			//executorService.awaitTermination(2L, TimeUnit.SECONDS);
			clean();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	
	//this runs asynchronously
	private void trace(CountDownLatch pl, int pixelNumber, ArrayList<BoundingVol> boundingList, Light globalLight, ConcurrentHashMap<Integer, Pixel> renderMap)
	{
		
		Vector3D color = Vector3D.ZERO;
		
		Ray ray = rayMap.get(pixelNumber);
		int[] arr = pixelMap.get(pixelNumber);
		
		color = plusEqual(color, TBV(ray, boundingList, globalLight));
		
		pl.countDown();
		Pixel p = new Pixel(arr[0], arr[1], (float) color.getX(), (float) color.getY(), (float) color.getZ());
		renderMap.put(pixelNumber, p);
	}
	
	private Vector3D TBV(Ray ray, ArrayList<BoundingVol> boundingList, Light globalLight)
	{
		for (BoundingVol bound : boundingList)
		{
			if (bound.shape.intersects(ray, EPSILON, Double.MAX_VALUE).didIntersect)
			{
				OclusionObject oclusionObject = new OclusionObject(bound.listOfShapes);
				return goTrace(ray, oclusionObject, globalLight);
			}
		}
		return Vector3D.ZERO; //it never reaches this point??
	}
	
	
	private Vector3D goTrace(Ray ray, IShape mainShape, Light light)
	{
		RayInfo rayInfo = mainShape.intersects(ray, EPSILON, Double.MAX_VALUE); //maybe implement a min and max intersection distance.
		if (rayInfo.didIntersect)
		{
			return shade(rayInfo, light);
		} else //else, lerp to create a gradient background.
		{
			return applyBackground(ray.dir);
		}
	}
	
	
	private Vector3D applyBackground(Vector3D rayDir)
	{
		Vector3D unitVectorDirection = VecMath.getUnitVector(rayDir.normalize());
		double multiplier = 0.9;
		double t = multiplier * -unitVectorDirection.getY() + 1.0;
		return lerp(new Vector3D(1.0, 1.0, 1.0), Globals.bkgColor, t);
	}
	
	
	private Vector3D shade(RayInfo rayInfo, Light light)
	{
		Vector3D c;
		double normalDotLightpos = rayInfo.normal.dotProduct(light.position.normalize());
		double ambiance = light.ambience + ((1.0 - light.ambience) * Math.max(0.0, normalDotLightpos));
		c = rayInfo.material.albedo.scalarMultiply(ambiance);
		return c;
	}
	
	private Vector3D lerp(Vector3D startValue, Vector3D endValue, double t)
	{
		return startValue.scalarMultiply(1.0 - t).add(endValue.scalarMultiply(t));
	}
	
	//assign math.
	private Vector3D plusEqual(Vector3D v, Vector3D v1)
	{
		double x = v.getX();
		double y = v.getY();
		double z = v.getZ();
		
		x += v1.getX();
		y += v1.getY();
		z += v1.getZ();
		return new Vector3D(x, y, z);
	}
	
	//finalize for GC
	private void clean()
	{
		this.executorService = null;
		this.pixelLatch = null;
		this.BL = null;
		this.GL = null;
		this.PID = null;
		this.renderMap = null;
		this.pixelLatch = null;
		this.rayMap = null;
	}
	
	private void pushToRenderer()
	{
		ArrayList<Pixel> pixels = new ArrayList<>();
		renderMap.forEach((k, v) ->
		{
			pixels.add(v);
		});
		Globals.renderer.enhancedPixelRenderMap.put(id, pixels);
	}
	
}
