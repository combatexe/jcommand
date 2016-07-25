package org.jcommand.server.security.api;

public interface SecurityToken {

	SecurityToken getSecurityTokenForService(Class<?> serviceClass);
	
	String getToken();
}
