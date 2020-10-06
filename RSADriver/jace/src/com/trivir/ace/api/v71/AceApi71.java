/**
 * 
 */
package com.trivir.ace.api.v71;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.naming.Context;

import com.rsa.admin.AddPrincipalsCommand;
import com.rsa.admin.DeletePrincipalsCommand;
import com.rsa.admin.EndSearchPrincipalsIterativeCommand;
import com.rsa.admin.GetPrincipalGroupsCommand;
import com.rsa.admin.LinkGroupPrincipalsCommand;
import com.rsa.admin.LookupSecurityDomainCommand;
import com.rsa.admin.RegisterPrincipalsCommand;
import com.rsa.admin.SearchGroupsCommand;
import com.rsa.admin.SearchPrincipalsCommand;
import com.rsa.admin.SearchPrincipalsIterativeCommand;
import com.rsa.admin.SearchRealmsCommand;
import com.rsa.admin.SearchSecurityDomainCommand;
import com.rsa.admin.UnRegisterPrincipalsCommand;
import com.rsa.admin.UnlinkGroupPrincipalsCommand;
import com.rsa.admin.UpdatePrincipalCommand;
import com.rsa.admin.data.AttributeDTO;
import com.rsa.admin.data.GroupDTO;
import com.rsa.admin.data.IdentitySourceDTO;
import com.rsa.admin.data.ModificationDTO;
import com.rsa.admin.data.PrincipalDTO;
import com.rsa.admin.data.RealmDTO;
import com.rsa.admin.data.SecurityDomainDTO;
import com.rsa.admin.data.UpdatePrincipalDTO;
import com.rsa.authmgr.activitymonitor.ActivityMonitorCommand;
import com.rsa.authmgr.activitymonitor.data.ActivityRecordDTO;
import com.rsa.authmgr.admin.principalmgt.AddAMPrincipalCommand;
import com.rsa.authmgr.admin.principalmgt.LookupAMPrincipalCommand;
import com.rsa.authmgr.admin.principalmgt.UpdateAMPrincipalCommand;
import com.rsa.authmgr.admin.principalmgt.data.AMPrincipalDTO;
import com.rsa.authmgr.admin.radius.LinkPrincipalsToProfileCommand;
import com.rsa.authmgr.admin.radius.ListRadiusProfilesCommand;
import com.rsa.authmgr.admin.radius.UnlinkPrincipalsFromProfileCommand;
import com.rsa.authmgr.admin.radius.data.RadiusProfileDTO;
import com.rsa.authmgr.admin.tokenmgt.LinkTokensWithPrincipalCommand;
import com.rsa.authmgr.admin.tokenmgt.ListTokensByPrincipalCommand;
import com.rsa.authmgr.admin.tokenmgt.LookupTokenCommand;
import com.rsa.authmgr.admin.tokenmgt.LookupTokenEmergencyAccessCommand;
import com.rsa.authmgr.admin.tokenmgt.ReplaceTokensCommand;
import com.rsa.authmgr.admin.tokenmgt.SearchTokensCommand;
import com.rsa.authmgr.admin.tokenmgt.UnlinkTokensFromPrincipalsCommand;
import com.rsa.authmgr.admin.tokenmgt.UpdateTokenCommand;
import com.rsa.authmgr.admin.tokenmgt.UpdateTokenEmergencyAccessCommand;
import com.rsa.authmgr.admin.tokenmgt.data.ListTokenDTO;
import com.rsa.authmgr.admin.tokenmgt.data.ReplaceTokenDTO;
import com.rsa.authmgr.admin.tokenmgt.data.TokenDTO;
import com.rsa.authmgr.admin.tokenmgt.data.TokenEmergencyAccessDTO;
import com.rsa.authn.AuthenticationCommandException;
import com.rsa.command.ClientSession;
import com.rsa.command.CommandException;
import com.rsa.command.Connection;
import com.rsa.command.ConnectionFactory;
import com.rsa.command.TargetableCommand;
import com.rsa.command.exception.DuplicateDataException;
import com.rsa.command.exception.InvalidArgumentException;
import com.rsa.common.SystemException;
import com.rsa.common.search.Filter;
import com.trivir.ace.AceToolkitException;
import com.trivir.ace.api.AceApi;
import com.trivir.ace.api.AceApiEvent;
import com.trivir.ace.api.AceApiEventType;
import com.trivir.ace.api.AceEventException;

@SuppressWarnings("unchecked")
public class AceApi71 implements AceApi {

	private static final String INITIAL_CONTEXT_FACTORY_CLASS = "weblogic.jndi.WLInitialContextFactory";
    private static final SimpleDateFormat rsaDateFormat = getRsaDateFormat();
    private static final SimpleDateFormat tempUserDateFormat = new SimpleDateFormat("MM/dd/yyyy");
	private static final String RELEASE = "Release: 7.1";
	private static final String PROPERTY_ALLOW_ARRAY_SYNTAX = "sun.lang.ClassLoader.allowArraySyntax";

	private static final Map<String,String> RSA7_REQUIRED_JAR_TO_CLASS_MAP;
	private static final Map<String,String> RSA8_REQUIRED_JAR_TO_CLASS_MAP;

	static {
		HashMap<String,String> rsa7Jars = new LinkedHashMap<String,String>();

		// For RSA 7: am-client.jar and ims-client.jar must be in the class path for this class to load
//		rsa7Jars.put("axis-1.3.jar", "org.apache.axis.attachments.AttachmentPart");
		rsa7Jars.put("com.bea.core.process_5.3.0.0.jar", "com.bea.utils.misc.ProcessException");
//		rsa7Jars.put("commons-beanutils-1.7.0.jar", "org.apache.commons.beanutils.BasicDynaBean");		
//		rsa7Jars.put("commons-discovery-0.2.jar", "org.apache.commons.discovery.ant.ServiceDiscoveryTask");
//		rsa7Jars.put("commons-lang-2.2.jar", "org.apache.commons.lang.ArrayUtils");		
//		rsa7Jars.put("commons-logging-1.0.4.jar", "org.apache.commons.logging.LogFactory");
//		rsa7Jars.put("EccpressoAsn1.jar", "com.certicom.ecc.asn1.CharacteristicTwo");
//		rsa7Jars.put("EccpressoCore.jar", "com.certicom.ecc.ECA");
//		rsa7Jars.put("EccpressoJcae.jar", "com.certicom.ecc.jcae.AESCipherSpi");
		rsa7Jars.put("gson-2.2.4.jar", "com.google.gson.Gson");
		rsa7Jars.put("hibernate-3.2.2.jar", "org.hibernate.HibernateException");
		rsa7Jars.put("hsqldb.jar", "org.hsqldb.ClientConnection");
//		rsa7Jars.put("ims-client.jar", "com.rsa.admin.AddAdminConsolePreferencesCommand");
		rsa7Jars.put("iScreen-1-1-0rsa-2.jar", "org.iscreen.ValidationException");
//		rsa7Jars.put("iScreen-ognl-1-1-0rsa-2.jar", "org.iscreen.ognl.OgnlMessage");
//		rsa7Jars.put("jdom-1.0.jar", "org.jdom.CDATA");
//		rsa7Jars.put("jsafe-3.6.jar", "com.rsa.jsafe.CryptoJ");
//		rsa7Jars.put("jsafeJCE-3.6.jar", "com.rsa.jsafe.asn1.ASN1");
//		rsa7Jars.put("log4j-1.2.11rsa-3.jar", "org.apache.log4j.Appender");		
		rsa7Jars.put("ognl-2.6.7.jar", "ognl.OgnlContext");
		rsa7Jars.put("spring-2.0.7.jar", "org.springframework.beans.factory.access.BeanFactoryLocator");
//		rsa7Jars.put("systemfields-o.jar", "com.rsa.ims.security.keymanager.sys.DigestProxy");
//		rsa7Jars.put("ucm-client.jar", "com.rsa.ucm.admin.AdminConverter");
//		rsa7Jars.put("wlcipher.jar", "weblogic.jce.WLCipher");
		rsa7Jars.put("wlfullclient.jar", "weblogic.security.SSL.TrustManager");

		RSA7_REQUIRED_JAR_TO_CLASS_MAP = Collections.unmodifiableMap(rsa7Jars);

		HashMap<String,String> rsa8Jars = new LinkedHashMap<String,String>();

		// For RSA 8: am-client.jar must be in the class path for this class to load
//		rsa8Jars.put("aopalliance.jar", "");
//		rsa8Jars.put("axis-jaxrpc.jar", "");
//		rsa8Jars.put("axis-saaj.jar", "");
//		rsa8Jars.put("axis.jar", "org.apache.axis.soap.SOAPConnectionImpl");
//		rsa8Jars.put("clu-common.jar", "");
		rsa8Jars.put("commons-beanutils.jar", "org.apache.commons.beanutils.Converter");
//		rsa8Jars.put("commons-codec.jar", "");
//		rsa8Jars.put("commons-discovery.jar", "org.apache.commons.discovery.Resource");
//		rsa8Jars.put("commons-httpclient.jar", "");
//		rsa8Jars.put("commons-lang.jar", "org.apache.commons.lang.ArrayUtils");
		rsa8Jars.put("commons-logging.jar", "org.apache.commons.logging.LogFactory");
		rsa8Jars.put("gson-2.2.4.jar", "com.google.gson.Gson");
		//rsa8Jars.put("hsqldb.jar", "org.hsqldb.ClientConnection");
		rsa8Jars.put("iScreen.jar", "org.iscreen.ValidationException");
//		rsa8Jars.put("iScreen-ognl.jar", "org.iscreen.ognl.OgnlMessage");
		rsa8Jars.put("log4j.jar", "org.apache.log4j.Priority");
		rsa8Jars.put("ognl.jar", "ognl.OgnlException");
//		rsa8Jars.put("spring-aop.jar", "");
		rsa8Jars.put("spring-asm.jar", "org.springframework.asm.ClassVisitor");
		rsa8Jars.put("spring-beans.jar", "org.springframework.beans.factory.access.SingletonBeanFactoryLocator");
//		rsa8Jars.put("spring-context-support.jar", "");
		rsa8Jars.put("spring-context.jar", "org.springframework.context.access.ContextSingletonBeanFactoryLocator");
		rsa8Jars.put("spring-core.jar", "org.springframework.core.NestedRuntimeException");
		rsa8Jars.put("spring-expression.jar", "org.springframework.expression.PropertyAccessor");
//		rsa8Jars.put("spring-web.jar", "");
		rsa8Jars.put("wlfullclient.jar", "weblogic.security.SSL.TrustManager");
//		rsa8Jars.put("wsdl4j.jar", "");
//		rsa8Jars.put("xercesImpl.jar", "");

		RSA8_REQUIRED_JAR_TO_CLASS_MAP = Collections.unmodifiableMap(rsa8Jars);
	}

