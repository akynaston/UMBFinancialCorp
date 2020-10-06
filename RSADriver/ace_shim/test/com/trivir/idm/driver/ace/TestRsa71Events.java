package com.trivir.idm.driver.ace;

import junit.framework.TestCase;

import com.rsa.admin.SearchPrincipalsCommand;
import com.rsa.admin.SearchRealmsCommand;
import com.rsa.admin.data.IdentitySourceDTO;
import com.rsa.admin.data.PrincipalDTO;
import com.rsa.admin.data.RealmDTO;
import com.rsa.admin.data.SecurityDomainDTO;
import com.rsa.common.search.Filter;
import com.trivir.ace.api.AceApiEvent;
import com.trivir.ace.api.AceApiEventType;
import com.trivir.ace.api.v71.AceApi71;

public class TestRsa71Events  extends TestCase{
	AceApi71 api;
	
	public TestRsa71Events() {
    }

    protected void setUp() throws Exception {
		api = TestUtil.getApi();
    }
    
    protected void tearDown() throws Exception {
    }

    public void testMonitorEvents() throws Exception {
    	long currentTime = System.currentTimeMillis();
    	long endTime = currentTime + 60 * 000;
    	
    	while(System.currentTimeMillis() < endTime) {
    		AceApiEvent event = api.monitorHistory();
    		if(event.getType() == AceApiEventType.USER_MODIFY) {
    			
    		}
    		System.out.println(String.format("%s-%s",event.getType(),event.getDescription()));
    		Thread.sleep(1000);
    	}
    }

    public void testUserInDifferentIdentitySource() throws Exception {
    	String realmName = "SystemDomain";
    	String idSourceName = "AD";

    	// Get our Realm
        SearchRealmsCommand searchRealmsCommand = new SearchRealmsCommand();
        searchRealmsCommand.setFilter( Filter.equal( RealmDTO.NAME_ATTRIBUTE, realmName));
        searchRealmsCommand.setLimit(1);
        
        searchRealmsCommand.execute();

        RealmDTO realm = searchRealmsCommand.getRealms()[0];

        SecurityDomainDTO securityDomain = realm.getTopLevelSecurityDomain();
        System.out.println("Top Level Security Domain - " + securityDomain.getName());
        
        IdentitySourceDTO idSource = null;
        for(IdentitySourceDTO tempIdSource : realm.getIdentitySources()) {
        	if(tempIdSource.getName().equals(idSourceName)) {
        		idSource = tempIdSource; 
        	}
        }
        
    	SearchPrincipalsCommand searchPrincipalsCommand = new SearchPrincipalsCommand();
	
		// create a filter with the login UID equal condition
    	String defaultLogin = "TestUser1";
    	searchPrincipalsCommand.setFilter(Filter.equal(PrincipalDTO.LOGINUID, defaultLogin));
    	searchPrincipalsCommand.setSystemFilter(Filter.empty());
    	searchPrincipalsCommand.setLimit(1);
    	searchPrincipalsCommand.setIdentitySourceGuid(idSource.getGuid());
//    	searchPrincipalsCommand.setSecurityDomainGuid(securityDomain.getGuid());
    	searchPrincipalsCommand.setGroupGuid(null);
    	searchPrincipalsCommand.setOnlyRegistered(true);
    	searchPrincipalsCommand.setSearchSubDomains(true);
	
    	searchPrincipalsCommand.execute();
	
		PrincipalDTO  principal = searchPrincipalsCommand.getPrincipals()[0];
		System.out.println("Middle - " + principal.getMiddleName());

    }
}
