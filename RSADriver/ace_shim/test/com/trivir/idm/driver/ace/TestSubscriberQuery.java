package com.trivir.idm.driver.ace;

import java.io.IOException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.novell.nds.dirxml.driver.SubscriptionShim;
import com.novell.nds.dirxml.driver.Trace;
import com.novell.nds.dirxml.driver.XmlDocument;
import com.trivir.ace.AceToolkitException;
import com.trivir.ace.api.AceApi;

public class TestSubscriberQuery extends XMLTestCase {
    private AceDriverShim driver;
    private SubscriptionShim subscriber;
    private AceApi api;
    
    private static final String tokenSerialNum = TestUtil.tokenSerialNum;
    private static final String tokenSerialNum2 = TestUtil.tokenSerialNum2;
    private static final String tokenExpiration = TestUtil.tokenExpiration;
    
    private static final String groupName = "TestGroup1";
    private static final String groupName1 = "TestGroup2";
    private static final String groupName2 = "TestGroup3@TestSecurityDomain";
    
    private static final String profileName = "TESTPROFILE1";

    public TestSubscriberQuery() {
        Trace.registerImpl(CustomTraceInterface.class, 1);
    }

    protected void setUp() throws Exception {
        driver = new AceDriverShim();
        Document response = driver.init(new XmlDocument(TestDriverShim.getInitRequest())).getDocument();
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);
        subscriber = driver.getSubscriptionShim();

        XmlDocument request = new XmlDocument(TestChannelInit.subscriberInitRequest);
        response = subscriber.init(request).getDocument();
        //AllTests.printDocumentToScreen("setUp", response);
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);
        
        api = TestUtil.getApi();
        
        try {
            api.listUserInfo("-TestUser2");
            api.deleteUser("-TestUser2");
        } catch (Exception e) {}
        api.addUser("last", "first", "TestUser2", null, "/bin/bash", TestUtil.DEFAULT_USER_PASSWORD);
    }
    
    protected void tearDown() throws Exception {
        try {
            api.listUserInfo("-TestUser2");
            api.deleteUser("-TestUser2");
        } catch (Exception e) {}
        api.destroy();
    }

//	====================================================================================================================================

    private static final String queryUserByDefaultLoginNoUser =
    	"<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
    		"<source>" + 
    			"<product version=\"3.5.1.20070411 \">DirXML</product>" +
    			"<contact>Novell, Inc.</contact>" +
    		"</source>" +
    		"<input>" +
    			"<query class-name=\"User\" event-id=\"0\" scope=\"subtree\">" +
	    			"<search-class class-name=\"User\"/>" +
	    			"<search-attr attr-name=\"DefaultLogin\">" +
	    				"<value type=\"string\">TestUser2DOESNTEXIST</value>" + 
	    			"</search-attr>" + 
	    			"<read-attr/>" +
    			"</query>" +
    		"</input>" +
		"</nds>";

	private static final String queryUserByDefaultLogiNoUserResponse =
		"<nds dtdversion=\"2.0\">" +
			"<source>" +
				"<contact>TriVir</contact>" +
				"<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
			"</source>" +
			"<output>" +
				"<status event-id=\"0\" level=\"success\" type=\"driver-general\"/>" +
			"</output>" +
		"</nds>";
  
	public void testQueryByDefaultLoginNoUser() throws SAXException, IOException, TransformerException, TransformerConfigurationException {
		XmlDocument request = new XmlDocument(queryUserByDefaultLoginNoUser);
		Document response = subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testQueryByDefaultLoginNoUser", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(queryUserByDefaultLogiNoUserResponse), response);
	}
