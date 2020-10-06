package com.trivir.idm.driver.ace;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TimeZone;

import com.novell.nds.dirxml.driver.SubscriptionShim;
import com.novell.nds.dirxml.driver.Trace;
import com.novell.nds.dirxml.driver.XmlDocument;
import com.novell.nds.dirxml.driver.XmlQueryProcessor;
import com.novell.nds.dirxml.driver.xds.CommandElement;
import com.novell.nds.dirxml.driver.xds.DTD;
import com.novell.nds.dirxml.driver.xds.StatusLevel;
import com.novell.nds.dirxml.driver.xds.StatusType;
import com.novell.nds.dirxml.driver.xds.ValueType;
import com.novell.nds.dirxml.driver.xds.XDSAddAssociationElement;
import com.novell.nds.dirxml.driver.xds.XDSAddAttrElement;
import com.novell.nds.dirxml.driver.xds.XDSAddElement;
import com.novell.nds.dirxml.driver.xds.XDSAddValueElement;
import com.novell.nds.dirxml.driver.xds.XDSAssociationElement;
import com.novell.nds.dirxml.driver.xds.XDSAttrElement;
import com.novell.nds.dirxml.driver.xds.XDSCommandDocument;
import com.novell.nds.dirxml.driver.xds.XDSCommandResultDocument;
import com.novell.nds.dirxml.driver.xds.XDSDeleteElement;
import com.novell.nds.dirxml.driver.xds.XDSElement;
import com.novell.nds.dirxml.driver.xds.XDSException;
import com.novell.nds.dirxml.driver.xds.XDSInitDocument;
import com.novell.nds.dirxml.driver.xds.XDSInstanceElement;
import com.novell.nds.dirxml.driver.xds.XDSModifyAssociationElement;
import com.novell.nds.dirxml.driver.xds.XDSModifyAttrElement;
import com.novell.nds.dirxml.driver.xds.XDSModifyElement;
import com.novell.nds.dirxml.driver.xds.XDSParseException;
import com.novell.nds.dirxml.driver.xds.XDSQueryElement;
import com.novell.nds.dirxml.driver.xds.XDSRemoveAllValuesElement;
import com.novell.nds.dirxml.driver.xds.XDSRemoveValueElement;
import com.novell.nds.dirxml.driver.xds.XDSValueElement;
import com.trivir.ace.AceToolkitException;
import com.trivir.ace.api.AceApi;
import com.trivir.ace.api.TimeUtil;
import com.trivir.util.Version;

@SuppressWarnings("unchecked")
class SubscriberShim implements SubscriptionShim {
	
	private static final String DRIVER_ID_VALUE = "RSA";
	private static final String DRIVER_MIN_ACTIVATION_VERSION = "1";

    private Trace trace;
	private AceDriverShim driver;
    DataModel rsaData;
    private QueryHandler queryHandler;
	
	SubscriberShim(AceDriverShim driver) throws AceToolkitException {
		this.driver = driver;
        trace = new Trace(driver.getDriverRDN() + "\\Subscriber");
        rsaData = driver.rsaData;
        queryHandler = new QueryHandler(driver, rsaData);
	}
    
    void shutdown() throws AceToolkitException {
    }

    //example command document:
	/*
    <nds dtdversion="1.1" ndsversion="8.6">
        <source>
            <product version="1.1a">DirXML</product>
            <contact>Novell, Inc.</contact>
        </source>
        <input>
            <add class-name="User" event-id="0" src-dn="\NEW_DELL_TREE\NOVELL\test\a" src-entry-id="33071">
                <add-attr attr-name="Surname">
                    <value timestamp="1040071990#3" type="string">a</value>
                </add-attr>
                <add-attr attr-name="Telephone Number">
                    <value timestamp="1040072034#1" type="teleNumber">111-1111</value>
                </add-attr>
            </add>
        </input>
    </nds>
	*/

    //example result document:
	/*
    <nds dtdversion="1.1" ndsversion="8.6">
        <source>
            <product build="20021216_0123" instance="Skeleton Driver (Java, XDS)" version="1.1">DirXML Skeleton Driver (Java, XDS)</product>
            <contact>My Company Name</contact>
        </source>
        <output>
            <add-association dest-dn="\NEW_DELL_TREE\NOVELL\test\a" event-id="0">1</add-association>
            <init-params>
                <subscriber-state>
                    <current-association>2</current-association>
                </subscriber-state>
            </init-params>
            <status event-id="0" level="success" type="driver-general"/>
        </output>
    </nds>
	*/
	public XmlDocument execute(XmlDocument commandXML, XmlQueryProcessor processor) {
		String eventID = null;
		
		try {
			XDSCommandResultDocument result = new XDSCommandResultDocument();
			driver.appendSourceInfo(result);

			XDSCommandDocument commands = new XDSCommandDocument(commandXML);
			if (commands.containsIdentityQuery()) {
				XDSQueryElement query;

				query = commands.identityQuery();
				eventID = query.getEventID();
				appendDriverIdentityInfo(result);

				Util.addStatus(result, StatusLevel.SUCCESS, StatusType.DRIVER_GENERAL, eventID, null);
			} else {
				ListIterator c;
				CommandElement command;

				c = commands.childElements().listIterator();
				while (c.hasNext()) {
					command = (CommandElement) c.next();
					eventID = command.getEventID();
					dispatch(command, result);
				}
			}

			return result.toXML();
		} catch (XDSException e) {
			//command document is malformed or invalid
			return driver.createStatusDocument(StatusLevel.ERROR, StatusType.DRIVER_GENERAL, null, null, e, false, commandXML);
		} catch (Exception e) {
			return driver.createStatusDocument(eventID, e, commandXML);
		}
	}

