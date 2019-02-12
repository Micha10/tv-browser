package util.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;

/**
 * A class for drawing icons with better interpolation.
 * 
 * @author René Mach
 * @since 4.1
 */
public class ImageIconEnhanced extends ImageIcon {
	public ImageIconEnhanced() {
		super();
	}
	
	public ImageIconEnhanced(Image img) {
		super(img);
	}
	
	public ImageIconEnhanced(String path) {
		super(path);
	}
	
	public ImageIconEnhanced(byte[] data) {
		super(data);
	}
	
	@Override
	public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
		if(g instanceof Graphics2D) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		}
		
		super.paintIcon(c, g, x, y);
	}
}
