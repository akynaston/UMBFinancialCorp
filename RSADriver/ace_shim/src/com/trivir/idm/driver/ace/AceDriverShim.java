package com.trivir.idm.driver.ace;

import java.io.File;
//import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.HashMap;
//import java.util.HashSet;
import java.util.Iterator;
//import java.util.LinkedList;
import java.util.List;
import java.util.Map;
//import java.util.Set;
//import java.util.Vector;

import com.novell.nds.dirxml.driver.DriverShim;
import com.novell.nds.dirxml.driver.PublicationShim;
import com.novell.nds.dirxml.driver.SubscriptionShim;
import com.novell.nds.dirxml.driver.Trace;
import com.novell.nds.dirxml.driver.XmlDocument;
import com.novell.nds.dirxml.driver.xds.NonXDSElement;
import com.novell.nds.dirxml.driver.xds.StatusLevel;
import com.novell.nds.dirxml.driver.xds.StatusType;
import com.novell.nds.dirxml.driver.xds.ValueType;
import com.novell.nds.dirxml.driver.xds.WriteableDocument;
import com.novell.nds.dirxml.driver.xds.XDSAttrDefElement;
import com.novell.nds.dirxml.driver.xds.XDSAuthenticationInfoElement;
import com.novell.nds.dirxml.driver.xds.XDSClassDefElement;
import com.novell.nds.dirxml.driver.xds.XDSInitDocument;
import com.novell.nds.dirxml.driver.xds.XDSInitParamsElement;
import com.novell.nds.dirxml.driver.xds.XDSParseException;
import com.novell.nds.dirxml.driver.xds.XDSProductElement;
import com.novell.nds.dirxml.driver.xds.XDSResultDocument;
import com.novell.nds.dirxml.driver.xds.XDSSchemaDefElement;
import com.novell.nds.dirxml.driver.xds.XDSSchemaResultDocument;
import com.novell.nds.dirxml.driver.xds.XDSSourceElement;
import com.novell.nds.dirxml.driver.xds.XDSStatusElement;
import com.novell.nds.dirxml.driver.xds.util.XDSUtil;
import com.rsa.admin.data.PrincipalDTO;
import com.rsa.common.search.Filter;
import com.trivir.ace.api.AceApi;
import com.trivir.ace.api.v71.AceApi71;
import com.trivir.util.Version;

@SuppressWarnings("unchecked")
public class AceDriverShim implements DriverShim {
    static final String CLASS_USER = "User";

    static final String CLASS_TOKEN = "Token";

    private Trace initTrace;
	private Trace trace;
	
	private String driverRDN;

    private SubscriberShim subscriberShim;
	private PublisherShim publisherShim;
    private List<String> tokenExtensions = new ArrayList<String>();
    private List<String> userExtensions = new ArrayList<String>();
    DataModel rsaData;
    private Map<String,String> aceParams = new HashMap<String,String>();

	public AceDriverShim() {
		initTrace = new Trace("RSA");
	}

	public PublicationShim getPublicationShim() {
		return publisherShim;
	}

    //example initialization document:
	/*
    <nds dtdversion="1.1" ndsversion="8.6">
        <source>
            <product version="1.1a">DirXML</product>
            <contact>Novell, Inc.</contact>
        </source>
        <input>
            <init-params src-dn="\NEW_DELL_TREE\NOVELL\Driver Set\Skeleton Driver (Java, XDS)">
                <authentication-info>
                    <server>server.app:400</server>
                    <user>User1</user>
                </authentication-info>
                <driver-options>
		            <option-1 display-name="Sample String option">This is a string</option-1>
		            <option-2 display-name="Sample int option (enter an integer)">10</option-2>
		            <option-3 display-name="Sample boolean option (enter a boolean value)">true</option-3>
		            <option-4 display-name="Sample required option (enter some value)">not null</option-4>
		        </driver-options>
                <subscriber-options>
		            <sub-1 display-name="Sample Subscriber option">String for Subscriber</sub-1>
		        </subscriber-options>
                <publisher-options>
			        <pub-1 display-name="Sample Publisher option">String for Publisher</pub-1>
			        <polling-interval display-name="Polling interval in seconds">10</polling-interval>
			    </publisher-options>
            </init-params>
        </input>
    </nds>
	*/

