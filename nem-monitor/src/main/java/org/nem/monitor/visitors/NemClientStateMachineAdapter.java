package org.nem.monitor.visitors;

import org.nem.core.model.NemStatus;
import org.nem.core.node.NodeEndpoint;
import org.nem.monitor.node.NemNodeType;
import org.nem.ncc.controller.viewmodels.ConfigurationViewModel;

import java.util.function.*;

import com.github.oxo42.stateless4j.*;

// TODO 20141108 J-T: i like this, but since it is pretty critical, it really needs to be tested thoroughly
// TODO 20141108 J-T: is there any benefit on having this state machine vs two; one for nis and one for ncc
// > i guess in launching the browser?
// TODO 20141109 T-J: Yes, I want to have the option to set both states into correlation to perform an action.

/**
 * Visitor that collects node status changes to derive next steps for starting nodes.
 */
public class NemClientStateMachineAdapter implements NodeStatusVisitor {
	final StateMachineConfig<State, NemStatus> nemClientNcc;
	final StateMachineConfig<State, NemStatus> nemClientNis;

	final StateMachine<State, NemStatus> nccStateMachine;
	final StateMachine<State, NemStatus> nisStateMachine;
	Consumer<String> startNcc;
	Consumer<String> startBrowser;
	Supplier<ConfigurationViewModel> configNcc;
	Consumer<String> startNis;
	Consumer<Boolean> localNisInfoConsumer;

	/**
	 * Creates a new visitor.
	 *
	 * @param nodeType The node type being monitored.
	 * @param statusDescriptionConsumer The function to call when a description change is triggered.
	 */
	public NemClientStateMachineAdapter() {
		this.nemClientNcc = new StateMachineConfig<>();
		this.nemClientNis = new StateMachineConfig<>();

		initializeStateMachine();

		this.nccStateMachine = new StateMachine<NemClientStateMachineAdapter.State, NemStatus>(State.Ncc_Unknown, this.nemClientNcc);
		this.nisStateMachine = new StateMachine<NemClientStateMachineAdapter.State, NemStatus>(State.Nis_Unknown, this.nemClientNis);
	}

	// TODO 20141108 J-T: any reason you can't pass these to the constructor (maybe in an options class that you use a builder to create)
	// TODO 20141109 T-J: Not actually a reason, passing as constructure wasn't readable anymore, but encapsulating in it options 
	// > would be possible

	public void setStartNccConfigurationSupplier(final Supplier<ConfigurationViewModel> configNcc) {
		this.configNcc = configNcc;
	}

	public void setStartNisEventConsumer(final Consumer<String> startNis) {
		this.startNis = startNis;
	}

	public void setStartNccEventConsumer(final Consumer<String> startNcc) {
		this.startNcc = startNcc;
	}

	public void setStartBrowserEventConsumer(final Consumer<String> startBrowser) {
		this.startBrowser = startBrowser;
	}

	public void setLocalNisConfiguredEventConsumer(final Consumer<Boolean> localNisInfoConsumer) {
		this.localNisInfoConsumer = localNisInfoConsumer;
	}

