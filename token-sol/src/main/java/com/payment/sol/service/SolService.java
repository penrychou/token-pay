package com.payment.sol.service;

/***
 *
 **/
public interface SolService {
    Long getBlockchainHeight();

    EthBlock.Block getBlockByNumber(long l);

    Transaction getTransactionByHash(String txHash);
}
