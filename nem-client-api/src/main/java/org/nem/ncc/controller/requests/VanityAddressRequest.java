package org.nem.ncc.controller.requests;

import org.nem.core.serialization.Deserializer;

/**
 * A request containing information necessary to generate a vanity address.
 */
public class VanityAddressRequest {
	private final String pattern;
	private final int maxAttempts;

	/**
	 * Creates a vanity address request.
	 *
	 * @param pattern The desired address pattern.
	 * @param maxAttempts The maximum number of attempts.
	 */
	public VanityAddressRequest(final String pattern, final int maxAttempts) {
		this.pattern = pattern;
		this.maxAttempts = maxAttempts;
		this.validate();
	}

	/**
	 * Deserializes a vanity address request.
	 *
	 * @param deserializer The deserializer.
	 */
	public VanityAddressRequest(final Deserializer deserializer) {
		this.pattern = deserializer.readString("pattern");
		this.maxAttempts = deserializer.readInt("max_attempts");
		this.validate();
	}

	private void validate() {
		if (this.maxAttempts <= 0) {
			throw new IllegalArgumentException("max attempts must be greater than zero");
		}
	}

	/**
	 * Gets the pattern.
	 *
	 * @return The pattern.
	 */
	public String getPattern() {
		return this.pattern;
	}

	/**
	 * Gets the maximum number of attempts.
	 *
	 * @return The maximum number of attempts.
	 */
	public int getMaxAttempts() {
		return this.maxAttempts;
	}
}

