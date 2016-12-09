package nl.prowareness.automation.selenium.webanalytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.openqa.selenium.Proxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import nl.prowareness.automation.selenium.exceptions.AutomationDriverException;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;

@Component
public class WebAnalyticsDriver {

	private static final Logger LOGGER = Logger.getLogger(WebAnalyticsDriver.class);

	@Autowired
	private Environment properties;

	private Map<String,String>webTrendsParametersMap = null;
	private BrowserMobProxy proxy = null;


	/**
	 * Default constructor
	 */

	public WebAnalyticsDriver(){
		proxy = new BrowserMobProxyServer();
	}

	public Proxy startBrowserMobProxyServer(){
		Proxy seleniumProxy = null;
		if(!this.proxy.isStarted()){
			this.proxy.start();
			seleniumProxy = ClientUtil.createSeleniumProxy(this.proxy);
		}

		return seleniumProxy;
	}


	public void stopBrowserMobProxyServer(){
		this.proxy.stop();
	}

	public void createNewHtmlArchive(String archiveName) throws AutomationDriverException{
		if(this.proxy.isStarted())
			this.proxy.newHar(archiveName);
		else
			throw new AutomationDriverException("Browser Mob Proxy is not started. Please start the proxy...");
	}

	public void stopCapturingHtmlArchive() throws AutomationDriverException{
		if(this.proxy.isStarted())
			this.proxy.endHar();
		else
			throw new AutomationDriverException("Browser Mob Proxy is not started. Please start the proxy...");

	}


	public Map<String,String> readAndGetWebAnalyticsParameters() throws AutomationDriverException{
		webTrendsParametersMap = new ConcurrentHashMap<String,String>();

		try {
			Thread.sleep(Long.parseLong(properties.getProperty("webanalytics.parameters.readwait")));
		} catch (NumberFormatException e) {
			throw new AutomationDriverException("Wait time is not properly provided [webanalytics.parameters.readwait]");
		} catch (InterruptedException e) {
			throw new AutomationDriverException("Browser Mob Proxy : InterruptedException",e);
		}

        Har har = proxy.getHar();

	    for(HarEntry he: har.getLog().getEntries()){

	    	if(he.getRequest()!=null && he.getRequest().getQueryString()!=null){

	    		
		    		Iterator<HarNameValuePair> queryStringIter
		    				= he.getRequest().getQueryString().iterator();

		    		while(queryStringIter.hasNext()){
		    			HarNameValuePair nvp = queryStringIter.next();
		    			webTrendsParametersMap.put(nvp.getName(), nvp.getValue());
		    	
		    	  }

	    	  }
	      }
	    LOGGER.info("Web Analytics Parameters Captured: "+webTrendsParametersMap);

	    return webTrendsParametersMap;

	 }


	public void writeWebAnalyticsParametersToGivenFile(String path) throws AutomationDriverException{

		try {
			Thread.sleep(Long.parseLong(properties.getProperty("webanalytics.parameters.readwait")));
		} catch (NumberFormatException e) {
			throw new AutomationDriverException("Wait time is not properly provided [webanalytics.parameters.readwait]");
		} catch (InterruptedException e) {
			throw new AutomationDriverException("Browser Mob Proxy : InterruptedException",e);
		}

        Har har = proxy.getHar();
        writeHarToFile(har,path);
	 }


	private void writeHarToFile(Har hr, String filePath) throws AutomationDriverException {
		FileOutputStream fos = null;
		File file = new File(filePath);

		try {

			if(!file.exists())
				file.createNewFile();

			fos = new FileOutputStream(file,false);
			hr.writeTo(fos);
			fos.flush();
		} catch (IOException ioe) {
			throw new AutomationDriverException("IOException: "+ioe);

		} finally{

			try {
				if(fos!=null)
					fos.close();
			} catch (IOException e) {
				throw new AutomationDriverException("IOException: "+e);
			}

		}
	}


	public BrowserMobProxy getProxy(){
		return proxy;
	}

}
