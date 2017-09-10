package core.concurrency;

import core.world.ray.Ray;

/**
 * Created by Gabriel Jadderson on 09-08-2017.
 */
public class ConcurrentNode
{
	Ray ray;
	int x;
	int y;
	int pixelNumber;
	
	public ConcurrentNode(Ray ray, int x, int y, int pixelNumber)
	{
		this.ray = ray;
		this.x = x;
		this.y = y;
		this.pixelNumber = pixelNumber;
	}
}
