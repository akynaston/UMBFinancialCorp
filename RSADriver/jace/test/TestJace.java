import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import javax.naming.Context;
import javax.naming.NamingException;

import com.rsa.admin.AddPrincipalsCommand;
import com.rsa.admin.DeletePrincipalsCommand;
import com.rsa.admin.SearchPrincipalsCommand;
import com.rsa.admin.SearchRealmsCommand;
import com.rsa.admin.SearchSecurityDomainCommand;
import com.rsa.admin.data.IdentitySourceDTO;
import com.rsa.admin.data.PrincipalDTO;
import com.rsa.admin.data.RealmDTO;
import com.rsa.admin.data.SecurityDomainDTO;
import com.rsa.authmgr.activitymonitor.ActivityMonitorCommand;
import com.rsa.authmgr.activitymonitor.data.ActivityRecordDTO;
import com.rsa.authmgr.activitymonitor.data.LabelValueBean;
import com.rsa.authmgr.admin.principalmgt.AddAMPrincipalCommand;
import com.rsa.authmgr.admin.principalmgt.data.AMPrincipalDTO;
import com.rsa.authmgr.admin.tokenmgt.LinkTokensWithPrincipalCommand;
import com.rsa.authmgr.admin.tokenmgt.LookupTokenCommand;
import com.rsa.authmgr.admin.tokenmgt.UpdateTokenCommand;
import com.rsa.authmgr.admin.tokenmgt.data.TokenDTO;
import com.rsa.command.ClientSession;
import com.rsa.command.CommandException;
import com.rsa.command.CommandTargetPolicy;
import com.rsa.command.Connection;
import com.rsa.command.ConnectionFactory;
import com.rsa.command.exception.DataNotFoundException;
import com.rsa.command.exception.DuplicateDataException;
import com.rsa.command.exception.InsufficientPrivilegeException;
import com.rsa.command.exception.InvalidArgumentException;
import com.rsa.common.SystemException;
import com.rsa.common.search.Filter;

public class TestJace {
    private final static String USER_NAME = "jdoe";

