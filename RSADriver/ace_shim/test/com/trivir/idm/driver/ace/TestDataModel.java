package com.trivir.idm.driver.ace;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import com.novell.nds.dirxml.driver.Trace;
import com.trivir.ace.AceToolkitException;
import com.trivir.ace.api.AceApiEvent;
import com.trivir.ace.api.AceEventException;
import com.trivir.ace.api.v71.AceApi71;
import com.trivir.idm.driver.ace.DataModel.ChangeListener;

@SuppressWarnings("unchecked")
public class TestDataModel extends TestCase {
    @SuppressWarnings("unused")
	private static final String profileName1 = "TestProfile1";
    private static final String tokenSerialNum1 = TestUtil.tokenSerialNum;
    private static final String cacheName = "testing";
    private String groupName1 = "TestGroup1";
    @SuppressWarnings("unused")
	private static final String[] allTokenAttrs = {
    	"SerialNum", 
    	"PINClear",
    	"NumDigits",
    	"Interval",
    	"Birth",
    	"Death",
    	"LastLogin",
    	"Type",
    	"Hex",
    	"NewPINMode",
    	"UserNum",
    	"NextCodeStatus",
    	"BadTokenCodes",
    	"BadPINs",
    	"PINChangedDate",
    	"DisabledDate",
    	"CountsLastModified",
    	"Protected",
    	"Deployment",
    	"Deployed",
    	"Count",
    	"SoftPassword",
    	"PIN",
    	"Disabled",
    	"Assigned",
    	"SeedSize",
    	"Keypad",
    	"LocalPIN",
    	"Version",
    	"FormFactor",
    	"PINType",
    	"Assignment",
    	"FirstLogin",
    	"LastDACode",
    	"EACExpires",
    	"EACPasscode",
    	"EmergencyAccess"
    };

    private AceApi71 api;

    public TestDataModel() {
        Trace.registerImpl(CustomTraceInterface.class, 1);
	}

	protected void setUp() throws Exception {
        api = TestUtil.getApi();
        TestUtil.dropCacheDatabase(cacheName);
    }

    protected void tearDown() throws Exception {
    	api.destroy();
    }

    public void testAddUser() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException, DriverObjectCacheException {
		Map filter = new HashMap();
        filter.put("User", new String[] {"LastName", "FirstName"});
        try {
            TestChangeListener listener = new TestChangeListener();
            DataModel model = createAndInitModel(api, cacheName, filter, listener);
            
            listener.putUserAddEvent("tuser", new String[] {"FirstName : Test", "LastName : User"});
            api.addUser("User", "Test", "tuser", null, "", TestUtil.DEFAULT_USER_PASSWORD);
/*            try {
        		Thread.sleep(10000);
            }
            catch (InterruptedException e) {}*/
            model.generateEvents(listener);
            
            assertEquals("Events not received", 0, listener.eventsNotReceived());
        } finally {
            api.deleteUser("-tuser");
        }        
    }

    public void testModifyUserFirstAndLastName() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException, DriverObjectCacheException {
        Map filter = new HashMap();
        filter.put("User", new String[] {"LastName", "FirstName"});
        try {
            TestChangeListener listener = new TestChangeListener();
            DataModel model = createAndInitModel(api, cacheName, filter, listener);

            listener.putUserAddEvent("tuser", new String[] {"FirstName : Test", "LastName : User"});
            api.addUser("User", "Test", "tuser", null, "", TestUtil.DEFAULT_USER_PASSWORD);
            model.generateEvents(listener);

            listener.putUserModifyEvent("tuser", new String[] {"FirstName : Test", "LastName : User"}, new String[] {"FirstName : NewFirstName", "LastName : NewLastName"} );
            api.setUser("NewLastName", "NewFirstName", "tuser", null, "", "-tuser", null);
            model.generateEvents(listener);

            assertEquals("Events not received", 0, listener.eventsNotReceived());
        } finally {
            api.deleteUser("-tuser");
        }        
    }
    
