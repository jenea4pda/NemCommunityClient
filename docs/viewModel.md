The view model used by the NCC server.

### AccountIdRequest

```
{
	"account" : "TCN33UYH7OREBBFPA4D7GAE6TALNAZJOF6ZPDVA2"
}
```

### AccountHashRequest

```
{
	"account" : "TCN33UYH7OREBBFPA4D7GAE6TALNAZJOF6ZPDVA2",
	"hash": "37ccf321509d58a0d5f7fd48ff4350786abcddddf72d4b5baeae6d3c7f506150"
}
```

### AccountTimeStampRequest

```
{
	"account" : "TCN33UYH7OREBBFPA4D7GAE6TALNAZJOF6ZPDVA2",
	"timeStamp" : 1408700457000
}
```

### AccountTransactionsPair

```
{
	"address": "TATHIESMY6QO6XKPCBZFEVVVFVL2UT3ESDHAVGL7",
	"balance": 50170416240761,
	"importance": 0.012124950571331367,
	"label": null,
	"publicKey": "03c55bd250e56c292ed4c898b0883676313283251d21b6a9099bb989db99d736d2",
	"transactions": [{																	# collection of [TransferViewModel](viewModel.md#transferviewmodel)
		"id": "19283746535271819",
		"hash": "1e9d8feec7206ccc3df254bde2a98372b61eef56ac5d907479bc8cc78aa8d1bd",
		"sender" : "TCN33UYH7OREBBFPA4D7GAE6TALNAZJOF6ZPDVA2",
		"timeStamp" : 1234211,
		"fee": 1.0,
		"recipient": "TBYDNCLNEGESF5EWRFWFJU7RNCPWOOQH5LMRSQ5A",
		"amount":  10000.0,
		"message": "My first message",
		"encrypt": 1,
		"confirmed" : 1,
		"confirmations" : 1440,
		"blockHeight" : 80765,
		"direction": 0 
	},
	{
		"timeStamp": 1408403524000,
		"amount": 15000000000,
		"encrypted": 0,
		"sender": "TDUDEHNJR324TIYOYON5T4Z6ZZSCAAAAFDSG56DC",
		"fee": 1000000000,
		"recipient": "TATHIESMY6QO6XKPCBZFEVVVFVL2UT3ESDHAVGL7",
		"id": 2206077648187190476,
		"message": "Easy money\n\nParana",
		"confirmations": 6564,
		"confirmed": 1,
		"hash": "1e9d8feec7206ccc3df254bde2a98372b61eef56ac5d907479bc8cc78aa8d1bd",
	"blockHeight" : 80765,
		"direction": 1
	}],
	"foragedBlocks": 97,
	"status": "UNLOCKED"
	"remoteStatus": "ACTIVATING"
}
```

possible remoteStatus values:

* REMOTE - shouldn't normally occur, both buttons should be disabled
* INACTIVE - "activate" button should be enabled, "start remote harvesting" - disabled
* ACTIVATING - "activate" button should be disabled, "start remote harvesting" - enabled
* ACTIVE - "DEactivate" button should be enabled, "start remote harvesting" - enabled
* DEACTIVATING - "DEactivate" button should be disabled,  "start remote harvesting" - disabled

### AccountStatusViewModel

```
{
	"status" : "UNLOCKED"
}
```

possible status values

* LOCKED
* UNLOCKED

### AccountViewModel

```
{
	"address": "TBYDNCLNEGESF5EWRFWFJU7RNCPWOOQH5LMRSQ5A",
	"balance": 0,
	"importance": 0.0,
	"label": "private Label",
	"publicKey": null,
	"foragedBlocks": 0,
	"status": "LOCKED"
}
```

### AccountWalletRequest

```
{
	"account" : "TCN33UYH7OREBBFPA4D7GAE6TALNAZJOF6ZPDVA2",
	"wallet" : "Test-Wallet"
}
```

### TransferImportanceRequest
```
{
	"account" : "TCN33UYH7OREBBFPA4D7GAE6TALNAZJOF6ZPDVA2",
	"wallet" : "Test-Wallet",
	"password": "Very Secret Password To The Wallet",
	"hours_due": "6" // (max: 24)
}
```

### BootNodeRequest

