package org.nem.ncc.model;

import org.nem.core.crypto.KeyPair;
import org.nem.core.model.Address;

import java.util.concurrent.*;
import java.util.function.*;

/**
 * Helper class for generating vanity addresses.
 */
public class VanityAddressGenerator {
	private final Supplier<KeyPair> generateKeyPair;
	private final Function<KeyPair, Address> getAddressFromKeyPair;

	/**
	 * Creates a vanity generator.
	 *
	 * @param generateKeyPair The function that should be called to generate a new key pair.
	 * @param getAddressFromKeyPair The function that should be called to map a key pair to an address.
	 */
	public VanityAddressGenerator(
			final Supplier<KeyPair> generateKeyPair,
			final Function<KeyPair, Address> getAddressFromKeyPair) {
		this.generateKeyPair = generateKeyPair;
		this.getAddressFromKeyPair = getAddressFromKeyPair;
	}

	/**
	 * Starts generating a vanity address asynchronously.
	 *
	 * @param pattern The desired vanity pattern.
	 * @param maxAttempts The maximum number of generation attempts.
	 * @return The token for the generate operation.
	 */
	public GenerateToken generateAsync(final String pattern, final int maxAttempts, final Consumer<GenerateToken> matchConsumer) {
		final VanityAddressSelector selector = new VanityAddressSelector(pattern);
		return new GenerateToken(selector, maxAttempts, matchConsumer);
	}

	/**
	 * The token returned by generateAsync.
	 */
	public class GenerateToken {
		private final VanityAddressSelector selector;
		private final FutureTask<Void> future;
		private int numGenerations;

		private GenerateToken(final VanityAddressSelector selector, final int maxAttempts, final Consumer<GenerateToken> matchConsumer) {
			this.selector = selector;
			this.future = new FutureTask<Void>(() -> {
				for (int i = 0; i < maxAttempts && !Thread.currentThread().isInterrupted(); ++i) {
					final KeyPair keyPair = this.generateKeyPair();
					final Address address = VanityAddressGenerator.this.getAddressFromKeyPair.apply(keyPair);
					if(selector.addCandidate(keyPair, address)) {
						matchConsumer.accept(this);
					}
				}
				
				return null;
			});
			
			CompletableFuture.runAsync(future);
		}

		private KeyPair generateKeyPair() {
			++this.numGenerations;
			return VanityAddressGenerator.this.generateKeyPair.get();
		}

		/**
		 * Gets the future associated with this token.
		 *
		 * @return The future.
		 */
		public FutureTask<Void> getFuture() {
			return this.future;
		}

		/**
		 * Gets the number of attempts.
		 *
		 * @return The number of attempts.
		 */
		public int getNumAttempts() {
			return this.numGenerations;
		}

		/**
		 * Gets the best key pair.
		 *
		 * @return The best key pair.
		 */
		public KeyPair getBestKeyPair() {
			return this.selector.getBestKeyPair();
		}
	}

	private static class VanityAddressSelector {
		private final String pattern;
		private KeyPair bestKeyPair;

		public VanityAddressSelector(final String pattern) {
			this.pattern = pattern;
		}

		public KeyPair getBestKeyPair() {
			return this.bestKeyPair;
		}

		public boolean addCandidate(final KeyPair keyPair, final Address address) {
			final String encodedAddress = address.toString();
			
			if(!encodedAddress.contains(pattern)) {
				return false;
			}

			this.bestKeyPair = keyPair;

			return true;
		}
	}
}
