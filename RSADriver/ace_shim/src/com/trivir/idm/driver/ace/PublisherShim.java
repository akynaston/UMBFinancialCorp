package com.trivir.idm.driver.ace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.novell.nds.dirxml.driver.PublicationShim;
import com.novell.nds.dirxml.driver.Trace;
import com.novell.nds.dirxml.driver.XmlCommandProcessor;
import com.novell.nds.dirxml.driver.XmlDocument;
import com.novell.nds.dirxml.driver.xds.DataType;
import com.novell.nds.dirxml.driver.xds.Parameter;
import com.novell.nds.dirxml.driver.xds.RangeConstraint;
import com.novell.nds.dirxml.driver.xds.StatusLevel;
import com.novell.nds.dirxml.driver.xds.StatusType;
import com.novell.nds.dirxml.driver.xds.ValueType;
import com.novell.nds.dirxml.driver.xds.XDSAddAttrElement;
import com.novell.nds.dirxml.driver.xds.XDSAddElement;
import com.novell.nds.dirxml.driver.xds.XDSAllowAttrElement;
import com.novell.nds.dirxml.driver.xds.XDSAllowClassElement;
import com.novell.nds.dirxml.driver.xds.XDSCommandDocument;
import com.novell.nds.dirxml.driver.xds.XDSDeleteElement;
import com.novell.nds.dirxml.driver.xds.XDSDriverFilterElement;
import com.novell.nds.dirxml.driver.xds.XDSHeartbeatDocument;
import com.novell.nds.dirxml.driver.xds.XDSInitDocument;
import com.novell.nds.dirxml.driver.xds.XDSModifyAssociationElement;
import com.novell.nds.dirxml.driver.xds.XDSModifyAttrElement;
import com.novell.nds.dirxml.driver.xds.XDSModifyElement;
import com.novell.nds.dirxml.driver.xds.XDSStatusElement;
import com.novell.nds.dirxml.driver.xds.XDSValueElement;
import com.novell.nds.dirxml.driver.xds.util.StatusAttributes;
import com.novell.nds.dirxml.driver.xds.util.XDSUtil;
import com.trivir.ace.AceToolkitException;
import com.trivir.ace.api.AceApi;
import com.trivir.ace.api.AceEventException;

@SuppressWarnings("unchecked")
public class PublisherShim implements PublicationShim, DataModel.ChangeListener {
    private static final String TAG_PUBLISHER_DISABLE = "disable";
    private static final String TAG_HEARTBEAT_INTERVAL = "pub-heartbeat-interval";
    private static final String TAG_POLL_RATE_INTERVAL = "pollRate";
    
    private static final String DEFAULT_PUBLISHER_DISABLED = "_"; // _ = not disabled.
    private static final String DEFAULT_HEARTBEAT_INTERVAL = "0"; //minutes (disabled)
    private static final String DEFAULT_POLL_RATE = "5"; //minutes

    private Trace trace;
	private AceDriverShim driver;

    private long heartbeatInterval = 0;
    private long pollRateInterval = 0;
    private boolean publisherDisabled;

	private boolean shutdown = false;
    private Object semaphore = new Object();
    private DataModel rsaData;

    private XmlCommandProcessor commandProcessor;
    private QueryHandler queryHandler;
    

	PublisherShim(AceDriverShim driver) {
        publisherDisabled = false;
		this.driver = driver;
        trace = new Trace(driver.getDriverRDN() + "\\Publisher");
        rsaData = driver.rsaData;
        queryHandler = new QueryHandler(driver, rsaData);
	}
	
	public XmlDocument init(XmlDocument initXML) {
        trace.trace("init", 1);

        try {
            XDSInitDocument init = new XDSInitDocument(initXML);
            Map params = buildParamMap();
            init.parameters(params);

            Parameter paramDisabled = (Parameter) params.get(TAG_PUBLISHER_DISABLE);
            publisherDisabled = ("1".equalsIgnoreCase(paramDisabled.toString()));
            if (publisherDisabled == false) {
                Parameter param = (Parameter) params.get(TAG_HEARTBEAT_INTERVAL);
                heartbeatInterval = param.toInteger().intValue() * 60 * 1000;
                
                Parameter paramPoll = (Parameter) params.get(TAG_POLL_RATE_INTERVAL);
                pollRateInterval = paramPoll.toInteger().intValue() * 60 * 1000;
                if (pollRateInterval == 0) {
                    pollRateInterval = 5 * 1000;
                }
            }

            initializeFilter(init.extractInitParamsElement().extractDriverFilterElement());

            return driver.createSuccessDocument(params);
        } catch (Exception e) {
        	return driver.createStatusDocument(e, initXML);
        }
	}

    private Map filter = new HashMap();

    private void initializeFilter(XDSDriverFilterElement filterElement)
    {
        if (filterElement == null) {
            return;
        }

        List classes = filterElement.childElements();
        for (Iterator i=classes.iterator(); i.hasNext(); ) {
            
            XDSAllowClassElement allowClass = (XDSAllowClassElement)i.next();
            List attrs = new ArrayList();
            List allowAttrs = allowClass.extractAllowAttrElements();
            for (Iterator a=allowAttrs.iterator(); a.hasNext(); ) {
                XDSAllowAttrElement attr = (XDSAllowAttrElement)a.next();
                attrs.add(attr.getAttrName());
            }
            filter.put(allowClass.getClassName(), attrs.toArray(new String[attrs.size()]));
        }
    }