```
{
	"node_name": "Arthur",
	"wallet": "PrivateWallet",
	"account" : "TCN33UYH7OREBBFPA4D7GAE6TALNAZJOF6ZPDVA2"
}
```

### ConfigurationViewModel
The remoteServer specifies the NIS that NCC will try to connect to.
If nisBootInfo.bootNis is set to 1, then upon opening a wallet the GUI will try to boot NIS if not already done.
The primary account of the wallet will be used if the account field is an empty string.
Only a local NIS can be booted.
```
{
	"remoteServer": {
		"protocol":"http",			# "http" or "https"
		"host":"79.220.34.156",			# ip address or "localhost" to use a local NIS
		"port":7890				# port to use
	},
	"nisBootInfo": {
		"nodeName": "Alice",			# (arbitrary) name of the node
		"bootNis": 0,				# 1=auto-boot, 0=do not auto-boot
		"account": "TALICEMYYJFPJ4FWCMMNFCCOTFV635SBL2QWP73K"	# account to tie the node to 
									# or "" to use the wallet's primary account
	},
	"language": "DE"				# 2 letter abbreviation of the language to use
}
```

### GraphViewModel

```
{
	"meta": {
		"meta": [{
			"endpoint": {
				"protocol": "http",
				"port": 7890,
				"host": "85.94.111.196"
			},
			"address": "TCJQPPB7ESZJWHILWBFG5TJRWXWJD6MZUGO6XA7S",
			"active": 1,
			"version": "0.4.3-BETA",
			"platform": "Oracle Corporation (1.8.0_11) on Windows 8.1"
		},
		{
			"endpoint": {
				"protocol": "http",
				"port": 7890,
				"host": "199.217.112.135"
			},
			"address": "TCAGZZKJXE43NNWBMB23ODTANMALICE4XDOEAWXB",
			"active": 1,
			"version": "0.4.3-BETA",
			"platform": "?"
		}]
	},
	"graph": {
		"nodes": [{
			"id": "TCJQPPB7ESZJWHILWBFG5TJRWXWJD6MZUGO6XA7S",
			"label": "pole"
		},
		{
			"id": "TCAGZZKJXE43NNWBMB23ODTANMALICE4XDOEAWXB",
			"label": "Hi, I am Alice4"
		}],
		"edges": [{
			"id": "TBGIMREUQQ5ZQX6C3IGLBSVPMROHCMPEIHY4GV2L -> TB6TEQCWJUSANWAAFRGKTQKMOGOB6G2ZH7OMKOGO",
			"source": "TBGIMREUQQ5ZQX6C3IGLBSVPMROHCMPEIHY4GV2L",
			"target": "TB6TEQCWJUSANWAAFRGKTQKMOGOB6G2ZH7OMKOGO"
		},
		{
			"id": "TBGIMREUQQ5ZQX6C3IGLBSVPMROHCMPEIHY4GV2L -> TCBMYGMLALICE2FITJVBJDEC2SNSPLYDMDM4WL5W",
			"source": "TBGIMREUQQ5ZQX6C3IGLBSVPMROHCMPEIHY4GV2L",
			"target": "TCBMYGMLALICE2FITJVBJDEC2SNSPLYDMDM4WL5W"
		}]
	}
}
```

### HarvestInfoViewModelList

```
{
	"data": [{
		"id": "http",
		"hash": "1e9d8feec7206ccc3df254bde2a98372b61eef56ac5d907479bc8cc78aa8d1bd",
		"message": "",
		"timeStamp": 1408700457000,
		"fee": 10.0,
		"height" : 17801
	}]
}
```

### ListOfWalletDescriptorViewModel

```
{
	"wallets": [{
		"wallet": "Test",
		"location": "C:\\Users\\Thies1965\\git\\NemCommunityClient\\nem-client-api\\Test.wlt"
	},
	{
		"wallet": "Thies1965",
		"location": "C:\\Users\\Thies1965\\git\\NemCommunityClient\\nem-client-api\\Thies1965.wlt"
	}]
}
```

### NisInfoViewModel

```
{
	"metaData": {
		"currentTime": 1179142,               # time in milli seconds
		"application": "NEM",                 # name of application
		"startTime": 1177027,                 # time in milli seconds, when application was started
		"version": "0.2.0-DEVELOPER BUILD",   # version of application
		"signer": null                        # name of the signer of the code
	},
	"remoteServer": null,                     # not used
	"language": "DE"                          # language for UI
}
```

