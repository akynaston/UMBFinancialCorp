package com.trivir.idm.driver.ace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.novell.nds.dirxml.driver.XmlDocument;
import com.novell.nds.dirxml.driver.XmlQueryProcessor;
import com.novell.nds.dirxml.driver.xds.CommandElement;
import com.novell.nds.dirxml.driver.xds.StatusLevel;
import com.novell.nds.dirxml.driver.xds.StatusType;
import com.novell.nds.dirxml.driver.xds.ValueType;
import com.novell.nds.dirxml.driver.xds.XDSAttrElement;
import com.novell.nds.dirxml.driver.xds.XDSCommandDocument;
import com.novell.nds.dirxml.driver.xds.XDSCommandResultDocument;
import com.novell.nds.dirxml.driver.xds.XDSInstanceElement;
import com.novell.nds.dirxml.driver.xds.XDSParseException;
import com.novell.nds.dirxml.driver.xds.XDSQueryElement;
import com.novell.nds.dirxml.driver.xds.XDSReadAttrElement;
import com.novell.nds.dirxml.driver.xds.XDSSearchAttrElement;
import com.novell.nds.dirxml.driver.xds.XDSValueElement;
import com.trivir.ace.AceToolkitException;
import com.trivir.ace.api.AceApi;

@SuppressWarnings("unchecked")
class QueryHandler implements XmlQueryProcessor {

    private AceDriverShim driver;
    private DataModel rsaData;

    QueryHandler(AceDriverShim driver, DataModel rsaData) {
        this.driver = driver;
        this.rsaData = rsaData;
    }

    /*
    <nds dtdversion="1.1" ndsversion="8.6">
        <source>
            <product version="1.1a">DirXML</product>
            <contact>Novell, Inc.</contact>
        </source>
        <input>
            <query class-name="User" event-id="0">
                <search-class class-name="User"/>
                <search-attr attr-name="Surname">
                    <value timestamp="1040071990#3" type="string">a</value>
                </search-attr>
                <search-attr attr-name="Telephone Number">
                    <value timestamp="1040072034#1" type="teleNumber">222-2222</value>
                </search-attr>
                <read-attr/>
            </query>
        </input>
    </nds>
     */

    //example query result:

    /*
        <nds dtdversion="1.1" ndsversion="8.6">
            <source>
                <product build="20021216_0123" instance="Skeleton Driver (Java, XDS)" version="1.1">DirXML Skeleton Driver (Java, XDS)</product>
                <contact>My Company Name</contact>
            </source>
            <output>
                <status event-id="0" level="success" type="driver-general"/>
            </output>
        </nds>
     */
	public XmlDocument query(XmlDocument commandXML) {

        XDSCommandResultDocument result = new XDSCommandResultDocument();
        driver.appendSourceInfo(result);

        XDSCommandDocument commands = null;
        try {
            commands = new XDSCommandDocument(commandXML);
        } catch (XDSParseException e) {
            //command document is malformed or invalid
            return driver.createStatusDocument(StatusLevel.ERROR, StatusType.DRIVER_GENERAL, null, null, e, false, commandXML);
        }

        for (Iterator i=commands.childElements().iterator(); i.hasNext(); ) {
            CommandElement command = (CommandElement) i.next();
            if (command.getClass() == XDSQueryElement.class) {
                query((XDSQueryElement)command, result);
            } else {
                Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                        command.getEventID(), "Only queries are supported by this interface");
            }
        }

