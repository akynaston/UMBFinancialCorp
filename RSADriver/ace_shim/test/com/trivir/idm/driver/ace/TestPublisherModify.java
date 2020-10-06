package com.trivir.idm.driver.ace;

import java.io.IOException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.novell.nds.dirxml.driver.Trace;
import com.novell.nds.dirxml.driver.XmlDocument;
import com.trivir.ace.AceToolkitException;
import com.trivir.ace.api.AceApi;

public class TestPublisherModify extends XMLTestCase {
    private AceDriverShim driver;
    private AceApi api;

    private static final String user = "-TestUser1";
    private static final String newUser = "-NewTestUser2";
    private static final String tokenSerialNum1 = TestUtil.tokenSerialNum;
    private static final String tokenSerialNum2 = TestUtil.tokenSerialNum2;
    private static final String groupName = "TestGroup1";

    public TestPublisherModify() {
        Trace.registerImpl(CustomTraceInterface.class, 1);
    }

    protected void setUp() throws Exception {
    	TestUtil.dropCacheDatabase();
    	TestUtil.initializeCache();
        driver = new AceDriverShim();
        Document response = driver.init(new XmlDocument(TestDriverShim.getInitRequest())).getDocument();
        TestUtil.printDocumentToScreen("setUpShimResponse", response);
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);

        XmlDocument request = new XmlDocument(TestPublisher.getPublisherInitRequest());
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

    private static final String standardSuccessResponse =
        "<nds dtdversion=\"2.0\">" +
            "<source>" +
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<output>" +
                "<status event-id=\"0\" level=\"success\" type=\"driver-general\"/>" +
            "</output>" +
        "</nds>";

    private static final String modifyUserEvent =
        "<nds dtdversion=\"2.0\">" +
            "<source>" + 
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"User\" event-id=\"0\">" +
                    "<association>" + user.substring(1) + "</association>" +
                    "<modify-attr attr-name=\"FirstName\">" +
                        "<remove-value>" +
                            "<value>Test</value>" +
                        "</remove-value>" +
                        "<add-value>" +
                            "<value>NewFirstName</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                    "<modify-attr attr-name=\"LastName\">" +
                        "<remove-value>" +
                            "<value>User</value>" +
                        "</remove-value>" +
                        "<add-value>" +
                            "<value>NewLastName</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +
            "</input>" +
        "</nds>";

