package com.trivir.idm.driver.ace;

import java.io.IOException;
import java.util.Map;

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

/* Test time values
 * 01/02/2008 @ 08:00 MST = 1199286000
 * 11/12/2013 @ 19:00 MST = 1384308000
 * 11/11/2011 @ 11:00 MST = 1321034400
 * 12/12/2012 @ 12:00 MST = 1358017200
 * 
 * 01/01/1986 @ 0:00 UTC = 504921600
 */
public class TestSubscriberModifyUserTemp extends XMLTestCase {
	private AceDriverShim driver;
	private SubscriptionShim subscriber;
    private AceApi api;
    
    private static final String modifyResponseSuccess =
        "<nds dtdversion=\"2.0\">" +
            "<source>" +
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<output>" +
                "<status event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" level=\"success\" type=\"driver-general\"/>" +
            "</output>" +
        "</nds>";

    public TestSubscriberModifyUserTemp() throws Exception {
        Trace.registerImpl(CustomTraceInterface.class, 1);
    }

    protected void setUp() throws Exception {
        driver = new AceDriverShim();
        Document response = driver.init(new XmlDocument(TestDriverShim.getInitRequest())).getDocument();
        TestUtil.printDocumentToScreen("setUp", response);
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);

        subscriber = driver.getSubscriptionShim();
        XmlDocument request = new XmlDocument(TestChannelInit.subscriberInitRequest);
		response = subscriber.init(request).getDocument();
        //AllTests.printDocumentToScreen("Setup", response);
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);
        
		api = TestUtil.getApi();
		
        try {
            api.deleteUser("-TestUser2");
        } catch (Exception e) {}        
		
        api.addUser("User1", "Test", "TestUser2",null, "", TestUtil.DEFAULT_USER_PASSWORD);
    }
    
    private void setTempUser(String defaultLogin) throws AceToolkitException {
        final String startDate = "11/11/2011";
        final int startHour = 11;
        final String endDate = "12/12/2012";
        final int endHour = 12;
        api.setTempUser(startDate, startHour, endDate, endHour, defaultLogin);
    }
    
    protected void tearDown() throws Exception {
        try {
            api.deleteUser("-TestUser2");
        } catch (Exception e) {
           return; // swallow the exception - we'll only try to delete if it exists. 
        }        
        api.destroy();
    }

//  ====================================================================================================================================

    private static final String modifyRequestAddStart =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
        "<source>" + 
            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
            "<contact>TriVir</contact>" +
        "</source>" +
        "<input>" + 
            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
                "<association>TestUser2</association>" +
                "<modify-attr attr-name=\"Start\">" +
                    "<add-value>" +
                        "<value timestamp=\"1181832286#1\" type=\"string\">1226502000</value>" + // 11/12/2008 @ 08:00 MST
                    "</add-value>" +
                "</modify-attr>" +
//                "<modify-attr attr-name=\"End\">" +
//	                "<add-value>" +
//	                    "<value timestamp=\"1181832286#1\" type=\"string\">1236502000</value>" + // ?
//	                "</add-value>" +
//	            "</modify-attr>" +
            "</modify>" +           
        "</input>" +
    "</nds>";

    private static final String modifyResponseTempAddStart = modifyResponseSuccess;

    public void testTempAddStart() throws SAXException, IOException, TransformerException, AceToolkitException {
        setTempUser("-TestUser2");
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestAddStart);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testTempAddStart: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseTempAddStart), response);
        
		Map<String, Object> userInfo = api.listUserInfo("-TestUser2");        
		assertEquals(1226502000L, userInfo.get(AceApi.ATTR_START));
    }

    private static final String modifyResponseNonTempAddStart =
        "<nds dtdversion=\"2.0\">" +
            "<source>" +
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<output>" +
                "<status event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" level=\"error\" type=\"driver-general\">" +
                    "<description>Cannot set Start date without End date.</description>" +
                "</status>" +
            "</output>" +
        "</nds>";
        
    public void testNonTempAddStart() throws SAXException, IOException, TransformerException, AceToolkitException {        
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestAddStart);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testNonTempAddStart: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseNonTempAddStart), response);
  
        Map<String, Object> userInfo = api.listUserInfo("-TestUser2");
		assertEquals(null, userInfo.get(AceApi.ATTR_START));
    }

