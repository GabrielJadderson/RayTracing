import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by Gabriel Jadderson on 26-02-2017.
 */
public class Main
{

    static int W = 1500;
    static int H = 1500;

    static Color color_WHITE = new Color(255, 255, 255);
    static Color color_RED = new Color(255, 0, 0);

    public static void main(String[] args) throws Exception
    {
        File outfile = new File("out.jpg");

        BufferedImage bufferedImage = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);


        Color[][] pixel_colors = new Color[H][W];

        for (int i = 0; i < H; i++)
        {
            for (int j = 0; j < W; j++)
            {
                pixel_colors[i][j] = new Color(255, 255, 255);
            }
        }

        Sphere sphere = new Sphere(new Vector3(W / 2, H / 2, 50), 250);
        Sphere light = new Sphere(new Vector3(W / 2, 0, 49), 50);


        for (int y = 0; y < H; y++)
        {
            for (int x = 0; x < W; x++)
            {

                Ray ray = new Ray(new Vector3(x, y, 0), new Vector3(0, 0, 1));

                double t = 405;

                if (sphere.intersects(ray, t))
                {
                    Vector3 pi = Vector3.add(ray.getOrigin(), Vector3.multiply(ray.getDirection(), t));
                    Vector3 l = Vector3.sub(light.getCenter(), pi);
                    Vector3 N = sphere.getNormal(pi);
                    double dt = Vector3.dot(l.normalize(), N.normalize());

                    float multiplier = 1f;
                    pixel_colors[y][x] = Color.add(color_RED, Color.multiply(color_WHITE, dt * multiplier));
                }

                int[] rbg = new int[3];
                rbg[0] = (int) pixel_colors[y][x].getR();
                rbg[1] = (int) pixel_colors[y][x].getG();
                rbg[2] = (int) pixel_colors[y][x].getB();
                bufferedImage.getRaster().setPixel(x, y, rbg); //this is so bad
            }
        }

        ImageIO.write(bufferedImage, "jpg", outfile);


    }
}
