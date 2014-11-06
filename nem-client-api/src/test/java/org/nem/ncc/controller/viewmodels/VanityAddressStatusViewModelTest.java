package org.nem.ncc.controller.viewmodels;

import net.minidev.json.JSONObject;
import org.hamcrest.core.IsEqual;
import org.junit.*;
import org.nem.core.crypto.KeyPair;
import org.nem.core.serialization.JsonSerializer;

public class VanityAddressStatusViewModelTest {

	@Test
	public void canCreateViewModel() {
		// Arrange:
		final KeyPairViewModel keyPairViewModel = new KeyPairViewModel(new KeyPair(), (byte)7);

		// Act:
		final VanityAddressStatusViewModel viewModel = new VanityAddressStatusViewModel(keyPairViewModel, true, 17);

		// Assert:
		Assert.assertThat(viewModel.getKeyPairViewModel(), IsEqual.equalTo(keyPairViewModel));
		Assert.assertThat(viewModel.isRunning(), IsEqual.equalTo(true));
		Assert.assertThat(viewModel.getNumAttempts(), IsEqual.equalTo(17));
	}

	@Test
	public void canSerializeRunningViewModel() {
		// Assert:
		assertCanSerializeViewModel(true, 1);
	}

	@Test
	public void canSerializeStoppedViewModel() {
		// Assert:
		assertCanSerializeViewModel(false, 0);
	}

	private static void assertCanSerializeViewModel(final boolean isRunning, final int serializedIsRunning) {
		// Arrange:
		final KeyPairViewModel keyPairViewModel = new KeyPairViewModel(new KeyPair(), (byte)7);
		final VanityAddressStatusViewModel viewModel = new VanityAddressStatusViewModel(keyPairViewModel, isRunning, 17);

		// Act:
		final JSONObject jsonObject = JsonSerializer.serializeToJson(viewModel);

		// Assert:
		final Object serializedPrivateKey = ((JSONObject)jsonObject.get("keyPair")).get("privateKey");
		Assert.assertThat(serializedPrivateKey, IsEqual.equalTo(keyPairViewModel.getKeyPair().getPrivateKey().toString()));
		Assert.assertThat(jsonObject.get("isRunning"), IsEqual.equalTo(serializedIsRunning));
		Assert.assertThat(jsonObject.get("numAttempts"), IsEqual.equalTo(17));
	}
}