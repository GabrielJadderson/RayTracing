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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static core.Globals.EPSILON;

/**
 * Created by Gabriel Jadderson on 09-08-2017.
 */
public class RenderingTile
{
	private ConcurrentHashMap<Integer, Pixel> renderMap;
	private ArrayList<ConcurrentNode> nodes;
	
	private ArrayList<BoundingVol> BL;
	private Light GL;
	
	private int id = 0;
	
	public RenderingTile(int id, ArrayList<ConcurrentNode> nodes, ArrayList<BoundingVol> BL, Light GL)
	{
		this.id = id;
		this.BL = BL;
		this.GL = GL;
		
		this.renderMap = new ConcurrentHashMap<>();
		
		this.nodes = nodes;
	}
	
	
	public void start(CountDownLatch cl)
	{
		System.out.println("Tile " + this.id + " started");
		CountDownLatch countDownLatch = new CountDownLatch(nodes.size());
		
		nodes.forEach(x -> {
			trace(countDownLatch, x.pixelNumber, x.ray, x.x, x.y, this.BL, this.GL, this.renderMap);
		});
		
		cl.countDown();
		System.out.println("Tile " + this.id + " finished");
		
	}
	
	
	//this runs asynchronously
	private void trace(CountDownLatch pl, int pixelNumber, Ray ray, int x, int y, ArrayList<BoundingVol> boundingList, Light globalLight, ConcurrentHashMap<Integer, Pixel> renderMap)
	{
		Vector3D color = Vector3D.ZERO;
		
		color = plusEqual(color, TBV(ray, boundingList, globalLight));
		
		pl.countDown();
		Pixel p = new Pixel(x, y, (float) color.getX(), (float) color.getY(), (float) color.getZ());
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
		this.BL = null;
		this.GL = null;
		this.renderMap = null;
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
