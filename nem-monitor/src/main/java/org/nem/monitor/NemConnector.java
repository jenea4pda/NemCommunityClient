package org.nem.monitor;

import org.nem.core.connect.client.*;
import org.nem.core.model.NemStatus;
import org.nem.core.model.ncc.NemRequestResult;
import org.nem.core.serialization.Deserializer;
import org.nem.core.utils.LockFile;
import org.nem.monitor.node.NemNodePolicy;
import org.nem.ncc.controller.viewmodels.ConfigurationViewModel;

import java.util.concurrent.*;
import java.util.logging.*;

/**
 * This connector can interact with both NIS and NCC because the APIs it uses are exposed by both.
 */
public class NemConnector {
	private final static Logger LOGGER = Logger.getLogger(NemConnector.class.getName());

	private final NemNodePolicy policy;
	private final DefaultAsyncNemConnector<String> connector;

	/**
	 * Creates a new nem connector.
	 *
	 * @param policy The nem node policy.
	 * @param connector The connector.
	 */
	public NemConnector(final NemNodePolicy policy, final DefaultAsyncNemConnector<String> connector) {
		this.policy = policy;
		this.connector = connector;
	}

	/**
	 * Checks if the connected process is running.
	 *
	 * @return The node status.
	 */
	public CompletableFuture<NemStatus> getStatus() {
		return this.getAsync(NisApiId.NIS_REST_STATUS.toString()).handle((d, e) -> {
			if (isRunning(e)) {
				return NemStatus.RUNNING;
			}

			if (null != e) {
				return this.isBooting() ? NemStatus.STARTING : NemStatus.STOPPED;
			}

			return this.nemStatusFromNemRequestResult(d);
		});
	}

	// TODO 20141108 J-T: test for this
	// TODO 20141108 J-T: since as far as i can tell, you're only using this to check if nis is local or remote,
	// > consider changing it to boolean isNisLocal(); if possible, i don't think the rest of the NOM code needs to depend
	// > on ncc types
	// TODO 20141109 T-J: That is correct.

	/**
	 * Request configuration from node.
	 *
	 * @return The node status.
	 */
	public ConfigurationViewModel getConfiguration() {
		ConfigurationViewModel result = null;
		CompletableFuture<Deserializer> future = this.getAsync("/configuration/get");
		try {
			if (future != null) {
				Deserializer deserializer = future.get();
				result = new ConfigurationViewModel(deserializer);
			}
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.log(Level.WARNING, String.format("get configuration (policy: %s", this.policy.toString()), e);
		}
		return result;
	}

	private boolean isBooting() {
		return LockFile.isLocked(this.policy.getLockFile());
	}

	private static boolean isRunning(final Throwable ex) {
		return (ex instanceof CompletionException && ex.getCause() instanceof NemNodeExpectedException);
	}

	private NemStatus nemStatusFromNemRequestResult(final Deserializer deserializer) {
		if (null == deserializer) {
			return NemStatus.RUNNING;
		}

		final NemRequestResult result = new NemRequestResult(deserializer);
		return NemStatus.fromValue(result.getCode());
	}

	/**
	 * Shuts down the connected process.
	 *
	 * @return The future.
	 */
	public CompletableFuture<Void> shutdown() {
		return this.getAsync(NisApiId.NIS_REST_SHUTDOWN.toString()).thenApply(d -> null);
	}

	private CompletableFuture<Deserializer> getAsync(final String apiUrl) {
		return this.connector.getAsync(this.policy.getEndpoint(), this.policy.mapToUrlPath(apiUrl), null);
	}
}
