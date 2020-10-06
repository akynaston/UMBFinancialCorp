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
import com.trivir.ace.api.AceApi;

public class TestPublisherAdd extends XMLTestCase {
	private static final String user = "-TestUser1";
	
    private AceDriverShim driver;
    private PublisherQueue publisher;
    private AceApi api;
    
    private static final String tokenSerialNum = TestUtil.tokenSerialNum;
    private static final String tokenSerialNum2 = TestUtil.tokenSerialNum2;

    private static final String group1 = "TestGroup1";
    private static final String group2 = "TestGroup2";
    
    private static final String profileName = "TESTPROFILE1";
    
    /* Test time values
     * 01/02/2008 @ 08:00 MST = 1199286000
     * 11/12/2013 @ 19:00 MST = 1384308000
     */
    
    private static final String startDateYYMMDD = "01/02/2008";
    private static final int startHourTOD = 8; 
    private static final String endDateYYMMDD = "01/02/2008";
    private static final int endHourTOD = 19;
    
    private static final String startDateSYNTIME = "1199286000";
    private static final String endDateSYNTIME = "1199325600";
    
    public TestPublisherAdd() {
        Trace.registerImpl(CustomTraceInterface.class, 1);
    }
    
    protected void setUp() throws Exception {
    	TestUtil.dropCacheDatabase();
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

        api = TestUtil.getApi(); 
    }
    
    protected void tearDown() throws Exception {
        driver.shutdown(new XmlDocument(TestPublisher.shutdown));
		api.destroy();
    }

//  ====================================================================================================================================
    
	private static final String addRequest =
		"<nds dtdversion=\"2.0\">" +
			"<source>" + 
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
			"</source>" +
			"<input>" + 
				"<add class-name=\"User\" event-id=\"0\">" +
                    "<association>" + user.substring(1)+ "</association>" +
					"<add-attr attr-name=\"DefaultLogin\">" +
						"<value type=\"string\">" + user.substring(1) + "</value>" +
					"</add-attr>" +
                    "<add-attr attr-name=\"DefaultShell\">" +
                        "<value type=\"string\">defaultShell</value>" +
                    "</add-attr>" +
                    "<add-attr attr-name=\"End\">" +
                        "<value type=\"time\">" + endDateSYNTIME + "</value>" +
                    "</add-attr>" +
                    "<add-attr attr-name=\"FirstName\">" +
                        "<value type=\"string\">Test</value>" +
                    "</add-attr>" +
					"<add-attr attr-name=\"LastName\">" +
						"<value type=\"string\">User1</value>" +
					"</add-attr>" +
                    "<add-attr attr-name=\"MemberOf\">" +
                        "<value type=\"string\">" + group1 + "</value>" +
                        "<value type=\"string\">" + group2 + "</value>" +
                    "</add-attr>" +
                    "<add-attr attr-name=\"ProfileName\">" +
                        "<value type=\"string\">" + profileName + "</value>" +
                    "</add-attr>" +
                    "<add-attr attr-name=\"Start\">" +
                        "<value type=\"time\">" + startDateSYNTIME + "</value>" +
                    "</add-attr>" +
                    "<add-attr attr-name=\"TempUser\">" +
                        "<value type=\"string\">TRUE</value>" +
                    "</add-attr>" +
					"<add-attr attr-name=\"TokenSerialNumber\">" +
						"<value type=\"string\">" + tokenSerialNum + "</value>" +
						"<value type=\"string\">" + tokenSerialNum2 + "</value>" +
					"</add-attr>" +
				"</add>" +
			"</input>" +
		"</nds>";
    
