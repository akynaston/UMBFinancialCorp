package com.trivir.idm.driver.ace;




import com.trivir.ace.api.AceApi;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {
	
    public static String startYear = null;
    public static String endYear = null;
    
    static {
        try {
        	AceApi api = TestUtil.getApi();
            String revString = api.apiRev();
            // can't be called statically - shouldn't be newing up a new one either . . . .fix this: 
        	//api.destroy();            
            if (revString.startsWith("Release: 7.1")) {  //Release: 7.1, Date: Oct 26 2005 14:39:38
                startYear = "1986";
                endYear = "1986";
            } else if (revString.startsWith("Release: 6.1")) {  //Release: 6.1, Date: Oct 26 2005 14:39:38
                startYear = "1986";
                endYear = "1986";
            } else if (revString.startsWith("Release: 5.2")) { //Release: 5.2, Date: Nov  4 2003 11:28:11
                startYear = "2001";
                endYear = "2010";
            } else {
                throw new Exception("FATAL: Don't know how to handle new ace version: [" + revString + "]");                
            }
        } catch (Exception e) {
            System.out.println("Error setting up RSA API for tests.");
            e.printStackTrace();
            System.exit(-1);    
        } 
    }
    	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new TestSuite(TestChannelInit.class));
		suite.addTest(new TestSuite(TestDataModel.class));
		suite.addTest(new TestSuite(TestDriverShim.class));
		suite.addTest(new TestSuite(TestPublisher.class));
		suite.addTest(new TestSuite(TestPublisherAdd.class));
		suite.addTest(new TestSuite(TestPublisherDelete.class));
		suite.addTest(new TestSuite(TestPublisherModify.class));
		suite.addTest(new TestSuite(TestPublisherQuery.class));
		suite.addTest(new TestSuite(TestSubscriber.class));
        suite.addTest(new TestSuite(TestSubscriberAdd.class));
        suite.addTest(new TestSuite(TestSubscriberModify.class));
        suite.addTest(new TestSuite(TestSubscriberModifyUserTemp.class));
        suite.addTest(new TestSuite(TestSubscriberQuery.class));
	    return suite; 
	}
}
