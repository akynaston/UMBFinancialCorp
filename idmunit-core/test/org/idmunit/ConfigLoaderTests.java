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

package org.idmunit;

import junit.framework.TestCase;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import org.idmunit.connector.ConnectionConfigData;

import java.io.IOException;
import java.util.Map;

public class ConfigLoaderTests extends TestCase {
    private static final String ENCRYPTION_KEY = "IDMUNIT1";

    public void testGetConfigData() throws IdMUnitException {
        Map<String, ConnectionConfigData> config = ConfigLoader.getConfigData("./examples/test/idmunit-config.xml", "IDMUNIT1");
        ConnectionConfigData idv = config.get("IDV");
        assertEquals("172.17.2.237:636", idv.getServerURL());
        assertEquals("cn=admin,o=services", idv.getAdminCtx());
        assertEquals("trivir", idv.getAdminPwd());
        assertEquals("", idv.getKeystorePath());

        assertEquals("IDV", idv.getName());
        assertEquals("org.idmunit.connector.LdapConnector", idv.getType());
        assertEquals("Connector for the identity vault", idv.getParam("description"));
        assertEquals("172.17.2.237:636", idv.getParam("server"));
        assertEquals("cn=admin,o=services", idv.getParam("user"));
        assertEquals("trivir", idv.getParam("password"));
        assertEquals("trivir", idv.getParam("encryptedFieldExample"));
        assertEquals("", idv.getParam("keystore-path")); //TODO Carl thinks we should delete this line.
        assertEquals(0, idv.getMultiplierRetry());
        assertEquals(0, idv.getMultiplierWait());
        assertEquals(2, idv.getSubstitutions().size());
        assertEquals("172.17.2.131", idv.getSubstitutions().get("%SMTP-IP-SYSTEM1%"));
        assertEquals("172.17.2.132", idv.getSubstitutions().get("%SMTP-IP-SYSTEM2%"));
        assertEquals(2, idv.getDataInjections().size());
        assertEquals("%TODAY%", idv.getDataInjections().get(0).getKey());
        assertEquals("org.idmunit.injector.DateInjection", idv.getDataInjections().get(0).getType());
        assertEquals("yyyyMMdd", idv.getDataInjections().get(0).getFormat());
        assertEquals(null, idv.getDataInjections().get(0).getMutator());
        assertEquals("%TODAY+30%", idv.getDataInjections().get(1).getKey());
        assertEquals("org.idmunit.injector.DateInjection", idv.getDataInjections().get(1).getType());
        assertEquals("yyyyMMdd", idv.getDataInjections().get(1).getFormat());
        assertEquals("30", idv.getDataInjections().get(1).getMutator());
        assertEquals(1, idv.getIdmunitAlerts().size());
        assertEquals("Alert", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getName());
        assertEquals("Email recipients will be notified if a test marked as \"Critical\" fails", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getDescription());
        assertEquals("smtp.EXAMPLE.com", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getSmtpServer());
        assertEquals("idmunitAlerts@example.org", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getAlertSender());
        assertEquals("idmunitAlerts@example.org", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getAlertReipient());
        assertEquals("IdMUnit Test Failed: ", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getSubjectPrefix());
        assertEquals("c:/idmunitAlerts.log", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getLogPath());

        ConnectionConfigData ad = config.get("AD");
        assertEquals("AD", ad.getName());
        assertEquals("org.idmunit.connector.LdapConnector", ad.getType());
        assertEquals("Connector for Active Directory", ad.getParam("description"));
        assertEquals("172.17.2.173:636", ad.getParam("server"));
        assertEquals("CN=Administrator,CN=Users,DC=EXAMPLE,DC=ORG", ad.getParam("user"));
        assertEquals("trivir", ad.getParam("password"));
        assertEquals("", ad.getParam("keystore-path")); //TODO Carl thinks we should delete this line.
        assertEquals(0, ad.getMultiplierRetry());
        assertEquals(0, ad.getMultiplierWait());
        assertEquals(1, ad.getSubstitutions().size());
        assertEquals("DC=EXAMPLE,DC=ORG", ad.getSubstitutions().get("%AD_DOMAIN%"));
        assertEquals(2, ad.getDataInjections().size());
        assertEquals("%TODAY%", ad.getDataInjections().get(0).getKey());
        assertEquals("org.idmunit.injector.DateInjection", ad.getDataInjections().get(0).getType());
        assertEquals("yyyyMMdd", ad.getDataInjections().get(0).getFormat());
        assertEquals(null, ad.getDataInjections().get(0).getMutator());
        assertEquals("%TODAY+30%", ad.getDataInjections().get(1).getKey());
        assertEquals("org.idmunit.injector.DateInjection", ad.getDataInjections().get(1).getType());
        assertEquals("yyyyMMdd", ad.getDataInjections().get(1).getFormat());
        assertEquals("30", ad.getDataInjections().get(1).getMutator());
        assertEquals(1, ad.getIdmunitAlerts().size());
        assertEquals("Alert", ad.getIdmunitAlerts().get("idmunitAlert_Alert").getName());
        assertEquals("Email recipients will be notified if a test marked as \"Critical\" fails", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getDescription());
        assertEquals("smtp.EXAMPLE.com", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getSmtpServer());
        assertEquals("idmunitAlerts@example.org", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getAlertSender());
        assertEquals("idmunitAlerts@example.org", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getAlertReipient());
        assertEquals("IdMUnit Test Failed: ", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getSubjectPrefix());
        assertEquals("c:/idmunitAlerts.log", idv.getIdmunitAlerts().get("idmunitAlert_Alert").getLogPath());

//		ConnectionConfigData orcl = (ConnectionConfigData)config.get("ORCL");
//		assertEquals("ORCL", orcl.getName());
//		assertEquals("com.trivir.idmunit.connector.Oracle", orcl.getType());
//		assertEquals("Connector to an Remedy database on an Oracle server", orcl.getParam("description"));
//		assertEquals("jdbc:oracle:thin:@192.168.1.119:1526:REMEDY01", orcl.getParam("server"));
//		assertEquals("idmunit", orcl.getParam("user"));
//		assertEquals("trivir", orcl.getParam("password"));
//		assertEquals("", orcl.getParam("keystore-path"));
//		assertEquals(0, orcl.getMultiplierRetry());
//		assertEquals(0, orcl.getMultiplierWait());
//		assertEquals(null, orcl.getSubstitutions());
//		assertEquals(null, orcl.getDataInjections());
//		assertEquals(1, orcl.getIdmunitAlerts().size());
//		assertEquals("TriVir", orcl.getIdmunitAlerts().get("idmunitAlert_TriVir").getName());
//		assertEquals("TriVir personnel will be notified if a test marked as \"Critical\" fails", orcl.getIdmunitAlerts().get("idmunitAlert_TriVir").getDescription());
//		assertEquals("smtp.MYSERVER.com", orcl.getIdmunitAlerts().get("idmunitAlert_TriVir").getSmtpServer());
//		assertEquals("idmunitAlerts@idmunit.org", orcl.getIdmunitAlerts().get("idmunitAlert_TriVir").getAlertSender());
//		assertEquals("bkynaston@trivir.com", orcl.getIdmunitAlerts().get("idmunitAlert_TriVir").getAlertReipient());
//		assertEquals("IdMUnit Test Failed: ", orcl.getIdmunitAlerts().get("idmunitAlert_TriVir").getSubjectPrefix());
//		assertEquals("c:/idmunitAlerts.log", orcl.getIdmunitAlerts().get("idmunitAlert_TriVir").getLogPath());
//
//		ConnectionConfigData dtf = (ConnectionConfigData)config.get("DTF");
//		assertEquals("DTF", dtf.getName());
//		assertEquals("org.idmunit.connector.DTF", dtf.getType());
//		assertEquals("Connector to TriVirDTF data feed - must  map drive/share or UNC to IDM server or remote loader running the DTF driver", dtf.getParam("description"));
//		assertEquals("DriverInputFilePath=x:/input/inputFile.csv|DriverOutputFilePath=x:/output/outputFile.csv|delimiter=$", dtf.getParam("server"));
//		assertEquals("", dtf.getParam("user"));
//		assertEquals("", dtf.getParam("password"));
//		assertEquals("", dtf.getParam("keystore-path"));
//		assertEquals(0, dtf.getMultiplierRetry());
//		assertEquals(0, dtf.getMultiplierWait());
//		assertEquals(3, dtf.getSubstitutions().size());
//		assertEquals("333-333-3333", dtf.getSubstitutions().get("X3"));
//		assertEquals("222-222-2222", dtf.getSubstitutions().get("X2"));
//		assertEquals("111-111-1111", dtf.getSubstitutions().get("X1"));
//		assertEquals(null, dtf.getDataInjections());
//		assertEquals(1, dtf.getIdmunitAlerts().size());
//		assertEquals("TriVir", dtf.getIdmunitAlerts().get("idmunitAlert_TriVir").getName());
//		assertEquals("TriVir personnel will be notified if a test marked as \"Critical\" fails", dtf.getIdmunitAlerts().get("idmunitAlert_TriVir").getDescription());
//		assertEquals("smtp.MYSERVER.com", dtf.getIdmunitAlerts().get("idmunitAlert_TriVir").getSmtpServer());
//		assertEquals("idmunitAlerts@idmunit.org", dtf.getIdmunitAlerts().get("idmunitAlert_TriVir").getAlertSender());
//		assertEquals("bkynaston@trivir.com", dtf.getIdmunitAlerts().get("idmunitAlert_TriVir").getAlertReipient());
//		assertEquals("IdMUnit Test Failed: ", dtf.getIdmunitAlerts().get("idmunitAlert_TriVir").getSubjectPrefix());
//		assertEquals("c:/idmunitAlerts.log", dtf.getIdmunitAlerts().get("idmunitAlert_TriVir").getLogPath());
    }