    public static void main(String[] args) throws NamingException, InvalidArgumentException, DataNotFoundException, InsufficientPrivilegeException, CommandException, SystemException, ParseException, InterruptedException {
    	initializeCommandTarget();

    	if(args.length == 1) {
    		if(args[0].equals("monitorEvents")) {
    	        monitorEvents();
    			return;
    		}
    	}
    	
        // create a user and the AMPrincipal user record
    	String userGuid;
    	try {
    		userGuid = createUser(USER_NAME, "Password123!", "John", "Doe", lookupSecurityDomain("Demo Security Domain").getGuid());
    		createAMUser(userGuid);
    	} catch(Exception e) {}
        System.out.println("Created user " + USER_NAME);

        userGuid = lookupUser(USER_NAME).getGuid();
        
        PrincipalDTO user = lookupUser(USER_NAME);
        System.out.println("User start :" + user.getAccountStartDate());
        System.out.println("User expire :" + user.getAccountExpireDate());
        System.out.println("User ID : " + user.getUserID());

        TokenDTO token = getToken("000111646289");
        System.out.println("Token userid: " + token.getAssignedUserId());
        System.out.println("Token username: " + token.getAssignedUserName());
        

        assignTokenToUser(userGuid, "000111646289");
        System.out.println("Assigning token 000111646289 to user " + USER_NAME);

        token = getToken("000111646289");
        System.out.println("Token userid: " + token.getAssignedUserId());
        System.out.println("Token username: " + token.getAssignedUserName());
        System.out.println("Token guid: " + token.getId());
        setTokenPin("000111646289", "1111", true);
        System.out.println("Set token pin to 1111 and to reset on use.");

        deleteUser(userGuid);
        System.out.println("Deleted user " + USER_NAME);

//        try {
//            AceToolkit atk = new AceToolkit();
//            System.out.println("success");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
    
    private static void monitorEvents() throws CommandException, ParseException, InterruptedException {
    	Date lastLogTime = new Date();
    	
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    	simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    	
    	System.out.println("Date - " + lastLogTime);
    	System.out.println("RSA Date using SimpleDateFormat - " + simpleDateFormat.format(lastLogTime));
    	System.out.println("========================================");
    	
    	while (true) {
        	ActivityMonitorCommand cmd = new ActivityMonitorCommand(Filter.empty(), 1);
        	cmd.setLogType(ActivityMonitorCommand.LOG_TYPE_ADM);
        	cmd.setLastLogTime(simpleDateFormat.format(lastLogTime));
	    	cmd.execute();
	    	ActivityRecordDTO[] activities = cmd.getMessageDTO();
	    	if(activities != null) {
		    	if(activities.length == 1) {
		    		dumpFields(activities[0]);
		        	System.out.println("===== Last log time via cmd  " + activities[0].getUtcDateTime().getValue() + " =====");
		    		lastLogTime = simpleDateFormat.parse(activities[0].getUtcDateTime().getValue());
		    	} else if (activities.length == 0) {
		    		System.out.println("Sleeping");
		    		Thread.sleep(5000);
		    	} else {
		    		throw new RuntimeException("Received something other than one event.");
		    	}
	    	} else {
	    		Thread.sleep(5000);
	    	}

        	System.out.println("===== Last log time via date " + simpleDateFormat.format(lastLogTime) + " =====");
        	System.out.println("==========================================================");
    	}
    }

	private static void dumpFields(ActivityRecordDTO activity) {
		Method[] methods = activity.getClass().getMethods();
		for(Method method : methods) {
			if(method.getName().startsWith("get") && !Modifier.isStatic(method.getModifiers()) 
					&& Modifier.isPublic(method.getModifiers())&& method.getGenericReturnType() == LabelValueBean.class
					&& method.getGenericParameterTypes().length == 0) {
				try {
					LabelValueBean lvBean = (LabelValueBean) method.invoke(activity);
					if(!lvBean.getLabel().equals("N/A") && !lvBean.getLabel().equals("N/A")) {
						System.out.println(lvBean.getLabel() + " -> " + lvBean.getValue());
					}
				} catch (Exception e) {
					System.out.println("Failed to dump activity values");
				}
			}
		}
	}
	
    /** Name of the JNDI Context Factory to use.  This is the BEA WebLogic provider. */
    private static final String INITIAL_CONTEXT_FACTORY_CLASS = "weblogic.jndi.WLInitialContextFactory";

    /** URL of the server(s) to contact.  May be a comma separated list of URLs for
     * failover purposes.  Should be set to a deployment-appropriate value.  */
    private static String serverURL = "t3s://amserver.trivir.com:7002";

    private static SecurityDomainDTO domain;
    private static IdentitySourceDTO idSource;

    /**
     * Initialze the connection to the remote server.
     *
     * @throws NamingException If unable to establish a connection.
     * @throws SystemException 
     * @throws CommandException 
     * @throws InsufficientPrivilegeException 
     * @throws DataNotFoundException 
     * @throws InvalidArgumentException 
     */
    public static void initializeCommandTarget() throws NamingException, InvalidArgumentException, DataNotFoundException, InsufficientPrivilegeException, CommandException, SystemException {
        Properties properties = new Properties();
        properties.put( Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY_CLASS );
        properties.put( Context.PROVIDER_URL, serverURL );
        properties.put( Context.SECURITY_PRINCIPAL, "CmdClient_qytc0b7i" );
        properties.put( Context.SECURITY_CREDENTIALS, "eWTDf6yiet" );
//        EJBRemoteTarget target = new EJBRemoteTarget( properties );
//        CommandTargetPolicy.setDefaultCommandTarget(target);

        // establish a connected session with given credentials
//        Connection conn = ConnectionFactory.getConnection();
        Connection conn = ConnectionFactory.getConnection(properties);
        ClientSession session = conn.connect("trivir", "trivir&rsa");

        // make all commands execute using this target automatically
        CommandTargetPolicy.setDefaultCommandTarget(session);

        SearchRealmsCommand searchRealmCmd = new SearchRealmsCommand();
        searchRealmCmd.setFilter( Filter.equal( RealmDTO.NAME_ATTRIBUTE, "SystemDomain"));
        searchRealmCmd.execute();
        RealmDTO[] realms = searchRealmCmd.getRealms();
        if( realms.length == 0 ) {
            System.out.println( "ERROR: Could not find realm SystemDomain" );
            System.exit( 2 );
        }
        domain = realms[0].getTopLevelSecurityDomain();
        idSource = realms[0].getIdentitySources()[0];

    }

    private static String createUser(String userId, String password, String first, String last, String sdGuid) throws CommandException {
		Calendar cal = Calendar.getInstance();
		
		// the start date
		Date now = cal.getTime();
		
		cal.add(Calendar.YEAR, 1);
		
		// the account end date
		Date expire = cal.getTime();
		
		PrincipalDTO principal = new PrincipalDTO();
		principal.setUserID( userId );
		principal.setFirstName( first );
		principal.setLastName( last );
//		principal.setPassword( password );
		
		principal.setEnabled(true);
		principal.setAccountStartDate(now);
		principal.setAccountExpireDate(expire);
		principal.setCanBeImpersonated(false);
		principal.setTrustToImpersonate(false);
		
		principal.setSecurityDomainGuid(sdGuid);
		principal.setIdentitySourceGuid(idSource.getGuid());
		//require user to change password at next login
		//principal.setPasswordExpired(true);
		//principal.setDescription("Created by AM Demo code");
		
		AddPrincipalsCommand cmd = new AddPrincipalsCommand();
		cmd.setPrincipals( new PrincipalDTO[] { principal } );
		
		try {
			cmd.execute();
		} catch (DuplicateDataException e) {
			System.out.println("ERROR: User " + userId + " already exists.");
			throw e;
		}

		// only one user was created, there should be one GUID result
		return cmd.getGuids()[0];
	}
    private static void createAMUser(String guid)
    throws CommandException {
	    AMPrincipalDTO principal = new AMPrincipalDTO();
	    principal.setGuid(guid);
	    principal.setDefaultShell("/bin/sh");
	    principal.setDefaultUserIdShellAllowed(true);
//	    principal.setStaticPassword("12345678");
//	    principal.setStaticPasswordSet(true);
//	    principal.setWindowsPassword("Password123!");
	
	    AddAMPrincipalCommand cmd = new AddAMPrincipalCommand(principal);
	    cmd.execute();
	}
    
	private static void assignTokenToUser(String userGuid, String serialNumber) throws CommandException {
		TokenDTO token = getToken(serialNumber);
		String[] tokens = new String[] { token.getId() };
		LinkTokensWithPrincipalCommand cmd2 = new LinkTokensWithPrincipalCommand(tokens, userGuid);
		cmd2.execute();
		System.out.println("Assigned token 111646289 to user with guid " + userGuid );
	}

	private static TokenDTO getToken(String serialNumber) throws CommandException {
		LookupTokenCommand cmd = new LookupTokenCommand();
		
		try {
			cmd.setToken(new TokenDTO());
			cmd.setSerialNumber(serialNumber);
			cmd.execute();
			return cmd.getToken();
		} catch (DataNotFoundException e) {
			System.out.println("ERROR: No tokens available");
			throw e;
		}

	}

	private static void setTokenPin(String serial, String pin, boolean newPinMode) throws CommandException {
		UpdateTokenCommand cmd = new UpdateTokenCommand();
		TokenDTO token = getToken(serial);
		token.setPinType(TokenDTO.USE_PIN);
		token.setPin(pin);
		token.setNewPinMode(newPinMode);
		cmd.setToken(token);
		cmd.execute();
	}
    
    private static void deleteUser(String userGuid) throws CommandException {
	    DeletePrincipalsCommand cmd = new DeletePrincipalsCommand();
	    cmd.setGuids(new String[] { userGuid } );
	    cmd.setIdentitySourceGuid( idSource.getGuid() );
	    cmd.execute();
    }

	private static PrincipalDTO lookupUser(String userId) throws CommandException {
		SearchPrincipalsCommand cmd = new SearchPrincipalsCommand();
	
		// create a filter with the login UID equal condition
		cmd.setFilter(Filter.equal(PrincipalDTO.LOGINUID, userId));
		cmd.setSystemFilter(Filter.empty());
		cmd.setLimit(1);
		cmd.setIdentitySourceGuid(idSource.getGuid());
		cmd.setSecurityDomainGuid(domain.getGuid());
		cmd.setGroupGuid(null);
		cmd.setOnlyRegistered(true);
		cmd.setSearchSubDomains(true);
	
		cmd.execute();
	
		if (cmd.getPrincipals().length < 1) {
			System.out.println("ERROR: Unable to find user " + userId + ".");
			System.exit(2);
		}

		return cmd.getPrincipals()[0];
	}

    private static SecurityDomainDTO lookupSecurityDomain(String name) throws CommandException {
    	SearchSecurityDomainCommand cmd = new SearchSecurityDomainCommand();
        cmd.setFilter(Filter.equal(SecurityDomainDTO.NAME_ATTRIBUTE, name));
        cmd.setLimit(1);

        // in order to search all levels we set searchbase to "*"
        cmd.setSearchBase("*");
        cmd.setSearchScope(SecurityDomainDTO.SEARCH_SCOPE_SUB);
        cmd.execute();

        if (cmd.getSecurityDomains().length == 0) {
            System.out.println("Could not find security domain " + name);
            System.exit(2);
        }

        return cmd.getSecurityDomains()[0];
    }

}