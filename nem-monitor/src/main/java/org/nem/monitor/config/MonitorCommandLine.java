package org.nem.monitor.config;

import org.nem.core.deploy.NemCommandLine;

import org.apache.commons.cli.Option;

import java.util.Arrays;

public class MonitorCommandLine {
	// TODO 20141108 J-T: i guess i will see this later; but you're expecting the commandline params
	// > to be properties files instead of jnlp urls now?
	private static final String NCC_CONFIG = "nccConfig";
	private static final String NCC_CONFIG_DESCRIPTION = "Config file name for starting NCC.";
	private static final String NIS_CONFIG = "nisConfig";
	private static final String NIS_CONFIG_DESCRIPTION = "Config file name for starting NIS.";

	private final String nccConfig;
	private final String nisConfig;

	private MonitorCommandLine(final String nccConfig, final String nisConfig) {
		this.nccConfig = nccConfig;
		this.nisConfig = nisConfig;
	}

	/**
	 * Gets the NCC config file name as string.
	 *
	 * @return The NCC config file name as string.
	 */
	public String getNccConfig() {
		return this.nccConfig;
	}

	/**
	 * Gets the NIS config file name as string.
	 *
	 * @return The NIS config file name as string.
	 */
	public String getNisConfig() {
		return this.nisConfig;
	}

	/**
	 * Parses an array of commandline arguments into a MonitorCommandLine instance.
	 *
	 * @param args The arguments.
	 * @return The configuration.
	 */
	public static MonitorCommandLine parse(final String[] args) {
		final NemCommandLine commandLine = new NemCommandLine(Arrays.asList(
				new Option(NCC_CONFIG, true, NCC_CONFIG_DESCRIPTION),
				new Option(NIS_CONFIG, true, NIS_CONFIG_DESCRIPTION)));

		String nccConfig = "ncc-config.properties";
		String nisConfig = "nis-config.properties";
		if (commandLine.parse(args)) {
			nccConfig = getParameterOrDefault(commandLine, NCC_CONFIG, nccConfig);
			nisConfig = getParameterOrDefault(commandLine, NIS_CONFIG, nisConfig);
		}

		return new MonitorCommandLine(nccConfig, nisConfig);
	}

	private static String getParameterOrDefault(final NemCommandLine commandLine, final String key, final String defaultValue) {
		return commandLine.hasParameter(key) ? commandLine.getParameter(key) : defaultValue;
	}
}
