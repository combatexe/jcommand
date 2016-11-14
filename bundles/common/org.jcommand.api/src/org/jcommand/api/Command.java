package org.jcommand.api;

/**
 * @author fhoelting
 *
 */
public interface Command {

	/**
	 * Get the time to execute this command. Use standalone test result on the same hardware and software environment.
	 * @return execution time in milli. sec.
	 */
	long getAvgExecuteTime();
	
	 /**
	 * Get the static priority of this command. Define by operation ad develop time.
	 * Every call of getStaticPriority increment the dynamic priority.
	 * @return current dynamic priority
	 */
	short getStaticPriority();
	 
	 /**
	 * Every call of getStaticPriority increment the dynamic priority.
	 * @return current dynamic priority
	 */
	short getDynamicPriority();
}

