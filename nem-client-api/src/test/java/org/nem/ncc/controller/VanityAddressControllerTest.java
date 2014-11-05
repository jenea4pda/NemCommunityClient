package org.nem.ncc.controller;

import org.hamcrest.core.*;
import org.junit.*;
import org.nem.core.model.*;
import org.nem.core.utils.ExceptionUtils;
import org.nem.ncc.controller.requests.VanityAddressRequest;
import org.nem.ncc.controller.viewmodels.KeyPairViewModel;
import org.nem.ncc.test.ExceptionAssert;

import java.util.logging.Logger;

public class VanityAddressControllerTest {
	private static final Logger LOGGER = Logger.getLogger(VanityAddressControllerTest.class.getName());
	private static final byte MAIN_NETWORK_VERSION = NetworkInfo.getMainNetworkInfo().getVersion();

	@Test
	public void createVanityRealAccountDataReturnsKeyPairViewModelWithMainNetworkVersion() {
		// Arrange:
		final VanityAddressController controller = new VanityAddressController();
		final VanityAddressRequest request = new VanityAddressRequest("NEM", 1000);

		// Act:
		controller.startVanityRealAccountDataGeneration(request);
		ExceptionUtils.propagateVoid(() -> Thread.sleep(1000));
		final KeyPairViewModel viewModel = controller.statusVanityRealAccountDataGeneration();
		// TODO 20141105 J-J: wait for completion instead of using sleep!

		// Assert:
		Assert.assertThat(viewModel.getKeyPair(), IsNull.notNullValue());
		Assert.assertThat(viewModel.getKeyPair().getPrivateKey(), IsNull.notNullValue());
		Assert.assertThat(viewModel.getNetworkVersion(), IsEqual.equalTo(MAIN_NETWORK_VERSION));

		final Address address = Address.fromPublicKey(MAIN_NETWORK_VERSION, viewModel.getKeyPair().getPublicKey());
		final int patternIndex = address.toString().indexOf("NEM");
		LOGGER.info(String.format("%s generated (pattern index %d)", address, patternIndex));
		Assert.assertThat(patternIndex, IsNot.not(IsEqual.equalTo(-1)));
	}

	//region illegal state transitions

	@Test
	public void cannotStartVanityAddressGenerationWhenGenerationIsStarted() {
		// Arrange:
		final VanityAddressController controller = new VanityAddressController();
		final VanityAddressRequest request = new VanityAddressRequest("NEM", 1000);
		controller.startVanityRealAccountDataGeneration(request);

		// Act:
		ExceptionAssert.assertThrows(
				v -> controller.startVanityRealAccountDataGeneration(request),
				IllegalStateException.class);
	}

	// TODO 20141105 J-J: this might be undesirable!
	@Test
	public void cannotQueryVanityAddressGenerationWhenGenerationIsStopped() {
		// Arrange:
		final VanityAddressController controller = new VanityAddressController();

		// Act:
		ExceptionAssert.assertThrows(
				v -> controller.statusVanityRealAccountDataGeneration(),
				IllegalStateException.class);
	}

	@Test
	public void cannotStopVanityAddressGenerationWhenGenerationIsStopped() {
		// Arrange:
		final VanityAddressController controller = new VanityAddressController();

		// Act:
		ExceptionAssert.assertThrows(
				v -> controller.stopVanityRealAccountDataGeneration(),
				IllegalStateException.class);
	}

	//endregion
}