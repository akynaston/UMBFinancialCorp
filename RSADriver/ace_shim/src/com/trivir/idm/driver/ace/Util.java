package com.trivir.idm.driver.ace;

import com.novell.nds.dirxml.driver.Trace;
import com.novell.nds.dirxml.driver.XmlDocument;
import com.novell.nds.dirxml.driver.xds.StatusDocument;
import com.novell.nds.dirxml.driver.xds.StatusLevel;
import com.novell.nds.dirxml.driver.xds.StatusType;
import com.novell.nds.dirxml.driver.xds.XDSStatusElement;
import com.novell.nds.dirxml.driver.xds.util.StatusAttributes;
import com.novell.nds.dirxml.driver.xds.util.XDSUtil;
import com.trivir.ace.AceToolkitException;

class Util {
	private Util() {}

    static XDSStatusElement addStatusAppError(Trace trace, StatusDocument result, String eventID, AceToolkitException e, String description, String defaultLogin) {
    	Throwable cause = e.getCause();
    	String causeMessage = "";
    	if(cause != null) causeMessage = " - " + cause.getMessage();
    	String fullMessage = e.getMessage() + causeMessage;
        trace.trace(fullMessage + " (" + e.getError() + ")", Trace.XML_TRACE);
        StatusAttributes attrs = StatusAttributes.factory(StatusLevel.ERROR, StatusType.APP_GENERAL, eventID);
        XDSStatusElement status = XDSUtil.appendStatus(result,  attrs, description + " " + fullMessage);
        XDSUtil.appendXML(status.domElement(), "<default-login>" + defaultLogin + "</default-login>");
        XDSUtil.appendXML(status.domElement(), "<error-code>" + e.getError() + "</error-code>");
        return status;
    }

	static XDSStatusElement addStatus(StatusDocument result, StatusLevel level, StatusType type, String eventID, String description) {
		StatusAttributes attrs = StatusAttributes.factory(level, type, eventID);
		return XDSUtil.appendStatus(result,  attrs, description);
	}

    static XDSStatusElement addStatus(StatusDocument result, StatusLevel level, StatusType type, String eventID) {
        return addStatus(result, level, type, eventID, null);
    }

	static XDSStatusElement addStatus(StatusDocument result, StatusLevel level, StatusType type, String eventID, String description, Exception e, boolean appendStackTrace, XmlDocument xmlToAppend) {
		StatusAttributes attrs = StatusAttributes.factory(level, type, eventID);
		return XDSUtil.appendStatus(result,  attrs, description, e, appendStackTrace, xmlToAppend);
	}

    static XDSStatusElement addStatus(StatusDocument result, StatusLevel level, StatusType type, String eventID, String description, Exception e, boolean appendStackTrace) {
        return addStatus(result, level, type, eventID, description, e, appendStackTrace, null);
    }
}
