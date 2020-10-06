package com.trivir.ace;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;

public class UserTests extends TestCase {
//    private AceToolkit atk;

    private static final String tokenSerialNum = "000044619408"; // Currently, the only available token in system. assigned to frankh
    public static final String startDate = "11/11/2011";
    public static final int startHour = 11;// 11 am in utc
    public static final String endDate = "12/12/2012";
    public static final int endHour = 12; // 12 pm in utc
    
    private String startYear;
    private String endYear;

//    protected void setUp() throws Exception {
//        atk = new AceToolkit();
//        // ========================================================
//        // Added the following to clean up user if it already exists - tear down appears to not be hit at times . .
//        try {
//            atk.listUserInfo("-tuser", (byte)'|');
//            atk.deleteUser("-tuser");
//            System.out.println("Note: had to delete tuser . .");
//        } catch (AceToolkitException e) { }        
//        // ========================================================
//        
//        String revString = atk.apiRev();
//        if (revString.startsWith("Release: 6.1")) {  //Release: 6.1, Date: Oct 26 2005 14:39:38
//            startYear = AllTests.START_YEAR_61; 
//            endYear = AllTests.END_YEAR_61;
//        } else if (revString.startsWith("Release: 5.2")) { //Release: 5.2, Date: Nov  4 2003 11:28:11
//            startYear = AllTests.START_YEAR_52; 
//            endYear = AllTests.END_YEAR_52;
//        } else {
//            throw new Exception("FATAL: Don't know how to handle new ace version: [" + revString + "]");                
//        }
//        
//        atk.addUser("User", "Test", "tuser", "");
//        // Confirm we start with an expected user configuration:
//        String userInfo = atk.listUserInfo("-tuser", (byte)'|');
//        int index = userInfo.indexOf("|"); 
//        assertEquals("| User | Test | tuser | 1 | 0 |  | 0 | 01/01/" + startYear + " | 0 | 01/01/" + endYear + " | 0", userInfo.substring(index));
//    }
//
//    public void testUserAdd() throws AceToolkitException {
//        atk.addUser("User1", "Test", "tuser1", "");
//        String userInfo = atk.listUserInfo("-tuser1", (byte)'|');
//        int i = userInfo.indexOf('|'); // skip the ID field
//        assertEquals("| User1 | Test | tuser1 | 1 | 0 |  | 0 | 01/01/" + startYear + " | 0 | 01/01/" + endYear + " | 0", userInfo.substring(i));
//    }
//    
//    public void testUserAddNoDefaultLogin()  throws AceToolkitException {
//		try {
//			atk.addUser("lastname", "firstname", "", "defaultshell");
//		} catch (AceToolkitException e) {
//			assertEquals("Sd_AddUser Error Invalid login/last name", e.getMessage());
//		}    	    	
//    }
//
//    public void testUserAddNoLastName()  throws AceToolkitException {
//		try {
//			atk.addUser("", "firstname", "defaultlogin", "defaultshell");
//		} catch (AceToolkitException e) {
//			assertEquals("Sd_AddUser Error Invalid login/last name", e.getMessage());
//		}    	    	
//    }
//
//    public void testUserAddNoFirstName()  throws AceToolkitException {    	
//    	atk.addUser("lastname", "", "defaultlogin", "defaultshell");
//		String userInfo = atk.listUserInfo("-defaultlogin", (byte)'|');		
//		assertEquals("defaultlogin", userInfo.split("[|]")[3].trim());
//		assertEquals("", userInfo.split("[|]")[2].trim());
//    }
//
//    public void testUserAddNoShellName()  throws AceToolkitException {    	
//    	atk.addUser("lastname", "firstname", "defaultlogin", "");
//		String userInfo = atk.listUserInfo("-defaultlogin", (byte)'|');		
//		assertEquals("defaultlogin", userInfo.split("[|]")[3].trim());
//		assertEquals("", userInfo.split("[|]")[6].trim());
//    }
//    
//    public void testUserAddTemp()  throws AceToolkitException {
//        atk.setTempUser(startDate, startHour, endDate, endHour, "-tuser");
//        String tempUserInfo = atk.listUserInfo("-tuser", (byte)'|');
//        int i = tempUserInfo.indexOf('|'); // skip the ID field
//        assertEquals("| User | Test | tuser | 1 | 0 |  | 1 | 11/11/2011 | 64800 | 12/12/2012 | 68400", tempUserInfo.substring(i));
//    }
//
//    public void testUserAddTempNoStartDate() throws AceToolkitException {
//        String emptyStartDate = "";
//        int emptyStartHour = 0;
//        atk.setTempUser(emptyStartDate, emptyStartHour, endDate, endHour, "-tuser");
//        
//        // get the current UTC time:        
//        Calendar calCreationTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
//        // pull out the current day:
//        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
//        String startDateToday = formatter.format(calCreationTime.getTime());
//        // get the current hour:
//        int startHourExpected = calCreationTime.get(Calendar.HOUR_OF_DAY);
//
//        // test by looking only at the temp values - we can't do a assertEquals since our start seconds may not be on entirely.
//        String[] values = atk.listUserInfo("-tuser", (byte)'|').split(" *[|] *");
//        
//        assertEquals("1", values[7]); // is temp user
//        assertEquals(startDateToday, values[8]); // dateStart
//        assertEquals(startHourExpected, Integer.parseInt(values[9]) / 3600); // todStart
//        assertEquals(endDate, values[10]); // dateStart
//        assertEquals(endHour + 7, Integer.parseInt(values[11]) / 3600); // todStart
//    }
//
//    public void testVerifyTempUserStartDateNoEndDate() throws AceToolkitException {
//        String emptyEndDate = "";
//        int emptyEndHour = 0;
//        try {
//            atk.setTempUser(startDate, startHour, emptyEndDate, emptyEndHour, "-tuser");
//            fail("Test should have thrown an invalid end date error!");
//        } catch (AceToolkitException e) {
//            assertEquals("Sd_SetTempUser Error Invalid end date", e.getMessage());
//        }        
//    }    
//    
//    public void testVerifyTempUserStartDateInvalidEndDate() throws AceToolkitException {
//        String invalidEndDate = "11/11/1911";
//        int invalidEndHour = 12;
//        try {
//            atk.setTempUser(startDate, startHour, invalidEndDate, invalidEndHour, "-tuser");
//            fail("Test should have thrown an invalid end date error!");
//        } catch (AceToolkitException e) {
//            assertEquals("Sd_SetTempUser Error Invalid end date", e.getMessage());
//        }        
//    }  
//    
//    public void testUserDelete() throws AceToolkitException {
//        atk.deleteUser("-tuser");
//        try {
//            atk.listUserInfo("-tuser", (byte)'|');
//            fail("User not deleted.");
//        } catch (AceToolkitException e) {
//            if (e.getError() != AceToolkitException.API_ERROR_INVUSR) {
//                throw e;
//            }
//        }
//    }
//    
//    public void testVerifyUnSetTempUser() throws AceToolkitException {
//        atk.setTempUser(startDate, startHour, endDate, endHour, "-tuser");
//        // confirm user is a temp:
//        assertEquals("1", atk.listUserInfo("-tuser", (byte)'|').split(" *[|] *")[7].trim());
//        // TEST:
//        atk.setTempUser("", 0, "", 0, "-tuser");
//        // confirm user is no longer a temp:
//        assertEquals("0", atk.listUserInfo("-tuser", (byte)'|').split(" *[|] *")[7].trim());
//    }
//
//    public void testListUserInfo() throws AceToolkitException {
//        String userInfo = atk.listUserInfo("-tuser", (byte)'|');
//        int i = userInfo.indexOf('|'); // skip the ID field
//        assertEquals("| User | Test | tuser | 1 | 0 |  | 0 | 01/01/" + startYear + " | 0 | 01/01/" + endYear + " | 0", userInfo.substring(i));
//    }
//
//    public void testListUserInfoInvalidUserDefaultLogin() throws AceToolkitException {
//        try {
//            atk.listUserInfo("-tuserdoesntexist", (byte) '|');
//        } catch (Exception e) {
//            // Ensure 'invalid user' is returned:
//            assertEquals("" + AceToolkitException.API_ERROR_INVUSR, e.getMessage());
//        }        
//    }
//
//    public void testListUserInfoInvalidUserToken() throws AceToolkitException {
//        try {
//            // query without the prefixed '-' to represent
//            atk.listUserInfo("002353invalidtoken", (byte) '|');
//        } catch (Exception e) {
//            // Ensure 'invalid token' is returned:
//            assertEquals("" + AceToolkitException.API_ERROR_INVTKN, e.getMessage());
//        }        
//    }
// 
//    public void testSetPin() throws AceToolkitException {
//        String pinToUse = "2352345";
//
//        // TODO: there's got to be a more elegant way to setup/tear down individual tests - this test is not even using
//        //  the standard setup and tear down except for initing and clearing the atk object.
//        try {
//            // delete user created by assigntoken - swallow exception if thrown.
//            atk.deleteUser("-testusersetpin");
//        } catch (AceToolkitException e) {  }
//        
//        // First confirm pin modification attemps cause exceptions when not assigned:
//        try {
//            atk.setPin("PIN", tokenSerialNum); // do PIN check
//            fail("SetPin should have thrown exception, token is not assigned!");            
//        } catch (AceToolkitException e) {
//          assertEquals("Sd_SetPin Error Token is not assigned", e.getMessage());
//        }   
//        
//        try {
//            atk.setPin("1234", tokenSerialNum);
//            fail("SetPin should have thrown exception, token is not assigned!");            
//        } catch (AceToolkitException e) {
//          assertEquals("Sd_SetPin Error Token is not assigned", e.getMessage());
//        }   
//        
//        try {
//            atk.setPin("", tokenSerialNum);
//            fail("SetPin should have thrown exception, token is not assigned!");            
//        } catch (AceToolkitException e) {
//          assertEquals("Sd_SetPin Error Token is not assigned", e.getMessage());
//        }   
//        
//        // assign the token to the user so we can modify the pin:
//        atk.assignToken("lastname", "firstname", "testusersetpin", "", tokenSerialNum);
//
//        // First, confirm pin is not set:
//        assertEquals("False", atk.setPin("PIN", tokenSerialNum));
//        assertEquals(pinToUse, atk.setPin(pinToUse, tokenSerialNum));
//        assertEquals("True", atk.setPin("PIN", tokenSerialNum));
//        
//        // clear pin, then confirm it is not set:
//        atk.setPin("", tokenSerialNum);
//        assertEquals("False", atk.setPin("PIN", tokenSerialNum));
//        
//        // Assing random pin:
//        atk.setPin("-1", tokenSerialNum);
//        assertEquals("True", atk.setPin("PIN", tokenSerialNum));
//        
//        // clear pin, then confirm it is not set:
//        atk.setPin("", tokenSerialNum);
//        assertEquals("False", atk.setPin("PIN", tokenSerialNum));
//                
//        // Assing explicit pin:
//        atk.setPin("9876", tokenSerialNum);
//        assertEquals("True", atk.setPin("PIN", tokenSerialNum));
//        
//        // clear pin, then confirm it is not set:
//        atk.setPin("", tokenSerialNum);
//        assertEquals("False", atk.setPin("PIN", tokenSerialNum));
//
//        // delete user created by assigntoken
//        atk.deleteUser("-testusersetpin");
//
//    }
//
//    private static final String groupName = "VPN - Remotely Anywhere Applications"; // MemberOf - See user is member of group after assignment 
//
//    public void testSetGroup() throws AceToolkitException {
//        // Confirm that we begin with no group memberships.
//        assertEquals("Done", atk.listGroupMembership("-tuser", ""));
//        atk.addLoginToGroup("", groupName, "", "-tuser");
//        // Confirm we have the proper group assigned:
//        assertEquals("tuser ,  , VPN - Remotely Anywhere Applications , ",atk.listGroupMembership("-tuser", ""));
//        // Confirm that we have no other groups:
//        assertEquals("Done", atk.listGroupMembership("-tuser", ""));
//            
//    }
//    
//    public void testSetGroupNotFound() throws AceToolkitException {
//        assertEquals("Done", atk.listGroupMembership("-tuser", ""));
//		try {
//			atk.addLoginToGroup("", "doesntexist", "", "-tuser");
//		} catch (Exception e) {
//			assertEquals("Sd_AddLoginToGroup Error Invalid group", e.getMessage());
//		}        
//            
//    }
//
//    private static final String profileName = "VPN - Remotely Anywhere Applications"; // ProfileName - test assignment - succeeds/fails
//
//    public void testSetProfile() throws AceToolkitException {
//        // see we have no profiles set:
//        assertEquals("", atk.listUserInfoExt("-tuser", 7, (byte)'|'));
//        // assign a profile:
//        atk.assignProfile("-tuser", profileName);
//        // see we know have the proper profile:
//        assertEquals(profileName, atk.listUserInfoExt("-tuser", 7, (byte)'|'));
//    }
//    
//    public void testSetProfileNotFound() throws AceToolkitException {
//        assertEquals("", atk.listUserInfoExt("-tuser", 7, (byte)'|'));
//		try {
//			atk.assignProfile("-tuser", "FakeNonexistantprofile");
//			fail("Test should throw exception . .");
//		} catch (Exception e) {
//			assertEquals("Sd_AssignProfile Error Profile not found", e.getMessage());
//		}        
//    }
//    
//    public void testListUsersByFieldMatching() throws AceToolkitException {    	
//    	assertEquals("tuser", atk.listUsersByField(AceToolkit.FIELD_LOGIN, AceToolkit.FILTER_EQUALS, "tuser", ""));
//    	assertEquals("Done", atk.listUsersByField(AceToolkit.FIELD_LOGIN, AceToolkit.FILTER_EQUALS, "tuser", ""));
//    }        
//
//    public void testListUsersByFieldStartsWith() throws AceToolkitException {
//    	// query passed tus*
//    	assertEquals("tuser", atk.listUsersByField(AceToolkit.FIELD_LOGIN, AceToolkit.FILTER_STARTS, "tus", ""));
//    	assertEquals("Done", atk.listUsersByField(AceToolkit.FIELD_LOGIN, AceToolkit.FILTER_STARTS, "tus", ""));
//    }
//
//    public void testListUsersByFieldContains() throws AceToolkitException {
//    	// query passed *us*
//    	assertEquals("tuser", atk.listUsersByField(AceToolkit.FIELD_LOGIN, AceToolkit.FILTER_HASIN, "us", ""));
//    	assertEquals("Done", atk.listUsersByField(AceToolkit.FIELD_LOGIN, AceToolkit.FILTER_HASIN, "us", ""));
//    }
//    
//    public void testSetUserFirstName() throws AceToolkitException {
//    	String[] values  = atk.listUserInfo("-tuser", (byte)'|').split(" *[|] *");    	
//        atk.setUser(values[1], "Aaron", values[3], values[6], "-" + values[3]);        
//        assertEquals(values[0] + " | User | Aaron | tuser | 1 | 0 |  | 0 | 01/01/" + startYear + " | 0 | 01/01/" + endYear + " | 0", atk.listUserInfo("-tuser", (byte)'|'));
//    }
//    
//    public void testSetUserLastName() throws AceToolkitException {
//    	String[] values  = atk.listUserInfo("-tuser", (byte)'|').split(" *[|] *");    	
//        atk.setUser("Kynaston", values[2], values[3], values[6], "-" + values[3]);        
//        assertEquals(values[0] + " | Kynaston | Test | tuser | 1 | 0 |  | 0 | 01/01/" + startYear + " | 0 | 01/01/" + endYear + " | 0", atk.listUserInfo("-tuser", (byte)'|'));
//    }
//    
//    public void testSetUserDefaultShellName() throws AceToolkitException {
//    	String[] values  = atk.listUserInfo("-tuser", (byte)'|').split(" *[|] *");    	
//        atk.setUser(values[1], values[2], values[3], "newdefaultshell", "-" + values[3]);        
//        assertEquals(values[0] + " | User | Test | tuser | 1 | 0 | newdefaultshell | 0 | 01/01/" + startYear + " | 0 | 01/01/" + endYear + " | 0", atk.listUserInfo("-tuser", (byte)'|'));
//    }
//
//    public void testSetUserDefaultLogin() throws AceToolkitException {
//    	String[] values  = atk.listUserInfo("-tuser", (byte)'|').split(" *[|] *");    	
//        atk.setUser(values[1], values[2], "tuserNEW", values[6], "-" + values[3]);        
//        assertEquals(values[0] + " | User | Test | tuserNEW | 1 | 0 |  | 0 | 01/01/" + startYear + " | 0 | 01/01/" + endYear + " | 0", atk.listUserInfo("-tuserNEW", (byte)'|'));
//    }
//    
//    private static final String groupNameInSite = "Test Group3@TestSite"; // MemberOf - See user is member of group after assignment 
//
//    public void testDeleteUserFromGroupInSite() throws AceToolkitException {
//        atk.addLoginToGroup("", groupNameInSite, "", "-tuser");
//        // seems to be a bug with deleting a user that has been a member of a group in a site:
//        //		remove user from group now, then attempt deletion of user.
//        atk.delLoginFromGroup("tuser", groupNameInSite);
//        atk.deleteUser("-tuser");
//    }
//
//    protected void tearDown() throws Exception {
//    	deleteUserIgnoreDoesNotExist("tuser");
//    	deleteUserIgnoreDoesNotExist("defaultLogin");
//    	deleteUserIgnoreDoesNotExist("testusersetpin");
//    	deleteUserIgnoreDoesNotExist("tuser1");
//    	deleteUserIgnoreDoesNotExist("tuserNew");
//        atk.destroy();
//    }    
//    
//    private void deleteUserIgnoreDoesNotExist(String defaultLogin) throws AceToolkitException {
//    	try {
//    		atk.deleteUser("-" + defaultLogin);
//    	} catch(AceToolkitException e) {
//    		if (!"Sd_DeleteUser Error Invalid user".equalsIgnoreCase(e.getMessage())) {
//    			throw e;
//    		}
//    	}
//    }
}
