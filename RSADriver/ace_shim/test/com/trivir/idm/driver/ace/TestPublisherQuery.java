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

public class TestPublisherQuery extends XMLTestCase {
    private AceDriverShim driver;
    private AceApi api;

    public TestPublisherQuery() {
    	Trace.registerImpl(CustomTraceInterface.class, 1);
	}
        
    private static final String tokenSerialNum = "000042414140"; // This token should not be used in other tests. It's usage will cause several attributes to change causing test failures below.

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
							"<allow-attr attr-name=\"FirstName\"/>" +
							"<allow-attr attr-name=\"LastName\"/>" +
							"<allow-attr attr-name=\"TokenSerialNumber\"/>" +
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
        TestUtil.printDocumentToScreen("Setup initresponse", response);
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);

        XmlDocument request = new XmlDocument(publisherInitRequestFourSecondPoll);
        TestUtil.printDocumentToScreen("Setup request:", request.getDocument());
        response = driver.getPublicationShim().init(request).getDocument();
        //AllTests.printDocumentToScreen("Setup", response);
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);
        
        //publisher = new PublisherQueue(driver, 0);
        
        api = TestUtil.getApi();     
//		try {
//			api.addUser("User", "Test", "tuser", "", TestUtil.DEFAULT_USER_PASSWORD);
//		} catch (AceToolkitException e) {
//			api.deleteUser("-tuser");
//			fail();
//		}

    }
    
    private static final String shutdown =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<input/>" +
        "</nds>";
    
    protected void tearDown() throws Exception {
        driver.shutdown(new XmlDocument(shutdown));
//        try {        	
//        	//api = new AceToolkit();
//			api.deleteUser("-tuser");
//		} catch (AceToolkitException e) {
//			if ("Database connection is not established!".equalsIgnoreCase(e.getMessage())) {
//				api.destroy();
//				api = TestUtil.getApi();
//				api.deleteUser("-tuser");
//			} else if ("User with login 'tuser' not found.".equalsIgnoreCase(e.getMessage())) {
//                // user already deleted . .
//            } else {
//				throw e;
//			}
//		}
		api.destroy();
    }
    
    private static final String queryEntryXDSInvalid =
		"<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
			"<source>" +
				"<product version=\"3.5.1.20070411 \">DirXML</product>" +
				"<contact>Novell, Inc.</contact>" +
			"</source>" +
			"<input>" +
				"<query class-name=\"Token\" event-id=\"0\" scope=\"entry\">" +
					"<read-attr attr-name=\"Disabled\"/>" +
				"</query>" +
			"</input>" +
		"</nds>";
    
    private static final String queryEntryXDSResponseInvalid =
		"<nds dtdversion=\"2.0\">" +
			"<source>" +
				"<contact>TriVir</contact>" +
				"<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
			"</source>" +
			"<output>" +
				// Not sure what goes here, but it shouldn't be a null pointer exception and stop the driver . ..
				"<status event-id=\"0\" level=\"error\" type=\"driver-general\">" +
            		"<description>Invalid query, missing association.</description>" +
            	"</status>" +
            "</output>" +
		"</nds>";
	
    public void testQueryInvalid() throws AceToolkitException, TransformerConfigurationException, TransformerException, SAXException, IOException, XpathException {
        XmlDocument request = new XmlDocument(queryEntryXDSInvalid);
        QueryHandler queryHandler = new QueryHandler(driver, driver.rsaData);
		Document response = queryHandler.query(request).getDocument();
		TestUtil.printDocumentToScreen("testQueryInvalid", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(queryEntryXDSResponseInvalid), response);
    }

    private static final String queryTokenImplicitAllAttrs =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" +
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>Novell, Inc.</contact>" +
            "</source>" +
            "<input>" +
                "<query class-name=\"Token\" event-id=\"0\" scope=\"entry\">" +
                    "<association>" + tokenSerialNum + "</association>" +
                "</query>" +
            "</input>" +
        "</nds>";
    
    private static final String queryResponseTokenImplicitAllAttrs =
        "<nds dtdversion=\"2.0\">" +
            "<source>" +
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<output>" +
                "<instance class-name=\"Token\">" + 
                    "<association>" + tokenSerialNum + "</association>" + 
                    "<attr attr-name=\"Death\">" +
                    	"<value>1490918400</value>" +
                    "</attr>" +
                    "<attr attr-name=\"Interval\">" +
	                    "<value>60</value>" +
	                "</attr>" +
                    "<attr attr-name=\"Count\">" +
	                    "<value>0</value>" +
	                "</attr>" +
                    "<attr attr-name=\"FormFactor\">" +
	                    "<value>00100000000000000000000000000001</value>" +
	                "</attr>" +
                    "<attr attr-name=\"Type\">" +
                        "<value>4</value>" +
                    "</attr>" +
                    "<attr attr-name=\"PINChangedDate\">" +
	                    "<value>0</value>" +
	                "</attr>" +
