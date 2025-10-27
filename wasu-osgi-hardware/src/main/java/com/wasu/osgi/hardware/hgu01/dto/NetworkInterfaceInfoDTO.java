package com.wasu.osgi.hardware.hgu01.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class NetworkInterfaceInfoDTO implements Serializable {
    private String ifType;
    private String displayname;
    private String ifName;
    private String pid;
    private String ipType;
    private String status;
    private String mac;
    private String mtu;
}