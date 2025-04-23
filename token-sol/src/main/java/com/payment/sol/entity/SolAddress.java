package com.payment.sol.entity;

import lombok.Data;

/***
 *
 **/
@Data
public class SolAddress {

    private String address;

    private String keystoreName;

    private String mnemonic;
}