    //example result document:
	/*
    <nds dtdversion="1.1" ndsversion="8.6">
        <source>
            <product build="20021214_0304" instance="Skeleton Driver (Java, XDS)" version="1.1">DirXML Skeleton Driver (Java, XDS)</product>
            <contact>My Company Name</contact>
        </source>
        <output>
            <schema-def application-name="Skeleton Application" hierarchical="true">
                <class-def class-name="fake-class-1" container="true">
                    <attr-def attr-name="fake-attr-1" case-sensitive="true" multi-valued="true" naming="true" read-only="true" required="true" type="string"/>
                </class-def>
                <class-def class-name="fake-class-2" container="false">
                    <attr-def attr-name="fake-attr-2" case-sensitive="false" multi-valued="false" naming="false" read-only="false" required="false" type="int"/>
                </class-def>
            </schema-def>
            <status level="warning" type="driver-general">
                <description>Get schema not implemented.</description>
            </status>
        </output>
    </nds>
	*/
	public XmlDocument getSchema(XmlDocument initXML) {
        XDSInitDocument init;

        //parse initialization document
        try {
			init = new XDSInitDocument(initXML);
            
            driverRDN = init.rdn();

            trace = new Trace(driverRDN + "\\Driver");
		} catch (XDSParseException e) {
            XDSResultDocument result = new XDSResultDocument();

            Util.addStatus(result, StatusLevel.FATAL, StatusType.DRIVER_STATUS, null, null, e, XDSUtil.appendStackTrace(e), initXML);
            return result.toXML();
		}

		XDSSchemaResultDocument result = new XDSSchemaResultDocument();

        //append a <source> element to the result document
        appendSourceInfo(result);

        XDSSchemaDefElement schemaDef = result.appendSchemaDefElement();
        schemaDef.setApplicationName("RSA");
        schemaDef.setHierarchical(false);

        XDSClassDefElement userClass = schemaDef.appendClassDefElement();
        userClass.setClassName(CLASS_USER);
        userClass.setContainer(false);

        appendAttrDefElement(userClass, AceApi.ATTR_DEFAULT_LOGIN, false, false, true, false, true, ValueType.STRING);
        appendAttrDefElement(userClass, AceApi.ATTR_DEFAULT_SHELL, false, false, false, false, false, ValueType.STRING);
        appendAttrDefElement(userClass, AceApi.ATTR_FIRST_NAME, false, false, false, false, false, ValueType.STRING);
        appendAttrDefElement(userClass, AceApi.ATTR_LAST_NAME, false, false, false, false, true, ValueType.STRING);
        appendAttrDefElement(userClass, AceApi.ATTR_EMAIL_ADDRESS, false, false, false, false, false, ValueType.STRING);
        appendAttrDefElement(userClass, AceApi.ATTR_TOKEN_SERIAL_NUMBER, false, true, false, false, false, ValueType.STRING);
        appendAttrDefElement(userClass, AceApi.ATTR_PROFILE_NAME, false, false, false, false, false, ValueType.STRING);
        appendAttrDefElement(userClass, AceApi.ATTR_MEMBER_OF, false, true, false, false, false, ValueType.STRING);
        appendAttrDefElement(userClass, AceApi.ATTR_TEMP_USER, false, false, false, false, false, ValueType.STATE);
        appendAttrDefElement(userClass, AceApi.ATTR_START, false, false, false, false, false, ValueType.TIME);
        appendAttrDefElement(userClass, AceApi.ATTR_END, false, false, false, false, false, ValueType.TIME);
        appendAttrDefElement(userClass, AceApi.ATTR_PASSWORD, true, false, false, false, false, ValueType.STRING);

        XDSClassDefElement tokenClass = schemaDef.appendClassDefElement();
        tokenClass.setClassName(CLASS_TOKEN);
        tokenClass.setContainer(false);

        appendAttrDefElement(tokenClass, AceApi.ATTR_SERIAL_NUM, false, false, true, true, true, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_PIN_CLEAR, false, false, false, true, true, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_NUM_DIGITS, false, false, false, true, true, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_INTERVAL, false, false, false, true, true, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_BIRTH, false, false, false, true, true, ValueType.TIME);
        appendAttrDefElement(tokenClass, AceApi.ATTR_DEATH, false, false, false, true, true, ValueType.TIME);
        appendAttrDefElement(tokenClass, AceApi.ATTR_LAST_LOGIN, false, false, false, true, false, ValueType.TIME);
        appendAttrDefElement(tokenClass, AceApi.ATTR_TYPE, false, false, false, true, true, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_HEX, false, false, false, true, true, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_NEW_PIN_MODE, false, false, false, false, true, ValueType.STATE);
        appendAttrDefElement(tokenClass, AceApi.ATTR_USER_NUM, false, false, false, false, false, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_DEFAULT_LOGIN, false, false, false, false, false, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_NEXT_CODE_STATUS, false, false, false, false, true, ValueType.STATE);
        appendAttrDefElement(tokenClass, AceApi.ATTR_BAD_TOKEN_CODES, false, false, false, true, true, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_BAD_PINS, false, false, false, true, true, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_PIN_CHANGED_DATE, false, false, false, true, true, ValueType.TIME);
        appendAttrDefElement(tokenClass, AceApi.ATTR_DISABLED_DATE, false, false, false, true, true, ValueType.TIME);
        appendAttrDefElement(tokenClass, AceApi.ATTR_COUNTS_LAST_MODIFIED, false, false, false, true, true, ValueType.TIME);
        appendAttrDefElement(tokenClass, AceApi.ATTR_PIN_CHANGED_DATE, false, false, false, true, true, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_PROTECTED, false, false, false, false, true, ValueType.STATE);
        appendAttrDefElement(tokenClass, AceApi.ATTR_DEPLOYMENT, false, false, false, true, true, ValueType.TIME);
        appendAttrDefElement(tokenClass, AceApi.ATTR_DEPLOYED, false, false, false, false, true, ValueType.STATE);
        appendAttrDefElement(tokenClass, AceApi.ATTR_COUNT, false, false, false, false, false, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_SOFT_PASSWORD, false, false, false, false, false, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_PIN, false, false, false, false, false, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_DISABLED, false, false, false, false, true, ValueType.STATE);
        appendAttrDefElement(tokenClass, AceApi.ATTR_ASSIGNED, false, false, false, false, true, ValueType.STATE);
        appendAttrDefElement(tokenClass, AceApi.ATTR_SEED_SIZE, false, false, false, true, true, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_KEYPAD, false, false, false, true, true, ValueType.STATE);
        appendAttrDefElement(tokenClass, AceApi.ATTR_LOCAL_PIN, false, false, false, true, true, ValueType.STATE);
        appendAttrDefElement(tokenClass, AceApi.ATTR_VERSION, false, false, false, true, true, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_FORM_FACTOR, false, false, false, true, true, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_PIN_TYPE, false, false, false, true, true, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_ASSIGNMENT, false, false, false, true, false, ValueType.TIME);
        appendAttrDefElement(tokenClass, AceApi.ATTR_FIRST_LOGIN, false, false, false, true, false, ValueType.STATE);
        appendAttrDefElement(tokenClass, AceApi.ATTR_LAST_DA_CODE, false, false, false, true, false, ValueType.TIME);
        appendAttrDefElement(tokenClass, AceApi.ATTR_EAC_EXPIRES, false, false, false, true, false, ValueType.TIME);
        appendAttrDefElement(tokenClass, AceApi.ATTR_EAC_PASSCODE, false, false, false, true, false, ValueType.STRING);
        appendAttrDefElement(tokenClass, AceApi.ATTR_EMERGENCY_ACCESS, false, false, false, false, false, ValueType.STATE);

        Util.addStatus(result, StatusLevel.SUCCESS, StatusType.DRIVER_STATUS, null, null);

        return result.toXML();
	}

