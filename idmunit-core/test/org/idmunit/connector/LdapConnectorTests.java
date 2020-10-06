/*
 * IdMUnit - Automated Testing Framework for Identity Management Solutions
 * Copyright (c) 2005-2016 TriVir, LLC
 *
 * This program is licensed under the terms of the GNU General Public License
 * Version 2 (the "License") as published by the Free Software Foundation, and
 * the TriVir Licensing Policies (the "License Policies").  A copy of the License
 * and the Policies were distributed with this program.
 *
 * The License is available at:
 * http://www.gnu.org/copyleft/gpl.html
 *
 * The Policies are available at:
 * http://www.idmunit.org/licensing/index.html
 *
 * Unless required by applicable law or agreed to in writing, this program is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied.  See the License and the Policies
 * for specific language governing the use of this program.
 *
 * www.TriVir.com
 * TriVir LLC
 * 13890 Braddock Road
 * Suite 310
 * Centreville, Virginia 20121
 *
 */

package org.idmunit.connector;

import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import org.idmunit.IdMUnitException;
import org.idmunit.connector.ldap.BinaryAttrUtil;
import org.idmunit.util.KeyStoreHelper;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import static org.idmunit.connector.ConfigTests.*;
import static org.idmunit.connector.ConfigTests.OCTET_TEST_ATTR;

public class LdapConnectorTests extends TestCase {

    private static final char SEP = File.separatorChar;
    private static final String JSSECACERTS = System.getProperty("java.home") + SEP + "lib" + SEP + "security" + SEP + "cacerts";

    private static final String BIN_TEST_ATTR = BinaryAttrUtil.ATTR_PREFIX_BIN + OCTET_TEST_ATTR;
    private static final String B64_TEST_ATTR = BinaryAttrUtil.ATTR_PREFIX_B64 + OCTET_TEST_ATTR;

    private LdapConnector connector;

