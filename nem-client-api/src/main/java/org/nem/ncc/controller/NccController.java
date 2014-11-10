package org.nem.ncc.controller;

import org.nem.core.metadata.ApplicationMetaData;
import org.nem.core.model.primitive.BlockHeight;
import org.nem.core.node.NisNodeInfo;
import org.nem.ncc.connector.PrimaryNisConnector;
import org.nem.ncc.controller.viewmodels.*;
import org.nem.ncc.exceptions.NccException;
import org.nem.ncc.model.*;
import org.nem.ncc.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Handles requests related to NCC and NIS.
 */
@RestController
public class NccController {
	private final Configuration configuration;
	private final ApplicationMetaData metaData;
	private final PrimaryNisConnector connector;
	private final ChainServices chainServices;
	private final NodeServices nodeServices;

	/**
	 * Creates a new NCC controller.
	 *
	 * @param configuration The NCC configuration.
	 * @param metaData The NCC meta data.
	 * @param connector The NIS connector.
	 * @param chainServices The chain services.
	 * @param nodeServices The node services.
	 */
	@Autowired(required = true)
	public NccController(
			final Configuration configuration,
			final ApplicationMetaData metaData,
			final PrimaryNisConnector connector,
			final ChainServices chainServices,
			final NodeServices nodeServices) {
		this.configuration = configuration;
		this.metaData = metaData;
		this.connector = connector;
		this.chainServices = chainServices;
		this.nodeServices = nodeServices;
	}

	/**
	 * Gets information about the NCC server.
	 *
	 * @return The NCC information.
	 */
	@RequestMapping(value = "/info/ncc", method = RequestMethod.GET)
	public NccInfoViewModel getNccInfo() {
		return new NccInfoViewModel(this.metaData, this.configuration);
	}

	/**
	 * Gets information about the NIS server.
	 *
	 * @return The NIS information.
	 */
	@RequestMapping(value = "/info/nis", method = RequestMethod.GET)
	public NisInfoViewModel getNisInfo() {
		if (!this.connector.isConnected()) {
			throw new NccException(NccException.Code.NIS_NOT_AVAILABLE);
		}

		final NisNodeInfo nodeInfo = this.connector.forward(this.nodeServices::getNisNodeInfoAsync);
		final NisNodeMetaData nodeMetaData = this.connector.forward(this.chainServices::getNodeMetaDataAsync);
		return new NisInfoViewModel(nodeInfo, nodeMetaData);
	}

	/**
	 * Gets a boolean value indicating whether or not NIS is running.
	 *
	 * @return "1" if NIS is available, "0" otherwise.
	 */
	@RequestMapping(value = "/info/nis/check", method = RequestMethod.GET)
	public String isNisRunning() {
		return this.connector.isConnected() ? "1" : "0";
	}

	/**
	 * Gets the block height of the NIS node.
	 *
	 * @return The current NIS block height.
	 */
	@RequestMapping(value = "/info/nis/chain/height", method = RequestMethod.GET)
	public BlockHeight getBlockHeight() {
		return this.connector.forward(this.chainServices::getChainHeightAsync);
	}
}
