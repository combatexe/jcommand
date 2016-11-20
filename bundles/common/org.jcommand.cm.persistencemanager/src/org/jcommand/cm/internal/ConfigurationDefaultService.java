package org.jcommand.cm.internal;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jcommand.cm.ConfigurationNode;
import org.jcommand.cm.ConfigurationNodeType;
import org.jcommand.cm.ConfigurationService;
import org.jcommand.cm.internal.parser.ConfigurationParser;
import org.jcommand.cm.internal.parser.LevelComperator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component
public class ConfigurationDefaultService implements ConfigurationService {

	Map<String, Map<String, ConfigurationNode>> configurationsByXPathGroupByPid = new HashMap<>(100);

	@Activate
	public void activate() {
		Map<String, List<ConfigurationNode>> configurationsByXPath = parseConfigurations();
		for (Map.Entry<String, List<ConfigurationNode>> nodesByXPath : configurationsByXPath.entrySet()) {

			Map<String, List<ConfigurationNode>> configurationsByPid = groupByPid(nodesByXPath.getValue());
			Map<String, ConfigurationNode> configurationByPid = putParents(configurationsByPid);
			resolveExpressionWithValue(configurationByPid);
			configurationsByXPathGroupByPid.put(nodesByXPath.getKey(), configurationByPid);
		}
	}

	private void resolveExpressionWithValue(Map<String, ConfigurationNode> configurationByPid) {
		Pattern expresionPattern = Pattern.compile("\\$\\{(.*?)\\}");

		for (Entry<String, ConfigurationNode> entry : configurationByPid.entrySet()) {
			ConfigurationNode configurationNode = entry.getValue();
			for (String propertyKey : configurationNode.keySet()) {
				Object value = configurationNode.get(propertyKey);
				if (value != null && value instanceof String) {

					Matcher expressionMatcher = expresionPattern.matcher((String) value);
					String[] valueSegments = ((String) value).split("\\$\\{(.*?)\\}");
					StringBuilder newValueBuilder = new StringBuilder(value.toString().length() + (5 * 10));
					if (!valueSegments[0].isEmpty()) {
						newValueBuilder.append(valueSegments[0]);
					}
					int count = 1;
					while (expressionMatcher.find()) {
						String expression = expressionMatcher.group(1);

						Object substitotionValue = configurationNode
								.get(configurationNode.getConfigurationPath() + "/" + expression);
						if (substitotionValue != null) {
							newValueBuilder.append(substitotionValue);
						} else {
							newValueBuilder.append("[unresolve expression: ");
							newValueBuilder.append(expression);
							newValueBuilder.append("]");
						}
						if (count < valueSegments.length) {
							newValueBuilder.append(valueSegments[count]);
						}
						count++;
					}
					configurationNode.put(propertyKey, newValueBuilder.toString());
				}
			}
		}
	}

	private Map<String, ConfigurationNode> putParents(Map<String, List<ConfigurationNode>> configurationsByPid) {
		Map<String, ConfigurationNode> configurationByPid = new HashMap<String, ConfigurationNode>();
		List<String> pids = new ArrayList<String>(configurationsByPid.keySet().size());
		for (String pid : configurationsByPid.keySet()) {
			pids.add(pid);
			List<ConfigurationNode> configurationNodes = configurationsByPid.get(pid);
			configurationNodes.sort(new LevelComperator());
			ConfigurationNode currentParent = null;
			for (ConfigurationNode configurationNode : configurationNodes) {
				configurationNode.setParent(currentParent);
				currentParent = configurationNode;
			}
			configurationByPid.put(pid, currentParent);
		}
		return configurationByPid;
	}

	private Map<String, List<ConfigurationNode>> groupByPid(List<ConfigurationNode> unsortList) {
		Map<String, List<ConfigurationNode>> configurationsByPid = new HashMap<String, List<ConfigurationNode>>();
		List<ConfigurationNode> defaultConfigurationNodes = new ArrayList<>();
		for (ConfigurationNode configurationNode : unsortList) {
			if (configurationNode.getType().equals(ConfigurationNodeType.DEFAULT)) {
				defaultConfigurationNodes.add(configurationNode);
				continue;
			}
			String configurationPath = configurationNode.getConfigurationPath();
			List<ConfigurationNode> configurationNodes = configurationsByPid
					.get(configurationNode.get(configurationPath + "/pid"));
			if (configurationNodes == null) {
				String pid = (String) configurationNode.get(configurationPath + "/pid");
				if (pid == null) {
					pid = String.valueOf(configurationNode.getConfigurationPosition());
				}
				configurationNodes = new ArrayList<ConfigurationNode>();
				configurationsByPid.put(configurationPath + '.' + pid, configurationNodes);
			}
			configurationNodes.add(configurationNode);
		}
		if (defaultConfigurationNodes.size() > 0) {
			for (Entry<String, List<ConfigurationNode>> entry : configurationsByPid.entrySet()) {
				List<ConfigurationNode> configurationNodes = entry.getValue();
				if (configurationNodes != null) {
					configurationNodes.addAll(defaultConfigurationNodes);
				}
			}
		}

		return configurationsByPid;
	}

	private Map<String, List<ConfigurationNode>> parseConfigurations() {
		Path configRoot = Paths.get(System.getProperty("configurationLocation",
				"../git/jcommand/bundles/common/org.jcommand.cm.persistencemanager/config"));
		if (!Files.exists(configRoot, LinkOption.NOFOLLOW_LINKS)) {
			throw new RuntimeException("Configuration path not exist:" + configRoot.toAbsolutePath());
		}
		String systemConfigurationMatcher = System.getProperty("systemConfigurationMatcher");
		ConfigurationParser configurationParser = new ConfigurationParser(configRoot, systemConfigurationMatcher);
		return configurationParser.parse();
	}

	@Override
	public Map<String, ConfigurationNode> getConfiguration(String xpath) {
		return configurationsByXPathGroupByPid.get(xpath);
	}

}
