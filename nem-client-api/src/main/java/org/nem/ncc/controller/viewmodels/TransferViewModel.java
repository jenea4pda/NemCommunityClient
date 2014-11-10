package org.nem.ncc.controller.viewmodels;

import org.nem.core.crypto.Hash;
import org.nem.core.model.*;
import org.nem.core.model.ncc.TransactionMetaDataPair;
import org.nem.core.model.primitive.*;
import org.nem.core.serialization.*;
import org.nem.core.time.UnixTime;
import org.nem.core.utils.StringEncoder;

/**
 * A view model representing a transfer transaction.
 */
public class TransferViewModel implements SerializableEntity {
	private static final int INCOMING_FLAG = 1;
	private static final int OUTGOING_FLAG = 2;

	private final long transactionId;
	private final Hash transactionHash;

	private final Address sender;
	private final long timeStamp;
	private final Amount fee;

	private final Address recipient;
	private final Amount amount;
	private final String message;
	private final boolean isEncrypted;

	private boolean isConfirmed;
	private long confirmations;
	private long blockHeight;
	private final int direction; // 1 - incoming, 2 - outgoing, 3 - self

	/**
	 * Creates a new transfer view model around an unconfirmed transaction.
	 *
	 * @param transaction The transaction.
	 * @param relativeAccountAddress The relative account address.
	 */
	public TransferViewModel(
			final Transaction transaction,
			final Address relativeAccountAddress) {
		if (TransactionTypes.TRANSFER != transaction.getType() && TransactionTypes.IMPORTANCE_TRANSFER != transaction.getType()) {
			throw new IllegalArgumentException("TransferViewModel can only be created around TRANSFER or IMPORTANCE_TRANSFER");
		}

		this.transactionHash = HashUtils.calculateHash(transaction);
		this.transactionId = this.transactionHash.getShortId();

		this.sender = transaction.getSigner().getAddress();
		this.timeStamp = UnixTime.fromTimeInstant(transaction.getTimeStamp()).getMillis();
		this.fee = transaction.getFee();

		// TODO 20141014 J-G: we should definitely add tests for this mapping
		// > also from a design perspective, is it reasonable to show importance transfers like regular transfers?
		switch (transaction.getType()) {
			case TransactionTypes.TRANSFER: {
				final TransferTransaction transfer = (TransferTransaction)transaction;
				this.recipient = transfer.getRecipient().getAddress();
				this.amount = transfer.getAmount();

				final Message message = transfer.getMessage();
				this.message = getMessageText(message);
				this.isEncrypted = isEncrypted(message);
			}
			break;
			case TransactionTypes.IMPORTANCE_TRANSFER: {
				final ImportanceTransferTransaction importanceTransfer = (ImportanceTransferTransaction)transaction;
				this.recipient = importanceTransfer.getRemote().getAddress();
				this.amount = Amount.ZERO;
				this.message = null;
				this.isEncrypted = false;
			}
			break;
			default:
				throw new IllegalArgumentException("TransferViewModel ctor error");
		}

		this.confirmations = 0;
		this.blockHeight = 0;
		this.isConfirmed = false;

		this.direction =
				(this.sender.equals(relativeAccountAddress) ? OUTGOING_FLAG : 0) +
						(this.recipient.equals(relativeAccountAddress) ? INCOMING_FLAG : 0);
	}

	/**
	 * Creates a new transfer view model around a confirmed transaction.
	 *
	 * @param metaDataPair The transaction metadata pair.
	 * @param relativeAccountAddress The relative account address.
	 * @param lastBlockHeight The last block height.
	 */
	public TransferViewModel(
			final TransactionMetaDataPair metaDataPair,
			final Address relativeAccountAddress,
			final BlockHeight lastBlockHeight) {
		this(metaDataPair.getTransaction(), relativeAccountAddress);

		this.confirmations = lastBlockHeight.subtract(metaDataPair.getMetaData().getHeight()) + 1;
		this.blockHeight = metaDataPair.getMetaData().getHeight().getRaw();
		this.isConfirmed = true;
	}

	/**
	 * Gets the transaction id.
	 *
	 * @return The transaction id.
	 */
	public long getId() {
		return this.transactionId;
	}

	/**
	 * Gets the transaction hash.
	 *
	 * @return The transaction hash.
	 */
	public Hash getHash() {
		return this.transactionHash;
	}

	/**
	 * Gets the sender address.
	 *
	 * @return The sender address.
	 */
	public Address getSender() {
		return this.sender;
	}

	/**
	 * Gets the timestamp (unix time).
	 *
	 * @return The timestamp.
	 */
	public long getTimeStamp() {
		return this.timeStamp;
	}

	/**
	 * Gets the fee.
	 *
	 * @return The fee.
	 */
	public Amount getFee() {
		return this.fee;
	}

	/**
	 * Gets the recipient address.
	 *
	 * @return The recipient address.
	 */
	public Address getRecipient() {
		return this.recipient;
	}

	/**
	 * Gets the amount.
	 *
	 * @return The amount.
	 */
	public Amount getAmount() {
		return this.amount;
	}

	/**
	 * Gets the message.
	 *
	 * @return The message.
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * Gets a value indicating whether or not the message is encrypted.
	 *
	 * @return true if the message is encrypted.
	 */
	public boolean isEncrypted() {
		return this.isEncrypted;
	}

	/**
	 * Gets a value indicating whether or not the transaction is confirmed.
	 *
	 * @return true if the message is confirmed.
	 */
	public boolean isConfirmed() {
		return this.isConfirmed;
	}

	/**
	 * Gets the number of confirmations.
	 *
	 * @return The number of confirmations.
	 */
	public long getConfirmations() {
		return this.confirmations;
	}

	/**
	 * Gets the height of the block that includes the transaction
	 * or zero if the transaction is unconfirmed.
	 *
	 * @return The height of the block that includes the transaction.
	 */
	public long getBlockHeight() {
		return this.blockHeight;
	}

	/**
	 * Gets the transaction direction relative to the reference account.
	 *
	 * @return The transaction direction.
	 */
	public int getDirection() {
		return this.direction;
	}

	private static boolean isEncrypted(final Message message) {
		return null != message && MessageTypes.SECURE == message.getType();
	}

	private static String getMessageText(final Message message) {
		if (null == message) {
			return null;
		}

		return message.canDecode()
				? StringEncoder.getString(message.getDecodedPayload())
				: "Warning: message cannot be decoded!";
	}

	@Override
	public void serialize(final Serializer serializer) {
		serializer.writeLong("id", this.transactionId);
		serializer.writeString("hash", this.transactionHash.toString());

		Address.writeTo(serializer, "sender", this.sender);
		serializer.writeLong("timeStamp", this.timeStamp);
		Amount.writeTo(serializer, "fee", this.fee);

		Address.writeTo(serializer, "recipient", this.recipient);
		Amount.writeTo(serializer, "amount", this.amount);
		if (this.message != null) {
			serializer.writeString("message", this.message);
			serializer.writeInt("encrypted", this.isEncrypted ? 1 : 0);
		}

		serializer.writeInt("confirmed", this.isConfirmed ? 1 : 0);
		serializer.writeLong("confirmations", this.confirmations);
		serializer.writeLong("blockHeight", this.blockHeight);
		serializer.writeInt("direction", this.direction);
	}
}
