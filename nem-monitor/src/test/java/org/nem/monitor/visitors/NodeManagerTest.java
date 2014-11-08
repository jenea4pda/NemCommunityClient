package org.nem.monitor.visitors;

import org.nem.core.node.NodeEndpoint;
import org.nem.monitor.*;
import org.nem.monitor.node.*;

import org.junit.Test;
import org.mockito.Mockito;

public class NodeManagerTest {

	@Test
	public void shutdownDelegatesToConnector() {
		// Arrange:
		final TestContext context = new TestContext();

		// Act:
		context.manager.shutdown();

		// Assert:
		Mockito.verify(context.connector, Mockito.times(1)).shutdown();
	}

	@Test
	public void launchDelegatesToLauncher() {
		// Arrange:
		final TestContext context = new TestContext();

		// Act:
		context.manager.launch();

		// Assert:
		Mockito.verify(context.launcher, Mockito.times(1)).launch(NemNodeType.NCC, "foo.jnlp");
	}

	@Test
	public void launchBrowserDelegatesToBrowserWhenNodeHasBrowserGui() {
		// Arrange:
		final TestContext context = new TestContext(true);

		// Act:
		context.manager.launchBrowser();

		// Assert:
		Mockito.verify(context.browser, Mockito.times(1)).navigate(NodeEndpoint.fromHost("10.0.0.12"));
	}

	@Test
	public void launchBrowserDoesNotDelegateToBrowserWhenNodeDoesNotHaveBrowserGui() {
		// Arrange:
		final TestContext context = new TestContext(false);

		// Act:
		context.manager.launchBrowser();

		// Assert:
		Mockito.verify(context.browser, Mockito.times(0)).navigate(NodeEndpoint.fromHost(Mockito.any()));
	}

	private static class TestContext {
		private final NemConnector connector = Mockito.mock(NemConnector.class);
		private final JavaLauncher launcher = Mockito.mock(JavaLauncher.class);
		private final WebBrowser browser = Mockito.mock(WebBrowser.class);
		private final NodeManager manager;

		private TestContext() {
			this(false);
		}

		private TestContext(final boolean hasBrowserGui) {
			// TODO 20141108 J-T: can you explicitly set NodeType?
			final NemNodePolicy nodePolicy = Mockito.mock(NemNodePolicy.class);
			Mockito.when(nodePolicy.getEndpoint()).thenReturn(NodeEndpoint.fromHost("10.0.0.12"));
			Mockito.when(nodePolicy.hasBrowserGui()).thenReturn(hasBrowserGui);

			this.manager = new NodeManager(nodePolicy, "foo.jnlp", this.connector, this.launcher, this.browser);
		}
	}
}