        return result.toXML();
    }

    public void query(XDSQueryElement query, XDSCommandResultDocument result) {

        String className = query.getClassName();
        if (className.equals(AceDriverShim.CLASS_TOKEN)) {
            queryToken(query, result);
        } else if (className.equals(AceDriverShim.CLASS_USER)) {
            queryUser(query, result);
        } else {
            Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                    query.getEventID(), "class '" + className + "' not supported");
        }
    }

    private void queryUser(XDSQueryElement query, XDSCommandResultDocument result)
    {
        String[] readAttrs;
        if (query.containsReadAttrElements()) {
            List attrs = query.extractReadAttrElements();
            readAttrs = new String[attrs.size()];
            for (int i=0; i<attrs.size(); ++i) {
                readAttrs[i] = ((XDSReadAttrElement)attrs.get(i)).getAttrName();
            }
        } else if (query.shouldReadAttributes()) {
            readAttrs = null;
        } else {
            readAttrs = new String[0];
        }
        
        if (query.hasSubtreeScope()) {
            String value;
            List<String> users;

            if (query.containsSearchAttrElements()) {
                List l = query.extractSearchAttrElements();
                if (l.size() != 1) {
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                            query.getEventID(), "Query using more than one search-attr is not supported.");
                    return;
                }
                
                XDSSearchAttrElement searchAttr = (XDSSearchAttrElement)l.get(0);
                String searchAttrName = searchAttr.getAttrName();
                if (searchAttrName.equals(AceApi.ATTR_DEFAULT_LOGIN) == false) {
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                            query.getEventID(), "Query using '" + searchAttrName + "' as a search attribute is not supported.");
                    return;
                }
                
                List values = searchAttr.extractValueElements();
                if (values.size() != 1) {
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                            query.getEventID(), "Query using a search-attr must have one value for search-attr.");
                    return;
                }

                value = ((XDSValueElement)values.get(0)).extractText();
                try {
                	users = rsaData.listUsersByLogin(value);
                } catch (AceToolkitException e) {
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.APP_GENERAL, query.getEventID(), "Error searching users", e, true);
                    return;
                }
            } else {
                try {
                	users = new ArrayList<String>(rsaData.listAllUsers().values());
                } catch (AceToolkitException e) {
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.APP_GENERAL, query.getEventID(), "Error searching users", e, true);
                    return;
                }
            }

            for (Iterator i=users.iterator(); i.hasNext(); ) {
                String defaultLogin = (String)i.next();
                
                try {
                    addUserInfo(result, defaultLogin, readAttrs);
                } catch (AceToolkitException e) {
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.APP_GENERAL, query.getEventID(), "Error retrieving information for " + defaultLogin, e, false);
                    return;
                } catch (AceDriverException e) {
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.APP_GENERAL, query.getEventID(), "Error retrieving information for " + defaultLogin, e, false);
                    return;
                }
            }
        } else if (query.hasEntryScope()) {
            try {
                if (readAttrs == null || readAttrs.length > 0 || rsaData.listUsersByLogin(query.extractAssociationText()).size() == 1) {
                    addUserInfo(result, query.extractAssociationText(), readAttrs);
                }
            } catch (AceToolkitException e) {
                Util.addStatus(result, StatusLevel.ERROR, StatusType.APP_GENERAL, query.getEventID(), "Error retrieving information for " + query.extractAssociationText(), e, false);
                return;
            } catch (AceDriverException e) {
                Util.addStatus(result, StatusLevel.ERROR, StatusType.APP_GENERAL, query.getEventID(), "Error retrieving information for " + query.extractAssociationText(), e, false);
                return;
            }
        }
        

        Util.addStatus(result, StatusLevel.SUCCESS, StatusType.DRIVER_GENERAL, query.getEventID());
    }

    private void addUserInfo(XDSCommandResultDocument result, String defaultLogin, String[] readAttrs) throws AceToolkitException, AceDriverException {
        Map info;
        Iterator attrNames;
        try {
            if (readAttrs == null) {
                info = rsaData.getUserAttributes(defaultLogin);
                attrNames = info.keySet().iterator();
            } else if (readAttrs.length > 0) {
                List l = Arrays.asList(readAttrs);
                info = rsaData.getUserAttributes(defaultLogin, l);
                attrNames = l.iterator();
            } else {
                info = null;
                attrNames = Collections.EMPTY_SET.iterator();
            }
        } catch (AceToolkitException e) {
            if (e.getError() == AceToolkitException.API_ERROR_INVUSR) {
                return;
            } else {
                throw e;
            }
        }

        XDSInstanceElement instance;
        instance = result.appendInstanceElement();
        instance.setClassName(AceDriverShim.CLASS_USER);
        instance.appendAssociationElement(defaultLogin);
        while (attrNames.hasNext()) {
            String attrName = (String)attrNames.next();
            Object value = info.get(attrName);
            if (value == null) {
            	continue;
            }
            
            XDSAttrElement attr = instance.appendAttrElement();
            attr.setAttrName(attrName);
            if (value instanceof Collection) {
            	for (Iterator j=((Collection)value).iterator(); j.hasNext(); ) {
                    XDSValueElement ve = attr.appendValueElement(j.next().toString());
                    ve.setType(ValueType.STRING);
            	}
            } else {
                XDSValueElement ve = attr.appendValueElement(value.toString());
                ve.setType(ValueType.STRING);
            }
        }
    }

    private void queryToken(XDSQueryElement query, XDSCommandResultDocument result) {
        List readAttrs = new ArrayList();
        if (query.shouldReadAttributes() && query.containsReadAttrElements()) {
            for (Iterator i = query.extractReadAttrElements().listIterator(); i.hasNext(); ) {
                XDSReadAttrElement readAttr = (XDSReadAttrElement) i.next();
                String attrName = readAttr.getAttrName();
                readAttrs.add(attrName);
            }
        }

        if (query.hasEntryScope()) {
            String tokenSerialNumber = query.extractAssociationText();
            if (tokenSerialNumber == null) {
                Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL, query.getEventID(), "Invalid query, missing association.");
                return;
            }

            try {
                if (query.containsReadAttrElements()) {
                    addTokenInstance(result, query.extractAssociationText(), readAttrs);
                } else {
                    addTokenInstance(result, query.extractAssociationText());
                }
            } catch (AceDriverException e) {
                Util.addStatus(result, StatusLevel.ERROR, StatusType.APP_GENERAL, query.getEventID(), "Error getting token information", e, false);
                return;
            } catch (AceToolkitException e) {
                Util.addStatus(result, StatusLevel.ERROR, StatusType.APP_GENERAL, query.getEventID(), "Error getting token information", e, false);
                return;
            }
            Util.addStatus(result, StatusLevel.SUCCESS, StatusType.DRIVER_GENERAL, query.getEventID());
        } else if (query.hasSubtreeScope()) {
            Collection tokens;

            if (query.containsSearchAttrElements()) {
                List l = query.extractSearchAttrElements();
                if (l.size() != 1) {
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                            query.getEventID(), "Query using more than one search-attr is not supported.");
                    return;
                }

                XDSSearchAttrElement searchAttr = (XDSSearchAttrElement)l.get(0);
                String searchAttrName = searchAttr.getAttrName();
                if (searchAttrName.equals(AceApi.ATTR_ASSIGNED) == false) {
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                            query.getEventID(), "Query using '" + searchAttrName + "' as a search attribute is not supported.");
                    return;
                }

                List values = searchAttr.extractValueElements();
                if (values.size() != 1) {
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                            query.getEventID(), "Query using a search-attr must have one value for search-attr.");
                    return;
                }

                if (((XDSValueElement)values.get(0)).extractText().equals("FALSE")) {
                	try {
                		tokens = rsaData.listUnassignedTokens();
                    } catch (AceToolkitException e) {
                        Util.addStatus(result, StatusLevel.ERROR, StatusType.APP_GENERAL, query.getEventID(), "Error searching tokens", e, false);
                        return;
                    }
                } else {
                	try {
                		tokens = rsaData.listAssignedTokens();
                    } catch (AceToolkitException e) {
                        Util.addStatus(result, StatusLevel.ERROR, StatusType.APP_GENERAL, query.getEventID(), "Error searching tokens", e, false);
                        return;
                    }
                }
            } else {
            	try {
            		tokens = rsaData.listAllTokens();
                } catch (AceToolkitException e) {
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.APP_GENERAL, query.getEventID(), "Error searching tokens", e, false);
                    return;
                }
            }
            for (Iterator i=tokens.iterator(); i.hasNext(); ) {
            	String serialNumber = (String)i.next();
                try {
                    addTokenInstance(result, serialNumber, readAttrs);
                } catch (AceDriverException e) {
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.APP_GENERAL, query.getEventID(), "Error getting token information", e, false);
                    return;
                } catch (AceToolkitException e) {
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.APP_GENERAL, query.getEventID(), "Error getting token information", e, false);
                    return;
                }
            }

            Util.addStatus(result, StatusLevel.SUCCESS, StatusType.DRIVER_GENERAL, query.getEventID());
        } else {            
        }
    }
    
    private void addTokenInstance(XDSCommandResultDocument result, String tokenSerialNumber) throws AceToolkitException, AceDriverException {

        // Read the info first in case there is an error.
        Map values = rsaData.listTokenInfo(tokenSerialNumber);

        XDSInstanceElement instance = result.appendInstanceElement();
        instance.setClassName(AceDriverShim.CLASS_TOKEN);
        instance.appendAssociationElement(tokenSerialNumber);

        for (Iterator i=values.keySet().iterator(); i.hasNext(); ) {
            String attrName = (String)i.next();
            Object attrValue = values.get(attrName);
            if (attrValue != null) {
                XDSAttrElement attr = instance.appendAttrElement();
                attr.setAttrName(attrName);
                attr.appendValueElement(attrValue.toString());
            }
        }
    }

    private void addTokenInstance(XDSCommandResultDocument result, String tokenSerialNumber, List readAttrs) throws AceToolkitException, AceDriverException {

        // Read the info first in case there is an error.
        Map values = Collections.EMPTY_MAP;
        if (readAttrs.size() > 0) {
            values = rsaData.listTokenInfo(tokenSerialNumber);
        }

        XDSInstanceElement instance = result.appendInstanceElement();
        instance.setClassName(AceDriverShim.CLASS_TOKEN);
        instance.appendAssociationElement(tokenSerialNumber);

        for (Iterator i=readAttrs.iterator(); i.hasNext(); ) {
            String attrName = (String)i.next();
            Object attrValue = values.get(attrName);
            if (attrValue != null) {
                XDSAttrElement attr = instance.appendAttrElement();
                attr.setAttrName(attrName);
                attr.appendValueElement(attrValue.toString());
            }
        }
    }

    /*
    trace.trace("queryHandler: has limited results?        "
            + query.hasLimitedResults());
    trace.trace("queryHandler: has unlimited results?      "
            + query.hasUnlimitedResults());
    trace.trace("queryHandler: has root base object?       "
            + query.hasRootBaseObject());
    trace.trace("queryHandler: is cancelled query?         "
            + query.isCancelled());
    trace.trace("queryHandler: is extended query?          "
            + query.isExtended());
    trace.trace("queryHandler: is identity query?          "
            + query.isIdentity());
    trace.trace("queryHandler: is initial query?           "
            + query.isInitialQuery());
    trace.trace("queryHandler: is subsequent query?        "
            + query.isSubsequentQuery());
    trace.trace("queryHandler: should read attrs?          "
            + query.shouldReadAttributes());
    trace.trace("queryHandler: should read parent?         "
            + query.shouldReadParent());
    trace.trace("queryHandler: should search all classes?  "
            + query.shouldSearchAllClasses());
            */
}
