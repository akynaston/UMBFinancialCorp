package com.trivir.idm.driver.ace;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.naming.Context;

import com.rsa.admin.AddPrincipalsCommand;
import com.rsa.admin.DeletePrincipalsCommand;
import com.rsa.admin.SearchRealmsCommand;
import com.rsa.admin.data.IdentitySourceDTO;
import com.rsa.admin.data.PrincipalDTO;
import com.rsa.admin.data.RealmDTO;
import com.rsa.admin.data.SecurityDomainDTO;
import com.rsa.authmgr.activitymonitor.ActivityMonitorCommand;
import com.rsa.authmgr.activitymonitor.data.ActivityRecordDTO;
import com.rsa.authmgr.admin.principalmgt.AddAMPrincipalCommand;
import com.rsa.authmgr.admin.principalmgt.data.AMPrincipalDTO;
import com.rsa.command.ClientSession;
import com.rsa.command.CommandException;
import com.rsa.command.Connection;
import com.rsa.command.ConnectionFactory;
import com.rsa.command.exception.DuplicateDataException;
import com.rsa.common.search.Filter;
import com.trivir.ace.AceToolkitException;

public class TestPolling {
	
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
	private String userGuid;
	private DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
	
	public static void main(String[] args) throws CommandException, InterruptedException, ParseException {
		TestPolling test = new TestPolling();
		test.initParams = SERVER_VERSION == 81 ? allParams8_1 : allParams8;
		test.init();
		Thread.sleep(5000);
		while (true) {
			test.createUser();
			test.pollRSA();
			//Thread.sleep(10000);
			test.deleteUser();
			System.out.println("Sleeping");
			Thread.sleep(10000); //10 Seconds
		}
	}
	private void close() throws CommandException {
		session.logout();
		session = null;
	}
	
	private void pollRSA() throws CommandException, ParseException {
		System.out.println(df.format(new Date()));
		System.out.println("Begin Polling...");
		ActivityRecordDTO[] activities;
   		int countToRetrieve = 2;
   		int retrieveCount = 0;
   		do {
   			System.out.println(getRsaDateFormat().format(lastLogTime));
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
   		
   		System.out.println(activities.length + " Event(s) Found.");
   		for (ActivityRecordDTO dtoItem : activities) {
   			System.out.println(dtoItem.getActionKey().getValue());
   		}
   		System.out.println("...End Polling");
   		String rsaDateTime = normalizeRsaDateStr(activities[activities.length - 1].getUtcDateTime().getValue());
   		lastLogTime = getRsaDateFormat().parse(rsaDateTime);
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

	private void createUser() {
		AddPrincipalsCommand command = new AddPrincipalsCommand();
		
		// the start date
		PrincipalDTO principal = new PrincipalDTO();
		principal.setUserID("tuser");
		principal.setFirstName("Test");
		principal.setLastName("User");
		
		principal.setEnabled(true);
		principal.setCanBeImpersonated(false);
		principal.setTrustToImpersonate(false);
		
		principal.setSecurityDomainGuid(domain.getGuid());
		principal.setIdentitySourceGuid(idSource.getGuid());

		principal.setPassword("Trivir1!");
		principal.setPasswordExpired(false);
		
		command.setPrincipals( new PrincipalDTO[] { principal } );
		
		try {
			command.execute(session);
		} catch (DuplicateDataException e) {
			throw new RuntimeException("User 'tuser' already exists", e);
		} catch (CommandException e) {
			throw new RuntimeException("Error occured while creating user 'tuser'.", e);
		}

		userGuid = command.getGuids()[0];
		AMPrincipalDTO amPrincipal = new AMPrincipalDTO();
		amPrincipal.setGuid(userGuid);
		amPrincipal.setDefaultShell("");
		amPrincipal.setDefaultUserIdShellAllowed(true);
	
	    AddAMPrincipalCommand amCcommand = new AddAMPrincipalCommand(amPrincipal);
	    try {
	    	amCcommand.execute(session);
	    } catch (CommandException e) {
	    	throw new RuntimeException("Error creating AM user for user with GUID '" + userGuid + "'.", e);
	    }
	}
	
	public void deleteUser() {
		DeletePrincipalsCommand cmd = new DeletePrincipalsCommand();
	    cmd.setGuids(new String[] { userGuid } );
	    cmd.setIdentitySourceGuid( idSource.getGuid() );
	    try {
	    	cmd.execute(session);
	    } catch (CommandException e) {
	    	throw new RuntimeException("Error occured while deleting user 'tuser'.", e);
	    }
	}
	
	private String normalizeRsaDateStr(String rsaDateTime) {
		int diff = rsaDateTime.length() - rsaDateTime.lastIndexOf(".");
		
		if(diff == 4) {
			return rsaDateTime;
		} else if(diff == 3) {
			return rsaDateTime + "0";
		}else if(diff == 2) {
			return rsaDateTime + "00";
		}else if(diff == 1) {
			return rsaDateTime + "000";
		}
		
		throw new RuntimeException("Error normalizing rsa event date time.");
	}
	
	static SimpleDateFormat getRsaDateFormat() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		return df;
	}
}