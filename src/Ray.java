/**
 * Created by Gabriel Jadderson on 26-02-2017.
 */
public class Ray
{
    private Vector3 origin;
    private Vector3 direction;

    public Ray(Vector3 origin, Vector3 direction)
    {
        this.origin = origin;
        this.direction = direction;
    }

    public Vector3 getDirection()
    {
        return direction;
    }

    public Vector3 getOrigin()
    {
        return origin;
    }
}
