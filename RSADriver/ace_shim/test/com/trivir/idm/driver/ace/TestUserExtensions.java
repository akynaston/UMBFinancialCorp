package com.trivir.idm.driver.ace;

//import java.io.IOException;
//
//import javax.xml.transform.TransformerConfigurationException;
//import javax.xml.transform.TransformerException;
//
import org.custommonkey.xmlunit.XMLTestCase;
//import org.custommonkey.xmlunit.XMLUnit;
//import org.custommonkey.xmlunit.exceptions.XpathException;
//import org.w3c.dom.Document;
//import org.xml.sax.SAXException;
//
//import com.novell.nds.dirxml.driver.Trace;
//import com.novell.nds.dirxml.driver.XmlDocument;
//import com.trivir.ace.AceToolkit;
//import com.trivir.ace.AceToolkitException;
//import com.trivir.ace.api.AceApi;

public class TestUserExtensions extends XMLTestCase {
/*
	private AceDriverShim driver;
    private PublisherQueue publisher;
    private AceApi api;
    
    public TestUserExtensions() {
        Trace.registerImpl(CustomTraceInterface.class, 1);
    }
    
    private static final String publisherInitRequestFourSecondPoll =
        "<nds dtdversion=\"2.0\">" + 
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" + 
                "<contact>Novell, Inc.</contact>" + 
            "</source>" + 
            "<input>" + 
                "<init-params src-dn=\"\\TESTTREE\\resources\\EAPDrivers\\RSADriver\\Publisher\">" + 
                    "<authentication-info>" + 
                        "<server>REMOTE(hostname=172.17.2.38 port=8090 )</server>" + 
                        "<password><!-- content suppressed --></password>" + 
                    "</authentication-info>" + 
                    "<driver-filter>" + 
                        "<allow-class class-name=\"User\">" + 
                            "<allow-attr attr-name=\"DefaultLogin\"/>" + 
                            "<allow-attr attr-name=\"DefaultShell\"/>" + 
                            "<allow-attr attr-name=\"FirstName\"/>" +
                            "<allow-attr attr-name=\"LastName\"/>" +
                            "<allow-attr attr-name=\"extension1\"/>" +
                        "</allow-class>" + 
                        "<allow-class class-name=\"Token\">" + 
                            "<allow-attr attr-name=\"UserNum\"/>" +
                        "</allow-class>" + 
                    "</driver-filter>" + 
                    "<publisher-options>" + 
                        "<pollRate display-name=\"Polling interval in minutes\">0</pollRate>" +
                        "<pub-heartbeat-interval display-name=\"Heartbeat interval in minutes\">0</pub-heartbeat-interval>" +                               
                    "</publisher-options>" + 
                "</init-params>" + 
            "</input>" + 
        "</nds>";

    protected void setUp() throws Exception {
        driver = new AceDriverShim();
        Document response = driver.init(new XmlDocument(TestDriverShim.getInitRequest())).getDocument();
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);

        publisher = new PublisherQueue(driver, 0);
        
        api = TestUtil.getApi();        
    }

    private void initPublisher() throws TransformerException, XpathException {
        XmlDocument request = new XmlDocument(publisherInitRequestFourSecondPoll);
        AllTests.printDocumentToScreen("Setup request:", request.getDocument());
        Document response = driver.getPublicationShim().init(request).getDocument();
        //AllTests.printDocumentToScreen("Setup", response);
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);
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

    private static final String shutdown =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<input/>" +
        "</nds>";
    
    protected void tearDown() throws Exception {
        driver.shutdown(new XmlDocument(shutdown));
        try {           
            //atk = new AceToolkit();
            api.deleteUser("-tuser");
        } catch (AceToolkitException e) {
            if ("Database connection is not established!".equalsIgnoreCase(e.getMessage())) {
                api.destroy();
                api = TestUtil.getApi();
                api.deleteUser("-tuser");
            } else if ("Sd_DeleteUser Error Invalid user".equalsIgnoreCase(e.getMessage())) {
                // user already deleted . .
            } else {
                throw e;
            }
        }
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
                    "<association>tuser</association>" +
                    "<add-attr attr-name=\"DefaultLogin\">" +
                        "<value type=\"string\">tuser</value>" +
                    "</add-attr>" +
                    "<add-attr attr-name=\"FirstName\">" +
                        "<value type=\"string\">Test</value>" +
                    "</add-attr>" +
                    "<add-attr attr-name=\"LastName\">" +
                        "<value type=\"string\">User</value>" +
                    "</add-attr>" +
                    "<add-attr attr-name=\"extension1\">" +
                        "<value type=\"string\">value1</value>" +
                    "</add-attr>" +
                "</add>" +
            "</input>" +
        "</nds>";
    
    public void testUserAdd() throws AceToolkitException, TransformerConfigurationException, TransformerException, SAXException, IOException, XpathException {
        initPublisher();
        publisher = new PublisherQueue(driver, 10000 * 1000);
        Thread pubThread = new Thread(publisher);
        pubThread.start();

        api.addUser("User", "Test", "tuser", "", TestUtil.DEFAULT_USER_PASSWORD);
        api.addUserExtension("extension1", "value1", "-tuser");

        XmlDocument event = null;
        
        try {
            event = publisher.getEventDocument();
        } catch (InterruptedException e) {
        }       

        publisher.setResponseDocument(standardSuccessResponse);
        if (event == null) {
            assertFalse("Timeout expired before publisher returned a response!", true);
        }
        AllTests.printDocumentToScreen("testUserAdd2", event.getDocument());
        this.assertXMLEqual(XMLUnit.buildControlDocument(addRequest), event.getDocument());
    }
    
    private static final String modifyUserEvent =
        "<nds dtdversion=\"2.0\">" +
            "<source>" + 
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"User\" event-id=\"0\">" +
                    "<association>" + "tuser" + "</association>" +
                    "<modify-attr attr-name=\"extension1\">" +
                        "<add-value>" +
                            "<value>value1</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +
            "</input>" +
        "</nds>";

    public void testModifyUser() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, XpathException {
        api.addUser("User", "Test", "tuser", "", TestUtil.DEFAULT_USER_PASSWORD);
        initPublisher();
        
        PublisherQueue publisher = new PublisherQueue(driver, 10000 * 1000);
        Thread pubThread = new Thread(publisher);
        pubThread.start();

        api.addUserExtension("extension1", "value1", "-tuser");

        XmlDocument event = null;
        
        try {
            event = publisher.getEventDocument();
        } catch (InterruptedException e) {
        }       

        publisher.setResponseDocument(standardSuccessResponse);

        if (event == null) {
            fail("Timeout expired before publisher returned a response!");
        }
        AllTests.printDocumentToScreen("testUserAdd2", event.getDocument());
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyUserEvent), event.getDocument());
    }
*/
}
