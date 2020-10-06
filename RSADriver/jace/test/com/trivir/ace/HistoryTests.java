package com.trivir.ace;

import junit.framework.TestCase;

public class HistoryTests extends TestCase {
//    private AceToolkit atk;

    private static final String tokenSerialNum = "000044619408";
    int EVENT_NAME = 0;
    int EVENT_LOCAL_DATE = 1;
    int EVENT_LOCAL_TIME = 2;
    int EVENT_USERS_LOGIN = 3;
    int EVENT_USERS_AFFECTED_USERNAME = 4;
    int EVENT_SITE = 5;
    int EVENT_AUTHENTICATION_MANAGER_NAMES = 6;
    int EVENT_FIELD8 = 7;
    int EVENT_AGENT_HOST = 8;
    int EVENT_TOKEN_SERIAL_NUMBER = 9;
    int EVENT_NUMBER = 10;
    

    protected void setUp() throws Exception {
//        atk = new AceToolkit();
    }

    protected void tearDown() throws Exception {
//        atk.destroy();
    }

//    public void testMonitorHistory() throws AceToolkitException {
//        atk.addUser("User", "Test", "tuser", "");
//        atk.addUser("User", "Test", "tuser2", "");
//        atk.addUser("User", "Test", "tuser3", "");
//        atk.addUser("User", "", "tuser4", "");
//        atk.assignAnotherToken("-tuser", tokenSerialNum);
//        atk.disableToken(tokenSerialNum);
//        atk.setUser("NewLastName", "NewFirstName", "newDefaultLogin", "newShell", "-tuser");
//        atk.deleteUser("-newDefaultLogin");
//        String event = "";
//        while (true) {
//            event = atk.monitorHistory("", "");
//            if (event.equals("Done")) {
//                break;
//            }
//            System.out.println(event);
//            String[] eventFields = event.split(" \\| ");
//
//            System.out.println("Event Name: " + eventFields[EVENT_NAME]);
////            System.out.println("Event Local Date: " + eventFields[EVENT_LOCAL_DATE]);
////            System.out.println("Event Local Time: " + eventFields[EVENT_LOCAL_TIME]);
//            if (eventFields[EVENT_USERS_LOGIN].equals("Administrator") == false) {
//                System.out.println("Event Users Login: " + eventFields[EVENT_USERS_LOGIN]);
//            }
//            if (eventFields[EVENT_USERS_AFFECTED_USERNAME].length() > 0) {
//                System.out.println("Event Users Affected Username: " + eventFields[EVENT_USERS_AFFECTED_USERNAME]);
//            }
//            if (eventFields[EVENT_SITE].length() > 0) {
//                System.out.println("Event Site: " + eventFields[EVENT_SITE]);
//            }
//            if (eventFields[EVENT_AUTHENTICATION_MANAGER_NAMES].length() > 0) {
//                System.out.println("Event Authentication Manager Names: " + eventFields[EVENT_AUTHENTICATION_MANAGER_NAMES]);
//            }
//            if (eventFields[EVENT_FIELD8].length() > 0) {
//                System.out.println("Event ?: " + eventFields[EVENT_FIELD8]);
//            }
//            if (eventFields[EVENT_AGENT_HOST].equals("ace-server/API") == false) {
//                System.out.println("Event Agent Host: " + eventFields[EVENT_AGENT_HOST]);
//            }
//            if (eventFields[EVENT_TOKEN_SERIAL_NUMBER].length() > 0) {
//                System.out.println("Event Token Serial Number: " + eventFields[EVENT_TOKEN_SERIAL_NUMBER]);
//            }
////            System.out.println("Event Number: " + eventFields[EVENT_NUMBER]);
//            
//            if (eventFields[EVENT_USERS_AFFECTED_USERNAME].length() > 0) {
//                String[] names = eventFields[EVENT_USERS_AFFECTED_USERNAME].split(" ");
//                while (true) {
//                    String defaultLogin = atk.listUsersByField(AceToolkit.FIELD_LAST, AceToolkit.FILTER_HASIN, names[names.length-1], "");
//                    if (defaultLogin.equals("Done")) {
//                        break;
//                    }
//                    System.out.println("Default Login: " + defaultLogin + " Full Name: " + eventFields[EVENT_USERS_AFFECTED_USERNAME]);
//                }
//            }
//        }
//        atk.deleteUser("-tuser2");
//        atk.deleteUser("-tuser3");
//        atk.deleteUser("-tuser4");
//    }
}
