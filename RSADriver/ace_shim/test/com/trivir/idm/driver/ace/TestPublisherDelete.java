package com.trivir.idm.driver.ace;

import java.io.IOException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.novell.nds.dirxml.driver.Trace;
import com.novell.nds.dirxml.driver.XmlDocument;
import com.trivir.ace.AceToolkitException;
import com.trivir.ace.api.AceApi;

	
public class TestPublisherDelete extends XMLTestCase {
    private static final String user = "-TestUser1";

    private AceDriverShim driver;
    private AceApi api;
    
    public TestPublisherDelete() {
        Trace.registerImpl(CustomTraceInterface.class, 1);
    }
    
    protected void setUp() throws Exception {
    	TestUtil.dropCacheDatabase();
    	
    	api = TestUtil.getApi(); 
        api.addUser("User1", "Test", user.substring(1), null, "", TestUtil.DEFAULT_USER_PASSWORD);

        driver = new AceDriverShim();
        Document response = driver.init(new XmlDocument(TestDriverShim.getInitRequest())).getDocument();
        TestUtil.printDocumentToScreen("setUpShimResponse", response);
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);

        XmlDocument request = new XmlDocument(TestPublisher.getPublisherInitRequest(true, false));
        TestUtil.printDocumentToScreen("setUpPublisherRequest:", request.getDocument());
        response = driver.getPublicationShim().init(request).getDocument();
        ((PublisherShim)driver.getPublicationShim()).setPollRate(1 * 1000);
        TestUtil.printDocumentToScreen("setUpPublisherResponse", response);
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);
    }
    
    protected void tearDown() throws AceToolkitException {
        driver.shutdown(new XmlDocument(TestPublisher.shutdown));
        api.destroy();
    }

//	  ====================================================================================================================================
    
    private static final String deleteRequest =
		"<nds dtdversion=\"2.0\">" +
			"<source>" + 
				"<contact>TriVir</contact>" +
				"<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
			"</source>" +
			"<input>" + 
				"<delete class-name=\"User\">" +
					"<association>" + user.substring(1) + "</association>" +
				"</delete>" +
			"</input>" +
		"</nds>";
    
    public void testUserDelete() throws AceToolkitException, TransformerConfigurationException, TransformerException, SAXException, IOException, InterruptedException {
        XmlDocument event = null;
        
        PublisherQueue publisherQueue = new PublisherQueue(driver, 20000 * 1000);
        
        Thread pubThread = new Thread(publisherQueue);
        pubThread.start();
        Thread.sleep(10 * 1000); // needed for everything to start up before we perform an action
        
        api.deleteUser(user);

		try {
			event = publisherQueue.getEventDocument();
		} catch (InterruptedException e) {
		}		

		publisherQueue.setResponseDocument(TestPublisher.standardSuccessResponse);
		if (event == null) {
			assertFalse("Event received from Publisher was null, should have contained response!", true);
		}
        TestUtil.printDocumentToScreen("testUserDelete", event.getDocument());
        this.assertXMLEqual(XMLUnit.buildControlDocument(deleteRequest), event.getDocument());
    }
    

}
