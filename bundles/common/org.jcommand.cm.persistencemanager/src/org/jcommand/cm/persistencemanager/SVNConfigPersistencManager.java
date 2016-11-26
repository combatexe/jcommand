package org.jcommand.cm.persistencemanager;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.felix.cm.NotCachablePersistenceManager;
import org.apache.felix.cm.PersistenceManager;
import org.jcommand.cm.ConfigurationNode;
import org.jcommand.cm.ConfigurationService;
import org.jcommand.cm.SubConfiguration;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;
import org.xml.sax.SAXException;

@Component(enabled = true, immediate = true, property = {
		"service.ranking=" + Integer.MAX_VALUE }, service = PersistenceManager.class)
public class SVNConfigPersistencManager implements NotCachablePersistenceManager {

	public static final String FACTORY_PID = "factory.pid";

	public static final String FACTORY_PID_LIST = "factory.pidList";

	Map<String, ConfigurationNode> configurationByPid = new HashMap<>();

	private ConfigurationService configurationService;

	private LogService logService;

	@Activate
	public void activate() throws IOException, ParserConfigurationException, SAXException {
		for (SubConfiguration subConfiguration : SubConfiguration.values()) {
			logService.log(LogService.LOG_INFO, "activate jcommand " + PersistenceManager.class.getName());

			Map<String, ConfigurationNode> subConfigurationByPid = configurationService
					.getConfiguration(subConfiguration.toString());
			if (subConfigurationByPid == null) {
				logService.log(LogService.LOG_ERROR, "no pid for subConfiguration" + subConfiguration.name());
				continue;
			}
			for (Entry<String, ConfigurationNode> entry : subConfigurationByPid.entrySet()) {

				if (subConfiguration.isFactory()) {
					entry.getValue().put(Constants.SERVICE_PID, entry.getKey());
					entry.getValue().put("service.factoryPid", subConfiguration.getPid());
				} else {
					entry.getValue().put(Constants.SERVICE_PID, subConfiguration.getPid());
				}
			}
			if (subConfiguration.isFactory()) {
				ConfigurationNode factoryToPidMapping = new ConfigurationNode("", "");
				factoryToPidMapping.put(FACTORY_PID, subConfiguration.getPid());
				factoryToPidMapping.put(FACTORY_PID_LIST,
						subConfigurationByPid.keySet().toArray(new String[subConfigurationByPid.keySet().size()]));
				subConfigurationByPid.put(subConfiguration.getPid() + ".factory", factoryToPidMapping);
			}
			configurationByPid.putAll(subConfigurationByPid);
		}

	}

	@Reference
	public void bindConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public void unbindConfigurationService(ConfigurationService configurationService) {
		this.configurationService = null;
	}

	@Override
	public void delete(String pid) throws IOException {
		configurationByPid.remove(pid);
	}

	@Override
	public boolean exists(String pid) {
		return configurationByPid.containsKey(pid);
	}

	@Override
	public Enumeration<ConfigurationNode> getDictionaries() throws IOException {
		return Collections.enumeration(configurationByPid.values());
	}

	@Override
	public Dictionary<String, Object> load(String pid) throws IOException {
		return configurationByPid.get(pid);
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void store(String pid, Dictionary properties) throws IOException {
		// String configClassAsString = (String)
		// properties.get("service.factoryPid");
		// Class<?> configClass;
		// ConfigurationNode configurationNode = null;
		//
		// try {
		// configClass =
		// FrameworkUtil.getBundle(getClass()).getBundleContext().getBundle()
		// .loadClass(configClassAsString);
		// configurationNode = (ConfigurationNode)
		// configClass.getConstructor(Dictionary.class)
		// .newInstance(properties);
		// } catch (InstantiationException | IllegalAccessException |
		// IllegalArgumentException | InvocationTargetException
		// | NoSuchMethodException | SecurityException | ClassNotFoundException
		// e) {
		// e.printStackTrace();
		// }
		//
		// configurationByPid.put(pid, configurationNode);
	}

	@Reference
	public void bindLogService(LogService logService) {
		this.logService = logService;
	}
}
