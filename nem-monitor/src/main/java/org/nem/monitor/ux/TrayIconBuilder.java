package org.nem.monitor.ux;

import org.nem.core.async.*;
import org.nem.core.connect.*;
import org.nem.core.connect.client.DefaultAsyncNemConnector;
import org.nem.monitor.*;
import org.nem.monitor.node.*;
import org.nem.monitor.visitors.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;

/**
 * A builder that is used to construct the system tray icon.
 */
public class TrayIconBuilder {
	private static final Logger LOGGER = Logger.getLogger(TrayIconBuilder.class.getName());

	private final HttpMethodClient<ErrorResponseDeserializerUnion> client;
	private final WebStartLauncher webStartLauncher;
	private final WebBrowser webBrowser;
	private final boolean isStartedViaWebStart;
	private final TrayIcon trayIcon;
	private final Dimension dimension;
	private final PopupMenu popup;
	private final Collection<NodeStatusVisitor> visitors = new ArrayList<>();
	private final Collection<NemNodePolicy> nodePolicies = new ArrayList<>();

	/**
	 * Creates a new builder.
	 *
	 * @param client The http method client.
	 * @param webStartLauncher The web start launcher.
	 * @param webBrowser The web browser.
	 * @param isStartedViaWebStart true if the program was started via webstart.
	 */
	public TrayIconBuilder(
			final HttpMethodClient<ErrorResponseDeserializerUnion> client,
			final WebStartLauncher webStartLauncher,
			final WebBrowser webBrowser,
			final boolean isStartedViaWebStart) {
		this.client = client;
		this.webStartLauncher = webStartLauncher;
		this.webBrowser = webBrowser;
		this.isStartedViaWebStart = isStartedViaWebStart;

		// initially create the tray icon around a 1x1 pixel image because it cannot be created around a null image
		this.trayIcon = new TrayIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
		this.dimension = this.trayIcon.getSize();
		this.popup = new PopupMenu();

		this.visitors.add(new NodeStatusToIconDescriptorAdapter(this::setImage));
	}

	private void setImage(final IconDescriptor descriptor) {
		final URL imageUrl = TrayIconBuilder.class.getClassLoader().getResource(descriptor.getImageFileName());
		// we could use this.trayIcon.setAutoSize, but it gives uglier result
		final Image unscaledImage = (new ImageIcon(imageUrl, descriptor.getDescription())).getImage();
		final Image scaledImage = unscaledImage.getScaledInstance(this.dimension.width, this.dimension.height, Image.SCALE_SMOOTH);
		this.trayIcon.setImage(scaledImage);
		this.trayIcon.setToolTip(descriptor.getDescription());
		this.trayIcon.displayMessage(LanguageSupport.message("tooltip.info"), descriptor.getDescription(), TrayIcon.MessageType.NONE);
	}

	/**
	 * Adds status menu items for the specified node policy.
	 *
	 * @param nodePolicy The node policy.
	 * @param jnlpUrl The webstart url.
	 */
	public void addStatusMenuItems(final NemNodePolicy nodePolicy, final String jnlpUrl) {
		final NemNodeType nodeType = nodePolicy.getNodeType();
		final NodeManager manager = new NodeManager(
				nodePolicy,
				jnlpUrl,
				this.createConnector(nodePolicy),
				this.webStartLauncher,
				this.webBrowser);
		final NodeStatusToManagementActionAdapter actionAdapter = new NodeStatusToManagementActionAdapter(nodeType, manager);

		final MenuItem statusMenuItem = new MenuItem();
		final MenuItem actionMenuItem = new MenuItem();
		final NodeStatusToStatusDescriptionAdapter visitor = new NodeStatusToStatusDescriptionAdapter(nodeType, description -> {
			statusMenuItem.setLabel(description.getStatusMessage());
			actionMenuItem.setLabel(description.getActionMessage());
		});

		this.popup.add(statusMenuItem);
		this.popup.add(actionMenuItem);
		if (nodePolicy.hasBrowserGui()) {
			final MenuItem launchMenuItem = new MenuItem(String.format(LanguageSupport.message("menu.open.in.browser"), nodePolicy.getNodeType()));
			launchMenuItem.addActionListener(e -> manager.launchBrowser());
			this.popup.add(launchMenuItem);
		}

		actionMenuItem.addActionListener(actionAdapter);
		if (this.isStartedViaWebStart) {
			// simulate a click, which will trigger a webstart launch
			actionAdapter.actionPerformed(new ActionEvent(actionMenuItem, ActionEvent.ACTION_PERFORMED, actionMenuItem.getLabel()));
		}

		this.visitors.add(visitor);
		this.visitors.add(actionAdapter);
		this.nodePolicies.add(nodePolicy);
	}

	/**
	 * Adds a menu item separator.
	 */
	public void addSeparator() {
		this.popup.addSeparator();
	}

	/**
	 * Adds an exit menu item.
	 *
	 * @param tray The system tray.
	 */
	public void addExitMenuItem(final SystemTray tray) {
		final MenuItem exitItem = new MenuItem(LanguageSupport.message("menu.exit"));
		this.popup.add(exitItem);
		exitItem.addActionListener(e -> {
			tray.remove(this.trayIcon);
			exit();
		});
	}

	/**
	 * Adds a shutdown and exit menu item.
	 *
	 * @param tray The system tray.
	 */
	public void addExitAndShutdownMenuItem(final SystemTray tray) {
		final MenuItem exitItem = new MenuItem(LanguageSupport.message("menu.exit.and.shutdown"));
		this.popup.add(exitItem);

		tray.remove(this.trayIcon);
		exitItem.addActionListener(e -> this.shutdownAll().thenAccept(v -> exit()));
	}

	private CompletableFuture<Void> shutdownAll() {
		final List<CompletableFuture> futures = this.nodePolicies.stream()
				.map(np -> this.createConnector(np)
						.shutdown()
						.exceptionally(e -> {
							LOGGER.warning(String.format("an error occurred while attempting to shutdown %s: %s", np, e));
							return null;
						}))
				.collect(Collectors.toList());
		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
	}

	private static void exit() {
		System.exit(0);
	}

	/**
	 * Creates the tray icon and hooks up all event handlers.
	 *
	 * @return The tray icon.
	 */
	public TrayIcon create() {
		final AggregateNemStatusVisitor visitor = new AggregateNemStatusVisitor(this.visitors);

		for (final NemNodePolicy nodePolicy : this.nodePolicies) {
			final NemNodeType nodeType = nodePolicy.getNodeType();
			final NemConnector connector = this.createConnector(nodePolicy);
			new AsyncTimer(
					() -> connector.getStatus().thenAccept(status -> visitor.notifyStatus(nodeType, status)),
					250,
					new UniformDelayStrategy(1000),
					null);
		}

		this.trayIcon.setPopupMenu(this.popup);
		return this.trayIcon;
	}

	private NemConnector createConnector(final NemNodePolicy nodePolicy) {
		final DefaultAsyncNemConnector<String> connector = new DefaultAsyncNemConnector<>(
				this.client,
				r -> { throw new NemNodeExpectedException(); });
		connector.setAccountLookup(null);
		return new NemConnector(nodePolicy, connector);
	}
}