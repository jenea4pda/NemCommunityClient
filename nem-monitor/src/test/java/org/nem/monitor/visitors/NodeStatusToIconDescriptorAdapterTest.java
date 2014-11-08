package org.nem.monitor.visitors;

import org.hamcrest.core.IsEqual;
import org.junit.*;
import org.mockito.*;

import org.nem.core.i18n.LanguageSupport;
import org.nem.core.model.NemStatus;
import org.nem.monitor.node.NemNodeType;

import java.util.*;
import java.util.function.Consumer;

public class NodeStatusToIconDescriptorAdapterTest {
	private static final IconDescriptor ICON_11_DESCRIPTOR =
			createDescriptor("icon_11.png", "status.nis.is.stopped", "status.ncc.is.stopped");
	private static final IconDescriptor ICON_12_DESCRIPTOR =
			createDescriptor("icon_12.png", "status.nis.is.starting", "status.ncc.is.stopped");
	private static final IconDescriptor ICON_13_DESCRIPTOR =
			createDescriptor("icon_13.png", "status.nis.is.running", "status.ncc.is.stopped");
	private static final IconDescriptor ICON_14_DESCRIPTOR =
			createDescriptor("icon_14.png", "status.nis.is.booted", "status.ncc.is.stopped");
	private static final IconDescriptor ICON_15_DESCRIPTOR =
			createDescriptor("icon_15.png", "status.nis.is.synchronized", "status.ncc.is.stopped");
	private static final IconDescriptor ICON_21_DESCRIPTOR =
			createDescriptor("icon_21.png", "status.nis.is.stopped", "status.ncc.is.starting");
	private static final IconDescriptor ICON_22_DESCRIPTOR =
			createDescriptor("icon_22.png", "status.nis.is.starting", "status.ncc.is.starting");
	private static final IconDescriptor ICON_23_DESCRIPTOR =
			createDescriptor("icon_23.png", "status.nis.is.running", "status.ncc.is.starting");
	private static final IconDescriptor ICON_24_DESCRIPTOR =
			createDescriptor("icon_24.png", "status.nis.is.booted", "status.ncc.is.starting");
	private static final IconDescriptor ICON_25_DESCRIPTOR =
			createDescriptor("icon_25.png", "status.nis.is.synchronized", "status.ncc.is.starting");
	private static final IconDescriptor ICON_31_DESCRIPTOR =
			createDescriptor("icon_31.png", "status.nis.is.stopped", "status.ncc.is.running");
	private static final IconDescriptor ICON_32_DESCRIPTOR =
			createDescriptor("icon_32.png", "status.nis.is.starting", "status.ncc.is.running");
	private static final IconDescriptor ICON_33_DESCRIPTOR =
			createDescriptor("icon_33.png", "status.nis.is.running", "status.ncc.is.running");
	private static final IconDescriptor ICON_34_DESCRIPTOR =
			createDescriptor("icon_34.png", "status.nis.is.booted", "status.ncc.is.running");
	private static final IconDescriptor ICON_35_DESCRIPTOR =
			createDescriptor("icon_35.png", "status.nis.is.synchronized", "status.ncc.is.running");

	@Test
	public void initiallyStoppedIconIsChosen() {
		// Arrange:
		final TestContext context = new TestContext();

		// Assert:
		Assert.assertThat(context.getLastDescriptor(), IsEqual.equalTo(ICON_11_DESCRIPTOR));
	}

	@Test
	public void allGoodIconIsReturnedWhenNccIsRunningAndNisIsSynchronized() {
		// Arrange:
		final Map<StatusPair, IconDescriptor> statusToDescriptorMap = new HashMap<StatusPair, IconDescriptor>() {
			{ this.put(new StatusPair(NemStatus.RUNNING, NemStatus.SYNCHRONIZED), ICON_35_DESCRIPTOR); }
		};

		// Assert:
		assertDescriptorMappingForPairs(statusToDescriptorMap);
	}

