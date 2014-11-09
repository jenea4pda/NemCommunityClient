package org.nem.monitor.spawn;

import java.io.*;

// TODO 20141108: comment class
// TODO 20141108: can you move this and derivatives to their own package; since they are very specialized (and simple) i'm ok with not having tests for them
// > put i think they should be separated a bit from the other code
// TODO 20141109: T-J Done.

/**
 * Interface to spawn a new component (NCC or NIS).
 *
 */
public interface JavaSpawnBuilder {

	/**
	 * Sets the log file.
	 *
	 * @param logFile The log file.
	 */
	public abstract void setLogFile(final File logFile);

	/**
	 * Starts to spawn the component.
	 */
	public abstract void start() throws IOException;

}