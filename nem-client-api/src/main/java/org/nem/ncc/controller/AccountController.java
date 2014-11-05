package org.nem.ncc.controller;

import org.nem.core.connect.HttpJsonPostRequest;
import org.nem.core.connect.client.NisApiId;
import org.nem.core.crypto.*;
import org.nem.core.model.*;
import org.nem.core.model.ncc.HarvestInfo;
import org.nem.core.model.primitive.BlockHeight;
import org.nem.core.serialization.*;
import org.nem.ncc.connector.PrimaryNisConnector;
import org.nem.ncc.controller.annotations.RequiresTrustedNis;
import org.nem.ncc.controller.requests.*;
import org.nem.ncc.controller.viewmodels.*;
import org.nem.ncc.exceptions.NccException;
import org.nem.ncc.model.VanityAddressGenerator;
import org.nem.ncc.services.*;
import org.nem.ncc.wallet.WalletAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles requests related to the REST resource "account".
 */
@RestController
public class AccountController {
	private final AccountServices accountServices;
	private final AccountMapper accountMapper;
	private final WalletServices walletServices;
	private final ChainServices chainServices;
	private final PrimaryNisConnector nisConnector;

	/**
	 * Creates a new account controller.
	 *
	 * @param accountServices The account services.
	 * @param accountMapper The account mapper.
	 * @param walletServices The wallet services.
	 * @param chainServices The chain services.
	 * @param nisConnector The NIS connector.
	 */
	@Autowired(required = true)
	public AccountController(
			final AccountServices accountServices,
			final AccountMapper accountMapper,
			final WalletServices walletServices,
			final ChainServices chainServices,
			final PrimaryNisConnector nisConnector) {
		this.accountServices = accountServices;
		this.accountMapper = accountMapper;
		this.walletServices = walletServices;
		this.chainServices = chainServices;
		this.nisConnector = nisConnector;
	}

	//region find

	/**
	 * Gets information about the specified account.
	 * Looks-up an account which is known by its address id
	 *
	 * @param aidRequest The account identifier.
	 * @return The account information.
	 */
	@RequestMapping(value = "/account/find", method = RequestMethod.POST)
	public AccountViewModel getAccountInfo(@RequestBody final AccountIdRequest aidRequest) {
		final Address address = aidRequest.getAccountId();
		return this.accountMapper.toViewModel(address);
	}

	//endregion

	//region transactions/unconfirmed

	/**
	 * Gets information about the specified account and unconfirmed transactions.
	 *
	 * @param ahRequest The account identifier.
	 * @return The account and transactions information.
	 */
	@RequestMapping(value = "/account/transactions/unconfirmed", method = RequestMethod.POST)
	public AccountTransactionsPair getAccountTransactionsUnconfirmed(@RequestBody final AccountHashRequest ahRequest) {
		final AccountViewModel account = this.getAccountInfo(ahRequest);
		return new AccountTransactionsPair(account, this.getUnconfirmedTransactions(ahRequest));
	}

	private Collection<TransferViewModel> getUnconfirmedTransactions(final AccountHashRequest ahRequest) {
		// the unconfirmed transactions api does not support paging
		// in order to simplify the UX code, pretend all subsequent pages are empty
		if (null != ahRequest.getHash()) {
			return new ArrayList<>();
		}

		final Address address = ahRequest.getAccountId();
		return this.accountServices.getUnconfirmedTransactions(address).stream()
				.map(t -> new TransferViewModel(t, address))
				.collect(Collectors.toList());
	}

	//endregion

	//region transactions/(all|confirmed|incoming|outgoing)

	/**
	 * Gets information about the specified account and incoming and outgoing confirmed and unconfirmed transactions.
	 *
	 * @param ahRequest The account identifier.
	 * @return The account and transactions information.
	 */
	@RequestMapping(value = "/account/transactions/all", method = RequestMethod.POST)
	public AccountTransactionsPair getAccountTransactionsAll(@RequestBody final AccountHashRequest ahRequest) {
		final AccountViewModel account = this.getAccountInfo(ahRequest);
		final List<TransferViewModel> allTransfers = new ArrayList<>();
		allTransfers.addAll(this.getUnconfirmedTransactions(new AccountHashRequest(ahRequest.getAccountId(), null)));
		allTransfers.addAll(this.getConfirmedTransactions(TransactionDirection.ALL, ahRequest));
		return new AccountTransactionsPair(account, allTransfers);
	}