	//example initialization document:
	/*
    <nds dtdversion="1.1" ndsversion="8.6">
        <source>
            <product version="1.1a">DirXML</product>
            <contact>Novell, Inc.</contact>
        </source>
        <input>
            <init-params src-dn="\NEW_DELL_TREE\NOVELL\Driver Set\Skeleton Driver (Java, XDS)\Subscriber">
                <authentication-info>
                    <server>server.app:400</server>
                    <user>User1</user>
                </authentication-info>
                <driver-filter>
                    <allow-class class-name="User">
                        <allow-attr attr-name="Surname"/>
                        <allow-attr attr-name="Telephone Number"/>
                        <allow-attr attr-name="Given Name"/>
                    </allow-class>
                </driver-filter>
                <subscriber-options>
            <sub-1 display-name="Sample Subscriber option">String for Subscriber</sub-1>
        </subscriber-options>
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
                    <current-association display-name="current-association">1</current-association>
                    <sub-1 display-name="Sample Subscriber option">String for Subscriber</sub-1>
                </parameters>
            </status>
        </output>
    </nds>
	*/
	public XmlDocument init(XmlDocument initXML) {
        trace.trace("init", Trace.DEFAULT_TRACE);

        try {
            XDSInitDocument init = new XDSInitDocument(initXML);
            Map params = buildParamMap();
            init.parameters(params);
            
            return driver.createSuccessDocument(params);
        } catch (Exception e) {
        	return driver.createStatusDocument(e, initXML);
        }
	}

    //example identity query:
	/*
    <nds dtdversion="1.1" ndsversion="8.6">
        <source>
            <product version="1.1a">DirXML</product>
            <contact>Novell, Inc.</contact>
        </source>
        <input>
            <query event-id="query-driver-ident" scope="entry">
                <search-class class-name="__driver_identification_class__"/>
                <read-attr/>
            </query>
        </input>
    </nds>
	*/

    //example identity query response:
	/*
    <nds dtdversion="1.1" ndsversion="8.6">
        <source>
            <product build="20021214_0304" instance="Skeleton Driver (Java, XDS)" version="1.1">DirXML Skeleton Driver (Java, XDS)</product>
            <contact>My Company Name</contact>
        </source>
        <output>
            <instance class-name="__driver_identification_class__">
                <attr attr-name="driver-id">
                    <value type="string">JSKEL</value>
                </attr>
                <attr attr-name="driver-version">
                    <value type="string">1.1</value>
                </attr>
                <attr attr-name="min-activation-version">
                    <value type="string">0</value>
                </attr>
            </instance>
            <status event-id="query-driver-ident" level="success" type="driver-general"/>
        </output>
    </nds>
	*/
    private void appendDriverIdentityInfo(XDSCommandResultDocument result)
    {
        XDSInstanceElement instance = result.appendInstanceElement();
        instance.setClassName(DTD.VAL_DRIVER_IDENT_CLASS);
        XDSAttrElement attr = instance.appendAttrElement();
        attr.setAttrName(DTD.VAL_DRIVER_ID);
        attr.appendValueElement(ValueType.STRING, DRIVER_ID_VALUE);
        attr = instance.appendAttrElement();
        attr.setAttrName(DTD.VAL_DRIVER_VERSION);
        attr.appendValueElement(ValueType.STRING, Version.getVersion(this.getClass()));
        attr = instance.appendAttrElement();
        attr.setAttrName(DTD.VAL_MIN_ACTIVATION_VERSION);
        attr.appendValueElement(ValueType.STRING, DRIVER_MIN_ACTIVATION_VERSION);
    }

    private void dispatch(CommandElement command, XDSCommandResultDocument result)
    	throws XDSParseException
    {
        if (command.getClassName() == null) {
            Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL, command.getEventID(), "'" + command.tagName() + "' operation missing class-name attribute.");
            return;
        }

