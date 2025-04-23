package com.payment.core.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 区块高度同步记录
 */
@Data
@Entity
@Table(name = "pay_height")
public class Height implements Serializable {

    @Id
    private Long currencyId;

    private String currencyName;

    private Integer currentHeight;

    private Date updatedAt;
}
