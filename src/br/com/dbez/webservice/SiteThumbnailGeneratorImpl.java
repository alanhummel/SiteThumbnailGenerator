package br.com.dbez.webservice;

import java.util.Base64;

import javax.jws.WebService;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import br.com.dbez.bc.WebsiteThumbnailGenerator;

@WebService(endpointInterface="br.com.dbez.webservice.SiteThumbnailGenerator") 
public class SiteThumbnailGeneratorImpl implements SiteThumbnailGenerator {
	private Display display = null;
	private Browser browser = null;
	private Shell shell = null;
	
	public SiteThumbnailGeneratorImpl(Display mainDisplay, Browser mainBrowser, Shell mainShell) {
		display = mainDisplay;
		browser = mainBrowser;
		shell = mainShell;
	}
	
	
	public String generateThumbnail(String site) {
		WebsiteThumbnailGenerator wstbGen = new WebsiteThumbnailGenerator(display, browser, shell);
		byte[] thumbnail = wstbGen.generateThumbNail(site, 1280, 1024);
		byte[] thumbnailEncoded = null;
		if (thumbnail!=null) {
			thumbnailEncoded = Base64.getEncoder().encode(thumbnail);
		}
		if (thumbnailEncoded!=null) {
			return new String(thumbnailEncoded);
		} else {
			return "";			
		}
	}
}