        if (command.getClass() == XDSAddElement.class) {
            addHandler((XDSAddElement)command, result);
        } else if (command.getClass() == XDSModifyElement.class) {
            modifyHandler((XDSModifyElement)command, result);
        } else if (command.getClass() == XDSQueryElement.class) {
            queryHandler.query((XDSQueryElement)command, result);
        } else if (command.getClass() == XDSDeleteElement.class) {
            deleteHandler((XDSDeleteElement)command, result);
    	} else {
    		trace.trace("unhandled element:  " + command.tagName(), Trace.DEFAULT_TRACE|Trace.XML_TRACE);
    		Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL, command.getEventID(), "Operation <" + command.tagName() + "> not supported");
    	}
    }

    //  example <add> element:
    /*
    <nds dtdversion="1.1" ndsversion="8.6">
	    <source>
		    <product version="1.1a">DirXML</product>
		    <contact>Novell, Inc.</contact>
	    </source>
	    <input>
		    <add class-name="User" event-id="0" src-dn="\NEW_DELL_TREE\NOVELL\test\a" src-entry-id="33071">
		        <add-attr attr-name="Surname">
		            <value timestamp="1040071990#3" type="string">a</value>
		        </add-attr>
		        <add-attr attr-name="Telephone Number">
		            <value timestamp="1040072034#1" type="teleNumber">111-1111</value>
		        </add-attr>
		    </add>
	    </input>
    </nds>
    */

    //    example add result:
    /*
    <nds dtdversion="1.1" ndsversion="8.6">
	    <source>
		    <product build="20021216_0123" instance="Skeleton Driver (Java, XDS)" version="1.1">DirXML Skeleton Driver (Java, XDS)</product>
		    <contact>My Company Name</contact>
	    </source>
	    <output>
		    <add-association dest-dn="\NEW_DELL_TREE\NOVELL\test\a" event-id="0">1</add-association>
		    <init-params>
		        <subscriber-state>
		            <current-association>2</current-association>
		        </subscriber-state>
		    </init-params>
		    <status event-id="0" level="success" type="driver-general"/>
	    </output>
    </nds>
    */
    private void addHandler(XDSAddElement add, XDSCommandResultDocument result)
    {
    	trace.trace("addHandler", Trace.DEFAULT_TRACE);

    	if (add.getClassName().equals(AceDriverShim.CLASS_USER) == false) {
    		Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
    				add.getEventID(), "class '" + add.getClassName() + "' not supported");
    		return;
    	}

        String defaultLogin = null;
        String defaultShell = null;
        String firstName = null;
        String lastName = null;
        String emailAddress = null;
        String tokenSerialNumber = null;
        String tokenPIN = null;
        String profileName = null;
        String password = null;
        XDSAddAttrElement memberOf = null;
        long start = 0;
        long end = 0;
    	
    	ListIterator addElements = add.extractAddAttrElements().listIterator();
    	while (addElements.hasNext()) {
    		XDSAddAttrElement addAttr = (XDSAddAttrElement) addElements.next();
            String attrName = addAttr.getAttrName();
            if(attrName.equals(AceApi.ATTR_DEFAULT_LOGIN)) {
            	defaultLogin = getValue(addAttr).trim();
            } else if(attrName.equals(AceApi.ATTR_DEFAULT_SHELL)) {
            	defaultShell = getValue(addAttr).trim();
            } else if(attrName.equals(AceApi.ATTR_FIRST_NAME)) {
                firstName = getValue(addAttr).trim();
            } else if(attrName.equals(AceApi.ATTR_LAST_NAME)) {
                lastName = getValue(addAttr).trim();
            } else if(attrName.equals(AceApi.ATTR_EMAIL_ADDRESS)) {
                emailAddress = getValue(addAttr).trim();
            } else if(attrName.equals(AceApi.ATTR_TOKEN_SERIAL_NUMBER)) {
                tokenSerialNumber = getValue(addAttr).trim();
            } else if(attrName.equals(AceApi.ATTR_TOKEN_PIN)) {
                tokenPIN = getValue(addAttr).trim();
            } else if(attrName.equals(AceApi.ATTR_PROFILE_NAME)) {
                profileName = getValue(addAttr).trim();
            } else if(attrName.equals(AceApi.ATTR_MEMBER_OF)) {
            	memberOf = addAttr;
            } else if (attrName.equals(AceApi.ATTR_PASSWORD)) {
                password = getValue(addAttr).trim();
            } else if(attrName.equals(AceApi.ATTR_START)) {
                try {
                    start = Long.parseLong(getValue(addAttr));
                } catch (NumberFormatException e) {
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                            add.getEventID(), "Error parsing Start value.");
                    return;
                }
            } else if(attrName.equals(AceApi.ATTR_END)) {
                try {
                    end = Long.parseLong(getValue(addAttr));
                } catch (NumberFormatException e) {
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                            add.getEventID(), "Error parsing Start value.");
                    return;
                }
            } else {
                trace.trace("unhandled attribute:  " + attrName, Trace.DEFAULT_TRACE|Trace.XML_TRACE);
            }
    	}
    	
    	if (defaultLogin == null) {
    		Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
    				add.getEventID(), "Unable to create account, missing DefaultLogin.");
    		return;
    	}

    	if (firstName == null) {
    		Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
    				add.getEventID(), "Unable to create account, missing FirstName.");
    		return;
    	}

        if (lastName == null) {
            Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                    add.getEventID(), "Unable to create account, missing LastName.");
            return;
        }
        
        if (defaultShell == null) {
            defaultShell = "";
        }

        try {
            if (tokenSerialNumber == null) {
                rsaData.addUser(lastName, firstName, emailAddress, defaultLogin, defaultShell, password);
            } else {
            	rsaData.assignToken(lastName, firstName, emailAddress, defaultLogin, defaultShell, tokenSerialNumber, password);
            }
        } catch (AceToolkitException e) {
            Util.addStatusAppError(trace, result, add.getEventID(), e, "Unable to create account.", defaultLogin);
            return;
        } catch (AceDriverException e) {
            Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL, add.getEventID());
            return;
        }
        
        if (end != 0) {
            try {
                setTempUser(start, end, "-" + defaultLogin);
            } catch (AceToolkitException e) {
                Util.addStatusAppError(trace, result, add.getEventID(), e, "Unable to set account as temporary.", defaultLogin);
                return;
            }
        }

        if (profileName != null) {
            try {
            	rsaData.assignProfile("-" + defaultLogin, profileName);
            } catch (AceToolkitException e) {
                Util.addStatusAppError(trace, result, add.getEventID(), e,
                        "Unable to assign profile.", defaultLogin);
                return;
            }
        }
        
        if (memberOf != null) {
            for (Iterator j=memberOf.extractValueElements().iterator(); j.hasNext(); ) {
                XDSValueElement val = (XDSValueElement) j.next();
                try {
                	rsaData.addLoginToGroup("", val.extractText().trim(), "", "-" + defaultLogin);
                } catch (AceToolkitException e) {
                    Util.addStatusAppError(trace, result, add.getEventID(), e,
                            "Error adding user to a group", defaultLogin);
                    return;
                }
            }
        }

        if (tokenPIN != null && tokenSerialNumber != null) {
            try {
            	rsaData.setPin(tokenPIN, tokenSerialNumber);
            } catch (AceToolkitException e) {
                Util.addStatusAppError(trace, result, add.getEventID(), e,
                        "Unable to set PIN.", defaultLogin);
                return;
            }
        }

        XDSAddAssociationElement addAssociation = result.appendAddAssociationElement();
        addAssociation.setDestDN(add.getSrcDN());
        addAssociation.setEventID(add.getEventID());
        addAssociation.appendText(defaultLogin);

        Util.addStatus(result, StatusLevel.SUCCESS, StatusType.DRIVER_GENERAL, add.getEventID(), null);
    }

    /*
    <nds dtdversion="1.1" ndsversion="8.6">
        <source>
            <product version="1.1a">DirXML</product>
            <contact>Novell, Inc.</contact>
        </source>
        <input>
            <modify class-name="User" event-id="0" src-dn="\NEW_DELL_TREE\NOVELL\test\a" src-entry-id="33071" timestamp="1040072695#2">
                <association state="associated">1</association>
                <modify-attr attr-name="Telephone Number">
                    <remove-value>
                        <value type="teleNumber">111-1111</value>
                    </remove-value>
                    <add-value>
                        <value timestamp="1040072695#2" type="teleNumber">222-2222</value>
                    </add-value>
                </modify-attr>
            </modify>
        </input>
    </nds>
  */

 //example modify result:

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
    private void modifyHandler(XDSModifyElement modify, XDSCommandResultDocument result)
    {
        trace.trace("modifyHandler", Trace.DEFAULT_TRACE);

        if (modify.getClassName().equals(AceDriverShim.CLASS_USER) == true) {
            modifyUser(modify, result);
        } else if (modify.getClassName().equals(AceDriverShim.CLASS_TOKEN) == true) {
            modifyToken(modify, result);
        } else {
            Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                    modify.getEventID(), "class '" + modify.getClassName() + "' not supported");
            return;
        }
    }

    private void modifyUser(XDSModifyElement modify, XDSCommandResultDocument result)
    {
        String defaultLogin = "-" + modify.extractAssociationText();

        List newDefaultLogin = null;
        List firstName = null;
        List lastName = null;
        List emailAddress = null;
        List defaultShell = null;
        List memberOf = null;
        List profileName = null;
        List tokenSerialNumber = null;
        List end = null;
        List start = null;
        List password = null;
        
        ListIterator m = modify.extractModifyAttrElements().listIterator();
        while (m.hasNext()) {
            XDSModifyAttrElement modifyAttr = (XDSModifyAttrElement)m.next();

            String attrName = modifyAttr.getAttrName();
            if (attrName.equals(AceApi.ATTR_FIRST_NAME)) {
                firstName = modifyAttr.childElements();
            } else if (attrName.equals(AceApi.ATTR_LAST_NAME)) {
                lastName = modifyAttr.childElements();
            } else if (attrName.equals(AceApi.ATTR_EMAIL_ADDRESS)) {
            	emailAddress = modifyAttr.childElements();
            } else if (attrName.equals(AceApi.ATTR_DEFAULT_SHELL)) {
                defaultShell = modifyAttr.childElements();
            } else if (attrName.equals(AceApi.ATTR_MEMBER_OF)) {
                memberOf = modifyAttr.childElements();
            } else if (attrName.equals(AceApi.ATTR_PROFILE_NAME)) {
                profileName = modifyAttr.childElements();
            } else if (attrName.equals(AceApi.ATTR_TOKEN_SERIAL_NUMBER)) {
                tokenSerialNumber = modifyAttr.childElements();
            } else if (attrName.equals(AceApi.ATTR_END)) {
                end = modifyAttr.childElements();
            } else if (attrName.equals(AceApi.ATTR_START)) {
                start = modifyAttr.childElements();
            } else if (attrName.equals(AceApi.ATTR_PASSWORD)) {
                password = modifyAttr.childElements();
            } else if (attrName.equals(AceApi.ATTR_DEFAULT_LOGIN)) {
//                Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL, modify.getEventID(), "'" + AceApi.ATTR_DEFAULT_LOGIN + "' is the naming attribute for user and may not be modified. The object must be renamed to change '" + AceApi.ATTR_DEFAULT_LOGIN + "'");
            	newDefaultLogin = modifyAttr.childElements();
            } else {
                Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL, modify.getEventID(), "Unhandled attribute '" + attrName + "'");
                return;
            }
        }

        if (start != null || end != null) {
            try {
                handleModifyStartAndEnd(defaultLogin, start, end);
            } catch (AceDriverException e) {
                Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                        modify.getEventID(), e.getMessage());
                return;
            }
        }

        if (memberOf != null) {
            try {
                handleModifyMemberOf(modify.extractAssociationText(), memberOf);
            } catch (AceDriverException e) {
                Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                        modify.getEventID(), e.getMessage());
                return;
            }
        }

        if (profileName != null) {
            String newProfileName = computeNewValue(profileName);
            try {
                if (newProfileName.length() > 0) {
                	rsaData.assignProfile(defaultLogin, newProfileName);
                } else {
                	rsaData.unassignProfile(defaultLogin);
                }
            } catch (AceToolkitException e) {
                Util.addStatusAppError(trace, result, modify.getEventID(), e,
                        "Error updating profile name",
                        modify.extractAssociationText());
                return;
            }
        }

        if (tokenSerialNumber != null) {
            try {
                handleModifyTokenSerialNumber(modify.extractAssociationText(), tokenSerialNumber);
            } catch (AceDriverException e) {
                Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                        modify.getEventID(), e.getMessage());
                return;
            }
        }

        if (firstName != null || lastName != null || defaultShell != null || password != null || newDefaultLogin != null) {
            Map userInfo;
            try {
                userInfo = rsaData.listUserInfo(defaultLogin);
            } catch (AceToolkitException e) {
                Util.addStatusAppError(trace, result, modify.getEventID(), e,
                        "Error retrieving user information",
                        modify.extractAssociationText());
                return;
            }
            
            String newFirstName = computeNewValue((String)userInfo.get(AceApi.ATTR_FIRST_NAME), firstName);
            String newLastName = computeNewValue((String)userInfo.get(AceApi.ATTR_LAST_NAME), lastName);
            String newEmailAddress = computeNewValue((String)userInfo.get(AceApi.ATTR_EMAIL_ADDRESS), emailAddress);
            String newDefaultShell = computeNewValue((String)userInfo.get(AceApi.ATTR_DEFAULT_SHELL), defaultShell);
            String newPassword = password == null ? null : computeNewValue("", password);
            String newDefaultLoginString = newDefaultLogin == null ? null : computeNewValue((String)userInfo.get(AceApi.ATTR_DEFAULT_LOGIN), newDefaultLogin);
            
            if (newDefaultLoginString != null && !newDefaultLoginString.equals(userInfo.get(AceApi.ATTR_DEFAULT_LOGIN))) {
            	XDSModifyAssociationElement modAssoc = result.appendModifyAssociationElement();
                modAssoc.appendAssociationElement((String)userInfo.get(AceApi.ATTR_DEFAULT_LOGIN));
                modAssoc.appendAssociationElement(newDefaultLoginString);
            }

            
            try {
            	String modificationDefaultLogin = newDefaultLoginString ==  null ? modify.extractAssociationText() : newDefaultLoginString;
            	rsaData.setUser(newLastName, newFirstName, newEmailAddress, modificationDefaultLogin, newDefaultShell, defaultLogin, newPassword);
            } catch (AceToolkitException e) {
                Util.addStatusAppError(trace, result, modify.getEventID(), e,
                        "Error updating user information",
                        modify.extractAssociationText());
                return;
            }
        }

        Util.addStatus(result, StatusLevel.SUCCESS, StatusType.DRIVER_GENERAL, modify.getEventID(), null);
    }

    private void modifyToken(XDSModifyElement modify, XDSCommandResultDocument result)
    {
        String serialNumber = modify.extractAssociationText();

        List pin = null;
        List disabled = null;
        List newPINMode = null;
        
        ListIterator m = modify.extractModifyAttrElements().listIterator();
        while (m.hasNext()) {
            XDSModifyAttrElement modifyAttr = (XDSModifyAttrElement)m.next();

            String attrName = modifyAttr.getAttrName();
            if (attrName.equals(AceApi.ATTR_PIN)) {
                pin = modifyAttr.childElements();
            } else if (attrName.equals(AceApi.ATTR_DISABLED)) {
                disabled = modifyAttr.childElements();
            } else if (attrName.equals(AceApi.ATTR_NEW_PIN_MODE)) {
                newPINMode = modifyAttr.childElements();
            } else {
                Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                        modify.getEventID(), "Unhandled attribute '" + attrName + "'");
                return;
            }
        }

        if (pin != null) {
            String newPIN = computeNewValue(pin);
            try {
                if (newPIN.length() > 0) {
                	rsaData.setPin(newPIN, serialNumber);
                }
            } catch (AceToolkitException e) {
                trace.trace(e.getMessage());
                Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                        modify.getEventID(), "Error setting PIN");
                return;
            }
        }
        
        if (disabled != null) {
            Map currentInfo;
            try {
                currentInfo = rsaData.listTokenInfo(serialNumber);
            } catch (AceToolkitException e) {
                trace.trace(e.getMessage());
                Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                        modify.getEventID(), "Error getting token info");
                return;
            }
            String curDisabled = (String)currentInfo.get(AceApi.ATTR_DISABLED);
            String newDisabled = computeNewValue(curDisabled, disabled);

            if (newDisabled == null && curDisabled.equalsIgnoreCase("TRUE") || newDisabled != null && curDisabled.equalsIgnoreCase(newDisabled) == false) {
                try {
                    if (newDisabled != null && newDisabled.equalsIgnoreCase("TRUE")) {
                    	rsaData.disableToken(serialNumber);
                    } else {
                    	rsaData.enableToken(serialNumber);
                    }
                } catch (AceToolkitException e) {
                    trace.trace(e.getMessage());
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                            modify.getEventID(), "Error enabling/disabling token");
                    return;
                }
            }
        }

        if (newPINMode != null) {
            String newNewPINMode = computeNewValue(newPINMode);
            try {
                if (newNewPINMode.equalsIgnoreCase("TRUE")) {
                    rsaData.newPin(serialNumber);
                } else {
                    Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                            modify.getEventID(), "The only valid value for " + AceApi.ATTR_NEW_PIN_MODE + " is 'TRUE'");
                    return;
                }
            } catch (AceToolkitException e) {
                trace.trace(e.getMessage());
                Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                        modify.getEventID(), "Error setting PIN");
                return;
            }
        }
        
        Util.addStatus(result, StatusLevel.SUCCESS, StatusType.DRIVER_GENERAL, modify.getEventID(), null);
    }

    private String computeNewValue(List modifyAttr) {
        return computeNewValue("", modifyAttr);
    }
    
    private String computeNewValue(String currentValue, List modifyAttr) {
        String newValue = currentValue;
        if (newValue == null) {
            newValue = "";
        }

        if (modifyAttr == null) {
            return newValue;
        }

        for (Iterator i=modifyAttr.iterator(); i.hasNext(); ) {
            XDSElement mod = (XDSElement)i.next();
            if (mod.getClass() == XDSAddValueElement.class) {
                newValue = getValue((XDSAddValueElement)mod);
            } else if (mod.getClass() == XDSRemoveValueElement.class) {
                String removeValue = getValue((XDSRemoveValueElement)mod);
                if (newValue.equals(removeValue)) {
                    newValue = "";
                }
            } else if (mod.getClass() == XDSRemoveAllValuesElement.class) {
                newValue = "";
            } else {
                trace.trace("unhandled element:  " + mod.tagName(), Trace.DEFAULT_TRACE | Trace.XML_TRACE);
            }
        }
        return newValue;
    }

    private void handleModifyTokenSerialNumber(String defaultLogin, List tokenSerialNumberModifyAttr) throws AceDriverException {
        String tokenSerialOrLogin = "-" + defaultLogin;

        if (tokenSerialNumberModifyAttr.size() > 2) {
            throw new AceDriverException("Operation doesn't make sense");
        }

        if (tokenSerialNumberModifyAttr.size() == 2) {
            Object operation1 = tokenSerialNumberModifyAttr.get(0);
            Object operation2 = tokenSerialNumberModifyAttr.get(1);
            
            if (operation1.getClass() == XDSRemoveValueElement.class || operation1.getClass() == XDSRemoveAllValuesElement.class && operation2.getClass() == XDSAddValueElement.class) {
                String currentSerialNumber;
                if (operation1.getClass() == XDSRemoveValueElement.class) {
                    XDSRemoveValueElement removeValue = (XDSRemoveValueElement)operation1;
                    List values = removeValue.extractValueElements();
                    if (values.size() != 1) {
                        throw new AceDriverException("Cannot replace more than one token at a time");
                    }
                    
                    currentSerialNumber = ((XDSValueElement)values.get(0)).extractText().trim();
                    
                    try {
                        if (rsaData.isSerialAssignedToLogin(currentSerialNumber, defaultLogin) == false) {
                            throw new AceDriverException("Token not currently assigned");
                        }
                    } catch (AceToolkitException e) {
                        trace.trace(e.getMessage());
                        throw new AceDriverException("Error retrieving serial numbers", e);
                    }
                } else {
                    try {
                        List<String> serialNumbers = rsaData.getSerialByLogin(defaultLogin);
                        if (serialNumbers.size() > 1) {
                            throw new AceDriverException("Must specify token to replace because the user has more than one token assigned");
                        } else if (serialNumbers.size() == 0) {
                            throw new AceDriverException("No tokens currently assigned");
                        }
                        currentSerialNumber = serialNumbers.get(0);
                    } catch (AceToolkitException e) {
                        trace.trace(e.getMessage());
                        throw new AceDriverException("Error retrieving serial numbers", e);
                    }
                    
                }
                
                XDSAddValueElement addValue = (XDSAddValueElement)operation2;
                List values = addValue.extractValueElements();
                
                if (values.size() != 1) {
                    throw new AceDriverException("Cannot replace more than one token at a time");
                }
                
                String newSerialNumber = ((XDSValueElement)values.get(0)).extractText().trim();
                
                try {
                	rsaData.replaceToken(currentSerialNumber, newSerialNumber, false);
                } catch (AceToolkitException e) {
                    trace.trace(e.getMessage());
                    throw new AceDriverException("Error replacing token", e);
                }
                
                return;
            }
        }
        
        for (Iterator i=tokenSerialNumberModifyAttr.iterator(); i.hasNext(); ) {
            XDSElement mod = (XDSElement)i.next();
            if (mod.getClass() == XDSAddValueElement.class) {
                XDSAddValueElement addValue = (XDSAddValueElement)mod;
                for (Iterator j=addValue.extractValueElements().iterator(); j.hasNext(); ) {
                    XDSValueElement val = (XDSValueElement) j.next();
                    try {
                    	rsaData.assignAnotherToken(tokenSerialOrLogin, val.extractText().trim());
                    } catch (AceToolkitException e) {
                        trace.trace(e.getMessage());
                        throw new AceDriverException("Error assigning token to user", e);
                    }
                }
            } else if (mod.getClass() == XDSRemoveValueElement.class) {
                XDSRemoveValueElement removeValue = (XDSRemoveValueElement)mod;
                for (Iterator j=removeValue.extractValueElements().iterator(); j.hasNext(); ) {
                    XDSValueElement val = (XDSValueElement) j.next();
                    try {
                    	rsaData.rescindToken(val.extractText().trim(), false);
                    } catch (AceToolkitException e) {
                        trace.trace(e.getMessage());
                        throw new AceDriverException("Error unassigning token from user", e);
                    }
                }
            } else if (mod.getClass() == XDSRemoveAllValuesElement.class) {
                try {
                    List<String> assignedSerialNumbers = rsaData.getSerialByLogin(defaultLogin);
                    for (String assignedSerialNumber : assignedSerialNumbers) {
                    	rsaData.rescindToken(assignedSerialNumber, false);
                    }
                } catch (AceToolkitException e) {
                    trace.trace(e.getMessage());
                    throw new AceDriverException("Error unassigning all tokens from user", e);
                }
            } else {
                trace.trace("unhandled element:  " + mod.tagName(), Trace.DEFAULT_TRACE | Trace.XML_TRACE);
            }
        }
    }
    
