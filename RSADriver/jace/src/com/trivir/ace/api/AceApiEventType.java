/**
 * 
 */
package com.trivir.ace.api;

public enum AceApiEventType {
	DOWNLOAD_LOG_RECORDS,
	GROUP_MODIFY,
	IGNORED,
	NOOP,
	TERMINATOR,
	TOKEN_ASSIGNMENT_MODIFY,
	TOKEN_DELETE,
	TOKEN_MODIFY,
	UNKNOWN,
	USER_ADD,
	USER_DELETE,
	USER_MODIFY,
	USER_RADIUS_MODIFY;
	
	@Override
	public String toString() {
		return this.name();
	}
}