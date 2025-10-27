package com.wasu.osgi.model.hgu01.handler;

import org.json.JSONObject;

/**
 * @author: glmx_
 * @date: 2024/8/27
 * @description:
 */
public interface IHandler {

    Object handle(String method, JSONObject params);
}