//	====================================================================================================================================

    private static final String queryUserByDefaultLogin =
    	"<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
    		"<source>" + 
    			"<product version=\"3.5.1.20070411 \">DirXML</product>" +
    			"<contact>Novell, Inc.</contact>" +
    		"</source>" +
    		"<input>" +
    			"<query class-name=\"User\" event-id=\"0\" scope=\"subtree\">" +
	    			"<search-class class-name=\"User\"/>" +
	    			"<search-attr attr-name=\"DefaultLogin\">" +                            
	    				"<value type=\"string\">TestUser2</value>" + 
	    			"</search-attr>" + 
	    			"<read-attr/>" +
    			"</query>" +
    		"</input>" +
		"</nds>";

	private static final String queryUserByDefaultLoginResponse =
		"<nds dtdversion=\"2.0\">" +
			"<source>" +
				"<contact>TriVir</contact>" +
				"<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
			"</source>" +
			"<output>" +
				"<instance class-name=\"User\">" +
					"<association>TestUser2</association>" + 
				"</instance>" +
				"<status event-id=\"0\" level=\"success\" type=\"driver-general\"/>" +
			"</output>" +
		"</nds>";
  
	public void testQueryByDefaultLogin() throws SAXException, IOException, TransformerException, TransformerConfigurationException {
		XmlDocument request = new XmlDocument(queryUserByDefaultLogin);
		Document response = subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testQueryByDefaultLogin", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(queryUserByDefaultLoginResponse), response);
	}

//	====================================================================================================================================

	private static final String queryUserReadAllAttrsExplicit =
		"<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
			"<source>" + 
				"<product version=\"3.5.1.20070411 \">DirXML</product>" +
				"<contact>Novell, Inc.</contact>" +
			"</source>" +
			"<input>" +
				"<query class-name=\"User\" event-id=\"0\" scope=\"entry\">" +               		
					"<association>TestUser2</association>" +
					"<read-attr attr-name=\"DefaultLogin\"/>" +
					"<read-attr attr-name=\"LastName\"/>" +
					"<read-attr attr-name=\"FirstName\"/>" +
					"<read-attr attr-name=\"DefaultShell\"/>" +
					"<read-attr attr-name=\"ProfileName\"/>" +
					"<read-attr attr-name=\"MemberOf\"/>" +
					"<read-attr attr-name=\"TokenSerialNumber\"/>" +
					"<read-attr attr-name=\"TokenPIN\"/>" +                 
					"<read-attr attr-name=\"Start\"/>" +                    
					"<read-attr attr-name=\"End\"/>" +                  
				"</query>" +
			"</input>" +
		"</nds>";

	private static final String queryUserReadAllAttrsSingleTokenResponse =
	"<nds dtdversion=\"2.0\">" +
		"<source>" +
			"<contact>TriVir</contact>" +	   			
			"<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
		"</source>" +
		"<output>" +
			"<instance class-name=\"User\">" +
				"<association>TestUser2</association>" +
				"<attr attr-name=\"DefaultLogin\">" +
					"<value type=\"string\">TestUser2</value>" +
				"</attr>" +
				"<attr attr-name=\"LastName\">" +
					"<value type=\"string\">last</value>" +
				"</attr>" +
				"<attr attr-name=\"FirstName\">" +
					"<value type=\"string\">first</value>" +
				"</attr>" +
				"<attr attr-name=\"DefaultShell\">" +
					"<value type=\"string\">/bin/bash</value>" +
				"</attr>" +
				"<attr attr-name=\"ProfileName\">" +
					"<value type=\"string\">" + profileName + "</value>" +
				"</attr>" +
				"<attr attr-name=\"MemberOf\">" +
					"<value type=\"string\">" + groupName + "</value>" +
					"<value type=\"string\">" + groupName1 + "</value>" +
					"<value type=\"string\">" + groupName2 + "</value>" +
				"</attr>" +
				"<attr attr-name=\"TokenSerialNumber\">" +
					"<value type=\"string\">" + tokenSerialNum + "</value>" +
				"</attr>" +
