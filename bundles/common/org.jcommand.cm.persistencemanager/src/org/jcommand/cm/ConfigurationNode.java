package org.jcommand.cm;

import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ConfigurationNode extends Hashtable<String, Object> {

	public static final String OVERRIDE = ".override";

	public String configurationLevel = "/";

	private ConfigurationNodeType type;

	@SuppressWarnings("unchecked")
	public ConfigurationNode(ConfigurationNode parent, Dictionary<String, Object> properties, String configurationPath,
			int configurationPosition) {
		super((Map<String, Object>) properties);
		this.parent = parent;
		this.configurationPath = configurationPath;
		this.setConfigurationPosition(configurationPosition);
	}

	public ConfigurationNode(String configurationPath, String configurationLevel) {
		this(configurationPath, configurationLevel, ConfigurationNodeType.NODE);
	}

	public ConfigurationNode(String configurationPath, String configurationLevel, ConfigurationNodeType type) {
		this.configurationPath = configurationPath;
		this.configurationLevel = configurationLevel;
		this.setType(type);
	}

	
	@Override
	public boolean containsValue(Object value) {
		boolean exist = super.containsValue(value);
		exist = exist | getParent() != null ? getParent().containsValue(value) : false;
		return exist;
	}

	@Override
	public boolean containsKey(Object key) {
		boolean exist = super.containsKey(key);
		exist = exist | super.containsKey(key + OVERRIDE);
		exist = exist | getParent() != null ? getParent().containsKey(key) : false;
		return exist;
	}

	@Override
	public Object get(Object key) {
		Object value = super.get(key + OVERRIDE);
		if (value == null) {
			value = super.get(key);
			if (value == null && getParent() != null) {
				value = getParent().get(key);
			}
		}
		return value;
	}

	@Override
	public Object getOrDefault(Object key, Object defaultValue) {
		Object value = get(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		Set<Entry<String, Object>> entrySet = new HashSet<>();
		entrySet.addAll(super.entrySet());
		if (parent != null) {
			entrySet.addAll(parent.entrySet());
		}
		return entrySet;
	}

	@Override
	public Set<String> keySet() {
		Set<String> keySet = new HashSet<>();
		keySet.addAll(super.keySet());
		if (parent != null) {
			keySet.addAll(parent.keySet());
		}
		return keySet;
	}

	@Override
	public Collection<Object> values() {
		Collection<Object> values = super.values();
		if(parent != null){
			values.addAll(parent.values());
		}
		return values;
	}
	
	@Override
	public synchronized Enumeration<String> keys() {
		return Collections.enumeration(keySet());
	}

	@Override
	public synchronized Enumeration<Object> elements() {
		return super.elements();
	}



	/**
	 * 
	 */
	private static final long serialVersionUID = -5972907460755196646L;

	private ConfigurationNode parent;

	private String configurationPath;

	private int configurationPosition;

	public String getLevel() {
		return configurationLevel;
	}

	public void setLevel(String configurationLevel) {
		this.configurationLevel = configurationLevel;
	}

	public ConfigurationNode getParent() {
		return parent;
	}

	public void setParent(ConfigurationNode parent) {
		this.parent = parent;
	}

	public String getConfigurationPath() {
		return configurationPath;
	}

	public int getConfigurationPosition() {
		return configurationPosition;
	}

	public void setConfigurationPosition(int configurationPosition) {
		this.configurationPosition = configurationPosition;
	}

	public ConfigurationNodeType getType() {
		return type;
	}

	public void setType(ConfigurationNodeType type) {
		this.type = type;
	}
}
