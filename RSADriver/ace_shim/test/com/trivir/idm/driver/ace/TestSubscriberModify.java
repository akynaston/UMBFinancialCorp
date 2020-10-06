package com.trivir.idm.driver.ace;

import java.io.IOException;
import java.util.List;
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

public class TestSubscriberModify extends XMLTestCase {

	private AceDriverShim driver;
    private SubscriptionShim subscriber;
    private AceApi api;
    
    private static final String user = "-TestUser2";
    
    private static final String tokenSerialNum1 = TestUtil.tokenSerialNum;
    private static final String tokenSerialNum2 = TestUtil.tokenSerialNum2;
    
    private static final String groupName = "TestGroup1";
    private static final String groupName1 = "TestGroup2";
    private static final String groupName2 = "TestGroup3@TestSecurityDomain";
    
    private static final String profileName = "TESTPROFILE1";
    private static final String profileName1 = "TESTPROFILE2";
    private static final String profileName2 = "TESTPROFILE3";
    
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
    
    private static final String modifyResponseSuccessRename = 
    	"<nds dtdversion=\"2.0\">" +
        	"<source>" +
            	"<contact>TriVir</contact>" +
            	"<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
           	"</source>" +
           	"<output>" +
           		"<modify-association>" +
           			"<association>TestUser2</association>" +
           			"<association>testLogin</association>" +
           		"</modify-association>" +
           		"<status event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" level=\"success\" type=\"driver-general\"/>" +
        	"</output>" +
        "</nds>";

    public TestSubscriberModify() throws Exception {
        Trace.registerImpl(CustomTraceInterface.class, Trace.XML_TRACE);
    }

    protected void setUp() throws Exception {
        driver = new AceDriverShim();
        Document response = driver.init(new XmlDocument(TestDriverShim.getInitRequest())).getDocument();
        TestUtil.printDocumentToScreen("Setup", response);
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);
        subscriber = driver.getSubscriptionShim();

        XmlDocument request = new XmlDocument(TestChannelInit.subscriberInitRequest);
        response = subscriber.init(request).getDocument();
        //AllTests.printDocumentToScreen("Setup", response);
        assertXpathEvaluatesTo("success", "//nds/output/status/@level", response);

        driver.getPublicationShim().init(new XmlDocument(TestChannelInit.publisherInitRequest));

        api = TestUtil.getApi();
        
        try {
            api.listUserInfo(user);
            api.deleteUser(user);
        } catch (Exception e) {}        
        
        api.addUser("User1", "Test", user.substring(1), null, "", TestUtil.DEFAULT_USER_PASSWORD);
    }
    
    protected void tearDown() throws Exception {
        try {
            api.listUserInfo(user);
            api.deleteUser(user);
        } catch (Exception e) {}        
        api.destroy();
    }

//  ====================================================================================================================================

    private static final String modifyRequestName =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>TriVir</contact>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\" + user.substring(1) + "\">" +
                    "<association>" + user.substring(1) + "</association>" +
                    "<modify-attr attr-name=\"FirstName\">" +
                        "<add-value>" +
                            "<value timestamp=\"1181832286#1\" type=\"string\">Fred</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                    "<modify-attr attr-name=\"LastName\">" +
	                    "<add-value>" +
	                        "<value timestamp=\"1181832286#1\" type=\"string\">Jones</value>" +
	                    "</add-value>" +
	                "</modify-attr>" +
                "</modify>" +           
            "</input>" +
        "</nds>";

    private static final String modifyNameResponse = modifyResponseSuccess;

    public void testModifyName() throws SAXException, IOException, TransformerException, AceToolkitException {
        XmlDocument request = new XmlDocument(modifyRequestName);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testTempAddStart:", response);
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyNameResponse), response);
        
        Map<String, Object> userInfo = api.listUserInfo(user);
        assertEquals("Fred", userInfo.get(AceApi.ATTR_FIRST_NAME));
        assertEquals("Jones", userInfo.get(AceApi.ATTR_LAST_NAME));
    }
    
//  ====================================================================================================================================

    private static final String modifyRequestDefaultLogin =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>TriVir</contact>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\" + user.substring(1) + "\">" +
                    "<association>" + user.substring(1) + "</association>" +
                    "<modify-attr attr-name=\"DefaultLogin\">" +
                        "<add-value>" +
                            "<value timestamp=\"1181832286#1\" type=\"string\">testLogin</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                 "</modify>" +           
            "</input>" +
        "</nds>";

    private static final String modifyDefaultLoginResponse = modifyResponseSuccessRename;

    public void testModifyDefaultLogin() throws SAXException, IOException, TransformerException, AceToolkitException {
        XmlDocument request = new XmlDocument(modifyRequestDefaultLogin);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testTempAddStart:", response);
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyDefaultLoginResponse), response);
        
        Map<String, Object> userInfo = api.listUserInfo("-testLogin");
        System.out.println(userInfo.get(AceApi.ATTR_DEFAULT_LOGIN));
        assertEquals("testLogin", userInfo.get(AceApi.ATTR_DEFAULT_LOGIN));
        api.deleteUser("-testLogin");
    }
    