	@Test
	public void allStateCombinationsAreSupported() {
		// Arrange:
		final Map<StatusPair, IconDescriptor> statusToDescriptorMap = new HashMap<>();
		for (final NemStatus status : new NemStatus[] { NemStatus.UNKNOWN, NemStatus.STOPPED }) {
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.UNKNOWN), ICON_11_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.STOPPED), ICON_11_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.STARTING), ICON_12_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.RUNNING), ICON_13_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.BOOTING), ICON_14_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.BOOTED), ICON_14_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.SYNCHRONIZED), ICON_15_DESCRIPTOR);
		}

		for (final NemStatus status : new NemStatus[] { NemStatus.STARTING }) {
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.UNKNOWN), ICON_21_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.STOPPED), ICON_21_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.STARTING), ICON_22_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.RUNNING), ICON_23_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.BOOTING), ICON_24_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.BOOTED), ICON_24_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.SYNCHRONIZED), ICON_25_DESCRIPTOR);
		}

		for (final NemStatus status : new NemStatus[] { NemStatus.RUNNING, NemStatus.BOOTING, NemStatus.BOOTED, NemStatus.SYNCHRONIZED }) {
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.UNKNOWN), ICON_31_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.STOPPED), ICON_31_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.STARTING), ICON_32_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.RUNNING), ICON_33_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.BOOTING), ICON_34_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.BOOTED), ICON_34_DESCRIPTOR);
			statusToDescriptorMap.put(new StatusPair(status, NemStatus.SYNCHRONIZED), ICON_35_DESCRIPTOR);
		}

		// Assert:
		assertDescriptorMappingForPairs(statusToDescriptorMap);
		Assert.assertThat(statusToDescriptorMap.size(), IsEqual.equalTo(NemStatus.values().length * NemStatus.values().length));
	}

	private static void assertDescriptorMappingForPairs(final Map<StatusPair, IconDescriptor> statusToDescriptorMap) {
		// Arrange:
		final TestContext context = new TestContext();

		for (final Map.Entry<StatusPair, IconDescriptor> entry : statusToDescriptorMap.entrySet()) {
			// Act:
			context.visitor.notifyStatus(NemNodeType.NCC, entry.getKey().nccStatus);
			context.visitor.notifyStatus(NemNodeType.NIS, entry.getKey().nisStatus);

			// Assert:
			System.out.println(String.format(
					"Expecting %s for (%s, %s)",
					entry.getValue().getImageFileName(),
					entry.getKey().nccStatus,
					entry.getKey().nisStatus));
			Assert.assertThat(context.getLastDescriptor(), IsEqual.equalTo(entry.getValue()));
		}
	}

	private static IconDescriptor createDescriptor(final String imageName, final String nisMessage, final String nccMessage) {
		return new IconDescriptor(imageName, String.format("%s, %s", LanguageSupport.message(nisMessage), LanguageSupport.message(nccMessage)));
	}

	private static class StatusPair {
		private final NemStatus nccStatus;
		private final NemStatus nisStatus;

		private StatusPair(final NemStatus nccStatus, final NemStatus nisStatus) {
			this.nccStatus = nccStatus;
			this.nisStatus = nisStatus;
		}
	}

	private static class TestContext {
		@SuppressWarnings("unchecked")
		private final Consumer<IconDescriptor> imageDescriptorConsumer = Mockito.mock(Consumer.class);
		private final NodeStatusVisitor visitor = new NodeStatusToIconDescriptorAdapter(this.imageDescriptorConsumer);

		private IconDescriptor getLastDescriptor() {
			final ArgumentCaptor<IconDescriptor> descriptorCaptor = ArgumentCaptor.forClass(IconDescriptor.class);
			Mockito.verify(this.imageDescriptorConsumer, Mockito.atLeastOnce()).accept(descriptorCaptor.capture());
			return descriptorCaptor.getValue();
		}
	}
}