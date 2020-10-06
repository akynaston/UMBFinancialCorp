package com.trivir.idm.driver.ace;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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

public class TestSubscriberAdd extends XMLTestCase {

	private AceDriverShim driver;
	private SubscriptionShim subscriber;
	private AceApi api;

	private static final String tokenSerialNum = TestUtil.tokenSerialNum;
	private static final String groupName = "TestGroup1"; // MemberOf - See user is member of group after assignment
	private static final String groupName1 = "TestGroup2";
	private static final String groupNameInSite = "TestGroup3@TestSecurityDomain"; // MemberOf - See user is member of group after assignment
	private static final String profileName = "TESTPROFILE1"; // ProfileName - test assignment - succeeds/fails




	public TestSubscriberAdd() throws Exception {
		Trace.registerImpl(CustomTraceInterface.class, 1);
	}

	protected void setUp() throws Exception {
		driver = new AceDriverShim();
		Document response = driver.init(new XmlDocument(TestDriverShim.getInitRequest())).getDocument();
		TestUtil.printDocumentToScreen("SetupDriver", response);
		assertXpathEvaluatesTo("success", "//nds/output/status/@level",
				response);
		subscriber = driver.getSubscriptionShim();

		XmlDocument request = new XmlDocument(TestChannelInit.subscriberInitRequest);
		response = subscriber.init(request).getDocument();
		assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);

		api = TestUtil.getApi();

