package br.com.dbez.bc;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class WebsiteThumbnailGenerator {
	byte[] currentThumbnail = null;
	Display display = null;
	Browser browser = null;
	Shell shell = null;
	boolean requisitonOver = false;
	
	public static Long lStartTime = new Long(0);
	public static Long lEndTime = new Long(0);


	public WebsiteThumbnailGenerator(Display mainDisplay, Browser mainBrowser, Shell mainShell) {
		display = mainDisplay;
		browser = mainBrowser;
		shell = mainShell;
	}

	public byte[] generateThumbNail(String site, int width, int height) {

		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				browser.addProgressListener(new ProgressListener() {

					@Override
					public void changed( ProgressEvent event ) {
						lEndTime = new Long(new Date().getTime());
						long totalTime = lEndTime.longValue() - lStartTime.longValue();
						if (totalTime > 5000) {
							browser.stop();
							System.out.println("Browser stopped. Took: " + totalTime + " ms.");
							lStartTime = new Long(lEndTime.longValue());
						}
					}

					@Override
					public void completed( ProgressEvent event ) {
						shell.forceActive();
						//display.syncExec(new Runnable() {

							//@Override
							//public void run() {
						currentThumbnail = grab(display, shell, browser);
						requisitonOver = true;
							//}
						//});

					}
				});
				browser.setJavascriptEnabled(false);
				browser.setUrl(site);
				lStartTime = new Long(new Date().getTime());

				shell.setSize(width, height);
				shell.open();

				while ( !requisitonOver ) {
					if ( !display.readAndDispatch() ) 
						display.sleep();
				}
				//display.dispose();				
			}

		});


		return currentThumbnail;
	}
	
	/**
	 * Resizes an image, using the given scaling factor. Constructs a new image resource, please take care of resource
	 * disposal if you no longer need the original one. This method is optimized for quality, not for speed.
	 * 
	 * @param image source image
	 * @param scale scale factor (<1 = downscaling, >1 = upscaling)
	 * @return scaled image
	 */
	private static org.eclipse.swt.graphics.Image resize (org.eclipse.swt.graphics.Image image, float scale) {
	    int w = image.getBounds().width;
	    int h = image.getBounds().height;

	    // convert to buffered image
	    BufferedImage img = convertToAWT(image.getImageData());

	    // resize buffered image
	    int newWidth = Math.round(scale * w);
	    int newHeight = Math.round(scale * h);

	    // determine scaling mode for best result: if downsizing, use area averaging, if upsizing, use smooth scaling
	    // (usually bilinear).
	    int mode = scale < 1 ? BufferedImage.SCALE_AREA_AVERAGING : BufferedImage.SCALE_SMOOTH;
	    java.awt.Image scaledImage = img.getScaledInstance(newWidth, newHeight, mode);

	    // convert the scaled image back to a buffered image
	    img = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
	    img.getGraphics().drawImage(scaledImage, 0, 0, null);

	    // reconstruct swt image
	    ImageData imageData = convertToSWT(img);
	    return new org.eclipse.swt.graphics.Image(Display.getDefault(), imageData);
	}	
	
	public static BufferedImage convertToAWT (ImageData data) {
	    ColorModel colorModel = null;
	    PaletteData palette = data.palette;
	    if (palette.isDirect) {
	        colorModel = new DirectColorModel(data.depth, palette.redMask, palette.greenMask, palette.blueMask);
	        BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(data.width, data.height),
	            false, null);
	        WritableRaster raster = bufferedImage.getRaster();
	        int[] pixelArray = new int[3];
	        for (int y = 0; y < data.height; y++) {
	            for (int x = 0; x < data.width; x++) {
	                int pixel = data.getPixel(x, y);
	                RGB rgb = palette.getRGB(pixel);
	                pixelArray[0] = rgb.red;
	                pixelArray[1] = rgb.green;
	                pixelArray[2] = rgb.blue;
	                raster.setPixels(x, y, 1, 1, pixelArray);
	            }
	        }
	        return bufferedImage;
	    } else {
	        RGB[] rgbs = palette.getRGBs();
	        byte[] red = new byte[rgbs.length];
	        byte[] green = new byte[rgbs.length];
	        byte[] blue = new byte[rgbs.length];
	        for (int i = 0; i < rgbs.length; i++) {
	            RGB rgb = rgbs[i];
	            red[i] = (byte) rgb.red;
	            green[i] = (byte) rgb.green;
	            blue[i] = (byte) rgb.blue;
	        }
	        if (data.transparentPixel != -1) {
	            colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue, data.transparentPixel);
	        } else {
	            colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue);
	        }
	        BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(data.width, data.height),
	            false, null);
	        WritableRaster raster = bufferedImage.getRaster();
	        int[] pixelArray = new int[1];
	        for (int y = 0; y < data.height; y++) {
	            for (int x = 0; x < data.width; x++) {
	                int pixel = data.getPixel(x, y);
	                pixelArray[0] = pixel;
	                raster.setPixel(x, y, pixelArray);
	            }
	        }
	        return bufferedImage;
	    }
	}

	public static ImageData convertToSWT (BufferedImage bufferedImage) {
	    if (bufferedImage.getColorModel() instanceof DirectColorModel) {
	        DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
	        PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask());
	        ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
	        WritableRaster raster = bufferedImage.getRaster();
	        int[] pixelArray = new int[3];
	        for (int y = 0; y < data.height; y++) {
	            for (int x = 0; x < data.width; x++) {
	                raster.getPixel(x, y, pixelArray);
	                int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
	                data.setPixel(x, y, pixel);
	            }
	        }
	        return data;
	    } else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
	        IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
	        int size = colorModel.getMapSize();
	        byte[] reds = new byte[size];
	        byte[] greens = new byte[size];
	        byte[] blues = new byte[size];
	        colorModel.getReds(reds);
	        colorModel.getGreens(greens);
	        colorModel.getBlues(blues);
	        RGB[] rgbs = new RGB[size];
	        for (int i = 0; i < rgbs.length; i++) {
	            rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
	        }
	        PaletteData palette = new PaletteData(rgbs);
	        ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
	        data.transparentPixel = colorModel.getTransparentPixel();
	        WritableRaster raster = bufferedImage.getRaster();
	        int[] pixelArray = new int[1];
	        for (int y = 0; y < data.height; y++) {
	            for (int x = 0; x < data.width; x++) {
	                raster.getPixel(x, y, pixelArray);
	                data.setPixel(x, y, pixelArray[0]);
	            }
	        }
	        return data;
	    }
	    return null;
	}	
