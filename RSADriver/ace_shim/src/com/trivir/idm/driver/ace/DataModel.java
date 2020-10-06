package com.trivir.idm.driver.ace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.novell.nds.dirxml.driver.Trace;
import com.rsa.common.SystemException;
import com.trivir.ace.AceToolkitException;
import com.trivir.ace.api.AceApi;
import com.trivir.ace.api.AceApiEvent;
import com.trivir.ace.api.AceApiEventType;
import com.trivir.ace.api.AceEventException;
import com.trivir.ace.api.v71.AceApi71;

@SuppressWarnings("unchecked")
class DataModel {
	@SuppressWarnings("serial")
	private static List<String> ADD_ATTS = new ArrayList<String>() {{
		add(AceApi.ATTR_USER_NUM);
		add(AceApi.ATTR_LAST_NAME);
		add(AceApi.ATTR_FIRST_NAME);
		add(AceApi.ATTR_EMAIL_ADDRESS);
		add(AceApi.ATTR_DEFAULT_LOGIN);
		add(AceApi.ATTR_DEFAULT_SHELL);
		add(AceApi.ATTR_TOKEN_SERIAL_NUMBER);
	}};
	
    private final AceApi71 api;
    private final DriverObjectCache cache;
    private Collection userFilter = null;
    private Collection userExtensions;
    private Collection tokenFilter = null;
    @SuppressWarnings("unused")
	private Collection tokenExtensions;
    Map<String, String> allUsers = null;
    private List<String> addedUsers = null;
    private List<String> deletedUsers = null;
    private List<String> currentUsers = null;
    private List<String> unprocessedUsers = null;
    private List unprocessedTokens = null;
    private int batchSize = 1000;
	private boolean initialized = false;

    public static final String TERMINATOR = "Done";

	private Trace trace;

	private void log(String msg) {
		trace.trace(msg, Trace.XML_TRACE);
	}
	private void logDebug(String msg) {
		trace.trace(msg, 3);
	}
	
	private void logEvent(AceApiEvent event, String objectClass) {
		log("Handling '" + event.getType() + "-" + event.getInfo() + "' for " + objectClass + ".");
	}
	
    private void logUnhandledEvent(AceApiEvent event, String objectClass) {
    	log("Known event type '" + event.getType() + "-" + event.getInfo() + "' not handled for " + objectClass + ".\n" + event.getDescription());
	}

    private void logUnknownEvent(AceApiEvent event, String objectClass) {
    	log("Unknown event value '" + event.getInfo() + "' not handled for " + objectClass + ".\n" + event.getDescription());
	}

    DataModel(AceApi71 api, String cacheName, List userExtensions, List tokenExtensions) throws DriverObjectCacheException {
        this.api = api;
		trace = new Trace("RSADM");
		this.cache = DriverObjectCache.getInstance(cacheName, trace);
		if (userExtensions == null) {
	        this.userExtensions = Collections.EMPTY_LIST;
		} else {
	        this.userExtensions = userExtensions;
		}
		if (tokenExtensions == null) {
			this.tokenExtensions = Collections.EMPTY_LIST;
		} else {
			this.tokenExtensions = tokenExtensions;
		}
    }

