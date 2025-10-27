package com.wasu.osgi.hardware.hgu01.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class HwinfoDTO implements Serializable {
    private String chipmodel;
    private String hwmodel;
    private String hwver;
}