//				"<attr attr-name=\"Start\">" +
//					"<value type=\"string\">504921600</value>" +
//				"</attr>" +
//				"<attr attr-name=\"End\">" +                     
//					"<value type=\"string\">504921600</value>" +
//				"</attr>" +
			"</instance>" +
			"<status event-id=\"0\" level=\"success\"  type=\"driver-general\"/>" +
		"</output>" +
	"</nds>";
   
	public void testQueryUserReadAllAttrsExplicit() throws TransformerException, TransformerConfigurationException, AceToolkitException, XpathException, SAXException, IOException {
		api.assignProfile("-TestUser2", profileName);
		api.assignAnotherToken("-TestUser2", tokenSerialNum);
        api.addLoginToGroup("", groupName, "", "-TestUser2");
        api.addLoginToGroup("", groupName1, "", "-TestUser2");
        api.addLoginToGroup("", groupName2, "", "-TestUser2");
		XmlDocument request = new XmlDocument(queryUserReadAllAttrsExplicit);
		Document response = subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testQueryUserReadAllAttrsExplicit", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(queryUserReadAllAttrsSingleTokenResponse), response);
	}
  
//	====================================================================================================================================

	private static final String queryUserReadGroupWithSite =
		"<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
			"<source>" + 
				"<product version=\"3.5.1.20070411 \">DirXML</product>" +
				"<contact>Novell, Inc.</contact>" +
			"</source>" +
			"<input>" +
				"<query class-name=\"User\" event-id=\"0\" scope=\"entry\">" +               		
					"<association>TestUser2</association>" +
					"<read-attr attr-name=\"MemberOf\"/>" +
				"</query>" +
			"</input>" +
		"</nds>";

	private static final String queryUserReadGroupWithSiteResponse =
	"<nds dtdversion=\"2.0\">" +
		"<source>" +
			"<contact>TriVir</contact>" +	   			
			"<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
		"</source>" +
		"<output>" +
			"<instance class-name=\"User\">" +
				"<association>TestUser2</association>" +
				"<attr attr-name=\"MemberOf\">" +
					"<value type=\"string\">" + groupName + "</value>" +
					"<value type=\"string\">" + groupName2 + "</value>" +
				"</attr>" +
			"</instance>" +
			"<status event-id=\"0\" level=\"success\"  type=\"driver-general\"/>" +
		"</output>" +
	"</nds>";
   
	public void testQueryUserReadGroupWithSite() throws SAXException, IOException, TransformerException, TransformerConfigurationException, AceToolkitException {
        api.addLoginToGroup("", groupName, "", "-TestUser2");
        api.addLoginToGroup("", groupName2, "", "-TestUser2");
		XmlDocument request = new XmlDocument(queryUserReadGroupWithSite);
		Document response = subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testQueryUserReadGroupWithSite", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(queryUserReadGroupWithSiteResponse), response);
	}
  