	void setFilter(Map filter) throws AceToolkitException, AceDriverException {
		String[] userFilterArray = (String[])filter.get(AceDriverShim.CLASS_USER);
		if (userFilterArray == null) {
			userFilter = null;
		} else {
			userFilter = Arrays.asList(userFilterArray);
			if (cache.isNewDatabase()) {
				allUsers = listAllUsers();
	            unprocessedUsers = new ArrayList<String>(allUsers.keySet());
	            trace.trace("Initializing new cache. " + unprocessedUsers.size() + " existing users to add to cache.", Trace.DEFAULT_TRACE);
			} else {
				allUsers = listAllUsers();
				Set<String> cacheUsers;
				try {
					cacheUsers = cache.getAllGUIDs();
				} catch (DriverObjectCacheException e) {
					throw new AceDriverException("Error getting list of all GUIDS from cache.", e);
				}

				deletedUsers = new ArrayList<String>(cacheUsers);
				deletedUsers.removeAll(allUsers.keySet());
				if (deletedUsers.size() > 0) {
					trace.trace("" + deletedUsers.size() + " users deleted while the driver was not running.", Trace.DEFAULT_TRACE);
					for (String s : deletedUsers) {
						try {
							allUsers.put(s, cache.getCachedUserAttrsByGuid(s).get(AceApi.ATTR_DEFAULT_LOGIN).toString());
						}
						catch (DriverObjectCacheException e) {
							throw new AceDriverException("Error reading deleted user from cache.", e);
						}
						trace.trace(s + " | " + allUsers.get(s), Trace.DEFAULT_TRACE);
					}
				}
				addedUsers = new ArrayList<String>(allUsers.keySet());
				addedUsers.removeAll(cacheUsers);
				if (addedUsers.size() > 0) {
					trace.trace("" + addedUsers.size() + " users added while the driver was not running.", Trace.DEFAULT_TRACE);
					for (String s : addedUsers) {
						trace.trace(s + " | " + allUsers.get(s), Trace.DEFAULT_TRACE);
					}
				}
				currentUsers = new ArrayList<String>(cacheUsers);
				currentUsers.removeAll(addedUsers);
				currentUsers.removeAll(deletedUsers);
			}
		}

		String[] tokenFilterArray = (String[])filter.get(AceDriverShim.CLASS_TOKEN);
		if (tokenFilterArray == null) {
			tokenFilter = null;
		} else {
			tokenFilter = Arrays.asList(tokenFilterArray);
			if (cache.isNewDatabase()) {
	        	unprocessedTokens = listAllTokens();
	            trace.trace("Initializing new cache. " + unprocessedTokens.size() + " existing tokens to add to cache.", Trace.DEFAULT_TRACE);
			}
		}

        // clear the event history
        api.clearHistory();
    }
    
	boolean init() throws AceDriverException, AceToolkitException {
        int count = 0;
        
        if (unprocessedUsers != null && unprocessedUsers.size() > 0) {
            log("Caching user information, " + unprocessedUsers.size() + " users remaining");
            while (unprocessedUsers.size() > 0 && count < batchSize) {
                ++count;
                String defaultLogin = allUsers.get(unprocessedUsers.get(0));
                try {
                	cache.addCachedUser(getUserAttributes(defaultLogin));
                } catch (AceToolkitException e) {
                    if (e.getError() != AceToolkitException.API_ERROR_INVUSR) {
                    	throw new AceDriverException("Error getting user attributes", e);
                    }
                    log("User '" + defaultLogin + "' was deleted before the information could be cached.");
                } catch (DriverObjectCacheException e) {
                	throw new AceDriverException("Error adding user to the cache.", e);
				}
                unprocessedUsers.remove(0);
            }
            if (unprocessedUsers.size() == 0) {
                log("All users cached.");
            } else {
                log(unprocessedUsers.size() + " users remaining to be cached.");
            }
            if (count >= batchSize) {
                return false;
            }
        }

        if (unprocessedTokens != null && unprocessedTokens.size() > 0) {
            log("Caching token information, " + unprocessedTokens.size() + " tokens remaining");
            if (tokenFilter.size() == 0 && unprocessedTokens.size() > 0) {
            	log("No need to cache tokens because the filter doesn't contain any token attributes");
            	unprocessedTokens = Collections.EMPTY_LIST;
            } else {
                while (unprocessedTokens.size() > 0 && count < batchSize) {
                    ++count;
                    String serialNumber = (String)unprocessedTokens.get(0);
                    try {
                    	cache.addCachedToken(serialNumber, getTokenInfo(serialNumber, tokenFilter));
                    } catch (DriverObjectCacheException e) {
                    	throw new AceDriverException("Error adding token to cache.", e);
                    }
                    unprocessedTokens.remove(0);
                }
                if (unprocessedTokens.size() == 0) {
                    log("All tokens cached.");
                } else {
                    log(unprocessedTokens.size() + " tokens remaining to be cached.");
                }
                if (count >= batchSize) {
                    return false;
                }
            }
        }
        
        return true;
    }
	
