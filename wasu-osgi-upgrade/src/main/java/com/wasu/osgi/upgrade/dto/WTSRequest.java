package com.wasu.osgi.upgrade.dto;

import com.wasu.osgi.upgrade.config.CommonConstant;
import com.wasu.osgi.upgrade.util.Sign;
import lombok.Data;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author: glmx_
 * @date: 2024/9/3
 * @description:
 */
@Data
public class WTSRequest<T> implements Serializable {
    private SystemReq system;
    private T params;

    public WTSRequest<T> create(T params) {
        WTSRequest<T> req = new WTSRequest<>();
        SystemReq systemReq = new SystemReq();
        req.setSystem(systemReq);
        req.setParams(params);
        systemReq.setSign(Sign.generateSign(new JSONObject(req).toString(), CommonConstant.CLIENT_SECRET));
        return req;
    }

    @Data
    public static class SystemReq implements Serializable {
        private String clientId = CommonConstant.CLIENT_ID;
        private String requestId = UUID.randomUUID().toString();
        private String sign;
        private String apiVersion = "4";
        private String signVersion = "4";
        private String ts = String.valueOf(System.currentTimeMillis());
    }
}