//  ====================================================================================================================================

    private static final String modifyRequestAddMemberOf =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
	        "<source>" + 
	            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
	            "<contact>TriVir</contact>" +
	        "</source>" +
	        "<input>" + 
	            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\" + user.substring(1) + "\">" +
	                "<association>" + user.substring(1) + "</association>" +
	                "<modify-attr attr-name=\"MemberOf\">" +
	                    "<add-value>" +
	                        "<value timestamp=\"1181832286#1\" type=\"string\">" + groupName + "</value>" +
	                    "</add-value>" +
	                "</modify-attr>" +
	            "</modify>" +           
	        "</input>" +
	    "</nds>";

    private static final String modifyResponseAddMemberOf = modifyResponseSuccess;

    public void testUserModifyMemberOf() throws TransformerException, SAXException, IOException, AceToolkitException {
    	XmlDocument request = new XmlDocument(modifyRequestAddMemberOf);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyMemberOf:", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseAddMemberOf), response);
        
        List<String> groupMembership = api.listGroupMembership(user);
        assertTrue(groupMembership.contains(groupName));
        assertEquals(1, groupMembership.size());
    }

//  ====================================================================================================================================

    private static final String modifyRequestAddMemberOfMultiple =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
	        "<source>" + 
	            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
	            "<contact>TriVir</contact>" +
	        "</source>" +
	        "<input>" + 
	            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\" + user.substring(1) + "\">" +
	                "<association>" + user.substring(1) + "</association>" +
	                "<modify-attr attr-name=\"MemberOf\">" +
	                    "<add-value>" +
                        	"<value timestamp=\"1181832286#1\" type=\"string\">" + groupName + "</value>" +
                        	"<value timestamp=\"1181832286#1\" type=\"string\">" + groupName1 + "</value>" +
                        	"<value timestamp=\"1181832286#1\" type=\"string\">" + groupName2 + "</value>" +
	                    "</add-value>" +	                    
	                "</modify-attr>" +
	            "</modify>" +           
	        "</input>" +
	    "</nds>";

    private static final String modifyResponseAddMemberOfMulitple = modifyResponseSuccess;

    public void testUserModifyMemberOfMultiple() throws TransformerException, SAXException, IOException, AceToolkitException {
    	XmlDocument request = new XmlDocument(modifyRequestAddMemberOfMultiple);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyMemberOfMultiple:", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseAddMemberOfMulitple), response);
        
        List<String> groupMembership = api.listGroupMembership(user);
        assertTrue(groupMembership.contains(groupName));
        assertTrue(groupMembership.contains(groupName1));
        assertTrue(groupMembership.contains(groupName2));
        assertEquals(3,groupMembership.size());
    }