### NodeEndpointList

```
{
	"data": [{
		"protocol": "http",
		"host": "bob.nem.ninja",
		"port": 7890
	},
	{
		"protocol": "http",
		"host": "alice.nem.ninja",
		"port": 7890
	}]
}
```

### NemRequestResult

```
{
	"code": 3,            # 0 - UNKNOWN
	                      # 1 - STOPPED
	                      # 2 - STARTING
	                      # 3 - RUNNING
	                      # 4 - BOOTING
	                      # 5 - BOOTED
	                      # 6 - SYNCHRONIZED
	"type": 4,            # A NemRequestResult can have serveral meanings. 
                          # The type indicates the meaning. 
	                      # type 4 - TYPE_STATUS, the code and the message fields 
	                      # have to be interpreted as status.
	"message": "status"
}
```


### TransferSendRequest

```
{
	"wallet": "PrivateWallet",
	"password": "Very Secret Password To The Wallet",
	"account" : "TCN33UYH7OREBBFPA4D7GAE6TALNAZJOF6ZPDVA2",
	"recipient": "TBYDNCLNEGESF5EWRFWFJU7RNCPWOOQH5LMRSQ5A",
	"amount":  10000.0,
	"fee": 1.0,
	"message": "My first message",
	"encrypt": 1,
	"hours_due": 18
}
```

### TransferViewModel

```
{
	"id": "19283746535271819",
	"hash": "1e9d8feec7206ccc3df254bde2a98372b61eef56ac5d907479bc8cc78aa8d1bd",	#
	"sender" : "TCN33UYH7OREBBFPA4D7GAE6TALNAZJOF6ZPDVA2",
	"timeStamp" : 1234211,
	"fee": 1.0,
	"recipient": "TBYDNCLNEGESF5EWRFWFJU7RNCPWOOQH5LMRSQ5A",
	"amount":  10000.0,
	"message": "My first message",												# optional, only if message available
	"encrypt": 1,																# optional, 1 - encrypted
																				# 0 - clear text
	"confirmed" : 1,
	"confirmations" : 1440,
	"blockHeight" : 80765,
	"direction": 0																# 1 - incoming, 
																				# 2 - outgoing, 
																				# 3 - self
}
```

### PartialTransferInformationRequest

```
{
	"recipient": "TBYDNCLNEGESF5EWRFWFJU7RNCPWOOQH5LMRSQ5A",
	"amount":  10000.0,
	"message": "My first message",
	"encrypt": 1,
}
```

### PartialTransferInformationViewModel

```
{
	"fee": 1.0,               # reflects the minimum fee that is required 
	                          # to send the transaction
	"encryptionSupported" : 1 # indicates whether an encrypted message can be sent
	                          # to the recipient (if provided)
	                          # 0 - encryption is not supported
	                          # 1 - encryption is supported
}
```

### WalletName

```
{
	"wallet": "PrivateWallet"
}
```

### WalletNamePasswordBag

```
{
	"wallet": "PrivateWallet",
	"password": "Very Secret Password To The Wallet",
	"new_password": "NEW very secret Password",   # optional, required for password change
	"new_name": "P-Wallet"         ,   # optional, required for wallet name change
	"label": "my private label"    ,   # optional, used during account creation and label change   
	"account" : "TCN33UYH7OREBBFPA4D7GAE6TALNAZJOF6ZPDVA2"
	                                   # optional, required for remove account, label change
}
```

### WalletNamePasswordPair

```
{
	"wallet": "PrivateWallet",
	"password": "Very Secret Password To The Wallet, can contain ÄÖÜäöüß"
}
```

### WalletViewModel

```
{
	"otherAccounts": [],
	"lastRefresh": 1408796675000,
	"name": "Test",
	"primaryAccount": {
		"address": "TCN33UYH7OREBBFPA4D7GAE6TALNAZJOF6ZPDVA2",
		"balance": 0,
		"importance": 0.0,
		"label": null,
		"publicKey": "03c55bd250e56c292ed4c898b0883676313283251d21b6a9099bb989db99d736d2",
		"foragedBlocks": 0,
		"status": "LOCKED",
		"remoteStatus": "INACTIVE"
	}
}
```

