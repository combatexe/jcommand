package org.jcommand.queue.manager;

import org.jcommand.cm.NodeContentType;
import org.jcommand.cm.SubConfiguration;

public enum QueueConfigurationKeys {
	pid("pid", NodeContentType.String), queueLocation("queueLocation", NodeContentType.File), queueStatus("queueStatus",
			NodeContentType.Integer), maxQueueEntries("maxQueueEntries", NodeContentType.Integer);

	public static final String PATH = SubConfiguration.CommandQueue.toString();

	private String elementName;

	private NodeContentType contentType;

	QueueConfigurationKeys(String elementName, NodeContentType contentType) {
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