    private void appendAttrDefElement(XDSClassDefElement classDef, String name,
            boolean caseSensitive, boolean multiValued, boolean naming,
            boolean readOnly, boolean required, ValueType type)
    {
        XDSAttrDefElement attrDef = classDef.appendAttrDefElement();
        attrDef.setAttrName(name);
        attrDef.setCaseSensitive(caseSensitive);
        attrDef.setMultiValued(multiValued);
        attrDef.setNaming(naming);
        attrDef.setReadOnly(readOnly);
        attrDef.setRequired(required);
        attrDef.setType(type);
    }

	public SubscriptionShim getSubscriptionShim() {
		return subscriberShim;
	}

    //example initialization document:

	/*
    <nds dtdversion="1.1" ndsversion="8.6">
        <source>
            <product version="1.1a">DirXML</product>
            <contact>Novell, Inc.</contact>
        </source>
        <input>
            <init-params src-dn="\NEW_DELL_TREE\NOVELL\Driver Set\Skeleton Driver (Java, XDS)">
                <authentication-info>
                    <server>server.app:400</server>
                    <user>User1</user>
                </authentication-info>
                <driver-options>
		            <option-1 display-name="Sample String option">This is a string</option-1>
		            <option-2 display-name="Sample int option (enter an integer)">10</option-2>
		            <option-3 display-name="Sample boolean option (enter a boolean value)">true</option-3>
		            <option-4 display-name="Sample required option (enter some value)">not null</option-4>
		        </driver-options>
            </init-params>
        </input>
    </nds>
	*/

