package org.nem.ncc.controller.viewmodels;

import net.minidev.json.JSONObject;
import org.hamcrest.core.*;
import org.junit.*;
import org.nem.core.crypto.Hash;
import org.nem.core.messages.*;
import org.nem.core.model.*;
import org.nem.core.model.ncc.*;
import org.nem.core.model.primitive.*;
import org.nem.core.serialization.JsonSerializer;
import org.nem.core.time.*;
import org.nem.core.utils.StringEncoder;
import org.nem.ncc.test.*;

public class TransferViewModelTest {

	//region constructor

	@Test(expected = IllegalArgumentException.class)
	public void cannotCreateViewModelAroundNonTransfer() {
		// Arrange:
		final Transaction transaction = new MockTransaction();

		// Act:
		new TransferViewModel(transaction, Address.fromEncoded("foo"));
	}

	@Test
	public void canCreateViewModelAroundUnconfirmedTransfer() {
		// Arrange:
		final Account sender = Utils.generateRandomAccount();
		final Account recipient = Utils.generateRandomAccount();
		final Transaction transaction = new TransferTransaction(
				new TimeInstant(125),
				sender,
				recipient,
				Amount.fromNem(576),
				new PlainMessage(StringEncoder.getBytes("hello world")));
		transaction.setFee(Amount.fromNem(3));
		final Hash transactionHash = HashUtils.calculateHash(transaction);

		// Act:
		final TransferViewModel viewModel = new TransferViewModel(transaction, Address.fromEncoded("foo"));

		// Assert:
		Assert.assertThat(viewModel.getId(), IsEqual.equalTo(transactionHash.getShortId()));
		Assert.assertThat(viewModel.getHash(), IsEqual.equalTo(transactionHash));
		Assert.assertThat(viewModel.getSender(), IsEqual.equalTo(sender.getAddress()));
		Assert.assertThat(viewModel.getTimeStamp(), IsEqual.equalTo(SystemTimeProvider.getEpochTimeMillis() + 125 * 1000));
		Assert.assertThat(viewModel.getFee(), IsEqual.equalTo(Amount.fromNem(3)));
		Assert.assertThat(viewModel.getRecipient(), IsEqual.equalTo(recipient.getAddress()));
		Assert.assertThat(viewModel.getAmount(), IsEqual.equalTo(Amount.fromNem(576)));
		Assert.assertThat(viewModel.getMessage(), IsEqual.equalTo("hello world"));
		Assert.assertThat(viewModel.isEncrypted(), IsEqual.equalTo(false));
		Assert.assertThat(viewModel.isConfirmed(), IsEqual.equalTo(false));
		Assert.assertThat(viewModel.getConfirmations(), IsEqual.equalTo(0L));
		Assert.assertThat(viewModel.getBlockHeight(), IsEqual.equalTo(0L));
		Assert.assertThat(viewModel.getDirection(), IsEqual.equalTo(0));
	}

	@Test
	public void canCreateViewModelAroundConfirmedTransfer() {
		// Arrange:
		final Account sender = Utils.generateRandomAccount();
		final Account recipient = Utils.generateRandomAccount();
		final Transaction transaction = new TransferTransaction(
				new TimeInstant(125),
				sender,
				recipient,
				Amount.fromNem(576),
				new PlainMessage(StringEncoder.getBytes("hello world")));
		transaction.setFee(Amount.fromNem(3));
		final Hash transactionHash = HashUtils.calculateHash(transaction);

		// Act:
		final TransferViewModel viewModel = new TransferViewModel(
				new TransactionMetaDataPair(transaction, new TransactionMetaData(new BlockHeight(7))),
				Address.fromEncoded("foo"),
				new BlockHeight(12));

		// Assert:
		Assert.assertThat(viewModel.getId(), IsEqual.equalTo(transactionHash.getShortId()));
		Assert.assertThat(viewModel.getHash(), IsEqual.equalTo(transactionHash));
		Assert.assertThat(viewModel.getSender(), IsEqual.equalTo(sender.getAddress()));
		Assert.assertThat(viewModel.getTimeStamp(), IsEqual.equalTo(SystemTimeProvider.getEpochTimeMillis() + 125 * 1000));
		Assert.assertThat(viewModel.getFee(), IsEqual.equalTo(Amount.fromNem(3)));
		Assert.assertThat(viewModel.getRecipient(), IsEqual.equalTo(recipient.getAddress()));
		Assert.assertThat(viewModel.getAmount(), IsEqual.equalTo(Amount.fromNem(576)));
		Assert.assertThat(viewModel.getMessage(), IsEqual.equalTo("hello world"));
		Assert.assertThat(viewModel.isEncrypted(), IsEqual.equalTo(false));
		Assert.assertThat(viewModel.isConfirmed(), IsEqual.equalTo(true));
		Assert.assertThat(viewModel.getConfirmations(), IsEqual.equalTo(6L));
		Assert.assertThat(viewModel.getBlockHeight(), IsEqual.equalTo(7L));
		Assert.assertThat(viewModel.getDirection(), IsEqual.equalTo(0));
	}

