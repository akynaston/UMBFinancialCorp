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
package org.idmunit.extension;

import javax.activation.DataSource;
import java.io.*;

/**
 * Provides a wrapper for email content
 *
 * @author Sun Microsystems, Inc.
 * @version %I%, %G%
 * @see EmailUtil
 */
public class ByteArrayDataSource implements DataSource {
    private byte[] data;    // data
    private String type;    // content-type

    /* Create a DataSource from an input stream */
    public ByteArrayDataSource(InputStream is, String type) {
        this.type = type;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int ch;

            while ((ch = is.read()) != -1) {
            // XXX - must be made more efficient by
            // doing buffered reads, rather than one byte reads
                os.write(ch);
            }
            data = os.toByteArray();

        } catch (IOException ioex) {
            //ignore exception
        }
    }

    /* Create a DataSource from a byte array */
    public ByteArrayDataSource(byte[] data, String type) {
        this.data = data;
        this.type = type;
    }

    /* Create a DataSource from a String */
    public ByteArrayDataSource(String data, String type) {
        try {
            // Assumption that the string contains only ASCII
            // characters!  Otherwise just pass a charset into this
            // constructor and use it in getBytes()
            this.data = data.getBytes("iso-8859-1");
        } catch (UnsupportedEncodingException uex) {
            //ignore exception
        }
        this.type = type;
    }

    /**
     * Return an InputStream for the data.
     * Note - a new stream must be returned each time.
     */
    public InputStream getInputStream() throws IOException {
        if (data == null) {
            throw new IOException("no data");
        }
        return new ByteArrayInputStream(data);
    }

    public OutputStream getOutputStream() throws IOException {
        throw new IOException("cannot do this");
    }

    public String getContentType() {
        return type;
    }

    public String getName() {
        return "dummy";
    }
}
