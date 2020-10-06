package com.trivir.idm.driver.ace;

import java.io.IOException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.novell.nds.dirxml.driver.PublicationShim;
import com.novell.nds.dirxml.driver.SubscriptionShim;
import com.novell.nds.dirxml.driver.Trace;
import com.novell.nds.dirxml.driver.XmlDocument;

public class TestChannelInit extends XMLTestCase {
	private AceDriverShim driver;

	public TestChannelInit() {
		Trace.registerImpl(CustomTraceInterface.class, 1);
	}

	protected void setUp() throws Exception {
		driver = new AceDriverShim();
		Document response = driver.init(new XmlDocument(TestDriverShim.getInitRequest())).getDocument();
        TestUtil.printDocumentToScreen("setUp", response);
		assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);
	}

	static final String subscriberInitRequest =
		"<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
			"<source>" + 
				"<product version=\"3.5.1.20070411 \">DirXML</product>" +
				"<contact>Novell, Inc.</contact>" +
			"</source>" +
			"<input>" + 
				"<init-params src-dn=\"\\TRIVIR\\services\\DriverSet\\RSA Driver\\Subscriber\">" +
					"<authentication-info>" +
						"<server>server.app:400</server>" +
						"<user>User1</user>" +
				        "<password><!-- content suppressed --></password>" +
					"</authentication-info>" +
					"<driver-filter>" +
						"<allow-class class-name=\"User\">" +
                            "<allow-attr attr-name=\"DefaultLogin\"/>" +
                            "<allow-attr attr-name=\"DefaultShell\"/>" +
                            "<allow-attr attr-name=\"FirstName\"/>" +
                            "<allow-attr attr-name=\"LastName\"/>" +
                        "</allow-class>" +
					"</driver-filter>" +
					"<subscriber-options>" +
						"<disable display-name=\"Disable subscriber?\">_</disable>" +
					"</subscriber-options>" +
				"</init-params>" +
			"</input>" +
		"</nds>";

	private static final String subscriberInitResponse =
		"<nds dtdversion=\"2.0\">" +
			"<source>" +
				"<contact>TriVir</contact>" +
				"<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
			"</source>" +
			"<output>" +
				"<status level=\"success\" type=\"driver-status\"/>" +
			"</output>" +
		"</nds>";
	public void testSubscriberInit() throws SAXException, IOException, TransformerConfigurationException, TransformerException  {
		SubscriptionShim subscriber = driver.getSubscriptionShim();

        XmlDocument request = new XmlDocument(subscriberInitRequest);
		Document response = subscriber.init(request).getDocument();
        TestUtil.printDocumentToScreen("testSubscriberInit", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(subscriberInitResponse), response);
	}

	static final String subscriberInitWithCredentialsRequest =
		"<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
			"<source>" + 
				"<product version=\"3.5.1.20070411 \">DirXML</product>" +
				"<contact>Novell, Inc.</contact>" +
			"</source>" +
			"<input>" + 
				"<init-params src-dn=\"\\TRIVIR\\services\\DriverSet\\RSA Driver\\Subscriber\">" +
					"<authentication-info>" +
						"<server>server.app:400</server>" +
						"<user>SYSTEM</user>" +
				        "<password>1234</password>" +
					"</authentication-info>" +
					"<driver-filter>" +
						"<allow-class class-name=\"User\">" +
                            "<allow-attr attr-name=\"DefaultLogin\"/>" +
                            "<allow-attr attr-name=\"DefaultShell\"/>" +
                            "<allow-attr attr-name=\"FirstName\"/>" +
                            "<allow-attr attr-name=\"LastName\"/>" +
                        "</allow-class>" +
					"</driver-filter>" +
					"<subscriber-options>" +
						"<disable display-name=\"Disable subscriber?\">_</disable>" +
					"</subscriber-options>" +
				"</init-params>" +
			"</input>" +
		"</nds>";

	public void testSubscriberInitWithCredentials() throws SAXException, IOException, TransformerConfigurationException, TransformerException  {
		SubscriptionShim subscriber = driver.getSubscriptionShim();

        XmlDocument request = new XmlDocument(subscriberInitWithCredentialsRequest);
		Document response = subscriber.init(request).getDocument();
        TestUtil.printDocumentToScreen("testSubscriberInit", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(subscriberInitResponse), response);
	}

	static final String publisherInitRequest =
		"<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" + 
			"<source>" + 
				"<product version=\"3.5.1.20070411 \">DirXML</product>" + 
				"<contact>Novell, Inc.</contact>" + 
			"</source>" + 
			"<input>" + 
				"<init-params src-dn=\"\\TESTTREE\\resources\\EAPDrivers\\RSADriver\\Publisher\">" + 
					"<authentication-info>" + 
						"<server>REMOTE(hostname=172.17.2.38 port=8090 )</server>" + 
				        "<user>aceauthid</user>" +
						"<password><!-- content suppressed --></password>" + 
					"</authentication-info>" + 
					"<driver-filter>" + 
						"<allow-class class-name=\"User\">" + 
							"<allow-attr attr-name=\"DefaultLogin\"/>" + 
							"<allow-attr attr-name=\"DefaultShell\"/>" + 
							"<allow-attr attr-name=\"FirstName\"/>" +
							"<allow-attr attr-name=\"LastName\"/>" +
							"<allow-attr attr-name=\"TokenSerialNumber\"/>" +
							"<allow-attr attr-name=\"TokenPIN\"/>" +
							"<allow-attr attr-name=\"ProfileName\"/>" +
							"<allow-attr attr-name=\"MemberOf\"/>" +
							"<allow-attr attr-name=\"TempUser\"/>" +
							"<allow-attr attr-name=\"Start\"/>" +
							"<allow-attr attr-name=\"End\"/>" +
							"<allow-attr attr-name=\"CreatePIN\"/>" +
							"<allow-attr attr-name=\"MustCreatePIN\"/>" +
						"</allow-class>" + 
					"</driver-filter>" + 
					"<publisher-options>" + 
						"<disable display-name=\"Disable publisher?\">_</disable>" +
						"<pollRate display-name=\"Polling Interval in Seconds\">60</pollRate>" +
						"<pub-heartbeat-interval display-name=\"Heartbeat interval in minutes\">1</pub-heartbeat-interval>" +								
					"</publisher-options>" + 
				"</init-params>" + 
			"</input>" + 
		"</nds>";
	
	private static final String publisherInitResponse =
		"<nds dtdversion=\"2.0\">" +
			"<source>" +
				"<contact>TriVir</contact>" +
				"<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
			"</source>" +
			"<output>" +
			"<status level=\"success\" type=\"driver-status\">" +
				"<options>" +
		        	"<disable display-name=\"Disable publisher?\">_</disable>" +
					"<pollRate display-name=\"Polling Interval in Seconds\">60</pollRate>" +
					"<pub-heartbeat-interval display-name=\"Heartbeat interval in minutes\">1</pub-heartbeat-interval>" +
				"</options>" +
			"</status>" +
			"</output>" +
		"</nds>";

	public void testPublisherInit() throws SAXException, IOException, TransformerException, TransformerConfigurationException {
		PublicationShim publisher = driver.getPublicationShim();
        XmlDocument request = new XmlDocument(publisherInitRequest);
		Document response = publisher.init(request).getDocument();
        TestUtil.printDocumentToScreen("testPublisherInit", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(publisherInitResponse), response);
	}
	
	public void testInitBothChannelsSameTime() throws SAXException, IOException, TransformerException, TransformerConfigurationException {
		SubscriptionShim subscriber = driver.getSubscriptionShim();
		PublicationShim publisher = driver.getPublicationShim();

		Document response = subscriber.init(new XmlDocument(subscriberInitRequest)).getDocument();
        TestUtil.printDocumentToScreen("testSubscriberInit", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(subscriberInitResponse), response);

        response = publisher.init(new XmlDocument(publisherInitRequest)).getDocument();
        TestUtil.printDocumentToScreen("testPublisherInit", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(publisherInitResponse), response);
	}
	
}
