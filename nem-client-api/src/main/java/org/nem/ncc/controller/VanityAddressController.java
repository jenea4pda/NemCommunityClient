package org.nem.ncc.controller;

import org.nem.core.crypto.KeyPair;
import org.nem.core.model.*;
import org.nem.ncc.controller.requests.VanityAddressRequest;
import org.nem.ncc.controller.viewmodels.KeyPairViewModel;
import org.nem.ncc.model.VanityAddressGenerator;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controller for generating vanity addresses.
 */
public class VanityAddressController {
	// TODO 20141105 - this can get removed post-BETA
	private static final byte NETWORK_VERSION = NetworkInfo.getMainNetworkInfo().getVersion();
	final VanityAddressGenerator generator = new VanityAddressGenerator(
			KeyPair::new,
			kp -> Address.fromPublicKey(NETWORK_VERSION, kp.getPublicKey()));

	private final AtomicBoolean isRunning = new AtomicBoolean(false);
	private VanityAddressGenerator.GenerateToken generateToken;

	/**
	 * Creates a real (main-net) vanity private key.
	 *
	 * @param request The vanity request.
	 */
	@RequestMapping(value = "/account/vanity/start", method = RequestMethod.POST)
	public void startVanityRealAccountDataGeneration(final VanityAddressRequest request) {
		if (!this.isRunning.compareAndSet(false, true)) {
			throw new IllegalStateException("already generating a vanity address");
		}

		this.generateToken = this.generator.generateAsync(request.getPattern(), request.getMaxAttempts());
	}

	/**
	 * Gets status about the vanity address creation processes.
	 *
	 * @return The key pair view model corresponding to the best vanity address.
	 */
	@RequestMapping(value = "/account/vanity/stop", method = RequestMethod.POST)
	public KeyPairViewModel statusVanityRealAccountDataGeneration() {
		if (!this.isRunning.compareAndSet(true, true)) {
			throw new IllegalStateException("not generating a vanity address");
		}

		// TODO: actually return status (e.g. number of iterations; need a different view model) !!!
		// > probably should poll in start to ensure this is not null!
		return new KeyPairViewModel(this.generateToken.getBestKeyPair(), NETWORK_VERSION);
	}

	/**
	 * Stops creating a real (main-net) vanity private key.
	 */
	@RequestMapping(value = "/account/vanity/stop", method = RequestMethod.POST)
	public void stopVanityRealAccountDataGeneration() {
		if (!this.isRunning.compareAndSet(true, false)) {
			throw new IllegalStateException("not generating a vanity address");
		}

		this.generateToken.getFuture().cancel(true);
	}
}