//    private static final char[] serialNumberPadding = new char[] {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'};
    
//    private String padSerialNumber(String currentSerialNumber) {
//        final int SERIAL_NUMBER_LENGTH = 12;
//        if (currentSerialNumber.length() == SERIAL_NUMBER_LENGTH) {
//            return currentSerialNumber;
//        }
//        
//        StringBuffer newSerialNumber = new StringBuffer(SERIAL_NUMBER_LENGTH);
//        newSerialNumber.append(serialNumberPadding, 0, SERIAL_NUMBER_LENGTH - currentSerialNumber.length());
//        newSerialNumber.append(currentSerialNumber);
//        return newSerialNumber.toString();
//    }

    private void handleModifyMemberOf(String defaultLogin, List memberOfModifyAttr) throws AceDriverException {
        String tokenSerialOrLogin = "-" + defaultLogin;
        for (Iterator i=memberOfModifyAttr.iterator(); i.hasNext(); ) {
            XDSElement mod = (XDSElement)i.next();
            if (mod.getClass() == XDSAddValueElement.class) {
                XDSAddValueElement addValue = (XDSAddValueElement)mod;
                for (Iterator j=addValue.extractValueElements().iterator(); j.hasNext(); ) {
                    XDSValueElement val = (XDSValueElement) j.next();
                    try {
                    	rsaData.addLoginToGroup("", val.extractText().trim(), "", tokenSerialOrLogin);
                    } catch (AceToolkitException e) {
                        trace.trace(e.getMessage());
                        throw new AceDriverException("Error adding user to a group", e);
                    }
                }
            } else if (mod.getClass() == XDSRemoveValueElement.class) {
                XDSRemoveValueElement removeValue = (XDSRemoveValueElement)mod;
                for (Iterator j=removeValue.extractValueElements().iterator(); j.hasNext(); ) {
                    XDSValueElement val = (XDSValueElement) j.next();
                    try {
                    	rsaData.delLoginFromGroup(defaultLogin, val.extractText().trim());
                    } catch (AceToolkitException e) {
                        trace.trace(e.getMessage());
                        throw new AceDriverException("Error removing user from a group", e);
                    }
                }
            } else if (mod.getClass() == XDSRemoveAllValuesElement.class) {
                try {
                	Collection memberships = rsaData.listGroupMembership(tokenSerialOrLogin);
                	for (Iterator j=memberships.iterator(); j.hasNext(); ) {
                        String groupName = (String)j.next();
                        rsaData.delLoginFromGroup(defaultLogin, groupName);
                	}
                } catch (AceToolkitException e) {
                    trace.trace(e.getMessage());
                    throw new AceDriverException("Error removing user from all groups", e);
                }
            } else {
                trace.trace("unhandled element:  " + mod.tagName(), Trace.DEFAULT_TRACE | Trace.XML_TRACE);
            }
        }
    }

    private void handleModifyStartAndEnd(String defaultLogin, List startModifyAttr, List endModifyAttr) throws AceDriverException {
        /*
         * Changes to start and end need to be correlated. A start date cannot
         * be set or removed without setting or removing the end date.
         * 
         * Also, since start and end are single valued there are really only
         * three possible operations: set new start and end values, replace
         * existing start and end values, or remove start and end values.
         */
        // Since Start and End are single valued, all we should need to care
        // about is the last operation
        XDSElement end = null;
        if (endModifyAttr != null && endModifyAttr.size() > 0) {
            end = (XDSElement)endModifyAttr.get(endModifyAttr.size()-1);
        }

        XDSElement start = null;
        if (startModifyAttr != null && startModifyAttr.size() > 0) {
            start = (XDSElement)startModifyAttr.get(startModifyAttr.size()-1);
        }

        long endValue = 0;
        long startValue = 0;

        boolean currentTempUser = false;
        long currentStart = 0;
        long currentEnd = 0;
        if (end == null || end.getClass() == XDSAddValueElement.class && start == null) {
            Map userInfo;
            try {
                userInfo = rsaData.listUserInfo(defaultLogin);
            } catch (AceToolkitException e) {
                throw new AceDriverException("Error retrieving current user information");
            }
            if(userInfo.get(AceApi.ATTR_START) != null) currentStart = ((Long)userInfo.get(AceApi.ATTR_START)).longValue();
            if(userInfo.get(AceApi.ATTR_END) != null) currentEnd = ((Long)userInfo.get(AceApi.ATTR_END)).longValue();
            currentTempUser = userInfo.get(AceApi.ATTR_TEMP_USER).equals("TRUE");
        }

        if (start != null) {
            if (start.getClass() == XDSAddValueElement.class) {
                // Retrieve the current End values if new values were not specified
                if (end == null) {
                    if (currentTempUser == false) {
                        throw new AceDriverException("Cannot set Start date without End date.");
                    }
                    endValue = currentEnd;
                }

                try {
                    startValue = Long.parseLong(getValue((XDSAddValueElement)start));
                } catch (NumberFormatException e) {
                    throw new AceDriverException("Error parsing Start value.", e);
                }
            } else {
                if (end == null) {
                    if (currentTempUser) {
                        endValue = currentEnd;
                    } else {
                        return;
                    }
                }

                startValue = 0;
            }
        }

        if (end != null) {
            if (end.getClass() == XDSAddValueElement.class) {
                try {
                    endValue = Long.parseLong(getValue((XDSAddValueElement)end));
                } catch (NumberFormatException e) {
                    throw new AceDriverException("Error parsing End value.", e);
                }

                // Retrieve the current Start values if new values were not specified
                if (start == null) {
                    if (currentTempUser == false) {
                        startValue = 0;
                    } else {
                        if (currentStart < endValue) {
                            startValue = currentStart;
                        } else {
                            startValue = endValue - 24 * 60 * 60;
                        }
                    }
                }
            } else {
                startValue = 0;
                endValue = 0;
            }
        }
        
        try {
            setTempUser(startValue, endValue, defaultLogin);
        } catch (AceToolkitException e) {
            throw new AceDriverException("Error setting new Start and End dates. " + e.getMessage(), e);
        }
    }

    private void setTempUser(long start, long end, String tokenSerialOrLogin) throws AceToolkitException {
        String dateStart;
        int hourStart;
        if (start == 0) {
            if (end != 0 && end < new Date().getTime()/1000) {
                Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                c.set(1986, Calendar.JANUARY, 1, 0, 0, 0);
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                dateStart = formatter.format(c.getTime());
                c.setTimeZone(formatter.getTimeZone());
                hourStart = c.get(Calendar.HOUR_OF_DAY);
            } else {
                dateStart = "";
                hourStart = 0;
            }
        } else {
            dateStart = TimeUtil.localDateFromCtime(start);
            hourStart = TimeUtil.localHoursFromCtime(start);
        }

        String dateEnd;
        int hourEnd;
        if (end == 0) {
            dateEnd = "";
            hourEnd = 0;
        } else {
            dateEnd = TimeUtil.localDateFromCtime(end);
            hourEnd = TimeUtil.localHoursFromCtime(end);
        }
        
        if (dateEnd != null) {
        	rsaData.setTempUser(dateStart, hourStart, dateEnd, hourEnd, tokenSerialOrLogin);
        }
    }

    private void deleteHandler(XDSDeleteElement del, XDSCommandResultDocument result)
    {
        if (del.getClassName().equals(AceDriverShim.CLASS_USER) == false) {
            Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                    del.getEventID(), "delete operation not supported for class '" + del.getClassName() + "'.");
            return;
        }

        String defaultLogin = del.extractAssociationText();
        
        try {
        	rsaData.deleteUserByDefaultLogin(defaultLogin);
        } catch (AceToolkitException e) {
            Util.addStatusAppError(trace, result, del.getEventID(), e,
                    "Unable to delete account.", defaultLogin);
            return;
        } catch (AceDriverException e) {
            Util.addStatus(result, StatusLevel.ERROR, StatusType.DRIVER_GENERAL,
                    del.getEventID(), e.getMessage());
            return;
		}

        Util.addStatus(result, StatusLevel.SUCCESS, StatusType.DRIVER_GENERAL, del.getEventID(), null);
    }
    
	private Map buildParamMap() {
		Map params = new HashMap();
        /*
		Parameter createScriptParam = new Parameter(TAG_CREATE_SCRIPT, null, DataType.STRING);
		createScriptParam.add(RequiredConstraint.REQUIRED);
		params.put(createScriptParam.tagName(), createScriptParam);
        */

		return params;
	}

    private String getValue(XDSAddAttrElement addAttr) {
        // The document has already been validated and the DTD requires at
        // least one value, so we don't need to check here.
        return ((XDSValueElement)addAttr.extractValueElements().get(0)).extractText().trim();
    }

    private String getValue(XDSAddValueElement addAttr) {
        // The document has already been validated and the DTD requires at
        // least one value, so we don't need to check here.
        if (((XDSValueElement)addAttr.extractValueElements().get(0)).extractText() != null) {
            return ((XDSValueElement)addAttr.extractValueElements().get(0)).extractText().trim();
        } else {
            return null;
        }
    }

    private String getValue(XDSRemoveValueElement removeAttr) {
        // The document has already been validated and the DTD requires at
        // least one value, so we don't need to check here.
        return ((XDSValueElement)removeAttr.extractValueElements().get(0)).extractText().trim();
    }
    public void initFilter() throws AceDriverException, AceToolkitException {
    	Map<String, String[]> filter = new HashMap<String, String[]>();
    	String[] userFilter = new String[] {"DefaultLogin", "DefaultShell", "FirstName", "LastName", "EmailAddress", "TokenSerialNumber", "TokenPIN", "ProfileName", "MemberOf", "TempUser", "Start", "End", "CreatePIN", "MustCreatePIN"};
        String[] tokenFilter = new String[] {"TokenSerialNumber", "TokenPIN", "Disabled"};
        filter.put("User", userFilter);
        filter.put("Token", tokenFilter);
        rsaData.setFilter(filter);
    }
}
