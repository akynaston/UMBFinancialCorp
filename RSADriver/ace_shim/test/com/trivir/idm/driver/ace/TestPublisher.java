package com.trivir.idm.driver.ace;

import java.io.IOException;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringEscapeUtils;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.novell.nds.dirxml.driver.Trace;
import com.novell.nds.dirxml.driver.XmlDocument;

public class TestPublisher extends XMLTestCase {
    private AceDriverShim driver;
    private PublisherQueue publisher;
    
    public TestPublisher() {
        Trace.registerImpl(CustomTraceInterface.class, 1);
    }

    protected void setUp() throws Exception {
        driver = new AceDriverShim();
        Document response = driver.init(new XmlDocument(TestDriverShim.getInitRequest())).getDocument();
        TestUtil.printDocumentToScreen("setUpDriver", response);
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);

    }

    static final String shutdown =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<input/>" +
        "</nds>";
    
    protected void tearDown() throws Exception {
        driver.shutdown(new XmlDocument(shutdown));
    }

    private static final String heartbeatResponse =
        "<nds dtdversion=\"2.0\">" +
            "<source>" +
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<input>" +
                "<status level=\"success\" type=\"heartbeat\"/>" +
            "</input>" +
        "</nds>";
    
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
        
    static final String[] userFilter = new String[] {"DefaultLogin", "DefaultShell", "FirstName", "LastName", "TokenSerialNumber", "TokenPIN", "ProfileName", "MemberOf", "TempUser", "Start", "End", "CreatePIN", "MustCreatePIN"};
    static final String[] tokenFilter = new String[] {"TokenSerialNumber", "TokenPIN", "Disabled"};
    
	static String getPublisherInitRequest() {
		return getPublisherInitRequest(true, true);
	}

	static String getPublisherInitRequest(boolean defaultUserFilter, boolean defaultTokenFilter) {
		return getPublisherInitRequest(defaultUserFilter ? userFilter : null, defaultTokenFilter ? tokenFilter : null);
	}

	static String getPublisherInitRequest(String[] userFilter, String[] tokenFilter) {
		String publisherInitRequest =
			"<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" + 
				"<source>" + 
					"<product version=\"3.5.1.20070411 \">DirXML</product>" + 
					"<contact>Novell, Inc.</contact>" + 
				"</source>" + 
				"<input>" + 
					"<init-params src-dn=\"\\TESTTREE\\resources\\EAPDrivers\\RSADriver\\Publisher\">" + 
						"<authentication-info>"; 
		for(Map.Entry<String, String> authParam : TestUtil.getAuthParams().entrySet()) {
			publisherInitRequest += String.format("<%s>%s</%s>", authParam.getKey(), StringEscapeUtils.escapeXml(authParam.getValue()), authParam.getKey());
		}
		publisherInitRequest +=
						"</authentication-info>" + 
						"<driver-filter>";
		if (userFilter != null) {
			publisherInitRequest += 
							"<allow-class class-name=\"User\">";
			for (String attr : userFilter) {
				publisherInitRequest += 
								"<allow-attr attr-name=\""+attr+"\"/>"; 
 
			}
			publisherInitRequest += 
							"</allow-class>"; 
		}
		if (tokenFilter != null) {
			publisherInitRequest +=
							"<allow-class class-name=\"Token\">";
			for (String attr : tokenFilter) {
				publisherInitRequest +=
								"<allow-attr attr-name=\""+attr+"\"/>";
			}
			publisherInitRequest +=
							"</allow-class>"; 
		}
		publisherInitRequest +=
						"</driver-filter>" + 
						"<publisher-options>" + 
							"<pollRate display-name=\"Polling Interval in Seconds\">10</pollRate>" +
							"<pub-heartbeat-interval display-name=\"Heartbeat interval in minutes\">0</pub-heartbeat-interval>" +								
						"</publisher-options>" + 
					"</init-params>" + 
				"</input>" + 
			"</nds>";
		return publisherInitRequest;
	}

	public void testHeartBeat() throws InterruptedException, SAXException, IOException, XpathException,TransformerConfigurationException, TransformerException  {
        XmlDocument request = new XmlDocument(TestPublisher.getPublisherInitRequest());
        Document response = driver.getPublicationShim().init(request).getDocument();
        ((PublisherShim)driver.getPublicationShim()).setHeartBeatInterval(1 * 1000);
        TestUtil.printDocumentToScreen("testHeartBeat", response);
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);
        
        publisher = new PublisherQueue(driver, 30 * 1000);
        Thread pubThread = new Thread(publisher);
        pubThread.start();

        XmlDocument event = publisher.getEventDocument();
		
        publisher.setResponseDocument(heartbeatResponse);
        TestUtil.printDocumentToScreen("testHeartBeat", event.getDocument());
        this.assertXMLEqual(XMLUnit.buildControlDocument(heartbeatResponse), event.getDocument());
	}
}