/*
	private byte[] scaleDown(ImageData data, int width) {
		// Retain the original width and height
		double oWidth = data.width;
		double oHeight = data.height;

		// Use the width as the scale so the end image is proportional
		// "width" is the newly desired width of the resulting image
		double yScale = width / oWidth;
		int newWidth = (int) (oWidth * yScale);
		int newHeight = (int) (oHeight * yScale);

		// Call the magic API to scale the image data
		ImageData nData = data.scaledTo(newWidth, newHeight);

		byte[] returnData = null;

		returnData = convertImageDataToByteArray(nData);

		return returnData;
	}
*/
	public byte[] convertImageDataToByteArray(ImageData imData) {
		byte[] buffer = null;
		try {
			// write data to a byte array
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream writeOut = new DataOutputStream(out);
			ImageLoader loader = new ImageLoader();
			loader.data = new ImageData[] { imData };
			loader.save(writeOut, SWT.IMAGE_BMP);
			writeOut.close();
			buffer = out.toByteArray();
			out.close();
		} catch (IOException e) {
		}
		return buffer;
	}	

	private byte[] grab(final Display display, final Shell shell, final Browser browser) {
		Image image = new Image(display, browser.getBounds());
		GC gc = new GC(browser);
		gc.copyArea(image, 0, 0);
		gc.dispose();
		
		image = resize(image, (float) 0.4);

		byte[] thumb = convertImageDataToByteArray(image.getImageData()); //scaleDown(image.getImageData(), 512);
		thumb = thumb.clone();
		image.dispose();

		//shell.dispose();
		return thumb;
	}
}
