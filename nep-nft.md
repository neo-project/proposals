<pre>
  NEP: <to be assigned>
  Title: non-fungible tokens
  Author: Li Jianying <lightsever@hotmail.com>
  Type: Standard
  Status: Draft
  Created: 2018-04-10
</pre>

==Abstract==

[eng]
此标准提出一种新的资产模型，该类资产不可分割，每一个个体都是独一无二的资产。


==Motivation==

随着NEO的发展，想要建立管理独一无二资产的需要越来越强烈，特提出此标准
As the NEO blockchain scales, Smart Contract deployment and invocation will become increasingly important.  Without a standard interaction method, systems will be required to maintain a unique API for each contract, regardless of their similarity to other contracts.  Tokenized contracts present themselves as a prime example of this need because their basic operating mechanism is the same.  A standard method for interacting with these tokens relieves the entire ecosystem from maintaining a definition for basic operations that are required by every Smart Contract that employs a token.

==Specification==

In the method definitions below, we provide both the definitions of the functions as they are defined in the contract as well as the invoke parameters.

This standard defines two method types:

* '''(Required)''' : methods that are present on all NEP5 tokens.

* '''(Optional)''' : methods that are optionally implemented on NEP5 tokens. These method types are not required for standard interfacing, and most tokens should not use them. All optional methods must be enabled if choosing to use them.

===Methods===


        //nft notify
        public delegate void deleTransfer(TransferType type, byte[] nftid, byte[] from, byte[] to);
        [DisplayName("transfer")]
        public static event deleTransfer Transferred;

====totalSupply====

* Syntax: <code>public static BigInteger totalSupply()</code>

* Remarks: "totalSupply" returns the total token Count deployed in the system.

====name====

* Syntax: <code>public static string name()</code>

* Remarks: "name" returns the token name.

====symbol====

* Syntax: <code>public static string symbol()</code>

* Remarks: "symbol" returns the token symbol.

====transfer====

* Syntax: <code>public static bool transfer(byte[] nftid, byte[] from, byte[] to)</code>

* Remarks: "transfer" will transfer an token's owner from the '''from''' account to the '''to''' account.

每一个nftid都是一个32字节长度的hash值，每一个nftid产生时都必须指定一个byte[]作为数据，data必须是独一无二的，nftid是该data的hash256.
我们对transfer的实现做出约定，

1.transfer 内必须使用checkwitness(from)限制来自from的鉴权信息。

2.transfer 不允许通过跳板合约调用

 <code> if (ExecutionEngine.EntryScriptHash.AsBigInteger() != ExecutionEngine.CallingScriptHash.AsBigInteger())
                        return false;</code>

====transfer_app====

* Syntax: <code>public static bool transfer_app(byte[] nftid, byte[] from, byte[] to)</code>

* Remarks: "transfer" will transfer an token's owner from the '''from''' account to the '''to''' account.

transfer_app 功能和transfer相同，不同之处在于鉴权方式

transfer_app 内必须限制from是智能合约账户，并且由from合约调用本合约的transfer_app。

<code> if (from.AsBigInteger() != ExecutionEngine.CallingScriptHash.AsBigInteger())
                        return false;</code>

如果要做一个智能合约集中管理多个用户的NFT资产，比如交易所，拍卖场。则需要如此实现，将鉴证过程交给应用合约

====getTXInfo====

* Syntax: <code>public static TransferInfo getTXInfo(byte[] txid)</code>

* Remarks: 每一笔transfer 或者transfer_app会留下一条交易记录

<code>

        public class TransferInfo
        {
            public byte[] nftid;
            public byte[] from;
            public byte[] to;
        }
</code>

这个功能实现保证在交易成功后留下交易记录，则将txid可以作为交易凭证提供给另一智能合约。

====getNFTOwner====

* Syntax: <code>public static byte[] getNFTOwner(byte[] nftid)</code>

* Remarks: 返回一个nft资产的所有者

====getNFTData====

* Syntax: <code>public static byte[] getNFTData(byte[] nftid)</code>

* Remarks: 返回一个nft资产的源数据

===Events===

====transfer====

* Syntax: <code>public static event Action<byte[] nftid，byte[] from, byte[] to> transfer</code>

* Remarks: The "transfer" event is raised after a successful execution of the "transfer" or "transfer_app" method.

==Implementation==

*Sample from NEL: https://github.com/NewEconoLab/dapp_nep5/blob/master/nft_contract/Contract1.cs