	public void testUserAdd() throws Exception, TransformerConfigurationException, TransformerException, SAXException, IOException, InterruptedException {
    	publisher = new PublisherQueue(driver, 20 * 1000);
        Thread pubThread = new Thread(publisher);
        pubThread.start();
        Thread.sleep(10 * 1000);

        try {
	    	api.assignToken("User1", "Test", user.substring(1), null, "defaultShell", tokenSerialNum, TestUtil.DEFAULT_USER_PASSWORD);
	    	api.assignAnotherToken(user, tokenSerialNum2);
	    	api.setTempUser(startDateYYMMDD, startHourTOD, endDateYYMMDD, endHourTOD, user);    	
	    	api.addLoginToGroup("", group1, "defaultShell", user);
	    	api.addLoginToGroup("", group2, "defaultShell", user);
	    	api.assignProfile(user, profileName);    	
	
	        XmlDocument event = null;
	    	
			try {
				event = publisher.getEventDocument();
			} catch (InterruptedException e) {
			}		
	
			publisher.setResponseDocument(TestPublisher.standardSuccessResponse);
			if (event == null) {
				fail("Timeout expired before publisher returned a response!");
			}
	        TestUtil.printDocumentToScreen("testUserAdd2", event.getDocument());
	        this.assertXMLEqual(XMLUnit.buildControlDocument(addRequest), event.getDocument());
        } finally {
        	api.deleteUser(user);
        }
    }

//TODO: What is this test trying to show. It is very non-deterministic and needs comments when we have figured it out. NOTE: This may be very 5.x/6.x specific and may not apply to 7.x
//	private static final String addRequestInvalidModify =
//		"<nds dtdversion=\"2.0\">" +
//			"<source>" + 
//                "<contact>TriVir</contact>" +
//                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
//			"</source>" +
//			"<input>" + 
//				"<add class-name=\"User\" event-id=\"0\">" +
//                    "<association>" + user.substring(1)+ "</association>" +
//					"<add-attr attr-name=\"DefaultLogin\">" +
//						"<value type=\"string\">" + user.substring(1) + "</value>" +
//					"</add-attr>" +
//                    "<add-attr attr-name=\"DefaultShell\">" +
//                        "<value type=\"string\">defaultShell</value>" +
//                    "</add-attr>" +
//                    "<add-attr attr-name=\"End\">" +
//                        "<value type=\"time\">" + endDateSYNTIME + "</value>" +
//                    "</add-attr>" +
//                    "<add-attr attr-name=\"FirstName\">" +
//                        "<value type=\"string\">Test</value>" +
//                    "</add-attr>" +
//					"<add-attr attr-name=\"LastName\">" +
//						"<value type=\"string\">User1</value>" +
//					"</add-attr>" +
//                    "<add-attr attr-name=\"MemberOf\">" +
//                        "<value type=\"string\">" + group1 + "</value>" +
//                        "<value type=\"string\">" + group2 + "</value>" +
//                    "</add-attr>" +
//                    "<add-attr attr-name=\"ProfileName\">" +
//                        "<value type=\"string\">" + profileName + "</value>" +
//                    "</add-attr>" +
//                    "<add-attr attr-name=\"Start\">" +
//                        "<value type=\"time\">" + startDateSYNTIME + "</value>" +
//                    "</add-attr>" +
//                    "<add-attr attr-name=\"TempUser\">" +
//                        "<value type=\"string\">TRUE</value>" +
//                    "</add-attr>" +
//					"<add-attr attr-name=\"TokenSerialNumber\">" +
//						"<value type=\"string\">" + tokenSerialNum + "</value>" +
//						"<value type=\"string\">" + tokenSerialNum2 + "</value>" +
//					"</add-attr>" +
//				"</add>" +
//			"</input>" +
//		"</nds>";
//
//    public void testUserAddShowInvalidModifyAfterAdd() throws Exception, TransformerConfigurationException, TransformerException, SAXException, IOException, InterruptedException {
//    	publisher = new PublisherQueue(driver, 20 * 1000); // wait should be set to 10 seconds or higher to confirm secondary modify isn't sent.
//        Thread pubThread = new Thread(publisher);
//        pubThread.start();
//        Thread.sleep(10 * 1000);
//
//        try {
//			api.assignToken("User1", "Test", user.substring(1), "defaultShell", tokenSerialNum);
//	    	api.assignAnotherToken(user, tokenSerialNum2);
//	    	api.setTempUser(startDateYYMMDD, startHourTOD, endDateYYMMDD, endHourTOD, user);    	
//	    	api.addLoginToGroup("", group1, "defaultShell", user);
//	    	api.addLoginToGroup("", group2, "defaultShell", user);
//	    	api.assignProfile(user, profileName);    	
//	    	
//	    	XmlDocument event = null;
//
//	    	System.out.println("Waiting for initial add response event . .");
//			try {
//				event = publisher.getEventDocument();
//			} catch (InterruptedException e) {
//			}		
//			publisher.setResponseDocument(TestPublisher.standardSuccessResponse);
//	
//			if (event == null) {
//				fail("Timeout expired before publisher returned initial add - timeout must be greater than the poll rate, and see the add response!");
//			}
//			AllTests.printDocumentToScreen("testUserAddShowInvalidModifyAfterAdd: confirming normal add came back:", event.getDocument());
//	        this.assertXMLEqual(XMLUnit.buildControlDocument(addRequestInvalidModify), event.getDocument());
//	    	
//			try {
//				System.out.println("Waiting for error event . .");
//				event = publisher.getEventDocument();
//			} catch (InterruptedException e) { }		
//	
//			publisher.setResponseDocument(TestPublisher.standardSuccessResponse);
//			if (event != null) {
//				fail("Shim should not have sent event:\n" + AllTests.getDocumentAsString(event.getDocument()));
//			} 
//        } finally {
//        	api.deleteUser(user);
//        }
//    }
    
//  ====================================================================================================================================
    