//	====================================================================================================================================

	private static final String queryUserReadAllAttrsImplicit =
		"<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
			"<source>" + 
				"<product version=\"3.5.1.20070411 \">DirXML</product>" +
				"<contact>Novell, Inc.</contact>" +
			"</source>" +
			"<input>" +
				"<query class-name=\"User\" event-id=\"0\" scope=\"entry\">" +               		
					"<association>TestUser2</association>" +
				"</query>" +
			"</input>" +
		"</nds>";

	private static String queryUserReadAllAttrsImplicitResponse(String userNum) {
		String response =
		"<nds dtdversion=\"2.0\">" +
			"<source>" +
				"<contact>TriVir</contact>" +               
				"<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
			"</source>" +
			"<output>" +
				"<instance class-name=\"User\">" +
					"<association>TestUser2</association>" +
//                    "<attr attr-name=\"CreatePIN\">" +                     
//                        "<value type=\"string\">True</value>" +
//                    "</attr>" +
                    "<attr attr-name=\"DefaultLogin\">" +
                        "<value type=\"string\">TestUser2</value>" +
                    "</attr>" +
                    "<attr attr-name=\"DefaultShell\">" +
                        "<value type=\"string\">/bin/bash</value>" +
                    "</attr>" +
//                    "<attr attr-name=\"End\">" +                     
//                        "<value type=\"string\">504921600</value>" +
//                    "</attr>" +
                    "<attr attr-name=\"FirstName\">" +
                        "<value type=\"string\">first</value>" +
                    "</attr>" +
                    "<attr attr-name=\"LastName\">" +
                        "<value type=\"string\">last</value>" +
                    "</attr>" +
                    "<attr attr-name=\"MemberOf\">" +
                        "<value type=\"string\">" + groupName + "</value>" +
                    "</attr>" +
//                    "<attr attr-name=\"MustCreatePIN\">" +
//                        "<value type=\"string\">False</value>" +
//                    "</attr>" +
                    "<attr attr-name=\"ProfileName\">" +
                        "<value type=\"string\">" + profileName + "</value>" +
                    "</attr>" +
//					"<attr attr-name=\"Start\">" +
//						"<value type=\"string\">504921600</value>" +
//					"</attr>" +
					"<attr attr-name=\"TempUser\">" +                     
						"<value type=\"string\">FALSE</value>" +
					"</attr>" +
					"<attr attr-name=\"TokenSerialNumber\">" +
						"<value type=\"string\">" + tokenSerialNum + "</value>" +
					"</attr>" +
                    "<attr attr-name=\"UserNum\">" +
                        "<value type=\"string\">" + userNum + "</value>" +
                    "</attr>" +
				"</instance>" +
				"<status event-id=\"0\" level=\"success\"  type=\"driver-general\"/>" +
			"</output>" +
		"</nds>";
		
		return response;
	}
   
	public void testQueryUserReadAllAttrsImplicit() throws SAXException, IOException, TransformerException, TransformerConfigurationException, AceToolkitException {
		api.addLoginToGroup("", groupName, "", "-TestUser2");
		api.assignProfile("-TestUser2", profileName);
		api.assignAnotherToken("-TestUser2", tokenSerialNum);
		String userNum = api.listUserInfo("-TestUser2").get(AceApi.ATTR_USER_NUM).toString();
		XmlDocument request = new XmlDocument(queryUserReadAllAttrsImplicit);
		Document response = subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testQueryUserReadAllAttrsImplicit", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(queryUserReadAllAttrsImplicitResponse(userNum)), response);
	}
   
//	====================================================================================================================================

	private static final String queryUserReadAllAttrsMultipleTokenRequest =
		"<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
			"<source>" + 
				"<product version=\"3.5.1.20070411 \">DirXML</product>" +
				"<contact>Novell, Inc.</contact>" +
			"</source>" +
			"<input>" +
				"<query class-name=\"User\" event-id=\"0\" scope=\"entry\">" +                       
					"<association>TestUser2</association>" +
					"<read-attr attr-name=\"TokenSerialNumber\"/>" +
				"</query>" +
			"</input>" +
		"</nds>";

	private static final String queryUserReadAllAttrsMultipleTokenResponse =
		"<nds dtdversion=\"2.0\">" +
			"<source>" +
				"<contact>TriVir</contact>" +
				"<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
			"</source>" +
			"<output>" +
				"<instance class-name=\"User\">" +
					"<association>TestUser2</association>" +
					"<attr attr-name=\"TokenSerialNumber\">" +
						"<value type=\"string\">" + tokenSerialNum + "</value>" +
						"<value type=\"string\">" + tokenSerialNum2 + "</value>" +
					"</attr>" +                    
				"</instance>" +
				"<status event-id=\"0\" level=\"success\"  type=\"driver-general\"/>" +
			"</output>" +
		"</nds>";
    
	public void testQueryUserReadAllAttrsMulipleTokens() throws SAXException, IOException, TransformerException, TransformerConfigurationException, AceToolkitException {
		api.assignAnotherToken("-TestUser2", tokenSerialNum);
		api.assignAnotherToken("-TestUser2", tokenSerialNum2);
		XmlDocument request = new XmlDocument(queryUserReadAllAttrsMultipleTokenRequest);
		Document response = subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testQueryUserReadAllAttrsMulipleTokens", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(queryUserReadAllAttrsMultipleTokenResponse), response);
	}

