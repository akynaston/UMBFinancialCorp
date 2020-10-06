package com.trivir.idm.driver.ace;

import com.novell.nds.dirxml.driver.DriverShim;
import com.novell.nds.dirxml.driver.XmlDocument;
import com.novell.nds.dirxml.driver.XmlCommandProcessor;
import com.novell.nds.dirxml.driver.XmlQueryProcessor;

class PublisherQueue implements Runnable, XmlCommandProcessor {
    private DriverShim shim;
    long timeout;
    private XmlDocument event = null;
    private XmlDocument response = null;
    
    PublisherQueue(DriverShim shim, long timeout) {
        this.shim = shim;
        this.timeout = timeout;
    }
    
    public void run() {
        XmlDocument response = shim.getPublicationShim().start(this);
        synchronized (this) {
            if (this.event != null) {
                System.out.println("Discarding event");
                return;
            }
            this.event = response;
            notify();
        }
    }
    
    public XmlDocument execute(XmlDocument event, XmlQueryProcessor queryProcessor) {
        synchronized (this) {
            if (this.event != null) {
                System.out.println("Discarding event");
                return null;
            }
            this.event = event;
            notify();
        }
        
        synchronized (shim) {
            if (response == null) {
                try {
                    shim.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }
    }
    
    public XmlDocument getEventDocument() throws InterruptedException {
        XmlDocument eventDocument;
        synchronized (this) {
            if (event == null) {
                wait(timeout);
            }
            eventDocument = event;
        }
        event = null;

        return eventDocument;
    }
    
    public void setResponseDocument(String response) {
        synchronized (shim) {
            this.response = new XmlDocument(response);
            shim.notify();
        }
    }

}
