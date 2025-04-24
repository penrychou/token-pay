package com.payment.sol.config;

import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/***
 *
 **/
@Configuration
public class SolClientConfig {

    @Bean
    public RpcClient rpcClient() {
        return new RpcClient(Cluster.DEVNET);
    }
}