//  ====================================================================================================================================
   
    private static final String queryMatchingUser =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
          "<source>" + 
            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
            "<contact>Novell, Inc.</contact>" +
          "</source>" +
          "<input>" +
            "<query class-name=\"User\" event-id=\"0\" scope=\"entry\">" +
              "<association>TestUser2</association>" +
              "<read-attr/>" +
            "</query>" +
          "</input>" +
        "</nds>";
    
    private static final String queryMatchingUserResponse =
        "<nds dtdversion=\"2.0\">" +
            "<source>" +
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<output>" +
                "<instance class-name=\"User\">" +
                    "<association>TestUser2</association>" + 
                "</instance>" +
                "<status event-id=\"0\" level=\"success\"  type=\"driver-general\"/>" +
            "</output>" +
        "</nds>";
    
    public void testQueryForMatchingUser() throws SAXException, IOException, TransformerException, TransformerConfigurationException {
        XmlDocument request = new XmlDocument(queryMatchingUser);
        Document response = subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testQueryForMatchingUser", response);
        this.assertXMLEqual(XMLUnit.buildControlDocument(queryMatchingUserResponse), response);
    }

//  ====================================================================================================================================
    
    private static final String queryEntryXDSTokenExp =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>Novell, Inc.</contact>" +
            "</source>" +
            "<input>" +
                "<query class-name=\"Token\" event-id=\"0\" scope=\"entry\">" +
                    "<association>" + tokenSerialNum + "</association>" +
                    "<read-attr attr-name=\"Death\"/>" +
                "</query>" +
            "</input>" +
        "</nds>";

    private static final String queryEntryXDSResponseTokenExp =
        "<nds dtdversion=\"2.0\">" +
            "<source>" +
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<output>" +
                "<instance class-name=\"Token\">" +
                    "<association>" + tokenSerialNum + "</association>" + 
                    "<attr attr-name=\"Death\">" +
                        "<value>" + tokenExpiration + "</value>" +
                    "</attr>" +                
                "</instance>" +
                "<status event-id=\"0\" level=\"success\" type=\"driver-general\"/>" +
            "</output>" +
        "</nds>";
   
    public void testQueryForTokenExp() throws SAXException, IOException, TransformerException, TransformerConfigurationException {
        XmlDocument request = new XmlDocument(queryEntryXDSTokenExp);
        Document response = subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testQueryForTokenExp", response);
        this.assertXMLEqual(XMLUnit.buildControlDocument(queryEntryXDSResponseTokenExp), response);
    }

//  ====================================================================================================================================
       
	private static final String queryEntryXDSTokenExpAndSerial =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>Novell, Inc.</contact>" +
            "</source>" +
            "<input>" +
                "<query class-name=\"Token\" event-id=\"0\" scope=\"entry\">" +
                    "<association>" + tokenSerialNum + "</association>" + 
                    "<read-attr attr-name=\"Death\"/>" +
                    "<read-attr attr-name=\"SerialNum\"/>" +
                "</query>" +
            "</input>" +
        "</nds>";

	private static final String queryEntryXDSResponseExpAndSerial =
		"<nds dtdversion=\"2.0\">" +
			"<source>" +
				"<contact>TriVir</contact>" +
				"<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
			"</source>" +
			"<output>" +
				"<instance class-name=\"Token\">" +
					"<association>" + tokenSerialNum + "</association>" + 
					"<attr attr-name=\"Death\">" +
						"<value>" + tokenExpiration + "</value>" +
					"</attr>" +                
					"<attr attr-name=\"SerialNum\">" +
						"<value>" + tokenSerialNum + "</value>" +
					"</attr>" +                
				"</instance>" +
			"<status event-id=\"0\" level=\"success\" type=\"driver-general\"/>" +
			"</output>" +
		"</nds>";
    
	public void testQueryForTokenExpAndSerial() throws SAXException, IOException, TransformerException, TransformerConfigurationException {
		XmlDocument request = new XmlDocument(queryEntryXDSTokenExpAndSerial);
		Document response = subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testQueryForTokenExpAndSerial", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(queryEntryXDSResponseExpAndSerial), response);
	}