	protected void initializeStateMachine() {

		// TODO 20141108: at least have two initialize functions; one for ncc and one for nis

		// Ncc State Machine
		nemClientNcc.configure(State.Ncc_Unknown).permit(NemStatus.STOPPED, State.Ncc_Stopped_A);
		nemClientNcc.configure(State.Ncc_Unknown).permit(NemStatus.STARTING, State.Ncc_Starting_B);
		nemClientNcc.configure(State.Ncc_Unknown).permit(NemStatus.RUNNING, State.Ncc_Running_D);

		nemClientNcc.configure(State.Ncc_Stopped_A).permit(NemStatus.STARTING, State.Ncc_Spawned_E);
		nemClientNcc.configure(State.Ncc_Stopped_A).permit(NemStatus.RUNNING, State.Ncc_Running_D);
		nemClientNcc.configure(State.Ncc_Stopped_A).onEntryFrom(NemStatus.STOPPED, () -> startNcc.accept(null));

		nemClientNcc.configure(State.Ncc_Starting_B).permit(NemStatus.RUNNING, State.Ncc_Running_D);
		nemClientNcc.configure(State.Ncc_Starting_B).permit(NemStatus.STOPPED, State.Ncc_Manual_C);

		nemClientNcc.configure(State.Ncc_Running_D).permit(NemStatus.STOPPED, State.Ncc_Manual_C);
		nemClientNcc.configure(State.Ncc_Running_D).onEntryFrom(NemStatus.RUNNING, () -> {
			Boolean isNccUsingLocalNis = isNccUsingLocalNis();
			this.localNisInfoConsumer.accept(isNccUsingLocalNis);

			if (isNccUsingLocalNis) {
				if (nisStateMachine.getState() == State.Nis_Stopped_M) {
					this.startNis.accept(null);
				}
			} else {
				this.startBrowser.accept(null);
			}
		});

		nemClientNcc.configure(State.Ncc_Spawned_E).permit(NemStatus.RUNNING, State.Ncc_Running_D);
		nemClientNcc.configure(State.Ncc_Spawned_E).permit(NemStatus.STOPPED, State.Ncc_Manual_C);

		nemClientNcc.configure(State.Ncc_Manual_C).ignore(NemStatus.STARTING);
		nemClientNcc.configure(State.Ncc_Manual_C).ignore(NemStatus.RUNNING);
		nemClientNcc.configure(State.Ncc_Manual_C).ignore(NemStatus.STOPPED);

		// Nis State Machine
		nemClientNis.configure(State.Nis_Unknown).permit(NemStatus.STOPPED, State.Nis_Stopped_M);
		nemClientNis.configure(State.Nis_Unknown).permit(NemStatus.STARTING, State.Nis_Spawned_N);
		nemClientNis.configure(State.Nis_Unknown).permit(NemStatus.RUNNING, State.Nis_Running_O);
		nemClientNis.configure(State.Nis_Unknown).permit(NemStatus.BOOTING, State.Nis_Running_O);
		nemClientNis.configure(State.Nis_Unknown).permit(NemStatus.BOOTED, State.Nis_Running_O);
		nemClientNis.configure(State.Nis_Unknown).permit(NemStatus.SYNCHRONIZED, State.Nis_Running_O);

		nemClientNis.configure(State.Nis_Stopped_M).permit(NemStatus.STARTING, State.Nis_Spawned_N);
		nemClientNis.configure(State.Nis_Stopped_M).permit(NemStatus.RUNNING, State.Nis_Running_O);
		nemClientNis.configure(State.Nis_Stopped_M).permit(NemStatus.BOOTING, State.Nis_Running_O);
		nemClientNis.configure(State.Nis_Stopped_M).permit(NemStatus.BOOTED, State.Nis_Running_O);
		nemClientNis.configure(State.Nis_Stopped_M).permit(NemStatus.SYNCHRONIZED, State.Nis_Running_O);

		nemClientNis.configure(State.Nis_Spawned_N).permit(NemStatus.STOPPED, State.Nis_Stopped_R);
		nemClientNis.configure(State.Nis_Spawned_N).permit(NemStatus.RUNNING, State.Nis_Running_O);
		nemClientNis.configure(State.Nis_Spawned_N).permit(NemStatus.BOOTING, State.Nis_Running_O);
		nemClientNis.configure(State.Nis_Spawned_N).permit(NemStatus.BOOTED, State.Nis_Running_O);
		nemClientNis.configure(State.Nis_Spawned_N).permit(NemStatus.SYNCHRONIZED, State.Nis_Running_O);

		nemClientNis.configure(State.Nis_Running_O).permit(NemStatus.STOPPED, State.Nis_Manual_P);
		nemClientNis.configure(State.Nis_Running_O).ignore(NemStatus.RUNNING);
		nemClientNis.configure(State.Nis_Running_O).ignore(NemStatus.BOOTING);
		nemClientNis.configure(State.Nis_Running_O).ignore(NemStatus.BOOTED);
		nemClientNis.configure(State.Nis_Running_O).ignore(NemStatus.SYNCHRONIZED);
		nemClientNis.configure(State.Nis_Running_O).onEntryFrom(NemStatus.RUNNING, () -> startBrowser.accept(null));

		nemClientNis.configure(State.Nis_Manual_P).ignore(NemStatus.STOPPED);
		nemClientNis.configure(State.Nis_Manual_P).ignore(NemStatus.RUNNING);
		nemClientNis.configure(State.Nis_Manual_P).ignore(NemStatus.BOOTING);
		nemClientNis.configure(State.Nis_Manual_P).ignore(NemStatus.BOOTED);
		nemClientNis.configure(State.Nis_Manual_P).ignore(NemStatus.SYNCHRONIZED);
	}

	@Override
	public void notifyStatus(final NemNodeType type, final NemStatus status) {
		switch (type) {
			case NCC:
				nccStateMachine.fire(status);
				break;
			case NIS:
				nisStateMachine.fire(status);
				break;
		}
	}

	private boolean isNccUsingLocalNis() {
		// TODO 20141108: J-T i thought we had a more direct way of doing this?
		// TODO 20141109: T-J was also wondering, but I did not find a more elegant way.
		ConfigurationViewModel configModel = configNcc.get();
		return (configModel != null) && (configModel.getNisEndpoint().equals(NodeEndpoint.fromHost("localhost")));
	}

	// TODO 20141108 J-T: does this really need to be public? seems like an implementation detail
	// TODO 20141108: also might make sense for two enums (one for ncc and one for nis)
	// TODO 20141108: what are the letter postfixes for? and consider commenting the states ;) (i'm not sure what manual is)
	// TODO 20141109 T-J: Agree.
	public enum State {
		Ncc_Unknown, Ncc_Stopped_A, Ncc_Spawned_E, Ncc_Starting_B, Ncc_Running_D, Ncc_Manual_C, Nis_Unknown, Nis_Stopped_M, Nis_Spawned_N, Nis_Stopped_R, Nis_Running_O, Nis_Manual_P
	}
}
