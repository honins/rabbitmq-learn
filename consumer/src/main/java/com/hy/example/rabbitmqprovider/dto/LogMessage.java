package com.hy.example.rabbitmqprovider.dto;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Created by hy
 * @date on 2020/11/23 13:29
 */
@Data
@ToString
public class LogMessage implements Serializable {

    private Long id;
    private String msg;
    private String logLevel;
    private String serviceType;
    private Date createTime;
    private Long userId;

}
