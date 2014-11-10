package org.nem.ncc;

import org.nem.core.async.*;
import org.nem.core.time.TimeProvider;
import org.nem.ncc.time.synchronization.NccTimeSynchronizer;

import java.util.*;

/**
 * Scheduler that keeps track of all scheduled, recurring timers.
 */
public class NccScheduler implements AutoCloseable {
	private static final int ONE_SECOND = 1000;
	private static final int ONE_MINUTE = 60 * ONE_SECOND;

	private static final int TIME_SYNC_INITIAL_DELAY = 10 * ONE_SECOND;
	private static final int TIME_SYNC_INTERVAL = ONE_MINUTE;

	private final TimeProvider timeProvider;
	private final List<NemAsyncTimerVisitor> timerVisitors = new ArrayList<>();
	private final List<AsyncTimer> timers = new ArrayList<>();

	/**
	 * Creates a new scheduler.
	 *
	 * @param timeProvider The time provider.
	 */
	public NccScheduler(final TimeProvider timeProvider) {
		this.timeProvider = timeProvider;
	}

	/**
	 * Gets all timer visitors.
	 *
	 * @return All timer visitors.
	 */
	public List<NemAsyncTimerVisitor> getVisitors() {
		return this.timerVisitors;
	}

	/**
	 * Adds the NCC time synchronization task.
	 *
	 * @param nccTimeSynchronizer The time synchronizer.
	 */
	public void addTimeSynchronizationTask(final NccTimeSynchronizer nccTimeSynchronizer) {
		final AsyncTimerVisitor timerVisitor = this.createNamedVisitor("TIME SYNCHRONIZATION");
		// TODO 20141110 G-J: have I changed it correctly?
		final AsyncTimerOptions options = new AsyncTimerOptionsBuilder()
				.setRecurringFutureSupplier(
						nccTimeSynchronizer::synchronizeTime
				)
				.setInitialDelay(TIME_SYNC_INITIAL_DELAY)
				.setDelayStrategy(new UniformDelayStrategy(TIME_SYNC_INTERVAL))
				.setVisitor(timerVisitor)
				.create();
		this.timers.add(new AsyncTimer(options));
	}

	// TODO 20140928 J-B (minor) might want to add a test that close transitions all times to stopped
	@Override
	public void close() {
		this.timers.forEach(AsyncTimer::close);
	}

	private NemAsyncTimerVisitor createNamedVisitor(final String name) {
		final NemAsyncTimerVisitor timerVisitor = new NemAsyncTimerVisitor(name, this.timeProvider);
		this.timerVisitors.add(timerVisitor);
		return timerVisitor;
	}
}
