
The following API list is not yet complete and will likely be changed during implementation. The web-context is defined as `/ncc/api` (full URI would be, for instance,  `/ncc/api/client/info`)

Amount values are all in the smallest possible NEM fraction, that means that 1000000 means 1.000000 NEM.

Rest calls must have the content type set:

`Content-Type: application/json`

***

### /info/ncc
Gets information about the NCC server.

* Request Method: _GET_
* Request: `-`
* Response: [NisInfoViewModel](viewModel.md#nisinfoviewmodel)
* Dependent from NIS: _No_

***

### /info/nis
Gets information about the NIS server.

* Request Method: _GET_
* Request: `-`
* Response: [NisInfoViewModel](viewModel.md#nisinfoviewmodel)
* Dependent from NIS: _Yes_

***

### /info/nis/check
Checks availability of NIS server.

* Request Method: _GET_
* Request: `-`
* Response:

** 0 - NIS server is not running
** 1 - NIS server is running (but may not be booted)

* Dependent from NIS: _Yes_

### /configuration/get
Gets the current configuration.

* Request Method: _GET_
* Request: `-`
* Response: [ConfigurationViewModel](viewModel.md#configurationviewmodel)
* Dependent from NIS: _No_

### /configuration/update
Updates the current configuration with the given values.

* Request Method: _POST_
* Request: [ConfigurationViewModel](viewModel.md#configurationviewmodel)
* Response: `-`
* Dependent from NIS: _No_

### /wallet/list
Gets all wallets that are located at the configured storage location for wallets.

* Request Method: _GET_
* Request: `-`
* Response: [ListOfWalletDescriptorViewModel](viewModel.md#listofwalletdescriptorviewmodel)
* Dependent from NIS: _No_

### /wallet/create
Creates a new wallet with one account in it. The wallet is stored
 encrypted in the configured wallet directory. The name of the wallet file
 is the encoded (URL encoded) version of the wallet name. The provided
 password is used to encrypt the wallet.

* Request Method: _POST_
* Request: [WalletNamePasswordPair](viewModel.md#walletnamepasswordpair)
* Response: [WalletViewModel](viewModel.md#walletviewmodel)
* Dependent from NIS: _No_

### /wallet/open
Open a wallet. The wallet is defined by the wallet name. The wallet has
	  to be located in the wallet storage location. The password must match the
	  password of the given wallet. For opening a wallet at a different
	  location the function importWallet has to be used.

* Request Method: _POST_
* Request: [WalletNamePasswordPair](viewModel.md#walletnamepasswordpair)
* Response: [WalletViewModel](viewModel.md#walletviewmodel)
* Dependent from NIS: _No_

### /wallet/info
Returns information about a wallet that is already open.

* Request Method: _POST_
* Request: [WalletName](viewModel.md#walletname)
* Response: [WalletViewModel](viewModel.md#walletviewmodel)
* Dependent from NIS: _No_

### /wallet/close
Closes a wallet by removing it from the list of opened wallets.

* Request Method: _POST_
* Request: [WalletName](viewModel.md#walletname)
* Response: `-`
* Dependent from NIS: _No_

### /wallet/password/change
Changes the password of a wallet.

* Request Method: _POST_
* Request: [WalletNamePasswordBag](viewModel.md#walletnamepasswordbag)
* Response: `-`
* Dependent from NIS: _No_

### /wallet/name/change
Changes the password of a wallet.

* Request Method: _POST_
* Request: [WalletNamePasswordBag](viewModel.md#walletnamepasswordbag)
* Response: `-`
* Dependent from NIS: _No_

### /wallet/account/new
Adds a new account to the wallet.

* Request Method: _POST_
* Request: [WalletNamePasswordBag](viewModel.md#walletnamepasswordbag)
* Response: [AccountViewModel](viewModel.md#accountviewmodel)
* Dependent from NIS: _No_

### /wallet/account/primary
Sets an existing account to be the primary account.

* Request Method: _POST_
* Request: [WalletNamePasswordBag](viewModel.md#walletnamepasswordbag)
* Response: [WalletViewModel](viewModel.md#walletviewmodel)
* Dependent from NIS: _No_

### /wallet/account/remove
Removes an existing account from a wallet.

* Request Method: _POST_
* Request: [WalletNamePasswordBag](viewModel.md#walletnamepasswordbag)
* Response: [WalletViewModel](viewModel.md#walletviewmodel)
* Dependent from NIS: _No_

### /wallet/account/label
Updates the label of an existing account.

* Request Method: _POST_
* Request: [WalletNamePasswordBag](viewModel.md#walletnamepasswordbag)
* Response: [AccountViewModel](viewModel.md#accountviewmodel)
* Dependent from NIS: _No_

### /wallet/account/transaction/send
Sends a new transaction (i.e., sending NEM, messages, assets).

* Request Method: _POST_
* Request: [TransferSendRequest](viewModel.md#transfersendrequest)
* Response: `-`
* Dependent from NIS: _Yes_

### /wallet/account/transaction/validate
Validates a new transaction (i.e., sending NEM, messages, assets) and provides information on fee and encryption possibility.

* Request Method: _POST_
* Request: [TransferValidateRequest](viewModel.md#transfervalidaterequest)
* Response: [ValidatedTransferViewModel](viewModel.md#validatedtransferviewmodel)
* Dependent from NIS: _Yes_

### /wallet/account/unlock
Unlock the account on the connected NIS server (start foraging).

* Request Method: _POST_
* Request: [AccountWalletRequest](viewModel.md#accountwalletrequest)
* Response: `-`
* Dependent from NIS: _Yes_

### /wallet/account/lock
Lock the account on the connected NIS server (stop foraging).

* Request Method: _POST_
* Request: [AccountWalletRequest](viewModel.md#accountwalletrequest)
* Response: `-`
* Dependent from NIS: _Yes_

### /wallet/account/remote/activate
Announces activation of remote harvesting account to the network (FEE: 1)

* Request Method: _POST_
* Request: [TransferImportanceRequest](viewModel.md#transferImportanceRequest)
* Response: `-`
* Dependent from NIS: _Yes_

### /wallet/account/remote/deactivate
Announces deactivation of remote harvesting account to the network (FEE: 1)

* Request Method: _POST_
* Request: [TransferImportanceRequest](viewModel.md#transferImportanceRequest)
* Response: `-`
* Dependent from NIS: _Yes_

### /wallet/account/remote/status
Checks if account is unlocked on remote server, will return an error if

* Request Method: _POST_
* Request: [AccountWalletRequest](viewModel.md#accountWalletRequest)
* Response: [AccountStatusViewModel](viewModel.md#accountStatusViewModel)
* Dependent from NIS: _Yes_

### /wallet/account/remote/unlock
Unlock the account on the remote NIS server (start secure foraging).

* Request Method: _POST_
* Request: [AccountWalletPasswordRequest](viewModel.md#accountWalletPasswordRequest)
* Response: `-`
* Dependent from NIS: _Yes_

### /wallet/account/remote/lock
Lock the account on the remote NIS server (stop secure foraging).

* Request Method: _POST_
* Request: [AccountWalletPasswordRequest](viewModel.md#accountWalletPasswordRequest)
* Response: `-`
* Dependent from NIS: _Yes_

### /account/find
Gets information about the specified account.
	 Looks-up an account which is known by its address id

* Request Method: _POST_
* Request: [AccountIdRequest](viewModel.md#accountidrequest)
* Response: [AccountViewModel](viewModel.md#accountViewModel)
* Dependent from NIS: _Yes_

### /account/transactions/unconfirmed
Gets information about the specified account and unconfirmed transactions.

* Request Method: _POST_
* Request: [AccountHashRequest](viewModel.md#accounthashrequest)
* Response: [AccountTransactionsPair](viewModel.md#accounttransactionspair)
* Dependent from NIS: _Yes_

### /account/transactions/incoming
Gets information about the specified account and incoming transactions.

* Request Method: _POST_
* Request: [AccountHashRequest](viewModel.md#accounthashrequest)
* Response: [AccountTransactionsPair](viewModel.md#accounttransactionspair)
* Dependent from NIS: _Yes_

### /account/transactions/outgoing
Gets information about the specified account and outgoing transactions.

* Request Method: _POST_
* Request: [AccountHashRequest](viewModel.md#accounthashrequest)
* Response: [AccountTransactionsPair](viewModel.md#accounttransactionspair)
* Dependent from NIS: _Yes_

### /account/transactions/all
Gets information about the specified account and incoming and outgoing transactions.

* Request Method: _POST_
* Request: [AccountHashRequest](viewModel.md#accounthashrequest)
* Response: [TransferViewModel](viewModel.md#transferviewmodel)
* Dependent from NIS: _Yes_

### /account/harvests
Retrieves a list of infos on harvested blocks for an account.

* Request Method: _POST_
* Request: [AccountTimeStampRequest](viewModel.md#accounttimestamprequest)
* Response: [HarvestInfoViewModelList](viewModel.md#harvestinfoviewmodellist)
* Dependent from NIS: _Yes_

### /node/boot
Boots the local node.

* Request Method: _POST_
* Request: [BootNodeRequest](viewModel.md#bootnoderequest)
* Response: [FeeViewModel](viewModel.md#feeviewmodel)
* Dependent from NIS: _Yes_

### /node/status
Checks the status of the local node.

* Request Method: _GET_
* Request: `-`
* Response: [NemRequestResult](viewModel.md#nemrequestresult)
* Dependent from NIS: _Yes_

### /network
Requests a view model for the peers to which the specified peers are connected.

* Request Method: _POST_
* Request: [NodeEndpointList](viewModel.md#nodeendpointlist)
* Response: [GraphViewModel](viewModel.md#graphviewmodel)
* Dependent from NIS: _Yes_

### /network/local
Requests a view model for the peers to which the local NIS is connected.

* Request Method: _GET_
* Request: `-`
* Response: [GraphViewModel](viewModel.md#graphviewmodel)
* Dependent from NIS: _Yes_

### /shutdown
Stops the NCC server. After that NCC GUI does not respond to any clicks.

* Request Method: _GET_
* Request: `-`
* Response: `-`
* Dependent from NIS: _No_

### /status
Gets the current status of the NCC server. Currently it is only 'running' (3).

* Request Method: _GET_
* Request: `-`
* Response: [NemRequestResult](viewModel.md#nemrequestresult)
* Dependent from NIS: _No_
