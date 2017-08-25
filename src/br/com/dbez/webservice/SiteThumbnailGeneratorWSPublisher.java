package br.com.dbez.webservice;
import javax.xml.ws.Endpoint;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;  

public class SiteThumbnailGeneratorWSPublisher {
	public static Long lStartTime = new Long(0);
	public static Long lEndTime = new Long(0);

	public static void main(String[] args) {  
		final Display display = new Display();

		final Shell shell = new Shell();
		shell.setLayout(new FillLayout());
		final Browser browser = new Browser(shell, SWT.NONE);


		shell.setSize(1280, 1024);
		shell.open();
        Endpoint endpoint = Endpoint.create(new SiteThumbnailGeneratorImpl(display, browser, shell));
        endpoint.publish("http://localhost:8080/WS/SiteThumbnailGenerator");
         
		System.out.println("Web Service published.");

		while ( !shell.isDisposed() ) {
			if ( !display.readAndDispatch() ) display.sleep();
		}
		display.dispose();
		endpoint.stop();
		System.out.println("Web Service unpublished.");
		
	}  	
}