    public void testModifyUserDefaultLogin() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException, DriverObjectCacheException {
        Map filter = new HashMap();
        filter.put("User", new String[] {"DefaultLogin"});
        try {
            TestChangeListener listener = new TestChangeListener();
            DataModel model = createAndInitModel(api, cacheName, filter, listener);

            listener.putUserAddEvent("tuser", new String[] {"DefaultLogin : tuser"});
            api.addUser("User", "Test", "tuser", null, "", TestUtil.DEFAULT_USER_PASSWORD);
            model.generateEvents(listener);

            listener.putUserModifyEvent("testLogin", new String[] {"DefaultLogin : tuser"}, new String[] {"DefaultLogin : testLogin"} );
            api.setUser("User", "Test", "testLogin", null, "", "-tuser", null);
            model.generateEvents(listener);

            assertEquals("Events not received", 0, listener.eventsNotReceived());
        } finally {
            api.deleteUser("-testLogin");
        }        
    }

	public void testModifyAssignToken() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException, DriverObjectCacheException {
		Map filter = new HashMap();
        filter.put("User", new String[] {"TokenSerialNumber"});
        filter.put("Token", new String[] {"DefaultLogin"});
        try {
            TestChangeListener listener = new TestChangeListener();
            DataModel model = createAndInitModel(api, cacheName, filter, listener);

            listener.putUserAddEvent("tuser", new String[] {});
            api.addUser("User", "Test", "tuser", null, "", TestUtil.DEFAULT_USER_PASSWORD);
            model.generateEvents(listener);

            listener.putUserModifyEvent("tuser", new String[] {}, new String[] {"TokenSerialNumber : [" + tokenSerialNum1 + "]"});
            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {}, new String[] {"DefaultLogin : tuser"});
            api.assignAnotherToken("-tuser", tokenSerialNum1);
            model.generateEvents(listener);

            assertEquals("Events not received", 0, listener.eventsNotReceived());
        } finally {
            api.deleteUser("-tuser");
        }        
    }

    public void testModifyUnassignToken() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException, DriverObjectCacheException {
        Map filter = new HashMap();
        filter.put("User", new String[] {"TokenSerialNumber"});
        filter.put("Token", new String[] {"DefaultLogin"});
        try {
            TestChangeListener listener = new TestChangeListener();
            DataModel model = createAndInitModel(api, cacheName, filter, listener);

            listener.putUserAddEvent("tuser", new String[] {"TokenSerialNumber : [" + tokenSerialNum1 + "]"});
            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {}, new String[] {"DefaultLogin : tuser"});
            api.addUser("User", "Test", "tuser", null, "", TestUtil.DEFAULT_USER_PASSWORD);
            api.assignAnotherToken("-tuser", tokenSerialNum1);
            model.generateEvents(listener);
            
            api.rescindToken(tokenSerialNum1, false);
            listener.putUserModifyEvent("tuser", new String[] {"TokenSerialNumber : [" + tokenSerialNum1 + "]"}, new String[] {});
            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {"DefaultLogin : tuser"}, new String[0]);
            model.generateEvents(listener);

            assertEquals("Events not received", 0, listener.eventsNotReceived());
        } finally {
            api.deleteUser("-tuser");
        }        
    }

    public void testModifyDisableEnableToken() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException, DriverObjectCacheException {
        Map filter = new HashMap();
        filter.put("User", new String[] {"TokenSerialNumber"});
        filter.put("Token", new String[] {"Disabled"});
        try {
            TestChangeListener listener = new TestChangeListener();
            DataModel model = createAndInitModel(api, cacheName, filter, listener);

            listener.putUserAddEvent("tuser", new String[] {"TokenSerialNumber : [" + tokenSerialNum1 + "]"});
            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {"Disabled : TRUE"}, new String[] {"Disabled : FALSE"});
            api.addUser("User", "Test", "tuser", null, "", TestUtil.DEFAULT_USER_PASSWORD);
            api.assignAnotherToken("-tuser", tokenSerialNum1);
            model.generateEvents(listener);

            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {"Disabled : FALSE"}, new String[] {"Disabled : TRUE"});
            api.disableToken(tokenSerialNum1);
            model.generateEvents(listener);
            
            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {"Disabled : TRUE"}, new String[] {"Disabled : FALSE"});
            api.enableToken(tokenSerialNum1);
            model.generateEvents(listener);

            assertEquals("Events not received", 0, listener.eventsNotReceived());
        } finally {
            api.deleteUser("-tuser");
        }        
    }

    public void testModifyClearPIN() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException, DriverObjectCacheException {
        Map filter = new HashMap();
        filter.put("User", new String[] {"TokenSerialNumber"});
        filter.put("Token", new String[] {"PINClear"});
        try {
            TestChangeListener listener = new TestChangeListener();
            DataModel model = createAndInitModel(api, cacheName, filter, listener);

            listener.putUserAddEvent("tuser", new String[] {"TokenSerialNumber : [" + tokenSerialNum1 + "]"});
            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {"PINClear : FALSE"}, new String[] {"PINClear : TRUE"});
            api.addUser("User", "Test", "tuser", null, "", TestUtil.DEFAULT_USER_PASSWORD);
            api.assignAnotherToken("-tuser", tokenSerialNum1);
            api.changeAuthWith(0, tokenSerialNum1); // This line is necessary for our 5.2 install because the system default is to use token codes instead of PINs. This is noop'ed in the 7.1 api for now. It can't be set using the API.
            api.setPin("1234", tokenSerialNum1);
            model.generateEvents(listener);

            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {"PINClear : TRUE"}, new String[] {"PINClear : FALSE"});
            api.setPin("", tokenSerialNum1);
            model.generateEvents(listener);
            
            assertEquals("Events not received", 0, listener.eventsNotReceived());
        } finally {
            api.deleteUser("-tuser");
        }        
    }

