package org.nem.ncc.controller;

import org.hamcrest.core.*;
import org.junit.*;
import org.nem.core.model.*;
import org.nem.core.utils.ExceptionUtils;
import org.nem.ncc.controller.requests.VanityAddressRequest;
import org.nem.ncc.controller.viewmodels.*;
import org.nem.ncc.test.ExceptionAssert;

import java.util.logging.Logger;

public class VanityAddressControllerTest {
	private static final Logger LOGGER = Logger.getLogger(VanityAddressControllerTest.class.getName());
	private static final byte MAIN_NETWORK_VERSION = NetworkInfo.getMainNetworkInfo().getVersion();
	private static final int MAX_ATTEMPTS = 10000;

	//region basic operations

	@Test
	public void statusIsAvailableImmediatelyAfterStart() {
		// Arrange:
		final VanityAddressController controller = new VanityAddressController();
		final VanityAddressRequest request = new VanityAddressRequest("NEMNEM", MAX_ATTEMPTS);

		// Act:
		controller.start(request);
		final VanityAddressStatusViewModel viewModel = controller.status();

		// Assert:
		assertRunning(viewModel);
		assertNonPatternMatchedMainNetKeyPair(viewModel.getKeyPairViewModel(), "NEMNEM");
	}

	@Test
	public void vanityAddressIsReturnedOnCompletion() {
		// Arrange:
		final VanityAddressController controller = new VanityAddressController();
		final VanityAddressRequest request = new VanityAddressRequest("NEM", MAX_ATTEMPTS);

		// Act:
		controller.start(request);
		final VanityAddressStatusViewModel viewModel = waitUntilStopped(controller);

		// Assert:
		assertStopped(viewModel);
		assertPatternMatchedMainNetKeyPair(viewModel.getKeyPairViewModel(), "NEM");
	}

	@Test
	public void stopImmediatelyStopsAddressGeneration() {
		// Arrange:
		final VanityAddressController controller = new VanityAddressController();
		final VanityAddressRequest request = new VanityAddressRequest("NEMNEM", MAX_ATTEMPTS);

		// Act:
		controller.start(request);
		controller.stop();
		final VanityAddressStatusViewModel viewModel = controller.status();

		// Assert:
		assertStopped(viewModel);
		assertNonPatternMatchedMainNetKeyPair(viewModel.getKeyPairViewModel(), "NEMNEM");
	}

	@Test
	public void canRestartAfterStop() {
		// Arrange:
		final VanityAddressController controller = new VanityAddressController();
		final VanityAddressRequest request = new VanityAddressRequest("NEM", MAX_ATTEMPTS);

		// Act:
		controller.start(request);
		controller.stop();
		controller.start(request);
		final VanityAddressStatusViewModel viewModel = waitUntilStopped(controller);

		// Assert:
		assertStopped(viewModel);
		assertPatternMatchedMainNetKeyPair(viewModel.getKeyPairViewModel(), "NEM");
	}

	//region utils

	private static VanityAddressStatusViewModel waitUntilStopped(final VanityAddressController controller) {
		for (int i = 0; i < 100; ++i) {
			final VanityAddressStatusViewModel viewModel = controller.status();
			if (!viewModel.isRunning()) {
				return viewModel;
			}

			ExceptionUtils.propagateVoid(() -> Thread.sleep(10));
		}

		throw new IllegalStateException("generation never stopped");
	}

	private static void assertRunning(final VanityAddressStatusViewModel viewModel) {
		Assert.assertThat(viewModel.isRunning(), IsEqual.equalTo(true));
		assertNumAttemptsAtLeastOneLessThanMax(viewModel.getNumAttempts());
	}

	private static void assertStopped(final VanityAddressStatusViewModel viewModel) {
		Assert.assertThat(viewModel.isRunning(), IsEqual.equalTo(false));
		assertNumAttemptsAtLeastOneLessThanMax(viewModel.getNumAttempts());
	}

	private static void assertNumAttemptsAtLeastOneLessThanMax(final int numAttempts) {
		LOGGER.info(String.format("%d number of attempts", numAttempts));
		Assert.assertThat(numAttempts, IsNot.not(IsEqual.equalTo(0)));
		Assert.assertThat(numAttempts, IsNot.not(IsEqual.equalTo(MAX_ATTEMPTS)));
	}

	private static void assertPatternMatchedMainNetKeyPair(final KeyPairViewModel viewModel, final String pattern) {
		Assert.assertThat(getPatternIndex(viewModel, pattern), IsNot.not(IsEqual.equalTo(-1)));
	}

	private static void assertNonPatternMatchedMainNetKeyPair(final KeyPairViewModel viewModel, final String pattern) {
		Assert.assertThat(getPatternIndex(viewModel, pattern), IsEqual.equalTo(-1));
	}

	private static int getPatternIndex(final KeyPairViewModel viewModel, final String pattern) {
		Assert.assertThat(viewModel.getKeyPair(), IsNull.notNullValue());
		Assert.assertThat(viewModel.getKeyPair().getPrivateKey(), IsNull.notNullValue());
		Assert.assertThat(viewModel.getNetworkVersion(), IsEqual.equalTo(MAIN_NETWORK_VERSION));

		final Address address = Address.fromPublicKey(MAIN_NETWORK_VERSION, viewModel.getKeyPair().getPublicKey());
		final int patternIndex = address.toString().indexOf(pattern);
		LOGGER.info(String.format("%s generated (pattern index %d)", address, patternIndex));
		return patternIndex;
	}

	//endregion

	//endregion

	//region illegal state transitions

	@Test
	public void startCannotBeCalledWhenOperationIsInProgress() {
		// Arrange:
		final VanityAddressController controller = new VanityAddressController();
		final VanityAddressRequest request = new VanityAddressRequest("NEM", MAX_ATTEMPTS);
		controller.start(request);

		// Act:
		ExceptionAssert.assertThrows(
				v -> controller.start(request),
				IllegalStateException.class);
	}

	@Test
	public void statusCannotBeCalledIfStartWasNeverCalled() {
		// Arrange:
		final VanityAddressController controller = new VanityAddressController();

		// Act:
		ExceptionAssert.assertThrows(
				v -> controller.status(),
				IllegalStateException.class);
	}

	@Test
	public void stopCannotBeCalledWhenNoOperationIsInProgress() {
		// Arrange:
		final VanityAddressController controller = new VanityAddressController();

		// Act:
		ExceptionAssert.assertThrows(
				v -> controller.stop(),
				IllegalStateException.class);
	}

	//endregion
}