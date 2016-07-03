package org.jcommand.prevayler.serializer;

import com.thoughtworks.xstream.XStream;

public class XStreamSerializer extends org.prevayler.foundation.serialization.XStreamSerializer{

	public XStreamSerializer(ClassLoader classLoader) {
		super();
		this.classLoader = classLoader;
	}

	private ClassLoader classLoader;

	/**
	 * Create a new XStream instance. This must be a new instance because XStream instances are not threadsafe.
	 */
	protected XStream createXStream() {
		XStream xstream = new XStream();
		xstream.setClassLoader(classLoader);
		return xstream;
}
}