    //example result document:
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
	public XmlDocument start(XmlCommandProcessor processor) {
        trace.trace("start", Trace.DEFAULT_TRACE);

        this.commandProcessor = processor;
        
        XDSHeartbeatDocument heartbeatInit = new XDSHeartbeatDocument();
        driver.appendSourceInfo(heartbeatInit);
        XmlDocument heartbeat = heartbeatInit.toXML();


        try {
            if (publisherDisabled) {
                while (!shutdown) {
                    synchronized (semaphore) {
                        trace.trace("Publisher disabled sleeping forever", Trace.DEFAULT_TRACE);
                        semaphore.wait();
                    }
                 }
                return driver.createSuccessDocument();
            }

            rsaData.setFilter(filter);

            long timeSinceLastHeartbeat = 0;
            long timeSinceLastPoll = 0;

            while (!shutdown) {
                long sleep = (heartbeatInterval == 0) ? pollRateInterval - timeSinceLastPoll :
                    Math.min(heartbeatInterval - timeSinceLastHeartbeat,
                        pollRateInterval - timeSinceLastPoll);

                sleep = Math.min(sleep, 2000);

                trace.trace("Going back to sleep for " + sleep + " milliseconds", 4);
                
                synchronized (semaphore) {
                    semaphore.wait(sleep);
                }
                
                if (shutdown) {
                    break;
                }

                timeSinceLastPoll += sleep;
                if (timeSinceLastPoll >= pollRateInterval) {
                    timeSinceLastPoll = 0;
                	try {
                        boolean generatedEvent = rsaData.generateEvents(this);
                        if (generatedEvent) {
                            timeSinceLastHeartbeat = 0;
                            continue;
                        }
                	} catch (AceEventException e) {
                		XDSCommandDocument result = new XDSCommandDocument();
                        driver.appendSourceInfo(result);
                        StatusAttributes attrs = StatusAttributes.factory(StatusLevel.ERROR, StatusType.APP_GENERAL, null);
                        XDSStatusElement status = XDSUtil.appendStatus(result,  attrs, "An error occured while generating events", e, true, null);
                        XDSUtil.appendXML(status.domElement(), "<rsa-event>" + e.getEvent() + "</rsa-event>");
                        commandProcessor.execute(result.toXML(), queryHandler);
                        timeSinceLastHeartbeat = 0;
                        continue;
                	}
                }

                if (heartbeatInterval > 0) {
                    timeSinceLastHeartbeat += sleep;
                    if (timeSinceLastHeartbeat >= heartbeatInterval) {
                        timeSinceLastHeartbeat = 0;
                        processor.execute(heartbeat, queryHandler);
                    }
                }
             }

            return driver.createSuccessDocument();
        } catch (Exception e) {
        	return driver.createStatusDocument(e);
        } finally {
        	// TODO: Fix this thing here.
            rsaData.close();
            trace.trace("stopping...", Trace.XML_TRACE);
        }
	}

	public void userAdded(String defaultLogin, Map attrs) {
        XDSCommandDocument command = new XDSCommandDocument();
        driver.appendSourceInfo(command);

        XDSAddElement add = command.appendAddElement();
        add.setClassName("User");
        add.setEventID("0");
        add.appendAssociationElement(defaultLogin);

        for (Iterator i=attrs.keySet().iterator(); i.hasNext(); ) {
            String attrName = (String)i.next();
            Object attrValue = attrs.get(attrName);
            if (attrValue instanceof List) {
                XDSAddAttrElement attr = add.appendAddAttrElement();
                attr.setAttrName(attrName);
                List values = (List)attrValue;
                for (Iterator j=values.iterator(); j.hasNext(); ) {
                    XDSValueElement ve = attr.appendValueElement(j.next().toString());
                    ve.setType(ValueType.STRING);
                }
            } else {
                XDSAddAttrElement attr = add.appendAddAttrElement();
                attr.setAttrName(attrName);
                XDSValueElement ve = attr.appendValueElement(attrValue.toString());
                if (attrName.equals(AceApi.ATTR_START) || attrName.equals(AceApi.ATTR_END)) {
                    ve.setType(ValueType.TIME);
                } else if (attrValue.getClass() == Long.class) {
                    ve.setType(ValueType.INT);
                } else if (attrName.equals(AceApi.ATTR_DISABLED)) {
//                    ve.setType(ValueType.STRING);
                } else {
                    ve.setType(ValueType.STRING);
                }
            }
        }

        commandProcessor.execute(command.toXML(), queryHandler);
    }

    public void userDeleted(String defaultLogin) {
        XDSCommandDocument command = new XDSCommandDocument();
        driver.appendSourceInfo(command);
        XDSDeleteElement del = command.appendDeleteElement();
        del.setClassName(AceDriverShim.CLASS_USER);
        del.appendAssociationElement(defaultLogin);
        commandProcessor.execute(command.toXML(), queryHandler);
    }