    //example result document:
	/*
    <nds dtdversion="1.1" ndsversion="8.6">
        <source>
            <product build="20021214_0304" instance="Skeleton Driver (Java, XDS)" version="1.1">DirXML Skeleton Driver (Java, XDS)</product>
            <contact>My Company Name</contact>
        </source>
        <output>
            <status level="success" type="driver-status">
                <parameters>
                    <option-1 display-name="Sample String option">This is a string</option-1>
                    <option-2 display-name="Sample int option (enter an integer)">10</option-2>
                    <option-3 display-name="Sample boolean option (enter a boolean value)">true</option-3>
                    <option-4 display-name="Sample required option (enter some value)">not null</option-4>
                    <password display-name="password"><!-- content suppressed -->
                    </password>
                    <server display-name="server">server.app:400</server>
                    <user display-name="user">User1</user>
                </parameters>
            </status>
        </output>
    </nds>
	*/

	public XmlDocument init(XmlDocument initXML) {
		initTrace.trace("init", Trace.DEFAULT_TRACE);
		
        try {
            XDSInitDocument init = new XDSInitDocument(initXML);
            XDSInitParamsElement params = init.extractInitParamsElement();
            XDSAuthenticationInfoElement authInfo = params.extractAuthenticationInfoElement();
            if (authInfo != null) {
            	
            	aceParams.put(AceApi.SERVER, authInfo.extractServerText());
            	aceParams.put(AceApi.PASSWORD, authInfo.extractPasswordText());
            	aceParams.put(AceApi.USERNAME, authInfo.extractUserText());
            }

            List options = params.extractDriverOptionsElement().childElements();
            for (Iterator i = options.iterator(); i.hasNext(); ) {
                NonXDSElement option = (NonXDSElement) i.next();
                if (option.localName().equals("userExtensions")) {
                    String value = option.extractText();
                    userExtensions.add(value);
                } else if (option.localName().equals("tokenExtensions")) {
                    String value = option.extractText();
                    tokenExtensions.add(value);
                } else if (option.localName().equals(AceApi.COMMAND_CLIENT_USERNAME) ||
                		option.localName().equals(AceApi.COMMAND_CLIENT_PASSWORD) ||
                		option.localName().equals(AceApi.RSA_REALM) ||
                		option.localName().equals(AceApi.WEBLOGIC_LIB_DIR) ||
                		option.localName().equals(AceApi.RSA_KEYSTORE_FILE) ||
                		option.localName().equals(AceApi.API_VERSION) ||
                		option.localName().equals(AceApi.RSA_IDENTITY_SOURCE) ||
                		option.localName().equals(AceApi.APPEND_SITE_TO_ALL_GROUPS)) {
                	aceParams.put(option.localName(), option.extractText());
                }
            }

            driverRDN = init.rdn();

            trace = new Trace(driverRDN + "\\Driver");

            //TODO: Following try/catch is temporary debug code.
//            try {
//    			ClassLoader appLoader = ClassLoader.getSystemClassLoader();
//    			ClassLoader currentLoader = AceDriverShim.class.getClassLoader();
//
//    			ClassLoader[] loaders = new ClassLoader[] { appLoader, currentLoader };
//    			
//    			final Class< ?>[] classes = ClassScope.getLoadedClasses(loaders);
//    			
//    			for (Class<?> cls : classes) {
//	    			String className = cls.getName();
//	    			URL classLocation = ClassScope.getClassLocation(cls);
//	    			
//	    			initTrace.trace("Class <" + className + "> is in location <" + classLocation + ">", Trace.DEFAULT_TRACE);
//    			}
//
//    		} catch (Exception e) {
//    			throw new AceToolkitException("Testing output", e);
//    		}

            if(aceParams.get(AceApi.RSA_IDENTITY_SOURCE) == null) {
                trace.trace("RSA Identity Source not specified. This may result in the driver using an unexpected or default identity source with RSA 7.x.", Trace.DEFAULT_TRACE);
            }
            
            AceApi71 api = new AceApi71(aceParams);
            for (PrincipalDTO user : api.searchUsers(Filter.empty())) {
    			trace.trace(user.getGuid() + " || " + user.getUserID(), Trace.DEFAULT_TRACE);
    		}
            rsaData = new DataModel(api, driverRDN, userExtensions, tokenExtensions);

            //TODO: Following class.forname(), trace, and try/catch is temporary debug code.
//            Class a3class = Class.forName("com.rsa.jsafe.a3");
//			initTrace.trace("!!!! POST INIT !!!!", Trace.DEFAULT_TRACE);
//    		try {
//    			ClassLoader appLoader = ClassLoader.getSystemClassLoader();
//    			ClassLoader currentLoader = AceDriverShim.class.getClassLoader();
//
//    			ClassLoader[] loaders = new ClassLoader[] { appLoader, currentLoader };
//    			
//    			final Class< ?>[] classes = ClassScope.getLoadedClasses(loaders);
//    			
//    			for (Class<?> cls : classes) {
//	    			String className = cls.getName();
//	    			URL classLocation = ClassScope.getClassLocation(cls);
//	    			
//	    			initTrace.trace("Class <" + className + "> is in location <" + classLocation + ">", Trace.DEFAULT_TRACE);
//    			}
//
//    		} catch (Exception e) {
//    			throw new AceToolkitException("Testing output", e);
//    		}
            
            subscriberShim = new SubscriberShim(this);
            publisherShim = new PublisherShim(this);

            XDSResultDocument result = createResultDoc();

            trace.trace("jace version: " + Version.getVersion(AceApi.class), Trace.DEFAULT_TRACE);
            trace.trace("jace library version: " + api.getLibraryVersion(), Trace.DEFAULT_TRACE);
            Util.addStatus(result, StatusLevel.SUCCESS, StatusType.DRIVER_STATUS, null, null);

            return result.toXML();
        } catch (Exception e) {
            XDSResultDocument result = new XDSResultDocument();

            Util.addStatus(result, StatusLevel.FATAL, StatusType.DRIVER_STATUS, null, null, e, XDSUtil.appendStackTrace(e), initXML);
            return result.toXML();
        }
	}