//  ====================================================================================================================================
	   
	private static final String queryEntryXDSTokenAll =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>Novell, Inc.</contact>" +
            "</source>" +
            "<input>" +
                "<query class-name=\"Token\" event-id=\"0\" scope=\"subtree\">" +
                	"<read-attr/>" +
                "</query>" + 
            "</input>" +
        "</nds>";

	public void testQueryForTokensAll() throws TransformerException, XpathException {
		XmlDocument request = new XmlDocument(queryEntryXDSTokenAll);
		Document response = subscriber.execute(request, null).getDocument();
//		AllTests.printDocumentToScreen("testQueryForTokensAll", response);
		this.assertXpathEvaluatesTo(Integer.toString(TestUtil.tokenCount), "count(//instance/association)", response);
	}

//  ====================================================================================================================================
	   
	private static final String queryEntryXDSTokenAllUnassigned =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>Novell, Inc.</contact>" +
            "</source>" +
            "<input>" +
                "<query class-name=\"Token\" event-id=\"0\" scope=\"subtree\">" +
                	"<search-attr attr-name=\"Assigned\">" +
                		"<value>FALSE</value>" +                		
                	"</search-attr>" +
                "</query>" + 
            "</input>" +
        "</nds>";

	public void testQueryForTokensAllUnassigned() throws TransformerException, XpathException, AceToolkitException {
		api.assignAnotherToken("-TestUser2", tokenSerialNum);
		XmlDocument request = new XmlDocument(queryEntryXDSTokenAllUnassigned);
		Document response = subscriber.execute(request, null).getDocument();
//		AllTests.printDocumentToScreen("testQueryForTokensAllUnassigned", response);
		this.assertXpathEvaluatesTo(Integer.toString(TestUtil.tokenCount - 1), "count(//instance/association)", response);
	}
	
	//  ====================================================================================================================================
	   
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
						"<value>FALSE</value>" +
					"</attr>" +                
				"</instance>" +
				"<status event-id=\"0\" level=\"success\" type=\"driver-general\"/>" +
			"</output>" +
		"</nds>";
    
	public void testQueryForTokenDisabled() throws InterruptedException, AceToolkitException, SAXException, IOException, TransformerException, TransformerConfigurationException {
		api.assignAnotherToken("-TestUser2", tokenSerialNum);
		api.enableToken(tokenSerialNum);
		XmlDocument request = new XmlDocument(queryEntryXDSTokenDisabled);
		Document response = subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testQueryForTokenDisabled", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(queryEntryXDSResponseDisabled), response);
	}
	
//  ====================================================================================================================================

	private static final String queryGetAllUsers =
		"<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
			"<source>" + 
				"<product version=\"3.5.1.20070411 \">DirXML</product>" +
				"<contact>Novell, Inc.</contact>" +
			"</source>" +
			"<input>" +
				"<query class-name=\"User\" event-id=\"0\" scope=\"subtree\">" +
					"<read-attr/>" +
				"</query>" +
			"</input>" +
		"</nds>";
    
	private static final String queryGetAllUsersResponse =
		"<nds dtdversion=\"2.0\">" +
			"<source>" +
				"<contact>TriVir</contact>" +
				"<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
			"</source>" +
			"<output>" +
				"<instance class-name=\"User\">" + 
					"<association>@PROXYUSER@</association>" + 
				"</instance>" +
