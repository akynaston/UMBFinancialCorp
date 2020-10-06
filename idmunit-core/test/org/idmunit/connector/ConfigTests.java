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

//TODO: move to a properties file?
public class ConfigTests {

    static final String TEST_CONTAINER_DN = "o=users";
    static final String OCTET_TEST_ATTR = "jpegPhoto";

    static final String AD_SERVER_HOST = "10.10.30.251";
    static final int AD_SERVER_PORT = 636;
    static final String AD_ADMIN = "cn=Administrator,CN=users,DC=idmunit-test-windows,DC=dock,DC=trivir,DC=com";
    static final String AD_ADMIN_PASS = "Trivir#1";
    static final String AD_TEST_USER = "cn=someTestUserInAD2,CN=Users,DC=idmunit-test-windows,DC=dock,DC=trivir,DC=com";
    
    static final String E_DIR_SERVER_HOST = "10.10.30.249";
    static final int E_DIR_SERVER_PORT = 636;
    static final String E_DIR_ADMIN = "cn=admin,o=services";
    static final String E_DIR_ADMIN_PASS = "trivir";

    static final String OPENDJ_SERVER_HOST = "10.10.30.249";
    static final int OPENDJ_SERVER_PORT = 1389;

    static final String KEYSTORE = "testkeystore";
    static final String KEY_STORE_PASSPHRASE = "changeit";
}
