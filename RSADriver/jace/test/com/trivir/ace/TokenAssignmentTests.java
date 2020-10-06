package com.trivir.ace;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

public class TokenAssignmentTests extends TestCase {
//    private AceToolkit atk;
    
    
    private static final String tokenSerialNum = "000044619408";
    private static final String tokenSerialNum2 = "000044619358";
    
    private boolean isRelease61;

    protected void setUp() throws Exception {
//        atk = new AceToolkit();
//        
//        // Added the following to clean up user if it already exists - tear down appears to not be hit at times . .
//        try {
//            atk.listUserInfo("-tuser", (byte)'|');
//            atk.deleteUser("-tuser");
//            System.out.println("Note: had to delete tuser . .");
//        } catch (AceToolkitException e) { }        
//        atk.addUser("User", "Test", "tuser", "");
//        
//        String revString = atk.apiRev();
//        if (revString.startsWith("Release: 6.1")) {  //Release: 6.1, Date: Oct 26 2005 14:39:38
//        	isRelease61 = true;
//        } else if (revString.startsWith("Release: 5.2")) { //Release: 5.2, Date: Nov  4 2003 11:28:11
//        	isRelease61 = false;
//        } else {
//            throw new Exception("FATAL: Don't know how to handle new ace version: [" + revString + "]");                
//        }
//        
    }
   
//    public void testGetSerialByLogin() throws AceToolkitException {
//    	// confirm user has no tokens
//        assertEquals("Done", atk.getSerialByLogin("tuser", "0"));
//    }
//    
//
//    public void testAssignAnotherToken() throws AceToolkitException {
//        atk.assignAnotherToken("-tuser", tokenSerialNum);
//        String tokens = atk.getSerialByLogin("tuser", "0");
//        assertEquals(tokenSerialNum, tokens);
//    }
//
//    public void testAssignInvalidToken() throws AceToolkitException {
//        try {
//            atk.assignAnotherToken("-tuser", "123456789012");
//            fail("Assigned invalid token");
//        } catch (AceToolkitException e) {
//            if (e.getError() != AceToolkitException.API_ERROR_INVTKN) {
//                throw e;
//            }
//        }
//    }
//    
//    public void testNoAssignedTokens() throws AceToolkitException {
//        String tokens = atk.getSerialByLogin("tuser", "0");
//        assertEquals("Done", tokens);
//    }
//    
//    public void testlistTokenInfo() throws AceToolkitException, ParseException {
//        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
//        
//        String tokenFields = atk.listTokenInfo(tokenSerialNum);
//        String[] values = tokenFields.split("[,]");
//        
//        // TODO: tod* attributes below contain values that are the UTC number of seconds since modification - need to store this value above in other tests when these values are changed, and test them here. 
//        String todayMonthDayYear = formatter.format(new Date());
//        
//        assertEquals(tokenSerialNum, values[0].trim()); // serial number
//        assertEquals("0", values[1].trim()); // pinclear
//        assertEquals("6", values[2].trim()); // numdigits
//        assertEquals("60", values[3].trim()); // interval
//        assertEquals("08/22/2007", values[4].trim()); // datebirth
//        assertEquals("0", values[5].trim()); // todbirth
//        assertEquals("11/30/2011", values[6].trim()); //datedeath
//        assertEquals("0", values[7].trim()); // toddeath
//        // not testing last login time.
//        //assertEquals("10/23/2007", values[8].trim()); // datelastlogin
//        //assertEquals("72656", values[9].trim()); // todlastlogin
//        assertEquals("2", values[10].trim()); // type
//        assertEquals("0", values[11].trim()); // hex
//        assertEquals("1", values[12].trim()); // enabled
//        assertEquals("1", values[13].trim()); // newpinmode
//        assertEquals("0", values[14].trim()); // usernum
//        assertEquals("0", values[15].trim()); // nextcodestatus
//        assertEquals("0", values[16].trim()); // badtokencodes
//        assertEquals("0", values[17].trim()); // badpins
//        assertEquals(todayMonthDayYear, values[18].trim()); // datepin
//        //assertEquals("78982", values[19].trim()); // todpin - Can't seem to test - other tests are modifying this value . .
//        assertEquals(todayMonthDayYear, values[20].trim()); // dateenabled
//        //assertEquals("78982", values[21].trim()); // todenabled  - Can't seem to test - other tests are modifying this value . .
//        assertEquals(todayMonthDayYear, values[22].trim()); // datecoutnslastmodified
//        //assertEquals("78982", values[23].trim()); // todcountslastmodified  - Can't seem to test - other tests are modifying this value . .
//        assertEquals("0", values[24].trim()); // protected
//        assertEquals("0", values[25].trim()); // deployment
//        assertEquals("0", values[26].trim()); // deployed
//        assertEquals("0", values[27].trim()); // count
//        assertEquals("", values[28].trim()); // softpassword
//    }
//
//    public void testIsEmergencyAccess() throws AceToolkitException, ParseException {
//        atk.assignAnotherToken("-tuser", tokenSerialNum);        
//        assertEquals("False", atk.isEmergencyAccess(tokenSerialNum));
//        
//        @SuppressWarnings("unused")
//		String otp = atk.emergencyAccessOn(tokenSerialNum, 2, 5, 6);
//        assertEquals("True", atk.isEmergencyAccess(tokenSerialNum));
//    }
//
//    public void testlistTokenInfoExt() throws AceToolkitException, ParseException {
//        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
//
//        atk.assignAnotherToken("-tuser", tokenSerialNum);
//        @SuppressWarnings("unused")
//		String otp = atk.emergencyAccessOn(tokenSerialNum, 2, 5, 6);
//        Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
//
//        int outputFormat = 4;
//        if (isRelease61) {
//        	// Release 6.1: returns an extra 15 fields in 6.1.
//        	outputFormat = 4;
//        } else {
//        	// Release 5.2: only returns an extra 5 fields in 5.2.
//        	outputFormat = 1;
//        }
//        
//        String tokenFields = atk.listTokenInfoExt("asdfasdf" + tokenSerialNum, outputFormat);
//        String[] values = tokenFields.split("[,]");
//        
//        // TODO: tod* attributes below contain values that are the UTC number of seconds since modification - need to store this value above in other tests when these values are changed, and test them here. 
//        String todayMonthDayYear = formatter.format(today.getTime());
//        int seconds = today.get(Calendar.HOUR_OF_DAY) * (60*60) + today.get(Calendar.MINUTE) * 60 + today.get(Calendar.SECOND);
//        String todaySeconds = Integer.toString(seconds);
//        
//        assertEquals(tokenSerialNum, values[0].trim()); // serial number
//        assertEquals("0", values[1].trim()); // pinclear
//        assertEquals("6", values[2].trim()); // numdigits
//        assertEquals("60", values[3].trim()); // interval
//        assertEquals("08/22/2007", values[4].trim()); // datebirth
//        assertEquals("0", values[5].trim()); // todbirth
//        assertEquals("11/30/2011", values[6].trim()); //datedeath
//        assertEquals("0", values[7].trim()); // toddeath
//        // not testing last login time.
//        //assertEquals("10/23/2007", values[8].trim()); // datelastlogin
//        //assertEquals("72656", values[9].trim()); // todlastlogin
//        assertEquals("2", values[10].trim()); // type
//        assertEquals("0", values[11].trim()); // hex
//        assertEquals("1", values[12].trim()); // enabled
//        assertEquals("1", values[13].trim()); // newpinmode
//        assertTrue("usernum != 0", "0".equals(values[14].trim()) == false); // usernum
//        assertEquals("0", values[15].trim()); // nextcodestatus
//        assertEquals("0", values[16].trim()); // badtokencodes
//        assertEquals("0", values[17].trim()); // badpins
//        assertEquals(todayMonthDayYear, values[18].trim()); // datepin
//        //assertEquals("78982", values[19].trim()); // todpin - Can't seem to test - other tests are modifying this value . .
//        assertEquals(todayMonthDayYear, values[20].trim()); // dateenabled
//        //assertEquals("78982", values[21].trim()); // todenabled  - Can't seem to test - other tests are modifying this value . .
//        assertEquals(todayMonthDayYear, values[22].trim()); // datecoutnslastmodified
//        //assertEquals("78982", values[23].trim()); // todcountslastmodified  - Can't seem to test - other tests are modifying this value . .
//        assertEquals("0", values[24].trim()); // protected
//        assertEquals("0", values[25].trim()); // deployment
//        assertEquals("0", values[26].trim()); // deployed
//        assertEquals("0", values[27].trim()); // count
//        assertEquals("", values[28].trim()); // softpassword
//        assertEquals("128", values[29].trim()); // seed size
//        assertEquals("0", values[30].trim()); // bKeypad
//        assertEquals("0", values[31].trim()); // bLocalPin
//        assertEquals("0", values[32].trim()); // TokenVersion
//        assertEquals("00010000000000000000000000000001", values[33].trim()); // FormFactor
//        
//        if (isRelease61) {
//	        assertEquals("0", values[34].trim()); // PINType
//	        assertEquals(todayMonthDayYear, values[35].trim()); // TokenAssignmentDate
//	        assertEquals(todaySeconds, values[36].trim()); // TokenAssignmentTime
//	        assertEquals("0", values[37].trim()); // bFirstLogin
//	        assertEquals("UNDEFINED", values[38].trim()); // dateLastDACode
//	        assertEquals("UNDEFINED", values[39].trim()); // todLastDACode
//	        assertEquals("UNDEFINED", values[40].trim()); // dateEACExpires 
//	        assertEquals("UNDEFINED", values[41].trim()); // todEACExpires
//	        assertEquals("UNDEFINED", values[42].trim()); // EACPasscode
//        }
//    }
//    
//    public void testlistTokenInfoExtDefaultValues() throws AceToolkitException, ParseException {
//        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
//        
//        int outputFormat = 4;
//        if (isRelease61) {
//        	// Release 6.1: returns an extra 15 fields in 6.1.
//        	outputFormat = 4;
//        } else {
//        	// Release 5.2: only returns an extra 5 fields in 5.2.
//        	outputFormat = 1;
//        }
//        
//        String tokenFields = atk.listTokenInfoExt(tokenSerialNum, outputFormat);
//        String[] values = tokenFields.split("[,]");
//        
//        // TODO: tod* attributes below contain values that are the UTC number of seconds since modification - need to store this value above in other tests when these values are changed, and test them here.
//        Date curTime = new Date();
//        String todayMonthDayYear = formatter.format(curTime);
//
//        assertEquals(tokenSerialNum, values[0].trim()); // serial number
//        assertEquals("0", values[1].trim()); // pinclear
//        assertEquals("6", values[2].trim()); // numdigits
//        assertEquals("60", values[3].trim()); // interval
//        assertEquals("08/22/2007", values[4].trim()); // datebirth
//        assertEquals("0", values[5].trim()); // todbirth
//        assertEquals("11/30/2011", values[6].trim()); //datedeath
//        assertEquals("0", values[7].trim()); // toddeath
//        // not testing last login time.
//        //assertEquals("10/23/2007", values[8].trim()); // datelastlogin
//        //assertEquals("72656", values[9].trim()); // todlastlogin
//        assertEquals("2", values[10].trim()); // type
//        assertEquals("0", values[11].trim()); // hex
//        assertEquals("1", values[12].trim()); // enabled
//        assertEquals("1", values[13].trim()); // newpinmode
//        assertEquals("0", values[14].trim()); // usernum
//        assertEquals("0", values[15].trim()); // nextcodestatus
//        assertEquals("0", values[16].trim()); // badtokencodes
//        assertEquals("0", values[17].trim()); // badpins
//        assertEquals(todayMonthDayYear, values[18].trim()); // datepin
//        //assertEquals("78982", values[19].trim()); // todpin - Can't seem to test - other tests are modifying this value . .
//        assertEquals(todayMonthDayYear, values[20].trim()); // dateenabled
//        //assertEquals("78982", values[21].trim()); // todenabled  - Can't seem to test - other tests are modifying this value . .
//        assertEquals(todayMonthDayYear, values[22].trim()); // datecoutnslastmodified
//        //assertEquals("78982", values[23].trim()); // todcountslastmodified  - Can't seem to test - other tests are modifying this value . .
//        assertEquals("0", values[24].trim()); // protected
//        assertEquals("0", values[25].trim()); // deployment
//        assertEquals("0", values[26].trim()); // deployed
//        assertEquals("0", values[27].trim()); // count
//        assertEquals("", values[28].trim()); // softpassword
//        assertEquals("128", values[29].trim()); // seed size
//        assertEquals("0", values[30].trim()); // bKeypad
//        assertEquals("0", values[31].trim()); // bLocalPin
//        assertEquals("0", values[32].trim()); // TokenVersion
//        assertEquals("00010000000000000000000000000001", values[33].trim()); // FormFactor
//        if (isRelease61) {
//	        assertEquals("0", values[34].trim()); // PINType
//	        assertEquals("NONE", values[35].trim()); // TokenAssignmentDate
//	        assertEquals("UNDEFINED", values[36].trim()); // TokenAssignmentTime
//	        assertEquals("0", values[37].trim()); // bFirstLogin
//	        assertEquals("UNDEFINED", values[38].trim()); // dateLastDACode
//	        assertEquals("UNDEFINED", values[39].trim()); // todLastDACode
//	        assertEquals("UNDEFINED", values[40].trim()); // dateEACExpires 
//	        assertEquals("UNDEFINED", values[41].trim()); // todEACExpires
//	        assertEquals("UNDEFINED", values[42].trim()); // EACPasscode
//        }
//    }
//    
//    public void testListTokens() throws AceToolkitException {
//    	assertEquals(tokenSerialNum, atk.listTokens(""));
//    	assertEquals(tokenSerialNum2, atk.listTokens(""));
//    	assertEquals("Done", atk.listTokens(""));
//    }
//    
//    public void testEnableToken() throws AceToolkitException {   	
//    	atk.assignAnotherToken("-tuser", tokenSerialNum);
//    	atk.disableToken(tokenSerialNum);
//    	assertEquals("0", atk.listTokenInfo(tokenSerialNum).split(" *, *")[12]);
//    	
//    	atk.enableToken(tokenSerialNum);
//    	assertEquals("1", atk.listTokenInfo(tokenSerialNum).split(" *, *")[12]);
//    }
//
//    public void testDisableToken() throws AceToolkitException {   	
//    	atk.assignAnotherToken("-tuser", tokenSerialNum);
//    	atk.enableToken(tokenSerialNum);
//    	assertEquals("1", atk.listTokenInfo(tokenSerialNum).split(" *, *")[12]);
//    	
//    	atk.disableToken(tokenSerialNum);
//    	assertEquals("0", atk.listTokenInfo(tokenSerialNum).split(" *, *")[12]);
//    }
//    
//    protected void tearDown() throws Exception {
//        try {
//            atk.deleteUser("-tuser");
//        } catch (AceToolkitException e) {}
//        atk.destroy();
//    }
}