	void close() {
		cache.close();
	}

    boolean generateEvents(ChangeListener listener) throws AceDriverException, AceToolkitException, AceEventException, DriverObjectCacheException  {
        if (initialized == false) {
            initialized = init();
            return false;
        }

    	int count = 0;
        try {
    		logDebug("Data Model: Processing Events.");
        	if (deletedUsers != null && deletedUsers.size() > 0) {
        		logDebug("Processing deleted users: " + deletedUsers.size());
	        	for (ListIterator<String> i=deletedUsers.listIterator(); i.hasNext() && count < batchSize; ++count) {
	        		String guid = i.next();
	        		String defaultLogin = allUsers.get(guid);
	        		i.remove();
	        		allUsers.remove(guid);

	        		cache.removeCachedUser(guid);
	        		listener.userDeleted(defaultLogin);
	        	}
	        }
        	
	        if (addedUsers != null && addedUsers.size() > 0) {
	        	logDebug("Processing added users: " + addedUsers.size());
	            for (ListIterator<String> i=addedUsers.listIterator(); i.hasNext() && count < batchSize; ++count) {
	                String guid = i.next();
	            	String defaultLogin = allUsers.get(guid);
	                i.remove();
	                
	                Map<String,Object> attrs;
	                try {
	                	attrs = getUserAttributes(defaultLogin);
	                } catch (AceToolkitException e) {
	                    if (e.getError() != AceToolkitException.API_ERROR_INVUSR) {
	                        throw e;
	                    }
	                    //TODO send delete event
	                    logDebug("Data Model: User '" + defaultLogin + "' was deleted before the information could be cached.");
	                    continue;
	                }
	                handleUserAdd(listener, guid, attrs);
	            }
	        }
	        
	        if (currentUsers != null && currentUsers.size() > 0) {
	        	logDebug("Processing current users: " + currentUsers.size());
	        	for (ListIterator<String> i=currentUsers.listIterator(); i.hasNext() && count < batchSize; ++count) {
	        		String guid = i.next();
	        		i.remove();
	        		//handleUserModify(listener, allUsers.get(guid), guid);
	        	}
	        }

	        Date startDate = new Date();
	        while (count < batchSize) {
	            AceApiEvent event = monitorHistory();

	            AceApiEventType eventType = event.getType();
	            String eventDescription = event.getDescription();
	            logDebug("Data Model: " + eventDescription);

	            if (eventType == AceApiEventType.TERMINATOR) {
	            	logDebug("Data Model: No more events");
	                break;
	            }

	            if (eventType == AceApiEventType.DOWNLOAD_LOG_RECORDS) {
	            	Date eventDate = event.getDate();
	            	if (eventDate.after(startDate)) {
	            		break;
	            	}
	            }

	            if (eventType == AceApiEventType.IGNORED) {
	            	logDebug("Data Model: Ignoring event: " + event.getInfo());
	                continue;
	            }

	            if (eventType == AceApiEventType.NOOP) {
	            	logDebug("Data Model: Received NOOP event.");
	            }

	            ++count;

	            if (userFilter != null) {
	                if (eventType == AceApiEventType.USER_ADD) {
	                    logEvent(event, AceDriverShim.CLASS_USER);
	                    String defaultLogin = event.getUser();

	                    Map<String,Object> attrs;
	                    try {
    	                	attrs = getUserAttributes(defaultLogin);
	                    } catch (AceToolkitException e) {
	                        if (e.getError() != AceToolkitException.API_ERROR_INVUSR) {
	                            throw e;
	                        }
	                        logDebug("Data Model: User not found. The event may have been generated for a user in another identity store.");
	                        continue;
	                    }

	                    handleUserAdd(listener, (String)attrs.get(AceApi.ATTR_USER_NUM), attrs);
	                    continue;
	                } else if (eventType == AceApiEventType.USER_MODIFY) {
	                	logEvent(event, AceDriverShim.CLASS_USER);

	                    handleUserModify(listener, event.getUser(), event.getUserGuid());
	                    continue;
	                } else if (eventType == AceApiEventType.USER_DELETE) {
	                	logEvent(event, AceDriverShim.CLASS_USER);
	                    String defaultLogin = event.getUser();
	                    String guid = null;
	                    for (Entry<String, String> s : allUsers.entrySet()) {
	            			if (s.getValue().equalsIgnoreCase(defaultLogin)) guid = s.getKey();
	            		}
	            		if (guid == null) {
	            			logDebug("Data Model: Unable to find a user in the cache with the defaultLogin of: " + defaultLogin);
	            		}
	                    cache.removeCachedUser(guid);
	                    listener.userDeleted(defaultLogin);
	                    continue;
	                } else if (eventType == AceApiEventType.TOKEN_ASSIGNMENT_MODIFY ||
	                		eventType == AceApiEventType.GROUP_MODIFY ||
	                		eventType == AceApiEventType.USER_RADIUS_MODIFY) {
	                	logEvent(event, AceDriverShim.CLASS_USER);

                    	handleUserModify(listener, event.getUser(), event.getUserGuid());
	                } else if (eventType == AceApiEventType.UNKNOWN) {
	                    logUnknownEvent(event, AceDriverShim.CLASS_USER);
	                } else {
	                    logUnhandledEvent(event, AceDriverShim.CLASS_USER);
	                }
	            }

	            if (tokenFilter != null) {
	            	if (eventType == AceApiEventType.TOKEN_ASSIGNMENT_MODIFY ||
	            		eventType == AceApiEventType.TOKEN_MODIFY) {
	                    logEvent(event, AceDriverShim.CLASS_TOKEN);
	            		String tokenSerial = event.getTokenSerial();

	                    Map newInfo = getTokenInfo(tokenSerial, tokenFilter);
	                    newInfo.keySet().retainAll(tokenFilter);

                        Map curInfo = cache.getCachedTokenAttrs(tokenSerial);
                        if (curInfo == null) {
                            curInfo = new HashMap();
                            listener.tokenModified(tokenSerial, curInfo, newInfo);
                            logDebug("Data Model: Token " + tokenSerial + " did not in the cache. Adding it to the cache.");
                            cache.addCachedToken(tokenSerial, newInfo);
                        } else {
                            curInfo.keySet().retainAll(tokenFilter);
                            if (curInfo.equals(newInfo) == false) {
                                listener.tokenModified(tokenSerial, curInfo, newInfo);
                                cache.updateCachedTokenAttrs(tokenSerial, newInfo);
                            }
                        }
	            	} else if (eventType == AceApiEventType.TOKEN_DELETE) {
	                    logEvent(event, AceDriverShim.CLASS_TOKEN);
	                    cache.removeCachedToken(event.getTokenSerial());
	                } else if (eventType == AceApiEventType.UNKNOWN) {
	                    logUnknownEvent(event, AceDriverShim.CLASS_TOKEN);
	                } else {
	                    logUnhandledEvent(event, AceDriverShim.CLASS_TOKEN);
	                }
	            }
	        }
        } catch (SystemException e) {
			trace.trace(String.format("A communications error occured while communicating with Authentication Manager. Message: %s", e.getMessage()), Trace.DEFAULT_TRACE);
			try{
				api.reinit();
			} catch (Exception eInner) {
				trace.trace(String.format("An error occured while reinitializing the RSA API. Message: %s", e.getMessage()), Trace.DEFAULT_TRACE);
			}
	        return count > 0;
        }

        return count > 0;
    }

