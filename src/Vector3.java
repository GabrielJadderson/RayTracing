/**
 * Created by Gabriel Jadderson on 26-02-2017.
 */
public class Vector3
{
    private double x, y, z;

    public Vector3()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Vector3(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }


    public Vector3 normalize()
    {
        double mg = Math.sqrt((x * x) + (y * y) + (z * z));
        return new Vector3(x / mg, y / mg, z / mg);
    }

    public static Vector3 sub(Vector3 a, Vector3 b)
    {
        return new Vector3(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    public static Vector3 add(Vector3 a, Vector3 b)
    {
        return new Vector3(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    public static double dot(Vector3 a, Vector3 b)
    {
        return (a.x * b.x) + (a.y * b.y) + (a.z * b.z);
    }

    public static Vector3 multiply(Vector3 a, double b)
    {
        return new Vector3(a.x * b, a.y * b, a.z * b);
    }

    public static Vector3 divide(Vector3 a, double b)
    {
        return new Vector3(a.x / b, a.y / b, a.z / b);
    }
}