	//TODO: temporary code for debugging
//	private static class ClassScope
//	{
//	     private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
//	     private static final Throwable CVF_FAILURE, CR_FAILURE; // set in <clinit>
//	 
//	     static {
//	           Throwable failure = null;
//	 
//	           Field tempf = null;
//	           try {
//	                // this can fail if this is not a Sun-compatible JVM
//	                // or if the security is too tight:
//	 
//	                tempf = ClassLoader.class.getDeclaredField("classes");
//	                if (tempf.getType() != Vector.class) {
//	                     throw new RuntimeException("not of type java.util.Vector: "
//	                                + tempf.getType().getName());
//	                }
//	 
//	                tempf.setAccessible(true);
//	           } catch (Throwable t) {
//	                failure = t;
//	           }
//	           CLASSES_VECTOR_FIELD = tempf;
//	           CVF_FAILURE = failure;
//	 
//	           failure = null;
//	           CallerResolver tempcr = null;
//	           try {
//	                // this can fail if the current SecurityManager does not allow
//	                // RuntimePermission ("createSecurityManager"):
//	 
//	                tempcr = new CallerResolver();
//	           } catch (Throwable t) {
//	                failure = t;
//	           }
//	           CALLER_RESOLVER = tempcr;
//	           CR_FAILURE = failure;
//	     }
	 
