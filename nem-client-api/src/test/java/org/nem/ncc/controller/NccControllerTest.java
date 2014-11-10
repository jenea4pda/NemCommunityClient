package org.nem.ncc.controller;

import org.hamcrest.core.IsEqual;
import org.junit.*;
import org.mockito.Mockito;
import org.nem.core.crypto.KeyPair;
import org.nem.core.metadata.ApplicationMetaData;
import org.nem.core.model.primitive.BlockHeight;
import org.nem.core.node.*;
import org.nem.core.time.*;
import org.nem.ncc.connector.PrimaryNisConnector;
import org.nem.ncc.controller.viewmodels.*;
import org.nem.ncc.exceptions.NccException;
import org.nem.ncc.model.*;
import org.nem.ncc.services.*;
import org.nem.ncc.test.*;

import java.util.concurrent.CompletableFuture;

public class NccControllerTest {

	//region getNccInfo

	@Test
	public void getNccInfoCreatesInfoAroundConstructorParameters() {
		// Arrange:
		final TestContext context = new TestContext();

		// Act:
		final NccInfoViewModel viewModel = context.controller.getNccInfo();

		// Assert:
		Assert.assertThat(viewModel.getMetaData().getVersion(), IsEqual.equalTo("ver"));
		Assert.assertThat(viewModel.getRemoteServer(), IsEqual.equalTo("http://10.10.10.12:7890/"));
		Assert.assertThat(viewModel.getLanguage(), IsEqual.equalTo("de-DE"));
	}

	//endregion

	//region getNisInfo

	@Test
	public void getNisInfoFailsIfNisIsNotConnected() {
		// Arrange:
		final TestContext context = new TestContext();
		Mockito.when(context.connector.isConnected()).thenReturn(false);

		// Act:
		ExceptionAssert.assertThrowsNccException(
				v -> context.controller.getNisInfo(),
				NccException.Code.NIS_NOT_AVAILABLE);
	}

	@Test
	public void getNisInfoRetrievesNisNodeInfoFromConnector() {
		// Arrange:
		final TestContext context = new TestContext();
		final NodeIdentity identity = new NodeIdentity(new KeyPair());
		setupContextForNisInfoTests(context, identity);

		// Act:
		final NisNodeInfo info = context.controller.getNisInfo().getNodeInfo();

		// Assert:
		Assert.assertThat(info.getAppMetaData().getVersion(), IsEqual.equalTo("1.0.0"));
		Assert.assertThat(info.getNode().getIdentity(), IsEqual.equalTo(identity));
	}

	@Test
	public void getNisInfoRetrievesNisNodeMetaDataFromServices() {
		// Arrange:
		final TestContext context = new TestContext();
		final NodeIdentity identity = new NodeIdentity(new KeyPair());
		setupContextForNisInfoTests(context, identity);

		// Act:
		final NisNodeMetaData metaData = context.controller.getNisInfo().getNodeMetaData();

		// Assert:
		Assert.assertThat(metaData.getActivePeers(), IsEqual.equalTo(9));
		Assert.assertThat(metaData.getMaxBlockHeight(), IsEqual.equalTo(new BlockHeight(10)));
		Assert.assertThat(metaData.getNodeBlockHeight(), IsEqual.equalTo(new BlockHeight(5)));
	}

	private static void setupContextForNisInfoTests(final TestContext context, final NodeIdentity nodeIdentity) {
		Mockito.when(context.connector.isConnected()).thenReturn(true);
		final NisInfoViewModel viewModel = createNisInfoViewModel(nodeIdentity);

		final NisNodeInfo originalInfo = viewModel.getNodeInfo();
		Mockito.when(context.nodeServices.getNisNodeInfoAsync(context.nisEndpoint))
				.thenReturn(CompletableFuture.completedFuture(originalInfo));

		final NisNodeMetaData originalMetaData = viewModel.getNodeMetaData();
		Mockito.when(context.chainServices.getNodeMetaDataAsync(context.nisEndpoint))
				.thenReturn(CompletableFuture.completedFuture(originalMetaData));
	}

	private static NisInfoViewModel createNisInfoViewModel(final NodeIdentity nodeIdentity) {
		final TimeProvider timeProvider = Mockito.mock(TimeProvider.class);
		Mockito.when(timeProvider.getCurrentTime()).thenReturn(new TimeInstant(17));
		final ApplicationMetaData applicationMetaData = new ApplicationMetaData("NIS", "1.0.0", null, timeProvider);

		final NisNodeInfo nodeInfo = new NisNodeInfo(
				new Node(nodeIdentity, NodeEndpoint.fromHost("localhost")),
				applicationMetaData);
		final NisNodeMetaData nodeMetaData = new NisNodeMetaData(9, new BlockHeight(10), new BlockHeight(5));
		return new NisInfoViewModel(nodeInfo, nodeMetaData);
	}

	// endregion

	//region isNisRunning

	@Test
	public void isNisRunningReturnsZeroWhenNisIsNotConnected() {
		// Arrange:
		final TestContext context = new TestContext();
		Mockito.when(context.connector.isConnected()).thenReturn(false);

		// Act:
		final String result = context.controller.isNisRunning();

		// Assert:
		Assert.assertThat(result, IsEqual.equalTo("0"));
	}

	@Test
	public void isNisRunningReturnsOneWhenNisIsConnected() {
		// Arrange:
		final TestContext context = new TestContext();
		Mockito.when(context.connector.isConnected()).thenReturn(true);

		// Act:
		final String result = context.controller.isNisRunning();

		// Assert:
		Assert.assertThat(result, IsEqual.equalTo("1"));
	}

	//endregion

	//region getBlockHeight

	@Test
	public void getBlockHeightReturnsNisBlockHeight() {
		// Arrange:
		final TestContext context = new TestContext();
		Mockito.when(context.chainServices.getChainHeightAsync(context.nisEndpoint))
				.thenReturn(CompletableFuture.completedFuture(new BlockHeight(8)));

		// Act:
		final BlockHeight height = context.controller.getBlockHeight();

		// Assert:
		Assert.assertThat(height, IsEqual.equalTo(new BlockHeight(8)));
		Mockito.verify(context.chainServices, Mockito.only()).getChainHeightAsync(context.nisEndpoint);
	}

	//endregion

	private static class TestContext {
		private final NodeEndpoint nisEndpoint = NodeEndpoint.fromHost("10.0.0.99");
		private final ApplicationMetaData metaData;
		private final Configuration configuration;
		private final PrimaryNisConnector connector = Mockito.mock(PrimaryNisConnector.class);
		private final NccController controller;
		private final ChainServices chainServices = Mockito.mock(ChainServices.class);
		private final NodeServices nodeServices = Mockito.mock(NodeServices.class);

		private TestContext() {
			final TimeProvider timeProvider = Mockito.mock(TimeProvider.class);
			Mockito.when(timeProvider.getCurrentTime()).thenReturn(new TimeInstant(88));
			final NodeEndpoint remoteServer = NodeEndpoint.fromHost("10.10.10.12");
			final NisBootInfo bootInfo = new NisBootInfo(0, "aid", "nn");
			this.configuration = new Configuration("de-DE", remoteServer, bootInfo, "nem");
			this.metaData = new ApplicationMetaData("app", "ver", null, timeProvider);
			this.controller = new NccController(
					this.configuration,
					this.metaData,
					this.connector,
					this.chainServices,
					this.nodeServices);

			ServicesUtils.setupForwarding(this.connector, this.nisEndpoint);
		}
	}
}