//  ====================================================================================================================================

    private static final String modifyRequestMemberOfRemoveOne =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
	        "<source>" + 
	            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
	            "<contact>TriVir</contact>" +
	        "</source>" +
	        "<input>" + 
	            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\" + user.substring(1) + "\">" +
	                "<association>" + user.substring(1) + "</association>" +
	                "<modify-attr attr-name=\"MemberOf\">" +
	                    "<remove-value>" +
                        	"<value timestamp=\"1181832286#1\" type=\"string\">" + groupName1 + "</value>" +
                        "</remove-value>" + 
	                "</modify-attr>" +
	            "</modify>" +           
	        "</input>" +
	    "</nds>";

    private static final String modifyResponseMemberOfRemoveOne = modifyResponseSuccess;

    public void testUserModifyMemberOfRemoveOne() throws TransformerException, SAXException, IOException, AceToolkitException {
    	api.addLoginToGroup("", groupName, "", user);
    	api.addLoginToGroup("", groupName1, "", user);
    	api.addLoginToGroup("", groupName2, "", user);
    	
    	XmlDocument request = new XmlDocument(modifyRequestMemberOfRemoveOne);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyMemberOfRemoveOne:", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseMemberOfRemoveOne), response);
        
        List<String> groupMembership = api.listGroupMembership(user);
        assertTrue(groupMembership.contains(groupName));
        assertTrue(groupMembership.contains(groupName2));
        assertEquals(2, groupMembership.size());
    }

    //  ====================================================================================================================================

    private static final String modifyRequestMemberOfReplaceOne =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
	        "<source>" + 
	            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
	            "<contact>TriVir</contact>" +
	        "</source>" +
	        "<input>" + 
	            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\" + user.substring(1) + "\">" +
	                "<association>" + user.substring(1) + "</association>" +
	                "<modify-attr attr-name=\"MemberOf\">" +
	                    "<remove-value>" +
                        	"<value timestamp=\"1181832286#1\" type=\"string\">" + groupName + "</value>" +
                        "</remove-value>" + 
                        "<add-value>" +                        		
                        	"<value timestamp=\"1181832286#1\" type=\"string\">" + groupName1 + "</value>" +
	                    "</add-value>" +	                    
	                "</modify-attr>" +
	            "</modify>" +           
	        "</input>" +
	    "</nds>";

    private static final String modifyResponseMemberOfReplaceOne = modifyResponseSuccess;

    public void testUserModifyMemberOfReplaceOne() throws TransformerException, SAXException, IOException, AceToolkitException {
    	api.addLoginToGroup("", groupName, "", user);
    	api.addLoginToGroup("", groupName2, "", user);
    	
    	XmlDocument request = new XmlDocument(modifyRequestMemberOfReplaceOne);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyMemberOfReplaceOne:", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseMemberOfReplaceOne), response);
  
        List<String> groupMembership = api.listGroupMembership(user);
        assertTrue(groupMembership.contains(groupName1));
        assertTrue(groupMembership.contains(groupName2));
        assertEquals(2,groupMembership.size());
    }
    
    //  ====================================================================================================================================

    private static final String modifyRequestMemberOfReplaceAll =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
	        "<source>" + 
	            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
	            "<contact>TriVir</contact>" +
	        "</source>" +
	        "<input>" + 
	            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\" + user.substring(1) + "\">" +
	                "<association>" + user.substring(1) + "</association>" +
	                "<modify-attr attr-name=\"MemberOf\">" +
	                    "<remove-all-values/>" +
                        "<add-value>" +                        		
                        	"<value timestamp=\"1181832286#1\" type=\"string\">" + groupName2 + "</value>" +
	                    "</add-value>" +	                    
	                "</modify-attr>" +
	            "</modify>" +           
	        "</input>" +
	    "</nds>";

    private static final String modifyResponseMemberOfReplaceAll = modifyResponseSuccess;

    public void testUserModifyMemberOfReplaceAll() throws TransformerException, SAXException, IOException, AceToolkitException {
    	api.addLoginToGroup("", groupName, "", user);
    	api.addLoginToGroup("", groupName1, "", user);
    	
    	XmlDocument request = new XmlDocument(modifyRequestMemberOfReplaceAll);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyMemberOfReplaceAll:", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseMemberOfReplaceAll), response);
        
        List<String> groupMembership = api.listGroupMembership(user);
        assertTrue(groupMembership.contains(groupName2));
        assertEquals(1,groupMembership.size());
    }
    
    //  ====================================================================================================================================

    private static final String modifyRequestMemberOfRemoveAll =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
	        "<source>" + 
	            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
	            "<contact>TriVir</contact>" +
	        "</source>" +
	        "<input>" + 
	            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\" + user.substring(1) + "\">" +
	                "<association>" + user.substring(1) + "</association>" +
	                "<modify-attr attr-name=\"MemberOf\">" +
	                    "<remove-all-values/>" +
	                "</modify-attr>" +
	            "</modify>" +           
	        "</input>" +
	    "</nds>";

    public void testUserModifyMemberOfRemoveAll() throws TransformerException, SAXException, IOException, AceToolkitException {
    	api.addLoginToGroup("", groupName, "", user);
    	api.addLoginToGroup("", groupName1, "", user);
    	api.addLoginToGroup("", groupName2, "", user);
    	
    	XmlDocument request = new XmlDocument(modifyRequestMemberOfRemoveAll);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyMemberOfRemoveAll:", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseSuccess), response);

        List<String> groupMembership = api.listGroupMembership(user);
        assertEquals(0, groupMembership.size());
    }

    //  ====================================================================================================================================

    private static final String modifyRequestMemberOfErrorAdding =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
	        "<source>" + 
	            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
	            "<contact>TriVir</contact>" +
	        "</source>" +
	        "<input>" + 
	            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\" + user.substring(1) + "\">" +
	                "<association>" + user.substring(1) + "</association>" +
	                "<modify-attr attr-name=\"MemberOf\">" +
                        "<add-value>" +                        		
                        	"<value timestamp=\"1181832286#1\" type=\"string\">" + groupName1 + "</value>" +
	                    "</add-value>" +	                    
	                "</modify-attr>" +
	            "</modify>" +           
	        "</input>" +
	    "</nds>";

    public void testUserModifyMemberOfAddingExisting() throws TransformerException, SAXException, IOException, AceToolkitException {
    	api.addLoginToGroup("", groupName, "", user);
    	api.addLoginToGroup("", groupName1, "", user);
    	api.addLoginToGroup("", groupName2, "", user);
    	
    	XmlDocument request = new XmlDocument(modifyRequestMemberOfErrorAdding);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyMemberOfErrorAdding:", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseSuccess), response);
        
        List<String> groupMembership = api.listGroupMembership(user);
        // see user is still a member of all three groups:
        assertTrue(groupMembership.contains(groupName));
        assertTrue(groupMembership.contains(groupName1));
        assertTrue(groupMembership.contains(groupName2));
        assertEquals(3, groupMembership.size());
    }
 
    //  ====================================================================================================================================

    private static final String modifyRequestMemberOfErrorRemoving =
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
	        "<source>" + 
	            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
	            "<contact>TriVir</contact>" +
	        "</source>" +
	        "<input>" + 
	            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\" + user.substring(1) + "\">" +
	                "<association>" + user.substring(1) + "</association>" +
	                "<modify-attr attr-name=\"MemberOf\">" +
                        "<remove-value>" +                        		
                        	"<value timestamp=\"1181832286#1\" type=\"string\">doesntexist</value>" +
	                    "</remove-value>" +	                    
	                "</modify-attr>" +
	            "</modify>" +           
	        "</input>" +
	    "</nds>";

    private static final String modifyResponseMemberOfErrorRemoving =
		"<nds dtdversion=\"2.0\">" +
			"<source>" +
				"<contact>TriVir</contact>" +
				"<product instance=\"RSA Driver\">RSA IDM Driver</product>" +
			"</source>" +
			"<output>" +
				"<status event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" level=\"error\" type=\"driver-general\">" +
					"<description>Error removing user from a group</description>" +
				"</status>" +
			"</output>" +
		"</nds>";

    public void testUserModifyMemberOfErrorRemoving() throws TransformerException, SAXException, IOException, AceToolkitException {
    	XmlDocument request = new XmlDocument(modifyRequestMemberOfErrorRemoving);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyMemberOfErrorRemoving:", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseMemberOfErrorRemoving), response);
        
        // see user is not a member of any groups   
        assertEquals(0,api.listGroupMembership(user).size());
    }
 
    //  ====================================================================================================================================

    private static final String modifyRequestMemberOfSuccessRemovingNoOp =    	
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
	        "<source>" + 
	            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
	            "<contact>TriVir</contact>" +
	        "</source>" +
	        "<input>" + 
	            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\" + user.substring(1) + "\">" +
	                "<association>" + user.substring(1) + "</association>" +
	                "<modify-attr attr-name=\"MemberOf\">" +
                        "<remove-value>" +                        		
                        	"<value timestamp=\"1181832286#1\" type=\"string\">" + groupName + "</value>" +
	                    "</remove-value>" +	                    
	                "</modify-attr>" +
	            "</modify>" +           
	        "</input>" +
	    "</nds>";

    private static final String modifyResponseMemberOfSuccessRemovingNoOp = modifyResponseSuccess;   	

    public void testUserModifyMemberOfRemovingNoOp() throws TransformerException, SAXException, IOException, AceToolkitException {
        api.addLoginToGroup("", groupName, "", user);

    	XmlDocument request = new XmlDocument(modifyRequestMemberOfSuccessRemovingNoOp);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyMemberOfRemovingNoOp:", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseMemberOfSuccessRemovingNoOp), response);
        
        // see user is not a member of any groups
        assertEquals(0, api.listGroupMembership(user).size());
    }
 
    //  ====================================================================================================================================
    //  PROFILE TESTS
    //  ====================================================================================================================================
    //  ====================================================================================================================================

    private static final String modifyRequestProfileAdd =    	
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
	        "<source>" + 
	            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
	            "<contact>TriVir</contact>" +
	        "</source>" +
	        "<input>" + 
	            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
	                "<association>TestUser2</association>" +
	                "<modify-attr attr-name=\"ProfileName\">" +
                        "<add-value>" +                        		
                        	"<value timestamp=\"1181832286#1\" type=\"string\">" + profileName + "</value>" +
	                    "</add-value>" +	                    
	                "</modify-attr>" +
	            "</modify>" +           
	        "</input>" +
	    "</nds>";

    public void testUserModifyProfileAdd() throws TransformerException, SAXException, IOException, AceToolkitException {
    	XmlDocument request = new XmlDocument(modifyRequestProfileAdd);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyProfileAdd:", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseSuccess), response);
        
        assertEquals(profileName, api.listUserInfo(user).get(AceApi.ATTR_PROFILE_NAME)); //.listUserInfoExt("-TestUser2", 7, (byte)'|'));
    }
   
    //  ====================================================================================================================================
    private static final String modifyRequestProfileRemove =    	
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
	        "<source>" + 
	            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
	            "<contact>TriVir</contact>" +
	        "</source>" +
	        "<input>" + 
	            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
	                "<association>TestUser2</association>" +
	                "<modify-attr attr-name=\"ProfileName\">" +
                        "<remove-value>" +                        		
                        	"<value timestamp=\"1181832286#1\" type=\"string\">" + profileName + "</value>" +
	                    "</remove-value>" +	                    
	                "</modify-attr>" +
	            "</modify>" +           
	        "</input>" +
	    "</nds>";

    public void testUserModifyProfileRemove() throws TransformerException, SAXException, IOException, AceToolkitException {
    	api.assignProfile(user, profileName);

    	XmlDocument request = new XmlDocument(modifyRequestProfileRemove);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("modifyResponseProfileRemove:", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseSuccess), response);
        
        assertEquals(null, api.listUserInfo(user).get(AceApi.ATTR_PROFILE_NAME));
    }
   
    // 	====================================================================================================================================

	private static final String modifyRequestProfileRemoveAll =    	
	    "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
	        "<source>" + 
	            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
	            "<contact>TriVir</contact>" +
	        "</source>" +
	        "<input>" + 
	            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
	                "<association>TestUser2</association>" +
	                "<modify-attr attr-name=\"ProfileName\">" +
	                    "<remove-all-values/>" +                        		
	                "</modify-attr>" +
	            "</modify>" +           
	        "</input>" +
	    "</nds>";
	
	
	public void testUserModifyProfileRemoveAll() throws TransformerException, SAXException, IOException, AceToolkitException {
		api.assignProfile(user, profileName);
	
		XmlDocument request = new XmlDocument(modifyRequestProfileRemoveAll);
	    Document response = this.subscriber.execute(request, null).getDocument();
	    TestUtil.printDocumentToScreen("testUserModifyProfileRemoveAll:", response);
	    
	    this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseSuccess), response);
	    
	    // See user is a member of profile
	    assertEquals(null, api.listUserInfo(user).get(AceApi.ATTR_PROFILE_NAME));
	}
      
    // 	====================================================================================================================================
	
    private static final String modifyRequestProfileAddAlreadyPopulated =    	
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
	        "<source>" + 
	            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
	            "<contact>TriVir</contact>" +
	        "</source>" +
	        "<input>" + 
	            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
	                "<association>TestUser2</association>" +
	                "<modify-attr attr-name=\"ProfileName\">" +
                        "<add-value>" +                        		
                        	"<value timestamp=\"1181832286#1\" type=\"string\">" + profileName + "</value>" +
	                    "</add-value>" +	                    
	                "</modify-attr>" +
	            "</modify>" +           
	        "</input>" +
	    "</nds>";

    public void testUserModifyProfileAddAlreadyPopulated() throws TransformerException, SAXException, IOException, AceToolkitException {
		api.assignProfile(user, profileName1);

    	XmlDocument request = new XmlDocument(modifyRequestProfileAddAlreadyPopulated);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyProfileAdd:", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseSuccess), response);
        
        assertEquals(profileName, api.listUserInfo(user).get(AceApi.ATTR_PROFILE_NAME));
    }
    
    //  ====================================================================================================================================

    private static final String modifyRequestProfileRemoveAdd =    	
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
	        "<source>" + 
	            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
	            "<contact>TriVir</contact>" +
	        "</source>" +
	        "<input>" + 
	            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
	                "<association>TestUser2</association>" +
	                "<modify-attr attr-name=\"ProfileName\">" +
                        "<remove-value>" +                        		
                        	"<value timestamp=\"1181832286#1\" type=\"string\">" + profileName + "</value>" +
	                    "</remove-value>" +
	                    "<add-value>" + 
	                    	"<value timestamp=\"1181832286#1\" type=\"string\">" + profileName2 + "</value>" +
	                    "</add-value>" +
	                "</modify-attr>" +
	            "</modify>" +           
	        "</input>" +
	    "</nds>";

    public void testUserModifyProfileRemoveAdd() throws TransformerException, SAXException, IOException, AceToolkitException {
    	api.assignProfile("-TestUser2", profileName);

    	XmlDocument request = new XmlDocument(modifyRequestProfileRemoveAdd);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyProfileRemoveAdd:", response);
        
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseSuccess), response);
        
        assertEquals(profileName2, api.listUserInfo(user).get(AceApi.ATTR_PROFILE_NAME));
    }
   
    // 	====================================================================================================================================

	private static final String modifyRequestProfileRemoveAllAdd =    	
	    "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
	        "<source>" + 
	            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
	            "<contact>TriVir</contact>" +
	        "</source>" +
	        "<input>" + 
	            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
	                "<association>TestUser2</association>" +
	                "<modify-attr attr-name=\"ProfileName\">" +
	                    "<remove-all-values/>" +                        		
	                    "<add-value>" + 
	                    	"<value timestamp=\"1181832286#1\" type=\"string\">" + profileName1 + "</value>" +
	                    "</add-value>" +
	                "</modify-attr>" +
	            "</modify>" +           
	        "</input>" +
	    "</nds>";
	
	public void testUserModifyProfileRemoveAllAdd() throws TransformerException, SAXException, IOException, AceToolkitException {
		api.assignProfile("-TestUser2", profileName);
	
		XmlDocument request = new XmlDocument(modifyRequestProfileRemoveAllAdd);
	    Document response = this.subscriber.execute(request, null).getDocument();
	    TestUtil.printDocumentToScreen("testUserModifyProfileRemoveAllAdd:", response);
	    
	    this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseSuccess), response);
	    
	    // See user is assigned the profile
	    assertEquals(profileName1, api.listUserInfo(user).get(AceApi.ATTR_PROFILE_NAME));
	}

    // 	====================================================================================================================================

	private static final String modifyRequestProfileRemoveNonExistant =    	
	    "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
	        "<source>" + 
	            "<product version=\"3.5.1.20070411 \">DirXML</product>" +
	            "<contact>TriVir</contact>" +
	        "</source>" +
	        "<input>" + 
	            "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\TestUser2\">" +
	                "<association>TestUser2</association>" +
	                "<modify-attr attr-name=\"ProfileName\">" +
	                    "<remove-value>" + 
	                    	"<value timestamp=\"1181832286#1\" type=\"string\">" + profileName1 + "</value>" +
	                    "</remove-value>" +
	                "</modify-attr>" +
	            "</modify>" +           
	        "</input>" +
	    "</nds>";
	
	public void testUserModifyProfileRemoveNonExistant() throws TransformerException, SAXException, IOException, AceToolkitException {
		XmlDocument request = new XmlDocument(modifyRequestProfileRemoveNonExistant);
	    Document response = this.subscriber.execute(request, null).getDocument();
	    TestUtil.printDocumentToScreen("testUserModifyProfileRemoveNonExistant:", response);
	    
	    this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseSuccess), response);
	    
	    // See user is assigned the profile
	    assertEquals(null, api.listUserInfo(user).get(AceApi.ATTR_PROFILE_NAME));
	}

    //  ====================================================================================================================================
    
