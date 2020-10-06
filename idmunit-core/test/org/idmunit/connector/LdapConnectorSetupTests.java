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
import sun.security.provider.X509Factory;

import javax.net.ssl.*;
import java.io.*;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.TreeMap;

import static org.idmunit.connector.ConfigTests.E_DIR_SERVER_HOST;
import static org.idmunit.connector.ConfigTests.E_DIR_SERVER_PORT;

public class LdapConnectorSetupTests extends TestCase {
    private static final String SERVER = E_DIR_SERVER_HOST;
    private static final int PORT = E_DIR_SERVER_PORT;
    private static final String TRUSTED_CERT_FILE = "test.cer";
    private static final char SEP = File.separatorChar;
    private static final String JSSECACERTS = System.getProperty("java.home") + SEP + "lib" + SEP + "security" + SEP + "JSSECACERTS";
    private LdapConnector connector;

    private static void writeCertificatesToFile(String certFilePath, String host, int port) throws Exception {
        X509Certificate[] chain = getCertificates(host, port);

        FileOutputStream os = new FileOutputStream(certFilePath);

        try {
            for (int i = 0; i < chain.length; i++) {
                byte[] buf = chain[i].getEncoded();

                Writer wr = new OutputStreamWriter(os, Charset.forName("UTF-8"));
                try {
                    wr.write(X509Factory.BEGIN_CERT + "\n");
                    wr.write(new sun.misc.BASE64Encoder().encode(buf) + "\n");
                    wr.write(X509Factory.END_CERT + "\n");
                    wr.flush();
                } catch (IOException e) {
                    throw new Exception("Error writing certificate to file '" + certFilePath + "'");
                }
            }
        } finally {
            os.close();
        }
    }

    private static void writeCertificatesToKeyStore(String keyStorePath, char[] keyStorePassphrase, String alias, String host, int port) throws Exception {
        X509Certificate[] chain = getCertificates(host, port);

        File file = new File(keyStorePath);

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        if (file.exists()) {
            InputStream in = new FileInputStream(file);
            ks.load(in, keyStorePassphrase);
            in.close();
        } else {
            ks.load(null, null);
        }

        for (int i = 0; i < chain.length; ++i) {
            ks.setCertificateEntry(alias + "-" + i, chain[i]);
        }

        OutputStream out = new FileOutputStream(keyStorePath);
        ks.store(out, keyStorePassphrase);
        out.close();
    }

    private static X509Certificate[] getCertificates(String host, int port) throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore)null);

        X509TrustManager defaultTrustManager = (X509TrustManager)tmf.getTrustManagers()[0];

        SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);

        SSLContext context = SSLContext.getInstance("TLS");

        context.init(null, new TrustManager[]{tm}, null);

        SSLSocketFactory factory = context.getSocketFactory();

        SSLSocket socket = (SSLSocket)factory.createSocket(host, port);

        socket.setSoTimeout(5000);

        try {
            socket.startHandshake();
            socket.close();
            throw new Exception("Certificate is already trusted.");
        } catch (SSLException e) {
            //ignore exception
        } catch (IOException e) {
            //ignore exception
        }

        if (tm.chain == null) {
            throw new Exception("No certificate chain received.");
        }

        return tm.chain;
    }

    @Override
    protected void setUp() throws Exception {
        BasicConfigurator.configure();

        connector = new LdapConnector();
    }

    @Override
    protected void tearDown() throws Exception {
        connector.tearDown();
    }

    public void testTlsConnection() throws IdMUnitException, Exception {
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", SERVER + ":" + PORT);
        config.put("use-tls", "true");
        config.put("user", "cn=admin,o=Services");
        config.put("password", "trivir");
        config.put("operationalAttributes", "brown, red, green, blue , orange");

        writeCertificatesToKeyStore(JSSECACERTS, "changeit".toCharArray(), "test", SERVER, PORT);
        try {
            connector.setup(config);
        } finally {
            new File(JSSECACERTS).delete();
        }
    }

    // @TODO This test currently fails if run with the other tests.
    public void testTlsConnectionWithoutCert() throws IdMUnitException, Exception {
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", SERVER + ":" + PORT);
        config.put("use-tls", "true");
        config.put("user", "cn=admin,o=Services");
        config.put("password", "trivir");

        String certFilePath;
        // We need to create the default cert path that IDMUnit comes up with, which will be SERVER + ".cer" when PORT is 636; which we have in this test:
        certFilePath = SERVER + ".cer";

        // Start by ensuring the file isn't present; so our test confirms that it can be created automatically.
        new File(certFilePath).delete();

        try {
            assertTrue("Certificate file does not exist", new File(certFilePath).exists() == false);
            connector.setup(config);
            fail("Should have failed to connect");
        } catch (IdMUnitException e) {
            assertTrue("Certificate file exists", new File(certFilePath).exists());
        } finally {
            new File(certFilePath).delete();
        }
    }

    public void testTlsConnectionUsingKeyStore() throws IdMUnitException, Exception {
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", SERVER + ":" + PORT);
        config.put("user", "cn=admin,o=Services");
        config.put("password", "trivir");
        config.put("KEYSTORE-path", ConfigTests.KEYSTORE);


        writeCertificatesToKeyStore(ConfigTests.KEYSTORE, ConfigTests.KEY_STORE_PASSPHRASE.toCharArray(), "test", SERVER, PORT);
        try {
            connector.setup(config);
        } finally {
            new File(ConfigTests.KEYSTORE).delete();
        }
    }

    public void testTlsConnectionUsingCertFile() throws IdMUnitException, Exception {
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", SERVER + ":" + PORT);
        config.put("user", "cn=admin,o=Services");
        config.put("password", "trivir");
        config.put("trusted-cert-file", TRUSTED_CERT_FILE);

        writeCertificatesToFile(TRUSTED_CERT_FILE, SERVER, PORT);
        try {
            connector.setup(config);
        } finally {
            new File(TRUSTED_CERT_FILE).delete();
        }
    }

    public void testTlsConnectionTrustAll() throws IdMUnitException, Exception {
        Map<String, String> config = new TreeMap<String, String>();
        config.put("server", SERVER + ":" + PORT);
        config.put("user", "cn=admin,o=Services");
        config.put("password", "trivir");
        config.put("trust-all-certs", "true");

        connector.setup(config);
    }

    private static class SavingTrustManager implements X509TrustManager {
        private final X509TrustManager tm;
        private X509Certificate[] chain;

        SavingTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        public void checkClientTrusted(X509Certificate[] certChain, String authType) throws CertificateException {
            throw new UnsupportedOperationException();
        }

        public void checkServerTrusted(X509Certificate[] certChain, String authType) throws CertificateException {
            this.chain = certChain;
            tm.checkServerTrusted(certChain, authType);
        }

        public X509Certificate[] getAcceptedIssuers() {
            throw new UnsupportedOperationException();
        }
    }
}
