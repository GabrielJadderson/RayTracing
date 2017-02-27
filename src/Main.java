import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

/**
 * Created by Gabriel Jadderson on 26-02-2017.
 */
public class Main
{

    static int W = 1500;
    static int H = 1500;

    static Color color_WHITE = new Color(255, 255, 255);
    static Color color_RED = new Color(255, 0, 0);
    static Color color_GREEN = new Color(0, 0, 255);
    static Color color_BLACK = new Color(0, 0, 0);

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

        Sphere sphere = new Sphere(new Vector3(W / 2, H / 2, 15), 200);
        Sphere light = new Sphere(new Vector3(W / 2, 1000, 1000), 200);


        for (int y = 0; y < H; y++)
        {
            for (int x = 0; x < W; x++)
            {

                Ray ray = new Ray(new Vector3(x, y, 0), new Vector3(0, 0, 0));

                double t = 401;

                if (sphere.intersects(ray, t))
                {
                    Vector3 pi = Vector3.add(ray.getOrigin(), Vector3.multiply(ray.getDirection(), t));
                    Vector3 l = Vector3.sub(light.getCenter(), pi);
                    Vector3 N = sphere.getNormal(pi);
                    double dt = Vector3.dot(l.normalize(), N.normalize());

                    float multiplier = 1f;
                    pixel_colors[y][x] = Color.add(color_RED, Color.multiply(color_WHITE, dt * multiplier));

                    //Random random = new Random();
                    //pixel_colors[y][x] = random.nextInt(2) == random.nextInt(2) ? color_WHITE : color_RED;
                } else
                {
                    Random random = new Random();
                    pixel_colors[y][x] = random.nextInt(2) == random.nextInt(2) ? color_GREEN : color_BLACK;

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