// Replace token fails because our tokens are currently expired.
//    private static final String modifyRequestReplaceTokenRemoveAdd =     
//        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
//            "<source>" + 
//                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
//                "<contact>TriVir</contact>" +
//            "</source>" +
//            "<input>" + 
//                "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\" + user.substring(1) + "\">" +
//                    "<association>" + user.substring(1) + "</association>" +
//                    "<modify-attr attr-name=\"TokenSerialNumber\">" +
//                        "<remove-value>" +                              
//                            "<value timestamp=\"1181832286#1\" type=\"string\">" + tokenSerialNum1 + "</value>" +
//                        "</remove-value>" +
//                        "<add-value>" + 
//                            "<value timestamp=\"1181832286#1\" type=\"string\">" + tokenSerialNum2 + "</value>" +
//                        "</add-value>" +
//                    "</modify-attr>" +
//                "</modify>" +           
//            "</input>" +
//        "</nds>";
//
//    private static final String modifyResponseReplaceTokenRemoveAdd = modifyResponseSuccess;     
//
//    public void testUserModifyTokenReplaceRemoveAdd() throws TransformerException, SAXException, IOException, AceToolkitException {
//        api.assignAnotherToken(user, tokenSerialNum1);
//        XmlDocument request = new XmlDocument(modifyRequestReplaceTokenRemoveAdd);
//        Document response = this.subscriber.execute(request, null).getDocument();
//        AllTests.printDocumentToScreen("testUserModifyReplaceTokenRemoveAdd:", response);
//
//        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseReplaceTokenRemoveAdd), response);
//
//        List<String> tokenSerials = api.getSerialByLogin(user, "0");
//        assertTrue(tokenSerials.contains(tokenSerialNum2));
//        assertEquals(1, tokenSerials.size());
//    }
//
       //  ====================================================================================================================================

    private static final String modifyRequestTokenAdd =     
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>TriVir</contact>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\" + user.substring(1) + "\">" +
                    "<association>" + user.substring(1) + "</association>" +
                    "<modify-attr attr-name=\"TokenSerialNumber\">" +
                        "<add-value>" + 
                            "<value timestamp=\"1181832286#1\" type=\"string\">" + tokenSerialNum1 + "</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +           
            "</input>" +
        "</nds>";

    private static final String modifyResponseTokenAdd = modifyResponseSuccess;     

    public void testUserModifyTokenAdd() throws TransformerException, SAXException, IOException, AceToolkitException {
        XmlDocument request = new XmlDocument(modifyRequestTokenAdd);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyTokenAdd:", response);

        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseTokenAdd), response);

        List<String> tokens = api.getSerialByLogin(user, "0");
        assertTrue(tokens.contains(tokenSerialNum1));
        assertEquals(1, tokens.size());
    }


    //  ====================================================================================================================================

    private static final String modifyRequestMultipleTokenAdd =     
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>TriVir</contact>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\" + user.substring(1) + "\">" +
                    "<association>" + user.substring(1) + "</association>" +
                    "<modify-attr attr-name=\"TokenSerialNumber\">" +
                        "<add-value>" + 
                            "<value timestamp=\"1181832286#1\" type=\"string\">" + tokenSerialNum1 + "</value>" +
                        "</add-value>" +
                        "<add-value>" + 
                            "<value timestamp=\"1181832286#1\" type=\"string\">" + tokenSerialNum2 + "</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +           
            "</input>" +
        "</nds>";

    private static final String modifyResponseMultipleTokenAdd = modifyResponseSuccess;     

    public void testUserModifyTokenMultipleAdd() throws TransformerException, SAXException, IOException, AceToolkitException {
        XmlDocument request = new XmlDocument(modifyRequestMultipleTokenAdd);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyMultipleTokenAdd:", response);

        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseMultipleTokenAdd), response);

        List<String> tokens = api.getSerialByLogin(user, "0");
        assertTrue(tokens.contains(tokenSerialNum1));
        assertTrue(tokens.contains(tokenSerialNum2));
        assertEquals(2, tokens.size());
    }

    //  ====================================================================================================================================

    private static final String modifyRequestTokenRemove =     
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>TriVir</contact>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\" + user.substring(1) + "\">" +
                    "<association>" + user.substring(1) + "</association>" +
                    "<modify-attr attr-name=\"TokenSerialNumber\">" +
                        "<remove-value>" +                              
                            "<value timestamp=\"1181832286#1\" type=\"string\">" + tokenSerialNum1 + "</value>" +
                        "</remove-value>" +
                    "</modify-attr>" +
                "</modify>" +           
            "</input>" +
        "</nds>";

    private static final String modifyResponseTokenRemove = modifyResponseSuccess;     

    public void testUserModifyTokenRemove() throws TransformerException, SAXException, IOException, AceToolkitException {
        api.assignAnotherToken(user, tokenSerialNum1);
        XmlDocument request = new XmlDocument(modifyRequestTokenRemove);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyTokenRemove:", response);

        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseTokenRemove), response);

        assertEquals(0, api.getSerialByLogin(user, "0").size());
    }

    //  ====================================================================================================================================

    private static final String modifyRequestTokenRemoveAll =     
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>TriVir</contact>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\">" +
                    "<association>" + user.substring(1) + "</association>" +
                    "<modify-attr attr-name=\"TokenSerialNumber\">" +
                        "<remove-all-values/>" +                              
                    "</modify-attr>" +
                "</modify>" +           
            "</input>" +
        "</nds>";

    private static final String modifyResponseTokenRemoveAll = modifyResponseSuccess;     

    public void testUserModifyTokenRemoveAll() throws TransformerException, SAXException, IOException, AceToolkitException {
        api.assignAnotherToken(user, tokenSerialNum1);
        api.assignAnotherToken(user, tokenSerialNum2);
        XmlDocument request = new XmlDocument(modifyRequestTokenRemoveAll);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyTokenRemoveAll:", response);

        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseTokenRemoveAll), response);

        assertEquals(0, api.getSerialByLogin(user, "0").size());
    }

    //  ====================================================================================================================================

    private static final String modifyRequestSetPIN =     
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>TriVir</contact>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"Token\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\">" +
                    "<association>" + tokenSerialNum1 + "</association>" +
                    "<modify-attr attr-name=\"PIN\">" +
                        "<remove-all-values/>" +                              
                        "<add-value>" + 
                            "<value timestamp=\"1181832286#1\" type=\"string\">12345</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +           
            "</input>" +
        "</nds>";

    private static final String modifyResponseSetPIN = modifyResponseSuccess;     

    public void testTokenModifySetPIN() throws TransformerException, SAXException, IOException, AceToolkitException {
        api.assignAnotherToken(user, tokenSerialNum1);
        XmlDocument request = new XmlDocument(modifyRequestSetPIN);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testTokenModifySetPIN:", response);

        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseSetPIN), response);

        assertEquals("TRUE", api.setPin("PIN", tokenSerialNum1));
    }

    //  ====================================================================================================================================

