package org.jcommand.cm.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.jcommand.cm.ConfigurationNode;
import org.jcommand.cm.SubConfiguration;
import org.jcommand.cm.internal.ConfigurationDefaultService;
import org.jcommand.cm.persistencemanager.SVNConfigPersistencManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class BackingStoreTest {

	private static final String QUEUE_ID = "queueId";
	static File configDir = new File("config");

	@BeforeClass
	public static void beforeClass() throws IOException {
		
		configDir.mkdir();
	}

	@Before
	public void beforeTest() {
		System.setProperty("systemConfigurationMatcher", "/application/cluster1/tanaga/instance1");
		System.setProperty("configurationLocation","config");
		assertThat("config dir exist", configDir.exists(), is(true));
	}

	@Test
	public void singleNode() {
		ConfigurationNode config = new ConfigurationNode(SubConfiguration.CommandQueue.toString(), "/");
		config.put(QUEUE_ID, 34l);
		assertThat("id of queue", config.get(QUEUE_ID), is(34l));
	}

	@Test
	public void parentNode() {
		ConfigurationNode config = new ConfigurationNode(SubConfiguration.CommandQueue.toString(), "/application");
		config.setParent(new ConfigurationNode(SubConfiguration.CommandQueue.toString(), "/"));
		assertThat("ConfigurationNode parent exist", config.getParent(), notNullValue());
	}

	@Test
	public void mergeWithParentNode() {
		ConfigurationNode config = new ConfigurationNode(SubConfiguration.CommandQueue.toString(),
				"/application/cluster1");
		config.put(QUEUE_ID, 34l);
		config.setParent(new ConfigurationNode(SubConfiguration.CommandQueue.toString(), "/application"));
		config.getParent().put(QUEUE_ID, 99l);
		assertThat("id of queue", config.get(QUEUE_ID), is(34l));

		config.remove(QUEUE_ID, 34l);
		assertThat("id of queue", config.get(QUEUE_ID), is(99l));

		config.getParent().setParent(new ConfigurationNode(SubConfiguration.CommandQueue.toString(), "/"));
		config.getParent().getParent().put(QUEUE_ID, 100l);
		assertThat("id of queue", config.get(QUEUE_ID), is(99l));

		config.getParent().remove(QUEUE_ID, 99l);
		assertThat("id of queue", config.get(QUEUE_ID), is(100l));
	}

	@Test
	public void mergeWithLocalOverrideNode() {
		ConfigurationNode config = new ConfigurationNode(SubConfiguration.CommandQueue.toString(),
				"/application/cluster1");
		config.put(QUEUE_ID, 34l);
		config.setParent(new ConfigurationNode(SubConfiguration.CommandQueue.toString(), "/application/"));
		config.put(QUEUE_ID + ConfigurationNode.OVERRIDE, 77l);
		config.getParent().put(QUEUE_ID, 99l);
		assertThat("id of queue", config.get(QUEUE_ID), is(77l));

		config.remove(QUEUE_ID, 34l);
		assertThat("id of queue", config.get(QUEUE_ID), is(77l));

		config.getParent().setParent(new ConfigurationNode(SubConfiguration.CommandQueue.toString(), "/"));
		config.getParent().getParent().put(QUEUE_ID, 100l);
		assertThat("id of queue", config.get(QUEUE_ID), is(77l));

		config.getParent().remove(QUEUE_ID, 99l);
		assertThat("id of queue", config.get(QUEUE_ID), is(77l));
	}

	@Test
	public void parseConfiguration() throws IOException, ParserConfigurationException, SAXException {
		SVNConfigPersistencManager configPersistencManager = new SVNConfigPersistencManager();
		ConfigurationDefaultService configurationService = new ConfigurationDefaultService();
		configurationService.activate();
		configPersistencManager.bindConfigurationService(configurationService);
		configPersistencManager.activate();
	}
	
	//@Test(expected=RuntimeException.class)
	public void parseConfigurationWithWrongSystemConfigurationMatcher() throws IOException, ParserConfigurationException, SAXException {
		System.setProperty("systemConfigurationMatcher", "/application/cluster1");
		SVNConfigPersistencManager configPersistencManager = new SVNConfigPersistencManager();
		configPersistencManager.activate();
	}
}
