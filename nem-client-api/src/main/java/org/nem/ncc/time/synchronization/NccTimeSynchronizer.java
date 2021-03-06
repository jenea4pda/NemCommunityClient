package org.nem.ncc.time.synchronization;

import org.nem.core.model.primitive.*;
import org.nem.core.time.TimeProvider;
import org.nem.core.time.synchronization.*;
import org.nem.ncc.connector.PrimaryNisConnector;
import org.nem.ncc.services.TimeSynchronizationServices;

/**
 * NCC time synchronization with its NIS.
 */
public class NccTimeSynchronizer implements TimeSynchronizer {

	private final TimeSynchronizationServices timeSynchronizationServices;
	private final TimeProvider timeProvider;
	private final PrimaryNisConnector nisConnector;

	/**
	 * Creates a new NCC time synchronizer.
	 *
	 * @param timeSynchronizationServices The time synchronization services.
	 * @param timeProvider The time provider.
	 * @param nisConnector The NIS connector.
	 */
	public NccTimeSynchronizer(
			final TimeSynchronizationServices timeSynchronizationServices,
			final TimeProvider timeProvider,
			final PrimaryNisConnector nisConnector) {
		this.timeSynchronizationServices = timeSynchronizationServices;
		this.timeProvider = timeProvider;
		this.nisConnector = nisConnector;
	}

	@Override
	public java.util.concurrent.CompletableFuture<java.lang.Void> synchronizeTime() {
		final NetworkTimeStamp sendTimeStamp = this.timeProvider.getNetworkTime();
		return this.nisConnector.forwardAsync(this.timeSynchronizationServices::getCommunicationTimeStampsAsync)
				.thenAccept(remoteTimeStamps -> {
					final NetworkTimeStamp receiveTimeStamp = this.timeProvider.getNetworkTime();
					final TimeSynchronizationSample sample = new TimeSynchronizationSample(
							null,
							new CommunicationTimeStamps(sendTimeStamp, receiveTimeStamp),
							remoteTimeStamps);
					final TimeOffset timeOffset = new TimeOffset(sample.getTimeOffsetToRemote());
					this.timeProvider.updateTimeOffset(timeOffset);
				});
	}
}
