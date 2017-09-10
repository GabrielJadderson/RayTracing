package core.gui;

import core.Globals;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Gabriel Jadderson on 29-06-2017.
 */
public class ConcurrentRenderer extends JPanel
{
	public static ConcurrentHashMap<Integer, Pixel> pixelMap = new ConcurrentHashMap<>();
	public static ArrayList<Pixel> pixels = new ArrayList<>();
	public static ConcurrentHashMap<Integer, ArrayList<Pixel>> enhancedPixelRenderMap = new ConcurrentHashMap<>();
	public static boolean keepRendering = false;
	
	private boolean isFinalized = false;
	
	public ConcurrentRenderer()
	{
		setBounds(0, 0, Globals.WIDTH, Globals.HEIGHT);
		setBackground(Color.WHITE);
		setForeground(Color.WHITE);
		setVisible(true);
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		
		if (keepRendering)
		{
			repaint();
		}
		
		/*
		pixelMap.forEach((k, v) ->
		{
			g.setColor(new Color(v.r, v.g, v.b));
			g.drawLine(v.x, v.y, v.x, v.y);
		});
		*/
		
		
		if (!isFinalized)
		{
			
			enhancedPixelRenderMap.forEach((key, val) ->
			{
				val.forEach(v ->
				{
					g.setColor(new Color(v.r, v.g, v.b));
					g.drawLine(v.x, v.y, v.x, v.y);
				});
			});
		} else
		{
			pixels.forEach(v ->
			{
				g.setColor(new Color(v.r, v.g, v.b));
				g.drawLine(v.x, v.y, v.x, v.y);
			});
		}
		
		
	}
	
	public void finalizePicture()
	{
		enhancedPixelRenderMap.forEach((k, v) -> v.forEach(x -> this.pixels.add(x)));
		isFinalized = true;
		enhancedPixelRenderMap = null;
		pixelMap = null;
		Runtime.getRuntime().gc();
		Runtime.getRuntime().runFinalization();
		repaint();
	}
}