	//endregion

	//region serialization

	@Test
	public void canSerializeViewModelAroundUnconfirmedTransfer() {
		// Arrange:
		final Account sender = Utils.generateRandomAccount();
		final Account recipient = Utils.generateRandomAccount();
		final Transaction transaction = new TransferTransaction(
				new TimeInstant(125),
				sender,
				recipient,
				Amount.fromNem(576),
				new PlainMessage(StringEncoder.getBytes("hello world")));
		transaction.setFee(Amount.fromNem(3));
		final Hash transactionHash = HashUtils.calculateHash(transaction);

		// Act:
		final TransferViewModel viewModel = new TransferViewModel(transaction, recipient.getAddress());
		final JSONObject jsonObject = JsonSerializer.serializeToJson(viewModel);

		// Assert:
		Assert.assertThat(jsonObject.size(), IsEqual.equalTo(13));
		Assert.assertThat(jsonObject.get("id"), IsEqual.equalTo(transactionHash.getShortId()));
		Assert.assertThat(jsonObject.get("hash"), IsEqual.equalTo(transactionHash.toString()));
		Assert.assertThat(jsonObject.get("sender"), IsEqual.equalTo(sender.getAddress().toString()));
		Assert.assertThat(jsonObject.get("timeStamp"), IsEqual.equalTo(SystemTimeProvider.getEpochTimeMillis() + 125 * 1000));
		Assert.assertThat(jsonObject.get("fee"), IsEqual.equalTo(3000000L));
		Assert.assertThat(jsonObject.get("recipient"), IsEqual.equalTo(recipient.getAddress().toString()));
		Assert.assertThat(jsonObject.get("amount"), IsEqual.equalTo(576000000L));
		Assert.assertThat(jsonObject.get("message"), IsEqual.equalTo("hello world"));
		Assert.assertThat(jsonObject.get("encrypted"), IsEqual.equalTo(0));
		Assert.assertThat(jsonObject.get("confirmed"), IsEqual.equalTo(0));
		Assert.assertThat(jsonObject.get("confirmations"), IsEqual.equalTo(0L));
		Assert.assertThat(jsonObject.get("blockHeight"), IsEqual.equalTo(0L));
		Assert.assertThat(jsonObject.get("direction"), IsEqual.equalTo(1));
	}

	@Test
	public void canSerializeViewModelAroundConfirmedTransfer() {
		// Arrange:
		final Account sender = Utils.generateRandomAccount();
		final Account recipient = Utils.generateRandomAccount();
		final Transaction transaction = new TransferTransaction(
				new TimeInstant(125),
				sender,
				recipient,
				Amount.fromNem(576),
				new PlainMessage(StringEncoder.getBytes("hello world")));
		transaction.setFee(Amount.fromNem(3));
		final Hash transactionHash = HashUtils.calculateHash(transaction);

		// Act:
		final TransferViewModel viewModel = new TransferViewModel(
				new TransactionMetaDataPair(transaction, new TransactionMetaData(new BlockHeight(7))),
				sender.getAddress(),
				new BlockHeight(12));
		final JSONObject jsonObject = JsonSerializer.serializeToJson(viewModel);

		// Assert:
		Assert.assertThat(jsonObject.size(), IsEqual.equalTo(13));
		Assert.assertThat(jsonObject.get("id"), IsEqual.equalTo(transactionHash.getShortId()));
		Assert.assertThat(jsonObject.get("hash"), IsEqual.equalTo(transactionHash.toString()));
		Assert.assertThat(jsonObject.get("sender"), IsEqual.equalTo(sender.getAddress().toString()));
		Assert.assertThat(jsonObject.get("timeStamp"), IsEqual.equalTo(SystemTimeProvider.getEpochTimeMillis() + 125 * 1000));
		Assert.assertThat(jsonObject.get("fee"), IsEqual.equalTo(3000000L));
		Assert.assertThat(jsonObject.get("recipient"), IsEqual.equalTo(recipient.getAddress().toString()));
		Assert.assertThat(jsonObject.get("amount"), IsEqual.equalTo(576000000L));
		Assert.assertThat(jsonObject.get("message"), IsEqual.equalTo("hello world"));
		Assert.assertThat(jsonObject.get("encrypted"), IsEqual.equalTo(0));
		Assert.assertThat(jsonObject.get("confirmed"), IsEqual.equalTo(1));
		Assert.assertThat(jsonObject.get("confirmations"), IsEqual.equalTo(6L));
		Assert.assertThat(jsonObject.get("blockHeight"), IsEqual.equalTo(7L));
		Assert.assertThat(jsonObject.get("direction"), IsEqual.equalTo(2));
	}

