package com.trivir.util;

public class Version {
	private Version() {}

	@SuppressWarnings("unchecked")
	public static String getVersion(Class clazz) {
		return clazz.getPackage().getImplementationVersion();
	}
}
