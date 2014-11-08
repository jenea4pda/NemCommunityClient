package org.nem.monitor.visitors;

import org.nem.monitor.*;
import org.nem.monitor.node.NemNodePolicy;
// TODO 20141108: i guess i am missing something? the monitor is now referencing ncc?
import org.nem.ncc.controller.viewmodels.ConfigurationViewModel;

import java.util.logging.Logger;

/**
 * Class that provides management functionality for a node.
 */
public class NodeManager {
	private static final Logger LOGGER = Logger.getLogger(WebStartLauncher.class.getName());

	private final NemNodePolicy nodePolicy;
	private final String configFileName;
	private final NemConnector connector;
	private final JavaLauncher launcher;
	private final WebBrowser browser;

	/**
	 * Creates a new visitor.
	 *
	 * @param nodePolicy The policy of the node being managed.
	 * @param configFileName The config file name.
	 * @param connector The nem connector.
	 * @param launcher The Java start launcher.
	 * @param browser The web browser.
	 */
	public NodeManager(final NemNodePolicy nodePolicy, final String configFileName, final NemConnector connector, final JavaLauncher launcher, final WebBrowser browser) {
		this.nodePolicy = nodePolicy;
		this.configFileName = configFileName;
		this.connector = connector;
		this.launcher = launcher;
		this.browser = browser;
	}

	/**
	 * Shuts down the node.
	 */
	public void shutdown() {
		LOGGER.info(String.format("Shutting down node %s", this.nodePolicy.getNodeType()));
		this.connector.shutdown();
	}

	/**
	 * Launches the node.
	 */
	public void launch() {
		LOGGER.info(String.format("Launching node %s", this.nodePolicy.getNodeType()));
		this.launcher.launch(this.nodePolicy.getNodeType(), this.configFileName);
	}

	/**
	 * Navigates to the node's browser start page (if supported by the node).
	 */
	public void launchBrowser() {
		if (this.nodePolicy.hasBrowserGui()) {
			LOGGER.info(String.format("Launching browser for node %s", this.nodePolicy.getNodeType()));
			this.browser.navigate(this.nodePolicy.getEndpoint());
		}
	}

	// TODO 20141108: comment ;)

	public ConfigurationViewModel getConfig() {
		return this.connector.getConfiguration();
	}
}
