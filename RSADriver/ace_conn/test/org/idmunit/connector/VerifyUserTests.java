package org.idmunit.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.idmunit.IdMUnitException;

import com.trivir.ace.api.v71.AceApi71;
import com.trivir.ace.AceToolkitException;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class VerifyUserTests extends TestCase {
    AceApi71 atk;
    Connector conn = new ACE();
    
    public static final String startDate = "11/11/2011";
    public static final int startHour = 11;// 11 am in utc
    public static final String endDate = "12/12/2012";
    public static final int endHour = 12; // 12 pm in utc

    protected void setUp() throws IdMUnitException, AceToolkitException {
        atk = new AceApi71(AllTests.getDefaultRSAConnConfig());
        
        createTempUser("tuser", "user", "test", startDate, startHour, endDate, endHour);
        try {
            this.conn.setup(AllTests.getDefaultRSAConnConfig());
        } catch (IdMUnitException e) {
            this.atk.destroy();
            throw e;
        }
    }
    
    private void createTempUser(String defaultLogin, String lastName, String firstName, String startDate, int startHour, String endDate, int endHour) throws AceToolkitException {
        // ========================================================
        // Added the following to clean up user if it already exists - tear down appears to not be hit at times . .
        try {
            atk.listUserInfo("-" + defaultLogin);
            atk.deleteUser("-" + defaultLogin);
            System.out.println("Note: had to delete [" + defaultLogin + "] . .");
        } catch (AceToolkitException e) { }        
        // ========================================================
        
        String emailAddress = "hardcodedemail@email.com";
        String password = "default password";

        atk.addUser(lastName, firstName, defaultLogin, emailAddress, "", password);
        // ensure our test user is a temp user.
        atk.setTempUser(startDate, startHour, endDate, endHour, "-" + defaultLogin);
    }

    protected void tearDown() throws IdMUnitException {
        try {
            atk.deleteUser("-tuser");
        } catch (AceToolkitException e) {}
        try {
            atk.destroy();
        } catch (AceToolkitException e) {}
        conn.tearDown();
    }
    
    public void testVerifyBasicUser() throws IdMUnitException {
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "LastName", "user");
        addSingleValue(attrs, "FirstName", "test");

        conn.execute("validateObject", attrs);
    }

    public void testVerifyBadDefaultLogin() {
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "DefaultLogin", "tuser1");

        try {
            conn.execute("validateObject", attrs);
            fail("Expected failure verifying bad DefaultLogin");
        } catch (IdMUnitException e) {
            assertEquals("Error getting user info", e.getMessage());
        }
    }

    public void testVerifyBadFirstName() throws IdMUnitException {
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "FirstName", "test1");

        try {
            conn.execute("validateObject", attrs);
        } catch (AssertionFailedError e) {
            assertEquals("Validation failed: Attribute [FirstName] not equal.  Expected dest value: [test1] Actual dest value(s): [test]", e.getMessage());
            return;
        }
        fail("Expected failure verifying bad FirstName");
    }

    public void testVerifyBadLastName() throws IdMUnitException {
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "LastName", "user1");

        try {
            conn.execute("validateObject", attrs);
        } catch (AssertionFailedError e) {
            assertEquals("Validation failed: Attribute [LastName] not equal.  Expected dest value: [user1] Actual dest value(s): [user]", e.getMessage());
            return;
        }
        fail("Expected failure verifying bad LastName");
    }

    public void testVerifyBadObjectClass() {
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        
        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "LastName", "user1");
        addSingleValue(attrs, "objectClass", "objClassthatisnotsupported");

        try {
            conn.execute("validateObject", attrs);
            fail("Expected failure verifying bad LastName");
        } catch (IdMUnitException e) {
            assertEquals("objectClass 'objClassthatisnotsupported' not supported", e.getMessage());
        }
    }
    
    public void testVerifyIsTempUser() throws IdMUnitException {
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);

        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "LastName", "user");
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "TempUser", "FALSE");
     
        try {
            conn.execute("validateObject", attrs);
        } catch (AssertionFailedError e) {
            assertEquals("Validation failed: Attribute [TempUser] not equal.  Expected dest value: [FALSE] Actual dest value(s): [TRUE]", e.getMessage());
            return;
        }
        fail("Expected failure verifying isTempUser");
    }

    /**
     * Invalid test now, dateStart, and dateEnd don't seem to be a part of API.
     * 
     
    public void testVerifyTempUserStartDate() throws IdMUnitException {
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);

        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "LastName", "user");
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "TempUser", "1");
        addSingleValue(attrs, "dateStart", startDate + "wrong");
     
        try {
            conn.execute("validateObject", attrs);
        } catch (AssertionFailedError e) {
            assertEquals("Validation failed: Attribute [dateStart] not equal.  Expected dest value: [" + startDate + "wrong] Actual dest value(s): [" + startDate + "]", e.getMessage());
            return;
        }
        fail("Expected failure verifying startDate");
    }


    public void testVerifyTempUserStartHour() throws IdMUnitException {
        
        int startHourUTC = startHour + 7;        
        
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);

        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "LastName", "user");
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "TempUser", "1");
        addSingleValue(attrs, "todStart", "" + ((startHourUTC * 3600) + (3600 * 2))); // convert startHour to seconds, then add two more hours to be two hours off.
     
        try {
            conn.execute("validateObject", attrs);
        } catch (AssertionFailedError e) {
            assertEquals("Validation failed: Attribute [todStart] not equal.  Expected dest value: [" + ((startHourUTC * 3600) + (3600 * 2)) + "] Actual dest value(s): [" + startHourUTC * 3600 + "]", e.getMessage());
            return;
        }
        fail("Expected failure verifying todStart");
    }
    
    public void testVerifyTempUserEndDate() throws IdMUnitException {
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);

        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "LastName", "user");
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "TempUser", "1");
        addSingleValue(attrs, "dateEnd", endDate + "wrong");
     
        try {
            conn.execute("validateObject", attrs);
        } catch (AssertionFailedError e) {
            assertEquals("Validation failed: Attribute [dateEnd] not equal.  Expected dest value: [" + endDate + "wrong] Actual dest value(s): [" + endDate + "]", e.getMessage());
            return;
        }
        fail("Expected failure verifying todStart");
    }

    public void testVerifyTempUserEndHour() throws IdMUnitException {
        int endHourUTC = endHour + 7;        
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);

        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "LastName", "user");
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "TempUser", "1");
        addSingleValue(attrs, "todEnd", "" + Math.round((endHourUTC * 3600) + (3600 * 2))); // convert endHour to seconds, then add two more hours to be two hours off.
     
        try {
            conn.execute("validateObject", attrs);
        } catch (AssertionFailedError e) {
            assertEquals("Validation failed: Attribute [todEnd] not equal.  Expected dest value: [" + Math.round(((endHourUTC * 3600) + (3600 * 2))) + "] Actual dest value(s): [" + (endHourUTC * 3600) + "]", e.getMessage());
            return;
        }
        fail("Expected failure verifying todEnd");
    }
    */
    private static final String tokenSerialNum = AllTests.tokenSerialNum;

    public void testVerifyTokenAssignment() throws IdMUnitException, AceToolkitException {
        atk.assignAnotherToken("-tuser", tokenSerialNum);

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "TokenSerialNumber", tokenSerialNum);

        conn.execute("validateObject", attrs);
    }

    public void testVerifyBadTokenSerialNumber() throws AceToolkitException, IdMUnitException {
        atk.assignAnotherToken("-tuser", tokenSerialNum);

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "TokenSerialNumber", "123456789012");

        try {
            conn.execute("validateObject", attrs);
        } catch (AssertionFailedError e) {
            return;
        }
        fail("Expected failure verifying user with a bad token serial number");
    }

    private static final String profileName = AllTests.profileName;

    public void testVerifyProfileAssignment() throws IdMUnitException, AceToolkitException {
        atk.assignProfile("-tuser", profileName);

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "ProfileName", profileName);

        conn.execute("validateObject", attrs);
    }

    public void testVerifyBadProfileName() throws AceToolkitException, IdMUnitException {
        atk.assignProfile("-tuser", profileName);

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "ProfileName", "bar");

        try {
            conn.execute("validateObject", attrs);
        } catch (AssertionFailedError e) {
            return;
        }
        fail("Expected failure verifying user with a bad profile name");
    }

    private static final String groupName = AllTests.groupName;

    public void testVerifyGroupMembership() throws IdMUnitException, AceToolkitException {
        atk.addLoginToGroup("", groupName, "", "-tuser");

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "MemberOf", groupName);

        conn.execute("validateObject", attrs);
    }
    
    private static final String groupNameInSite = AllTests.groupName3WithNewSite;

    public void testVerifyGroupMembershipInSite() throws IdMUnitException, AceToolkitException {
        atk.addLoginToGroup("", groupNameInSite, "", "-tuser");

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "MemberOf", groupNameInSite);

        conn.execute("validateObject", attrs);
    }
    
    public void testVerifyGroupMembershipNoGroups() throws AceToolkitException, IdMUnitException {
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "MemberOf", groupName);

        try {
            conn.execute("validateObject", attrs);
        } catch (AssertionFailedError e) {
            return;
        }

        fail("Expected failure verifying user with no group memberships");
    }

    public void testVerifyGroupMembershipBadGroupName() throws AceToolkitException, IdMUnitException {
        atk.addLoginToGroup("", groupName, "", "-tuser");

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        
        addSingleValue(attrs, "objectClass", "User");
        addSingleValue(attrs, "DefaultLogin", "tuser");
        addSingleValue(attrs, "MemberOf", "foo");

        try {
            conn.execute("validateObject", attrs);
        } catch (AssertionFailedError e) {
            return;
        }
        fail("Expected failure verifying bad group name");
    }

    private static void addSingleValue(Map<String, Collection<String>> data, String name, String value) {
        List<String> values = new ArrayList<String>();
        values.add(value);
        data.put(name, values);
    }
}
