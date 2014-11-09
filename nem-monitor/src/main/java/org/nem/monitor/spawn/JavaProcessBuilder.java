package org.nem.monitor.spawn;

import org.nem.core.deploy.*;
import org.nem.monitor.node.NemNodeType;

import java.io.*;
import java.util.logging.Logger;

// TODO 20141108: fix javadoc remove commented out code
// TODO 20141109: done

/**
 * Allows the starting of components as separate processes.
 */
public class JavaProcessBuilder implements JavaSpawnBuilder {
	private static final Logger LOGGER = Logger.getLogger(JavaProcessBuilder.class.getName());

	private final ProcessBuilder builder;

	/**
	 * Creates a new Java process builder. 
	 * Depending on the node type, the configuration file name and node type are passed to common starter.
	 *
	 * @param nodeType
	 */
	public JavaProcessBuilder(final NemNodeType nodeType) {
		final String configFilePath = nodeType == NemNodeType.NCC ? "ncc-config.properties" : "nis-config.properties";
		final String nodeTypeText = nodeType == NemNodeType.NCC ? "-ncc" : "-nis";
		this.builder = new ProcessBuilder("java", "-classpath", System.getProperty("java.class.path"), CommonStarter.class.getCanonicalName(), "-config", configFilePath, nodeTypeText);
	}

	/* (non-Javadoc)
	 * @see org.nem.monitor.JavaSpawnBuilder#setLogFile(java.io.File)
	 */
	@Override
	public void setLogFile(final File logFile) {
		this.builder.redirectErrorStream(true);
		this.builder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
//		this.builder.directory(logFile.getParentFile());
	}

	/* (non-Javadoc)
	 * @see org.nem.monitor.JavaSpawnBuilder#start()
	 */
	@Override
	public void start() throws IOException {
		LOGGER.info(String.format("Starting Java process: %s", builder.command()));
		this.builder.start();
	}
}
