package com.trivir.idm.driver.ace;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.naming.Context;

import com.rsa.admin.EndSearchPrincipalsIterativeCommand;
import com.rsa.admin.SearchPrincipalsIterativeCommand;
import com.rsa.admin.SearchRealmsCommand;
import com.rsa.admin.data.IdentitySourceDTO;
import com.rsa.admin.data.PrincipalDTO;
import com.rsa.admin.data.RealmDTO;
import com.rsa.admin.data.SecurityDomainDTO;
import com.rsa.authmgr.activitymonitor.ActivityMonitorCommand;
import com.rsa.authmgr.activitymonitor.data.ActivityRecordDTO;
import com.rsa.command.ClientSession;
import com.rsa.command.CommandException;
import com.rsa.command.CommandTarget;
import com.rsa.command.CommandTargetPolicy;
import com.rsa.command.Connection;
import com.rsa.command.ConnectionFactory;
import com.rsa.common.search.Filter;

public class ConnectionTester {
	
	private static final String COMMAND_CLIENT_USERNAME = "commandClientUsername";
	private static final String COMMAND_CLIENT_PASSWORD = "commandClientPassword";
	private static final String RSA_REALM = "rsaRealm";
	private static final String WEBLOGIC_LIB_DIR = "weblogicLibDir";
	private static final String RSA_KEYSTORE_FILE = "rsaKeystoreFile";
	private static final String RSA_IDENTITY_SOURCE = "rsaIdentitySource";
	private static final String API_VERSION = "apiVersion";
	private static final String SERVER = "server";
	private static final String USERNAME = "user";
	private static final String PASSWORD = "password";
	
	private static final Map<String, String> authParams8 = new HashMap<String, String>() {
		{
			put(SERVER, "t3s://172.17.2.102:7002");
			put(USERNAME, "idmuser");
			put(PASSWORD, "Trivir7&");
		}
	};
	
	private static final Map<String, String> authParams8_1 = new HashMap<String, String>() {
		{
			put(SERVER, "t3s://172.17.2.104:7002");
			put(USERNAME, "idmuser");
			put(PASSWORD, "Trivir3#");
		}
	};
	
	private static final Map<String, String> driverParams8 = new HashMap<String, String>() {
		{
			put(API_VERSION, "71");
			put(COMMAND_CLIENT_USERNAME, "CmdClient_rpirxfgx");
			put(COMMAND_CLIENT_PASSWORD, "FhpcHW65eFjbLFevNledRtIV70fJMv");
			put(RSA_REALM, "SystemDomain");
			put(WEBLOGIC_LIB_DIR, System.getProperty("user.dir") + "/../jace/lib/rsa8");
			put(RSA_KEYSTORE_FILE, System.getProperty("user.dir") + "/../jace/lib/rsa8/trust.jks");
		}
	};
	
	private static final Map<String, String> driverParams8_1 = new HashMap<String, String>() {
		{
			put(API_VERSION, "71");
			put(COMMAND_CLIENT_USERNAME, "CmdClient_5dt475d8");
			put(COMMAND_CLIENT_PASSWORD, "IzMO8RTWBcS6vhjhlgeuPyg5fQThzT");
			put(RSA_REALM, "SystemDomain");
			put(WEBLOGIC_LIB_DIR, System.getProperty("user.dir") + "/../jace/lib/rsa8_1");
			put(RSA_KEYSTORE_FILE, System.getProperty("user.dir") + "/../jace/lib/rsa8_1/trust.jks");
		}
	};
	
	private static final Map<String, String> allParams8 = new HashMap<String, String>() {
		{
			putAll(authParams8);
			putAll(driverParams8);
		}
	};
	
	private static final Map<String, String> allParams8_1 = new HashMap<String, String>() {
		{
			putAll(authParams8_1);
			putAll(driverParams8_1);
		}
	};
	
	private static final int SERVER_VERSION = 81;
	
	private Map<String, String> initParams;
	private Connection conn;
	private ClientSession session;
	private SecurityDomainDTO domain;
	private IdentitySourceDTO idSource;
	private Date lastLogTime;
	private DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
	
	public static void main(String[] args) throws CommandException, InterruptedException {
		ConnectionTester test = new ConnectionTester();
		test.initParams = SERVER_VERSION == 81 ? allParams8_1 : allParams8;
		test.init();
		test.searchUsers();
		//test.close();
		while (true) {
			//for (int i = 0; i < 10; i++) {
				test.pollRSA();
				System.out.println("Sleeping");
				//Thread.sleep(1900000);
				//Thread.sleep(360000); // 6 minutes
				//Thread.sleep(180000);
				Thread.sleep(30000); //30 Seconds
			//}
		}
		//System.out.println("Complete");
	}
	private void close() throws CommandException {
		session.logout();
		session = null;
		/*CommandTargetPolicy.setDefaultCommandTarget(null);
		CommandTargetPolicy.reset();
		ConnectionFactory.reset();
		System.out.println(CommandTargetPolicy.getDefaultCommandTarget());
		((com.rsa.authn.AuthenticatedTargetImpl)CommandTargetPolicy.getDefaultCommandTarget()).logout();
		conn = null;*/
		//ConnectionFactory.logout(session.getUserGuid());
	}
	