    public void testModifyUser() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, XpathException, InterruptedException {
        PublisherQueue publisher = new PublisherQueue(driver, 20 * 1000);
        Thread pubThread = new Thread(publisher);
        pubThread.start();
        Thread.sleep(10 * 1000);

		try {
	        XmlDocument event = null;
			api.addUser("User", "Test", user.substring(1), null, "", TestUtil.DEFAULT_USER_PASSWORD);
	        try {
	            event = publisher.getEventDocument();
	        } catch (InterruptedException e) {
	        }       
	        publisher.setResponseDocument(standardSuccessResponse);
	        if (event == null) {
	            fail("Timeout expired before publisher returned a response!");
	        }

	        publisher = new PublisherQueue(driver, 10000);
	        pubThread = new Thread(publisher);
	        pubThread.start();
	        Thread.sleep(1000);

	        api.setUser("NewLastName", "NewFirstName", user.substring(1), null, "", user, null);

	        event = null;
	        
	        try {
	            event = publisher.getEventDocument();
	        } catch (InterruptedException e) {
	        }       

	        publisher.setResponseDocument(standardSuccessResponse);

	        if (event == null) {
	            fail("Timeout expired before publisher returned a response!");
	        }
	        TestUtil.printDocumentToScreen("testModifyUser", event.getDocument());
	        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyUserEvent), event.getDocument());
		} finally {
			api.deleteUser(user);
		}

    }

    // =========================================================================================================
    
    private static final String modifyUserAddTokenResponse =
        "<nds dtdversion=\"2.0\">" +
            "<source>" + 
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"User\" event-id=\"0\">" +
                    "<association>" + user.substring(1) + "</association>" +
                    "<modify-attr attr-name=\"TokenSerialNumber\">" +
                        "<add-value>" +
                        	"<value>" + tokenSerialNum1 + "</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +
            "</input>" +
        "</nds>";

    public void testModifyUserAddToken() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, XpathException, InterruptedException {
        PublisherQueue publisher = new PublisherQueue(driver, 20 * 1000);
        Thread pubThread = new Thread(publisher);
        pubThread.start();
        Thread.sleep(10 * 1000);
        
        try {
	        XmlDocument event = null;
			api.addUser("User", "Test", user.substring(1), null, "", TestUtil.DEFAULT_USER_PASSWORD);
	        try {
	            event = publisher.getEventDocument();
	        } catch (InterruptedException e) {
	        }       
	        publisher.setResponseDocument(standardSuccessResponse);
	        if (event == null) {
	            fail("Timeout expired before publisher returned a response!");
	        }

	        publisher = new PublisherQueue(driver, 20000);
	        pubThread = new Thread(publisher);
	        pubThread.start();
	        Thread.sleep(10 * 1000);

	        api.assignAnotherToken(user, tokenSerialNum1);
	
	        event = null;
	        
	        try {
	            event = publisher.getEventDocument();
	        } catch (InterruptedException e) {
	        }       
	
	        publisher.setResponseDocument(standardSuccessResponse);
	
	        if (event == null) {
	            fail("Timeout expired before publisher returned a response!");
	        }
	        TestUtil.printDocumentToScreen("testModifyUserAddToken", event.getDocument());
	        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyUserAddTokenResponse), event.getDocument());
        } finally {
			api.deleteUser(user);
        }
    }

    // =========================================================================================================
    
    private static final String modifyUserAddSecondaryTokenResponseWithToken1 =
        "<nds dtdversion=\"2.0\">" +
            "<source>" + 
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"User\" event-id=\"0\">" +
                    "<association>" + user.substring(1) + "</association>" +
                    "<modify-attr attr-name=\"TokenSerialNumber\">" +
                        "<add-value>" +
                        	"<value>" + tokenSerialNum1 + "</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +
            "</input>" +
        "</nds>";

    private static final String modifyUserAddSecondaryTokenResponseWithToken2 =
        "<nds dtdversion=\"2.0\">" +
            "<source>" + 
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"User\" event-id=\"0\">" +
                    "<association>" + user.substring(1) + "</association>" +
                    "<modify-attr attr-name=\"TokenSerialNumber\">" +
                        "<add-value>" +
                        	"<value>" + tokenSerialNum2 + "</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +
            "</input>" +
        "</nds>";

    public void testModifyUserAddAdditionalToken() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, XpathException, InterruptedException {
        PublisherQueue publisher = new PublisherQueue(driver, 20 * 1000);
        Thread pubThread = new Thread(publisher);
        pubThread.start();
        Thread.sleep(10 * 1000);

        try {
	        XmlDocument event = null;
			api.addUser("User", "Test", user.substring(1),null, "", TestUtil.DEFAULT_USER_PASSWORD);
	        try {
	            event = publisher.getEventDocument();
	        } catch (InterruptedException e) {
	        }       
	        publisher.setResponseDocument(standardSuccessResponse);
	        if (event == null) {
	            fail("Timeout expired before publisher returned a response!");
	        }

	        publisher = new PublisherQueue(driver, 20 * 1000);
	        pubThread = new Thread(publisher);
	        pubThread.start();
	        Thread.sleep(10 * 1000);

	        api.assignAnotherToken(user, tokenSerialNum1);
	        event = null;
	        try {
	            event = publisher.getEventDocument();
	        } catch (InterruptedException e) {
	        }       
	        publisher.setResponseDocument(standardSuccessResponse);
	        if (event == null) {
	            fail("Timeout expired before publisher returned a response!");
	        }
	        TestUtil.printDocumentToScreen("testModifyUserAddAdditionalToken", event.getDocument());
	        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyUserAddSecondaryTokenResponseWithToken1), event.getDocument());
	        try {
	            event = publisher.getEventDocument();
	        } catch (InterruptedException e) {
	        }       
	        publisher.setResponseDocument(standardSuccessResponse);
	        
	    	
	        publisher = new PublisherQueue(driver, 20 * 1000);
	        pubThread = new Thread(publisher);
	        pubThread.start();
	        Thread.sleep(10 * 1000);
	
	        api.assignAnotherToken(user, tokenSerialNum2);
	        event = null;
	        try {
	            event = publisher.getEventDocument();
	        } catch (InterruptedException e) {
	        }       
	        publisher.setResponseDocument(standardSuccessResponse);
	        if (event == null) {
	            fail("Timeout expired before publisher returned a response!");
	        }
	        TestUtil.printDocumentToScreen("testModifyUserAddAdditionalToken", event.getDocument());
	        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyUserAddSecondaryTokenResponseWithToken2), event.getDocument());
        } finally {
        	api.deleteUser(user);
        }
    }

    // =========================================================================================================
    
    private static final String modifyUserAddGroupResponse =
        "<nds dtdversion=\"2.0\">" +
            "<source>" + 
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"User\" event-id=\"0\">" +
                    "<association>" + user.substring(1) + "</association>" +
                    "<modify-attr attr-name=\"MemberOf\">" +
                        "<add-value>" +
                        	"<value>" + groupName + "</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +
            "</input>" +
        "</nds>";

    public void testModifyUserAddGroup() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, XpathException, InterruptedException {
        PublisherQueue publisher = new PublisherQueue(driver, 20 * 1000);
        Thread pubThread = new Thread(publisher);
        pubThread.start();
        Thread.sleep(10 * 1000);

        try {
	        XmlDocument event = null;
			api.addUser("User", "Test", user.substring(1), null, "", TestUtil.DEFAULT_USER_PASSWORD);
	        try {
	            event = publisher.getEventDocument();
	        } catch (InterruptedException e) {
	        }       
	        publisher.setResponseDocument(standardSuccessResponse);
	        if (event == null) {
	            fail("Timeout expired before publisher returned a response!");
	        }

	        publisher = new PublisherQueue(driver, 20 * 1000);
	        pubThread = new Thread(publisher);
	        pubThread.start();
	        Thread.sleep(10 * 1000);

	        api.addLoginToGroup("", groupName, "", user);
	
	        event = null;
	        
	        try {
	            event = publisher.getEventDocument();
	        } catch (InterruptedException e) {
	        }       
	
	        publisher.setResponseDocument(standardSuccessResponse);
	
	        if (event == null) {
	            fail("Timeout expired before publisher returned a response!");
	        }
	        TestUtil.printDocumentToScreen("testModifyUserAddGroup", event.getDocument());
	        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyUserAddGroupResponse), event.getDocument());
        } finally {
        	api.deleteUser(user);
        }
    }

    // =========================================================================================================
    
    private static final String modifyUserAddFirstName =
        "<nds dtdversion=\"2.0\">" +
            "<source>" + 
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"User\" event-id=\"0\">" +
                    "<association>" + user.substring(1) + "</association>" +
                    "<modify-attr attr-name=\"FirstName\">" +
                        "<add-value>" +
                        	"<value>FIRSTNAMEADDED</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +
            "</input>" +
        "</nds>";

    public void testModifyUserAddFirstNameToUserMissingFirstName() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, XpathException, InterruptedException {
        PublisherQueue publisher = new PublisherQueue(driver, 20 * 1000);
        Thread pubThread = new Thread(publisher);
        pubThread.start();
        Thread.sleep(10 * 1000);

        try {
	        XmlDocument event = null;
            api.addUser("User", "", user.substring(1), null, "", TestUtil.DEFAULT_USER_PASSWORD);
	        try {
	            event = publisher.getEventDocument();
	        } catch (InterruptedException e) {
	        }       
	        publisher.setResponseDocument(standardSuccessResponse);
	        if (event == null) {
	            fail("Timeout expired before publisher returned a response!");
	        }
            TestUtil.printDocumentToScreen("testModifyUserAddFirstNameToUserMissingFirstName", event.getDocument());

	        publisher = new PublisherQueue(driver, 20 * 1000);
	        pubThread = new Thread(publisher);
	        pubThread.start();
	        Thread.sleep(10 * 1000);

            api.setUser("User", "FIRSTNAMEADDED", user.substring(1), null, "", user, null);
            
            event = null;
            
            try {
                event = publisher.getEventDocument();
            } catch (InterruptedException e) {
            }       

            publisher.setResponseDocument(standardSuccessResponse);

            if (event == null) {
                fail("Timeout expired before publisher returned a response!");
            }
            TestUtil.printDocumentToScreen("testModifyUserAddFirstNameToUserMissingFirstName", event.getDocument());
            this.assertXMLEqual(XMLUnit.buildControlDocument(modifyUserAddFirstName), event.getDocument());
        } finally {
            api.deleteUser(user);
        }
            	
    }
    private static final String modifyUserDefaultLoginEvent =
        "<nds dtdversion=\"2.0\">" +
            "<source>" + 
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<input>" + 
            	"<modify-association>" + 
            		"<association>TestUser1</association>" +
            		"<association>NewTestUser2</association>" +
            	"</modify-association>" +
                "<modify class-name=\"User\" event-id=\"0\">" +
                    "<association>" + newUser.substring(1) + "</association>" +
                    "<modify-attr attr-name=\"DefaultLogin\">" +
                        "<remove-value>" +
                        	"<value>TestUser1</value>" +
                        "</remove-value>" +
                        "<add-value>" +
                        	"<value>NewTestUser2</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +
            "</input>" +
        "</nds>";

    public void testModifyUserDefaultLogin() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, XpathException, InterruptedException {
        PublisherQueue publisher = new PublisherQueue(driver, 20 * 1000);
        Thread pubThread = new Thread(publisher);
        pubThread.start();
        Thread.sleep(10 * 1000);

		try {
	        XmlDocument event = null;
			api.addUser("User", "Test", user.substring(1), null, "", TestUtil.DEFAULT_USER_PASSWORD);
	        try {
	            event = publisher.getEventDocument();
	        } catch (InterruptedException e) {
	        }       
	        publisher.setResponseDocument(standardSuccessResponse);
	        if (event == null) {
	            fail("Timeout expired before publisher returned a response!");
	        }

	        publisher = new PublisherQueue(driver, 10000);
	        pubThread = new Thread(publisher);
	        pubThread.start();
	        Thread.sleep(1000);

	        api.setUser("User", "Test", newUser.substring(1), null, "", user, null);

	        event = null;
	        
	        try {
	            event = publisher.getEventDocument();
	        } catch (InterruptedException e) {
	        }       

	        publisher.setResponseDocument(standardSuccessResponse);

	        if (event == null) {
	            fail("Timeout expired before publisher returned a response!");
	        }
	        TestUtil.printDocumentToScreen("testModifyUser", event.getDocument());
	        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyUserDefaultLoginEvent), event.getDocument());
		} finally {
			api.deleteUser(newUser);
		}

    }

    
}
