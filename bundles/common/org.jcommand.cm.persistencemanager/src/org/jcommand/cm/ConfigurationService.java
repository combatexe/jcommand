package org.jcommand.cm;

import java.util.Map;

public interface ConfigurationService {

	Map<String, ConfigurationNode> getConfiguration(String xpath);
	
}