    private SecurityDomainDTO domain;
    private IdentitySourceDTO idSource;

	private Date lastLogTime;
	private Queue<ActivityRecordDTO> cachedEvents = new LinkedList<ActivityRecordDTO>();
	private final Map<String, String> initParams;
	
	private ClientSession session;
	
	private List<String> activityLog;

	public AceApi71(Map<String,String> params) throws AceToolkitException {
		initParams = params;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -5);
        lastLogTime = cal.getTime(); // 2 seconds in the past is a fudge factor.
        checkEnvironmentDependencies(); // we don't call this in init because there is no need to test this on a reinit.
        activityLog = new ArrayList<String>();
        init();
    }

	public void addLoginToGroup(String groupLogin, String groupName, String groupShell, String tokenSerialOrLogin) throws AceToolkitException {
		if(!tokenSerialOrLogin.startsWith("-")) { 
			throw new AceToolkitException("Adding a login to a group using tokenSerial is not currently supported");
		}

		if(groupLogin.length() > 0) { 
			throw new AceToolkitException("Using groupLogin while adding a login to a group is not currently supported");
		}

		PrincipalDTO principal = getPrincipal(tokenSerialOrLogin);
		GroupDTO group = getGroup(groupName);
		
		LinkGroupPrincipalsCommand command = new LinkGroupPrincipalsCommand();
        command.setGroupGuids( new String[] { group.getGuid() } );
        command.setPrincipalGuids( new String[] { principal.getGuid() } );
        command.setIdentitySourceGuid(idSource.getGuid());

        try {
        	executeWithRetry(command);
        } catch (CommandException e) {
        	throw new AceToolkitException("Error occured while adding user '" + tokenSerialOrLogin + "' to group '" + groupName + "'.", e);
        }
	}

	public void addUser(String lastName, String firstName, String defaultLogin, String emailAddress, String defaultShell, String password) throws AceToolkitException {
		//TODO: This is not atomic, what do we do if createAMUser fails?
		String userGuid = createUserObject(defaultLogin, firstName, lastName, emailAddress, password);
		createAMUser(userGuid, defaultShell);
	}

	public String apiRev() throws AceToolkitException {
		return RELEASE;
	}

	public void assignAnotherToken(String tokenSerialOrLogin, String tokenSerialNumber) throws AceToolkitException {
		if(!tokenSerialOrLogin.startsWith("-")) {
			throw new AceToolkitException("Assigning another token using tokenSerial is not currently supported");
		}
		
		String defaultLogin = tokenSerialOrLogin.substring(1); // strip off the leading "-" used to identify login with older apis
		
		PrincipalDTO principal = getPrincipal(defaultLogin);
		TokenDTO token = getToken(tokenSerialNumber);
		
		LinkTokensWithPrincipalCommand command = new LinkTokensWithPrincipalCommand(new String[] {token.getId()}, principal.getGuid());
		
		try {
			executeWithRetry(command);
		} catch (CommandException e) {
        	throw new AceToolkitException("Error occured while assigning token '" + tokenSerialNumber + "' to user '" + defaultLogin + "'.", e);
		}
	}

	public void assignProfile(String tokenSerialOrLogin, String profileName) throws AceToolkitException {
		if(!tokenSerialOrLogin.startsWith("-")) {
			throw new AceToolkitException("Assigning profiles using tokenSerial is not currently supported");
		}
		
		String defaultLogin = tokenSerialOrLogin.substring(1); // strip off the leading "-" used to identify login with older apis

		PrincipalDTO principal = getPrincipal(defaultLogin);
		RadiusProfileDTO profile = getRadiusProfile(profileName);
		
		LinkPrincipalsToProfileCommand command = new LinkPrincipalsToProfileCommand();
		command.setPrincipalGuids(new String[] {principal.getGuid()});
		command.setProfileGuid(profile.getId());
		command.setIdentitySourceGuid(idSource.getGuid());
		command.setSecurityDomainGuid(domain.getGuid());
		
		try {
			executeWithRetry(command);
		} catch (CommandException e) {
        	throw new AceToolkitException("Error occured while assigning profile '%" + profileName+ "' to user '" +defaultLogin + "'.", e);
		}
	}

	public void assignToken(String lastName, String firstName, String defaultLogin, String emailAddress, String defaultShell, String tokenSerialNumber, String password) throws AceToolkitException {
		String userGuid = createUserObject(defaultLogin, firstName, lastName, emailAddress, password);
		createAMUser(userGuid, defaultShell);

		assignAnotherToken("-" + defaultLogin, tokenSerialNumber);
	}

	public void changeAuthWith(int tknAuthWith, String tokenSerialNumber) throws AceToolkitException {
//NOTE: not currently implemented. only used in tests, and seems we can safely noop this for now with 7.1.
//NOTE: The documentation says we can't set USE_PASSCODE or USE_TOKENCODE as it is only supported for read.
//		UpdateTokenCommand command = new UpdateTokenCommand();
//		TokenDTO token = getToken(tokenSerialNumber);
//		int pinType;
//		if(tknAuthWith == 0) {
//			pinType = TokenDTO.USE_PASSCODE;
//		} else if(tknAuthWith == 1) {
//			pinType = TokenDTO.USE_TOKENCODE;
//		} else {
//			throw new AceToolkitException("tknAuthWith only supports values of 0 or 1.");
//		}
//		token.setPinType(pinType);
//		command.setToken(token);
//		try {
//			executeWithRetry(command);
//		} catch (CommandException e) {
//			throw new AceToolkitException("Error occured while enabling token '" + tokenSerialNumber + "'.", e);
//		}
	}

	public void clearHistory() throws AceToolkitException {
		while(internalMonitorHistory() != null);
	}

	public void deleteUser(String defaultLogin) throws AceToolkitException {
		if(defaultLogin.startsWith("-")) { defaultLogin = defaultLogin.substring(1); }
		
		PrincipalDTO principal = getPrincipal(defaultLogin);
	    DeletePrincipalsCommand cmd = new DeletePrincipalsCommand();
	    cmd.setGuids(new String[] { principal.getGuid() } );
	    cmd.setIdentitySourceGuid( idSource.getGuid() );
	    try {
	    	executeWithRetry(cmd);
	    } catch (CommandException e) {
	    	throw new AceToolkitException("Error occured while deleting user '" + defaultLogin + "'.", e);
	    }
	}

	public void delLoginFromGroup(String defaultLogin, String groupName) throws AceToolkitException {
		PrincipalDTO principal = getPrincipal(defaultLogin);
		GroupDTO group = getGroup(groupName);
		
		UnlinkGroupPrincipalsCommand command = new UnlinkGroupPrincipalsCommand();
		command.setGroupGuids( new String[] { group.getGuid() });
		command.setPrincipalGuids( new String[] { principal.getGuid() });
		command.setIdentitySourceGuid(idSource.getGuid());
		
        try {
        	executeWithRetry(command);
        } catch (CommandException e) {
        	throw new AceToolkitException("Error occured while removing user '" + defaultLogin + "' from group '" + groupName + "'.", e);
        }
	}

	public void destroy() throws AceToolkitException {
		//Nothing to do really.
	}

	public void disableToken(String tokenSerialNumber) throws AceToolkitException {
		UpdateTokenCommand command = new UpdateTokenCommand();
		TokenDTO token = getToken(tokenSerialNumber);
		token.setTokenEnabled(false);
		command.setToken(token);
		try {
			executeWithRetry(command);
		} catch (CommandException e) {
			throw new AceToolkitException("Error occured while disabling token '" + tokenSerialNumber + "'.", e);
		}
	}

	public void emergencyAccessOff(String tokenSerialNumber) throws AceToolkitException {
		TokenDTO token = getToken(tokenSerialNumber);
		LookupTokenEmergencyAccessCommand command = new LookupTokenEmergencyAccessCommand();
		command.setGuid(token.getId());
		
		try {
			executeWithRetry(command);
		} catch (CommandException e) {
			throw new AceToolkitException("Error occured while while retrieving emergency access status for token '" + tokenSerialNumber + "'.", e);
		}

		TokenEmergencyAccessDTO tokenEmergencyAccess = command.getTokenEmergencyAccess();
		UpdateTokenEmergencyAccessCommand command2 = new UpdateTokenEmergencyAccessCommand();

		tokenEmergencyAccess.setEaMode(0);
		command2.setTokenEmergencyAccessDTO(tokenEmergencyAccess);
		try {
			executeWithRetry(command2);
		} catch (CommandException e) {
			throw new AceToolkitException("Error occured while while disabling emergency access for token '" + tokenSerialNumber + "'.", e);
		}
	}

	public String emergencyAccessOTP(String tokenSerialNumber, int number, int digits, int flags, int lifetime, String dateExpire, int hourExpire) throws AceToolkitException {
		throw new UnsupportedOperationException("emergencyAccessOTP is not supported in API 7.1.");
//		TokenDTO token = getToken(tokenSerialNumber);
//		LookupTokenEmergencyAccessCommand command = new LookupTokenEmergencyAccessCommand();
//		command.setGuid(token.getId());
//		
//		try {
//			executeWithRetry(command);
//		} catch (CommandException e) {
//			throw new AceToolkitException("Error occured while while retrieving emergency access status for token '" + tokenSerialNumber + "'.", e);
//		}
//
//		TokenEmergencyAccessDTO tokenEmergencyAccess = command.getTokenEmergencyAccess();
//		UpdateTokenEmergencyAccessCommand command2 = new UpdateTokenEmergencyAccessCommand();
//
//		tokenEmergencyAccess.setEaMode(2);
//		tokenEmergencyAccess.setN
//		command2.setTokenEmergencyAccessDTO(tokenEmergencyAccess);
//		try {
//			executeWithRetry(command2);
//		} catch (CommandException e) {
//			throw new AceToolkitException("Error occured while while disabling emergency access for token '" + tokenSerialNumber + "'.", e);
//		}
	}

	public void enableToken(String tokenSerialNumber) throws AceToolkitException {
		UpdateTokenCommand command = new UpdateTokenCommand();
		TokenDTO token = getToken(tokenSerialNumber);
		token.setTokenEnabled(true);
		command.setToken(token);
		try {
			executeWithRetry(command);
		} catch (CommandException e) {
			throw new AceToolkitException("Error occured while enabling token '" + tokenSerialNumber + "'.", e);
		}
	}

	public String getAceApiVersion() {
		return "71"; // This should match the number at the end of the class name.
	}

	public String getLibraryVersion() {
		URL path = null;
		try {
			Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);

			while (resources.hasMoreElements()) {
				URL url = (URL)resources.nextElement();
				if (url.getFile().contains("am-client.jar")) {
					path = url;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Error looking for " + JarFile.MANIFEST_NAME + " for am-client.jar.", e);
		}

//		Class clazz;
//		try {
//			clazz = Class.forName("com.rsa.authmgr.common.Action");
//		} catch(ClassNotFoundException e) {
//			return "";
//		}
//
//		String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
//		JarFile jar;
//		Manifest mf;
//		try {
//			jar = new JarFile(path);
//			mf = jar.getManifest();
//		} catch (IOException e) {
//			throw new RuntimeException("Unable to read " + JarFile.MANIFEST_NAME + " for am-client.jar.", e);
//		}

		Manifest mf;
		try {
			mf = new Manifest(path.openStream());
		} catch (IOException e) {
			throw new RuntimeException("Unable to read " + JarFile.MANIFEST_NAME + " for am-client.jar.", e);
		}

		Attributes rsaBuildInfo = mf.getAttributes("RSA-Build-Info");
		if (rsaBuildInfo != null) {
			return rsaBuildInfo.getValue("Module-Version");
		}

		rsaBuildInfo = mf.getAttributes("com/rsa/internal/am-client");
		if (rsaBuildInfo != null) {
			return rsaBuildInfo.getValue("Implementation-Version");
		}

		throw new RuntimeException("Manifest in am-client.jar does not have a RSA-Build-Info section.");
	}

	public List<String> getSerialByLogin(String defaultLogin, String number) throws AceToolkitException {
		int numSerials;
		try {
			numSerials = Integer.parseInt(number);
		} catch (NumberFormatException e) {
			throw new AceToolkitException("'" + number + "' is not a number.", e);
		}
		
		numSerials = (numSerials == 0) ? Integer.MAX_VALUE : numSerials;
		
		PrincipalDTO principal = getPrincipal(defaultLogin);
		ListTokensByPrincipalCommand command = new ListTokensByPrincipalCommand();
		command.setPrincipalId(principal.getGuid());
		
		try{
			executeWithRetry(command);
		} catch (CommandException e) {
			throw new AceToolkitException("Error occured while retreiving tokens for user '" + defaultLogin + "'.", e);
		}
		
		ListTokenDTO[] tokens = command.getTokenDTOs();
		if(tokens.length == 0) {
			return Collections.emptyList();
		} else {
			List<String> serials = new ArrayList<String>();
			for(ListTokenDTO token : tokens) {
				serials.add(token.getSerialNumber());
			}
			return serials;
		}
	}

	public boolean isEmergencyAccess(String tokenSerialNumber) throws AceToolkitException {
		return getTokenEmergencyAccess(tokenSerialNumber).isEmergencyAccessTokenCodeAllowed();
	}

	public List<String> listAllTokens() throws AceToolkitException {
		List<String> tokens = listAssignedTokens();
		tokens.addAll(listUnassignedTokens());
		
		return tokens;
	}

	public List<String> listAllUsers() throws AceToolkitException {
		return listAllUsersByDefaultLogin();
	}
	public List<String> listAllUsersByDefaultLogin() throws AceToolkitException {
		List<String> users = new ArrayList<String>();
		Filter filter = Filter.empty();  //default to an empty filter
		
		List<PrincipalDTO> results = searchUsers(filter);
		
		for(PrincipalDTO user : results) {
			users.add(user.getUserID());
		}
		
		return users;
	}
	
	public Map<String, String> listAllUsersByGUID() throws AceToolkitException {
		Map<String, String> users = new HashMap<String, String>();
		Filter filter = Filter.empty();  //default to an empty filter
		
		List<PrincipalDTO> results = searchUsers(filter);
		
		for(PrincipalDTO user : results) {
			users.put(user.getGuid(), user.getUserID());
		}
		
		return users;
	}

	public List<String> listAssignedTokens() throws AceToolkitException {
		List<String> tokens = new ArrayList<String>();
		Filter filter = Filter.empty();
		List<ListTokenDTO> assigned = searchTokens(filter, true); // unassigned tokens
		for(ListTokenDTO token : assigned) {
			tokens.add(token.getSerialNumber()); 
		}
		
		return tokens;
	}

	public Map<String,String> listExtensionsForUser(String tokenSerialOrLogin) throws AceToolkitException {
		if(!tokenSerialOrLogin.startsWith("-")) {
			throw new AceToolkitException("Assigning profiles using tokenSerial is not currently supported");
		}
		
		String defaultLogin = tokenSerialOrLogin.substring(1); // strip off the leading "-" used to identify login with older apis
		
		Map<String,String> extensions = new HashMap<String,String>();
		
		PrincipalDTO principal = getPrincipal(defaultLogin);
		AttributeDTO[] attributes = principal.getAttributes();
		for(int i = 0 ; i < attributes.length ; i++) {
			AttributeDTO attribute = attributes[i];
			if(attribute.getValues().length > 0) {
				extensions.put(attribute.getName(), attribute.getValues()[0].toString());
			}
		}
		
		return extensions;
	}

	public List listGroupMembership(String tokenSerialOrLogin) throws AceToolkitException{
		if(!tokenSerialOrLogin.startsWith("-")) {
			throw new AceToolkitException("Assigning profiles using tokenSerial is not currently supported");
		}
		
		String defaultLogin = tokenSerialOrLogin.substring(1); // strip off the leading "-" used to identify login with older apis
		
		PrincipalDTO user = getPrincipal(defaultLogin);
		GetPrincipalGroupsCommand command = new GetPrincipalGroupsCommand();
		command.setGuid(user.getGuid());
		command.setIdentitySourceGuid(idSource.getGuid());
		
		try {
			executeWithRetry(command);
		} catch(CommandException e) {
			throw new AceToolkitException("Error occured while while retrieving groups for user '" + defaultLogin + "'.", e);
		}
		
		GroupDTO[] groups = command.getGroups();
		List groupsList = new ArrayList();
		for(GroupDTO group : groups) {
			if(group.getSecurityDomainGuid().equals(domain.getGuid()) && !Boolean.parseBoolean(initParams.get(AceApi.APPEND_SITE_TO_ALL_GROUPS))) {
				groupsList.add(group.getName());
			} else {
				groupsList.add(group.getName() + "@" + getSecurityDomainByGuid(group.getSecurityDomainGuid()).getName());
			}
		}
		
		return groupsList;
	}

	public Map<String,Object> listTokenInfo(String tokenSerialNumber) throws AceToolkitException {
    	TokenDTO token = getToken(tokenSerialNumber);

        Map<String,Object> info = new HashMap<String,Object>();
        info.put(ATTR_SERIAL_NUM, token.getSerialNumber());
        info.put(ATTR_PIN_CLEAR, token.getPinIsSet() ? "TRUE" : "FALSE" );
        info.put(ATTR_NUM_DIGITS, Integer.toString(token.getTokenCodeLength()));
        info.put(ATTR_INTERVAL, Integer.toString(token.getTokenCodeInterval()));
        info.put(ATTR_BIRTH, new Long(token.getDateTokenStart().getTime() / 1000));
        info.put(ATTR_DEATH, new Long(token.getDateTokenShutdown().getTime() / 1000));
        info.put(ATTR_LAST_LOGIN, new Long( (token.getDateLastLogin() == null ? 0 : token.getDateLastLogin().getTime()) / 1000));
        info.put(ATTR_TYPE, Integer.toString(token.getTokenType()));
        info.put(ATTR_HEX, token.getHexDisplayed() ? "TRUE" : "FALSE");
        info.put(ATTR_DISABLED, !token.getTokenEnabled() ? "TRUE" : "FALSE");
        info.put(ATTR_NEW_PIN_MODE, token.getNewPinMode() ? "TRUE" : "FALSE");

        if(token.getAssignedUserId()!= null) {
            info.put(ATTR_ASSIGNED, "TRUE");
            try {
            	PrincipalDTO user = getPrincipal(token.getAssignedUserId());
                info.put(ATTR_USER_NUM, user.getGuid());
                info.put(ATTR_DEFAULT_LOGIN, user.getUserID());
            } catch (AceToolkitException e) {
            	//TODO: log this in the future. no logging facility in the API currently. This currently works around the fact that a user may be in another identity source.
            }
    		TokenEmergencyAccessDTO tokenEmergencyAccess = getTokenEmergencyAccess(tokenSerialNumber);
    		if(tokenEmergencyAccess != null) {
	            info.put(ATTR_EMERGENCY_ACCESS, Boolean.toString(tokenEmergencyAccess.isEmergencyAccessTokenCodeAllowed()).toUpperCase());
	           	if(tokenEmergencyAccess.getEacExpiresOn() != null) {
	           		info.put(ATTR_EAC_EXPIRES, new Long(tokenEmergencyAccess.getEacExpiresOn().getTime()/1000));
	           	}
	           	if(tokenEmergencyAccess.getEmergencyAccessTokenCode() != null && tokenEmergencyAccess.getEmergencyAccessTokenCode().length() > 0) {
	           		info.put(ATTR_EAC_PASSCODE, tokenEmergencyAccess.getEmergencyAccessTokenCode());
	           	}
    		}
        } else {
            info.put(ATTR_ASSIGNED, "FALSE");
        }
//TODO:        info.put(AceDriverShim.ATTR_NEXT_CODE_STATUS, token.getvalues[15]);
        info.put(ATTR_BAD_TOKEN_CODES, Integer.toString(token.getBadTokenCodeCount()));
//TODO:        info.put(AceDriverShim.ATTR_BAD_PINS, values[17]);
        info.put(ATTR_PIN_CHANGED_DATE, new Long( token.getDatePinModified() == null ? 0 : token.getDatePinModified().getTime() / 1000 ));
        info.put(ATTR_DISABLED_DATE, new Long( token.getDateEnableFlagModified() == null ? 0 : token.getDateEnableFlagModified().getTime() / 1000 ));
        info.put(ATTR_COUNTS_LAST_MODIFIED, new Long( (token.getDateCountsModified() == null ? 0 : token.getDateCountsModified().getTime()) / 1000));

       	info.put(ATTR_PROTECTED, token.getSoftIdCopyProtected() ? "TRUE" : "FALSE");
       	info.put(ATTR_DEPLOYED, token.getSoftIdDeployed() ? "TRUE" : "FALSE");
       	info.put(ATTR_COUNT, Integer.toString(token.getSoftIdCount()));
       	if(token.getSoftidPassword() != null && token.getSoftidPassword().length() > 0) {
           	info.put(ATTR_SOFT_PASSWORD, token.getSoftidPassword());
       	}
//TODO: Don't know where to get seed size.
//			info.put(AceDriverShim.ATTR_SEED_SIZE, values[29]);
		info.put(ATTR_KEYPAD, token.getKeyPad()?"TRUE":"FALSE");
		info.put(ATTR_LOCAL_PIN, token.getLocalPin()?"TRUE":"FALSE");
		info.put(ATTR_VERSION, Integer.toString(token.getAlgorithm())); // algorithm version
		info.put(ATTR_FORM_FACTOR, convertFormFactorToBinary(token.getFormFactor()));
       	info.put(ATTR_PIN_TYPE, Integer.toString(token.getPinType()));
       	if(token.getDateTokenAssigned() != null) {
       		info.put(ATTR_ASSIGNMENT, new Long(token.getDateTokenAssigned().getTime() / 1000));
       	}
       	info.put(ATTR_FIRST_LOGIN, token.getFirstLogin() ? "TRUE" : "FALSE");
//TODO: This doesn't seem to be available in 7.1
//			if (values[38].equals("UNDEFINED") == false && values[39].equals("UNDEFINED") == false) {
//			    try {
//			        info.put(AceDriverShim.ATTR_LAST_DA_CODE,
//			                new Long(TimeUtil.ctimeFromUTCDateAndSeconds(values[38], Integer.parseInt(values[39]))));
//			    } catch (ParseException e) {
//			        throw new AceDriverException("Error parsing LastDACode values.");
//			    }
//			}

       	return info;
	}

	public List<String> listUnassignedTokens() throws AceToolkitException {
		List<String> tokens = new ArrayList<String>();
		Filter filter = Filter.empty();
		List<ListTokenDTO> unassigned = searchTokens(filter, false); // unassigned tokens
		for(ListTokenDTO token : unassigned) {
			tokens.add(token.getSerialNumber()); 
		}
		
		return tokens;
	}

	public Map<String,Object> listUserInfo(String defaultLogin) throws AceToolkitException {
    	PrincipalDTO user;
    	AMPrincipalDTO amUser = null;

    	user = getPrincipal(defaultLogin);
		try {
			amUser = getAMPrincipal(user);
		} catch (AceToolkitException e) {
			// intentionally left blank
		}

	    Map<String,Object> info = new TreeMap<String,Object>();
	    
	    info.put(ATTR_USER_NUM, user.getGuid());
	    if (user.getLastName() != null) info.put(ATTR_LAST_NAME, user.getLastName().trim());
	    if (user.getFirstName() != null && user.getFirstName().length() > 0) info.put(ATTR_FIRST_NAME, user.getFirstName().trim());
	    if (user.getUserID() != null) info.put(ATTR_DEFAULT_LOGIN, user.getUserID().trim());
	    if (user.getEmail() != null) info.put(ATTR_EMAIL_ADDRESS, user.getEmail().trim());
	//TODO:token info?        if (values[4].length() != 0) info.put(AceDriverShim.ATTR_CREATE_PIN, values[4].equals("1")?"True":"False");
	//TODO:token info?        if (values[5].length() != 0) info.put(AceDriverShim.ATTR_MUST_CREATE_PIN, values[5].equals("1")?"True":"False");
	    info.put(ATTR_TEMP_USER, (user.getAccountExpireDate() != null)?"TRUE":"FALSE");
	
	    if(user.getAccountStartDate() != null) {
	    	info.put(ATTR_START, new Long(user.getAccountStartDate().getTime() / 1000));
	    }
	
	    if(user.getAccountExpireDate() != null) {
	    	info.put(ATTR_END, new Long(user.getAccountExpireDate().getTime() / 1000));
	    }
	    
	    if(amUser != null) {
	    	if (amUser.getDefaultShell() != null && amUser.getDefaultShell().length() > 0) info.put(ATTR_DEFAULT_SHELL, amUser.getDefaultShell().trim());
	    	if (amUser.getRadiusProfileGuid() != null) {
	    		info.put(ATTR_PROFILE_NAME, getRadiusProfileByGuid(amUser.getRadiusProfileGuid()).getName());
	    	}
	    }
	    
	
	    return info;
	}

	public List<String> listUsersByLogin(String defaultLogin) throws AceToolkitException {
		List<String> users = new ArrayList<String>();
		Filter filter = Filter.equal(PrincipalDTO.LOGINUID, defaultLogin);
		
		List<PrincipalDTO> results = searchUsers(filter);
		
		for(PrincipalDTO user : results) {
			users.add(user.getUserID());
		}
		
		return users;
	}

	public void newPin(String tokenSerialNumber) throws AceToolkitException {
		UpdateTokenCommand command = new UpdateTokenCommand();
		TokenDTO token = getToken(tokenSerialNumber);
		token.setNewPinMode(true);
		command.setToken(token);
		try {
			executeWithRetry(command);
		} catch(CommandException e) {
			throw new AceToolkitException("Error occured while setting new pin mode on token '" + tokenSerialNumber + "'.", e);
		}
	}
	
	public void registerUser(String defaultLogin) throws AceToolkitException {
		PrincipalDTO user = getPrincipal(defaultLogin);
		System.out.println("register: " + user.getGuid());
		RegisterPrincipalsCommand command = new RegisterPrincipalsCommand();

		command.setSecurityDomainGuid(domain.getGuid());
		command.setIdentitySourceGuid(idSource.getGuid());
		command.setPrincipalGuids(new String[] {user.getGuid()});
		try {
			executeWithRetry(command);
		} catch (CommandException e) {
			throw new AceToolkitException("Error occured while registering user '" + defaultLogin + "'.", e);
		}
		System.out.println(command.getRegisteredPrincipalGuids()[0]);
		System.out.println(command.getPrincipalGuids()[0]);
	}

	public void unregisterUser(String defaultLogin) throws AceToolkitException {
		PrincipalDTO user = getPrincipal(defaultLogin);
		System.out.println("unregister: " + user.getGuid());
		UnRegisterPrincipalsCommand command = new UnRegisterPrincipalsCommand();

		command.setGuids(new String[] {user.getGuid()});
		try {
			executeWithRetry(command);
		} catch (CommandException e) {
			throw new AceToolkitException("Error occured while registering user '" + defaultLogin + "'.", e);
		}
	}

	public void reinit() throws AceToolkitException {
		System.out.println("Reinitializing.......");
		System.out.println("Reinitializing.......");
		System.out.println("Reinitializing.......");
		setupSession();
	}
	
	public void replaceToken(String oldTokenSerialNumber, String newTokenSerialNumber, boolean resetPin) throws AceToolkitException {
		ReplaceTokensCommand command = new ReplaceTokensCommand();

		TokenDTO oldToken = getToken(oldTokenSerialNumber);
		TokenDTO newToken = getToken(newTokenSerialNumber);
		
		ReplaceTokenDTO replaceToken = new ReplaceTokenDTO();
		replaceToken.setReplacingTokenGuid(oldToken.getId()); // Replacing means "old". Go figure.
		replaceToken.setReplacementTokenGuid(newToken.getId());

		command.setRepTknDTO(new ReplaceTokenDTO[] {replaceToken});
		command.setSetNewPin(resetPin);
		
		try {
			executeWithRetry(command);
		} catch(CommandException e) {
			throw new AceToolkitException("Error occured while replacing token '" + oldTokenSerialNumber + "' with token '" + newTokenSerialNumber + "'.", e);
		}
	}

	public void rescindToken(String tokenSerialNumber, boolean revoke) throws AceToolkitException {
		if(revoke) {
			throw new AceToolkitException("Revoking software tokens is not supported by this implementation of rescindToken");
		}
		
		TokenDTO token = getToken(tokenSerialNumber);
		UnlinkTokensFromPrincipalsCommand command = new UnlinkTokensFromPrincipalsCommand();
		command.setTokenGuids(new String[] {token.getId()});
		
		try {
			executeWithRetry(command);
		} catch(CommandException e) {
			throw new AceToolkitException("Error occured while rescinding token '" + tokenSerialNumber + "'. " + e.getMessage(), e);
		}
		
	}

	public String setPin(String pin, String tokenSerialNumber) throws AceToolkitException {
		UpdateTokenCommand command = new UpdateTokenCommand();
		TokenDTO token = getToken(tokenSerialNumber);
		if(pin.equals("PIN")) {
			return token.getPinIsSet() ? "TRUE" : "FALSE";
		} else if (pin.equals("-1")) {
			throw new AceToolkitException("Setting a random pin by passing in -1 as the pin is not supported by API 7.1");
		} else {
			token.setPinType(TokenDTO.USE_PIN);
			token.setPin(pin);
			command.setToken(token);
			if("".equals(pin)) token.setPinIsSet(false);
			try {
				executeWithRetry(command);
			} catch(InvalidArgumentException e) { // This exception is thrown for 'Invalid token PIN'.
				throw new AceToolkitException(e.getMessage(), e);
			} catch(CommandException e) {
				throw new AceToolkitException("Token '" + tokenSerialNumber+ "' not found.", e);
			}
			
			return pin;
		}		
	}

	public void setPinToNTC(String tokenSerialNumber, String string) throws AceToolkitException {
		throw new UnsupportedOperationException("setPinToNTC is not implemented for API 7.1");
	}

	public void setTempUser(String dateStart, int hourStart, String dateEnd, int hourEnd, String tokenSerialOrLogin) throws AceToolkitException {
		if(!tokenSerialOrLogin.startsWith("-")) {
			throw new AceToolkitException("Setting temporary user using tokenSerial is not currently supported");
		}
		
		String defaultLogin = tokenSerialOrLogin.substring(1); // strip off the leading "-" used to identify login with older apis

		PrincipalDTO user = getPrincipal(defaultLogin);

		if(dateStart.trim().equals("") && hourStart == 0 && dateEnd.trim().equals("") && hourEnd == 0) {
			removeTempUser(user);
			return;
		}
		
		UpdatePrincipalCommand command = new UpdatePrincipalCommand();
        command.setIdentitySourceGuid(user.getIdentitySourceGuid());
    	
        UpdatePrincipalDTO updateDTO = new UpdatePrincipalDTO();
        updateDTO.setGuid(user.getGuid());

        // copy the rowVersion to satisfy optimistic locking requirements
        updateDTO.setRowVersion(user.getRowVersion());

        // collect all modifications here
        List mods = new ArrayList();
        ModificationDTO mod;

        // start time
		mod = new ModificationDTO();
        mod.setName(PrincipalDTO.START_DATE);
        if(dateStart.trim().equals("") && hourStart == 0) {
        	mod.setOperation(ModificationDTO.REMOVE_ATTRIBUTE);
		} else {
	        mod.setOperation(ModificationDTO.REPLACE_ATTRIBUTE);
	        mod.setValues(new Object[] { getTempUserDate(dateStart, hourStart) });
		}
        mods.add(mod); // add it to the list

        // end time
        mod = new ModificationDTO();
        mod.setOperation(ModificationDTO.REPLACE_ATTRIBUTE);
        mod.setName(PrincipalDTO.EXPIRATION_DATE);
        mod.setValues(new Object[] { getTempUserDate(dateEnd, hourEnd) });
        mods.add(mod); // add it to the list

        // set the requested updates into the UpdatePrincipalDTO
        updateDTO.setModifications((ModificationDTO[]) mods.toArray(new ModificationDTO[mods.size()]));
        command.setPrincipalModification(updateDTO);

        // perform the update
        try {
	        executeWithRetry(command);
        } catch(CommandException e) {
			throw new AceToolkitException("Error occured while modifying user '" + defaultLogin + "'.", e);
        }
	}

	public void setUser(String lastName, String firstName, String defaultLogin, String emailAddress, String defaultShell, String tokenSerialOrLogin, String password) throws AceToolkitException {
		if(!tokenSerialOrLogin.startsWith("-")) {
			throw new AceToolkitException("Modifying a user using tokenSerial is not currently supported");
		}
		
		String oldDefaultLogin = tokenSerialOrLogin.substring(1); // strip off the leading "-" used to identify login with older apis
		
		PrincipalDTO user = getPrincipal(oldDefaultLogin);
		modifyUserObject(user, firstName, lastName, emailAddress, defaultLogin, password);
		user = getPrincipal(defaultLogin);
		modifyAMUser(user, defaultShell);

	}

	public void unassignProfile(String tokenSerialOrLogin) throws AceToolkitException {
		if(!tokenSerialOrLogin.startsWith("-")) {
			throw new AceToolkitException("Unassigning profiles using tokenSerial is not currently supported");
		}
		
		String defaultLogin = tokenSerialOrLogin.substring(1); // strip off the leading "-" used to identify login with older apis

		PrincipalDTO principal = getPrincipal(defaultLogin);
		UnlinkPrincipalsFromProfileCommand command = new UnlinkPrincipalsFromProfileCommand();
		command.setPrincipalGuids(new String[] {principal.getGuid()} );
		command.setSecurityDomainGuid(domain.getGuid());
		
		try {
			executeWithRetry(command);
		} catch(CommandException e) {
			throw new AceToolkitException("Error unlinking user '" + defaultLogin + "' from it's profile.", e);
		}
	}

	public ActivityRecordDTO internalMonitorHistory() throws AceToolkitException {
		activityLog.add("Cached Events Size: " + cachedEvents.size());
   		try {
   			return cachedEvents .remove(); // return the next cached event if available.
   		} catch (NoSuchElementException e) {
   			// there were no cached events available. Moving on.
   		}

   		ActivityRecordDTO[] activities;
   		int countToRetrieve = 2;
   		int retrieveCount = 0;
   		do {
   			activityLog.add("Last Check Time = " + lastLogTime);
			ActivityMonitorCommand command = new ActivityMonitorCommand(Filter.empty(), countToRetrieve);
			command.setLogType(ActivityMonitorCommand.LOG_TYPE_ADM);
			command.setLastLogTime(dateToRsaDate(lastLogTime));
			
			try {
				executeWithRetry(command);
	   			retrieveCount = countToRetrieve;
			} catch(CommandException e) {
				throw new AceToolkitException("Error retrieving events.", e);
			}
			
			activities = command.getMessageDTO();

			if(activities == null) {
	   			activities = new ActivityRecordDTO[0];
	   		}
			
			for (ActivityRecordDTO item : activities) {
				activityLog.add("Activity Retrieved From RSA: " + item.getMessage().getValue());
			}
			
			countToRetrieve = countToRetrieve * 2;
   		} while(retrieveCount == activities.length);
   		

   		cachedEvents.addAll(Arrays.asList(activities));

   		try {
			//NOTE!!! do not use getUtcDateTimestamp(). It may be a date, but it can/will come back null. getUtcDateTime() will always come back.
   			if(activities.length > 0) {
   				// the date time needs to be normalized so it can be properly parsed
   				String rsaDateTime = normalizeRsaDateStr(activities[activities.length - 1].getUtcDateTime().getValue());
   				System.out.println("^^^^" + rsaDateTime);
   				lastLogTime = rsaDateFormat.parse(rsaDateTime);
   			}
		} catch (ParseException e) {
			throw new AceToolkitException("Unable to retrieve last log time from event. This may cause monitor history to stall.", e);
		}

		try {
			return cachedEvents.remove();// return the next cached event if available.
   		} catch (NoSuchElementException e) {
   			// there were no cached events available.
   			return null;
   		}
	}

	private String convertFormFactorToBinary(long formFactor) {
		String binaryString = Long.toString(formFactor << 33 >>> 33, 2);
		
		int numDigitsMissing = 32 - binaryString.length();
		
		char[] fill = new char[numDigitsMissing];
		Arrays.fill(fill, '0');
		
		return new String(fill) + binaryString;
	}

	private void createAMUser(String userGuid, String defaultShell) throws AceToolkitException {
	    AMPrincipalDTO principal = new AMPrincipalDTO();
	    principal.setGuid(userGuid);
	    principal.setDefaultShell(defaultShell);
	    principal.setDefaultUserIdShellAllowed(true);
	
	    AddAMPrincipalCommand command = new AddAMPrincipalCommand(principal);
	    try {
	    	executeWithRetry(command);
	    } catch (CommandException e) {
	    	throw new AceToolkitException("Error creating AM user for user with GUID '" + userGuid + "'.", e);
	    }
	}

	private String createUserObject(String defaultLogin, String firstName, String lastName, String emailAddress, String password) throws AceToolkitException {
		AddPrincipalsCommand command = new AddPrincipalsCommand();
		
		// the start date
		PrincipalDTO principal = new PrincipalDTO();
		principal.setUserID( defaultLogin );
		principal.setFirstName( firstName );
		principal.setLastName( lastName );
		
		principal.setEnabled(true);
		principal.setCanBeImpersonated(false);
		principal.setTrustToImpersonate(false);
		
		principal.setSecurityDomainGuid(domain.getGuid());
		principal.setIdentitySourceGuid(idSource.getGuid());

		principal.setPassword(password);
		principal.setPasswordExpired(false);
		
		principal.setEmail(emailAddress);
		
		command.setPrincipals( new PrincipalDTO[] { principal } );
		
		try {
			executeWithRetry(command);
		} catch (DuplicateDataException e) {
			throw new AceToolkitException(AceToolkitException.API_ERROR_USRALRINDB, "User '" + defaultLogin + "' already exists", e);
		} catch (CommandException e) {
			throw new AceToolkitException("Error occured while creating user '" + defaultLogin + "'.", e);
		}

		// only one user was created, there should be one GUID result
		return command.getGuids()[0];
	}
	
	private String dateToRsaDate(Date date) {
		return rsaDateFormat.format(date);
	}

	private AMPrincipalDTO getAMPrincipal(PrincipalDTO user) throws AceToolkitException {
		LookupAMPrincipalCommand command = new LookupAMPrincipalCommand();
		command.setGuid(user.getGuid());
		
		try {
			executeWithRetry(command);
		} catch(CommandException e) {
			throw new AceToolkitException("AMPrincipal not found for user '" + user.getUserID() + "'.", e);
		}
		
		return command.getAmp();
	}
	
	private GroupDTO getGroup(String groupName) throws AceToolkitException {
		SecurityDomainDTO groupDomain;
		if(groupName.indexOf("@") != -1) {
			String[] groupParts = groupName.split("@");

			if(groupParts.length != 2) {
				throw new AceToolkitException(String.format("Malformed group name '%s'.", groupName));
			}
			
			groupName = groupParts[0];
			String groupDomainName = groupParts[1];
			groupDomain = getSecurityDomain(groupDomainName);
			
		} else {
			groupDomain = domain;
		}
		
		SearchGroupsCommand command = new SearchGroupsCommand();
		command.setFilter(Filter.equal(GroupDTO.NAME, groupName));
		command.setSystemFilter(Filter.empty());
		command.setLimit(1);
		command.setIdentitySourceGuid(idSource.getGuid());
		command.setSecurityDomainGuid(groupDomain.getGuid());
		command.setSearchSubDomains(true);
		
		try {
			executeWithRetry(command);
		} catch (CommandException e) {
			throw new AceToolkitException("Error occured while retrieving group with name '" + groupName + "'.", e);
		}

		if (command.getGroups().length < 1) {
			throw new AceToolkitException("Group with name '" + groupName+ "' not found.");
		}

		return command.getGroups()[0];
	}

	private PrincipalDTO getPrincipal(String defaultLogin) throws AceToolkitException {
		if(defaultLogin.startsWith("-")) { defaultLogin = defaultLogin.substring(1); }
		
		SearchPrincipalsCommand command = new SearchPrincipalsCommand();
	
		// create a filter with the login UID equal condition
		command.setFilter(Filter.equal(PrincipalDTO.LOGINUID, defaultLogin));
		command.setSystemFilter(Filter.empty());
		command.setLimit(1);
		command.setIdentitySourceGuid(idSource.getGuid());
		command.setSecurityDomainGuid(domain.getGuid());
		command.setGroupGuid(null);
		command.setSearchSubDomains(true);
		try {
			executeWithRetry(command);
		} catch(CommandException e) {
			throw new AceToolkitException("Error occured while retrieving user with login '" + defaultLogin + "'.", e);
		}
	
		if (command.getPrincipals().length < 1) {
			throw new AceToolkitException(AceToolkitException.API_ERROR_INVUSR, "User with login '" + defaultLogin + "' not found.");
		}

		PrincipalDTO[] ret = command.getPrincipals();
		if (ret.length > 1) {
			StringBuffer sb = new StringBuffer("Multiple Principals Found!!");
			sb.append("Default Login = " + defaultLogin + "\r\n")
			.append("Principal Size = " + ret.length + "\r\n");
			for (int i = 0; i < ret.length; i++) {
				sb.append("GUID of Principal at index " + i + " = " + ret[i].getGuid() + "\r\n");
			}
			throw new AceToolkitException(AceToolkitException.API_ERROR_INVUSR, sb.toString());
		}
		return ret[0];
	}
	
	private RadiusProfileDTO getRadiusProfile(String profileName) throws AceToolkitException {
		ListRadiusProfilesCommand command = new ListRadiusProfilesCommand();
		command.setSecurityDomain(domain.getGuid());
		
		try {
			executeWithRetry(command);
		} catch (CommandException e) {
        	throw new AceToolkitException("Error occured while retreiving profiles for security domain.", e);
		}
		
		RadiusProfileDTO[] profiles = command.getRadiusProfiles();
		for(int i = 0 ; i < profiles.length ; i++) {
			RadiusProfileDTO profile = profiles[i];
			if(profile.getName().equals(profileName)) {
				return profile;
			}
		}

		throw new AceToolkitException("Profile '" + profileName + "' not found.");
	}
	
	private RadiusProfileDTO getRadiusProfileByGuid(String guid) throws AceToolkitException {
		ListRadiusProfilesCommand command = new ListRadiusProfilesCommand();
		command.setSecurityDomain(domain.getGuid());
		
		try {
			executeWithRetry(command);
		} catch (CommandException e) {
        	throw new AceToolkitException("Error occured while retreiving profiles for security domain.", e);
		}
		
		RadiusProfileDTO[] profiles = command.getRadiusProfiles();
		for(int i = 0 ; i < profiles.length ; i++) {
			RadiusProfileDTO profile = profiles[i];
			if(profile.getId().equals(guid)) {
				return profile;
			}
		}

		throw new AceToolkitException("Profile with guid '" + guid + "' not found.");
	}

    private RealmDTO getRealm(String realmName) throws AceToolkitException  {
        SearchRealmsCommand command = new SearchRealmsCommand();
        command.setFilter( Filter.equal( RealmDTO.NAME_ATTRIBUTE, realmName));
        command.setLimit(1);
        try {
			executeWithRetry(command);
		} catch (CommandException e) {
			throw new AceToolkitException("Error occured while retrieving realm with name '" + realmName + "'.", e);
		}
		
        if( command.getRealms().length < 1 ) {
            throw new AceToolkitException("Realm with name '" + realmName + "' not found.");
        }
        
        return command.getRealms()[0];
    }

    private SecurityDomainDTO getSecurityDomain(String securityDomainName) throws AceToolkitException {
        SearchSecurityDomainCommand command = new SearchSecurityDomainCommand();
        command.setFilter( Filter.equal( RealmDTO.NAME_ATTRIBUTE, securityDomainName));
        command.setLimit(1);
        try {
			executeWithRetry(command);
		} catch (CommandException e) {
			throw new AceToolkitException("Error occured while retrieving security domain with name '" + securityDomainName + "'.", e);
		}
		
        if( command.getSecurityDomains().length < 1 ) {
            throw new AceToolkitException("Realm with name '" + securityDomainName + "' not found.");
        }
        
        return command.getSecurityDomains()[0];
    }
    
    private SecurityDomainDTO getSecurityDomainByGuid(String guid) throws AceToolkitException {
        LookupSecurityDomainCommand command = new LookupSecurityDomainCommand();
        command.setGuid(guid);
        try {
			executeWithRetry(command);
		} catch (CommandException e) {
			throw new AceToolkitException("Error occured while retrieving security domain with guid '" + guid + "'.", e);
		}
		
        return command.getSecurityDomain();
    }
    
	private Date getTempUserDate(String dateStr, int hourStart) throws AceToolkitException {
		Calendar c = Calendar.getInstance();
        Date date;
		try {
			date = tempUserDateFormat.parse(dateStr);
		} catch (ParseException e) {
            throw new AceToolkitException("Failed to convert '" + dateStr + "' into a valid date.", e);
		}
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, hourStart);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
        return c.getTime();
	}

	private TokenDTO getToken(String serialNumber) throws AceToolkitException {
		LookupTokenCommand command = new LookupTokenCommand();
		
		command.setToken(new TokenDTO());
		command.setSerialNumber(serialNumber);

		try {
			executeWithRetry(command);
		} catch (CommandException e) {
			throw new AceToolkitException("Token '" + serialNumber + "' not found.", e);
		}

		return command.getToken();
	}

	private TokenEmergencyAccessDTO getTokenEmergencyAccess(String serialNumber) throws AceToolkitException {
		TokenDTO token = getToken(serialNumber);
		LookupTokenEmergencyAccessCommand command = new LookupTokenEmergencyAccessCommand();
		command.setGuid(token.getId());
		
		try {
			executeWithRetry(command);
		} catch (CommandException e) {
			throw new AceToolkitException("Error occured while while retrieving emergency access status for token '" + serialNumber + "'.", e);
		}
		
		TokenEmergencyAccessDTO tokenEmergencyAccess = command.getTokenEmergencyAccess();
		return tokenEmergencyAccess;

	}
	
	private void checkEnvironmentDependencies() throws AceToolkitException {
		List<String> missingJars = new ArrayList<String>();

		Map<String,String> dependencies;
		String libraryVersion = getLibraryVersion();
		if (libraryVersion.startsWith("7.") || libraryVersion.startsWith("am-7.1")) {
			dependencies = RSA7_REQUIRED_JAR_TO_CLASS_MAP;
		} else if (libraryVersion.startsWith("8.")) {
			dependencies = RSA8_REQUIRED_JAR_TO_CLASS_MAP;
		} else if (libraryVersion.length() == 0) {
			missingJars.add("am-client.jar");
			dependencies = Collections.emptyMap();
		} else {
			throw new AceToolkitException("Unsupported RSA API library version: " + getLibraryVersion());
		}

		for(Map.Entry<String,String> jarRequirementEntry : dependencies.entrySet()) {
			if(!classExists(jarRequirementEntry.getValue())) {
				missingJars.add(jarRequirementEntry.getKey());
			}
		}

		if(missingJars.size() > 0) {
			StringBuilder sb = new StringBuilder();
			Iterator<String> i = missingJars.iterator();
			if (i.hasNext()) {
				sb.append(i.next());
				while (i.hasNext()) {
					sb.append(", ").append(i.next());
				}
			}
		
			throw new AceToolkitException(String.format("The jar(s) %s seem to be missing. Please review the RSA driver installation instructions and confirm that the RSA jar files are correctly installed.", sb.toString()));
		}
	}
	
	public boolean classExists(String classToCheck) {
		try {
			Class.forName(classToCheck);
			return true;
		} catch(ClassNotFoundException e) {
			return false;
		}
	}
	
	private void init() throws AceToolkitException{
		if(initParams == null) {
			throw new AceToolkitException("API initialization parameters are null.");
		}

		String allowArraySyntax = System.getProperty(PROPERTY_ALLOW_ARRAY_SYNTAX);
		if(allowArraySyntax == null || Boolean.parseBoolean(allowArraySyntax) == false) {
			throw new AceToolkitException(String.format("The '%s' property is either false or not configured in your IDM installation. This value is required for RSA driver functionality. Please refer to the RSA driver documentation for configuration instructions.", PROPERTY_ALLOW_ARRAY_SYNTAX));
		}

		String weblogicLibDir = initParams.get(AceApi.WEBLOGIC_LIB_DIR);
		String rsaKeyStoreFile = initParams.get(AceApi.RSA_KEYSTORE_FILE);
		String serverUrl = initParams.get(AceApi.SERVER);
		String principal = initParams.get(AceApi.COMMAND_CLIENT_USERNAME);
		String credentials = initParams.get(AceApi.COMMAND_CLIENT_PASSWORD);
		String sessionUsername = initParams.get(AceApi.USERNAME);
		String sessionPassword = initParams.get(AceApi.PASSWORD);
		String realmName = initParams.get(AceApi.RSA_REALM);

		if(weblogicLibDir == null || serverUrl == null || principal == null || credentials == null ||
				sessionUsername == null || sessionPassword == null || realmName == null) {
			throw new AceToolkitException("This driver must be configured with server, user, password, commandClientUsername, commandClientPassword, weblogicLibDir, and rsaRealm.");
		}

		System.setProperty("bea.home", weblogicLibDir);
		if (rsaKeyStoreFile == null || rsaKeyStoreFile.length() == 0) {
			System.setProperty("UseSunHttpHandler", "true");
		} else {
			System.setProperty("weblogic.security.SSL.trustedCAKeyStore", rsaKeyStoreFile);
			System.setProperty("javax.net.ssl.trustStore", rsaKeyStoreFile);
		}

		setupSession();
	}

	private void setupSession() throws AceToolkitException {
		String identitySourceName = initParams.get(AceApi.RSA_IDENTITY_SOURCE);
		String realmName = initParams.get(AceApi.RSA_REALM);

		Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY_CLASS);
        properties.put(Context.PROVIDER_URL, initParams.get(AceApi.SERVER));
        properties.put(Context.SECURITY_PRINCIPAL, initParams.get(AceApi.COMMAND_CLIENT_USERNAME));
        properties.put(Context.SECURITY_CREDENTIALS, initParams.get(AceApi.COMMAND_CLIENT_PASSWORD));

        Connection conn = ConnectionFactory.getConnection(properties);
		try {
			session = conn.connect(initParams.get(AceApi.USERNAME), initParams.get(AceApi.PASSWORD));
        } catch (AuthenticationCommandException e) {
            if (e.getMessageKey().equals("ACCESS_DENIED")) {
                throw new AceToolkitException("Errror ('"+e.getMessage()+"') connecting to RSA server. The password for '" + initParams.get(AceApi.USERNAME) + "' may be expired or incorrect.");
            }
            throw new AceToolkitException("Error '" +  e.getMessage() + "' connecting to RSA server.", e);
		} catch (CommandException e) {
            throw new AceToolkitException("Error '" +  e.getMessage() + "' connecting to RSA server.", e);
		}

        // make all commands execute using this target automatically
        //CommandTargetPolicy.setDefaultCommandTarget(session);

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
	        	if(initParams.get(AceApi.RSA_IDENTITY_SOURCE).equals(identitySource.getName())) {
	        		idSource = identitySource;
	        		break;
	        	}
	        	separator = ", ";
	        }

	        if(idSource == null) {
	        	throw new AceToolkitException(String.format("Identity source '%s' does not match available identity source(s) '%s' in realm '%s'.", identitySourceName, identitySourceNames.toString(), realmName));
	        }
		}
	}
	
	private void modifyAMUser(PrincipalDTO user, String defaultShell) throws AceToolkitException {
		AMPrincipalDTO amPrincipal = getAMPrincipal(user);
		amPrincipal.setDefaultShell(defaultShell);
		
		UpdateAMPrincipalCommand command = new UpdateAMPrincipalCommand();
		command.setAmp(amPrincipal);
		
		try {
			executeWithRetry(command);
		} catch (CommandException e) {
			
		}
	
	}

	private void modifyUserObject(PrincipalDTO user, String firstName, String lastName, String emailAddress, String defaultLogin, String password) throws AceToolkitException {
		UpdatePrincipalCommand command = new UpdatePrincipalCommand();
        command.setIdentitySourceGuid(user.getIdentitySourceGuid());

        UpdatePrincipalDTO updateDTO = new UpdatePrincipalDTO();
        updateDTO.setGuid(user.getGuid());

        // copy the rowVersion to satisfy optimistic locking requirements
        updateDTO.setRowVersion(user.getRowVersion());

        // collect all modifications here
        List mods = new ArrayList();
        ModificationDTO mod;

        // lastName
        mod = new ModificationDTO();
        mod.setOperation(ModificationDTO.REPLACE_ATTRIBUTE);
        mod.setName(PrincipalDTO.LAST_NAME);
        mod.setValues(new Object[] { lastName });
        mods.add(mod); // add it to the list

        // firstName
        mod = new ModificationDTO();
        mod.setOperation(ModificationDTO.REPLACE_ATTRIBUTE);
        mod.setName(PrincipalDTO.FIRST_NAME);
        mod.setValues(new Object[] { firstName });
        mods.add(mod); // add it to the list

        // password
        if(password != null) {
	        mod = new ModificationDTO();
	        mod.setOperation(ModificationDTO.REPLACE_ATTRIBUTE);
	        mod.setName(PrincipalDTO.PASSWORD);
	        mod.setValues(new Object[] {password});
	        mods.add(mod);
        }
        
        // defaultLogin
        if(user.getUserID().toLowerCase().equals(defaultLogin.toLowerCase()) == false) {
	        mod = new ModificationDTO();
	        mod.setOperation(ModificationDTO.REPLACE_ATTRIBUTE);
	        mod.setName(PrincipalDTO.LOGINUID);
	        mod.setValues(new Object[] { defaultLogin });
	        mods.add(mod); // add it to the list
        }
        
        //emailAddress
        if(emailAddress != null) {
        	mod = new ModificationDTO();
        	mod.setOperation(ModificationDTO.REPLACE_ATTRIBUTE);
        	mod.setName(PrincipalDTO.EMAIL);
        	mod.setValues(new Object[] {emailAddress});
        	mods.add(mod);
        }
        
        // set the requested updates into the UpdatePrincipalDTO
        updateDTO.setModifications((ModificationDTO[]) mods.toArray(new ModificationDTO[mods.size()]));
        command.setPrincipalModification(updateDTO);

        // perform the update
        try {
	        executeWithRetry(command);
        } catch(CommandException e) {
			throw new AceToolkitException("Error occured while modifying user '" + defaultLogin + "'.", e);
        }
	}

	private String normalizeRsaDateStr(String rsaDateTime) throws AceToolkitException {
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
		
		throw new AceToolkitException("Error normalizing rsa event date time.");
	}

	private void removeTempUser(PrincipalDTO user) throws AceToolkitException {
		UpdatePrincipalCommand command = new UpdatePrincipalCommand();
        command.setIdentitySourceGuid(user.getIdentitySourceGuid());
    	
        UpdatePrincipalDTO updateDTO = new UpdatePrincipalDTO();
        updateDTO.setGuid(user.getGuid());

        // copy the rowVersion to satisfy optimistic locking requirements
        updateDTO.setRowVersion(user.getRowVersion());

        // collect all modifications here
        List mods = new ArrayList();
        ModificationDTO mod;

        // start time
        mod = new ModificationDTO();
        mod.setOperation(ModificationDTO.REMOVE_ATTRIBUTE);
        mod.setName(PrincipalDTO.START_DATE);
        mods.add(mod); // add it to the list

        // end time
        mod = new ModificationDTO();
        mod.setOperation(ModificationDTO.REMOVE_ATTRIBUTE);
        mod.setName(PrincipalDTO.EXPIRATION_DATE);
        mods.add(mod); // add it to the list

        // set the requested updates into the UpdatePrincipalDTO
        updateDTO.setModifications((ModificationDTO[]) mods.toArray(new ModificationDTO[mods.size()]));
        command.setPrincipalModification(updateDTO);

        // perform the update
        try {
	        executeWithRetry(command);
        } catch(CommandException e) {
			throw new AceToolkitException("Error occured while modifying user '" + user.getUserID() + "'.", e);
        }
		
	}
	
	private List<ListTokenDTO> searchTokens(Filter filter, boolean assigned) throws AceToolkitException {
		List tokens = new ArrayList();
		final int limit = 100;
		
		// unassigned tokens
		SearchTokensCommand command = new SearchTokensCommand();
		command.setAssigned(assigned);
		command.setLimit(limit);
		command.setFilter(filter);
		
		int firstResult = 0;
		ListTokenDTO[] results = null;
		do {
			command.setFirstResult(firstResult);
			try {
				executeWithRetry(command);
			} catch (CommandException e) {
				throw new AceToolkitException("Error retrieving tokens.", e);
			}
			results = command.getListSecurIDTokens();
			
			for (int tokenNum = 0 ; tokenNum < results.length ; tokenNum ++) {
				tokens.add(results[tokenNum]);
			}
			
			firstResult += limit;
		} while (results.length > 0);

		return tokens;
	}
	
	public List<PrincipalDTO> searchUsers(Filter filter) throws AceToolkitException {
		List<PrincipalDTO> users = new ArrayList<PrincipalDTO>();
		final int limit = 100;
		
		SearchPrincipalsIterativeCommand command = new SearchPrincipalsIterativeCommand();
		command.setIdentitySourceGuid(idSource.getGuid());
		command.setLimit(limit);
		command.setFilter(filter);
		
		int firstResult = 0;
		PrincipalDTO[] results = null;
		try {
			do {
				try {
					executeWithRetry(command);
				} catch (CommandException e) {
					throw new AceToolkitException("Error retrieving users.", e);
				}
				results = command.getPrincipals();
				
				for (int userNum = 0 ; userNum < results.length ; userNum ++) {
					users.add(results[userNum]);
				}
				
				firstResult += limit;
			} while (results.length > 0);
		} finally {
			//end the search
			EndSearchPrincipalsIterativeCommand endSearch = new EndSearchPrincipalsIterativeCommand();
			endSearch.setSearchContextId(command.getSearchContextId());
			try {
				executeWithRetry(endSearch);
			} catch (CommandException e) {
				throw new AceToolkitException("Error closing user search result set.", e);
			}
		}

		return users;
	}

	private void executeWithRetry(TargetableCommand command) throws CommandException, SystemException, AceToolkitException {
		try {
			command.execute(session);
		} catch(com.rsa.command.InvalidSessionException e) {
			setupSession();
			command.execute(session);
		} catch(com.rsa.session.InvalidSessionException e) {
			setupSession();
			command.execute(session);
		}
	}

	String getPrincipalGuid(String defaultLogin) throws AceToolkitException {
		return getPrincipal(defaultLogin).getGuid();
	}
	
	static SimpleDateFormat getRsaDateFormat() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		return df;
	}

	private static final SimpleDateFormat dateFormat = AceApi71.getRsaDateFormat();

	public AceApiEvent monitorHistory() throws AceEventException {
    	ActivityRecordDTO event;
		try {
    		event = internalMonitorHistory();
    		return map(event);
		} catch (AceToolkitException e) {
           	try {
               	reinit();
	            event = internalMonitorHistory();
	            return map(event);
           	} catch (AceToolkitException e2) {
    			throw new AceEventException("ERROR", "Unable to retrieve event for mapping. Exception on retry.", e2);
           	}
		}
	}

	private AceApiEvent map(ActivityRecordDTO eventDto) throws AceEventException {
		// Example values for eventDTO:
		// obj1guid = 14c6bb53650211ac02cc70caeff2cff0, obj1SrcName=AD, obj1Name = fredflint, obj1SecDomName = SystemDomain, obj1Type = 3, obj2guid=SYSTEM, obj2Name=UNKNOWN
		if(eventDto == null) {
			AceApiEvent event = new AceApiEvent();
			event.setType(AceApiEventType.TERMINATOR);
			event.setInfo("TERMINATOR");
			event.setDescription("No events available");
			event.setDate(new Date());
			return event;
		}

		String eventText = eventDto.getActionKey().getValue();
		String info = eventText;
		String description = eventDto.getActivity().getValue();

		Date date;
		try {
			date = dateFormat.parse(eventDto.getUtcDateTime().getValue());
		} catch (ParseException e) {
			throw new AceEventException(description, "Unable to map event - date '" + eventDto.getUtcDateTime().getValue() + "' is invalid.");
		}

		AceApiEventType eventType = (AceApiEventType) eventMap.get(eventText);

		if(eventType == null) {
			if(unmappedEvents.contains(eventText)) {
				eventType = AceApiEventType.IGNORED;
			} else {
				eventType = AceApiEventType.UNKNOWN;
			}
		}

		AceApiEvent event = new AceApiEvent();
		event.setType(eventType);
		event.setInfo(info);
		event.setDescription(description);
		event.setDate(date);

		if (eventType == AceApiEventType.USER_ADD || eventType == AceApiEventType.USER_DELETE || eventType == AceApiEventType.USER_RADIUS_MODIFY) {
			event.setUser(eventDto.getObj1Name().getValue());
		} else if (eventType == AceApiEventType.USER_MODIFY) {
			event.setUser(eventDto.getObj1Name().getValue());
			try {
				event.setUserGuid(getPrincipalGuid(eventDto.getObj1Name().getValue()));
			} catch (AceToolkitException e) {
				throw new AceEventException(event.getDescription(),String.format("Error retrieving user GUID for user '%s'.", eventDto.getObj1Name().getValue()), e);
			}
		} else if (eventType == AceApiEventType.TOKEN_ASSIGNMENT_MODIFY) {
			event.setTokenSerial(eventDto.getObj1Name().getValue());
			event.setUser(eventDto.getObj2Name().getValue());
		} else if (eventType == AceApiEventType.TOKEN_MODIFY || eventType == AceApiEventType.TOKEN_DELETE) {
			event.setTokenSerial(eventDto.getObj1Name().getValue());
		} else if (eventType == AceApiEventType.GROUP_MODIFY) {
			event.setGroup(eventDto.getObj1Name().getValue());
			event.setUser(eventDto.getObj2Name().getValue());
		}

		return event;
	}

	@SuppressWarnings("serial")
	private static final Map<String,AceApiEventType> eventMap = new HashMap<String,AceApiEventType>(){{
		put("Associate group with principal", AceApiEventType.GROUP_MODIFY);
		put("Clear Token Pin", AceApiEventType.TOKEN_MODIFY);
		put("Create principal", AceApiEventType.USER_ADD);
		put("Delete principal", AceApiEventType.USER_DELETE);
		put("Delete Token", AceApiEventType.TOKEN_DELETE);
		put("Disable Token", AceApiEventType.TOKEN_MODIFY);
		put("Disassociate Group from Principal", AceApiEventType.GROUP_MODIFY);
		put("Enable Token", AceApiEventType.TOKEN_MODIFY);
		put("Link Token with Principal", AceApiEventType.TOKEN_ASSIGNMENT_MODIFY);
		put("Manage RADIUS Attribute Values", AceApiEventType.USER_RADIUS_MODIFY);
		put("Register principal", null);
		put("Set New Pin Mode", AceApiEventType.TOKEN_MODIFY);
		put("TERMINATOR", AceApiEventType.TERMINATOR);
		put("Unlink Token with Principal", AceApiEventType.TOKEN_ASSIGNMENT_MODIFY);
		put("Unregister principal", null);
		put("Update principal", AceApiEventType.USER_MODIFY);
		put("Update Principal", AceApiEventType.USER_MODIFY); //RSA ticks me off sometimes. Just a case difference with the previous event.
		put("Update Token", AceApiEventType.TOKEN_MODIFY);
	}};
	
	@SuppressWarnings("serial")
	private static final Set<String> unmappedEvents = new HashSet<String>() {{
		add("Create security domain");
	}};

	public synchronized List<String> sweepLog() {
		if (activityLog == null) activityLog = new ArrayList<String>();
		List<String> ret = new ArrayList<String>(activityLog);
		activityLog.clear();
		return ret;
	}
	
	//TODO:REMOVE!!!
	public static void writeLog(String logEvent, boolean logIt) {
		if(!logIt) return;
		
		try{
			// Create file 
			FileWriter fstream = new FileWriter("c:/rsadebug.log",true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(logEvent + "\r\n");
			//Close the output stream
			out.close();
		}catch (Exception e){//Catch exception if any
		}
	}
}