	     /**
	      * Given a class loader instance, returns all classes currently loaded by
	      * that class loader.
	      *
	      * @param defining
	      *            class loader to inspect [may not be null]
	      * @return Class array such that every Class has 'loader' as its defining
	      *         class loader [never null, may be empty]
	      *
	      * @throws RuntimeException
	      *            if the "classes" field hack is not possible in this JRE
	      */
//	     public static Class<?>[] getLoadedClasses(final ClassLoader loader) {
//	           if (loader == null) {
//	                throw new IllegalArgumentException("null input: loader");
//	           }
//	           if (CLASSES_VECTOR_FIELD == null) {
//	                throw new RuntimeException(
//	                           "ClassScope::getLoadedClasses() cannot be used in this JRE",
//	                           CVF_FAILURE);
//	           }
//	 
//	           try {
//	                final Vector<Class<?>> classes =
//	                     (Vector<Class<?>>) CLASSES_VECTOR_FIELD.get(loader);
//	                if (classes == null)
//	                     return EMPTY_CLASS_ARRAY;
//	 
//	                final Class<?>[] result;
//	 
//	                // note: Vector is synchronized in Java 2, which helps us make
//	                // the following into a safe critical section:
//	 
//	                synchronized (classes) {
//	                     result = new Class<?>[classes.size()];
//	                     classes.toArray(result);
//	                }
//	 
//	                return result;
//	           }
//	           // this should not happen if <clinit> was successful:
//	           catch (IllegalAccessException e) {
//	                e.printStackTrace(System.out);
//	 
//	                return EMPTY_CLASS_ARRAY;
//	           }
//	     }
	 
	     /**
	      * A convenience multi-loader version of
	      * {@link #getLoadedClasses(ClassLoader)}.
	      *
	      * @param an
	      *            array of defining class loaders to inspect [may not be null]
	      * @return Class<?> array [never null, may be empty]
	      *
	      * @throws RuntimeException
	      *             if the "classes" field hack is not possible in this JRE
	      */
//	     public static Class<?>[] getLoadedClasses(final ClassLoader[] loaders) {
//	           if (loaders == null)
//	                throw new IllegalArgumentException("null input: loaders");
//	 
//	           final List<Class<?>> resultList = new LinkedList<Class<?>>();
//	 
//	           for (int l = 0; l < loaders.length; ++l) {
//	                final ClassLoader loader = loaders[l];
//	                if (loader != null) {
//	                     final Class<?>[] classes = getLoadedClasses(loaders[l]);
//	 
//	                     resultList.addAll(Arrays.asList(classes));
//	                }
//	           }
//	 
//	           final Class<?>[] result = new Class<?>[resultList.size()];
//	           resultList.toArray(result);
//	 
//	           return result;
//	     }
	 
