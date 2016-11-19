package org.jcommand.queue.manager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class PersistenceQueue<T> implements Serializable {

	private static final long serialVersionUID = 4169482821928612028L;

	public PriorityQueue<T> queue = new PriorityQueue<T>();

	public Map<Long, T> inTransaction = new HashMap<>();

}
