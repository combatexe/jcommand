package org.jcommand.cm.internal.parser;

import java.util.Comparator;

import org.jcommand.cm.ConfigurationNode;
import org.jcommand.cm.ConfigurationNodeType;

public class LevelComperator implements Comparator<ConfigurationNode> {

	@Override
	public int compare(ConfigurationNode node1, ConfigurationNode node2) {
		Integer node1SegmentCount = node1.getLevel().split("/").length;
		Integer node2SegmentCount = node2.getLevel().split("/").length;
		int compareResult = node1SegmentCount.compareTo(node2SegmentCount);
		if (compareResult == 0) {
			if (node1.getType().equals(node2.getType())) {
				return 0;
			} else if (node1.getType().equals(ConfigurationNodeType.DEFAULT)) {
				return -1;
			} else if (node2.getType().equals(ConfigurationNodeType.DEFAULT)) {
				return 1;
			}
		}
		return compareResult;
	}

}
