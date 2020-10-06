/**
 * 
 */
package com.trivir.ace.api;

import java.util.Date;

public class AceApiEvent {
	private AceApiEventType type;
	private String info;
	private String description;
	private Date date;
	private String user;
	private String tokenSerial;
	private String group;
	private String userGuid;

	public void setType(AceApiEventType type) {
		this.type = type;
	}
	
	public AceApiEventType getType() {
		return type;
	}
	
	
	public void setInfo(String info) {
		this.info = info;
	}
	
	public String getInfo() {
		return info;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setTokenSerial(String tokenSerial) {
		this.tokenSerial = tokenSerial;
	}
	
	public String getTokenSerial() {
		return tokenSerial;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	public String getGroup() {
		return group;
	}
	
	public void setUserGuid(String userGuid) {
		this.userGuid = userGuid;
	}
	
	public String getUserGuid() {
		return userGuid;
	}
	
	@Override
	public String toString() {
		return description;
	}
}