// TODO setPinToNTC is not supported by API 7.1. It is not used by the driver shim?    
//    public void testModifyNewPINNextTokenCode() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException {
//        Map filter = new HashMap();
//        filter.put("Token", new String[] {"PINClear", "NewPINMode"}); //, "PINChangedDate"}};
//        try {
//            api.addUser("User", "Test", "tuser", "", TestUtil.DEFAULT_USER_PASSWORD);
//            api.assignAnotherToken("-tuser", tokenSerialNum1);
//            api.changeAuthWith(0, tokenSerialNum1);
////            atk.setPin("1234", tokenSerialNum1);
//
//            TestChangeListener listener = new TestChangeListener();
//            DataModel model = new DataModel(api);
//            model.setListener(listener, filter, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
//            model.init();
//
//            api.setPinToNTC(tokenSerialNum1, "751298");
//            // The next line represents the results from running this test.
//            // However, the second line is what I thought the results should be.
//            // PINClear: 0 No PIN associated with token (old PIN, if any, cleared), 1 Token has a PIN associated with it
//            // NewPINMode: Whether the token is in New PIN mode (TRUE/FALSE)
//            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {"PINClear : 0", "NewPINMode : 1"}, new String[] {"PINClear : 1", "NewPINMode : 0"});
////            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {"PINClear : 0", "NewPINMode : FALSE"}, new String[] {"PINClear : 1", "NewPINMode : TRUE"});
//            
//            model.generateEvents();
//            
//            assertEquals("Events not received", 0, listener.eventsNotReceived());
//        } finally {
//        	api.deleteUser("-tuser");
//        }        
//    }

    public void testModifyPINSetByAdmin() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException, DriverObjectCacheException {
        Map filter = new HashMap();
        filter.put("User", new String[] {"TokenSerialNumber"});
        filter.put("Token", new String[] {"PINClear"});
        try {
            TestChangeListener listener = new TestChangeListener();
            DataModel model = createAndInitModel(api, cacheName, filter, listener);

            listener.putUserAddEvent("tuser", new String[] {"TokenSerialNumber : [" + tokenSerialNum1 + "]"});
//            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {"PINClear : TRUE"}, new String[] {"PINClear : FALSE"});
            api.addUser("User", "Test", "tuser", null, "", TestUtil.DEFAULT_USER_PASSWORD);
            api.assignAnotherToken("-tuser", tokenSerialNum1);
            model.generateEvents(listener);

            assertEquals("Events not received", 0, listener.eventsNotReceived());

            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {"PINClear : FALSE"}, new String[] {"PINClear : TRUE"});
            api.setPin("1234", tokenSerialNum1);
            model.generateEvents(listener);
            
            assertEquals("Events not received", 0, listener.eventsNotReceived());
        } finally {
        	api.deleteUser("-tuser");
        }        
    }

    public void testModifyNewPINMode() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException, DriverObjectCacheException {
        Map filter = new HashMap();
        filter.put("User", new String[] {"TokenSerialNumber"});
        filter.put("Token", new String[] {"NewPINMode"});
        try {
            TestChangeListener listener = new TestChangeListener();
            DataModel model = createAndInitModel(api, cacheName, filter, listener);

            listener.putUserAddEvent("tuser", new String[] {"TokenSerialNumber : [" + tokenSerialNum1 + "]"});
            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {"NewPINMode : TRUE"}, new String[] {"NewPINMode : FALSE"});
            api.addUser("User", "Test", "tuser", null, "", TestUtil.DEFAULT_USER_PASSWORD);
            api.assignAnotherToken("-tuser", tokenSerialNum1);
            api.changeAuthWith(0, tokenSerialNum1); // This line is necessary for our 5.2 install because the system default is to use token codes instead of PINs. This is noop'ed in the 7.1 api for now. It can't be set using the API.
            api.setPin("1234", tokenSerialNum1);
            System.out.println("====" + listener.eventsNotReceived());
            //try {Thread.sleep(10000);} catch (InterruptedException e) {}
            model.generateEvents(listener);
            System.out.println(">>>>" + listener.eventsNotReceived());
            api.newPin(tokenSerialNum1);
            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {"NewPINMode : FALSE"}, new String[] {"NewPINMode : TRUE"});
            //try {Thread.sleep(5000);} catch (InterruptedException e) {}
            model.generateEvents(listener);
            
            //try {Thread.sleep(5000);} catch (InterruptedException e) {}
            assertEquals("Events not received", 0, listener.eventsNotReceived());
        } finally {
        	api.deleteUser("-tuser");
        }        
    }