	@Test
	public void canSerializeViewModelAroundEncryptedMessage() {
		// Arrange:
		final Account sender = Utils.generateRandomAccount();
		final Account recipient = Utils.generateRandomAccount();
		final Transaction transaction = new TransferTransaction(
				new TimeInstant(125),
				sender,
				recipient,
				Amount.fromNem(576),
				SecureMessage.fromDecodedPayload(sender, recipient, StringEncoder.getBytes("hello world")));

		// Act:
		final TransferViewModel viewModel = new TransferViewModel(transaction, Address.fromEncoded("foo"));
		final JSONObject jsonObject = JsonSerializer.serializeToJson(viewModel);

		// Assert:
		Assert.assertThat(jsonObject.get("message"), IsEqual.equalTo("hello world"));
		Assert.assertThat(jsonObject.get("encrypted"), IsEqual.equalTo(1));
	}

	//endregion

	//region message

	@Test
	public void canCreateViewModelAroundTransactionWithoutMessage() {
		// Arrange:
		final Account sender = Utils.generateRandomAccount();
		final Account recipient = Utils.generateRandomAccount();
		final Transaction transaction = new TransferTransaction(
				new TimeInstant(125),
				sender,
				recipient,
				Amount.fromNem(576),
				null);

		// Act:
		final TransferViewModel viewModel = new TransferViewModel(transaction, Address.fromEncoded("foo"));

		// Assert:
		Assert.assertThat(viewModel.getMessage(), IsNull.nullValue());
		Assert.assertThat(viewModel.isEncrypted(), IsEqual.equalTo(false));
	}

	@Test
	public void canCreateViewModelAroundTransactionWithNonEncryptedMessage() {
		// Arrange:
		final Account sender = Utils.generateRandomAccount();
		final Account recipient = Utils.generateRandomAccount();
		final Transaction transaction = new TransferTransaction(
				new TimeInstant(125),
				sender,
				recipient,
				Amount.fromNem(576),
				new PlainMessage(StringEncoder.getBytes("hello world")));

		// Act:
		final TransferViewModel viewModel = new TransferViewModel(transaction, Address.fromEncoded("foo"));

		// Assert:
		Assert.assertThat(viewModel.getMessage(), IsEqual.equalTo("hello world"));
		Assert.assertThat(viewModel.isEncrypted(), IsEqual.equalTo(false));
	}

	@Test
	public void canCreateViewModelAroundTransactionWithEncryptedMessageThatCanBeDecoded() {
		// Arrange:
		final Account sender = Utils.generateRandomAccount();
		final Account recipient = Utils.generateRandomAccount();
		final Transaction transaction = new TransferTransaction(
				new TimeInstant(125),
				sender,
				recipient,
				Amount.fromNem(576),
				SecureMessage.fromDecodedPayload(sender, recipient, StringEncoder.getBytes("hello world")));

		// Act:
		final TransferViewModel viewModel = new TransferViewModel(transaction, Address.fromEncoded("foo"));

		// Assert:
		Assert.assertThat(viewModel.getMessage(), IsEqual.equalTo("hello world"));
		Assert.assertThat(viewModel.isEncrypted(), IsEqual.equalTo(true));
	}