	/**
	 * Gets information about the specified account and incoming and outgoing confirmed transactions.
	 *
	 * @param ahRequest The account identifier.
	 * @return The account and transactions information.
	 */
	@RequestMapping(value = "/account/transactions/confirmed", method = RequestMethod.POST)
	public AccountTransactionsPair getAccountTransactionsConfirmed(@RequestBody final AccountHashRequest ahRequest) {
		return this.getAccountTransactions(TransactionDirection.ALL, ahRequest);
	}

	/**
	 * Gets information about the specified account and incoming confirmed transactions.
	 *
	 * @param ahRequest The account identifier.
	 * @return The account and transactions information.
	 */
	@RequestMapping(value = "/account/transactions/incoming", method = RequestMethod.POST)
	public AccountTransactionsPair getAccountTransactionsIncoming(@RequestBody final AccountHashRequest ahRequest) {
		return this.getAccountTransactions(TransactionDirection.INCOMING, ahRequest);
	}

	/**
	 * Gets information about the specified account and outgoing confirmed transactions.
	 *
	 * @param ahRequest The account identifier.
	 * @return The account and transactions information.
	 */
	@RequestMapping(value = "/account/transactions/outgoing", method = RequestMethod.POST)
	public AccountTransactionsPair getAccountTransactionsOutgoing(@RequestBody final AccountHashRequest ahRequest) {
		return this.getAccountTransactions(TransactionDirection.OUTGOING, ahRequest);
	}

	private AccountTransactionsPair getAccountTransactions(final TransactionDirection direction, final AccountHashRequest ahRequest) {
		final AccountViewModel account = this.getAccountInfo(ahRequest);
		return new AccountTransactionsPair(account, this.getConfirmedTransactions(direction, ahRequest));
	}

	private Collection<TransferViewModel> getConfirmedTransactions(final TransactionDirection direction, final AccountHashRequest ahRequest) {
		final Address address = ahRequest.getAccountId();
		final BlockHeight lastBlockHeight = this.nisConnector.forward(this.chainServices::getChainHeightAsync);
		return this.accountServices.getTransactions(direction, address, ahRequest.getHash()).stream()
				.map(p -> new TransferViewModel(p, address, lastBlockHeight))
				.collect(Collectors.toList());
	}

	//endregion

	//region harvests

	/**
	 * Retrieves a list of infos on harvested blocks for an account.
	 *
	 * @param ahRequest The account identifier.
	 * @return The list of harvest infos.
	 */
	@RequestMapping(value = "/account/harvests", method = RequestMethod.POST)
	public SerializableList<HarvestInfoViewModel> getAccountHarvests(@RequestBody final AccountHashRequest ahRequest) {
		final List<HarvestInfo> harvestInfos = this.accountServices.getAccountHarvests(ahRequest.getAccountId(), ahRequest.getHash());
		return new SerializableList<>(harvestInfos.stream().map(HarvestInfoViewModel::new).collect(Collectors.toList()));
	}

	//endregion

	//region unlock|lock

	/**
	 * Unlock the account on the connected NIS server (start foraging).
	 *
	 * @param awRequest The account / wallet view model.
	 */
	@RequestMapping(value = "/wallet/account/unlock", method = RequestMethod.POST)
	@RequiresTrustedNis
	public void unlock(@RequestBody final AccountWalletRequest awRequest) {
		this.nisConnector.voidPost(NisApiId.NIS_REST_ACCOUNT_UNLOCK, new HttpJsonPostRequest(this.getPrivateKey(awRequest)));
	}

	/**
	 * Lock the account on the connected NIS server (stop foraging).
	 *
	 * @param awRequest The account / wallet view model.
	 */
	@RequestMapping(value = "/wallet/account/lock", method = RequestMethod.POST)
	@RequiresTrustedNis
	public void lock(@RequestBody final AccountWalletRequest awRequest) {
		this.nisConnector.voidPost(NisApiId.NIS_REST_ACCOUNT_LOCK, new HttpJsonPostRequest(this.getPrivateKey(awRequest)));
	}