    private static final String addRequestNoTokens =
        "<nds dtdversion=\"2.0\">" +
            "<source>" + 
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<input>" + 
                "<add class-name=\"User\" event-id=\"0\">" +
                    "<association>" + user.substring(1) + "</association>" +
                    "<add-attr attr-name=\"DefaultLogin\">" +
                        "<value type=\"string\">" + user.substring(1) + "</value>" +
                    "</add-attr>" +
                    "<add-attr attr-name=\"DefaultShell\">" +
                        "<value type=\"string\">defaultShell</value>" +
                    "</add-attr>" +
                    "<add-attr attr-name=\"End\">" +
                        "<value type=\"time\">" + endDateSYNTIME + "</value>" +
                    "</add-attr>" +
                    "<add-attr attr-name=\"FirstName\">" +
                        "<value type=\"string\">Test</value>" +
                    "</add-attr>" +
                    "<add-attr attr-name=\"LastName\">" +
                        "<value type=\"string\">User1</value>" +
                    "</add-attr>" +
                    "<add-attr attr-name=\"MemberOf\">" +
                        "<value type=\"string\">" + group1 + "</value>" +
                        "<value type=\"string\">" + group2 + "</value>" +
                    "</add-attr>" +
                    "<add-attr attr-name=\"ProfileName\">" +
                        "<value type=\"string\">" + profileName + "</value>" +
                    "</add-attr>" +
                    "<add-attr attr-name=\"Start\">" +
                        "<value type=\"time\">" + startDateSYNTIME + "</value>" +
                    "</add-attr>" +
                    "<add-attr attr-name=\"TempUser\">" +
                        "<value type=\"string\">TRUE</value>" +
                    "</add-attr>" +
                "</add>" +
            "</input>" +
        "</nds>";
    
    public void testUserAddNoTokens() throws Exception, TransformerConfigurationException, TransformerException, SAXException, IOException, InterruptedException {
        publisher = new PublisherQueue(driver, 20 * 1000);
        Thread pubThread = new Thread(publisher);
        pubThread.start();
        Thread.sleep(10 * 1000);

        try {
	        api.addUser("User1", "Test", user.substring(1), null, "defaultShell", TestUtil.DEFAULT_USER_PASSWORD);
	        api.setTempUser(startDateYYMMDD, startHourTOD, endDateYYMMDD, endHourTOD, user);        
	        api.addLoginToGroup("", group1, "defaultShell", user);
	        api.addLoginToGroup("", group2, "defaultShell", user);
	        api.assignProfile(user, profileName);       

	        XmlDocument event = null;
	        
	        try {
	            event = publisher.getEventDocument();
	        } catch (InterruptedException e) {
	        }       
	
	        publisher.setResponseDocument(TestPublisher.standardSuccessResponse);
	        if (event == null) {
	            fail("Timeout expired before publisher returned a response!");
	        }
	        TestUtil.printDocumentToScreen("testUserAddNoTokens", event.getDocument());
	        this.assertXMLEqual(XMLUnit.buildControlDocument(addRequestNoTokens), event.getDocument());
        } finally {
        	api.deleteUser(user);
        }
    }
}