	     /**
	      * Returns the class loader set "relevant" to the calling class, as
	      * described in the article. Starting with the class that is the caller of
	      * this method, it collects all class loaders that are loaders for all
	      * classes on the call stack and their respective parent loaders.
	      *
	      * @return ClassLoader array [never null]
	      *
	      * @throws RuntimeException
	      *             if the caller context resolver could not be instantiated
	      */
//	     public static ClassLoader[] getCallerClassLoaderTree() {
//	           if (CALLER_RESOLVER == null)
//	                throw new RuntimeException(
//	                           "Class<?>Scope::getCallerClassLoaderTree() cannot be used in this JRE",
//	                           CR_FAILURE);
//	 
//	           final Class<?>[] callContext = CALLER_RESOLVER.getClassContext();
//	 
//	           final Set<ClassLoader> resultSet = new HashSet<ClassLoader>();
//	 
//	           for (int c = 2; c < callContext.length; ++c) {
//	                getClassLoaderTree(callContext[c], resultSet);
//	           }
//	 
//	           final ClassLoader[] result = new ClassLoader[resultSet.size()];
//	           resultSet.toArray(result);
//	 
//	           return result;
//	     }
	 
	     /**
	      * Given a Class<?> object, attempts to find its .class location [returns
	      * null if no such definiton could be found].
	      *
	      * @return URL that points to the class definition [null if not found]
	      */
	     public static URL getClassLocation(final Class<?> cls) {
	           if (cls == null)
	                throw new IllegalArgumentException("null input: cls");
	 
	           URL result = null;
	           final String clsAsResource = cls.getName().replace('.', '/').concat(
	                     ".class");
	 
	           final ProtectionDomain pd = cls.getProtectionDomain();
	           // java.lang.Class<?> contract does not specify if 'pd' can ever be
	           // null;
	           // it is not the case for Sun's implementations, but guard against null
	           // just in case:
	           if (pd != null) {
	                final CodeSource cs = pd.getCodeSource();
	                // 'cs' can be null depending on the classloader behavior:
	                if (cs != null)
	                     result = cs.getLocation();
	 
	                if (result != null) {
	                     // convert a code source location into a full class file
	                     // location
	                     // for some common cases:
	                     if ("file".equals(result.getProtocol())) {
	                           try {
	                                if (result.toExternalForm().endsWith(".jar")
	                                           || result.toExternalForm().endsWith(".zip"))
	                                     result = new URL("jar:".concat(
	                                                result.toExternalForm()).concat("!/")
	                                                .concat(clsAsResource));
	                                else if (new File(result.getFile()).isDirectory())
	                                     result = new URL(result, clsAsResource);
	                           } catch (MalformedURLException ignore) {
	                           }
	                     }
	                }
	           }
	 
	           if (result == null) {
	                // try to find 'cls' definition as a resource; this is not
	                // documented to be legal but Sun's implementations seem to allow
	                // this:
	                final ClassLoader clsLoader = cls.getClassLoader();
	 
	                result = clsLoader != null ? clsLoader.getResource(clsAsResource)
	                           : ClassLoader.getSystemResource(clsAsResource);
	           }
	 
	           return result;
	     }
	 
	     /**
	      * A helper class to get the call context. It subclasses SecurityManager to
	      * make getClassContext() accessible. An instance of CallerResolver only
	      * needs to be created, not installed as an actual security manager.
	      */
//	     private static final class CallerResolver extends SecurityManager {
//	           protected Class<?>[] getClassContext() {
//	                return super.getClassContext();
//	           }
//	 
//	     } // end of nested class
	 
//	     private ClassScope() {
//	     } // this class is not extendible
	 
//	     private static void getClassLoaderTree(final Class<?> cls,
//	                final Set<ClassLoader> resultSet) {
//	           if ((cls != null) && (resultSet != null)) {
//	                for (ClassLoader loader = cls.getClassLoader(); loader != null; loader = loader
//	                           .getParent()) {
//	                     resultSet.add(loader);
//	                }
//	           }
//	     }
//	 
//	     private static final Field CLASSES_VECTOR_FIELD; // set in <clinit> [can be
//	                                                                           // null]
//	     private static final CallerResolver CALLER_RESOLVER; // set in <clinit> [can
//	                                                                                // be null]
//	 
//	 
//	}

	
	
	
	