//  ====================================================================================================================================

    private static final String modifyRequestAddStartAndEnd =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
        "<source>" + 
            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
            "<contact>TriVir</contact>" +
        "</source>" +
        "<input>" + 
            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
                "<association>TestUser2</association>" +
                "<modify-attr attr-name=\"Start\">" +
                    "<add-value>" +
                        "<value timestamp=\"1181832286#1\" type=\"string\">1199286000</value>" + // 1/2/2008 @ 08:00 MST
                    "</add-value>" +
                "</modify-attr>" +
                "<modify-attr attr-name=\"End\">" +
                    "<add-value>" +
                        "<value timestamp=\"1181832286#1\" type=\"string\">1384308000</value>" + // 11/12/2013 @ 19:00 MST
                    "</add-value>" +
                "</modify-attr>" +
            "</modify>" +           
        "</input>" +
    "</nds>";

    private static final String modifyResponseAddStartAndEnd = modifyResponseSuccess;

    public void testNonTempAddStartAndEnd() throws SAXException, IOException, TransformerException, AceToolkitException {
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestAddStartAndEnd);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testNonTempAddStartAndEnd: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseAddStartAndEnd), response);
        
        Map<String,Object> userInfo = api.listUserInfo("-TestUser2");        
        assertEquals(1199286000L, userInfo.get(AceApi.ATTR_START));
        assertEquals(1384308000L, userInfo.get(AceApi.ATTR_END));
    }

    public void testTempAddStartAndEnd() throws SAXException, IOException, TransformerException, AceToolkitException {
        setTempUser("-TestUser2");
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestAddStartAndEnd);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testTempAddStartAndEnd: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseAddStartAndEnd), response);
        
        Map<String,Object> userInfo = api.listUserInfo("-TestUser2");        
        assertEquals(1199286000L, userInfo.get(AceApi.ATTR_START));
        assertEquals(1384308000L, userInfo.get(AceApi.ATTR_END));
    }