	@Test
	public void canCreateViewModelAroundTransactionWithEncryptedMessageThatCannotBeDecoded() {
		// Arrange:
		final Account sender = Utils.generateRandomAccount();
		final Account recipient = Utils.generateRandomAccount();
		final byte[] secureMessagePayload = SecureMessage.fromDecodedPayload(
				sender,
				recipient,
				StringEncoder.getBytes("hello world")).getEncodedPayload();
		final Transaction transaction = new TransferTransaction(
				new TimeInstant(125),
				sender,
				recipient,
				Amount.fromNem(576),
				SecureMessage.fromEncodedPayload(
						new Account(sender.getAddress()),
						new Account(recipient.getAddress()),
						secureMessagePayload));

		// Act:
		final TransferViewModel viewModel = new TransferViewModel(transaction, Address.fromEncoded("foo"));

		// Assert:
		Assert.assertThat(viewModel.getMessage(), IsEqual.equalTo("Warning: message cannot be decoded!"));
		Assert.assertThat(viewModel.isEncrypted(), IsEqual.equalTo(true));
	}

	//endregion

	//region direction

	@Test
	public void canCreateViewModelAroundTransactionWithNoDirection() {
		// Arrange:
		final Account sender = Utils.generateRandomAccount();
		final Account recipient = Utils.generateRandomAccount();
		final Transaction transaction = new TransferTransaction(
				new TimeInstant(125),
				sender,
				recipient,
				Amount.fromNem(576),
				null);

		// Act:
		final TransferViewModel viewModel = new TransferViewModel(transaction, Address.fromEncoded("foo"));

		// Assert:
		Assert.assertThat(viewModel.getDirection(), IsEqual.equalTo(0));
	}

	@Test
	public void canCreateViewModelAroundTransactionWithIncomingDirection() {
		// Arrange:
		final Account sender = Utils.generateRandomAccount();
		final Account recipient = Utils.generateRandomAccount();
		final Transaction transaction = new TransferTransaction(
				new TimeInstant(125),
				sender,
				recipient,
				Amount.fromNem(576),
				null);

		// Act:
		final TransferViewModel viewModel = new TransferViewModel(transaction, recipient.getAddress());

		// Assert:
		Assert.assertThat(viewModel.getDirection(), IsEqual.equalTo(1));
	}

	@Test
	public void canCreateViewModelAroundTransactionWithOutgoingDirection() {
		// Arrange:
		final Account sender = Utils.generateRandomAccount();
		final Account recipient = Utils.generateRandomAccount();
		final Transaction transaction = new TransferTransaction(
				new TimeInstant(125),
				sender,
				recipient,
				Amount.fromNem(576),
				null);

		// Act:
		final TransferViewModel viewModel = new TransferViewModel(transaction, sender.getAddress());

		// Assert:
		Assert.assertThat(viewModel.getDirection(), IsEqual.equalTo(2));
	}

	@Test
	public void canCreateViewModelAroundTransactionWithTwoWayDirection() {
		// Arrange:
		final Account sender = Utils.generateRandomAccount();
		final Transaction transaction = new TransferTransaction(
				new TimeInstant(125),
				sender,
				sender,
				Amount.fromNem(576),
				null);

		// Act:
		final TransferViewModel viewModel = new TransferViewModel(transaction, sender.getAddress());

		// Assert:
		Assert.assertThat(viewModel.getDirection(), IsEqual.equalTo(3));
	}

	//endregion

	//region confirmed

	@Test
	public void transactionInLastBlockIsConfirmed() {
		// Arrange:
		final Account sender = Utils.generateRandomAccount();
		final Account recipient = Utils.generateRandomAccount();
		final Transaction transaction = new TransferTransaction(
				new TimeInstant(125),
				sender,
				recipient,
				Amount.fromNem(576),
				new PlainMessage(StringEncoder.getBytes("hello world")));

		// Act:
		final TransferViewModel viewModel = new TransferViewModel(
				new TransactionMetaDataPair(transaction, new TransactionMetaData(new BlockHeight(7))),
				Address.fromEncoded("foo"),
				new BlockHeight(7));

		// Assert:
		Assert.assertThat(viewModel.isConfirmed(), IsEqual.equalTo(true));
		Assert.assertThat(viewModel.getConfirmations(), IsEqual.equalTo(1L));
		Assert.assertThat(viewModel.getBlockHeight(), IsEqual.equalTo(7L));
	}

	//endregion
}