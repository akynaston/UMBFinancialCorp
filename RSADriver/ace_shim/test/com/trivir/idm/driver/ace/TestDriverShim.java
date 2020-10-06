package com.trivir.idm.driver.ace;

import java.io.IOException;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringEscapeUtils;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.novell.nds.dirxml.driver.Trace;
import com.novell.nds.dirxml.driver.XmlDocument;

public class TestDriverShim extends XMLTestCase {
	public static final String revision = "1.0";

	public TestDriverShim() {
		Trace.registerImpl(CustomTraceInterface.class, 1);
	}

	private static final String getSchemaRequest() {
		String schemaRequest = "<top>"
				+ "<driver-init>"
				+ "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">"
				+ "<source>"
				+ "<product version=\"3.5.1.20070411 \">DirXML</product>"
				+ "<contact>Novell, Inc.</contact>"
				+ "</source>"
				+ "<input>"
				+ "<init-params src-dn=\"\\TESTTREE\\resources\\EAPDrivers\\RSA Driver\">"
				+ "<authentication-info>";
		for (Map.Entry<String, String> authParam : TestUtil.getAuthParams()
				.entrySet()) {
			schemaRequest += String.format("<%s>%s</%s>", authParam.getKey(),
					StringEscapeUtils.escapeXml(authParam.getValue()),
					authParam.getKey());
		}
		schemaRequest += "</authentication-info>" + "<driver-options>";
		for (Map.Entry<String, String> driverParam : TestUtil.getDriverParams()
				.entrySet()) {
			schemaRequest += String.format("<%s display-name=\"%s\">%s</%s>",
					driverParam.getKey(), driverParam.getKey(),
					StringEscapeUtils.escapeXml(driverParam.getValue()),
					driverParam.getKey());
		}
		schemaRequest += "<userExtensions display-name=\"User Extensions\">extension1</userExtensions>"
				+ "<userExtensions display-name=\"User Extensions\">extension2</userExtensions>"
				+ "</driver-options>"
				+ "<subscriber-options>"
				+ "<disable display-name=\"Disable subscriber?\">_</disable>"
				+ "</subscriber-options>"
				+ "<publisher-options>"
				+ "<disable display-name=\"Disable publisher?\">_</disable>"
				+ "<pollRate display-name=\"Polling Interval in Seconds\">3</pollRate>"
				+ "<pub-heartbeat-interval display-name=\"Publisher heartbeat interval in Seconds\">3</pub-heartbeat-interval>"
				+ "</publisher-options>"
				+ "</init-params>"
				+ "</input>"
				+ "</nds>" + "</driver-init>" + "</top>";
		return schemaRequest;
	}

