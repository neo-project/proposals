```csharp
using Neo.SmartContract.Framework;
using Neo.SmartContract.Framework.Services.Neo;
using System.Numerics;


namespace NEO_NEP_5 
{
    public class NEO_NEP_5 : FunctionCode
    {

        /// <summary>
        ///   This is the NEP5 smart contract template.
        ///   Please read the comments for instructions 
        ///   on fields that need to be populated.
        /// 
        ///   Parameter List: 0505
        ///   Return List: 05
        ///
        ///   参数：0505
        ///   返回值：05
        /// </summary>
        /// <param name="method">
        ///   The NEP5 Method being invoked.
        ///   所调用的 NEP5 方法
        /// </param>
        /// <param name="args">
        ///   Optional input parameters used by the NEP5 functions.
        ///   NEP5 方法的参数
        /// </param>

        public static object Main(string method, params object[] args)
        {
           BigInteger supply = 1000000;
           string name = "Name";
           string symbol = "NME";
           BigInteger decimals = 1;
           

           if (method == "totalSupply") return supply;
 
           if (method == "name") return name;
 
           if (method == "symbol") return symbol;

           if (method == "decimals") return decimals;
           
           if (method == "balanceOf") return BytesToInt(Storage.Get(Storage.CurrentContext, (byte[]) args[1]));

           //if (method == "allowance") return Allowance(args[1], args[2]);


           //Verify that the originator is honest.
           //确认交易者诚实
           if (!Runtime.CheckWitness((byte[]) args[0])) return false;
            
           if (method == "transfer") return Transfer((byte[]) args[0], (byte[]) args[1], BytesToInt((byte[]) args[2]));
           
           //if (method == "transferFrom") return TransferFrom((byte[]) args[0], (byte[]) args[1], (byte[]) args[2], BytesToInt((byte[]) args[3]));

           //if (method == "approve") return Approve((byte[]) args[0], (byte[]) args[1], (byte[]) args[2]);

           return false;
        }


        /// <summary>
        ///   Transfer a balance to another account.
        ///   转帐
        /// </summary>
        /// <param name="originator">
        ///   The contract invoker.
        ///   合约调用者的公钥
        /// </param>
        /// <param name="to">
        ///   The account to transfer to.
        ///   转帐目标
        /// </param>
        /// <param name="amount">
        ///   The amount to transfer.
        ///   转账数量
        /// </param>
        /// <returns>
        ///   Transaction Successful?
        ///   交易是否成功，布尔值
        /// </returns>
        private static bool Transfer(byte[] originator, byte[] to, BigInteger amount)
        {           
            //Get the account value of the source and destination accounts.
            //获取源和目标账户的余额
            var originatorValue = Storage.Get(Storage.CurrentContext, originator);
            var targetValue = Storage.Get(Storage.CurrentContext, to);
            
            
            BigInteger nOriginatorValue = BytesToInt(originatorValue) - amount;
            BigInteger nTargetValue = BytesToInt(targetValue) + amount;
            
            //If the transaction is valid, proceed.
            //如果交易有效，继续
            if (nOriginatorValue >= 0 &&
                amount >= 0)
            {
                Storage.Put(Storage.CurrentContext, originator, IntToBytes(nOriginatorValue));
                Storage.Put(Storage.CurrentContext, to, IntToBytes(nTargetValue));
                Runtime.Notify("Transfer Successful", originator, to, amount, Blockchain.GetHeight()); 
                return true;
            }
            return false;
        }


        /// <summary>
        ///   Transfers a balance from one account to another
        ///   on behalf of the account owner.
        ///   代表账户所有者转账
        /// </summary>
        /// <param name="originator">
        ///   The contract invoker.
        ///   合约调用者的公钥
        /// </param>
        /// <param name="from">
        ///   The account to transfer a balance from.
        ///   转账源
        /// </param>
        /// <param name="to">
        ///   The account to transfer a balance to.
        ///   转账目标
        /// </param>
        /// <param name="amount">
        ///   The amount to transfer.
        ///   转账数量
        /// </param>
        /// <returns>
        ///   Transaction successful?
        ///   交易是否成功，布尔值
        /// </returns>
        private static bool TransferFrom(byte[] originator, byte[] from, byte[] to, BigInteger amount)
        {
            //Load the information for the requested transaction from field storage
            //从持久化存储区加载的信息
            var allValInt = BytesToInt(Storage.Get(Storage.CurrentContext, from.Concat(originator)));
          
            //If the transaction is valid, proceed.
            //如果交易有效，继续
            if (allValInt >= amount)
            {   
                if (Transfer(from, to, amount))
                {
                    Storage.Put(Storage.CurrentContext, from.Concat(originator), IntToBytes(allValInt - amount));
                    return true;
                }
            }
            return false;
        }

        
        /// <summary>
        ///   Approves another user to use the TransferFrom
        ///   function on the invoker's account.
        ///   批准另一个用户使用调用者户口的 TransferFrom 方法
        /// </summary>
        /// <param name="originator">
        ///   The contract invoker.
        ///   合约调用者
        /// </param>
        /// <param name="to">
        ///   The account to grant TransferFrom access to.
        ///   所批准的账户
        /// </param>
        /// <param name="amount">
        ///   The amount to grant TransferFrom access for.
        ///   所批准的数量
        /// </param>
        /// <returns>
        ///   Transaction Successful?
        ///   交易是否成功，布尔值
        /// </returns>
        private static bool Approve(byte[] originator, byte[] to, byte[] amount)
        {
            Storage.Put(Storage.CurrentContext, originator.Concat(to), amount);
            Runtime.Notify("Approval Successful", originator, to , amount, Blockchain.GetHeight()); 
            return true;
        }
       
 
        /// <summary>
        ///   Checks the TransferFrom approval of two accounts.
        ///   检查两账户的 TransferFrom 批准
        /// </summary>
        /// <param name="from">
        ///   The account which funds can be transfered from.
        ///   能动用的账户
        /// </param>
        /// <param name="to">
        ///   The account which is granted usage of the account.
        ///   被批准的账户
        /// </param>
        /// <returns>
        ///   The amount allocated for TransferFrom.
        ///   被批准的数量
        /// </returns>
        private static BigInteger Allowance(byte[] from, byte[] to)
        {
            return BytesToInt(Storage.Get(Storage.CurrentContext, from.Concat(to)));
        }
        
       
        private static byte[] IntToBytes(BigInteger value)
        {
            byte[] buffer = value.ToByteArray();
            return buffer;
        }
        
  
        private static BigInteger BytesToInt(byte[] array)
        {
            var buffer = new BigInteger(array);
            return buffer;
        }
        
    }
}
```
