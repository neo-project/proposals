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
        /// </summary>
        /// <param name="originator">
        ///   The transaction initiator's public key.
        /// </param>
        /// <param name="Event">
        ///   The NEP5 Event being invoked.
        /// </param>
        /// <param name="args0">
        ///   Optional input parameters used by the NEP5 functions.
        /// </param>

        public static object Main(byte[] originator, string Event, params object[] args)
        {
           BigInteger supply = 1000000;
           string name = "Name";
           string symbol = "NME";
           BigInteger decimals = 1;
           
           //Verify that the originator is honest.
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
        /// </summary>
        /// <param name="originator">
        ///   The contract invoker.
        /// </param>
        /// <param name="supply">
        ///   The supply of tokens to deploy.
        /// </param>
        /// <returns>
        ///   Transaction Successful?
        /// </returns>
        private static bool Deploy(byte[] originator, BigInteger supply)
        {
            //Define the admin public key in byte format
            // Reference: https://github.com/neo-project/docs/blob/master/en-us/sc/tutorial/Lock2.md
            var adminKey = new byte[] {};

            if (originator != adminKey) return false;
            
            //deploy the tokens to the admin
            Storage.Put(Storage.CurrentContext, originator, IntToBytes(supply));
            return true;
        }


        /// <summary>
        ///   Transfer a balance to another account.
        /// </summary>
        /// <param name="originator">
        ///   The contract invoker.
        /// </param>
        /// <param name="to">
        ///   The account to transfer to.
        /// </param>
        /// <param name="amount">
        ///   The amount to transfer.
        /// </param>
        /// <returns>
        ///   Transaction Successful?
        /// </returns>
        private static bool Transfer(byte[] originator, byte[] to, BigInteger amount)
        {           
            //Get the account value of the source and destination accounts.
            var originatorValue = Storage.Get(Storage.CurrentContext, originator);
            var targetValue = Storage.Get(Storage.CurrentContext, to);
            
            
            BigInteger nOriginatorValue = BytesToInt(originatorValue) - amount;
            BigInteger nTargetValue = BytesToInt(targetValue) + amount;
            
            //If the transaction is valid, proceed.
            if (nOriginatorValue >= 0 &&
                 amount >= 0)
            {
                Storage.Put(Storage.CurrentContext, originator, IntToBytes(nOriginatorValue));
                Storage.Put(Storage.CurrentContext, to, IntToBytes(nTargetValue));
                transferred(originator, to, amount);
                return true;
            }
            return false;
        }


        /// <summary>
        ///   Transfers a balance from one account to another
        ///   on behalf of the account owner.
        /// </summary>
        /// <param name="originator">
        ///   The contract invoker.
        /// </param>
        /// <param name="from">
        ///   The account to transfer a balance from.
        /// </param>
        /// <param name="to">
        ///   The account to transfer a balance to.
        /// </param>
        /// <param name="amount">
        ///   The amount to transfer.
        /// </param>
        /// <returns>
        ///   Transaction successful?
        /// </returns>
        private static bool TransferFrom(byte[] originator, byte[] from, byte[] to, BigInteger amount)
        {
            //Load the information for the requested transaction from field storage
            var allValInt = BytesToInt(Storage.Get(Storage.CurrentContext, from.Concat(originator)));
          
            //If the transaction is valid, proceed.
            if (allValInt >= amount)
            {   
                if (transfer(from, to, amount))
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
        /// </summary>
        /// <param name="originator">
        ///   The contract invoker.
        /// </param>
        /// <param name="to">
        ///   The account to grant TransferFrom access to.
        /// </param>
        /// <param name="amount">
        ///   The amount to grant TransferFrom access for.
        /// </param>
        /// <returns>
        ///   Transaction Successful?
        /// </returns>
        private static bool Approve(byte[] originator, byte[] to, byte[] amount)
        {
            Storage.Put(Storage.CurrentContext, originator.Concat(to), amount);
            return true;
        }
       
 
        /// <summary>
        ///   Checks the TransferFrom approval of two accounts.
        /// </summary>
        /// <param name="from">
        ///   The account which funds can be transfered from.
        /// </param>
        /// <param name="to">
        ///   The account which is granted usage of the account.
        /// </param>
        /// <returns>
        ///   The amount allocated for TransferFrom.
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
