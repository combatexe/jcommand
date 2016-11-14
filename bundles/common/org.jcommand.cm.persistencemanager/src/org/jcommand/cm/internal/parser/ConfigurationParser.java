package org.jcommand.cm.internal.parser;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jcommand.cm.ConfigurationNode;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class ConfigurationParser {

	private Path configRoot;
	private String systemConfigurationMatcher;
	private int systemConfigurationMatcherMaxLevel;

	public ConfigurationParser(Path configRoot, String systemConfigurationMatcher) {
		this.configRoot = configRoot;
		this.systemConfigurationMatcher = systemConfigurationMatcher;
		systemConfigurationMatcherMaxLevel = systemConfigurationMatcher.split("/").length;
	}

	public Map<String, List<ConfigurationNode>> parse() {

		Map<String, List<ConfigurationNode>> configurationsByPath = new HashMap<>();
		final XMLConfigurationSaxHandler configurationSaxHandler = new XMLConfigurationSaxHandler(configurationsByPath);
		try {
			Files.walkFileTree(configRoot, new SimpleFileVisitor<Path>() {

				private XMLReader xmlReader;

				{
					SAXParserFactory spf = SAXParserFactory.newInstance();
					SAXParser saxParser = spf.newSAXParser();
					xmlReader = saxParser.getXMLReader();
					xmlReader.setContentHandler(configurationSaxHandler);
				}

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes arg1) throws IOException {
					
					if (!isSystemConfigMatch(dir, systemConfigurationMatcher)) {
						return FileVisitResult.SKIP_SUBTREE;
					}

					Path configFile = dir.resolve("configuration.xml");
					if (Files.exists(configFile, LinkOption.NOFOLLOW_LINKS)) {

						try {
							xmlReader.parse(configFile.toUri().toString());
						} catch (SAXException e) {
							e.printStackTrace();
						}
					}
					return FileVisitResult.CONTINUE;
				}

				private boolean isSystemConfigMatch(Path dir, String systemConfigurationMatcher) {
					Path relativeDir = configRoot.relativize(dir);
					String comparePath = "/" + relativeDir.normalize().toString().replace('\\', '/');
					// TODO: Warning if no instance or node config is match or correct behavior - use only general configuration no instance configuration?
					if (relativeDir.getNameCount() > systemConfigurationMatcherMaxLevel) {
						String msg = String.format(
								"wrong systemConfigurationMatcher - compare systemConfigurationMatcher and configuration path! systemConfigurationMatcher: %s < relativeDir: %s ?",
								systemConfigurationMatcher, comparePath);
						throw new RuntimeException(msg);
					}
					configurationSaxHandler.setLevel(comparePath);
					return systemConfigurationMatcher.startsWith(comparePath);
				}
			});
		} catch (IOException | ParserConfigurationException | SAXException e) {
			throw new RuntimeException("Can't read Configuration", e);
		}

		return configurationsByPath;
	}
}
