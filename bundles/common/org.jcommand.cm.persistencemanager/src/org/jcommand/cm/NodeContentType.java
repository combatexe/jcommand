package org.jcommand.cm;

public enum NodeContentType {

	Long, Integer, Float, Double, String, Boolean, Url, File;

	public static NodeContentType toType(String typeAsString) {
		for (NodeContentType value : values()) {
			if (value.name().matches("(?i)"+typeAsString)) {
				return value;
			}
		}
		return null;
	}
}
