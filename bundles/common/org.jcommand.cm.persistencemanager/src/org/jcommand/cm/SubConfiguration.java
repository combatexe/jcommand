package org.jcommand.cm;

public enum SubConfiguration {
	CommandQueue("/instance/queues/queue", "org.jcommand.cm.persistencemanager.QueueConfiguration",
			true), Executor("/instance/threadpool", "org.jcommand.cm.persistencemanager.ExecutorConfiguration");

	private String xpath;
	private String pid;
	private boolean isFactory = false;

	private SubConfiguration(String xpath, String pid) {
		this.xpath = xpath;
		this.pid = pid;
	}

	private SubConfiguration(String xpath, String pid, boolean isFactory) {
		this.xpath = xpath;
		this.pid = pid;
		this.isFactory = isFactory;
	}

	@Override
	public String toString() {
		return xpath;
	}

	public String getPid() {
		return pid;
	}

	public boolean isFactory() {
		return isFactory;
	}
}