    private void handleUserAdd(ChangeListener listener, String guid, Map<String,Object> attrs) throws DriverObjectCacheException, AceDriverException, AceToolkitException {
        /*
         * An add event was reported but we need to check to see if this is the result
         * of an add on the subscriber channel. If so, treat this as a modify rather
         * than an add.
         */
//        Map<String,Object> curAttrs = cache.getCachedUserAttrsByDefaultLogin(defaultLogin);
    	String defaultLogin = (String)attrs.get(AceApi.ATTR_DEFAULT_LOGIN);
    	Map<String,Object> curAttrs = cache.getCachedUserAttrsByGuid(guid);
        if(curAttrs == null) {
            cache.addCachedUser(attrs);
            attrs.keySet().retainAll(userFilter);
            listener.userAdded(defaultLogin, attrs);
        } else {
            log("User exists in the cache, treating add event as a modify");
            Map<String,Object> newAttrs;
            try {
                newAttrs = getUserAttributes(defaultLogin);
            } catch (AceToolkitException e) {
                if (e.getError() != AceToolkitException.API_ERROR_INVUSR) {
                    throw e;
                }
                log("User not found. The event may have been generated for a user in another identity store.");
                return;
            }

            handleUserModify(listener, defaultLogin, curAttrs, newAttrs);
            allUsers.put(guid, defaultLogin);
        }
    }