//				"<instance class-name=\"User\">" + 
//					"<association>SelfServiceAdmin_juuseyre</association>" + 
//				"</instance>" +
				"<instance class-name=\"User\">" + 
					"<association>admin</association>" + 
				"</instance>" +
				"<instance class-name=\"User\">" + 
					"<association>trustedapp</association>" + 
				"</instance>" +
				"<instance class-name=\"User\">" + 
					"<association>idmuser</association>" + 
				"</instance>" +
				"<instance class-name=\"User\">" + 
					"<association>TestUser2</association>" + 
				"</instance>" +
				"<status event-id=\"0\" level=\"success\" type=\"driver-general\"/>" +
			"</output>" +
		"</nds>";
    
	public void testQueryForAllUsers() throws SAXException, IOException, TransformerException, TransformerConfigurationException {
		XmlDocument request = new XmlDocument(queryGetAllUsers);
		Document response = subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testQueryForAllUsers", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(queryGetAllUsersResponse), response);
	}

//  ====================================================================================================================================
    
//    private static final String queryExFirstRequest =
//		"<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
//			"<source>" + 
//				"<product version=\"3.5.1.20070411 \">DirXML</product>" +
//				"<contact>Novell, Inc.</contact>" +
//			"</source>" +
//			"<input>" +
//				"<query-ex class-name=\"User\" event-id=\"0\" max-result-count=\"2\" scope=\"subtree\">" + 
//					"<read-attr/>" + 
//				"</query-ex>" +
//			"</input>" +
//		"</nds>";
//    
//    private static final String queryExResponseFirstHalf =
//		"<nds dtdversion=\"1.0\" ndsversion=\"8.5\" xml:space=\"default\">" +
//	        "<source>" +
//		        "<contact>TriVir</contact>" +
//		        "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
//	        "</source>" +
//			"<output>" +
//				"<instance class-name=\"User\">" + 
//					"<association>TestUser2</association>" + 
//				"</instance>" +
//				"<instance class-name=\"User\">" + 
//					"<association>frankh</association>" + 
//				"</instance>" +
//				"<query-token event-id=\"0\">RSA1234</query-token>" + 
//				"<status event-id=\"0\" level=\"success\"  type=\"driver-general\"/>" +             
//			"</output>" +
//		"</nds>";
//    
//    private static final String queryExSecondRequest =
//		"<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
//			"<source>" + 
//				"<product version=\"3.5.1.20070411 \">DirXML</product>" +
//				"<contact>Novell, Inc.</contact>" +
//			"</source>" +
//			"<input>" +
//				"<query-ex class-name=\"User\" event-id=\"0\" max-result-count=\"2\" scope=\"subtree\">" + 
//					"<read-attr/>" +
//					"<query-token>RSA1234</query-token>" + 
//				"</query-ex>" +
//			"</input>" +
//		"</nds>";
//
//    private static final String queryExResponseSecondHalf =
//		"<nds dtdversion=\"1.0\" ndsversion=\"8.5\" xml:space=\"default\">" +
//			"<output>" +
//				"<instance class-name=\"User\">" + 
//					"<association>SYSTEM</association>" + 
//				"</instance>" +
//				"<instance class-name=\"User\">" + 
//					"<association>Administrator</association>" + 
//				"</instance>" +
//				// note: query-token apparently does not apear when results are exhausted.
//				"<status event-id=\"0\" level=\"success\" type=\"driver-general\"/>" +
//			"</output>" +
//		"</nds>";

// TODO!!!!!!! commented this test out because I haven't been able to find query-ex implemented in the code
//    public void testQueryExForAllUsersBatched() throws SAXException, IOException, TransformerException, TransformerConfigurationException {
//        XmlDocument request = new XmlDocument(queryExFirstRequest);
//        Document response = subscriber.execute(request, null).getDocument();
//        AllTests.printDocumentToScreen("testQueryExForAllUsersBatched-1", response);
//        this.assertXMLEqual(XMLUnit.buildControlDocument(queryExResponseFirstHalf), response);
//        
//        request = new XmlDocument(queryExSecondRequest);
//        response = subscriber.execute(request, null).getDocument();
//        AllTests.printDocumentToScreen("testQueryExForAllUsersBatched-2", response);
//        this.assertXMLEqual(XMLUnit.buildControlDocument(queryExResponseSecondHalf), response);        
//    }