    public void userModified(String defaultLogin, Map curInfo, Map newInfo) {
        generateModify(AceDriverShim.CLASS_USER, defaultLogin, curInfo, newInfo);
    }

    public void tokenModified(String serialNumber, Map curInfo, Map newInfo) {
        generateModify(AceDriverShim.CLASS_TOKEN, serialNumber, curInfo, newInfo);
    }

    public void generateModify(String className, String association, Map curInfo, Map newInfo) {
        XDSCommandDocument command = new XDSCommandDocument();
        driver.appendSourceInfo(command);

        XDSModifyElement modify = null;

        Set allAttrNames;
        if (curInfo.keySet().containsAll(newInfo.keySet())) {
            allAttrNames = curInfo.keySet();
        } else {
            allAttrNames = new TreeSet(curInfo.keySet());
            allAttrNames.addAll(newInfo.keySet());
        }
        if (!curInfo.get(AceApi.ATTR_DEFAULT_LOGIN).equals(newInfo.get(AceApi.ATTR_DEFAULT_LOGIN))) {
            XDSModifyAssociationElement modAssoc = command.appendModifyAssociationElement();
            modAssoc.appendAssociationElement(curInfo.get(AceApi.ATTR_DEFAULT_LOGIN).toString());
            modAssoc.appendAssociationElement(newInfo.get(AceApi.ATTR_DEFAULT_LOGIN).toString());
            
            
        }
        for (Iterator j=allAttrNames.iterator(); j.hasNext(); ) {
            String attrName = (String)j.next();
            Object curValue = curInfo.get(attrName);
            Object newValue = newInfo.get(attrName);

            if (curValue != null && curValue instanceof List ||
                newValue != null && newValue instanceof List)
            {
                List curValues = curValue == null ? Collections.EMPTY_LIST : (List)curValue;
                List newValues = newValue == null ? Collections.EMPTY_LIST : (List)newValue;

                Set removedValues = new HashSet(curValues);
                removedValues.removeAll(newValues);

                Set addedValues = new HashSet(newValues);
                addedValues.removeAll(curValues);

                if (removedValues.size() != 0 || addedValues.size() !=0) {
                    if (modify == null) {
                        modify = command.appendModifyElement();
                        modify.setClassName(className);
                        modify.setEventID("0");
                        modify.appendAssociationElement(association);
                    }

                    XDSModifyAttrElement modAttr = modify.appendModifyAttrElement();
                    modAttr.setAttrName(attrName);

                    for (Iterator k = removedValues.iterator(); k.hasNext(); ) {
                        modAttr.appendRemoveValueElement().appendValueElement(k.next().toString());
                    }

                    for (Iterator k = addedValues.iterator(); k.hasNext(); ) {
                        modAttr.appendAddValueElement().appendValueElement(k.next().toString());
                    }
                }
            } else {
                if (curValue == null || curValue.equals(newValue) == false) {
                    if (modify == null) {
                        modify = command.appendModifyElement();
                        modify.setClassName(className);
                        modify.setEventID("0");
                        modify.appendAssociationElement(association);
                    }

                    XDSModifyAttrElement modAttr = modify.appendModifyAttrElement();
                    modAttr.setAttrName(attrName);
                    if (curValue != null) {
                        modAttr.appendRemoveValueElement().appendValueElement(curValue.toString());
                    }

                    if (newValue != null) {
                        modAttr.appendAddValueElement().appendValueElement(newValue.toString());
                    }
                }
            }
        }
        
        if (modify != null) {
            commandProcessor.execute(command.toXML(), queryHandler);
            command = new XDSCommandDocument();
            driver.appendSourceInfo(command);
        }
    }

    void shutdown() throws AceToolkitException {
        trace.trace("shutdown", Trace.DEFAULT_TRACE);

        shutdown = true;

        synchronized (semaphore) {
            semaphore.notifyAll();
        }
    }

    private Map buildParamMap() {
		Map params = new HashMap();
        Parameter paramPubDisable = new Parameter(TAG_PUBLISHER_DISABLE,
				DEFAULT_PUBLISHER_DISABLED, DataType.STRING);
        Parameter param = new Parameter(TAG_HEARTBEAT_INTERVAL,
        								DEFAULT_HEARTBEAT_INTERVAL, DataType.INT);
        Parameter paramPollRate = new Parameter(TAG_POLL_RATE_INTERVAL,
        								DEFAULT_POLL_RATE, DataType.INT);
		
        param.add(RangeConstraint.NON_NEGATIVE);
		paramPollRate.add(RangeConstraint.NON_NEGATIVE);
		
		params.put(param.tagName(), param);
		params.put(paramPollRate.tagName(), paramPollRate);
		params.put(paramPubDisable.tagName(), paramPubDisable);
		return params;
	}

    // This method is provided only for testing
    void setHeartBeatInterval(long interval) {
        heartbeatInterval = interval;
    }
    
    // This method is provided only for testing
    void setPollRate(long pollRateInterval) {
    	this.pollRateInterval = pollRateInterval;
    }
}