	private static final String getSchemaResponse = "<nds dtdversion=\"2.0\">"
			+ "<source>"
			+ "<contact>TriVir</contact>"
			+ "<product instance=\"RSA Driver\">RSA IDM Driver</product>"
			+ "</source>"
			+ "<output>"
			+ "<schema-def application-name=\"RSA\" hierarchical=\"false\">"
			+ "<class-def class-name=\"User\" container=\"false\">"
			+ "<attr-def attr-name=\"DefaultLogin\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"true\" read-only=\"false\" required=\"true\" type=\"string\"/>"
			+ "<attr-def attr-name=\"DefaultShell\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"false\" type=\"string\"/>"
			+ "<attr-def attr-name=\"FirstName\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"false\" type=\"string\"/>"
			+ "<attr-def attr-name=\"LastName\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"true\" type=\"string\"/>"
			+ "<attr-def attr-name=\"EmailAddress\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"false\" type=\"string\"/>"
			+ "<attr-def attr-name=\"TokenSerialNumber\" case-sensitive=\"false\" multi-valued=\"true\" naming=\"false\" read-only=\"false\" required=\"false\" type=\"string\"/>"
			+ "<attr-def attr-name=\"ProfileName\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"false\" type=\"string\"/>"
			+ "<attr-def attr-name=\"MemberOf\" case-sensitive=\"false\" multi-valued=\"true\" naming=\"false\" read-only=\"false\" required=\"false\" type=\"string\"/>"
			+ "<attr-def attr-name=\"TempUser\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"false\" type=\"state\"/>"
			+ "<attr-def attr-name=\"Start\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"false\" type=\"time\"/>"
			+ "<attr-def attr-name=\"End\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"false\" type=\"time\"/>"
			+ "<attr-def attr-name=\"Password\" case-sensitive=\"true\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"false\" type=\"string\"/>"
			+ "</class-def>"
			+ "<class-def class-name=\"Token\" container=\"false\">"
			+ "<attr-def attr-name=\"SerialNum\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"true\" read-only=\"true\" required=\"true\" type=\"string\"/>"
			+ "<attr-def attr-name=\"PINClear\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"string\"/>"
			+ "<attr-def attr-name=\"NumDigits\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"string\"/>"
			+ "<attr-def attr-name=\"Interval\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"string\"/>"
			+ "<attr-def attr-name=\"Birth\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"time\"/>"
			+ "<attr-def attr-name=\"Death\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"time\"/>"
			+ "<attr-def attr-name=\"LastLogin\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"false\" type=\"time\"/>"
			+ "<attr-def attr-name=\"Type\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"string\"/>"
			+ "<attr-def attr-name=\"Hex\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"string\"/>"
			+ "<attr-def attr-name=\"NewPINMode\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"true\" type=\"state\"/>"
			+ "<attr-def attr-name=\"UserNum\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"false\" type=\"string\"/>"
			+ "<attr-def attr-name=\"DefaultLogin\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"false\" type=\"string\"/>"
			+ "<attr-def attr-name=\"NextCodeStatus\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"true\" type=\"state\"/>"
			+ "<attr-def attr-name=\"BadTokenCodes\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"string\"/>"
			+ "<attr-def attr-name=\"BadPINs\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"string\"/>"
			+ "<attr-def attr-name=\"PINChangedDate\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"time\"/>"
			+ "<attr-def attr-name=\"DisabledDate\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"time\"/>"
			+ "<attr-def attr-name=\"CountsLastModified\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"time\"/>"
			+ "<attr-def attr-name=\"PINChangedDate\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"string\"/>"
			+ "<attr-def attr-name=\"Protected\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"true\" type=\"state\"/>"
			+ "<attr-def attr-name=\"Deployment\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"time\"/>"
			+ "<attr-def attr-name=\"Deployed\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"true\" type=\"state\"/>"
			+ "<attr-def attr-name=\"Count\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"false\" type=\"string\"/>"
			+ "<attr-def attr-name=\"SoftPassword\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"false\" type=\"string\"/>"
			+ "<attr-def attr-name=\"PIN\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"false\" type=\"string\"/>"
			+ "<attr-def attr-name=\"Disabled\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"true\" type=\"state\"/>"
			+ "<attr-def attr-name=\"Assigned\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"true\" type=\"state\"/>"
			+ "<attr-def attr-name=\"SeedSize\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"string\"/>"
			+ "<attr-def attr-name=\"Keypad\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"state\"/>"
			+ "<attr-def attr-name=\"LocalPIN\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"state\"/>"
			+ "<attr-def attr-name=\"Version\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"string\"/>"
			+ "<attr-def attr-name=\"FormFactor\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"string\"/>"
			+ "<attr-def attr-name=\"PINType\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"true\" type=\"string\"/>"
			+ "<attr-def attr-name=\"Assignment\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"false\" type=\"time\"/>"
			+ "<attr-def attr-name=\"FirstLogin\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"false\" type=\"state\"/>"
			+ "<attr-def attr-name=\"LastDACode\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"false\" type=\"time\"/>"
			+ "<attr-def attr-name=\"EACExpires\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"false\" type=\"time\"/>"
			+ "<attr-def attr-name=\"EACPasscode\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"true\" required=\"false\" type=\"string\"/>"
			+ "<attr-def attr-name=\"EmergencyAccess\" case-sensitive=\"false\" multi-valued=\"false\" naming=\"false\" read-only=\"false\" required=\"false\" type=\"state\"/>"
			+ "</class-def>" + "</schema-def>"
			+ "<status level=\"success\" type=\"driver-status\"/>"
			+ "</output>" + "</nds>";

	static String getInitRequest() {
		String initRequest = "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">"
				+ "<source>"
				+ "<product version=\"3.5.1.20070411 \">DirXML</product>"
				+ "<contact>Novell, Inc.</contact>"
				+ "</source>"
				+ "<input>"
				+ "<init-params src-dn=\"\\TRIVIR\\services\\DriverSet\\RSA Driver\">"
				+ "<authentication-info>";
		for (Map.Entry<String, String> authParam : TestUtil.getAuthParams()
				.entrySet()) {
			initRequest += String.format("<%s>%s</%s>", authParam.getKey(),
					StringEscapeUtils.escapeXml(authParam.getValue()),
					authParam.getKey());
		}
		initRequest += "</authentication-info>" + "<driver-options>";
		for (Map.Entry<String, String> driverParam : TestUtil.getDriverParams()
				.entrySet()) {
			initRequest += String.format("<%s display-name=\"%s\">%s</%s>",
					driverParam.getKey(), driverParam.getKey(),
					StringEscapeUtils.escapeXml(driverParam.getValue()),
					driverParam.getKey());
		}
		initRequest += "<userExtensions display-name=\"User Extensions\">extension1</userExtensions>"
				+ "<userExtensions display-name=\"User Extensions\">extension2</userExtensions>"
				+ "</driver-options>"
				+ "</init-params>"
				+ "</input>"
				+ "</nds>";

		return initRequest;
	}

	public void testGetSchema() throws SAXException, IOException, TransformerException {
		AceDriverShim driver = new AceDriverShim();
		XmlDocument request = new XmlDocument(getSchemaRequest());
		Document response = driver.getSchema(request).getDocument();
		TestUtil.printDocumentToScreen("testGetSchema", response);
		assertXMLEqual(XMLUnit.buildControlDocument(getSchemaResponse), response);
	}

	private static final String initResponse = "<nds dtdversion=\"2.0\">"
			+ "<source>"
			+ "<contact>TriVir</contact>"
			+ "<product instance=\"RSA Driver\">RSA IDM Driver</product>"
			+ "</source>" + "<output>"
			+ "<status level=\"success\" type=\"driver-status\"/>" +
			"</output>" + "</nds>";

	public void testInit() throws SAXException, IOException, TransformerConfigurationException, TransformerException {
		AceDriverShim driver = new AceDriverShim();

		XmlDocument request = new XmlDocument(getInitRequest());
		Document response = driver.init(request).getDocument();
		TestUtil.printDocumentToScreen("testInit", response);
		assertXMLEqual(XMLUnit.buildControlDocument(initResponse), response);
		driver.shutdown(null);
	}
}
