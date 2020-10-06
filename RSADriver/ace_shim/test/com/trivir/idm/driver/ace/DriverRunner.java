package com.trivir.idm.driver.ace;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import com.novell.nds.dirxml.driver.DriverShim;
import com.novell.nds.dirxml.driver.Trace;
import com.novell.nds.dirxml.driver.XmlCommandProcessor;
import com.novell.nds.dirxml.driver.XmlDocument;
import com.novell.nds.dirxml.driver.XmlQueryProcessor;
import com.trivir.ace.api.AceApi;

public class DriverRunner {
	
	public static String startYear = null;
    public static String endYear = null;
    
    static {
        try {
        	AceApi api = TestUtil.getApi();
            String revString = api.apiRev();
            // can't be called statically - shouldn't be newing up a new one either . . . .fix this: 
        	//api.destroy();            
            if (revString.startsWith("Release: 7.1")) {  //Release: 7.1, Date: Oct 26 2005 14:39:38
                startYear = "1986";
                endYear = "1986";
            } else if (revString.startsWith("Release: 6.1")) {  //Release: 6.1, Date: Oct 26 2005 14:39:38
                startYear = "1986";
                endYear = "1986";
            } else if (revString.startsWith("Release: 5.2")) { //Release: 5.2, Date: Nov  4 2003 11:28:11
                startYear = "2001";
                endYear = "2010";
            } else {
                throw new Exception("FATAL: Don't know how to handle new ace version: [" + revString + "]");                
            }
        } catch (Exception e) {
            System.out.println("Error setting up RSA API for tests.");
            e.printStackTrace();
            System.exit(-1);    
        } 
    }
	
	private AceDriverShim driver;
    private RunnerQueue publisher;
    
    public DriverRunner() {
    	Trace.registerImpl(CustomTraceInterface.class, 7);
    }
	
	public static void main(String[] args) throws TransformerConfigurationException, TransformerException {
		DriverRunner runner = new DriverRunner();
		runner.runDriver();
	}
	
	public void runDriver() throws TransformerConfigurationException, TransformerException {
		driver = new AceDriverShim();
		Document response = driver.init(new XmlDocument(TestDriverShim.getInitRequest())).getDocument();
		TestUtil.printDocumentToScreen("setUpDriver", response);
		XmlDocument request = new XmlDocument(TestPublisher.getPublisherInitRequest());
        response = driver.getPublicationShim().init(request).getDocument();
        ((PublisherShim)driver.getPublicationShim()).setHeartBeatInterval(0);
        ((PublisherShim)driver.getPublicationShim()).setPollRate(30 * 1000);
        
        publisher = new RunnerQueue(driver);
//        Thread pubThread = new Thread(publisher);
//        pubThread.start();
        publisher.run();
	}
	
	private static class RunnerQueue implements Runnable, XmlCommandProcessor {
		
		static final String standardSuccessResponse =
	        "<nds dtdversion=\"2.0\">" +
	            "<source>" +
	                "<contact>TriVir</contact>" +
	                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
	            "</source>" +
	            "<output>" +
	                "<status event-id=\"0\" level=\"success\" type=\"driver-general\"/>" +
	            "</output>" +
	        "</nds>";
		
		private DriverShim shim;
		
		RunnerQueue(DriverShim shim) {
	        this.shim = shim;
	    }

		@Override
		public void run() {
			XmlDocument response = shim.getPublicationShim().start(this);
	        synchronized (this) {
	            System.out.println(response.getDocumentString());
	        }
			
		}

		@Override
		public XmlDocument execute(XmlDocument arg0, XmlQueryProcessor arg1) {
			System.out.println(arg0.getDocumentString());
			return new XmlDocument(standardSuccessResponse);
		}
		
	}
}