    public void testDecryptingSubstitutionValues() throws ParsingException, IOException, IdMUnitException {
        String encryptedValue = new EncTool(ENCRYPTION_KEY).encryptCredentials("EXAMPLE,DC=ORG");

        String configString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<idmunit live-profile=\"VM\">\n"+
                "<profiles>\n"+
                    "<profile name=\"VM\">\n"+
                        "<connection>\n"+
                            "<name>AD</name>\n"+
                            "<description>Connector for Active Directory</description>\n"+
                            "<type>org.idmunit.connector.LdapConnector</type>\n"+
                            "<substitutions>\n"+
                                "<substitution>\n"+
                                    "<replace>%AD_DOMAIN%</replace>\n"+
                                    "<new encrypted=\"true\">" + encryptedValue + "</new>\n"+
                                "</substitution>\n"+
                            "</substitutions>\n"+
                        "</connection>\n" +
                    "</profile>\n"+
                "</profiles>\n"+
            "</idmunit>";

        Document configXml = new Builder().build(configString, "file://foo");
        Map<String, ConnectionConfigData> config = ConfigLoader.getConfigData(configXml, "IDMUNIT1");
        ConnectionConfigData connection = config.get("AD");
        Map<String, String> substitutions = connection.getSubstitutions();
        assertEquals("EXAMPLE,DC=ORG", substitutions.get("%AD_DOMAIN%"));
    }
}
