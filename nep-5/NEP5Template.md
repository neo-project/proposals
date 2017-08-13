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
        ///   Parameter List: 050505
        ///   Return List: 05
        ///
        ///   参数：050505
        ///   返回值：05
        /// </summary>
        /// <param name="originator">
        ///   The transaction initiator's public key.
        ///   合约调用者的公钥
        /// </param>
        /// <param name="Event">
        ///   The NEP5 Event being invoked.
        ///   所调用的 NEP5 方法
        /// </param>
        /// <param name="args0">
        ///   Optional input parameters used by the NEP5 functions.
        ///   NEP5 方法的参数
        /// </param>

        public static object Main(byte[] originator, string Event, params object[] args)
        {
           BigInteger supply = 1000000;
           string name = "Name";
           string symbol = "NME";
           BigInteger decimals = 1;
           
           //Verify that the originator is honest.
           //确认交易者诚实
           if (!Runtime.CheckWitness(originator)) return false;

           if (Event == "deploy") return Deploy(originator, supply);

           if (Event == "totalSupply") return supply;
 
           if (Event == "name") return name;
 
           if (Event == "symbol") return symbol;

           if (Event == "decimals") return decimals;
           
           if (Event == "balanceOf") return Storage.Get(Storage.CurrentContext, byte [] args[0]);
            
           if (Event == "transfer") return Transfer(originator, byte[] args[0], BytesToInt(byte[] args[1]) );
           
           if (Event == "transferFrom") return TransferFrom(originator, byte[] args[0], byte[] args[1], BytesToInt(byte[] args[2]) );

           if (Event == "approve") return Approve(originator, byte[] args[0], byte[] args[1]);

           if (Event == "allowance") return Allowance(args[0], args[1]);

           return false;
        }

        
        /// <summary>
        ///   Deploys the tokens to the admin account.
        ///   部署以及发行资产给管理员账户
        /// </summary>
        /// <param name="originator">
        ///   The contract invoker.
        ///   合约交易者的公钥
        /// </param>
        /// <param name="supply">
        ///   The supply of tokens to deploy.
        ///   资产数量
        /// </param>
        /// <returns>
        ///   Transaction Successful?
        ///   交易是否成功，布尔值
        /// </returns>
        private static bool Deploy(byte[] originator, BigInteger supply)
        {
            //Define the admin public key in byte format
            //这里填入管理员的公钥
            // Reference: https://github.com/neo-project/docs/blob/master/en-us/sc/tutorial/Lock2.md
            var adminKey = new byte[] {};

            if (originator != adminKey) return false;
            
            //Deploy the tokens to the admin.
            //部署以及发行资产给管理员账户
            Storage.Put(Storage.CurrentContext, originator, IntToBytes(supply));
            return true;
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
                Transferred(originator, to, amount);
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

       
        private static void Transferred(byte[] originator, byte[] to, BigInteger amount)
        {
            Runtime.Log("Transfer Event");
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
