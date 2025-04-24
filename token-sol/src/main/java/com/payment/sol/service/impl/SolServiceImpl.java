package com.payment.sol.service.impl;

import com.payment.sol.service.SolService;
import lombok.extern.slf4j.Slf4j;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.Block;
import org.p2p.solanaj.rpc.types.config.Commitment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/***
 *
 **/
@Service
@Slf4j
public class SolServiceImpl implements SolService {


    @Resource
    private RpcClient rpcClient;

    @Override
    public Long getBlockHeight() {
        try {
            return rpcClient.getApi().getBlockHeight(Commitment.CONFIRMED);
        } catch (RpcException e) {
            log.error("获取区块高度失败", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Block getBlockByNumber(long l) {
        try {
            return rpcClient.getApi().getBlock((int) l);
        } catch (RpcException e) {
            log.error("获取区块高度失败", e);
            throw new RuntimeException(e);
        }
    }


}