		try {
			api.deleteUser("-TestUser2");
		} catch (Exception e) {
		}
	}

	protected void tearDown() throws Exception {
		driver.rsaData.close();
		try {
			api.listUserInfo("-TestUser2");
			api.deleteUser("-TestUser2");
		} catch (Exception e) {
		}
		api.destroy();
	}

	// ====================================================================================================================================

	private static final String addRequest = "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">"
			+ "<source>"
			+ "<product version=\"3.5.1.20070411 \">DirXML</product>"
			+ "<contact>TriVir</contact>"
			+ "</source>"
			+ "<input>"
			+ "<add class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" qualified-src-dn=\"O=VAULT\\CN=TestUser2\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\" src-entry-id=\"32902\">"
			+ "<add-attr attr-name=\"DefaultLogin\">"
			+ "<value timestamp=\"1181832286#1\" naming=\"true\" type=\"string\">TestUser2</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"LastName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">SurnameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"FirstName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">FirstNameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"EmailAddress\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">test@example.com</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"Password\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">" + TestUtil.DEFAULT_USER_PASSWORD + "</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"DefaultShell\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">DefaultShellValue</value>"
			+ "</add-attr>" + "</add>" + "</input>" + "</nds>";

	private static final String addResponse = "<nds dtdversion=\"2.0\">"
			+ "<source>"
			+ "<contact>TriVir</contact>"
			+ "<product instance=\"RSA Driver\">RSA IDM Driver</product>"
			+ "</source>"
			+ "<output>"
			+ "<add-association dest-dn=\"\\TRIVIR\\VAULT\\TestUser2\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\">TestUser2</add-association>"
			+ "<status event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" level=\"success\" type=\"driver-general\"/>"
			+ "</output>" + "</nds>";

	public void testAdd() throws SAXException, IOException, TransformerException, AceToolkitException {
		XmlDocument request = new XmlDocument(addRequest);
		Document response = this.subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testAdd", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(addResponse), response);

		// Ensure user exists in RSA:
		Map<String, Object> userInfo = Collections.emptyMap();
		try {
			userInfo = api.listUserInfo("-TestUser2");
		} catch (AceToolkitException e) {
			if (AceToolkitException.API_ERROR_INVUSR == e.getError()) {
				fail("User TestUser2 could not be found in ACE!");
			} else {
				throw e;
			}
		}

		assertEquals("SurnameValue", userInfo.get(AceApi.ATTR_LAST_NAME));
		assertEquals("FirstNameValue", userInfo.get(AceApi.ATTR_FIRST_NAME));
		assertEquals("TestUser2", userInfo.get(AceApi.ATTR_DEFAULT_LOGIN));
		// TODO: implement values in 7.1 api.
		// assertEquals("TRUE", userInfo.get(AceApi.ATTR_CREATE_PIN));
		// assertEquals("FALSE", userInfo.get(AceApi.ATTR_MUST_CREATE_PIN));
		assertEquals("DefaultShellValue", userInfo
				.get(AceApi.ATTR_DEFAULT_SHELL));
		assertEquals("FALSE", userInfo.get(AceApi.ATTR_TEMP_USER));
		assertEquals(null, userInfo.get(AceApi.ATTR_START));
		assertEquals(null, userInfo.get(AceApi.ATTR_END));

		// Confirm we have not yet been assigned a token
		List<String> serials = api.getSerialByLogin("TestUser2", "0");
		assertEquals(0, serials.size());
	}

	// ====================================================================================================================================

	private static final String addRequestAssignToken = "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">"
			+ "<source>"
			+ "<product version=\"3.5.1.20070411 \">DirXML</product>"
			+ "<contact>TriVir</contact>"
			+ "</source>"
			+ "<input>"
			+ "<add class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" qualified-src-dn=\"O=VAULT\\CN=TestUser2\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\" src-entry-id=\"32902\">"
			+ "<add-attr attr-name=\"DefaultLogin\">"
			+ "<value timestamp=\"1181832286#1\" naming=\"true\" type=\"string\">TestUser2</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"LastName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">SurnameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"FirstName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">FirstNameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"EmailAddress\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">test@example.com</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"DefaultShell\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">DefaultShellValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"TokenSerialNumber\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">"
			+ tokenSerialNum
			+ "</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"Password\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">" + TestUtil.DEFAULT_USER_PASSWORD + "</value>"
			+ "</add-attr>"
			+ "</add>"
			+ "</input>" + "</nds>";

	public void testAddAssignToken() throws SAXException, IOException,
			TransformerException, AceToolkitException {
		XmlDocument request = new XmlDocument(addRequestAssignToken);
		Document response = this.subscriber.execute(request, null)
				.getDocument();
		TestUtil.printDocumentToScreen("testAddAssignToken", response);
		this
				.assertXMLEqual(XMLUnit.buildControlDocument(addResponse),
						response);

		// Ensure user exists in RSA:
		Map<String, Object> userInfo = Collections.emptyMap();
		try {
			userInfo = api.listUserInfo("-TestUser2");
		} catch (AceToolkitException e) {
			if (AceToolkitException.API_ERROR_INVUSR == e.getError()) {
				fail("User TestUser2 could not be found in ACE!");
			} else {
				throw e;
			}
		}

		assertEquals("SurnameValue", userInfo.get(AceApi.ATTR_LAST_NAME));
		assertEquals("FirstNameValue", userInfo.get(AceApi.ATTR_FIRST_NAME));
		assertEquals("TestUser2", userInfo.get(AceApi.ATTR_DEFAULT_LOGIN));
		// TODO: implement values in 7.1 api.
		// assertTrue((Boolean)userInfo.get(AceApi.ATTR_CREATE_PIN));
		// assertFalse((Boolean)userInfo.get(AceApi.ATTR_MUST_CREATE_PIN));
		assertEquals("DefaultShellValue", userInfo
				.get(AceApi.ATTR_DEFAULT_SHELL));
		assertEquals("FALSE", userInfo.get(AceApi.ATTR_TEMP_USER).toString()
				.toUpperCase());
		assertEquals(null, userInfo.get(AceApi.ATTR_START));
		assertEquals(null, userInfo.get(AceApi.ATTR_END));

		// Confirm we've been assigned a token
		List<String> serials = api.getSerialByLogin("TestUser2", "0");
		assertEquals(1, serials.size());
		assertEquals(tokenSerialNum, serials.get(0));
	}

	// ====================================================================================================================================

	private static final String addRequestAssignTokenWithPIN = "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">"
			+ "<source>"
			+ "<product version=\"3.5.1.20070411 \">DirXML</product>"
			+ "<contact>TriVir</contact>"
			+ "</source>"
			+ "<input>"
			+ "<add class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" qualified-src-dn=\"O=VAULT\\CN=TestUser2\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\" src-entry-id=\"32902\">"
			+ "<add-attr attr-name=\"DefaultLogin\">"
			+ "<value timestamp=\"1181832286#1\" naming=\"true\" type=\"string\">TestUser2</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"LastName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">SurnameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"FirstName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">FirstNameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"DefaultShell\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">DefaultShellValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"TokenSerialNumber\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">"
			+ tokenSerialNum
			+ "</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"Password\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">" + TestUtil.DEFAULT_USER_PASSWORD + "</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"TokenPIN\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">234532</value>"
			+ "</add-attr>" + "</add>" + "</input>" + "</nds>";

	public void testAddAssignTokenWithPIN() throws SAXException, IOException,
			TransformerException, AceToolkitException {
		XmlDocument request = new XmlDocument(addRequestAssignTokenWithPIN);
		Document response = this.subscriber.execute(request, null)
				.getDocument();
		TestUtil.printDocumentToScreen("testAddAssignTokenWithPIN", response);
		this
				.assertXMLEqual(XMLUnit.buildControlDocument(addResponse),
						response);

		// Ensure user exists in RSA:
		Map<String, Object> userInfo = Collections.emptyMap();
		try {
			userInfo = api.listUserInfo("-TestUser2");
		} catch (AceToolkitException e) {
			if (AceToolkitException.API_ERROR_INVUSR == e.getError()) {
				fail("User TestUser2 could not be found in ACE!");
			} else {
				throw e;
			}
		}

		assertEquals("SurnameValue", userInfo.get(AceApi.ATTR_LAST_NAME));
		assertEquals("FirstNameValue", userInfo.get(AceApi.ATTR_FIRST_NAME));
		assertEquals("TestUser2", userInfo.get(AceApi.ATTR_DEFAULT_LOGIN));
		// TODO: implement values in 7.1 api.
		// assertTrue((Boolean)userInfo.get(AceApi.ATTR_CREATE_PIN));
		// assertFalse((Boolean)userInfo.get(AceApi.ATTR_MUST_CREATE_PIN));
		assertEquals("DefaultShellValue", userInfo
				.get(AceApi.ATTR_DEFAULT_SHELL));
		assertEquals("FALSE", userInfo.get(AceApi.ATTR_TEMP_USER).toString()
				.toUpperCase());
		assertEquals(null, userInfo.get(AceApi.ATTR_START));
		assertEquals(null, userInfo.get(AceApi.ATTR_END));

		// Confirm we've been assigned a token
		List<String> serials = api.getSerialByLogin("TestUser2", "0");
		assertEquals(1, serials.size());
		assertEquals(tokenSerialNum, serials.get(0));

		String pin = api.setPin("PIN", tokenSerialNum).toUpperCase();
		assertEquals("TRUE", pin);
	}

	// ====================================================================================================================================

	private static final String addRequestBadClassName = "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">"
			+ "<source>"
			+ "<product version=\"3.5.1.20070411 \">DirXML</product>"
			+ "<contact>TriVir</contact>"
			+ "</source>"
			+ "<input>"
			+ "<add class-name=\"SDUser\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" qualified-src-dn=\"O=VAULT\\CN=TestUser2\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\" src-entry-id=\"32902\">"
			+ "<add-attr attr-name=\"DefaultLogin\">"
			+ "<value timestamp=\"1181832286#1\" naming=\"true\" type=\"string\">TestUser2</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"LastName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">SurnameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"FirstName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">FirstNameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"Password\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">" + TestUtil.DEFAULT_USER_PASSWORD + "</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"DefaultShell\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">DefaultShellValue</value>"
			+ "</add-attr>" + "</add>" + "</input>" + "</nds>";

	private static final String addResponseBadClassName = "<nds dtdversion=\"2.0\">"
			+ "<source>"
			+ "<contact>TriVir</contact>"
			+ "<product instance=\"RSA Driver\">RSA IDM Driver</product>"
			+ "</source>"
			+ "<output>"
			+ "<status event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" level=\"error\" type=\"driver-general\">"
			+ "<description>class 'SDUser' not supported</description>"
			+ "</status>" + "</output>" + "</nds>";

	public void testAddUnsupportedClassName() throws SAXException, IOException,
			TransformerException {
		XmlDocument request = new XmlDocument(addRequestBadClassName);
		Document response = this.subscriber.execute(request, null)
				.getDocument();
		TestUtil.printDocumentToScreen("testAddUnsupportedClassName", response);
		this.assertXMLEqual(XMLUnit
				.buildControlDocument(addResponseBadClassName), response);
	}

	// ====================================================================================================================================

	private static final String addResponseUserAlreadyExists = "<nds dtdversion=\"2.0\">"
			+ "<source>"
			+ "<contact>TriVir</contact>"
			+ "<product instance=\"RSA Driver\">RSA IDM Driver</product>"
			+ "</source>"
			+ "<output>"
			+ "<status event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" level=\"error\" type=\"app-general\">"
			+ "<description>Unable to create account. User 'TestUser2' already exists (2276) - Principal with userid already exists in realm: TestUser2</description>"
			+ "<default-login>TestUser2</default-login>"
			+ "<error-code>2276</error-code>"
			+ "</status>"
			+ "</output>"
			+ "</nds>";

	public void testAddUserAlreadyExists() throws SAXException, IOException, TransformerException {
		XmlDocument request = new XmlDocument(addRequest);
		Document response = this.subscriber.execute(request, null).getDocument();
		// Execute add again to cause add failure:
		response = this.subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testAddUserAlreadyExists:", response);
		this.assertXMLEqual(XMLUnit
				.buildControlDocument(addResponseUserAlreadyExists), response);
	}

	// ====================================================================================================================================

	private static final String addRequestTempSynTime = "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">"
			+ "<source>"
			+ "<product version=\"3.5.1.20070411 \">DirXML</product>"
			+ "<contact>TriVir</contact>"
			+ "</source>"
			+ "<input>"
			+ "<add class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" qualified-src-dn=\"O=VAULT\\CN=TestUser2\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\" src-entry-id=\"32902\">"
			+ "<add-attr attr-name=\"DefaultLogin\">"
			+ "<value timestamp=\"1181832286#1\" naming=\"true\" type=\"string\">TestUser2</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"LastName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">SurnameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"FirstName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">FirstNameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"DefaultShell\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">DefaultShellValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"Start\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">1196308800</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"Password\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">" + TestUtil.DEFAULT_USER_PASSWORD + "</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"End\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">1355270400</value>"
			+ "</add-attr>" + "</add>" + "</input>" + "</nds>";

	public void testAddTempSynTime() throws SAXException, IOException,
			TransformerException, AceToolkitException {
		XmlDocument request = new XmlDocument(addRequestTempSynTime);
		Document response = this.subscriber.execute(request, null)
				.getDocument();
		TestUtil.printDocumentToScreen("testAddTempSynTime", response);
		this
				.assertXMLEqual(XMLUnit.buildControlDocument(addResponse),
						response);

		// Ensure user exists in RSA:
		Map<String, Object> userInfo = Collections.emptyMap();
		try {
			userInfo = api.listUserInfo("-TestUser2");
		} catch (AceToolkitException e) {
			if (AceToolkitException.API_ERROR_INVUSR == e.getError()) {
				fail("User TestUser2 could not be found in ACE!");
			} else {
				throw e;
			}
		}

		// new Date(1295518222000L) = Thu Jan 20 03:10:22 MST 2011, or Thu Jan
		// 20 10:10:22 MST 2011 UTC
		// or 01/20/11 and seconds from 12 am to 10:10 am = 10 hours + 10
		// minutes = 36600 seconds

		assertEquals("TRUE", userInfo.get(AceApi.ATTR_TEMP_USER).toString()
				.toUpperCase());
		assertEquals(1196308800L, userInfo.get(AceApi.ATTR_START));
		assertEquals(1355270400L, userInfo.get(AceApi.ATTR_END));

		// String bTempUser = fields[7];
		// String dateStart = fields[8];
		// String todStart = fields[9];
		// String dateEnd = fields[10];
		// String todEnd = fields[11];
		//
		// assertEquals("1", bTempUser); // bTempUser = TRUE
		// assertEquals("11/29/2007", dateStart); // Date start
		// assertEquals("14400", todStart); // Time of day start
		// assertEquals("12/12/2012", dateEnd); // Date end
		// assertEquals("0", todEnd); // Time of day end
	}

	// ====================================================================================================================================

	private static final String addRequestTempMissingStart = "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">"
			+ "<source>"
			+ "<product version=\"3.5.1.20070411 \">DirXML</product>"
			+ "<contact>TriVir</contact>"
			+ "</source>"
			+ "<input>"
			+ "<add class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" qualified-src-dn=\"O=VAULT\\CN=TestUser2\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\" src-entry-id=\"32902\">"
			+ "<add-attr attr-name=\"DefaultLogin\">"
			+ "<value timestamp=\"1181832286#1\" naming=\"true\" type=\"string\">TestUser2</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"LastName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">SurnameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"FirstName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">FirstNameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"DefaultShell\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">DefaultShellValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"Password\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">" + TestUtil.DEFAULT_USER_PASSWORD + "</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"End\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">1355270400</value>"
			+ "</add-attr>" + "</add>" + "</input>" + "</nds>";

	public void testAddTempMissingStart() throws SAXException, IOException,
			TransformerException, AceToolkitException {

		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		// ensure our formatter is in UTC.
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		XmlDocument request = new XmlDocument(addRequestTempMissingStart);
		Document response = this.subscriber.execute(request, null)
				.getDocument();
		TestUtil.printDocumentToScreen("testAddTempMissingStart", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(addResponse),response);

		// Ensure user exists in RSA:
		Map<String, Object> userInfo = Collections.emptyMap();
		userInfo = api.listUserInfo("-TestUser2");

		assertEquals("SurnameValue", userInfo.get(AceApi.ATTR_LAST_NAME));
		assertEquals("FirstNameValue", userInfo.get(AceApi.ATTR_FIRST_NAME));
		assertEquals("TestUser2", userInfo.get(AceApi.ATTR_DEFAULT_LOGIN));
		assertEquals("DefaultShellValue", userInfo.get(AceApi.ATTR_DEFAULT_SHELL));
		assertEquals("TRUE", userInfo.get(AceApi.ATTR_TEMP_USER).toString().toUpperCase());
		assertEquals(504921600L, userInfo.get(AceApi.ATTR_START));
		assertEquals(1355270400L, userInfo.get(AceApi.ATTR_END));

		// Calendar todaysTwelveAM = (Calendar)today.clone();
		// todaysTwelveAM.set(Calendar.HOUR_OF_DAY, 0);
		// todaysTwelveAM.set(Calendar.MINUTE, 0);
		// todaysTwelveAM.set(Calendar.SECOND, 0);
		// todaysTwelveAM.set(Calendar.MILLISECOND, 0);
		//           
		// int secondsSinceTwelveAM = (int)((today.getTimeInMillis() -
		// todaysTwelveAM.getTimeInMillis()) / 1000);
		//           
		// String TempUser = fields[7];
		// String dateStart = fields[8];
		// String todStart = fields[9];
		// String dateEnd = fields[10];
		// String todEnd = fields[11];
		//         
		// assertEquals("1", TempUser);
		// assertEquals(formatter.format(today.getTime()), dateStart); // Date
		// start
		// assertEquals("" + secondsSinceTwelveAM, todStart); // Time of day
		// start
		// assertEquals("12/12/2012", dateEnd); // Date end
		// assertEquals("0", todEnd); // Time of day end
	}

	// ====================================================================================================================================

	private static final String addGroupsRequest = "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">"
			+ "<source>"
			+ "<product version=\"3.5.1.20070411 \">DirXML</product>"
			+ "<contact>TriVir</contact>"
			+ "</source>"
			+ "<input>"
			+ "<add class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" qualified-src-dn=\"O=VAULT\\CN=TestUser2\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\" src-entry-id=\"32902\">"
			+ "<add-attr attr-name=\"DefaultLogin\">"
			+ "<value timestamp=\"1181832286#1\" naming=\"true\" type=\"string\">TestUser2</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"LastName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">SurnameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"FirstName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">FirstNameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"DefaultShell\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">DefaultShellValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"Password\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">" + TestUtil.DEFAULT_USER_PASSWORD + "</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"MemberOf\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">"
			+ groupName
			+ "</value>" + "</add-attr>" + "</add>" + "</input>" + "</nds>";

	public void testAddGroup() throws SAXException, IOException, TransformerException, AceToolkitException {
		XmlDocument request = new XmlDocument(addGroupsRequest);
		Document response = this.subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testAddGroups: add response", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(addResponse), response);

		// Confirm user is in ACE, and memberships have been assigned.
		try {
			// simply attempt a list - if it fails, then we'll exit . .
			api.listUserInfo("-TestUser2");
		} catch (AceToolkitException e) {
			fail("Add failed for addGroups test: [" + e + "]");
		}

		// Confirm we have the proper group assigned:
		List<String> groupMembership = api.listGroupMembership("-TestUser2");
		assertEquals(1, groupMembership.size());
		assertEquals(groupName, groupMembership.get(0));
	}

	// ====================================================================================================================================

	private static final String addWithMultipleGroupsRequest = "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">"
			+ "<source>"
			+ "<product version=\"3.5.1.20070411 \">DirXML</product>"
			+ "<contact>TriVir</contact>"
			+ "</source>"
			+ "<input>"
			+ "<add class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" qualified-src-dn=\"O=VAULT\\CN=TestUser2\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\" src-entry-id=\"32902\">"
			+ "<add-attr attr-name=\"DefaultLogin\">"
			+ "<value timestamp=\"1181832286#1\" naming=\"true\" type=\"string\">TestUser2</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"LastName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">SurnameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"FirstName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">FirstNameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"DefaultShell\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">DefaultShellValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"MemberOf\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">"
			+ groupName
			+ "</value>"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">"
			+ groupName1
			+ "</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"Password\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">" + TestUtil.DEFAULT_USER_PASSWORD + "</value>"
			+ "</add-attr>"
			+ "</add>"
			+ "</input>"
			+ "</nds>";

	public void testAddWithMultipleGroups() throws SAXException, IOException, TransformerException, AceToolkitException {
		XmlDocument request = new XmlDocument(addWithMultipleGroupsRequest);
		Document response = this.subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testAddWithMultipleGroups: add response", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(addResponse), response);

		// Confirm user is in ACE, and memberships have been assigned.
		try {
			// simply attempt a list - if it fails, then we'll exit . .
			api.listUserInfo("-TestUser2");
		} catch (AceToolkitException e) {
			fail("Add failed for addGroups test: [" + e + "]");
		}

		// Confirm we have the proper group assigned:
		List<String> groupMembership = api.listGroupMembership("-TestUser2");
		assertEquals(2, groupMembership.size());
		assertEquals(groupName, groupMembership.get(0));
		assertEquals(groupName1, groupMembership.get(1));
	}

	// ====================================================================================================================================

	private static final String addGroupInSiteRequest = "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">"
			+ "<source>"
			+ "<product version=\"3.5.1.20070411 \">DirXML</product>"
			+ "<contact>TriVir</contact>"
			+ "</source>"
			+ "<input>"
			+ "<add class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" qualified-src-dn=\"O=VAULT\\CN=TestUser2\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\" src-entry-id=\"32902\">"
			+ "<add-attr attr-name=\"DefaultLogin\">"
			+ "<value timestamp=\"1181832286#1\" naming=\"true\" type=\"string\">TestUser2</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"LastName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">SurnameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"FirstName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">FirstNameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"DefaultShell\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">DefaultShellValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"MemberOf\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">"
			+ groupNameInSite
			+ "</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"Password\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">" + TestUtil.DEFAULT_USER_PASSWORD + "</value>"
			+ "</add-attr>"
			+ "</add>"
			+ "</input>" + "</nds>";

	/**
	 * This test assumes that you have a Remote Access site and Hewit Associates
	 * is a member of the site.
	 * 
	 */
	public void testAddGroupInSite() throws SAXException, IOException, TransformerException, AceToolkitException {
		XmlDocument request = new XmlDocument(addGroupInSiteRequest);
		Document response = this.subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testAddGroupInSite: add response", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(addResponse), response);

		// Confirm user is in ACE, and memberships have been assigned.
		try {
			// simply attempt a list - if it fails, then we'll exit . .
			api.listUserInfo("-TestUser2");
		} catch (AceToolkitException e) {
			fail("Add failed for addGroups test: [" + e + "]");
		}

		// Confirm we have the proper group assigned:
		List<String> groupMembership = api.listGroupMembership("-TestUser2");
		assertEquals(1, groupMembership.size());
		assertEquals(groupNameInSite, groupMembership.get(0));
	}

	// ====================================================================================================================================

	private static final String addProfilesRequest = "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">"
			+ "<source>"
			+ "<product version=\"3.5.1.20070411 \">DirXML</product>"
			+ "<contact>TriVir</contact>"
			+ "</source>"
			+ "<input>"
			+ "<add class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" qualified-src-dn=\"O=VAULT\\CN=TestUser2\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\" src-entry-id=\"32902\">"
			+ "<add-attr attr-name=\"DefaultLogin\">"
			+ "<value timestamp=\"1181832286#1\" naming=\"true\" type=\"string\">TestUser2</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"LastName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">SurnameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"FirstName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">FirstNameValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"DefaultShell\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">DefaultShellValue</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"ProfileName\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">"
			+ profileName
			+ "</value>"
			+ "</add-attr>"
			+ "<add-attr attr-name=\"Password\">"
			+ "<value timestamp=\"1181832286#1\" type=\"string\">" + TestUtil.DEFAULT_USER_PASSWORD + "</value>"
			+ "</add-attr>"
			+ "</add>"
			+ "</input>"
			+ "</nds>";

	public void testAddProfiles() throws SAXException, IOException, TransformerException, AceToolkitException {
		XmlDocument request = new XmlDocument(addProfilesRequest);
		Document response = this.subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testAddProfiles: add response", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(addResponse), response);

		Map<String, Object> userInfo = Collections.emptyMap();
		// Confirm user is in ACE, and memberships have been assigned.
		try {
			// simply attempt a list - if it fails, then we'll exit . .
			userInfo = api.listUserInfo("-TestUser2");
		} catch (AceToolkitException e) {
			fail("Add failed for addProfiles test: [" + e + "]");
		}

		// Confirm we have the proper profile assigned:
		assertEquals(profileName, userInfo.get(AceApi.ATTR_PROFILE_NAME));
	}
}