//    private static final String modifyRequestSetNewPinMode =     
//        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
//            "<source>" + 
//                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
//                "<contact>TriVir</contact>" +
//            "</source>" +
//            "<input>" + 
//                "<modify class-name=\"Token\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\">" +
//                    "<association>" + tokenSerialNum1 + "</association>" +
//                    "<modify-attr attr-name=\"NewPINMode\">" +
//                        "<remove-all-values/>" +                              
//                        "<add-value>" + 
//                            "<value timestamp=\"1181832286#1\" type=\"string\">True</value>" +
//                        "</add-value>" +
//                    "</modify-attr>" +
//                "</modify>" +           
//            "</input>" +
//        "</nds>";
//
//    private static final String modifyResponseSetNewPinMode = modifyResponseSuccess;     
//
//    public void testTokenModifySetNewPinMode() throws TransformerException, SAXException, IOException, AceToolkitException {
//        fail("Test is incomplete . . - need ability to clear newpinmode as part of setup for test.");
//    	
//        assertEquals("0", api.listTokenInfo(tokenSerialNum1).split(" *[,] *")[TOKEN_FIELD_NEWPINMODE]);
//        XmlDocument request = new XmlDocument(modifyRequestSetNewPinMode);
//        Document response = this.subscriber.execute(request, null).getDocument();
//        AllTests.printDocumentToScreen("testTokenModifySetNewPinMode:", response);
//
//        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseSetNewPinMode), response);
//       
//        assertEquals("1", api.listTokenInfo(tokenSerialNum1).split("[ *, *]")[TOKEN_FIELD_NEWPINMODE]);
//    }
    
    //  ====================================================================================================================================

    private static final String modifyRequestAddRemove = 
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" +
                "<product version=\"3.5.10.20070918 \">DirXML</product>" +
                "<contact>Novell, Inc.</contact>" +
            "</source>" +
            "<input>" +
                "<modify class-name=\"User\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\" src-dn=\"\\TRIVIR\\VAULT\\" + user.substring(1) + "\">" +
                    "<association state=\"associated\">" + user.substring(1) + "</association>" +
                    "<modify-attr attr-name=\"LastName\">" +
                        "<add-value>" +
                            "<value timestamp=\"1202336145#4\" type=\"string\">Gee</value>" +
                        "</add-value>" +
                        "<remove-value>" +
                            "<value timestamp=\"1202336050#4\" type=\"string\">User1</value>" +
                        "</remove-value>" +
                    "</modify-attr>" +
                    "<modify-attr attr-name=\"FirstName\">" +
                        "<add-value>" +
                            "<value timestamp=\"1202336145#3\" type=\"string\">Gary</value>" +
                        "</add-value>" +
                        "<remove-value>" +
                            "<value timestamp=\"1202336050#3\" type=\"string\">GaryX</value>" +
                        "</remove-value>" +
                    "</modify-attr>" +
                "</modify>" +
            "</input>" +
        "</nds>";

    private static final String modifyResponseAddRemove = modifyResponseSuccess;     

    public void testUserModifyAddRemove() throws TransformerException, SAXException, IOException, AceToolkitException {
        XmlDocument request = new XmlDocument(modifyRequestAddRemove);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testUserModifyAddRemove:", response);

        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseAddRemove), response);

        Map<String, Object> userInfo = api.listUserInfo("-TestUser2");        
        assertEquals("Gee", userInfo.get(AceApi.ATTR_LAST_NAME));
        assertEquals("Gary", userInfo.get(AceApi.ATTR_FIRST_NAME));
    }

    //  ====================================================================================================================================

    private static final String modifyRequestTokenEnable =     
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>TriVir</contact>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"Token\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\">" +
                    "<association>" + tokenSerialNum1 + "</association>" +
                    "<modify-attr attr-name=\"Disabled\">" +
                        "<remove-all-values/>" +                              
                        "<add-value>" + 
                            "<value timestamp=\"1181832286#1\" type=\"string\">false</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +           
            "</input>" +
        "</nds>";

    private static final String modifyResponseTokenEnable = modifyResponseSuccess;     

    public void testTokenModifyEnable() throws TransformerException, SAXException, IOException, AceToolkitException {
        api.assignAnotherToken(user, tokenSerialNum1);
        api.disableToken(tokenSerialNum1);
        assertEquals("TRUE", api.listTokenInfo(tokenSerialNum1).get(AceApi.ATTR_DISABLED));
        
        XmlDocument request = new XmlDocument(modifyRequestTokenEnable);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testTokenModifyEnable:", response);
               
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseTokenEnable), response);
        
        assertEquals("FALSE", api.listTokenInfo(tokenSerialNum1).get(AceApi.ATTR_DISABLED));
    }

    //  ====================================================================================================================================

    private static final String modifyRequestTokenDisable =     
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>TriVir</contact>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"Token\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\">" +
                    "<association>" + tokenSerialNum1 + "</association>" +
                    "<modify-attr attr-name=\"Disabled\">" +
                        "<remove-all-values/>" +                              
                        "<add-value>" + 
                            "<value timestamp=\"1181832286#1\" type=\"string\">true</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +           
            "</input>" +
        "</nds>";

    private static final String modifyResponseTokenDisable = modifyResponseSuccess;     

    public void testTokenModifyDisable() throws TransformerException, SAXException, IOException, AceToolkitException {
        api.assignAnotherToken(user, tokenSerialNum1);
        api.enableToken(tokenSerialNum1);
        assertEquals("FALSE", api.listTokenInfo(tokenSerialNum1).get(AceApi.ATTR_DISABLED));

        XmlDocument request = new XmlDocument(modifyRequestTokenDisable);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testTokenModifyDisable:", response);

        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseTokenDisable), response);
        assertEquals("TRUE", api.listTokenInfo(tokenSerialNum1).get(AceApi.ATTR_DISABLED));
    }

    //  ====================================================================================================================================

    private static final String modifyRequestTokenEnableByMissingValue =     
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>TriVir</contact>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"Token\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\">" +
                    "<association>" + tokenSerialNum1 + "</association>" +
                    "<modify-attr attr-name=\"Disabled\">" +
                        "<remove-all-values/>" +                              
                        "<add-value>" + 
                            "<value/>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +           
            "</input>" +
        "</nds>";

    private static final String modifyResponseTokenEnableByMissingValue = modifyResponseSuccess;     

    public void testTokenModifyEnableByMissingValue() throws TransformerException, SAXException, IOException, AceToolkitException {
        api.assignAnotherToken(user, tokenSerialNum1);
        api.disableToken(tokenSerialNum1);
        assertEquals("TRUE", api.listTokenInfo(tokenSerialNum1).get(AceApi.ATTR_DISABLED));
        
        XmlDocument request = new XmlDocument(modifyRequestTokenEnableByMissingValue);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testTokenModifyEnableByMissingValue:", response);
               
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseTokenEnableByMissingValue), response);
        assertEquals("FALSE", api.listTokenInfo(tokenSerialNum1).get(AceApi.ATTR_DISABLED));
    }
    //  ====================================================================================================================================

    private static final String modifyRequestNewPinMode =     
        "<nds dtdversion=\"3.5\" ndsversion=\"8.x\">" +
            "<source>" + 
                "<product version=\"3.5.1.20070411 \">DirXML</product>" +
                "<contact>TriVir</contact>" +
            "</source>" +
            "<input>" + 
                "<modify class-name=\"Token\" event-id=\"TRIVIR-A2A2196B-NDS#20070614144447#1#1\">" +
                    "<association>" + tokenSerialNum1 + "</association>" +
                    "<modify-attr attr-name=\"NewPINMode\">" +
                        "<remove-all-values/>" +                              
                        "<add-value>" + 
                            "<value>TRUE</value>" +
                        "</add-value>" +
                    "</modify-attr>" +
                "</modify>" +           
            "</input>" +
        "</nds>";

    private static final String modifyResponseTokenNewPinMode = modifyResponseSuccess;     

    public void testTokenModifyNewPinMode() throws TransformerException, SAXException, IOException, AceToolkitException {
        api.assignAnotherToken(user, tokenSerialNum1);
        api.setPin("1234", tokenSerialNum1); // set pin to clear new pin mode
        
        // Confirm we're not in NewPin mode:
        assertEquals("FALSE", api.listTokenInfo(tokenSerialNum1).get(AceApi.ATTR_NEW_PIN_MODE));
                       
        XmlDocument request = new XmlDocument(modifyRequestNewPinMode);
        Document response = this.subscriber.execute(request, null).getDocument();
        TestUtil.printDocumentToScreen("testTokenModifyNewPinMode:", response);
               
        this.assertXMLEqual(XMLUnit.buildControlDocument(modifyResponseTokenNewPinMode), response);
        
        assertEquals("TRUE", api.listTokenInfo(tokenSerialNum1).get(AceApi.ATTR_NEW_PIN_MODE));
    }

}