// TODO emergencyAccessOTP needs to be implemented in API 7.1. It is not used by the driver shim.
//    public void testModifyTokenLost() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException {
//        Map filter = new HashMap();
//        filter.put("Token", new String[] {"EmergencyAccess"});
//        try {
//        	api.addUser("User", "Test", "tuser", "", TestUtil.DEFAULT_USER_PASSWORD);
//        	api.assignAnotherToken("-tuser", tokenSerialNum1);
//
//            TestChangeListener listener = new TestChangeListener();
//            DataModel model = new DataModel(api);
//            model.setListener(listener, filter, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
//            model.init();
//
//            api.emergencyAccessOTP(tokenSerialNum1, 2, 6, 1, 24, "01/21/2010", 23);
//            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {"EmergencyAccess : FALSE"}, new String[] {"EmergencyAccess : TRUE"});
//            
//            model.generateEvents();
//            
//            api.emergencyAccessOff(tokenSerialNum1);
//            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {"EmergencyAccess : TRUE"}, new String[] {"EmergencyAccess : FALSE"});
//            
//            model.generateEvents();
//            
//            assertEquals("Events not received", 0, listener.eventsNotReceived());
//        } finally {
//        	api.deleteUser("-tuser");
//        }        
//    }

    public void testModifyUserShell() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException, DriverObjectCacheException {
        Map filter = new HashMap();
        filter.put("User", new String[] {"DefaultShell"});
        try {
            TestChangeListener listener = new TestChangeListener();
            DataModel model = createAndInitModel(api, cacheName, filter, listener);

            listener.putUserAddEvent("tuser", new String[] {});
            api.addUser("User", "Test", "tuser", null, "", TestUtil.DEFAULT_USER_PASSWORD);
            model.generateEvents(listener);

            listener.putUserModifyEvent("tuser", new String[0], new String[] {"DefaultShell : /bin/bash"});
            api.setUser("User", "Test", "tuser", null, "/bin/bash", "-tuser", null);
            model.generateEvents(listener);
            
            assertEquals("Events not received", 0, listener.eventsNotReceived());
		} finally {
            api.deleteUser("-tuser");
        }        
    }

    public void testModifyUserGroupMembership() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException, DriverObjectCacheException {
        Map filter = new HashMap();
        filter.put("User", new String[] {"MemberOf"});
        try {

            TestChangeListener listener = new TestChangeListener();
            DataModel model = createAndInitModel(api, cacheName, filter, listener);

            listener.putUserAddEvent("tuser", new String[0]);
            api.addUser("User", "Test", "tuser", null, "", TestUtil.DEFAULT_USER_PASSWORD);
            model.generateEvents(listener);

            api.addLoginToGroup("", groupName1 , "", "-tuser");
            listener.putUserModifyEvent("tuser", new String[0], new String[] {"MemberOf : [" + groupName1 + "]"});

            model.generateEvents(listener);
            // generateEvents is called a second time to make sure we don't
            // receive any events as a result of querying for the user's
            // information during the previous generateEvents.
            model.generateEvents(listener);

            api.delLoginFromGroup("tuser", groupName1);
            listener.putUserModifyEvent("tuser", new String[] {"MemberOf : [" + groupName1 + "]"}, new String[0]);

            model.generateEvents(listener);
            
            assertEquals("Events not received", 0, listener.eventsNotReceived());
		} finally {
            api.deleteUser("-tuser");
        }        
    }

