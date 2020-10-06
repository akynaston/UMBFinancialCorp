package org.idmunit.connector;

import java.util.Collection;
import java.util.Map;

import org.idmunit.IdMUnitException;

import com.trivir.idmunit.connector.RsaConnector;

public class ACE extends RsaConnector {
    public void opDelObject(Map<String, Collection<String>> data) throws IdMUnitException {
        opDeleteObject(data);
    }

    public void opModObject(Map<String, Collection<String>> data) throws IdMUnitException {
        opReplaceAttr(data);
    }

    public void opModifyObject(Map<String, Collection<String>> data) throws IdMUnitException {
        opReplaceAttr(data);
    }
}
