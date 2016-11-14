package org.jcommand.cm.internal.parser;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.jcommand.cm.ConfigurationNode;
import org.jcommand.cm.ConfigurationNodeType;
import org.jcommand.cm.NodeContentType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLConfigurationSaxHandler extends DefaultHandler {

	private Map<String, List<ConfigurationNode>> configurationsByXPath;

	public XMLConfigurationSaxHandler(Map<String, List<ConfigurationNode>> configurationsByPath) {
		this.configurationsByXPath = configurationsByPath;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (currentConfigurationNode != null) {
			String nodePath = currentConfigurationNode.getConfigurationPath();
			if (nodePath.equals(toXPath(currentPath))) {
				List<ConfigurationNode> configurationNodes = configurationsByXPath
						.get(currentConfigurationNode.getConfigurationPath());
				if (configurationNodes == null) {
					configurationNodes = new ArrayList<ConfigurationNode>();
					configurationsByXPath.put(currentConfigurationNode.getConfigurationPath(), configurationNodes);
				}
				int position = countConfigurationNodesByLevel(configurationNodes, currentConfigurationNode.getLevel());
				currentConfigurationNode.setConfigurationPosition(position);
				configurationNodes.add(currentConfigurationNode);
				currentConfigurationNode = null;
			}
		}
		nodeContentType = null;
		currentPath.pop();
		super.endElement(uri, localName, qName);
	}

	private int countConfigurationNodesByLevel(List<ConfigurationNode> configurationNodes, String level){
		int position = 0;
		for (ConfigurationNode configurationNode : configurationNodes) {
			if(configurationNode.getLevel().equals(level)){
				position++;
			}
		}
		return position;
	}
	
	@Override
	public void error(SAXParseException exception) throws SAXException {
		System.err.println(exception);
		super.error(exception);
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		System.out.println(exception);
		super.warning(exception);
	}

	private ConfigurationNode currentConfigurationNode;
	private Stack<String> currentPath = new Stack<>();
	private String currentFilePathLevel;
	private NodeContentType nodeContentType = null;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		currentPath.push(qName);
		String type = attributes.getValue("type");
		if (type != null) {
			switch (type) {
			case "node":
				currentConfigurationNode = new ConfigurationNode(toXPath(currentPath), getLevel());
				break;
			case "default":
				currentConfigurationNode = new ConfigurationNode(toXPath(currentPath), getLevel(), ConfigurationNodeType.DEFAULT);
				break;
			default:
				nodeContentType = NodeContentType.toType(type);
				break;
			}
		}
		super.startElement(uri, localName, qName, attributes);
	}

	private String toXPath(Stack<String> pathStack) {
		StringBuilder xpath = new StringBuilder(pathStack.size() * 20);
		xpath.append('/');
		for (String string : pathStack) {
			xpath.append(string);
			xpath.append('/');
		}
		return xpath.substring(0, xpath.length() - 1);
	}

	public String getLevel() {
		return currentFilePathLevel;
	}

	public void setLevel(String currentFilePathLevel) {
		this.currentFilePathLevel = currentFilePathLevel;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String currentXPath = toXPath(currentPath);
		if (currentConfigurationNode != null && !currentConfigurationNode.getConfigurationPath().equals(currentXPath)) {
			String elementValue = new String(ch, start, length);
			Object value = null;
			if(nodeContentType == null){
				nodeContentType = NodeContentType.String;
			}
			try {
				switch (nodeContentType) {
				case Double:
					value = new Double(elementValue);
					break;
				case Long:
					value = new Long(elementValue);
					break;
				case Integer:
					value = new Integer(elementValue);
					break;
				case Float:
					value = new Float(elementValue);
					break;
				case Url:
					value = new URL(elementValue);
					break;
				case File:
					value = new File(elementValue);
					break;
				default:
					value = elementValue;
					break;
				}
			} catch (Exception e) {
				String msg = String.format("Can't create value from configuration %s with value %s, type shout by %s",
						toXPath(currentPath), elementValue, nodeContentType);
				throw new RuntimeException(msg, e);
			}
			currentConfigurationNode.put(currentXPath, value);
		}
		super.characters(ch, start, length);
	}
}