    private void handleUserModify(ChangeListener listener, String defaultLogin, Map<String,Object> curAttrs, Map<String,Object> newAttrs) throws DriverObjectCacheException {
    	if (newAttrs.equals(curAttrs) == false) {
            cache.updateCachedUserAtrrs(newAttrs);
            newAttrs.keySet().retainAll(userFilter);
            curAttrs.keySet().retainAll(userFilter);
            if (newAttrs.equals(curAttrs) == false) {
                listener.userModified(defaultLogin, curAttrs, newAttrs);
            }
    	}
    }

    private void handleUserModify(ChangeListener listener, String defaultLogin, String guid) throws AceDriverException, AceToolkitException, DriverObjectCacheException {
        Map<String,Object> newAttrs;
        try {
        	newAttrs = getUserAttributes(defaultLogin);
        } catch (AceToolkitException e) {
            if (e.getError() != AceToolkitException.API_ERROR_INVUSR) {
                throw e;
            }
            log("User not found. The event may have been generated for a user in another identity store.");
            return;
        }

        Map<String,Object> curAttrs = cache.getCachedUserAttrsByDefaultLogin(defaultLogin);
//        Map<String,Object> curAttrs = cache.getCachedUserAttrsByGuid(guid);
        if(curAttrs != null) {
        	handleUserModify(listener, defaultLogin, curAttrs, newAttrs);
        } else {
            log("User '" + defaultLogin + "' did not exist in cache. Treating as an add and adding to the cache.");
            handleUserAdd(listener, guid, newAttrs);
        }
    }

	Map getUserAttributes(String login) throws AceToolkitException, AceDriverException {
    	return getUserAttributes(login, null);
    }

    Map getUserAttributes(String login, Collection attrs) throws AceToolkitException, AceDriverException {
    	login = "-" + login;
        Map info = listUserInfo(login);

        if (attrs == null || attrs.contains(AceApi.ATTR_MEMBER_OF)) {
            Collection groupMembership = listGroupMembership(login);

            if (groupMembership.size() > 0) {
                info.put(AceApi.ATTR_MEMBER_OF, groupMembership);
            }
        }

        if (attrs == null || attrs.contains(AceApi.ATTR_TOKEN_SERIAL_NUMBER)) {
            List assignedTokens = getSerialByLogin((String)info.get(AceApi.ATTR_DEFAULT_LOGIN));
            if (assignedTokens.size() > 0) {
                info.put(AceApi.ATTR_TOKEN_SERIAL_NUMBER, assignedTokens);
            }
        }

        if (userExtensions.size() > 0) {
        	boolean extensionRequested = false;
        	if (attrs == null) {
        		extensionRequested = true;
        	} else {
            	for (Iterator i=userExtensions.iterator(); i.hasNext(); ) {
            		if (attrs.contains(i.next())) {
            			extensionRequested = true;
            			break;
            		}
            	}
        	}
        	if (extensionRequested) {
        		Map extensions = listExtensionsForUser(login);
        		for (Iterator i=extensions.keySet().iterator(); i.hasNext(); ) {
        			String extName = (String)i.next();
                    if (attrs != null && attrs.contains(extName) == false) {
                    	continue;
                    }
                    info.put(extName, extensions.get(extName));
        		}
        	}
        }

        if (attrs != null) {
            info.keySet().retainAll(attrs);
        }
        return info;
    }

	private Map getTokenInfo(String tokenSerialNumber, Collection attrs) throws AceToolkitException, AceDriverException {
        Map info = listTokenInfo(tokenSerialNumber);

        info.keySet().retainAll(attrs);

        return info;
    }