//                    "<attr attr-name=\"Assignment\">" +
//	                	"<value>1362091138</value>" +
//	                "</attr>" +
                    "<attr attr-name=\"CountsLastModified\">" +
	                    "<value>0</value>" +
	                "</attr>" +
                    "<attr attr-name=\"Protected\">" +
	                    "<value>FALSE</value>" +
	                "</attr>" +
                    "<attr attr-name=\"SerialNum\">" +
	                    "<value>" + tokenSerialNum + "</value>" +
	                "</attr>" +
                    "<attr attr-name=\"NumDigits\">" +
	                    "<value>8</value>" +
	                "</attr>" +
                    "<attr attr-name=\"FirstLogin\">" +
	                    "<value>TRUE</value>" +
	                "</attr>" +
                    "<attr attr-name=\"Disabled\">" +
	                    "<value>TRUE</value>" +
	                "</attr>" +
                    "<attr attr-name=\"PINClear\">" +
	                    "<value>FALSE</value>" +
	                "</attr>" +
                    "<attr attr-name=\"Version\">" +
	                    "<value>1</value>" +
	                "</attr>" +
                    "<attr attr-name=\"PINType\">" +
	                    "<value>0</value>" +
	                "</attr>" +
                    "<attr attr-name=\"LocalPIN\">" +
	                    "<value>FALSE</value>" +
	                "</attr>" +
                    "<attr attr-name=\"Deployed\">" +
	                    "<value>FALSE</value>" +
	                "</attr>" +
                    "<attr attr-name=\"Assigned\">" +
                        "<value>FALSE</value>" +
                    "</attr>" +
                    "<attr attr-name=\"Birth\">" +
	                    "<value>1172448000</value>" +
	                "</attr>" +
                    "<attr attr-name=\"Hex\">" +
	                    "<value>FALSE</value>" +
	                "</attr>" +
                    "<attr attr-name=\"LastLogin\">" +
	                    "<value>0</value>" +
	                "</attr>" +
                    "<attr attr-name=\"Keypad\">" +
	                    "<value>TRUE</value>" +
	                "</attr>" +
                    "<attr attr-name=\"DisabledDate\">" +
	                    "<value>0</value>" +
	                "</attr>" +
                    "<attr attr-name=\"NewPINMode\">" +
	                    "<value>TRUE</value>" +
	                "</attr>" +
                    "<attr attr-name=\"BadTokenCodes\">" +
                        "<value>0</value>" +
                    "</attr>" +
//                    "<attr attr-name=\"SeedSize\">" +
//                        "<value>128</value>" +
//                    "</attr>" +
//                    "<attr attr-name=\"NextCodeStatus\">" +
//                        "<value>0</value>" +
//                    "</attr>" +
//                    "<attr attr-name=\"BadPINs\">" +
//                        "<value>0</value>" +
//                    "</attr>" +
//                    "<attr attr-name=\"Deployment\">" +
//                        "<value>0</value>" +
//                    "</attr>" +
                "</instance>" +
                "<status event-id=\"0\" level=\"success\" type=\"driver-general\"/>" +
            "</output>" +
        "</nds>";
    
    public void testQueryTokenImplicitAllAttrs() throws AceToolkitException, TransformerConfigurationException, TransformerException, SAXException, IOException, XpathException {
        XmlDocument request = new XmlDocument(queryTokenImplicitAllAttrs);
        QueryHandler queryHandler = new QueryHandler(driver, driver.rsaData);
        Document response = queryHandler.query(request).getDocument();
        TestUtil.printDocumentToScreen("testQueryTokenImplicitAllAttrs", response);
        this.assertXMLEqual(XMLUnit.buildControlDocument(queryResponseTokenImplicitAllAttrs), response);
    }
    
    private static final String queryEntryXDSTokenDisabled =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" +
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>Novell, Inc.</contact>" +
            "</source>" +
            "<input>" +
                "<query class-name=\"Token\" event-id=\"0\" scope=\"entry\">" +
                    "<association>" + tokenSerialNum + "</association>" +
                    "<read-attr attr-name=\"Disabled\"/>" +
                "</query>" +
            "</input>" +
        "</nds>";
    
    private static final String queryEntryXDSResponseDisabled =
        "<nds dtdversion=\"2.0\">" +
            "<source>" +
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<output>" +
                "<instance class-name=\"Token\">" + 
                    "<association>" + tokenSerialNum + "</association>" + 
                    "<attr attr-name=\"Disabled\">" +
                        "<value>TRUE</value>" +
                    "</attr>" +                
                "</instance>" +
                "<status event-id=\"0\" level=\"success\" type=\"driver-general\"/>" +
            "</output>" +
        "</nds>";
    
    public void testQuery() throws AceToolkitException, TransformerConfigurationException, TransformerException, SAXException, IOException, XpathException {
        XmlDocument request = new XmlDocument(queryEntryXDSTokenDisabled);
        QueryHandler queryHandler = new QueryHandler(driver, driver.rsaData);
        Document response = queryHandler.query(request).getDocument();
        TestUtil.printDocumentToScreen("testQuery", response);
        this.assertXMLEqual(XMLUnit.buildControlDocument(queryEntryXDSResponseDisabled), response);
    }
}