//  ====================================================================================================================================
/*
    private static final String modifyRequestAddEnd =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
        "<source>" + 
            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
            "<contact>TriVir</contact>" +
        "</source>" +
        "<input>" + 
            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
                "<association>TestUser2</association>" +
                "<modify-attr attr-name=\"End\">" +
                    "<add-value>" +
                        "<value timestamp=\"1181832286#1\" type=\"string\">1384308000</value>" + // 11/12/2013 @ 19:00 MST
                    "</add-value>" +
                "</modify-attr>" +
            "</modify>" +           
        "</input>" +
    "</nds>";

    private static final String modifyResponseAddEnd = modifyResponseSuccess;

    public void testNonTempAddEnd() throws SAXException, IOException, TransformerException, AceToolkitException {
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestAddEnd);
        Document response = this.subscriber.execute(request, null).getDocument();
        long currentCtime = new Date().getTime() / 1000;
        String currentDate = TimeUtil.utcDateFromCtime(currentCtime);
        int currentSeconds = TimeUtil.utcSecondsFromCtime(currentCtime);
        AllTests.printDocumentToScreen("testNonTempAddEnd: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseAddEnd), response);
        
        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 1 | " + currentDate + " | " + currentSeconds + " | 11/13/2013 | 7200", tempUserInfo.substring(i));
    }

    public void testTempAddEnd() throws SAXException, IOException, TransformerException, AceToolkitException {
        setTempUser(defaultLogin);
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestAddEnd);
        Document response = this.subscriber.execute(request, null).getDocument();
        AllTests.printDocumentToScreen("testTempAddEnd: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseAddEnd), response);
        
        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 1 | 11/11/2011 | 64800 | 11/13/2013 | 7200", tempUserInfo.substring(i));
    }

//  ====================================================================================================================================
    
    private static final String modifyRequestRemoveStartAddEnd =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
        "<source>" + 
            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
            "<contact>TriVir</contact>" +
        "</source>" +
        "<input>" + 
            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
                "<association>TestUser2</association>" +
                "<modify-attr attr-name=\"Start\">" +
                    "<remove-value>" +
                        "<value timestamp=\"1181832286#1\" type=\"string\">1384308000</value>" + // 11/12/2013 @ 19:00 MST
                    "</remove-value>" +
                "</modify-attr>" +
                "<modify-attr attr-name=\"End\">" +
                    "<add-value>" +
                        "<value timestamp=\"1181832286#1\" type=\"string\">1384308000</value>" + // 11/12/2013 @ 19:00 MST
                    "</add-value>" +
                "</modify-attr>" +
            "</modify>" +           
        "</input>" +
    "</nds>";

    private static final String modifyResponseRemoveStartAddEnd = modifyResponseSuccess;

    public void testTempRemoveStartAddEnd() throws SAXException, IOException, TransformerException, AceToolkitException {
        setTempUser(defaultLogin);
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestRemoveStartAddEnd);
        Document response = this.subscriber.execute(request, null).getDocument();
        long currentCtime = new Date().getTime() / 1000;
        String currentDate = TimeUtil.utcDateFromCtime(currentCtime);
        int currentSeconds = TimeUtil.utcSecondsFromCtime(currentCtime);
        AllTests.printDocumentToScreen("testTempRemoveStartAddEnd: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseRemoveStartAddEnd), response);
        
        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 1 | " + currentDate + " | " + currentSeconds + " | 11/13/2013 | 7200", tempUserInfo.substring(i));
    }

    public void testNonTempRemoveStartAddEnd() throws SAXException, IOException, TransformerException, AceToolkitException {
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestRemoveStartAddEnd);
        Document response = this.subscriber.execute(request, null).getDocument();
        long currentCtime = new Date().getTime() / 1000;
        String currentDate = TimeUtil.utcDateFromCtime(currentCtime);
        int currentSeconds = TimeUtil.utcSecondsFromCtime(currentCtime);
        AllTests.printDocumentToScreen("testNonTempRemoveStartAddEnd: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseRemoveStartAddEnd), response);
        
        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 1 | " + currentDate + " | " + currentSeconds + " | 11/13/2013 | 7200", tempUserInfo.substring(i));
    }

//  ====================================================================================================================================
    
    private static final String modifyRequestRemoveAllStartAddEnd =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
        "<source>" + 
            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
            "<contact>TriVir</contact>" +
        "</source>" +
        "<input>" + 
            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
                "<association>TestUser2</association>" +
                "<modify-attr attr-name=\"Start\">" +
                    "<remove-all-values/>" +
                "</modify-attr>" +
                "<modify-attr attr-name=\"End\">" +
                    "<add-value>" +
                        "<value timestamp=\"1181832286#1\" type=\"string\">1384308000</value>" + // 11/12/2013 @ 19:00 MST
                    "</add-value>" +
                "</modify-attr>" +
            "</modify>" +           
        "</input>" +
    "</nds>";

    private static final String modifyResponseRemoveAllStartAddEnd = modifyResponseSuccess;

    public void testRemoveTempAllStartAddEnd() throws SAXException, IOException, TransformerException, AceToolkitException {
        setTempUser(defaultLogin);
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestRemoveAllStartAddEnd);
        Document response = this.subscriber.execute(request, null).getDocument();
        long currentCtime = new Date().getTime() / 1000;
        String currentDate = TimeUtil.utcDateFromCtime(currentCtime);
        int currentSeconds = TimeUtil.utcSecondsFromCtime(currentCtime);
        AllTests.printDocumentToScreen("testRemoveTempAllStartAddEnd: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseRemoveAllStartAddEnd), response);
        
        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 1 | " + currentDate + " | " + currentSeconds + " | 11/13/2013 | 7200", tempUserInfo.substring(i));
    }

    public void testRemoveNonTempAllStartAddEnd() throws SAXException, IOException, TransformerException, AceToolkitException {
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestRemoveAllStartAddEnd);
        Document response = this.subscriber.execute(request, null).getDocument();
        long currentCtime = new Date().getTime() / 1000;
        String currentDate = TimeUtil.utcDateFromCtime(currentCtime);
        int currentSeconds = TimeUtil.utcSecondsFromCtime(currentCtime);
        AllTests.printDocumentToScreen("testRemoveNonTempAllStartAddEnd: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseRemoveAllStartAddEnd), response);
        
        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 1 | " + currentDate + " | " + currentSeconds + " | 11/13/2013 | 7200", tempUserInfo.substring(i));
    }

//  ====================================================================================================================================
    
    private static final String modifyRequestRemoveStart =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
        "<source>" + 
            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
            "<contact>TriVir</contact>" +
        "</source>" +
        "<input>" + 
            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
                "<association>TestUser2</association>" +
                "<modify-attr attr-name=\"Start\">" +
                    "<remove-value>" +
                        "<value timestamp=\"1181832286#1\" type=\"string\">1384308000</value>" + // 11/12/2013 @ 19:00 MST
                    "</remove-value>" +
                "</modify-attr>" +
            "</modify>" +           
        "</input>" +
    "</nds>";

    private static final String modifyResponseRemoveStart = modifyResponseSuccess;

    public void testTempRemoveStart() throws SAXException, IOException, TransformerException, AceToolkitException {
        setTempUser(defaultLogin);
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestRemoveStart);
        Document response = this.subscriber.execute(request, null).getDocument();
        long currentCtime = new Date().getTime() / 1000;
        String currentDate = TimeUtil.utcDateFromCtime(currentCtime);
        int currentSeconds = TimeUtil.utcSecondsFromCtime(currentCtime);
        AllTests.printDocumentToScreen("testTempRemoveStart: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseRemoveStart), response);
        
        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 1 | " + currentDate + " | " + currentSeconds + " | 12/12/2012 | 68400", tempUserInfo.substring(i));
    }

    public void testNonTempRemoveStart() throws SAXException, IOException, TransformerException, AceToolkitException {
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestRemoveStart);
        Document response = this.subscriber.execute(request, null).getDocument();
        AllTests.printDocumentToScreen("testNonTempRemoveStart: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseRemoveStart), response);
    }

//  ====================================================================================================================================
    
    private static final String modifyRequestRemoveAllStart =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
        "<source>" + 
            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
            "<contact>TriVir</contact>" +
        "</source>" +
        "<input>" + 
            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
                "<association>TestUser2</association>" +
                "<modify-attr attr-name=\"Start\">" +
                    "<remove-all-values/>" +
                "</modify-attr>" +
            "</modify>" +           
        "</input>" +
    "</nds>";

    private static final String modifyResponseRemoveAllStart = modifyResponseSuccess;

    public void testTempRemoveAllStart() throws SAXException, IOException, TransformerException, AceToolkitException {
        setTempUser(defaultLogin);
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestRemoveAllStart);
        Document response = this.subscriber.execute(request, null).getDocument();
        long currentCtime = new Date().getTime() / 1000;
        String currentDate = TimeUtil.utcDateFromCtime(currentCtime);
        int currentSeconds = TimeUtil.utcSecondsFromCtime(currentCtime);
        AllTests.printDocumentToScreen("testTempRemoveAllStart: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseRemoveAllStart), response);
        
        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 1 | " + currentDate + " | " + currentSeconds + " | 12/12/2012 | 68400", tempUserInfo.substring(i));
    }

    public void testNonTempRemoveAllStart() throws SAXException, IOException, TransformerException, AceToolkitException {
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestRemoveAllStart);
        Document response = this.subscriber.execute(request, null).getDocument();
        AllTests.printDocumentToScreen("testNonTempRemoveAllStart: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseRemoveAllStart), response);
        
        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 0 | 01/01/" + AllTests.startYear + " | 0 | 01/01/" + AllTests.endYear + " | 0", tempUserInfo.substring(i));
    }

//  ====================================================================================================================================
    
    private static final String modifyRequestAddStartRemoveEnd =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
        "<source>" + 
            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
            "<contact>TriVir</contact>" +
        "</source>" +
        "<input>" + 
            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
                "<association>TestUser2</association>" +
                "<modify-attr attr-name=\"Start\">" +
                    "<add-value>" +
                        "<value timestamp=\"1181832286#1\" type=\"string\">1384308000</value>" + // 11/12/2013 @ 19:00 MST
                    "</add-value>" +
                "</modify-attr>" +
                "<modify-attr attr-name=\"End\">" +
                    "<remove-value>" +
                        "<value timestamp=\"1181832286#1\" type=\"string\">1384308000</value>" + // 11/12/2013 @ 19:00 MST
                    "</remove-value>" +
                "</modify-attr>" +
            "</modify>" +           
        "</input>" +
    "</nds>";

    private static final String modifyResponseAddStartRemoveEnd = modifyResponseSuccess;

    public void testTempAddStartRemoveEnd() throws SAXException, IOException, TransformerException, AceToolkitException {
        setTempUser(defaultLogin);
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestAddStartRemoveEnd);
        Document response = this.subscriber.execute(request, null).getDocument();
        AllTests.printDocumentToScreen("testTempAddStartRemoveEnd: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseAddStartRemoveEnd), response);
        
        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 0 | 11/11/2011 | 64800 | 12/12/2012 | 68400", tempUserInfo.substring(i));
    }

    public void testNonTempAddStartRemoveEnd() throws SAXException, IOException, TransformerException, AceToolkitException {
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestAddStartRemoveEnd);
        Document response = this.subscriber.execute(request, null).getDocument();
        AllTests.printDocumentToScreen("testNonTempAddStartRemoveEnd: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseAddStartRemoveEnd), response);
        
        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 0 | 01/01/" + AllTests.startYear + " | 0 | 01/01/" + AllTests.endYear + " | 0", tempUserInfo.substring(i));
    }

//  ====================================================================================================================================
    
    private static final String modifyRequestRemoveStartAndEnd =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
        "<source>" + 
            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
            "<contact>TriVir</contact>" +
        "</source>" +
        "<input>" + 
            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
                "<association>TestUser2</association>" +
                "<modify-attr attr-name=\"Start\">" +
                    "<add-value>" +
                        "<value timestamp=\"1181832286#1\" type=\"string\">1384308000</value>" + // 11/12/2013 @ 19:00 MST
                    "</add-value>" +
                "</modify-attr>" +
                "<modify-attr attr-name=\"End\">" +
                    "<remove-value>" +
                        "<value timestamp=\"1181832286#1\" type=\"string\">1384308000</value>" + // 11/12/2013 @ 19:00 MST
                    "</remove-value>" +
                "</modify-attr>" +
            "</modify>" +           
        "</input>" +
    "</nds>";

    private static final String modifyResponseRemoveStartAndEnd = modifyResponseSuccess;

    public void testTempRemoveStartAndEnd() throws SAXException, IOException, TransformerException, AceToolkitException {
        setTempUser(defaultLogin);
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestRemoveStartAndEnd);
        Document response = this.subscriber.execute(request, null).getDocument();
        AllTests.printDocumentToScreen("testTempRemoveStartAndEnd: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseRemoveStartAndEnd), response);
        
        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 0 | 11/11/2011 | 64800 | 12/12/2012 | 68400", tempUserInfo.substring(i));
    }

    public void testNonTempRemoveStartAndEnd() throws SAXException, IOException, TransformerException, AceToolkitException {
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestRemoveStartAndEnd);
        Document response = this.subscriber.execute(request, null).getDocument();
        AllTests.printDocumentToScreen("testNonTempRemoveStartAndEnd: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseRemoveStartAndEnd), response);
        
        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 0 | 01/01/" + AllTests.startYear + " | 0 | 01/01/" + AllTests.endYear + " | 0", tempUserInfo.substring(i));
    }

//  ====================================================================================================================================

    private static final String modifyRequestRemoveEnd =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
        "<source>" + 
            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
            "<contact>TriVir</contact>" +
        "</source>" +
        "<input>" + 
            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
                "<association>TestUser2</association>" +
                "<modify-attr attr-name=\"End\">" +
                    "<remove-value>" +
                        "<value timestamp=\"1181832286#1\" type=\"string\">1200164416640</value>" + // 12/12/2007 = TestSubscriber.endDate
                    "</remove-value>" +
                "</modify-attr>" +
            "</modify>" +           
        "</input>" +
    "</nds>";

    private static final String modifyResponseRemoveEnd = modifyResponseSuccess;
        
    public void testTempRemoveEnd() throws SAXException, IOException, TransformerException, AceToolkitException {        
        setTempUser(defaultLogin);
        // Now going to remove 1200164416640 or 12/12/2007 = TestSubscriber.endDate
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestRemoveEnd);
        Document response = this.subscriber.execute(request, null).getDocument();
        AllTests.printDocumentToScreen("testRemoveEnd: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseRemoveEnd), response);
        
        api.setTempUser("", 0, "", 0, "-TestUser2");
        
        // Confirm user is no longer a temp user, and that end dates are cleared:
        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        // Note: clearing the end date only removes temp status - it does not actually remove the value. 
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 0 | 11/11/2011 | 64800 | 12/12/2012 | 68400", tempUserInfo.substring(i));
    }

    public void testNonTempRemoveEnd() throws SAXException, IOException, TransformerException, AceToolkitException {        
        // Now going to remove 1200164416640 or 12/12/2007 = TestSubscriber.endDate
        XmlDocument request = new XmlDocument(TestSubscriberModifyUserTemp.modifyRequestRemoveEnd);
        Document response = this.subscriber.execute(request, null).getDocument();
        AllTests.printDocumentToScreen("testNonTempRemoveEnd: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(TestSubscriberModifyUserTemp.modifyResponseRemoveEnd), response);
        
        // Confirm user is no longer a temp user, and that end dates are cleared:
        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        // Note: clearing the end date only removes temp status - it does not actually remove the value. 
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 0 | 01/01/" + AllTests.startYear + " | 0 | 01/01/" + AllTests.endYear + " | 0", tempUserInfo.substring(i));
    }

//  ====================================================================================================================================

    private static final String modifyRequestAddEndPastNoStart =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" +
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>Novell, Inc.</contact>" +
            "</source>" +
            "<input>" +
                "<modify class-name=\"User\" event-id=\"ACE-SERVER-NDS#20071206074714#1#1\" qualified-src-dn=\"O=HEI\\OU=PEOPLE\\CN=TestUser2\" src-dn=\"\\HEITREE\\HEI\\PEOPLE\\TestUser2\" src-entry-id=\"33084\" timestamp=\"1196927234#2\">" +
                    "<association state=\"associated\">TestUser2</association>" +
//                    "<modify-attr attr-name=\"Start\">" +
//                        "<remove-all-values/>" +
//                        "<add-value>" +
//                            "<value>504921600</value>" +
//                        "</add-value>" +
//                    "</modify-attr>" +
                    "<modify-attr attr-name=\"End\">" +
                        "<remove-all-values/>" +
                        "<add-value>" +
                            "<value>1196840834</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +
            "</input>" +
        "</nds>";
    
    private static final String modifyResponseAddEndPastNoStart =
        "<nds dtdversion=\"2.0\">" +
            "<source>" +
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<output>" +
                "<status event-id=\"ACE-SERVER-NDS#20071206074714#1#1\" level=\"success\" type=\"driver-general\"/>" +
            "</output>" +
        "</nds>";

    public void testAddEndPastNoStart() throws SAXException, IOException, TransformerException, AceToolkitException {        
        XmlDocument request = new XmlDocument(modifyRequestAddEndPastNoStart);
        Document response = this.subscriber.execute(request, null).getDocument();
        AllTests.printDocumentToScreen("testAddEndPastNoStart: add response", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseAddEndPastNoStart), response);
        
        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 1 | 01/01/1986 | 0 | 12/05/2007 | 25200", tempUserInfo.substring(i));
    }

//  ====================================================================================================================================

    private static final String modifyRequestChangeEndDate = 
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" +
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>Novell, Inc.</contact>" +
            "</source>" +
            "<input>" +
                "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" qualified-src-dn=\"O=HEI\\OU=PEOPLE\\CN=TestUser2\" src-dn=\"\\HEITREE\\HEI\\PEOPLE\\TestUser2\" src-entry-id=\"33152\" timestamp=\"1197674917#2\">" +
                    "<association state=\"associated\">TestUser2</association>" +
                    "<modify-attr attr-name=\"End\">" +
                        "<remove-value>" +
                            "<value timestamp=\"1197674910#4\" type=\"time\">1355302800</value>" +
                        "</remove-value>" +
                        "<add-value>" +
                            "<value timestamp=\"1197674917#2\" type=\"time\">1318525200</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +
            "</input>" +
        "</nds>";

    private static final String modifyResponseChangeEndDate = modifyResponseSuccess;
    
    public void testChangeEndDate() throws SAXException, IOException, TransformerException, AceToolkitException {        
        api.setTempUser("1/1/1988", 0, "12/4/1998", 0, defaultLogin);
        XmlDocument request = new XmlDocument(modifyRequestChangeEndDate);
        Document response = this.subscriber.execute(request, null).getDocument();
        AllTests.printDocumentToScreen("testChangeEndDate: response", response);

        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseChangeEndDate), response);

        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 1 | 01/01/1988 | 25200 | 10/13/2011 | 61200", tempUserInfo.substring(i));
    }

//  ====================================================================================================================================

    private static final String modifyRequestChangeEndDatePast =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" +
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>Novell, Inc.</contact>" +
            "</source>" +
            "<input>" +
                "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" qualified-src-dn=\"O=HEI\\OU=PEOPLE\\CN=TestUser2\\\" src-dn=\"\\HEITREE\\HEI\\PEOPLE\\TestUser2\" src-entry-id=\"33565\" timestamp=\"1200406021#2\">" +
                    "<association state=\"associated\">TestUser2</association>" +
                    "<modify-attr attr-name=\"End\">" +
                        "<remove-value>" +
                            "<value timestamp=\"1200406012#4\" type=\"time\">1355302800</value>" +
                        "</remove-value>" +
                        "<add-value>" +
                            "<value timestamp=\"1200406021#2\" type=\"time\">976611600</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +
            "</input>" +
        "</nds>";

    private static final String modifyResponseChangeEndDatePast = modifyResponseSuccess;
    
    public void testChangeEndDatePast() throws SAXException, IOException, TransformerException, AceToolkitException {        
        api.setTempUser("10/10/2012", 0, "10/11/2012", 0, defaultLogin);
        XmlDocument request = new XmlDocument(modifyRequestChangeEndDatePast);
        Document response = this.subscriber.execute(request, null).getDocument();
        AllTests.printDocumentToScreen("testChangeEndDatePast: response", response);

        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseChangeEndDatePast), response);

        String tempUserInfo = api.listUserInfo("-TestUser2", (byte)'|');        
        int i = tempUserInfo.indexOf('|'); // skip the ID field
        assertEquals("| User1 | Test | TestUser2 | 1 | 0 |  | 1 | 12/11/2000 | 32400 | 12/12/2000 | 32400", tempUserInfo.substring(i));
    }
//*/
}

