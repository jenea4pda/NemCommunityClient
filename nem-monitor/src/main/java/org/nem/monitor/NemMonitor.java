package org.nem.monitor;

import org.nem.core.connect.*;
import org.nem.core.connect.client.DefaultAsyncNemConnector;
import org.nem.core.deploy.LoggingBootstrapper;
import org.nem.core.utils.LockFile;
import org.nem.monitor.config.*;
import org.nem.monitor.node.*;
import org.nem.monitor.ux.*;
import org.nem.monitor.visitors.*;

import java.awt.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

/**
 * The nem monitor program.
 */
public class NemMonitor {
	private static final Logger LOGGER = Logger.getLogger(NemMonitor.class.getName());
	private static Closeable fileLockHandle;

	static {
		// initialize logging before anything is logged; otherwise not all
		// settings will take effect
		LoggingBootstrapper.bootstrap(new MonitorConfiguration().getNemFolder());
	}

	/**
	 * The main entry point.
	 *
	 * @param args The command line arguments.
	 */
	public static void main(final String[] args) {
		startMonitor(args);
	}

	/**
	 * Starts the monitor with some configurations related to what has to be monitored or started by it
	 * 
	 * @param args - any parameters from the command line
	 */
	public static void startMonitor(final String[] args) {
		final MonitorCommandLine commandLine = MonitorCommandLine.parse(args);
		LOGGER.info(String.format("NCC config file configured as: %s", commandLine.getNccConfig()));
		LOGGER.info(String.format("NIS config file configured as: %s", commandLine.getNisConfig()));

		final String nemFolder = new MonitorConfiguration().getNemFolder();
		if (!canStart(nemFolder)) {
			return;
		}

		SwingUtilities.invokeLater(() -> {
			
			//Pop-Up the keep patient window
			KeepPatientWindow.openWindow();
			
			LOGGER.info("setting up system tray");

			// fail if the system tray is not supported
			if (!SystemTray.isSupported()) {
				throw new SystemTrayException("SystemTray is not supported");
			}

			// TODO 20141108: i would probably move this to a separate function too
			final SystemTray tray = SystemTray.getSystemTray();
			final JavaLauncher launcher = new JavaLauncher(nemFolder, isStartedViaWebStart());
			final WebBrowser webBrowser = new WebBrowser();
			final HttpMethodClient<ErrorResponseDeserializerUnion> httpClient = createHttpMethodClient();

			final NisNodePolicy nisPolicy = new NisNodePolicy(nemFolder);
			final NemConnector nisConnector = createConnector(nisPolicy, httpClient);
			final NodeManager nisManager = new NodeManager(nisPolicy, commandLine.getNisConfig(), nisConnector, launcher, webBrowser);

			final NccNodePolicy nccPolicy = new NccNodePolicy(nemFolder);
			final NemConnector nccConnector = createConnector(nccPolicy, httpClient);
			final NodeManager nccManager = new NodeManager(nccPolicy, commandLine.getNccConfig(), nccConnector, launcher, webBrowser);

			final TrayIconBuilder builder = new TrayIconBuilder(createHttpMethodClient());
			builder.addStatusMenuItems(nisManager, nisPolicy);
			builder.addSeparator();
			builder.addStatusMenuItems(nccManager, nccPolicy);
			builder.addSeparator();
			builder.addExitMenuItem(tray);
			builder.addExitAndShutdownMenuItem(tray);

			// TODO 20141108: can you move this to a separate function? also stateMachine
			final NemClientStateMachineAdapter statemachine = new NemClientStateMachineAdapter();
			statemachine.setStartNccEventConsumer(text -> nccManager.launch());
			statemachine.setStartNisEventConsumer(text -> nisManager.launch());
			statemachine.setStartNccConfigurationSupplier(nccManager::getConfig);
			statemachine.setStartBrowserEventConsumer(text -> {
				nccManager.launchBrowser();
				KeepPatientWindow.window.dispose();
			});
			statemachine.setLocalNisConfiguredEventConsumer(localNis -> KeepPatientWindow.window.updateLocalNisInformation(localNis));
			builder.getVisitors().add(statemachine);

			builder.getVisitors().add(KeepPatientWindow.window.addNccDescriptionUpdater());
			builder.getVisitors().add(KeepPatientWindow.window.addNisDescriptionUpdater());
			builder.getVisitors().add(KeepPatientWindow.window.addNccProgressUpdater());
			builder.getVisitors().add(KeepPatientWindow.window.addNisProgressUpdater());

			try {
				tray.add(builder.create());
			} catch (final AWTException e) {
				throw new SystemTrayException("Unable to add icon to system tray", e);
			}
		});
	}

	private static HttpMethodClient<ErrorResponseDeserializerUnion> createHttpMethodClient() {
		final int CONNECTION_TIMEOUT = 2000;
		final int SOCKET_TIMEOUT = 2000;
		final int REQUEST_TIMEOUT = 4000;
		return new HttpMethodClient<>(CONNECTION_TIMEOUT, SOCKET_TIMEOUT, REQUEST_TIMEOUT);
	}

	private static boolean canStart(final String nemFolder) {
		final File lockFile = Paths.get(nemFolder, "mon.lock").toFile();
		LOGGER.info(String.format("Acquiring exclusive lock to lock file: %s", lockFile));
		fileLockHandle = LockFile.tryAcquireLock(lockFile);
		if (null == fileLockHandle) {
			LOGGER.severe(String.format("Could not acquire exclusive lock to lock file"));
			return false;
		}

		return true;
	}

	public static boolean isStartedViaWebStart() {
		try {
			Class clazz = Class.forName("javax.jnlp.ServiceManager");
			Method lookup = clazz.getMethod("lookup", String.class);
			lookup.invoke(clazz, "javax.jnlp.DownloadService2");
			LOGGER.info("NEM monitor was started via web start.");
			return true;
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			return false;
		}
	}

	private static NemConnector createConnector(final NemNodePolicy nodePolicy, final HttpMethodClient<ErrorResponseDeserializerUnion> client) {
		final DefaultAsyncNemConnector<String> connector = new DefaultAsyncNemConnector<>(client, r -> {
			throw new NemNodeExpectedException();
		});
		connector.setAccountLookup(null);
		return new NemConnector(nodePolicy, connector);
	}
}