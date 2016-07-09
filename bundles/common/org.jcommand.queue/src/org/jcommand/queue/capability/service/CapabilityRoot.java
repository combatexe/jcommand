package org.jcommand.queue.capability.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.wiring.BundleCapability;

class CapabilityRoot implements Serializable{
	
	
	private static final long serialVersionUID = 1201561542495166062L;
	
	Long dbVersionSequencer = new Long(0);
	Map<Long, Set<BundleCapability>> repository = new HashMap<Long, Set<BundleCapability>>(1000);
}