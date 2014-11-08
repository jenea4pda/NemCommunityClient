package org.nem.monitor.visitors;

import org.nem.core.model.NemStatus;
import org.nem.monitor.node.NemNodeType;

import java.util.function.Consumer;

// TODO 20141108: should definitely have tests; fix javadoc (broken record)

/**
 * Visitor that keeps node menu item text in sync with status changes.
 */
public class NodeStatusToPercentageAdapter implements NodeStatusVisitor {
	private final NemNodeType nodeType;
	private final Consumer<Integer> statusPercentageConsumer;

	/**
	 * Creates a new visitor.
	 *
	 * @param nodeType The node type being monitored.
	 * @param statusDescriptionConsumer The function to call when a description change is triggered.
	 */
	public NodeStatusToPercentageAdapter(final NemNodeType nodeType, final Consumer<Integer> statusPercentageConsumer) {
		this.nodeType = nodeType;
		this.statusPercentageConsumer = statusPercentageConsumer;
		this.notifyStatus(nodeType, NemStatus.UNKNOWN);
	}

	@Override
	public void notifyStatus(final NemNodeType type, final NemStatus status) {
		if (this.nodeType != type) {
			return;
		}

		this.statusPercentageConsumer.accept(calculatePercentage(type, status));
	}

	private Integer calculatePercentage(final NemNodeType type, final NemStatus status) {
		return type == NemNodeType.NIS ? calculateNisPercentage(status) : calculateNccPercentage(status);
	}

	private Integer calculateNisPercentage(final NemStatus status) {
		// TODO 20141108: switch is a bit verbose; consider using a Map instead
		Integer percentage = 0;
		switch (status) {
			case SYNCHRONIZED:
				percentage = 100;
				break;

			case BOOTED:
				percentage = 95;
				break;

			case RUNNING:
				percentage = 90;
				break;

			case STOPPED:
				percentage = 0;
				break;

			case STARTING:
				percentage = 45;
				break;
			default:
				percentage = 0;
		}
		return percentage;
	}

	private Integer calculateNccPercentage(final NemStatus status) {
		Integer percentage = 0;
		switch (status) {
			case RUNNING:
				percentage = 100;
				break;
			case STOPPED:
				percentage = 0;
				break;
			case STARTING:
				percentage = 50;
				break;
			default:
				percentage = 0;
		}
		return percentage;
	}
}
