package com.trivir.idm.driver.ace;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.naming.Context;

import com.rsa.admin.SearchRealmsCommand;
import com.rsa.admin.data.IdentitySourceDTO;
import com.rsa.admin.data.RealmDTO;
import com.rsa.admin.data.SecurityDomainDTO;
import com.rsa.authmgr.activitymonitor.ActivityMonitorCommand;
import com.rsa.authmgr.activitymonitor.data.ActivityRecordDTO;
import com.rsa.command.ClientSession;
import com.rsa.command.CommandException;
import com.rsa.command.Connection;
import com.rsa.command.ConnectionFactory;
import com.rsa.common.search.Filter;

public class ActivityMonitorTool {
	
	private static final String COMMAND_CLIENT_USERNAME = "commandClientUsername";
	private static final String COMMAND_CLIENT_PASSWORD = "commandClientPassword";
	private static final String RSA_REALM = "rsaRealm";
	private static final String WEBLOGIC_LIB_DIR = "weblogicLibDir";
	private static final String RSA_KEYSTORE_FILE = "rsaKeystoreFile";
	private static final String RSA_IDENTITY_SOURCE = "rsaIdentitySource";
	private static final String SERVER = "server";
	private static final String USERNAME = "user";
	private static final String PASSWORD = "password";
	private static final String POLLING_RATE = "pollingIntervalSeconds";
	
	private Map<String, String> initParams;
	private Connection conn;
	private ClientSession session;
	private SecurityDomainDTO domain;
	private IdentitySourceDTO idSource;
	private Date lastLogTime;
	private DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
	
	public static void main(String[] args) throws CommandException, InterruptedException {
		ActivityMonitorTool monitor = new ActivityMonitorTool();
		Properties props = new Properties();
		try {
			InputStream is = new FileInputStream(args[0]);
			props.load(is);
			is.close();
		}
		catch (IOException e) {
			System.out.println("Unable to load config from file: " + args[0]);
			return;
		}
		monitor.initializeParams(props);
		monitor.doWork();
	}
	
	private void doWork() throws InterruptedException, CommandException {
		init();
		while (true) {
			pollRSA();
			System.out.println("Sleeping");
			Thread.sleep(Integer.valueOf(initParams.get(POLLING_RATE)) * 1000);
		}
	}
	
	private void initializeParams(Properties props) {
		initParams = new HashMap<String, String>();
		for (Object o : props.keySet()) {
			initParams.put(o.toString(), props.getProperty(o.toString()));
		}
	}

	private void pollRSA() throws CommandException {
		System.out.println("Begin Polling...");
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
			
			for (ActivityRecordDTO item : activities) {
				System.out.println("Activity Retrieved From RSA: " + item.getMessage().getValue());
			}
			
			countToRetrieve = countToRetrieve * 2;
   		} while(retrieveCount == activities.length);
   		try {
   			if(activities.length > 0) {
   				String rsaDateTime = normalizeRsaDateStr(activities[activities.length - 1].getUtcDateTime().getValue());
   				lastLogTime = getRsaDateFormat().parse(rsaDateTime);
   			}
		} catch (ParseException e) {
			throw new RuntimeException("Unable to retrieve last log time from event. This may cause monitor history to stall.", e);
		}
   		System.out.println("End Polling");
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
	
	private void close() throws CommandException {
		session.logout();
		session = null;
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