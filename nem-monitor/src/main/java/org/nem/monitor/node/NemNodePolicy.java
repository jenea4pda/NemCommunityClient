package org.nem.monitor.node;

import org.nem.core.node.NodeEndpoint;

import java.io.File;

/**
 * A strategy for handling different types of NEM nodes.
 */
public interface NemNodePolicy {

	/**
	 * Gets the endpoint for the NEM node.
	 *
	 * @return The endpoint.
	 */
	public NodeEndpoint getEndpoint();

	/**
	 * Gets the node type.
	 *
	 * @return The node type.
	 */
	public NemNodeType getNodeType();

	/**
	 * Gets the lock file.
	 *
	 * @return The lock file.
	 */
	public File getLockFile();

	// TODO 20141108 J-T: why did you change this from NisApiId

	/**
	 * Maps the specified api url to the url path.
	 *
	 * @param apiUrl The api url.
	 * @return The url path.
	 */
	public String mapToUrlPath(final String apiUrl);

	/**
	 * Gets a value indicating whether or not this node exposes a browser GUI.
	 *
	 * @return true if a browser gui is exposed, false otherwise.
	 */
	public boolean hasBrowserGui();
}
