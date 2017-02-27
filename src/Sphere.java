/**
 * Created by Gabriel Jadderson on 26-02-2017.
 */
public class Sphere
{
    private Vector3 center;
    private double radius = 0;

    public Sphere(Vector3 center, double radius)
    {
        this.center = center;
        this.radius = radius;
    }

    public double getRadius()
    {
        return radius;
    }

    public Vector3 getCenter()
    {
        return center;
    }

    public Vector3 getNormal(Vector3 pi)
    {
        return Vector3.divide(Vector3.sub(center, pi), radius);
    }

    public boolean intersects(Ray ray, double t)
    {
        Vector3 origin = ray.getOrigin();
        Vector3 direction = ray.getDirection();
        Vector3 OC = Vector3.sub(origin, center);
        double b = 2 * Vector3.dot(OC, direction);
        double c = Vector3.dot(OC, OC) - (radius * radius);
        double disc = (b * b) - (4 * c);
        if (disc < 0)
            return false;
        else
        {
            disc = Math.sqrt(disc);
            double t0 = -b - disc;
            double t1 = -b + disc;
            return (t0 < t1) ? true : false;
        }

    }
}
