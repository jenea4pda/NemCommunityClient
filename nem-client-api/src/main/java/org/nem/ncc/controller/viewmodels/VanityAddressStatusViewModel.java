package org.nem.ncc.controller.viewmodels;

import org.nem.core.serialization.*;

/**
 * A view model that contains information about the status of a vanity address generation operation.
 */
public class VanityAddressStatusViewModel implements SerializableEntity {
	private final KeyPairViewModel keyPairViewModel;
	private final boolean isRunning;
	private final int numAttempts;

	/**
	 * Creates a view model around.
	 *
	 * @param keyPairViewModel The key pair view model.
	 * @param isRunning true if the operation is still running.
	 * @param numAttempts The number of attempts.
	 */
	public VanityAddressStatusViewModel(
			final KeyPairViewModel keyPairViewModel,
			final boolean isRunning,
			final int numAttempts) {
		this.keyPairViewModel = keyPairViewModel;
		this.isRunning = isRunning;
		this.numAttempts = numAttempts;
	}

	/**
	 * Gets the key pair view model.
	 *
	 * @return The key pair view model.
	 */
	public KeyPairViewModel getKeyPairViewModel() {
		return this.keyPairViewModel;
	}

	/**
	 * Gets a value indicating whether or not the operation is still running.
	 *
	 * @return true if the operation is still running.
	 */
	public boolean isRunning() {
		return this.isRunning;
	}

	/**
	 * Gets the number of attempts.
	 *
	 * @return The number of attempts.
	 */
	public int getNumAttempts() {
		return this.numAttempts;
	}

	@Override
	public void serialize(final Serializer serializer) {
		serializer.writeObject("keyPair", this.keyPairViewModel);
		serializer.writeInt("isRunning", this.isRunning ? 1 : 0);
		serializer.writeInt("numAttempts", this.numAttempts);
	}
}