    void addLoginToGroup(String groupLogin, String groupName, String groupShell, String tokenSerialOrLogin) throws AceToolkitException {
    	synchronized (api) {
            api.addLoginToGroup(groupLogin, groupName, groupShell, tokenSerialOrLogin);
    	}
    }

	void addUser(String lastName, String firstName, String emailAddress, String defaultLogin, String defaultShell, String password) throws AceToolkitException, AceDriverException {
    	synchronized (api) {
            api.addUser(lastName, firstName, defaultLogin, emailAddress, defaultShell, password);
    	}

        if (userFilter != null) {
        	try {
				cache.addCachedUser(getUserAttributes(defaultLogin, ADD_ATTS));
			} catch (DriverObjectCacheException e) {
				throw new AceDriverException("Error adding user to cache.", e);
			}
        }
    }

    void assignAnotherToken(String tokenSerialOrLogin, String newTokenSerialNumber) throws AceToolkitException {
    	synchronized (api) {
        	api.assignAnotherToken(tokenSerialOrLogin, newTokenSerialNumber);
    	}
	}

    void assignProfile(String tokenSerialOrLogin, String profileName) throws AceToolkitException {
    	synchronized (api) {
        	api.assignProfile(tokenSerialOrLogin, profileName);
    	}
    }

    void assignToken(String lastName, String firstName, String emailAddress, String defaultLogin, String defaultShell, String tokenSerialNumber, String password) throws AceToolkitException, AceDriverException {
    	synchronized (api) {
            api.assignToken(lastName, firstName, defaultLogin, emailAddress, defaultShell, tokenSerialNumber, password);
    	}

        if (userFilter != null) {
        	try {
				cache.addCachedUser(getUserAttributes(defaultLogin, ADD_ATTS));
			} catch (DriverObjectCacheException e) {
				throw new AceDriverException("Error adding user to cache.", e);
			}
        }
    }

	void delLoginFromGroup(String defaultLogin, String groupName) throws AceToolkitException {
    	synchronized (api) {
    	    api.delLoginFromGroup(defaultLogin, groupName);
    	}
	}

	void deleteUser(String guid) throws AceToolkitException, AceDriverException {
    	synchronized (api) {
    		api.deleteUser("-" + allUsers.get(guid));
    	}

		if (userFilter != null) {
			try {
				cache.removeCachedUser(guid);
			} catch (DriverObjectCacheException e) {
				throw new AceDriverException("Error removing user from cache.", e);
			}
		}
	}
	
	void deleteUserByDefaultLogin(String defaultLogin) throws AceToolkitException, AceDriverException {
		String guid = null;
		for (Entry<String, String> s : allUsers.entrySet()) {
			if (s.getValue().equalsIgnoreCase(defaultLogin)) guid = s.getKey();
		}
		if (guid == null) {
			log("Unable to find a user in the cache with the defaultLogin of: " + defaultLogin);
		}
		deleteUser(guid);
	}

    void disableToken(String tokenSerialNumber) throws AceToolkitException {
    	synchronized (api) {
        	api.disableToken(tokenSerialNumber);
    	}
	}

    void enableToken(String tokenSerialNumber) throws AceToolkitException {
    	synchronized (api) {
        	api.enableToken(tokenSerialNumber);
    	}
	}

    boolean isSerialAssignedToLogin(String tokenSerialNumber, String defaultLogin) throws AceToolkitException {
    	synchronized (api) {
    	    return api.getSerialByLogin(defaultLogin, "0").contains(tokenSerialNumber);
    	}
    }

	List<String> getSerialByLogin(String defaultLogin) throws AceToolkitException {
		synchronized (api) {
			List<String> result = api.getSerialByLogin(defaultLogin, "0");
			Collections.sort(result);
			return result;
    	}
	}

	List<String> listAllTokens() throws AceToolkitException {
		synchronized (api) {
			return api.listAllTokens();
		}
	}
	
	Map<String, String> listAllUsers() throws AceToolkitException {
		synchronized (api) {
    		return api.listAllUsersByGUID();
		}
	}
	