	private void pollRSA() throws CommandException {
		System.out.println(df.format(new Date()));
		System.out.print("Begin Polling...");
		setupSession();
		ActivityRecordDTO[] activities;
   		int countToRetrieve = 2;
   		int retrieveCount = 0;
   		do {
			ActivityMonitorCommand command = new ActivityMonitorCommand(Filter.empty(), countToRetrieve);
			command.setLogType(ActivityMonitorCommand.LOG_TYPE_ADM);
			command.setLastLogTime(getRsaDateFormat().format(lastLogTime));
			command.execute(session);
   			retrieveCount = countToRetrieve;
			
			activities = command.getMessageDTO();

			if(activities == null) {
	   			activities = new ActivityRecordDTO[0];
	   		}
			
			countToRetrieve = countToRetrieve * 2;
   		} while(retrieveCount == activities.length);
   		System.out.println(" End Polling");
   		close();
	}
	
	public void searchUsers() throws CommandException {
		final int limit = 100;
		
		SearchPrincipalsIterativeCommand command = new SearchPrincipalsIterativeCommand();
		command.setIdentitySourceGuid(idSource.getGuid());
		command.setLimit(limit);
		command.setFilter(Filter.empty());
		
		int firstResult = 0;
		PrincipalDTO[] results = null;
		try {
			do {
				command.execute(session);
				results = command.getPrincipals();				
				for (int userNum = 0 ; userNum < results.length ; userNum ++) {
					System.out.println(results[userNum].getUserID());
				}
				
				firstResult += limit;
			} while (results.length > 0);
		} finally {
			//end the search
			EndSearchPrincipalsIterativeCommand endSearch = new EndSearchPrincipalsIterativeCommand();
			endSearch.setSearchContextId(command.getSearchContextId());
			endSearch.execute(session);
		}
	}
	
	private void init() throws CommandException {
		lastLogTime = new Date();
		String weblogicLibDir = initParams.get(WEBLOGIC_LIB_DIR);
		String rsaKeyStoreFile = initParams.get(RSA_KEYSTORE_FILE);

		System.setProperty("bea.home", weblogicLibDir);
		if (rsaKeyStoreFile == null || rsaKeyStoreFile.length() == 0) {
			System.setProperty("UseSunHttpHandler", "true");
		} else {
			System.setProperty("weblogic.security.SSL.trustedCAKeyStore", rsaKeyStoreFile);
			System.setProperty("javax.net.ssl.trustStore", rsaKeyStoreFile);
		}
		setupSession();
	}

	private void setupSession() throws CommandException {
		String identitySourceName = initParams.get(RSA_IDENTITY_SOURCE);
		String realmName = initParams.get(RSA_REALM);

		Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        properties.put(Context.PROVIDER_URL, initParams.get(SERVER));
        properties.put(Context.SECURITY_PRINCIPAL, initParams.get(COMMAND_CLIENT_USERNAME));
        properties.put(Context.SECURITY_CREDENTIALS, initParams.get(COMMAND_CLIENT_PASSWORD));

        conn = ConnectionFactory.getConnection(properties);
        session = conn.connect(initParams.get(USERNAME), initParams.get(PASSWORD));
		
        //System.out.println(CommandTargetPolicy.getDefaultCommandTarget());
        // make all commands execute using this target automatically
        //CommandTargetPolicy.setDefaultCommandTarget((CommandTarget)session);
        //System.out.println(CommandTargetPolicy.getDefaultCommandTarget());
        //CommandTargetPolicy.s

        if (domain == null) {
	        RealmDTO realm = getRealm(realmName);
	        domain = realm.getTopLevelSecurityDomain();
	        IdentitySourceDTO[] identitySources = realm.getIdentitySources();
	        
	        if(identitySourceName == null) {
	        	idSource = identitySources[0]; 
	        } else {
		        StringBuilder identitySourceNames = new StringBuilder();
		        String separator = "";
		        for(IdentitySourceDTO identitySource : realm.getIdentitySources()) {
		        	identitySourceNames.append(separator).append(identitySource.getName());
		        	if(initParams.get(RSA_IDENTITY_SOURCE).equals(identitySource.getName())) {
		        		idSource = identitySource;
		        		break;
		        	}
		        	separator = ", ";
		        }
	
		        if(idSource == null) {
		        	throw new RuntimeException(String.format("Identity source '%s' does not match available identity source(s) '%s' in realm '%s'.", identitySourceName, identitySourceNames.toString(), realmName));
		        }
			}
        }
	}
	
	private RealmDTO getRealm(String realmName) throws CommandException  {
        SearchRealmsCommand command = new SearchRealmsCommand();
        command.setFilter( Filter.equal( RealmDTO.NAME_ATTRIBUTE, realmName));
        command.setLimit(1);
        command.execute(session);
        if( command.getRealms().length < 1 ) {
            throw new RuntimeException("Realm with name '" + realmName + "' not found.");
        }
        
        return command.getRealms()[0];
    }
	
	static SimpleDateFormat getRsaDateFormat() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		return df;
	}
}