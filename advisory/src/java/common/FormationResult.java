/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import common.model.ExecuteResultParam;
import common.model.ResponseResultCode;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import webSocket.transfer.utile.ParamDeployKey;

/**
 *
 * @author Administrator
 */
public class FormationResult {

    /**
     *
     * @param resultCode
     * @param resultParam
     * @return
     */
    public String formationResult(ResponseResultCode resultCode, ExecuteResultParam resultParam) {
        return this.formationResult(resultCode, null, null, resultParam);
    }

    public String formationResult(ResponseResultCode resultCode, String resultParam) {
        return this.formationResult(resultCode, null, null, new ExecuteResultParam(resultParam, null));
    }

    /**
     *
     * @param resultCode
     * @param token
     * @param pushId
     * @param resultParam
     * @return
     */
    public String formationResult(ResponseResultCode resultCode, String token, String pushId, ExecuteResultParam resultParam) {

        JSONObject resultJson = new JSONObject();
        JSONObject resultHeadContext = new JSONObject();

        resultHeadContext.accumulate("resultCode", resultCode.toString());
        resultHeadContext.accumulate("errMsg", resultParam.errMsg);
        if (null != token) {
            resultHeadContext.accumulate("token", token);
        }
        if (null != pushId) {
            resultHeadContext.accumulate("pushId", pushId);
        }

        resultJson.accumulate("head", resultHeadContext);
        resultJson.accumulate("body", resultParam.ResultJsonObject);
        // if error,log information
        if (ResponseResultCode.Success != resultCode) {
            common.RSLogger.ErrorLogInfo(resultParam.executeStr, resultParam.exception);
        }
        return resultJson.toString();
    }

    /**
     *
     * @param resultCode
     * @param errorMsg error message
     * @param operate operate
     * @param jsonObj json
     * @return
     */
    public String formationWSTransferResult(ResponseResultCode resultCode, String errorMsg, String operate, JSONObject jsonObj) {
        JSONObject resultJson = new JSONObject();
        JSONObject resultHeadContext = new JSONObject();

        resultHeadContext.accumulate("resultCode", resultCode.toString());
        resultHeadContext.accumulate("errMsg", errorMsg);
        resultHeadContext.accumulate(ParamDeployKey.paramKey_operate, operate);

        resultJson.accumulate("head", resultHeadContext);
        resultJson.accumulate("body", jsonObj);
        // if error,log information
        if (ResponseResultCode.Success != resultCode) {
            common.RSLogger.wsErrorLogInfo(operate);
        }
        return resultJson.toString();
    }

    public String formationWSTransferResult(ResponseResultCode resultCode, String errorMsg, String operate) {
        JSONObject resultJson = new JSONObject();
        JSONObject resultHeadContext = new JSONObject();

        resultHeadContext.accumulate("resultCode", resultCode.toString());
        resultHeadContext.accumulate("errMsg", errorMsg);
        resultHeadContext.accumulate(ParamDeployKey.paramKey_operate, operate);

        resultJson.accumulate("head", resultHeadContext);
        resultJson.accumulate("body", null);
        // if error,log information
        if (ResponseResultCode.Success != resultCode) {
            common.RSLogger.wsErrorLogInfo(operate);
        }
        return resultJson.toString();
    }

    /**
     *
     * @param resultCode
     * @param errorMsg error message
     * @param operate operate
     * @param jsonObj json
     * @return
     */
    public String formationWSTransferResult(ResponseResultCode resultCode, String errorMsg, String operate, JSONArray jsonObj) {
        JSONObject resultJson = new JSONObject();
        JSONObject resultHeadContext = new JSONObject();

        resultHeadContext.accumulate("resultCode", resultCode.toString());
        resultHeadContext.accumulate("errMsg", errorMsg);
        resultHeadContext.accumulate(ParamDeployKey.paramKey_operate, operate);

        resultJson.accumulate("head", resultHeadContext);
        resultJson.accumulate("body", jsonObj);
        // if error,log information
        if (ResponseResultCode.Success != resultCode) {
            common.RSLogger.wsErrorLogInfo(operate);
        }
        return resultJson.toString();
    }

    /**
     *
     * @param resultCode
     * @param token
     * @param resultParam
     * @return
     */
    public String formationResult(ResponseResultCode resultCode, String token, ExecuteResultParam resultParam) {
        return this.formationResult(resultCode, token, null, resultParam);
    }

}
