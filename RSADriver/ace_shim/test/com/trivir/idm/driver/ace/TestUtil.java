package com.trivir.idm.driver.ace;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.trivir.ace.AceToolkitException;
import com.trivir.ace.api.AceApi;
import com.trivir.ace.api.v71.AceApi71;

@SuppressWarnings("serial")
public class TestUtil {
	public static final String DEFAULT_USER_PASSWORD = "Trivir1!";

	private static final Map<String,String> authParams71 = new HashMap<String,String>() {{
		put(AceApi.SERVER, "t3s://172.17.2.101:7002");
		put(AceApi.USERNAME, "idmuser");
		put(AceApi.PASSWORD, "Trivir7&");
	}};

	private static final Map<String,String> driverParams71 = new HashMap<String,String>() {{
		put(AceApi.API_VERSION, "71");
		put(AceApi.COMMAND_CLIENT_USERNAME, "CmdClient_dbfnwyrl");
		put(AceApi.COMMAND_CLIENT_PASSWORD, "Mxb1W8VkAd");
		put(AceApi.RSA_REALM, "SystemDomain");
//		put(AceApi.WEBLOGIC_LIB_DIR,"c:/novell/nds/lib");
//		put(AceApi.RSA_KEYSTORE_FILE,"c:/novell/nds/lib/trust.jks");
		put(AceApi.WEBLOGIC_LIB_DIR, System.getProperty("user.dir") + "/../jace/lib/rsa7");
		put(AceApi.RSA_KEYSTORE_FILE, System.getProperty("user.dir") + "/../jace/lib/rsa7/trust.jks");
//		put(AceApi.RSA_IDENTITY_SOURCE, "AD"); // "Internal Database");
//		put(AceApi.APPEND_SITE_TO_ALL_GROUPS, "true");
	}};

	private static final Map<String,String> allParams71 = new HashMap<String,String>() {{
		putAll(authParams71);
		putAll(driverParams71);
	}};

	private static final Map<String, String> authParams8 = new HashMap<String, String>() {
		{
			put(AceApi.SERVER, "t3s://172.17.2.102:7002");
			put(AceApi.USERNAME, "idmuser");
			put(AceApi.PASSWORD, "Trivir7&");
		}
	};
	
	private static final Map<String, String> authParams8_1 = new HashMap<String, String>() {
		{
			put(AceApi.SERVER, "t3s://172.17.2.104:7002");
			put(AceApi.USERNAME, "idmuser");
			put(AceApi.PASSWORD, "Trivir5%");
		}
	};

	private static final Map<String, String> driverParams8 = new HashMap<String, String>() {
		{
			put(AceApi.API_VERSION, "71");
			put(AceApi.COMMAND_CLIENT_USERNAME, "CmdClient_rpirxfgx");
			put(AceApi.COMMAND_CLIENT_PASSWORD, "FhpcHW65eFjbLFevNledRtIV70fJMv");
			put(AceApi.RSA_REALM, "SystemDomain");
			put(AceApi.WEBLOGIC_LIB_DIR, System.getProperty("user.dir") + "/../jace/lib/rsa8");
			put(AceApi.RSA_KEYSTORE_FILE, System.getProperty("user.dir") + "/../jace/lib/rsa8/trust.jks");
			// put(AceApi.RSA_IDENTITY_SOURCE, "AD");
			// put(AceApi.APPEND_SITE_TO_ALL_GROUPS, "true");
		}
	};
	
