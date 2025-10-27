# **NEP: Custom Contract Fees Mechanism**

**NEP**: TBD
**Title:** Custom Contract Fees with ABI Declaration and Runtime Enforcement
**Author:** Chris Schuchardt <cschuchardt88@gmail.com>
**Type:** Standard
**Status:** Draft
**Created:** 2025-10-27

---

## **Abstract**

This NEP introduces a standardized mechanism for smart contracts to declare **custom execution fees** in addition to standard network gas. These fees are paid by the caller to the contract owner (or designated beneficiary) upon successful invocation. The fee structure is declared in the contract's ABI using a new `"fee"` metadata field, supported by updated DevPack tooling and enforced by the Neo VM at runtime.

---

## **Motivation**

Many dApps require **micro-transactions**, **subscription models**, or **usage-based billing** directly within smart contracts. While GAS covers network costs, there is no native way to charge **contract-level fees** in NEO or NEP-17 tokens. This NEP enables:

- Pay-per-use contracts
- Premium feature access
- Decentralized SaaS models
- Fair compensation for contract maintainers

---

## **Specification**

### 1. **ABI Extension: Fee Declaration**

A new optional `"fee"` object is added to method definitions in the ABI.

```json
{
  "methods": [
    {
      "name": "queryData",
      "parameters": [...],
      "returnType": "String",
      "offset": 123,
      "safe": true,
      "fee": {
        "asset": "NEO" | "GAS" | "0x..." (NEP-17 hash),
        "amount": 100000000,  // 8 decimals (1.0 fixed-point)
        "beneficiary": "NM7g6DAeN3Nx8iK53GqY7fpeyqX5bUq5fJ",  // optional, defaults to contract owner
        "mode": "fixed" | "dynamic",  // fixed = static, dynamic = computed at runtime
        "dynamicScriptHash": "0x..."   // required if mode=dynamic
      }
    }
  ]
}
```

#### Field Details:

| Field | Type | Required | Description |
|------|------|----------|-----------|
| `asset` | string | Yes | `"NEO"`, `"GAS"`, or NEP-17 contract hash |
| `amount` | integer | Yes (if `mode=fixed`) | Amount in smallest unit (8 decimals) |
| `beneficiary` | string | No | Address to receive fee. Defaults to contract owner |
| `mode` | string | Yes | `"fixed"` or `"dynamic"` |
| `dynamicScriptHash` | string | Yes (if `mode=dynamic`) | Contract that computes fee via `calculateFee(...)` |

> **Note**: Dynamic fees allow complex logic (e.g., tiered pricing, time-based rates).

---

### 2. **Dynamic Fee Computation Interface**

Contracts declaring `mode: "dynamic"` must reference a fee calculator contract implementing:

```csharp
public interface IFeeCalculator
{
    BigInteger CalculateFee(ByteString method, object[] args);
}
```

- Called by VM before execution
- Must be **Safe** (no state changes)
- Returns fee in smallest unit

---

### 3. **Decorators**

#### C# Attributes

```csharp
[Fee(Asset = "GAS", Amount = 50000000, Mode = FeeMode.Fixed)] // 0.5 GAS
[Fee(Asset = "0xd2a4cff31913016155e38e474a2c06d08be276cf", Amount = 100, Mode = FeeMode.Fixed)]
public static string QueryData(string key) { ... }

[Fee(
    Asset = "GAS",
    Mode = FeeMode.Dynamic,
    Calculator = "0xb2a4cff31913016155e38e474a2c06d08be296cf"
)]
public static UInt160 GetReport(uint userId) { ... }
```

#### DevPack Export Logic

- `[Fee]` attribute is parsed during ABI generation
- Populates `fee` field in method ABI
- Validates:
  - Asset is valid (NEO, GAS, or deployed NEP-17)
  - `Calculator` implements `IFeeCalculator` if `mode=dynamic`
- Emits warning if fee > 10 GAS (configurable)

---

### 4. **Runtime Enforcement (Neo VM)**

#### Invocation Flow

```text
1. Caller invokes contract.method(args)
2. VM loads ABI
3. If method has .fee:
   a. If fixed → deduct exact amount from caller
   b. If dynamic → invoke calculator.CalculateFee(method, args)
        → deduct returned amount
4. Transfer fee to beneficiary (or contract owner)
5. Proceed with execution
6. On failure → refund fee (but not network GAS)
```

#### Safety Guarantees

- **Atomicity**: Fee deduction and transfer are atomic with execution
- **Reentrancy-safe**: Uses `ExecutionEngine` snapshot
- **No double-spend**: Fee checked before execution
- **Refund on revert**: Fee returned if contract throws

#### Syscall Additions

```csharp
// Pseudo-syscalls (internal)
bool ChargeFee(UInt160 asset, BigInteger amount, UInt160 beneficiary);
BigInteger QueryDynamicFee(UInt160 calculator, string method, object[] args);
```

---

### 5. **Contract Owner & Beneficiary**

- Contract owner = deployer address (stored in manifest)
- `beneficiary` overrides owner per method
- If omitted → owner

---

## **Security Considerations**

- **DoS via high dynamic fees**: Mitigated by gas cap on calculator
- **Calculator griefing**: Limit execution to 100k gas
- **Fee spoofing**: ABI is signed in manifest
- **Replay attacks**: Nonce not needed (fee per call)

---

## **Backwards Compatibility**

- Existing contracts without `[Fee]` → no fee charged
- Old DevPack versions → ignore attribute
- VM ignores unknown ABI fields

---

## **Appendix**

### Example Contract

```csharp
public class MyContract : SmartContract
{
    [Fee(Asset = "GAS", Amount = 100000000, Beneficiary = "NM7...")] // 1 GAS
    public static string GetPrice(string symbol) { ... }

    [Fee(Asset = "0xabc...", Mode = FeeMode.Dynamic, Calculator = "0xb2a4cff31913016155e38e474a2c06d08be296cf")]
    public static UInt160 GetReport(uint tier) { ... }
}
```