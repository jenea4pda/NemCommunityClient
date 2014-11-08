package org.nem.monitor.visitors;

import org.nem.core.model.NemStatus;
import org.nem.monitor.config.LanguageSupport;
import org.nem.monitor.node.NemNodeType;

import java.util.*;
import java.util.function.Consumer;

/**
 * NodeStatusVisitor that maps status changes to appropriate icons.
 */
public class NodeStatusToIconDescriptorAdapter implements NodeStatusVisitor {
	private static final Map<NemStatus, String> nemStatusToStateNameMap = new HashMap<NemStatus, String>() {
		{
			this.put(NemStatus.STOPPED, "stopped");
			this.put(NemStatus.STARTING, "starting");
			this.put(NemStatus.RUNNING, "running");
			this.put(NemStatus.BOOTED, "booted");
			this.put(NemStatus.SYNCHRONIZED, "synchronized");
		}
	};

	private final Consumer<IconDescriptor> iconDescriptorConsumer;
	private NemStatus nccStatus = clampNemStatus(NemStatus.UNKNOWN);
	private NemStatus nisStatus = clampNemStatus(NemStatus.UNKNOWN);

	/**
	 * Creates a new visitor.
	 *
	 * @param iconDescriptorConsumer The function to call when an icon change is triggered.
	 */
	public NodeStatusToIconDescriptorAdapter(final Consumer<IconDescriptor> iconDescriptorConsumer) {
		this.iconDescriptorConsumer = iconDescriptorConsumer;

		// TODO 20141108: why did you comment this out?
//		this.notifyStatus(NemNodeType.NIS, NemStatus.UNKNOWN);
	}

	@Override
	public void notifyStatus(final NemNodeType type, final NemStatus status) {
		switch (type) {
			case NCC:
				this.nccStatus = clampNccStatus(clampNemStatus(status));
				break;

			case NIS:
				this.nisStatus = clampNemStatus(status);
				break;
		}

		this.iconDescriptorConsumer.accept(this.getDescriptor());
	}

	private static NemStatus clampNemStatus(final NemStatus status) {
		switch (status) {
			case UNKNOWN:
				return NemStatus.STOPPED;

			case BOOTING:
				return NemStatus.BOOTED;

			default:
				return status;
		}
	}

	private static NemStatus clampNccStatus(final NemStatus status) {
		switch (status) {
			case BOOTED:
			case SYNCHRONIZED:
				return NemStatus.RUNNING;

			default:
				return status;
		}
	}

	private IconDescriptor getDescriptor() {
		final String imageName = getImageName(this.nccStatus, this.nisStatus);
		final String description = getDescription(
				String.format("status.nis.is.%s", nemStatusToStateNameMap.get(this.nisStatus)),
				String.format("status.ncc.is.%s", nemStatusToStateNameMap.get(this.nccStatus)));

		return new IconDescriptor(imageName, description);
	}

	private static String getImageName(final NemStatus nccStatus, final NemStatus nisStatus) {
		return String.format("icon_%d%d.png", getValue(nccStatus), getValue(nisStatus));
	}

	private static int getValue(final NemStatus status) {
		// this is a workaround so that the original image names can still be used
		// (before additional statuses were added)
		switch (status) {
			case BOOTED:
			case SYNCHRONIZED:
				return status.getValue() - 1;

			default:
				return status.getValue();
		}
	}

	private static String getDescription(final String msg1, final String msg2) {
		return String.format("%s, %s", LanguageSupport.message(msg1), LanguageSupport.message(msg2));
	}
}