//TODO: Need to get radius working properly with ACE 7.1 to test this.
//    public void testModifyUserAddProfileAssignment() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException {
//        Map filter = new HashMap();
//        filter.put("User", new String[] {"ProfileName"});
//        try {
//            TestChangeListener listener = new TestChangeListener();
//            DataModel model = new DataModel(api);
//            model.setListener(listener, filter, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
//            model.init();
//
//            listener.putUserAddEvent("tuser", new String[0]);
//            api.addUser("User", "Test", "tuser", "", TestUtil.DEFAULT_USER_PASSWORD);
//            model.generateEvents();
//
//            api.assignProfile("-tuser", profileName1);
//            listener.putUserModifyEvent("tuser", new String[0], new String[] {"ProfileName : " + profileName1});
//
//            model.generateEvents();
//
//            api.unassignProfile("-tuser");
//            listener.putUserModifyEvent("tuser", new String[] {"ProfileName : " + profileName1}, new String[0]);
//
//            model.generateEvents();
//
//            assertEquals("Events not received", 0, listener.eventsNotReceived());
//        } finally {
//            api.deleteUser("-tuser");
//        }        
//    }

    public void testDeleteUser() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException, DriverObjectCacheException {
        Map filter = new HashMap();
        filter.put("User", new String[] {});

        api.addUser("User", "Test", "tuser", null, "", TestUtil.DEFAULT_USER_PASSWORD);

        TestChangeListener listener = new TestChangeListener();
        DataModel model = createAndInitModel(api, cacheName, filter, listener);
        
        api.deleteUser("-tuser");
        listener.putUserDeleteEvent("tuser");
        
        model.generateEvents(listener);
        assertEquals("Events not received", 0, listener.eventsNotReceived());
    }

    public void testCachedAddUser() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException, DriverObjectCacheException {
        Map filter = new HashMap();
        filter.put("User", new String[] {"LastName", "FirstName"});
        try {
            TestChangeListener listener = new TestChangeListener();
            DataModel model = createAndInitModel(api, cacheName, filter, listener);
            model.close();

            api.addUser("User", "Test", "tuser", null, "", TestUtil.DEFAULT_USER_PASSWORD);
            api.clearHistory();

            listener.putUserAddEvent("tuser", new String[]{"FirstName : Test", "LastName : User"});

            model = createAndInitModel(api, cacheName, filter, listener);

            model.generateEvents(listener);
            model.close(); // this line will generate an error if we have a regression on token saves.

            assertEquals("Events not received", 0, listener.eventsNotReceived());
        } finally {
            api.deleteUser("-tuser");
        }        
    }
    
    public void testCachedDeleteUser() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException, DriverObjectCacheException {
    	Map filter = new HashMap();
        filter.put("User", new String[] {"LastName", "FirstName"});
        TestChangeListener listener = new TestChangeListener();
        DataModel model = createAndInitModel(api, cacheName, filter, listener);
        
        listener.putUserAddEvent("tuser", new String[] {"FirstName : Test", "LastName : User"});
        api.addUser("User", "Test", "tuser", null, "", TestUtil.DEFAULT_USER_PASSWORD);
        model.generateEvents(listener);

        assertEquals("Events not received", 0, listener.eventsNotReceived());
        
        model.close();

        api.deleteUser("-tuser");
        listener.putUserDeleteEvent("tuser");
        api.clearHistory();

        model = createAndInitModel(api, cacheName, filter, listener);

        model.generateEvents(listener);
        model.close(); // this line will generate an error if we have a regression on token saves.

        assertEquals("Events not received", 0, listener.eventsNotReceived());     
    }

    public void testSaveCacheWithTokens() throws AceToolkitException, SAXException, IOException, AceDriverException, TransformerConfigurationException, TransformerException, AceEventException, DriverObjectCacheException {
        Map filter = new HashMap();
        filter.put("Token", new String[] {"DefaultLogin"}); // added to test the 

        try {
            TestChangeListener listener = new TestChangeListener();
            DataModel model = createAndInitModel(api, cacheName, filter, listener);

            listener.putTokenModifyEvent(tokenSerialNum1, new String[] {}, new String[] {"DefaultLogin : tuser"});
            api.addUser("User", "Test", "tuser", null, "", TestUtil.DEFAULT_USER_PASSWORD);
            api.assignAnotherToken("-tuser", tokenSerialNum1);
            model.generateEvents(listener);

            assertEquals("Events not received", 0, listener.eventsNotReceived());
            model.close();
        } finally {
        	try {api.deleteUser("-tuser");} catch(Exception e) {System.out.println("Unable to delete user.");} // don't hide the thrown exception.
        }        
    }

    public void testRegisterUser() throws AceToolkitException, AceEventException {
        try {
			api.addUser("User", "Test", "tuser", null, "", TestUtil.DEFAULT_USER_PASSWORD);
			api.unregisterUser("-tuser");
			api.clearHistory();
			api.registerUser("-tuser");
			AceApiEvent event = api.monitorHistory(); // "Register principal"
			System.out.println(event.getDescription());
			api.unregisterUser("-tuser");
			event = api.monitorHistory();
		} finally {
        	try {api.deleteUser("-tuser");} catch(Exception e) {System.out.println("Unable to delete user.");} // don't hide the thrown exception.
		}
    	
    }
    
    private static DataModel createAndInitModel(AceApi71 api, String cacheName, Map filter, ChangeListener listener) throws DriverObjectCacheException, AceToolkitException, AceDriverException, AceEventException {
        DataModel model = new DataModel(api, cacheName, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        model.setFilter(filter);
        while(!model.init()) {}
        model.generateEvents(listener);
        return model;
    }

    static class TestChangeListener implements DataModel.ChangeListener {
        class Event {
            static final int ADD = 1;
            static final int MODIFY = 2;
            static final int DELETE = 3;
            int type;
            String association;
            String className;
            Map curAttrs;
            Map newAttrs;
            
            Event(int type, String association, String className, Map curAttrs, Map newAttrs) {
                this.type = type;
                this.association = association;
                this.className = className;
                this.curAttrs = curAttrs;
                this.newAttrs = newAttrs;
            }
        }

        private LinkedList events = new LinkedList();
        
        private Map parseMap(String[] s) {
            Map map = new HashMap();
            for (int i=0; i<s.length; ++i) {
                String[] pair = s[i].split(" : ");
                if (pair[1].matches("\\[.*\\]")) {
                    map.put(pair[0], Arrays.asList(pair[1].substring(1, pair[1].length()-1).split(" *[,] *")));
                } else {
                    map.put(pair[0], pair[1]);
                }
            }
            return map;
        }

        public void putTokenModifyEvent(String serialNumber, String[] curAttrs, String[] newAttrs) {
            events.add(new Event(Event.MODIFY, serialNumber, AceDriverShim.CLASS_TOKEN, parseMap(curAttrs), parseMap(newAttrs)));
        }

        void putUserAddEvent(String defaultLogin, String[] attrs) {
            events.add(new Event(Event.ADD, defaultLogin, AceDriverShim.CLASS_USER, parseMap(attrs), null));
        }

        void putUserModifyEvent(String defaultLogin, String[] curAttrs, String[] newAttrs) {
            events.add(new Event(Event.MODIFY, defaultLogin, AceDriverShim.CLASS_USER, parseMap(curAttrs), parseMap(newAttrs)));
        }

        void putUserDeleteEvent(String defaultLogin) {
            events.add(new Event(Event.DELETE, defaultLogin, AceDriverShim.CLASS_USER, null, null));
        }

        public void userAdded(String defaultLogin, Map attrs) {
        	System.out.println("Removing User Added");
            if (events.size() == 0) {
                fail("Received unexpected add event");
            }
            Event event = (Event)events.removeFirst();
            assertEquals("Event type", event.type, Event.ADD);
            assertEquals("DefaultLogin", event.association, defaultLogin);
            assertEquals("Class", event.className, AceDriverShim.CLASS_USER);
            assertEquals("Attrs", event.curAttrs, attrs);
        }

        public void userDeleted(String defaultLogin) {
        	System.out.println("Removing User Deleted");
            if (events.size() == 0) {
                fail("Received unexpected delete event");
            }
            Event event = (Event)events.removeFirst();
            assertEquals("Event type", event.type, Event.DELETE);
            assertEquals("DefaultLogin", event.association, defaultLogin);
            assertEquals("Class", event.className, AceDriverShim.CLASS_USER);
        }

        public void userModified(String defaultLogin, Map oldAttrs, Map newAttrs) {
        	System.out.println("Removing User Modified");
            if (events.size() == 0) {
                fail("Received unexpected modify event");
            }
            Event event = (Event)events.removeFirst();
            assertEquals("Event type", event.type, Event.MODIFY);
            assertEquals("DefaultLogin", event.association, defaultLogin);
            assertEquals("Class", event.className, AceDriverShim.CLASS_USER);
            assertEquals("Old Attributes", event.curAttrs, oldAttrs);
            assertEquals("New Attributes", event.newAttrs, newAttrs);
        }
        
        public void tokenModified(String serialNumber, Map oldAttrs, Map newAttrs) {
        	System.out.println("Removing Token Modified");
            if (events.size() == 0) {
                fail("Received unexpected modify event");
            }
            Event event = (Event)events.removeFirst();
            assertEquals("Event type", event.type, Event.MODIFY);
            assertEquals("DefaultLogin", event.association, serialNumber);
            assertEquals("Class", event.className, AceDriverShim.CLASS_TOKEN);
            assertEquals("Old Attributes", event.curAttrs, oldAttrs);
            assertEquals("New Attributes", event.newAttrs, newAttrs);
        }

        int eventsNotReceived() {
            return events.size();
        }
    }
}
