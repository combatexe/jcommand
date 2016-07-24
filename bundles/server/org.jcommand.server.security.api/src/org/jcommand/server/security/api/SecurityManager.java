package org.jcommand.server.security.api;

import org.jcommand.api.Command;

public interface SecurityManager {

	SecurityToken getCommandPermissions(Command command);
	
	boolean isServiceCallPermit(SecurityToken securityToken);
	
}