	List<String> listAssignedTokens() throws AceToolkitException {
		synchronized (api) {
			return api.listAssignedTokens();
		}
	}
	
	private Map listExtensionsForUser(String tokenSerialOrLogin) throws AceToolkitException {
    	Map returnedExtensions = new HashMap();
    	synchronized (api) {
    		Map extensions = api.listExtensionsForUser(tokenSerialOrLogin);
    		Iterator iter = extensions.entrySet().iterator();
    		while(iter.hasNext()) {
    			Map.Entry extension = (Map.Entry) iter.next(); 
    			if(! userExtensions.contains(extension.getKey())) {
    				continue;
    			}
    			
    			returnedExtensions.put(extension.getKey(), extension.getValue());
    		}
    	}
        return returnedExtensions;
	}

	Collection listGroupMembership(String tokenSerialOrLogin) throws AceToolkitException {
		synchronized (api) {
			List<String> result = api.listGroupMembership(tokenSerialOrLogin);
			Collections.sort(result);
			return result;
		}
	}

    Map listTokenInfo(String tokenSerialNumber) throws AceToolkitException {
    	synchronized (api) {
    		return api.listTokenInfo(tokenSerialNumber);
    	}
    }

	List<String> listUnassignedTokens() throws AceToolkitException {
		synchronized (api) {
			return api.listUnassignedTokens();
		}
	}
	
	Map listUserInfo(String defaultLogin) throws AceToolkitException {
    	synchronized (api) {
    		return api.listUserInfo(defaultLogin);
    	}
    }
    
    public List<String> listUsersByLogin(String defaultLogin) throws AceToolkitException{
    	synchronized (api) {
    		List<String> result = api.listUsersByLogin(defaultLogin);
    		Collections.sort(result);
			return result;
		}
    }

    public void newPin(String tokenSerialNumber) throws AceToolkitException {
        synchronized (api) {
            api.newPin(tokenSerialNumber);
        }
    }

    void replaceToken(String oldTokenSerialNumber, String newTokenSerialNumber, boolean resetPin) throws AceToolkitException {
    	synchronized (api) {
    		api.replaceToken(oldTokenSerialNumber, newTokenSerialNumber, resetPin);
    	}
	}

    void rescindToken(String tokenSerialNumber, boolean revoke) throws AceToolkitException {
    	synchronized (api) {
    		api.rescindToken(tokenSerialNumber, revoke);
    	}
	}

    String setPin(String pin, String tokenSerialNumber) throws AceToolkitException {
    	synchronized (api) {
        	api.setPin(pin, tokenSerialNumber);
    		return "";
    	}
    }

	void setTempUser(String dateStart, int hourStart, String dateEnd, int hourEnd, String tokenSerialOrLogin) throws AceToolkitException {
    	synchronized (api) {
    		api.setTempUser(dateStart, hourStart, dateEnd, hourEnd, tokenSerialOrLogin);
    	}
	}

    void setUser(String lastName, String firstName, String emailAddress,String defaultLogin, String defaultShell, String tokenSerialOrLogin, String password) throws AceToolkitException {
    	synchronized (api) {
        	api.setUser(lastName, firstName, defaultLogin, emailAddress, defaultShell, tokenSerialOrLogin, password);
    	}
	}

    void unassignProfile(String tokenSerialOrLogin) throws AceToolkitException {
    	synchronized (api) {
        	api.unassignProfile(tokenSerialOrLogin);
    	}
    }

    interface ChangeListener {
        public void userAdded(String defaultLogin, Map attrs);
        public void userDeleted(String defaultLogin);
        public void userModified(String defaultLogin, Map oldAttrs, Map newAttrs);
        public void tokenModified(String serialNumber, Map oldAttrs, Map newAttrs);
    }

    private AceApiEvent monitorHistory() throws AceToolkitException, AceEventException {
    	synchronized (api) {
    		AceApiEvent ret = api.monitorHistory();
    		List<String> activityLog = api.sweepLog();
    		for (String item : activityLog) {
    			logDebug("JACE Log: " + item);
    		}
    		return ret;
    	}
    }

}
