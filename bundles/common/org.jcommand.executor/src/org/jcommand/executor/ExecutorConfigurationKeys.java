package org.jcommand.executor;

import org.jcommand.cm.NodeContentType;
import org.jcommand.cm.SubConfiguration;

public enum ExecutorConfigurationKeys {
	pid("pid", NodeContentType.String), poolTargetSize("poolTargetSize",
			NodeContentType.Integer), maxWaitMinutesForShutdown("maxWaitMinutesForShutdown", NodeContentType.Integer);

	public static final String PATH = SubConfiguration.Executor.toString();

	private String elementName;

	private NodeContentType contentType;

	ExecutorConfigurationKeys(String elementName, NodeContentType contentType) {
		this.elementName = elementName;
		this.contentType = contentType;
	}

	public String getKey() {
		return PATH + "/" + elementName;
	}

	public NodeContentType getContentType() {
		return contentType;
	}
}