//  ====================================================================================================================================
    
    private static final String requestQueryWithoutClassName =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" +
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>Novell, Inc.</contact>" +
            "</source>" +
            "<input>" +
                "<query dest-dn=\"\\HEITREE\\SERVICES\\DriverSet\\RSA-ACE\" scope=\"entry\">" +
                    "<read-attr attr-name=\"Security Equals\"/>" +
                "</query>" +
            "</input>" +
        "</nds>";
    
    private static final String responseQueryWithoutClassName =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<nds dtdversion=\"2.0\">" +
            "<source>" +
                "<contact>TriVir</contact>" +
                "<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
            "</source>" +
            "<output>" +
                "<status level=\"error\" type=\"driver-general\">" +
                    "<description>'query' operation missing class-name attribute.</description>" +
                "</status>" +
            "</output>" +
        "</nds>";

    public void testQueryWithoutClassName() throws SAXException, IOException, TransformerException, TransformerConfigurationException {
        XmlDocument request = new XmlDocument(requestQueryWithoutClassName);
        Document response = subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testQueryWithoutClassName", response);
        this.assertXMLEqual(XMLUnit.buildControlDocument(responseQueryWithoutClassName), response);
    }
    
//  ====================================================================================================================================

    private static final String queryUserByAssociation =
    	"<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
    		"<source>" + 
    			"<product version=\"3.5.1.20070411 \">DirXML</product>" +
    			"<contact>Novell, Inc.</contact>" +
    		"</source>" +
    		"<input>" +
    			"<query class-name=\"User\" event-id=\"0\" scope=\"entry\">" +
    				"<association>TestUser2</association>" +
    				"<read-attr/>" + 
    			"</query>" +
    		"</input>" +
		"</nds>";

	private static final String queryUserByAssociationResponse =
		"<nds dtdversion=\"2.0\">" +
			"<source>" +
				"<contact>TriVir</contact>" +
				"<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
			"</source>" +
			"<output>" +
				"<instance class-name=\"User\">" +
					"<association>TestUser2</association>" + 
				"</instance>" +
				"<status event-id=\"0\" level=\"success\" type=\"driver-general\"/>" +
			"</output>" +
		"</nds>";
  
	
	public void testQueryByAssociation() throws SAXException, IOException, TransformerException, TransformerConfigurationException {
		XmlDocument request = new XmlDocument(queryUserByAssociation);
		Document response = subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testQueryByAssociation", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(queryUserByAssociationResponse), response);
	}

//	====================================================================================================================================

    private static final String queryUserByAssociationDoesntExist =
    	"<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
    		"<source>" + 
    			"<product version=\"3.5.1.20070411 \">DirXML</product>" +
    			"<contact>Novell, Inc.</contact>" +
    		"</source>" +
    		"<input>" +
    			"<query class-name=\"User\" event-id=\"0\" scope=\"entry\">" +
    				"<association>thisuserdoesntedxistinace</association>" + 
	    			"<read-attr attr-name=\"TokenSerialNumber\"/>" +
    			"</query>" +
    		"</input>" +
		"</nds>";

	private static final String queryUserByAssociationDoesntExistResponse =
		"<nds dtdversion=\"2.0\">" + 
			"<source>" + 
				"<contact>TriVir</contact>" + 
				"<product instance=\"RSA Driver\">RSA IDM Driver</product>" + 
			"</source>" + 
			"<output>" + 
				"<status event-id=\"0\" level=\"success\" type=\"driver-general\"/>" +
			"</output>" + 
		"</nds>";
	
	public void testQueryByAssociationDoesntExist() throws SAXException, IOException, TransformerException, TransformerConfigurationException {
		XmlDocument request = new XmlDocument(queryUserByAssociationDoesntExist);
		Document response = subscriber.execute(request, null).getDocument();
		TestUtil.printDocumentToScreen("testQueryByAssociationDoesntExist", response);
		this.assertXMLEqual(XMLUnit.buildControlDocument(queryUserByAssociationDoesntExistResponse), response);
	}
}