	private static final Map<String, String> driverParams8_1 = new HashMap<String, String>() {
		{
			put(AceApi.API_VERSION, "71");
			put(AceApi.COMMAND_CLIENT_USERNAME, "CmdClient_5dt475d8");
			put(AceApi.COMMAND_CLIENT_PASSWORD, "IzMO8RTWBcS6vhjhlgeuPyg5fQThzT");
			put(AceApi.RSA_REALM, "SystemDomain");
			put(AceApi.WEBLOGIC_LIB_DIR, System.getProperty("user.dir") + "/../jace/lib/rsa8_1");
			put(AceApi.RSA_KEYSTORE_FILE, System.getProperty("user.dir") + "/../jace/lib/rsa8_1/trust.jks");
			// put(AceApi.RSA_IDENTITY_SOURCE, "AD");
			// put(AceApi.APPEND_SITE_TO_ALL_GROUPS, "true");
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

	private static final Map<String, String> params;
	private static final Map<String, String> authParams;
	private static final Map<String, String> driverParams;

	private static final String[] RSA7_tokenSerialNumbers = {"000042414137", "000042414138"};
	private static final String[] RSA8_tokenSerialNumbers = {"000118815846", "000118815847"};
	private static final String[] RSA8_1_tokenSerialNumbers = {"000157228469", "000157228470"};
	public static final String tokenSerialNum;
	public static final String tokenSerialNum2;
	public static final String tokenExpiration;
	public static final int tokenCount;

	private static final int SERVER_VERSION = 81;

	static {
		if (SERVER_VERSION == 7) {
			params = allParams71;
			authParams = authParams71;
			driverParams = driverParams71;

			tokenSerialNum = RSA7_tokenSerialNumbers[0];
			tokenSerialNum2 = RSA7_tokenSerialNumbers[1];
			tokenExpiration = "1490918400";
			tokenCount = 6700;
		} else if (SERVER_VERSION == 8){
			params = allParams8;
			authParams = authParams8;
			driverParams = driverParams8;

			tokenSerialNum = RSA8_tokenSerialNumbers[0];
			tokenSerialNum2 = RSA8_tokenSerialNumbers[1];
			tokenExpiration = "1406764800";
			tokenCount = 5;
		}
		else {
			params = allParams8_1;
			authParams = authParams8_1;
			driverParams = driverParams8_1;

			tokenSerialNum = RSA8_1_tokenSerialNumbers[0];
			tokenSerialNum2 = RSA8_1_tokenSerialNumbers[1];
			tokenExpiration = "1559260800";
			tokenCount = 10;
		}
	}

	public static AceApi71 getApi() throws AceToolkitException {
		AceApi71 ret = new AceApi71(params);
		//try { Thread.sleep(5000); } catch (InterruptedException e) {}
		return ret;
	}

	public static Map<String,String> getAuthParams() {
		return authParams;
	}

	public static Map<String,String> getDriverParams() {
		return driverParams;
	}

	private static Connection createCacheConnection(String cacheName) {
		final String DB_BASE_NAME = "rsaCache";
		final String DB_URL_TEMPLATE = "jdbc:hsqldb:file:%s-%s";
	
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException e) {
	        throw new RuntimeException("Error loading HSQL JDBC driver.", e);
		}
	
		try {
			return DriverManager.getConnection(String.format(DB_URL_TEMPLATE, DB_BASE_NAME, cacheName) , "sa", "");
		} catch (SQLException e) {
	        throw new RuntimeException("Error connecting to cache database.", e);
		}
	}

	private static void close(Connection cacheConnection) throws SQLException {
    	if(cacheConnection != null) {
			Statement st = cacheConnection.createStatement();
			String sql = "SHUTDOWN COMPACT";
			st.execute(sql);

			cacheConnection.close();
    	}
	}

	private static final String DEFAULT_CACHE_NAME = "RSA Driver";
	static void dropCacheDatabase() throws SQLException {
		dropCacheDatabase(DEFAULT_CACHE_NAME);
	}

	static void dropCacheDatabase(String cacheName) throws SQLException {
		Connection cacheConnection = createCacheConnection(cacheName);

		try {
			Statement st = cacheConnection.createStatement();
			st.executeUpdate("DROP SCHEMA PUBLIC CASCADE");
			st.close();
		} finally {
			close(cacheConnection);
		}    	
	}

	static void initializeCache() throws AceToolkitException, DriverObjectCacheException, AceDriverException {
		initializeCache(DEFAULT_CACHE_NAME);
	}

	static void initializeCache(String cacheName) throws AceToolkitException, DriverObjectCacheException, AceDriverException {
        AceApi71 api = TestUtil.getApi();
        DataModel model = new DataModel(api, cacheName, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    	Map<String, String[]> filter = new HashMap<String, String[]>();
        filter.put("User", TestPublisher.userFilter);
        filter.put("Token", TestPublisher.tokenFilter);
        model.setFilter(filter);
        while(!model.init()) {}
        model.close();
        api.destroy();
	}

	public static String getDocumentAsString(Document doc) throws TransformerConfigurationException, TransformerException  {
	    TransformerFactory tFactory = TransformerFactory.newInstance();
	    Transformer transformer = tFactory.newTransformer();
	    DOMSource source = new DOMSource(doc);
	    StringWriter sw = new StringWriter();
	    StreamResult result = new StreamResult(sw);        
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	    transformer.transform(source, result);
	    return sw.toString();
	}

	public static void printDocumentToScreen(String testName, Document doc) throws TransformerConfigurationException, TransformerException  {
	    TransformerFactory tFactory = TransformerFactory.newInstance();
	    Transformer transformer = tFactory.newTransformer();
	    DOMSource source = new DOMSource(doc);
	    StreamResult result = new StreamResult(System.out);
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	    System.out.println("========================================================================================================================================");
	    System.out.println("Test: [" + testName + "]");
	    transformer.transform(source, result);
	}

	static String xmlToString(Node node) throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StringWriter stringWriter = new StringWriter(128);
		transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
		StringBuffer buffer = stringWriter.getBuffer();
		return buffer.toString();
	}
}
