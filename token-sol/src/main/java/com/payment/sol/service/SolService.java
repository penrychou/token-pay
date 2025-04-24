package com.payment.sol.service;

import org.p2p.solanaj.rpc.types.Block;

/***
 *
 **/
public interface SolService {
    Long getBlockHeight();

    Block getBlockByNumber(long l);
}
