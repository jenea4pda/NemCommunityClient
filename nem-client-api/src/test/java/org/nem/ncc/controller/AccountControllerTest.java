package org.nem.ncc.controller;

import net.minidev.json.*;
import org.hamcrest.core.*;
import org.junit.*;
import org.mockito.*;
import org.nem.core.connect.HttpPostRequest;
import org.nem.core.connect.client.NisApiId;
import org.nem.core.crypto.*;
import org.nem.core.model.*;
import org.nem.core.model.ncc.*;
import org.nem.core.model.primitive.*;
import org.nem.core.node.NodeEndpoint;
import org.nem.core.serialization.SerializableList;
import org.nem.core.time.TimeInstant;
import org.nem.ncc.connector.PrimaryNisConnector;
import org.nem.ncc.controller.requests.*;
import org.nem.ncc.controller.viewmodels.*;
import org.nem.ncc.exceptions.NccException;
import org.nem.ncc.services.*;
import org.nem.ncc.test.*;
import org.nem.ncc.wallet.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AccountControllerTest {
	private static final byte MAIN_NETWORK_VERSION = NetworkInfo.getMainNetworkInfo().getVersion();

	//region findAccount

	@Test
	public void getAccountInfoDelegatesToAccountMapper() {
		// Arrange:
		final Account account = Utils.generateRandomAccount();
		final TestContext context = new TestContext();
		final AccountViewModel originalAccountViewModel = createViewModel(account);
		Mockito.when(context.accountMapper.toViewModel(account.getAddress()))
				.thenReturn(originalAccountViewModel);

		// Act:
		final AccountIdRequest request = new AccountIdRequest(account.getAddress());
		final AccountViewModel accountViewModel = context.controller.getAccountInfo(request);

		// Assert:
		Mockito.verify(context.accountMapper, Mockito.times(1)).toViewModel(account.getAddress());
		Assert.assertThat(accountViewModel, IsEqual.equalTo(originalAccountViewModel));
	}

	//endregion

	//region getAccountTransactionsAll

	@Test
	public void getAccountTransactionsAllDelegatesToAccountMapperForAccountInformation() {
		// Arrange:
		final Account account = Utils.generateRandomAccount();
		final TestContext context = new TestContext();
		final AccountViewModel originalAccountViewModel = createViewModel(account);
		Mockito.when(context.accountMapper.toViewModel(account.getAddress()))
				.thenReturn(originalAccountViewModel);

		// Act:
		final AccountHashRequest request = new AccountHashRequest(account.getAddress(), Utils.generateRandomHash());
		final AccountTransactionsPair pair = context.controller.getAccountTransactionsAll(request);
		final AccountViewModel accountViewModel = pair.getAccount();

		// Assert:
		Mockito.verify(context.accountMapper, Mockito.times(1)).toViewModel(account.getAddress());
		Assert.assertThat(accountViewModel, IsEqual.equalTo(originalAccountViewModel));
	}

	@Test
	public void getAccountTransactionsAllDelegatesToAccountServicesForUnconfirmedTransaction() {
		// Arrange:
		final Account account = Utils.generateRandomAccount();
		final TestContext context = new TestContext();
		Mockito.when(context.accountMapper.toViewModel(account.getAddress()))
				.thenReturn(createViewModel(account));

		final List<Transaction> transactions = Arrays.asList(
				createTransfer(Utils.generateRandomAccount(), Amount.fromNem(124)),
				createTransfer(account, Amount.fromNem(572)),
				createTransfer(Utils.generateRandomAccount(), Amount.fromNem(323)));
		Mockito.when(context.accountServices.getUnconfirmedTransactions(account.getAddress()))
				.thenReturn(transactions);

		// Act:
		final AccountHashRequest request = new AccountHashRequest(account.getAddress(), Utils.generateRandomHash());
		final AccountTransactionsPair pair = context.controller.getAccountTransactionsAll(request);
		final Collection<TransferViewModel> transferViewModels = pair.getTransactions();

		// Assert:
		Mockito.verify(context.accountServices, Mockito.times(1)).getUnconfirmedTransactions(account.getAddress());
		Assert.assertThat(
				transferViewModels.stream().map(TransferViewModel::getAmount).collect(Collectors.toList()),
				IsEqual.equalTo(Arrays.asList(Amount.fromNem(124), Amount.fromNem(572), Amount.fromNem(323))));
		Assert.assertThat(
				transferViewModels.stream().map(TransferViewModel::getDirection).collect(Collectors.toList()),
				IsEqual.equalTo(Arrays.asList(0, 2, 0)));
		Assert.assertThat(
				transferViewModels.stream().map(TransferViewModel::getConfirmations).collect(Collectors.toList()),
				IsEqual.equalTo(Arrays.asList(0L, 0L, 0L)));
	}

	@Test
	public void getAccountTransactionsAllDelegatesToAccountServicesForConfirmedTransaction() {
		// Arrange:
		final Account account = Utils.generateRandomAccount();
		final TestContext context = new TestContext();
		Mockito.when(context.accountMapper.toViewModel(account.getAddress()))
				.thenReturn(createViewModel(account));
		context.setLastBlockHeight(27);

		final AccountHashRequest request = new AccountHashRequest(account.getAddress(), Utils.generateRandomHash());
		final List<TransactionMetaDataPair> pairs = Arrays.asList(
				createTransferMetaDataPair(Utils.generateRandomAccount(), Amount.fromNem(124), 19),
				createTransferMetaDataPair(account, Amount.fromNem(572), 17),
				createTransferMetaDataPair(Utils.generateRandomAccount(), Amount.fromNem(323), 27));
		Mockito.when(context.accountServices.getTransactions(TransactionDirection.ALL, account.getAddress(), request.getHash()))
				.thenReturn(pairs);

		// Act:
		final AccountTransactionsPair pair = context.controller.getAccountTransactionsAll(request);
		final Collection<TransferViewModel> transferViewModels = pair.getTransactions();

		// Assert:
		Mockito.verify(context.accountServices, Mockito.times(1))
				.getTransactions(TransactionDirection.ALL, account.getAddress(), request.getHash());
		Assert.assertThat(
				transferViewModels.stream().map(TransferViewModel::getAmount).collect(Collectors.toList()),
				IsEqual.equalTo(Arrays.asList(Amount.fromNem(124), Amount.fromNem(572), Amount.fromNem(323))));
		Assert.assertThat(
				transferViewModels.stream().map(TransferViewModel::getDirection).collect(Collectors.toList()),
				IsEqual.equalTo(Arrays.asList(0, 2, 0)));
		Assert.assertThat(
				transferViewModels.stream().map(TransferViewModel::getConfirmations).collect(Collectors.toList()),
				IsEqual.equalTo(Arrays.asList(9L, 11L, 1L)));
	}

	@Test
	public void getAccountTransactionsAllMergesUnconfirmedAndConfirmedTransactions() {
		// Arrange:
		final Account account = Utils.generateRandomAccount();
		final TestContext context = new TestContext();
		Mockito.when(context.accountMapper.toViewModel(account.getAddress()))
				.thenReturn(createViewModel(account));
		context.setLastBlockHeight(27);

		final List<Transaction> transactions = Arrays.asList(
				createTransfer(account, Amount.fromNem(124)));
		Mockito.when(context.accountServices.getUnconfirmedTransactions(account.getAddress()))
				.thenReturn(transactions);

		final AccountHashRequest request = new AccountHashRequest(account.getAddress(), Utils.generateRandomHash());
		final List<TransactionMetaDataPair> pairs = Arrays.asList(
				createTransferMetaDataPair(Utils.generateRandomAccount(), Amount.fromNem(323), 25));
		Mockito.when(context.accountServices.getTransactions(TransactionDirection.ALL, account.getAddress(), request.getHash()))
				.thenReturn(pairs);

		// Act:
		final AccountTransactionsPair pair = context.controller.getAccountTransactionsAll(request);
		final Collection<TransferViewModel> transferViewModels = pair.getTransactions();

		// Assert:
		Assert.assertThat(
				transferViewModels.stream().map(TransferViewModel::getAmount).collect(Collectors.toList()),
				IsEqual.equalTo(Arrays.asList(Amount.fromNem(124), Amount.fromNem(323))));
		Assert.assertThat(
				transferViewModels.stream().map(TransferViewModel::getDirection).collect(Collectors.toList()),
				IsEqual.equalTo(Arrays.asList(2, 0)));
		Assert.assertThat(
				transferViewModels.stream().map(TransferViewModel::getConfirmations).collect(Collectors.toList()),
				IsEqual.equalTo(Arrays.asList(0L, 3L)));
	}

	private static TransactionMetaDataPair createTransferMetaDataPair(final Account sender, final Amount amount, final int blockHeight) {
		return new TransactionMetaDataPair(
				createTransfer(sender, amount),
				new TransactionMetaData(new BlockHeight(blockHeight)));
	}

	private static Transaction createTransfer(final Account sender, final Amount amount) {
		return new TransferTransaction(
				new TimeInstant(125),
				sender,
				Utils.generateRandomAccount(),
				amount,
				null);
	}

	//endregion

	//region transactions/unconfirmed

	@Test
	public void getAccountTransactionsUnconfirmedDelegatesToAccountServicesForUnconfirmedTransaction() {
		// Arrange:
		final Account account = Utils.generateRandomAccount();
		final TestContext context = new TestContext();
		Mockito.when(context.accountMapper.toViewModel(account.getAddress()))
				.thenReturn(createViewModel(account));

		final AccountHashRequest request = new AccountHashRequest(account.getAddress(), null);
		final List<Transaction> pairs = Arrays.asList(
				createTransfer(Utils.generateRandomAccount(), Amount.fromNem(124)),
				createTransfer(account, Amount.fromNem(572)),
				createTransfer(Utils.generateRandomAccount(), Amount.fromNem(323)));
		Mockito.when(context.accountServices.getUnconfirmedTransactions(account.getAddress()))
				.thenReturn(pairs);

		// Act:
		final AccountTransactionsPair pair = context.controller.getAccountTransactionsUnconfirmed(request);
		final Collection<TransferViewModel> transferViewModels = pair.getTransactions();

		// Assert:
		Mockito.verify(context.accountServices, Mockito.times(1)).getUnconfirmedTransactions(account.getAddress());
		Assert.assertThat(
				transferViewModels.stream().map(TransferViewModel::getAmount).collect(Collectors.toList()),
				IsEqual.equalTo(Arrays.asList(Amount.fromNem(124), Amount.fromNem(572), Amount.fromNem(323))));
		Assert.assertThat(
				transferViewModels.stream().map(TransferViewModel::getDirection).collect(Collectors.toList()),
				IsEqual.equalTo(Arrays.asList(0, 2, 0)));
		Assert.assertThat(
				transferViewModels.stream().map(TransferViewModel::getConfirmations).collect(Collectors.toList()),
				IsEqual.equalTo(Arrays.asList(0L, 0L, 0L)));
	}

	@Test
	public void getAccountTransactionsUnconfirmedReturnsEmptyListWhenHashIsProvided() {
		// Arrange:
		final Account account = Utils.generateRandomAccount();
		final TestContext context = new TestContext();

		final AccountHashRequest request = new AccountHashRequest(account.getAddress(), Utils.generateRandomHash());
		Mockito.when(context.accountServices.getUnconfirmedTransactions(account.getAddress()))
				.thenReturn(Arrays.asList(createTransfer(account, Amount.fromNem(572))));

		// Act:
		final AccountTransactionsPair pair = context.controller.getAccountTransactionsUnconfirmed(request);

		// Assert:
		Mockito.verify(context.accountServices, Mockito.times(0)).getUnconfirmedTransactions(Mockito.any());
		Assert.assertThat(pair.getTransactions().size(), IsEqual.equalTo(0));
	}

	//endregion

	//region new transaction Handlers

	@Test
	public void getAccountTransactionsConfirmedDelegatesToAccountService() {
		this.assertGetTransactionsDelegateToAccountService(
				TransactionDirection.ALL,
				(TestContext ctx) -> ctx.controller::getAccountTransactionsConfirmed);
	}

	@Test
	public void getAccountTransactionsIncomingDelegatesToAccountService() {
		this.assertGetTransactionsDelegateToAccountService(
				TransactionDirection.INCOMING,
				(TestContext ctx) -> ctx.controller::getAccountTransactionsIncoming);
	}

	@Test
	public void getAccountTransactionsOutgoingDelegatesToAccountService() {
		this.assertGetTransactionsDelegateToAccountService(
				TransactionDirection.OUTGOING,
				(TestContext ctx) -> ctx.controller::getAccountTransactionsOutgoing);
	}

	private void assertGetTransactionsDelegateToAccountService(
			final TransactionDirection direction,
			final Function<TestContext, Function<AccountHashRequest, AccountTransactionsPair>> handlerFactory) {
		final Account account = Utils.generateRandomAccount();
		final TestContext context = new TestContext();
		Mockito.when(context.accountMapper.toViewModel(account.getAddress()))
				.thenReturn(createViewModel(account));
		context.setLastBlockHeight(34);

		final AccountHashRequest request = new AccountHashRequest(account.getAddress(), null);
		final List<TransactionMetaDataPair> pairs = Arrays.asList(
				createTransferMetaDataPair(Utils.generateRandomAccount(), Amount.fromNem(124), 19),
				createTransferMetaDataPair(account, Amount.fromNem(572), 17),
				createTransferMetaDataPair(Utils.generateRandomAccount(), Amount.fromNem(323), 27));
		Mockito.when(context.accountServices.getTransactions(direction, account.getAddress(), request.getHash()))
				.thenReturn(pairs);

		// Act:
		final AccountTransactionsPair pair = handlerFactory.apply(context).apply(request);
		final Collection<TransferViewModel> transferViewModels = pair.getTransactions();

		// Assert:
		Mockito.verify(context.accountServices, Mockito.times(1))
				.getTransactions(direction, account.getAddress(), request.getHash());
		Assert.assertThat(
				transferViewModels.stream().map(TransferViewModel::getAmount).collect(Collectors.toList()),
				IsEqual.equalTo(Arrays.asList(Amount.fromNem(124), Amount.fromNem(572), Amount.fromNem(323))));
		Assert.assertThat(
				transferViewModels.stream().map(TransferViewModel::getDirection).collect(Collectors.toList()),
				IsEqual.equalTo(Arrays.asList(0, 2, 0)));
		Assert.assertThat(
				transferViewModels.stream().map(TransferViewModel::getConfirmations).collect(Collectors.toList()),
				IsEqual.equalTo(Arrays.asList(16L, 18L, 8L)));
	}

	//endregion

	//region getAccountHarvests

	@Test
	public void getAccountHarvestsDelegatesToAccountServices() {
		// Arrange:
		final AccountHashRequest ahRequest = new AccountHashRequest(Utils.generateRandomAddress(), Utils.generateRandomHash());
		final TestContext context = new TestContext();
		final List<HarvestInfo> originalHarvestInfos = Arrays.asList(
				new HarvestInfo(Hash.ZERO, new BlockHeight(7), TimeInstant.ZERO, Amount.ZERO),
				new HarvestInfo(Hash.ZERO, new BlockHeight(5), TimeInstant.ZERO, Amount.ZERO),
				new HarvestInfo(Hash.ZERO, new BlockHeight(9), TimeInstant.ZERO, Amount.ZERO));

		Mockito.when(context.accountServices.getAccountHarvests(ahRequest.getAccountId(), ahRequest.getHash()))
				.thenReturn(originalHarvestInfos);

		// Act:
		final SerializableList<HarvestInfoViewModel> harvestInfos = context.controller.getAccountHarvests(ahRequest);

		// Assert:
		Mockito.verify(context.accountServices, Mockito.times(1)).getAccountHarvests(ahRequest.getAccountId(), ahRequest.getHash());
		Assert.assertThat(
				harvestInfos.asCollection().stream().map(HarvestInfoViewModel::getBlockHeight).collect(Collectors.toList()),
				IsEqual.equalTo(Arrays.asList(new BlockHeight(7), new BlockHeight(5), new BlockHeight(9))));
	}

	//endregion

	//region unlock / lock

	@Test
	public void unlockDelegatesToNisConnector() {
		// Assert:
		assertPrivateKeyConnectorRequest(AccountController::unlock, NisApiId.NIS_REST_ACCOUNT_UNLOCK);
	}

	@Test
	public void lockDelegatesToNisConnector() {
		// Assert:
		assertPrivateKeyConnectorRequest(AccountController::lock, NisApiId.NIS_REST_ACCOUNT_LOCK);
	}

	private static void assertPrivateKeyConnectorRequest(
			final BiConsumer<AccountController, AccountWalletRequest> action,
			final NisApiId apiId) {
		// Arrange:
		final TestContext context = new TestContext();
		final Wallet wallet = Mockito.mock(Wallet.class);
		final KeyPair keyPair = new KeyPair();
		final Account account = new Account(new KeyPair(keyPair.getPublicKey()));
		Mockito.when(context.walletServices.get(new WalletName("wallet"))).thenReturn(wallet);
		Mockito.when(wallet.getAccountPrivateKey(account.getAddress())).thenReturn(keyPair.getPrivateKey());

		// Act:
		action.accept(context.controller, new AccountWalletRequest(account.getAddress(), new WalletName("wallet")));

		final ArgumentCaptor<HttpPostRequest> requestCaptor = ArgumentCaptor.forClass(HttpPostRequest.class);
		Mockito.verify(context.connector, Mockito.times(1)).voidPost(Mockito.eq(apiId), requestCaptor.capture());
		final JSONObject jsonRequest = (JSONObject)JSONValue.parse(requestCaptor.getValue().getPayload());

		// Assert:
		Assert.assertThat(jsonRequest.size(), IsEqual.equalTo(1));
		Assert.assertThat(
				jsonRequest.get("value"),
				IsEqual.equalTo(keyPair.getPrivateKey().toString()));
	}

	//endregion

	//region createRealAccountData / createVanityRealAccountData / verifyRealAccountData

	@Test
	public void createRealAccountDataReturnsKeyPairViewModelWithMainNetworkVersion() {
		// Arrange:
		final TestContext context = new TestContext();

		// Act:
		final KeyPairViewModel viewModel = context.controller.createRealAccountData();

		// Assert:
		Assert.assertThat(viewModel.getKeyPair(), IsNull.notNullValue());
		Assert.assertThat(viewModel.getKeyPair().getPrivateKey(), IsNull.notNullValue());
		Assert.assertThat(viewModel.getNetworkVersion(), IsEqual.equalTo(MAIN_NETWORK_VERSION));
	}

	@Test
	public void verifyRealAccountDataSucceedsWhenPassedMainNetKeyPair() {
		// Arrange:
		final TestContext context = new TestContext();
		final KeyPairViewModel viewModel = new KeyPairViewModel(new KeyPair(), MAIN_NETWORK_VERSION);

		// Act:
		context.controller.verifyRealAccountData(viewModel);

		// Assert: (no exceptions)
	}

	@Test
	public void verifyRealAccountDataFailsWhenPassedTestNetKeyPair() {
		// Arrange:
		final TestContext context = new TestContext();
		final KeyPairViewModel viewModel = new KeyPairViewModel(new KeyPair(), NetworkInfo.getTestNetworkInfo().getVersion());

		// Act:
		ExceptionAssert.assertThrowsNccException(
				v -> context.controller.verifyRealAccountData(viewModel),
				NccException.Code.NOT_MAIN_NETWORK_ADDRESS);
	}

	//endregion

	private static AccountViewModel createViewModel(final Account account) {
		return Utils.createAccountViewModelFromAddress(account.getAddress());
	}

	private static class TestContext {
		private final NodeEndpoint nisEndpoint = NodeEndpoint.fromHost("10.0.0.99");
		private final AccountServices accountServices = Mockito.mock(AccountServices.class);
		private final AccountMapper accountMapper = Mockito.mock(AccountMapper.class);
		private final WalletServices walletServices = Mockito.mock(WalletServices.class);
		private final ChainServices chainServices = Mockito.mock(ChainServices.class);
		private final PrimaryNisConnector connector = Mockito.mock(PrimaryNisConnector.class);

		private final AccountController controller = new AccountController(
				this.accountServices,
				this.accountMapper,
				this.walletServices,
				this.chainServices,
				this.connector);

		private TestContext() {
			this.setLastBlockHeight(1);
			ServicesUtils.setupForwarding(this.connector, this.nisEndpoint);
		}

		private void setLastBlockHeight(final int height) {
			Mockito.when(this.chainServices.getChainHeightAsync(this.nisEndpoint))
					.thenReturn(CompletableFuture.completedFuture(new BlockHeight(height)));
		}
	}
}