	/*
    <nds dtdversion="1.1" ndsversion="8.6">
        <source>
            <product build="20021214_0304" instance="Skeleton Driver (Java, XDS)" version="1.1">DirXML Skeleton Driver (Java, XDS)</product>
            <contact>My Company Name</contact>
        </source>
        <output>
            <status level="success" type="driver-status"/>
        </output>
    </nds>
	*/
	public XmlDocument shutdown(XmlDocument reasonXML) {
        trace.trace("shutdown", Trace.DEFAULT_TRACE);

        try {
            if (publisherShim != null) {
                publisherShim.shutdown();
            }
        } catch (Exception e) {
            trace.trace("Error shutting down the publisher channel. " + e);
        }
        
        try {
            if (subscriberShim != null) {
                subscriberShim.shutdown();
            }

            return createSuccessDocument();
        } catch (Exception e) {
            return createStatusDocument(e);
        }
	}
	
	String getDriverRDN() {
		return driverRDN;
	}

	XDSResultDocument createResultDoc() {
        XDSResultDocument resultDoc = new XDSResultDocument();
        appendSourceInfo(resultDoc);

        return resultDoc;
    }

	void appendSourceInfo(WriteableDocument doc) {
        XDSSourceElement source = doc.appendSourceElement();
        source.appendContactElement("TriVir");

        XDSProductElement product = source.appendProductElement();
        String versionInfo;
        String shimVersion = Version.getVersion(this.getClass());
        String aceVersion = Version.getVersion(AceApi.class);
        if(shimVersion == null && aceVersion == null) {
        	versionInfo = null;
        } else {
        	versionInfo = Version.getVersion(this.getClass()) + "/" + Version.getVersion(AceApi.class);
        }
        product.setVersion(versionInfo);
        product.setInstance(driverRDN);
        product.appendText("RSA IDM Driver");
    }

	XmlDocument createStatusDocument(StatusLevel level, StatusType type, String eventID, String description) {
        XDSResultDocument result = createResultDoc();
        Util.addStatus(result, level, type, eventID, description);
		return result.toXML();
	}
	
	XmlDocument createStatusDocument(StatusLevel level, StatusType type, String eventID, String description, Exception e, boolean appendStackTrace, XmlDocument xmlToAppend) {
        XDSResultDocument result = createResultDoc();
        Util.addStatus(result, level, type, eventID, description, e, appendStackTrace, xmlToAppend);
		return result.toXML();
	}

	XmlDocument createSuccessDocument() {
		return createStatusDocument(StatusLevel.SUCCESS, StatusType.DRIVER_STATUS, null, null);
	}
	
	XmlDocument createSuccessDocument(Map params) {
        XDSResultDocument result = createResultDoc();
        XDSStatusElement status = Util.addStatus(result, StatusLevel.SUCCESS, StatusType.DRIVER_STATUS, null, null);
        status.parametersAppend(params);

        return result.toXML();
	}
	
	XmlDocument createStatusDocument(Exception e) {
		return createStatusDocument(StatusLevel.FATAL, StatusType.DRIVER_STATUS, null, null, e, true, null);
	}
	
	XmlDocument createStatusDocument(Exception e, XmlDocument xml) {
		return createStatusDocument(StatusLevel.FATAL, StatusType.DRIVER_STATUS, null, null, e, XDSUtil.appendStackTrace(e), xml);
	}
	
	XmlDocument createStatusDocument(String eventId, Exception e, XmlDocument commandXML) {
		return createStatusDocument(StatusLevel.FATAL, StatusType.DRIVER_STATUS, eventId, null, e, true, commandXML);
	}

    List getUserExtensions() {
        return userExtensions;
    }

    List getTokenExtensions() {
        return tokenExtensions;
    }
}