	private PrivateKey getPrivateKey(final AccountWalletRequest request) {
		return this.walletServices.get(request.getWalletName())
				.getAccountPrivateKey(request.getAccountId());
	}

	// TODO 20141005 J-G i guess we need tests for these
	// > in all seriousness, it seems like remote[un]lock can just delegate to un[lock]
	// > the only difference is that the remote* functions are assumed to be passed a remote account
	// > if it's not remote we might want to fail
	//
	// 20141006, G-J, now looking at it, I'm starting to think /lock and /unlock are wrong :/
	//   they were supposed to send "remote account", and from what I see they send account address
	//   I've fixed that
	//
	// TODO 20141010 J-G really remoteUnlock/Lock are the same except for the id

	/**
	 * Unlock the account on a remote NIS server (start foraging).
	 * Remote address being used has to be announced previously.
	 *
	 * @param awpRequest The remote harvester view model.
	 */
	@RequestMapping(value = "/wallet/account/remote/unlock", method = RequestMethod.POST)
	public void remoteUnlock(@RequestBody final AccountWalletPasswordRequest awpRequest) {
		final WalletAccount account = this.walletServices.tryFindOpenAccount(awpRequest.getAccountId());
		this.nisConnector.voidPost(NisApiId.NIS_REST_ACCOUNT_UNLOCK, new HttpJsonPostRequest(account.getRemoteHarvestingPrivateKey()));
	}

	@RequestMapping(value = "/wallet/account/remote/status", method = RequestMethod.POST)
	public AccountStatusViewModel remoteStatus(@RequestBody final AccountWalletRequest awRequest) {
		final WalletAccount account = this.walletServices.tryFindOpenAccount(awRequest.getAccountId());
		final Address remoteAddress = Address.fromPublicKey((new KeyPair(account.getRemoteHarvestingPrivateKey())).getPublicKey());
		final Deserializer deserializer = this.nisConnector.get(NisApiId.NIS_REST_ACCOUNT_STATUS, "address=" + remoteAddress.getEncoded());
		return new AccountStatusViewModel(deserializer);
	}

	/**
	 * Lock the account on a remote NIS server (stop foraging).
	 *
	 * @param awpRequest The remote harvester view model.
	 */
	@RequestMapping(value = "/wallet/account/remote/lock", method = RequestMethod.POST)
	public void remoteLock(@RequestBody final AccountWalletPasswordRequest awpRequest) {
		final WalletAccount account = this.walletServices.tryFindOpenAccount(awpRequest.getAccountId());
		this.nisConnector.voidPost(NisApiId.NIS_REST_ACCOUNT_LOCK, new HttpJsonPostRequest(account.getRemoteHarvestingPrivateKey()));
	}

	//endregion

	//region create/verify real private key

	/* TODO 20141016 BR: delete at launch */

	/**
	 * Temporary API for creating a real (main-net) private key during beta.
	 *
	 * @return A key pair view model.
	 */
	@RequestMapping(value = "/account/create-real-account-data", method = RequestMethod.GET)
	public KeyPairViewModel createRealAccountData() {
		final NetworkInfo networkInfo = NetworkInfo.getMainNetworkInfo();
		final KeyPair keyPair = new KeyPair();
		return new KeyPairViewModel(keyPair, networkInfo.getVersion());
	}

	/**
	 * Temporary API for verifying a real (main-net) private key during beta.
	 *
	 * @param viewModel A key pair view model.
	 */
	@RequestMapping(value = "/account/verify-real-account-data", method = RequestMethod.POST)
	public void verifyRealAccountData(@RequestBody final KeyPairViewModel viewModel) {
		final NetworkInfo networkInfo = NetworkInfo.getMainNetworkInfo();
		if (viewModel.getNetworkVersion() != networkInfo.getVersion()) {
			throw new NccException(NccException.Code.NOT_MAIN_NETWORK_ADDRESS);
		}
	}

	//endregion
}