    @Override
    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        connector = new LdapConnector();
    }

    @Override
    protected void tearDown() throws Exception {
        connector.tearDown();
    }

    public void testAddAttrOperation() throws IdMUnitException, Exception {

        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", E_DIR_SERVER_HOST + ":" + E_DIR_SERVER_PORT);
        config.put("user", E_DIR_ADMIN);
        config.put("password", E_DIR_ADMIN_PASS);
//        config.put("KEYSTORE-path", KEYSTORE);
        config.put("use-ssl", "true");
        config.put("trust-all-certs", "true");

        KeyStoreHelper.writeCertificatesToKeyStore(KEYSTORE, KEY_STORE_PASSPHRASE.toCharArray(), "test", E_DIR_SERVER_HOST, E_DIR_SERVER_PORT);

        try {
            connector.setup(config);
        } catch (IdMUnitException e) {
            throw e;
        } finally {
            new File(KEYSTORE).delete();
        }

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList("CN=gmathis,o=users"));
        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList("gmathis"));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("fullName", Arrays.asList("Gordon Mathis"));
        attrs.put("userPassword", Arrays.asList("trivir"));

        connector.opDeleteObject(attrs);

        connector.opAddObject(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList("CN=gmathis,o=users"));
        attrs.put("telephoneNumber", Arrays.asList("222-333-4444"));

        connector.opAddAttr(attrs);

        attrs.put("cn", Arrays.asList("gmathis"));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("fullName", Arrays.asList("Gordon Mathis"));

        connector.opValidateObject(attrs);
    }

    public void testRemoveAttrOperation() throws IdMUnitException, Exception {

        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", E_DIR_SERVER_HOST + ":" + E_DIR_SERVER_PORT);
        config.put("user", E_DIR_ADMIN);
        config.put("password", E_DIR_ADMIN_PASS);
        //config.put("KEYSTORE-path", KEYSTORE);
        config.put("use-ssl", "true");
        config.put("trust-all-certs", "true");

        KeyStoreHelper.writeCertificatesToKeyStore(KEYSTORE, KEY_STORE_PASSPHRASE.toCharArray(), "test", E_DIR_SERVER_HOST, E_DIR_SERVER_PORT);

        try {
            connector.setup(config);
        } catch (IdMUnitException e) {
            throw e;
        } finally {
            new File(KEYSTORE).delete();
        }

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList("CN=gmathis,O=users"));
        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList("gmathis"));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("fullName", Arrays.asList("Gordon Mathis"));
        attrs.put("telephoneNumber", Arrays.asList("222-333-4444"));
        attrs.put("userPassword", Arrays.asList("trivir"));

        connector.opDeleteObject(attrs);

        connector.opAddObject(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList("CN=gmathis,o=users"));
        attrs.put("telephoneNumber", Arrays.asList("222-333-4444"));

        connector.opRemoveAttr(attrs);

        attrs.remove("telephoneNumber");

        attrs.put("cn", Arrays.asList("gmathis"));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("fullName", Arrays.asList("Gordon Mathis"));

        connector.opValidateObject(attrs);
    }

    public void testExceptionChainOnConnection() throws IdMUnitException, Exception {
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", E_DIR_SERVER_HOST + ":" + E_DIR_SERVER_PORT);
        config.put("user", E_DIR_ADMIN);
        config.put("password", "trivir");

        try {
            connector.setup(config);
            fail();
        } catch (IdMUnitException e) {
            IdMUnitException exception = new IdMUnitException();
            assertTrue(e.getClass() == exception.getClass());
        }
    }

    public void testChangePasswordADStyle() throws IdMUnitException, Exception {
    	String testADUser = AD_TEST_USER;
    	
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", AD_SERVER_HOST + ":" + AD_SERVER_PORT);
        config.put("user", AD_ADMIN);
        config.put("password", AD_ADMIN_PASS);
        //config.put("KEYSTORE-path", KEYSTORE);
        config.put("use-ssl", "true");
        config.put("trust-all-certs", "true");

        // currently fails, as it appears our AD server doesn't have SSL enabled.
        KeyStoreHelper.writeCertificatesToKeyStore(KEYSTORE, KEY_STORE_PASSPHRASE.toCharArray(), "test", AD_SERVER_HOST, AD_SERVER_PORT);

        try {
            connector.setup(config);
        } finally {
            new File(KEYSTORE).delete();
        }

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();        
        attrs.put("dn", Arrays.asList(testADUser));
        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("givenName", Arrays.asList("testgivenname"));
        attrs.put("sn", Arrays.asList("testlast"));
        attrs.put("displayName", Arrays.asList("testfirst testlast"));
        attrs.put("sAMAccountName", Arrays.asList("tfirstdiff2"));
        attrs.put("userAccountControl", Arrays.asList("512"));
        attrs.put("userPrincipalName", Arrays.asList("sometestuserinad2@name.com"));
        attrs.put("unicodePwd", Arrays.asList("Trivir1234"));

        connector.opDeleteObject(attrs);

        connector.opAddObject(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(testADUser));
        attrs.put("oldUserPassword", Arrays.asList("Trivir1234"));
        attrs.put("unicodePwd", Arrays.asList("Novell1234#"));

        // note: we're using admin; but we're doing a change password since the old password is being provided.
        // TODO: should we actually log in as the user to do this?
        connector.opChangeUserPasswordUnicode(attrs);

        // Now login with new password
        config.put("user", testADUser);
        config.put("server", AD_SERVER_HOST + ":389");
        config.put("use-tls", "false");
        config.put("password", "Novell1234#");

        try {
            connector.setup(config);
        } finally {
            new File(JSSECACERTS).delete();
        }

        attrs.remove("unicodePwd");
        attrs.put("userPassword", Arrays.asList("Novell1234#"));

        connector.opValidateObject(attrs);
    }

    public void testValidateUsingAnyPasswordAttr() throws IdMUnitException, Exception {
        String testADUser = AD_TEST_USER;
        String password = "Trivir1234";

        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", AD_SERVER_HOST + ":" + AD_SERVER_PORT);
        config.put("user", AD_ADMIN);
        config.put("password", AD_ADMIN_PASS);
        //config.put("KEYSTORE-path", KEYSTORE);
        config.put("use-ssl", "true");
        config.put("trust-all-certs", "true");

        // currently fails, as it appears our AD server doesn't have SSL enabled.
        KeyStoreHelper.writeCertificatesToKeyStore(KEYSTORE, KEY_STORE_PASSPHRASE.toCharArray(), "test", AD_SERVER_HOST, AD_SERVER_PORT);

        try {
            connector.setup(config);
        } finally {
            new File(KEYSTORE).delete();
        }

        //TODO: minimize the set of attributes used in the add
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();
        attrs.put("dn", Arrays.asList(testADUser));
        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("givenName", Arrays.asList("testgivenname"));
        attrs.put("sn", Arrays.asList("testlast"));
        attrs.put("displayName", Arrays.asList("testfirst testlast"));
        attrs.put("sAMAccountName", Arrays.asList("tfirstdiff2"));
        attrs.put("userAccountControl", Arrays.asList("512"));
        attrs.put("userPrincipalName", Arrays.asList("sometestuserinad2@name.com"));
        attrs.put("unicodePwd", Arrays.asList(password));

        connector.opDeleteObject(attrs);

        connector.opAddObject(attrs);

        //should be able to validate using whatever password attribute you used when setting the password

        Map<String, Collection<String>> validateAttrs = new TreeMap<String, Collection<String>>();
        validateAttrs.put("dn", Arrays.asList(testADUser));
        validateAttrs.put("unicodePwd", Arrays.asList(password));

        connector.opValidateObject(attrs);

        validateAttrs = new TreeMap<String, Collection<String>>();
        validateAttrs.put("dn", Arrays.asList(testADUser));
        validateAttrs.put("userPassword", Arrays.asList(password));

        connector.opValidateObject(attrs);
    }

    public void testChangeTheirPasswordEDirStyle() throws IdMUnitException, Exception {
    	String testEdirLDAPUser = "cn=testuser,o=users";    	
    	
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", E_DIR_SERVER_HOST + ":" + E_DIR_SERVER_PORT);
        config.put("user", E_DIR_ADMIN);
        config.put("password", E_DIR_ADMIN_PASS);
        //config.put("KEYSTORE-path", KEYSTORE);
        config.put("use-ssl", "true");
        config.put("trust-all-certs", "true");

        KeyStoreHelper.writeCertificatesToKeyStore(KEYSTORE, KEY_STORE_PASSPHRASE.toCharArray(), "test", E_DIR_SERVER_HOST, E_DIR_SERVER_PORT);

        try {
            connector.setup(config);
        } catch (IdMUnitException e) {
            throw e;
        } finally {
            new File(KEYSTORE).delete();
        }

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(testEdirLDAPUser));
        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("sn", Arrays.asList("testuser"));
        attrs.put("userPassword", Arrays.asList("oldpassword"));

        connector.opDeleteObject(attrs);
        connector.opAddObject(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(testEdirLDAPUser));
        attrs.put("oldUserPassword", Arrays.asList("oldpassword"));
        attrs.put("userPassword", Arrays.asList("trivirChanged1234"));
        
        connector.opChangeUserPassword(attrs);

        // Now login with new password
        config.put("user", testEdirLDAPUser);
        config.put("server", E_DIR_SERVER_HOST + ":" + E_DIR_SERVER_PORT);
        config.put("use-tls", "true");
        config.put("trust-all-certs", "true");
        config.put("password", "trivirChanged1234");

        try {
            connector.setup(config);
        } finally {
            new File(JSSECACERTS).delete();
        }

        attrs.remove("oldUserPassword");
        attrs.put("userPassword", Arrays.asList("trivirChanged1234"));

        connector.opValidateObject(attrs);
    }
    
    public void testChangePasswordEdir() throws IdMUnitException {
    	String someNewTestUser = "cn=somenewtestuser,o=users";
    	
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", E_DIR_SERVER_HOST + ":" + E_DIR_SERVER_PORT);
        config.put("user", E_DIR_ADMIN);
        config.put("password", E_DIR_ADMIN_PASS);
        config.put("use-tls", "true");
        config.put("trust-all-certs", "true");

        connector.setup(config);

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(someNewTestUser));
        attrs.put("objectClass", Arrays.asList("person"));
        attrs.put("cn", Arrays.asList("gmathis"));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("fullName", Arrays.asList("Gordon Mathis"));
        attrs.put("userPassword", Arrays.asList("trivir"));

        connector.opDeleteObject(attrs);

        connector.opAddObject(attrs);
        
        String newPassword = "Trivir123";

        attrs = new TreeMap<String, Collection<String>>();
        attrs.put("dn", Arrays.asList(someNewTestUser));
        attrs.put("oldUserPassword", Arrays.asList("trivir"));
        attrs.put("userPassword", Arrays.asList(newPassword));

        connector.opChangeUserPassword(attrs);

        // Now login with new password
        config.put("user", someNewTestUser);
        config.put("server", E_DIR_SERVER_HOST + ":" + E_DIR_SERVER_PORT);
        config.put("use-tls", "true");
        config.put("password", newPassword);
        
        connector.setup(config);

        attrs.remove("newPassword");
        attrs.put("userPassword", Arrays.asList(newPassword));

        connector.opValidateObject(attrs);
    }

    public void testResetPasswordEdir() throws IdMUnitException {
    	String someNewTestUser = "cn=somenewtestuser,o=users";
    	
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", E_DIR_SERVER_HOST + ":" + E_DIR_SERVER_PORT);
        config.put("user", E_DIR_ADMIN);
        config.put("password", E_DIR_ADMIN_PASS);
        config.put("use-tls", "true");
        config.put("trust-all-certs", "true");

        connector.setup(config);

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(someNewTestUser));
        attrs.put("objectClass", Arrays.asList("person"));
        attrs.put("cn", Arrays.asList("gmathis"));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("fullName", Arrays.asList("Gordon Mathis"));
        attrs.put("userPassword", Arrays.asList("trivir"));

        connector.opDeleteObject(attrs);

        connector.opAddObject(attrs);
        
        String newPassword = "Trivir123REsetMe";

        attrs = new TreeMap<String, Collection<String>>();
        attrs.put("dn", Arrays.asList(someNewTestUser));
        attrs.put("userPassword", Arrays.asList(newPassword));

        connector.opResetUserPassword(attrs);

        // Now login with new password
        config.put("user", someNewTestUser);
        config.put("server", E_DIR_SERVER_HOST + ":" + E_DIR_SERVER_PORT);
        config.put("use-tls", "true");
        config.put("password", newPassword);
        
        connector.setup(config);

        attrs.remove("newPassword");
        attrs.put("userPassword", Arrays.asList(newPassword));

        connector.opValidateObject(attrs);
    }
    
    public void testResetPasswordADUnicode() throws IdMUnitException {
    	String someNewTestUser = AD_TEST_USER;
    	
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", AD_SERVER_HOST + ":" + AD_SERVER_PORT);
        config.put("user", AD_ADMIN);
        config.put("password", AD_ADMIN_PASS);
        config.put("use-tls", "true");
        config.put("trust-all-certs", "true");

        // currently fails, as it appears our AD server doesn't have SSL enabled.
        connector.setup(config);

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(someNewTestUser));
        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("sAMAccountName", Arrays.asList("gmathis"));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("userAccountControl", Arrays.asList("512"));
        attrs.put("unicodePwd", Arrays.asList("Trivir1234"));

        connector.opDeleteObject(attrs);

        connector.opAddObject(attrs);
        
        String newPassword = "Trivir123REsetMe";

        Map<String, Collection<String>> attrsUser = new TreeMap<String, Collection<String>>();
        attrsUser = new TreeMap<String, Collection<String>>();
        attrsUser.put("dn", Arrays.asList(someNewTestUser));
        attrsUser.put("unicodePwd", Arrays.asList(newPassword));

                
        connector.opResetUserPasswordUnicode(attrsUser);

        // Now login with new password
        config.put("user", someNewTestUser);
        config.put("server", AD_SERVER_HOST + ":" + AD_SERVER_PORT);
        config.put("use-tls", "true");
        config.put("password", newPassword);
        
        LdapConnector connectorTestAuth = new LdapConnector();
        connectorTestAuth.setup(config);

        attrsUser.remove("newPassword");
        attrsUser.put("userPassword", Arrays.asList(newPassword));

        connectorTestAuth.opValidateObject(attrsUser);
        
        connector.opDeleteObject(attrs);
    }
    
    
    public void testChangePasswordOpenDj() throws IdMUnitException, Exception {
        final String topLevelSuffix = "dc=example,dc=com";
        final String authCn = "Directory Manager";
        final String server = OPENDJ_SERVER_HOST + ":" + OPENDJ_SERVER_PORT;
        final String authPassword = "trivir";

        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", server);
        config.put("user", String.format("CN=%s", authCn));
        config.put("password", authPassword);
        config.put("KEYSTORE-path", KEYSTORE);
//        config.put("use-ssl", "true");
//        config.put("trust-all-certs", "true");

//        writeCertificatesToKeyStore(KEYSTORE, KEY_STORE_PASSPHRASE.toCharArray(), "test", OPENDJ_SERVER_HOST, OPENDJ_SERVER_PORT);

        try {
            connector.setup(config);
        } catch (IdMUnitException e) {
            throw e;
        } finally {
            new File(KEYSTORE).delete();
        }

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(String.format("uid=user.102,ou=People,%s", topLevelSuffix)));
        attrs.put("objectClass", Arrays.asList("person"));
        attrs.put("objectClass", Arrays.asList("organizationalPerson"));
        attrs.put("objectClass", Arrays.asList("inetOrgPerson"));
        attrs.put("cn", Arrays.asList("bwayne"));
        attrs.put("givenName", Arrays.asList("Bruce"));
        attrs.put("sn", Arrays.asList("Wayne"));
        attrs.put("displayName", Arrays.asList("Bruce Wayne"));
        attrs.put("userPassword", Arrays.asList("trivir"));

        connector.opDeleteObject(attrs);

        connector.opAddObject(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(String.format("uid=user.102,ou=People,%s", topLevelSuffix)));
        attrs.put("oldUserPassword", Arrays.asList("trivir"));
        attrs.put("userPassword", Arrays.asList("Trivir123"));

        connector.opChangeUserPassword(attrs);

        // Now login with new password
        config.put("user", String.format("CN=%s", authCn));
        config.put("server", server);
        config.put("use-tls", "false");
        config.put("password", authPassword);

        try {
            connector.setup(config);
        } finally {
            new File(JSSECACERTS).delete();
        }

        attrs.put("userPassword", Arrays.asList("Trivir123"));
        attrs.remove("newPassword");

        connector.opValidateObject(attrs);
    }

    public void testDeleteUserDoesNotExistOpenDJ() throws IdMUnitException {
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", OPENDJ_SERVER_HOST + ":" + OPENDJ_SERVER_PORT);
        config.put("user", "CN=Directory Manager");
        config.put("password", "trivir");
        config.put("KEYSTORE-path", KEYSTORE);

        try {
            connector.setup(config);
        } catch (IdMUnitException e) {
            throw e;
        } finally {
            new File(KEYSTORE).delete();
        }

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList("CN=bwayne,OU=users,OU=AU,DC=american,DC=edu"));
        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList("gmathis"));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("displayName", Arrays.asList("Gordon Mathis"));
        attrs.put("sAMAccountName", Arrays.asList("gmathis"));
        attrs.put("userAccountControl", Arrays.asList("512"));
        attrs.put("userPrincipalName", Arrays.asList("gmathis@american.edu"));
        attrs.put("unicodePwd", Arrays.asList("trivir"));

        connector.opDeleteObject(attrs);
    }

    public void testDeleteUserDoesNotExist() throws IdMUnitException {
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", E_DIR_SERVER_HOST + ":" + E_DIR_SERVER_PORT);
        config.put("user", E_DIR_ADMIN);
        config.put("password", E_DIR_ADMIN_PASS);
        //config.put("KEYSTORE-path", KEYSTORE);
        config.put("use-ssl", "true");
        config.put("trust-all-certs", "true");

        try {
            connector.setup(config);
        } catch (IdMUnitException e) {
            throw e;
        } finally {
            new File(KEYSTORE).delete();
        }

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList("CN=bwaynuserodesntexist,o=users"));
        attrs.put("cn", Arrays.asList("gmathis"));

        connector.opDeleteObject(attrs);
    }


    private void setupEdirConnector() throws Exception {
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", E_DIR_SERVER_HOST + ":" + E_DIR_SERVER_PORT);
        config.put("user", E_DIR_ADMIN);
        config.put("password", E_DIR_ADMIN_PASS);
//        config.put("KEYSTORE-path", KEYSTORE);
        config.put("use-ssl", "true");
        config.put("trust-all-certs", "true");

        KeyStoreHelper.writeCertificatesToKeyStore(KEYSTORE, KEY_STORE_PASSPHRASE.toCharArray(), "test", E_DIR_SERVER_HOST, E_DIR_SERVER_PORT);

        try {
            connector.setup(config);
        } catch (IdMUnitException e) {
            throw e;
        } finally {
            new File(KEYSTORE).delete();
        }
    }

    public void testAddAttrBin() throws Exception {

        final String cn = "testBin";
        final String dn = "CN=" + cn + "," + TEST_CONTAINER_DN;
        final String binFileUri = "./test/binary/test1.bin";

        setupEdirConnector();

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));

        connector.opDeleteObject(attrs);

        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList(cn));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("userPassword", Arrays.asList("trivir"));

        connector.opAddObject(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));
        attrs.put(BIN_TEST_ATTR, Arrays.asList(binFileUri));

        connector.opAddAttr(attrs);

        connector.opValidateObject(attrs);

        connector.opDeleteObject(attrs);

    }

    public void testAddAttr2Bin() throws Exception {

        final String cn = "testBin";
        final String dn = "CN=" + cn + "," + TEST_CONTAINER_DN;
        final String binFile1Uri = "./test/binary/test1.bin";
        final String binFile2Uri = "./test/binary/test2.bin";


        setupEdirConnector();

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));

        connector.opDeleteObject(attrs);

        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList(cn));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("userPassword", Arrays.asList("trivir"));

        connector.opAddObject(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));
        attrs.put(BIN_TEST_ATTR, Arrays.asList(binFile1Uri));

        connector.opAddAttr(attrs);

        attrs.put(BIN_TEST_ATTR, Arrays.asList(binFile2Uri));
        connector.opAddAttr(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));
        attrs.put(BIN_TEST_ATTR, Arrays.asList(binFile1Uri, binFile2Uri));

        connector.opValidateObject(attrs);

        connector.opDeleteObject(attrs);

    }

    public void testAddAttrB64() throws Exception {

        final String cn = "testBin";
        final String dn = "CN=" + cn + "," + TEST_CONTAINER_DN;
        final String b64 = "MIIDXDCCAkQCCQC2foxvrn4XwDANBgkqhkiG9w0BAQsFADBxMQswCQYDVQQGEwJV\n" +
                "UzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZpcjER\n" +
                "MA8GA1UEAxMITG9vcGJhY2sxIjAgBgkqhkiG9w0BCQEWE2xvb3BiYWNrQHRyaXZp\n" +
                "ci5jb20wHhcNMTgwMzA5MDcwMzAyWhcNMjAwMzA4MDcwMzAyWjBvMQswCQYDVQQG\n" +
                "EwJVUzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZp\n" +
                "cjEQMA4GA1UEAxMHY2xpZW50MTEhMB8GCSqGSIb3DQEJARYSY2xpZW50MUB0cml2\n" +
                "aXIuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtyEMxpY/+8NU\n" +
                "kfF2+01ji2SUAtC9N379pE7tThND4fGxDbkx4KP0nx5FP/GZXONnyDrWm+aJAwxl\n" +
                "lmF/WSRsGHpUtzywFEgndkyG4+sHkxOEm+ZYA+uor9ioBDemSnkQDZHIoSK4yb5Y\n" +
                "X4a03TP4/ZMnQdMMHuqUDJ9kjW5q6mjh7fOXJZv6wWAxiw0M1wKxCTPEnXVvR2FM\n" +
                "isB6BcuNUYkTduqoG4HfdU4AJtSIvb3NiMzpUj/91vMENYbpVu8dGeDHOYMfkZGK\n" +
                "2Lu62s9RQ6HVUH7MtwjrDz3p4u7T02vLemC4qNvp/F+dCYx++femSe8twTLiAEzf\n" +
                "CpxocLMevQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBsIVf+LsMlsDWvOCCOo0tR\n" +
                "b2QHqg9oWdTHeqeqxO9Wb3TXUT6h88qDiB6n/o72/cHfrwvrYGwN/9cVQ3xQhQRa\n" +
                "+T+qf8VUN1nhNkXbAmG4GABFntQQWtNNWxvqEOMvGO8y0PKh5/LVNqTNYdnjKqrO\n" +
                "bVYepE6WYaRvBDZNbYXwmRoSAuNnePH/cMcscNzplTY9S8zCPIsz1s6lkRMg/Yjd\n" +
                "+OgqsS/hNAzWX7ALu2v7O1PIKhX9MH89vLc+R+2pO4mOh0pKZMmtcy31ePVTkWcW\n" +
                "u+DTHVEcEkeRBJld4veAi01Pisea7B5sBJ6/dbGb/VWS0e3QyTNro0WnwqO7pdxh";

        setupEdirConnector();

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));

        connector.opDeleteObject(attrs);

        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList(cn));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("userPassword", Arrays.asList("trivir"));

        connector.opAddObject(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));
        attrs.put(B64_TEST_ATTR, Arrays.asList(b64));

        connector.opAddAttr(attrs);

        connector.opValidateObject(attrs);

        connector.opDeleteObject(attrs);

    }

    public void testAddAttr2B64() throws Exception {

        final String cn = "testBin";
        final String dn = "CN=" + cn + "," + TEST_CONTAINER_DN;
        final String b64_1 = "MIIDXDCCAkQCCQC2foxvrn4XwDANBgkqhkiG9w0BAQsFADBxMQswCQYDVQQGEwJV\n" +
                "UzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZpcjER\n" +
                "MA8GA1UEAxMITG9vcGJhY2sxIjAgBgkqhkiG9w0BCQEWE2xvb3BiYWNrQHRyaXZp\n" +
                "ci5jb20wHhcNMTgwMzA5MDcwMzAyWhcNMjAwMzA4MDcwMzAyWjBvMQswCQYDVQQG\n" +
                "EwJVUzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZp\n" +
                "cjEQMA4GA1UEAxMHY2xpZW50MTEhMB8GCSqGSIb3DQEJARYSY2xpZW50MUB0cml2\n" +
                "aXIuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtyEMxpY/+8NU\n" +
                "kfF2+01ji2SUAtC9N379pE7tThND4fGxDbkx4KP0nx5FP/GZXONnyDrWm+aJAwxl\n" +
                "lmF/WSRsGHpUtzywFEgndkyG4+sHkxOEm+ZYA+uor9ioBDemSnkQDZHIoSK4yb5Y\n" +
                "X4a03TP4/ZMnQdMMHuqUDJ9kjW5q6mjh7fOXJZv6wWAxiw0M1wKxCTPEnXVvR2FM\n" +
                "isB6BcuNUYkTduqoG4HfdU4AJtSIvb3NiMzpUj/91vMENYbpVu8dGeDHOYMfkZGK\n" +
                "2Lu62s9RQ6HVUH7MtwjrDz3p4u7T02vLemC4qNvp/F+dCYx++femSe8twTLiAEzf\n" +
                "CpxocLMevQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBsIVf+LsMlsDWvOCCOo0tR\n" +
                "b2QHqg9oWdTHeqeqxO9Wb3TXUT6h88qDiB6n/o72/cHfrwvrYGwN/9cVQ3xQhQRa\n" +
                "+T+qf8VUN1nhNkXbAmG4GABFntQQWtNNWxvqEOMvGO8y0PKh5/LVNqTNYdnjKqrO\n" +
                "bVYepE6WYaRvBDZNbYXwmRoSAuNnePH/cMcscNzplTY9S8zCPIsz1s6lkRMg/Yjd\n" +
                "+OgqsS/hNAzWX7ALu2v7O1PIKhX9MH89vLc+R+2pO4mOh0pKZMmtcy31ePVTkWcW\n" +
                "u+DTHVEcEkeRBJld4veAi01Pisea7B5sBJ6/dbGb/VWS0e3QyTNro0WnwqO7pdxh";
        final String b64_2 = "MIIDXDCCAkQCCQC2foxvrn4XwTANBgkqhkiG9w0BAQsFADBxMQswCQYDVQQGEwJV\n" +
                "UzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZpcjER\n" +
                "MA8GA1UEAxMITG9vcGJhY2sxIjAgBgkqhkiG9w0BCQEWE2xvb3BiYWNrQHRyaXZp\n" +
                "ci5jb20wHhcNMTgwMzA5MDcxMTQ2WhcNMjAwMzA4MDcxMTQ2WjBvMQswCQYDVQQG\n" +
                "EwJVUzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZp\n" +
                "cjEQMA4GA1UEAxMHY2xpZW50MjEhMB8GCSqGSIb3DQEJARYSY2xpZW50MkB0cml2\n" +
                "aXIuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxMtg5EIGUTEo\n" +
                "bsQ8hRzOLsoUDJqKRMZ/EpnKPvIOgxGQRwIoGFHUUxWlHmlvDM7x2C03lGPWWwMp\n" +
                "LsxeUVsDQ96UuQZ8T9CHEsoqyaVopWgkcO3I/cq8bpY95o9qD4fkWijtMizUAWfT\n" +
                "j/TN8Esa/VBc2jpqO7hOKSLcpiT8fTumVgRIpd6bWyS34kVVTQTtW6itJzIAMdY0\n" +
                "sPqU3snkydsHTU8RZyZhaAgCJLIy9lREm51t/GUrcyaPUNmQiq3HD6X6+cgDI3hf\n" +
                "kdRPbEPfGxFFoVZTJMg7HTHed3jjtdxvr2WuoBZ1CRx+hkKGi2+1+zGkruTlwDI8\n" +
                "J1RYsTD/WwIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBtA5WDLTjk4/flJ1NWqTSF\n" +
                "/0uMRr7Uai5QJ38Pd2HuYEv335cXWmAMJVLO1KmLc+eJ89/OnWHiyxPsT1xJz9Ak\n" +
                "TniiMEvldtUjRseoPv83Hx/D5nk38NJAKEcRK8P/xA/bz7BN25QZdhy4p/vf/0Xn\n" +
                "WVTja5eRJP09ZXx5igfdp9GVNXDQalucNhjhBUyVQQebVKXEXBeh/bUzqQi6Dx9j\n" +
                "KVtwrEXunFAPA9uxE+m63lCYEB5zu2uITuZtUsOnNvAcKe/t8t9oaKABnLk4QcLO\n" +
                "IaRINV8TqhkDfsqTmorKEnxNUK6AVii0y2Q+hBBFQ8qOFmv9VwQWd6vcB3EI/NVe";


        setupEdirConnector();

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));

        connector.opDeleteObject(attrs);

        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList(cn));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("userPassword", Arrays.asList("trivir"));

        connector.opAddObject(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));
        attrs.put(B64_TEST_ATTR, Arrays.asList(b64_1));

        connector.opAddAttr(attrs);

        attrs.put(B64_TEST_ATTR, Arrays.asList(b64_2));

        connector.opAddAttr(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));
        attrs.put(B64_TEST_ATTR, Arrays.asList(b64_2, b64_1));

        connector.opValidateObject(attrs);

        connector.opDeleteObject(attrs);

    }

    public void testReplaceAttrBin() throws Exception {

        final String cn = "testBin";
        final String dn = "CN=" + cn + "," + TEST_CONTAINER_DN;
        final String binFile1Uri = "./test/binary/test1.bin";
        final String binFile2Uri = "./test/binary/test2.bin";

        setupEdirConnector();

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));

        connector.opDeleteObject(attrs);

        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList(cn));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("userPassword", Arrays.asList("trivir"));
        attrs.put(BIN_TEST_ATTR, Arrays.asList(binFile1Uri));

        connector.opAddObject(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));
        attrs.put(BIN_TEST_ATTR, Arrays.asList(binFile2Uri));

        connector.opReplaceAttr(attrs);

        connector.opValidateObject(attrs);

        connector.opDeleteObject(attrs);
    }

    public void testReplaceAttrB64() throws Exception {

        final String cn = "testBin";
        final String dn = "CN=" + cn + "," + TEST_CONTAINER_DN;
        final String b64_1 = "MIIDXDCCAkQCCQC2foxvrn4XwDANBgkqhkiG9w0BAQsFADBxMQswCQYDVQQGEwJV\n" +
                "UzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZpcjER\n" +
                "MA8GA1UEAxMITG9vcGJhY2sxIjAgBgkqhkiG9w0BCQEWE2xvb3BiYWNrQHRyaXZp\n" +
                "ci5jb20wHhcNMTgwMzA5MDcwMzAyWhcNMjAwMzA4MDcwMzAyWjBvMQswCQYDVQQG\n" +
                "EwJVUzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZp\n" +
                "cjEQMA4GA1UEAxMHY2xpZW50MTEhMB8GCSqGSIb3DQEJARYSY2xpZW50MUB0cml2\n" +
                "aXIuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtyEMxpY/+8NU\n" +
                "kfF2+01ji2SUAtC9N379pE7tThND4fGxDbkx4KP0nx5FP/GZXONnyDrWm+aJAwxl\n" +
                "lmF/WSRsGHpUtzywFEgndkyG4+sHkxOEm+ZYA+uor9ioBDemSnkQDZHIoSK4yb5Y\n" +
                "X4a03TP4/ZMnQdMMHuqUDJ9kjW5q6mjh7fOXJZv6wWAxiw0M1wKxCTPEnXVvR2FM\n" +
                "isB6BcuNUYkTduqoG4HfdU4AJtSIvb3NiMzpUj/91vMENYbpVu8dGeDHOYMfkZGK\n" +
                "2Lu62s9RQ6HVUH7MtwjrDz3p4u7T02vLemC4qNvp/F+dCYx++femSe8twTLiAEzf\n" +
                "CpxocLMevQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBsIVf+LsMlsDWvOCCOo0tR\n" +
                "b2QHqg9oWdTHeqeqxO9Wb3TXUT6h88qDiB6n/o72/cHfrwvrYGwN/9cVQ3xQhQRa\n" +
                "+T+qf8VUN1nhNkXbAmG4GABFntQQWtNNWxvqEOMvGO8y0PKh5/LVNqTNYdnjKqrO\n" +
                "bVYepE6WYaRvBDZNbYXwmRoSAuNnePH/cMcscNzplTY9S8zCPIsz1s6lkRMg/Yjd\n" +
                "+OgqsS/hNAzWX7ALu2v7O1PIKhX9MH89vLc+R+2pO4mOh0pKZMmtcy31ePVTkWcW\n" +
                "u+DTHVEcEkeRBJld4veAi01Pisea7B5sBJ6/dbGb/VWS0e3QyTNro0WnwqO7pdxh";
        final String b64_2 = "MIIDXDCCAkQCCQC2foxvrn4XwTANBgkqhkiG9w0BAQsFADBxMQswCQYDVQQGEwJV\n" +
                "UzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZpcjER\n" +
                "MA8GA1UEAxMITG9vcGJhY2sxIjAgBgkqhkiG9w0BCQEWE2xvb3BiYWNrQHRyaXZp\n" +
                "ci5jb20wHhcNMTgwMzA5MDcxMTQ2WhcNMjAwMzA4MDcxMTQ2WjBvMQswCQYDVQQG\n" +
                "EwJVUzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZp\n" +
                "cjEQMA4GA1UEAxMHY2xpZW50MjEhMB8GCSqGSIb3DQEJARYSY2xpZW50MkB0cml2\n" +
                "aXIuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxMtg5EIGUTEo\n" +
                "bsQ8hRzOLsoUDJqKRMZ/EpnKPvIOgxGQRwIoGFHUUxWlHmlvDM7x2C03lGPWWwMp\n" +
                "LsxeUVsDQ96UuQZ8T9CHEsoqyaVopWgkcO3I/cq8bpY95o9qD4fkWijtMizUAWfT\n" +
                "j/TN8Esa/VBc2jpqO7hOKSLcpiT8fTumVgRIpd6bWyS34kVVTQTtW6itJzIAMdY0\n" +
                "sPqU3snkydsHTU8RZyZhaAgCJLIy9lREm51t/GUrcyaPUNmQiq3HD6X6+cgDI3hf\n" +
                "kdRPbEPfGxFFoVZTJMg7HTHed3jjtdxvr2WuoBZ1CRx+hkKGi2+1+zGkruTlwDI8\n" +
                "J1RYsTD/WwIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBtA5WDLTjk4/flJ1NWqTSF\n" +
                "/0uMRr7Uai5QJ38Pd2HuYEv335cXWmAMJVLO1KmLc+eJ89/OnWHiyxPsT1xJz9Ak\n" +
                "TniiMEvldtUjRseoPv83Hx/D5nk38NJAKEcRK8P/xA/bz7BN25QZdhy4p/vf/0Xn\n" +
                "WVTja5eRJP09ZXx5igfdp9GVNXDQalucNhjhBUyVQQebVKXEXBeh/bUzqQi6Dx9j\n" +
                "KVtwrEXunFAPA9uxE+m63lCYEB5zu2uITuZtUsOnNvAcKe/t8t9oaKABnLk4QcLO\n" +
                "IaRINV8TqhkDfsqTmorKEnxNUK6AVii0y2Q+hBBFQ8qOFmv9VwQWd6vcB3EI/NVe";

        setupEdirConnector();

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));

        connector.opDeleteObject(attrs);

        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList(cn));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("userPassword", Arrays.asList("trivir"));
        attrs.put(B64_TEST_ATTR, Arrays.asList(b64_1));

        connector.opAddObject(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));
        attrs.put(B64_TEST_ATTR, Arrays.asList(b64_2));

        connector.opReplaceAttr(attrs);

        connector.opValidateObject(attrs);

        connector.opDeleteObject(attrs);
    }

    public void testRemoveAttrBin() throws Exception {

        final String cn = "testBin";
        final String dn = "CN=" + cn + "," + TEST_CONTAINER_DN;
        final String binFile1Uri = "./test/binary/test1.bin";
        final String binFile2Uri = "./test/binary/test2.bin";

        setupEdirConnector();

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));

        connector.opDeleteObject(attrs);

        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList(cn));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("userPassword", Arrays.asList("trivir"));
        attrs.put(BIN_TEST_ATTR, Arrays.asList(binFile1Uri, binFile2Uri));

        connector.opAddObject(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));
        attrs.put(BIN_TEST_ATTR, Arrays.asList(binFile1Uri));

        connector.opRemoveAttr(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));
        attrs.put(BIN_TEST_ATTR, Arrays.asList(binFile2Uri));

        connector.opValidateObject(attrs);

        connector.opDeleteObject(attrs);

    }

    public void testRemoveAttrB64() throws Exception {

        final String cn = "testBin";
        final String dn = "CN=" + cn + "," + TEST_CONTAINER_DN;
        final String b64_1 = "MIIDXDCCAkQCCQC2foxvrn4XwDANBgkqhkiG9w0BAQsFADBxMQswCQYDVQQGEwJV\n" +
                "UzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZpcjER\n" +
                "MA8GA1UEAxMITG9vcGJhY2sxIjAgBgkqhkiG9w0BCQEWE2xvb3BiYWNrQHRyaXZp\n" +
                "ci5jb20wHhcNMTgwMzA5MDcwMzAyWhcNMjAwMzA4MDcwMzAyWjBvMQswCQYDVQQG\n" +
                "EwJVUzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZp\n" +
                "cjEQMA4GA1UEAxMHY2xpZW50MTEhMB8GCSqGSIb3DQEJARYSY2xpZW50MUB0cml2\n" +
                "aXIuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtyEMxpY/+8NU\n" +
                "kfF2+01ji2SUAtC9N379pE7tThND4fGxDbkx4KP0nx5FP/GZXONnyDrWm+aJAwxl\n" +
                "lmF/WSRsGHpUtzywFEgndkyG4+sHkxOEm+ZYA+uor9ioBDemSnkQDZHIoSK4yb5Y\n" +
                "X4a03TP4/ZMnQdMMHuqUDJ9kjW5q6mjh7fOXJZv6wWAxiw0M1wKxCTPEnXVvR2FM\n" +
                "isB6BcuNUYkTduqoG4HfdU4AJtSIvb3NiMzpUj/91vMENYbpVu8dGeDHOYMfkZGK\n" +
                "2Lu62s9RQ6HVUH7MtwjrDz3p4u7T02vLemC4qNvp/F+dCYx++femSe8twTLiAEzf\n" +
                "CpxocLMevQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBsIVf+LsMlsDWvOCCOo0tR\n" +
                "b2QHqg9oWdTHeqeqxO9Wb3TXUT6h88qDiB6n/o72/cHfrwvrYGwN/9cVQ3xQhQRa\n" +
                "+T+qf8VUN1nhNkXbAmG4GABFntQQWtNNWxvqEOMvGO8y0PKh5/LVNqTNYdnjKqrO\n" +
                "bVYepE6WYaRvBDZNbYXwmRoSAuNnePH/cMcscNzplTY9S8zCPIsz1s6lkRMg/Yjd\n" +
                "+OgqsS/hNAzWX7ALu2v7O1PIKhX9MH89vLc+R+2pO4mOh0pKZMmtcy31ePVTkWcW\n" +
                "u+DTHVEcEkeRBJld4veAi01Pisea7B5sBJ6/dbGb/VWS0e3QyTNro0WnwqO7pdxh";
        final String b64_2 = "MIIDXDCCAkQCCQC2foxvrn4XwTANBgkqhkiG9w0BAQsFADBxMQswCQYDVQQGEwJV\n" +
                "UzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZpcjER\n" +
                "MA8GA1UEAxMITG9vcGJhY2sxIjAgBgkqhkiG9w0BCQEWE2xvb3BiYWNrQHRyaXZp\n" +
                "ci5jb20wHhcNMTgwMzA5MDcxMTQ2WhcNMjAwMzA4MDcxMTQ2WjBvMQswCQYDVQQG\n" +
                "EwJVUzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZp\n" +
                "cjEQMA4GA1UEAxMHY2xpZW50MjEhMB8GCSqGSIb3DQEJARYSY2xpZW50MkB0cml2\n" +
                "aXIuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxMtg5EIGUTEo\n" +
                "bsQ8hRzOLsoUDJqKRMZ/EpnKPvIOgxGQRwIoGFHUUxWlHmlvDM7x2C03lGPWWwMp\n" +
                "LsxeUVsDQ96UuQZ8T9CHEsoqyaVopWgkcO3I/cq8bpY95o9qD4fkWijtMizUAWfT\n" +
                "j/TN8Esa/VBc2jpqO7hOKSLcpiT8fTumVgRIpd6bWyS34kVVTQTtW6itJzIAMdY0\n" +
                "sPqU3snkydsHTU8RZyZhaAgCJLIy9lREm51t/GUrcyaPUNmQiq3HD6X6+cgDI3hf\n" +
                "kdRPbEPfGxFFoVZTJMg7HTHed3jjtdxvr2WuoBZ1CRx+hkKGi2+1+zGkruTlwDI8\n" +
                "J1RYsTD/WwIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBtA5WDLTjk4/flJ1NWqTSF\n" +
                "/0uMRr7Uai5QJ38Pd2HuYEv335cXWmAMJVLO1KmLc+eJ89/OnWHiyxPsT1xJz9Ak\n" +
                "TniiMEvldtUjRseoPv83Hx/D5nk38NJAKEcRK8P/xA/bz7BN25QZdhy4p/vf/0Xn\n" +
                "WVTja5eRJP09ZXx5igfdp9GVNXDQalucNhjhBUyVQQebVKXEXBeh/bUzqQi6Dx9j\n" +
                "KVtwrEXunFAPA9uxE+m63lCYEB5zu2uITuZtUsOnNvAcKe/t8t9oaKABnLk4QcLO\n" +
                "IaRINV8TqhkDfsqTmorKEnxNUK6AVii0y2Q+hBBFQ8qOFmv9VwQWd6vcB3EI/NVe";

        setupEdirConnector();

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));

        connector.opDeleteObject(attrs);

        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList(cn));
        attrs.put("givenName", Arrays.asList("Gordon"));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put("userPassword", Arrays.asList("trivir"));
        attrs.put(B64_TEST_ATTR, Arrays.asList(b64_1, b64_2));

        connector.opAddObject(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));
        attrs.put(B64_TEST_ATTR, Arrays.asList(b64_1));

        connector.opRemoveAttr(attrs);

        attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));
        attrs.put(B64_TEST_ATTR, Arrays.asList(b64_2));

        connector.opValidateObject(attrs);

        connector.opDeleteObject(attrs);
    }

    public void testAddObjectBin() throws Exception {

        final String cn = "testBin";
        final String dn = "CN=" + cn + "," + TEST_CONTAINER_DN;
        final String binFile1Uri = "./test/binary/test1.bin";
        final String binFile2Uri = "./test/binary/test2.bin";

        setupEdirConnector();

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));

        connector.opDeleteObject(attrs);

        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList(cn));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put(BIN_TEST_ATTR, Arrays.asList(binFile1Uri, binFile2Uri));
        attrs.put("userPassword", Arrays.asList("trivir"));

        connector.opAddObject(attrs);

        attrs = new TreeMap<String, Collection<String>>();
        attrs.put("dn", Arrays.asList(dn));
        attrs.put(BIN_TEST_ATTR, Arrays.asList(binFile2Uri, binFile1Uri));

        connector.opValidateObject(attrs);

        connector.opDeleteObject(attrs);
    }

    public void testAddObjectB64FromFile() throws Exception {

        final String cn = "testBin";
        final String dn = "CN=" + cn + "," + TEST_CONTAINER_DN;
        final String b64File1Uri = "./test/binary/test1.b64";
        final String b64File2Uri = "./test/binary/test2.b64";

        setupEdirConnector();

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));

        connector.opDeleteObject(attrs);

        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList(cn));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put(B64_TEST_ATTR, Arrays.asList(b64File1Uri, b64File2Uri));
        attrs.put("userPassword", Arrays.asList("trivir"));

        connector.opAddObject(attrs);

        attrs = new TreeMap<String, Collection<String>>();
        attrs.put("dn", Arrays.asList(dn));
        attrs.put(B64_TEST_ATTR, Arrays.asList(b64File2Uri, b64File1Uri));

        connector.opValidateObject(attrs);

        connector.opDeleteObject(attrs);

    }

    public void testAddObjectB64() throws Exception {

        final String cn = "testBin";
        final String dn = "CN=" + cn + "," + TEST_CONTAINER_DN;
        final String b64_1 = "MIIDXDCCAkQCCQC2foxvrn4XwDANBgkqhkiG9w0BAQsFADBxMQswCQYDVQQGEwJV\n" +
                "UzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZpcjER\n" +
                "MA8GA1UEAxMITG9vcGJhY2sxIjAgBgkqhkiG9w0BCQEWE2xvb3BiYWNrQHRyaXZp\n" +
                "ci5jb20wHhcNMTgwMzA5MDcwMzAyWhcNMjAwMzA4MDcwMzAyWjBvMQswCQYDVQQG\n" +
                "EwJVUzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZp\n" +
                "cjEQMA4GA1UEAxMHY2xpZW50MTEhMB8GCSqGSIb3DQEJARYSY2xpZW50MUB0cml2\n" +
                "aXIuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtyEMxpY/+8NU\n" +
                "kfF2+01ji2SUAtC9N379pE7tThND4fGxDbkx4KP0nx5FP/GZXONnyDrWm+aJAwxl\n" +
                "lmF/WSRsGHpUtzywFEgndkyG4+sHkxOEm+ZYA+uor9ioBDemSnkQDZHIoSK4yb5Y\n" +
                "X4a03TP4/ZMnQdMMHuqUDJ9kjW5q6mjh7fOXJZv6wWAxiw0M1wKxCTPEnXVvR2FM\n" +
                "isB6BcuNUYkTduqoG4HfdU4AJtSIvb3NiMzpUj/91vMENYbpVu8dGeDHOYMfkZGK\n" +
                "2Lu62s9RQ6HVUH7MtwjrDz3p4u7T02vLemC4qNvp/F+dCYx++femSe8twTLiAEzf\n" +
                "CpxocLMevQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBsIVf+LsMlsDWvOCCOo0tR\n" +
                "b2QHqg9oWdTHeqeqxO9Wb3TXUT6h88qDiB6n/o72/cHfrwvrYGwN/9cVQ3xQhQRa\n" +
                "+T+qf8VUN1nhNkXbAmG4GABFntQQWtNNWxvqEOMvGO8y0PKh5/LVNqTNYdnjKqrO\n" +
                "bVYepE6WYaRvBDZNbYXwmRoSAuNnePH/cMcscNzplTY9S8zCPIsz1s6lkRMg/Yjd\n" +
                "+OgqsS/hNAzWX7ALu2v7O1PIKhX9MH89vLc+R+2pO4mOh0pKZMmtcy31ePVTkWcW\n" +
                "u+DTHVEcEkeRBJld4veAi01Pisea7B5sBJ6/dbGb/VWS0e3QyTNro0WnwqO7pdxh";
        final String b64_2 = "MIIDXDCCAkQCCQC2foxvrn4XwTANBgkqhkiG9w0BAQsFADBxMQswCQYDVQQGEwJV\n" +
                "UzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZpcjER\n" +
                "MA8GA1UEAxMITG9vcGJhY2sxIjAgBgkqhkiG9w0BCQEWE2xvb3BiYWNrQHRyaXZp\n" +
                "ci5jb20wHhcNMTgwMzA5MDcxMTQ2WhcNMjAwMzA4MDcxMTQ2WjBvMQswCQYDVQQG\n" +
                "EwJVUzELMAkGA1UECBMCVVQxDTALBgNVBAcTBExlaGkxDzANBgNVBAoTBlRyaVZp\n" +
                "cjEQMA4GA1UEAxMHY2xpZW50MjEhMB8GCSqGSIb3DQEJARYSY2xpZW50MkB0cml2\n" +
                "aXIuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxMtg5EIGUTEo\n" +
                "bsQ8hRzOLsoUDJqKRMZ/EpnKPvIOgxGQRwIoGFHUUxWlHmlvDM7x2C03lGPWWwMp\n" +
                "LsxeUVsDQ96UuQZ8T9CHEsoqyaVopWgkcO3I/cq8bpY95o9qD4fkWijtMizUAWfT\n" +
                "j/TN8Esa/VBc2jpqO7hOKSLcpiT8fTumVgRIpd6bWyS34kVVTQTtW6itJzIAMdY0\n" +
                "sPqU3snkydsHTU8RZyZhaAgCJLIy9lREm51t/GUrcyaPUNmQiq3HD6X6+cgDI3hf\n" +
                "kdRPbEPfGxFFoVZTJMg7HTHed3jjtdxvr2WuoBZ1CRx+hkKGi2+1+zGkruTlwDI8\n" +
                "J1RYsTD/WwIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBtA5WDLTjk4/flJ1NWqTSF\n" +
                "/0uMRr7Uai5QJ38Pd2HuYEv335cXWmAMJVLO1KmLc+eJ89/OnWHiyxPsT1xJz9Ak\n" +
                "TniiMEvldtUjRseoPv83Hx/D5nk38NJAKEcRK8P/xA/bz7BN25QZdhy4p/vf/0Xn\n" +
                "WVTja5eRJP09ZXx5igfdp9GVNXDQalucNhjhBUyVQQebVKXEXBeh/bUzqQi6Dx9j\n" +
                "KVtwrEXunFAPA9uxE+m63lCYEB5zu2uITuZtUsOnNvAcKe/t8t9oaKABnLk4QcLO\n" +
                "IaRINV8TqhkDfsqTmorKEnxNUK6AVii0y2Q+hBBFQ8qOFmv9VwQWd6vcB3EI/NVe";

        setupEdirConnector();

        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>();

        attrs.put("dn", Arrays.asList(dn));

        connector.opDeleteObject(attrs);

        attrs.put("objectClass", Arrays.asList("user"));
        attrs.put("cn", Arrays.asList(cn));
        attrs.put("sn", Arrays.asList("Mathis"));
        attrs.put(B64_TEST_ATTR, Arrays.asList(b64_1, b64_2));
        attrs.put("userPassword", Arrays.asList("trivir"));

        connector.opAddObject(attrs);

        attrs = new TreeMap<String, Collection<String>>();
        attrs.put("dn", Arrays.asList(dn));
        attrs.put(B64_TEST_ATTR, Arrays.asList(b64_2, b64_1));

        connector.opValidateObject(attrs);

        connector.opDeleteObject(attrs);

    }
}
