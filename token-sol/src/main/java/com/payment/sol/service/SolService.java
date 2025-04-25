package com.payment.sol.service;

import org.p2p.solanaj.rpc.types.Block;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;

/***
 *
 **/
public interface SolService {
    Long getBlockHeight();

    Block getBlockByNumber(long l);

    ConfirmedTransaction getTransactionByHash(String txHash);
}
