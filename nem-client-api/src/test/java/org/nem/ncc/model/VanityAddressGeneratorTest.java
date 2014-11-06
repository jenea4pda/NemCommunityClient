package org.nem.ncc.model;

import org.hamcrest.core.*;
import org.junit.*;
import org.nem.core.crypto.KeyPair;
import org.nem.core.model.Address;
import org.nem.core.utils.ExceptionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class VanityAddressGeneratorTest {

	//region basic operations

	@Test
	public void generatorReturnsFirstKeyPairWhenThereAreNoMatches() {
		// Arrange:
		final List<String> encodedAddresses = Arrays.asList("BALO_0", "ALBO_0");
		final TestContext context = new TestContext(encodedAddresses);

		// Act:
		final VanityAddressGenerator.GenerateToken token = context.generate("ZZZZ", 2);

		// Assert:
		context.assertToken(token, "BALO_0", 2);
	}

	@Test
	public void generatorReturnsLongestMatchingString() {
		// Arrange:
		final List<String> encodedAddresses = Arrays.asList("ABOB_1", "ALOB_2", "ALPO_3", "ZZZZ_0");
		final TestContext context = new TestContext(encodedAddresses);

		// Act:
		final VanityAddressGenerator.GenerateToken token = context.generate("ALPHA", 4);

		// Assert:
		context.assertToken(token, "ALPO_3", 4);
	}

	@Test
	public void generatorOnlyLooksForSubstringsFromStartOfPattern() {
		// Arrange:
		final List<String> encodedAddresses = Arrays.asList("ALOB_2", "PHAA_3");
		final TestContext context = new TestContext(encodedAddresses);

		// Act:
		final VanityAddressGenerator.GenerateToken token = context.generate("ALPHA", 2);

		// Assert:
		context.assertToken(token, "ALOB_2", 2);
	}

	@Test
	public void generatorPrefersEarlierMatchesToLaterMatches() {
		// Arrange:
		final List<String> encodedAddresses = Arrays.asList("BALO_2", "ALBO_2", "BOAL_2");
		final TestContext context = new TestContext(encodedAddresses);

		// Act:
		final VanityAddressGenerator.GenerateToken token = context.generate("ALPHA", 3);

		// Assert:
		context.assertToken(token, "ALBO_2", 3);
	}

	@Test
	public void generatorShortCircuitsWhenFullMatchIsMade() {
		// Arrange:
		final List<String> encodedAddresses = Arrays.asList("BALO_2", "ALPHA_5", "BALN_2");
		final TestContext context = new TestContext(encodedAddresses);

		// Act:
		final VanityAddressGenerator.GenerateToken token = context.generate("ALPHA", 1000);

		// Assert:
		context.assertToken(token, "ALPHA_5", 2);
	}

	private static class TestContext {
		private final VanityAddressGenerator generator;
		private final Map<KeyPair, Address> keyPairAddressMap;

		public TestContext(final List<String> encodedAddresses) {
			final List<KeyPair> keys = encodedAddresses.stream()
					.map(encodedAddress -> KeyPair.random())
					.collect(Collectors.toList());

			this.keyPairAddressMap = new HashMap<>();
			for (int i = 0; i < encodedAddresses.size(); ++i) {
				this.keyPairAddressMap.put(keys.get(i), Address.fromEncoded(encodedAddresses.get(i)));
			}

			final int i[] = new int[] { 0 };
			this.generator = new VanityAddressGenerator(
					() -> keys.get(i[0]++),
					this.keyPairAddressMap::get);
		}

		private VanityAddressGenerator.GenerateToken generate(final String pattern, final int maxAttempts) {
			return this.generator.generateAsync(pattern, maxAttempts);
		}

		private void assertToken(
				final VanityAddressGenerator.GenerateToken token,
				final String expectedAddress,
				final int expectedAttempts) {
			// Act:
			token.getFuture().join();
			final String bestAddress = this.keyPairAddressMap.get(token.getBestKeyPair()).toString();

			// Assert:
			Assert.assertThat(bestAddress, IsEqual.equalTo(expectedAddress));
			Assert.assertThat(token.getNumAttempts(), IsEqual.equalTo(expectedAttempts));
		}
	}

	//endregion

	//region

	@Test
	public void vanityAddressGenerationIsAsync() {
		// Arrange:
		final VanityAddressGenerator generator = createRealGenerator();

		// Act:
		final VanityAddressGenerator.GenerateToken token = generator.generateAsync("NEMNEM", 100);

		// Assert:
		Assert.assertThat(token.getFuture().isDone(), IsEqual.equalTo(false));

		// Cleanup:
		token.getFuture().join();

		// Assert:
		Assert.assertThat(token.getFuture().isDone(), IsEqual.equalTo(true));
		Assert.assertThat(token.getNumAttempts(), IsEqual.equalTo(100));
		Assert.assertThat(token.getBestKeyPair(), IsNull.notNullValue());
	}

	@Test
	public void vanityAddressGenerationCanBeCancelled() {
		// Arrange:
		final VanityAddressGenerator generator = createRealGenerator();

		// Act:
		final VanityAddressGenerator.GenerateToken token = generator.generateAsync("NEMNEM", 1000);
		ExceptionUtils.propagateVoid(() -> Thread.sleep(10));
		token.getFuture().cancel(true);

		// Assert:
		Assert.assertThat(token.getFuture().isDone(), IsEqual.equalTo(true));
		Assert.assertThat(token.getNumAttempts(), IsNot.not(IsEqual.equalTo(0)));
		Assert.assertThat(token.getNumAttempts(), IsNot.not(IsEqual.equalTo(1000)));
		Assert.assertThat(token.getBestKeyPair(), IsNull.notNullValue());
	}

	private static VanityAddressGenerator createRealGenerator() {
		return new VanityAddressGenerator(
				KeyPair::new,
				kp -> Address.fromPublicKey(kp.getPublicKey()));
	}

	//endregion
}