# NEP: Committee Node Liveness Proof via Lightweight Proof of Work
- NEP: TBA
- Title: Mechanism for Removing Inactive Committee Nodes
- Author: Fernando Diaz Toledano <shargon@gmail.com>
- Discussed in: https://github.com/neo-project/neo/pull/4304
- Type: Standard (Core)
- Status: Draft
- Created: 2025-11-11

## Summary

This NEP proposes a new mechanism to verify the liveness of committee nodes through a **lightweight proof of work (PoW)** based on the hash of previous blocks.  
The goal is to automatically detect and expel nodes that fail to demonstrate activity, ensuring a functional and reliable committee.

## Motivation

In the current protocol, committee nodes may stop participating without immediate detection, causing delays or loss of fault tolerance.  
This NEP introduces a **cryptographic liveness proof**, verifiable by other committee members, allowing inactive nodes to be identified and replaced automatically.

## Specification

Each committee node must periodically prove it is **alive** by performing a **PoW** over the hash of a block `X` blocks in the past.

### 1. Proof Base

- The hash of block `BlockHeight - X` is used as the base.
- Each node must find a nonce such that:

  ```
  SHA256(blockHash || nonce) < Target
  ```

  where `Target` defines the minimum difficulty threshold.

- The result is included in a `LivenessProof` message broadcast to other committee members.

### 2. Automatic Update for Primary

- If the node is **primary**, the height used for computing the proof (`BlockHeight - X`) is automatically updated with every confirmed block.
- Secondary nodes must update it upon receiving a new block or `LivenessRequest` message.

### 3. Verification and Expulsion

Each node validates received proofs. If a member does not submit a valid proof within the `LivenessWindow`, it is marked as **inactive** and automatically expelled from the committee.

Expulsion occurs by consensus during the next committee round, without external intervention.

## Rationale

This approach replaces network-based heartbeat detection with verifiable cryptographic evidence.  
The lightweight PoW ensures minimal computation cost while making falsification or replay infeasible.

## Compatibility

Backward compatible. The proposal introduces no new message types or block structures beyond internal committee validation.

## Reference Implementation

Based on the logic introduced in [neo-project/neo#4304](https://github.com/neo-project/neo/pull/4304), extended with PoW-based liveness checks.

## Security Considerations

The mechanism prevents false reporting and requires no external trust. Only nodes that actively participate and compute valid proofs remain in the committee.

## Conclusion

This NEP enhances NEO network reliability by enforcing continuous liveness verification through a lightweight proof of work, ensuring the committee remains composed solely of active nodes.
