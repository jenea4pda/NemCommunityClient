package org.nem.ncc.controller.requests;

import net.minidev.json.JSONObject;
import org.hamcrest.core.IsEqual;
import org.junit.*;
import org.nem.core.serialization.*;
import org.nem.ncc.test.*;

import java.util.*;
import java.util.function.Consumer;

public class VanityAddressRequestTest {

	@Test
	public void requestCanBeCreated() {
		// Act:
		final VanityAddressRequest request = new VanityAddressRequest("NEM", 123);

		// Assert:
		Assert.assertThat(request.getPattern(), IsEqual.equalTo("NEM"));
		Assert.assertThat(request.getMaxAttempts(), IsEqual.equalTo(123));
	}

	@Test
	public void requestCannotBeCreatedWithMaxAttemptsLessThanOne() {
		// Assert:
		for (final int maxAttempts : Arrays.asList(-1, 0)) {
			ExceptionAssert.assertThrows(
					v -> new VanityAddressRequest("NEM", maxAttempts),
					IllegalArgumentException.class);
		}
	}

	@Test
	public void requestCanBeDeserialized() {
		// Act:
		final VanityAddressRequest request = this.createRequestFromJson("NEM", 123);

		// Assert:
		Assert.assertThat(request.getPattern(), IsEqual.equalTo("NEM"));
		Assert.assertThat(request.getMaxAttempts(), IsEqual.equalTo(123));
	}

	@Test
	public void requestCannotBeDeserializedWithMissingParameters() {
		// Arrange:
		final List<Consumer<Void>> actions = Arrays.asList(
				v -> this.createRequestFromJson("NEM", null),
				v -> this.createRequestFromJson(null, 123));

		// Assert:
		for (final Consumer<Void> action : actions) {
			ExceptionAssert.assertThrows(
					v -> action.accept(null),
					SerializationException.class);
		}
	}

	@Test
	public void requestCannotBeDeserializedWithMaxAttemptsLessThanOne() {
		// Assert:
		for (final int maxAttempts : Arrays.asList(-1, 0)) {
			ExceptionAssert.assertThrows(
					v -> this.createRequestFromJson("NEM", maxAttempts),
					IllegalArgumentException.class);
		}
	}

	private VanityAddressRequest createRequestFromJson(
			final String pattern,
			final Integer maxAttempts) {
		final JSONObject jsonObject = new JSONObject();
		jsonObject.put("pattern", pattern);
		jsonObject.put("max_attempts", maxAttempts);
		return new VanityAddressRequest(new JsonDeserializer(jsonObject, null));
	}
}