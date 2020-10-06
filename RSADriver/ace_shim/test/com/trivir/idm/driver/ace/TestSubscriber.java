package com.trivir.idm.driver.ace;

import java.io.IOException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.novell.nds.dirxml.driver.SubscriptionShim;
import com.novell.nds.dirxml.driver.Trace;
import com.novell.nds.dirxml.driver.XmlDocument;
import com.trivir.ace.AceToolkitException;
import com.trivir.ace.api.AceApi;

public class TestSubscriber extends XMLTestCase {
	private AceDriverShim driver;
	private SubscriptionShim subscriber;
    private AceApi api;
    
    public TestSubscriber() {
        Trace.registerImpl(CustomTraceInterface.class, 1);
    }

    protected void setUp() throws Exception {
        driver = new AceDriverShim();
        TestUtil.initializeCache();
        Document response = driver.init(new XmlDocument(TestDriverShim.getInitRequest())).getDocument();
        TestUtil.printDocumentToScreen("SetupDriver", response);
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);
        subscriber = driver.getSubscriptionShim();
        ((SubscriberShim)subscriber).initFilter();

        XmlDocument request = new XmlDocument(TestChannelInit.subscriberInitRequest);
		response = subscriber.init(request).getDocument();
        //AllTests.printDocumentToScreen("SetupSubscriber", response);
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);
        
        api = TestUtil.getApi();

        api.addUser("User1", "Test", "TestUser2", null, "", TestUtil.DEFAULT_USER_PASSWORD);
        ((SubscriberShim)subscriber).rsaData.allUsers = ((SubscriberShim)subscriber).rsaData.listAllUsers();
    }
    
    protected void tearDown() throws Exception {
        try {
            api.listUserInfo("-TestUser2");
            api.deleteUser("-TestUser2");
        } catch (Exception e) {}        
        api.destroy();
    }

//  ====================================================================================================================================
    
    private static final String queryDriverIdentRequest =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 

            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>Novell, Inc.</contact>" +
            "</source>" +
            "<input>" + 
                "<query event-id=\"query-driver-ident\" scope=\"entry\">" +
                    "<search-class class-name=\"__driver_identification_class__\"/>" +
                    "<read-attr/>" +
                "</query>" +
            "</input>" +
        "</nds>";

    private static final String queryDriverIdentResponse =
        "<nds dtdversion=\"2.0\">" +
            "<source>" +
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<output>" +
                "<instance class-name=\"__driver_identification_class__\">" +
                    "<attr attr-name=\"driver-id\">" +
                        "<value type=\"string\">RSA</value>" +
                    "</attr>" +
                    "<attr attr-name=\"driver-version\">" +
//                      "<value type=\"string\">0.1.1</value>" +
                    "<value type=\"string\"></value>" +
                    "</attr>" +
                    "<attr attr-name=\"min-activation-version\">" +
                        "<value type=\"string\">1</value>" +
                    "</attr>" +
//                    "<attr attr-name=\"query-ex-supported\">" + 
//                    	"<value type=\"state\">true</value>" + 
//                    "</attr>" + 
                "</instance>" +
                "<status event-id=\"query-driver-ident\" level=\"success\" type=\"driver-general\"/>" +
            "</output>" +
        "</nds>";

    public void testQueryDriverIdent() throws SAXException, IOException, TransformerConfigurationException, TransformerException {
        XmlDocument request = new XmlDocument(queryDriverIdentRequest);
        Document response = subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testQueryDriverIdent", response);
        this.assertXMLEqual(XMLUnit.buildControlDocument(queryDriverIdentResponse), response);
    }

//  ====================================================================================================================================

    private static final String unSupportedXDSEvent = 
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
        "<source>" + 
          "<product version=\"3.5.1.20070411 \">DirXML</product>" +
          "<contact>Novell, Inc.</contact>" +
        "</source>" +
        "<input>" +
          "<heydosomeodatyo class-name=\"Token\" event-id=\"0\" scope=\"entry\">" +
            "<association>2353424534[TOKEN SERIAL NUMBER GOES HERE]</association>" +
            "<read-attr attr-name=\"TokenExpirationDate\"/>" +
          "</heydosomeodatyo>" +
        "</input>" +
      "</nds>";
        
    private static final String unSupportedXDSEventResponse = 
        "<nds dtdversion=\"2.0\">" + 
            "<source>" + 
                "<contact>TriVir</contact>" + 
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" + 
            "</source>" + 
            "<output>" + 
                "<status event-id=\"0\" level=\"error\" type=\"driver-general\">" + 
                    "<description>Operation &lt;heydosomeodatyo&gt; not supported</description>" + 
                "</status>" + 
            "</output>" + 
        "</nds>";
        
    public void testUnsupportedEvent() throws SAXException, IOException, TransformerException, TransformerConfigurationException {
        XmlDocument request = new XmlDocument(unSupportedXDSEvent);
        Document response = subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUnsupportedEvent", response);
        this.assertXMLEqual(XMLUnit.buildControlDocument(unSupportedXDSEventResponse), response);
    }
    
//  ====================================================================================================================================
    
    private static final String missingInputTagResponse =         
        "<nds dtdversion=\"2.0\">" + 
            "<source>" + 
                "<contact>TriVir</contact>" + 
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" + 
            "</source>" + 
            "<output>" + 
                "<status level=\"error\" type=\"driver-general\">" + 
                    "<description>com.novell.nds.dirxml.driver.xds.XDSParseException: Missing required element &lt;input&gt;.</description>" + 
                    "<exception class-name=\"com.novell.nds.dirxml.driver.xds.XDSParseException\">" + 
                    "<message>Missing required element &lt;input&gt;.</message>" + 
                    "</exception>" + 
                    "<document xml:space=\"preserve\">&lt;nds&gt;" + 
                        "\n\t&lt;empty/&gt;" + 
                    "\n&lt;/nds&gt;</document>" + 
                "</status>" + 
            "</output>" + 
        "</nds>";
    
    public void testMissingInputTag() throws SAXException, IOException, TransformerException, TransformerConfigurationException {        
        XmlDocument request = new XmlDocument("<nds><empty/></nds>"); // sending in doc with 
        Document response = subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testMissingInputTag", response);
        this.assertXMLEqual(XMLUnit.buildControlDocument(missingInputTagResponse), response);
    }

//  ====================================================================================================================================
    
    private static final String deleteRequest =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>TriVir</contact>" +
            "</source>" +
            "<input>" + 
                "<delete class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" qualified-src-dn=\"O=VAULT\\CN=TestUser2\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\" src-entry-id=\"32902\">" +
                    "<association>TestUser2</association>" +
                "</delete>" +
            "</input>" +
        "</nds>";
    
    private static final String deleteResponse =
        "<nds dtdversion=\"2.0\">" +
            "<source>" +
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<output>" +
                "<status event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" level=\"success\" type=\"driver-general\"/>" +
            "</output>" +
        "</nds>";
    
    public void testDeleteUser() throws SAXException, IOException, TransformerException, AceToolkitException {
        XmlDocument request = new XmlDocument(TestSubscriber.deleteRequest);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testDeleteUser: delete response", response);
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriber.deleteResponse), response);
    }
}

