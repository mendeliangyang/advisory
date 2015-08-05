/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket.transfer;

import common.base.commonAnalyzeParam;
import common.model.ParamBaseModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Administrator
 */
public class wsTransferAnalyzerParam extends commonAnalyzeParam {

    public void wsBaseAnalyzer(String param, wsTransferMessageModel msModel) throws Exception {
        if (param == null) {
            throw new Exception("origin param is null");
        }
        if (msModel == null || msModel.bodyValues == null) {
            throw new Exception("target paramModel is null");
        }
        ParamBaseModel baseModel = AnalyzeParamBase(param);
        msModel.operate = baseModel.jsonHead.getString("operate");
        for (String key : msModel.bodyValues.keySet()) {
            if (baseModel.jsonBody.containsKey(key)) {
                Object objTemp = baseModel.jsonBody.get(key);
                if (objTemp instanceof String) {
                    msModel.bodyValues.replace(key, objTemp);
                } else if (objTemp instanceof JSONArray) {
                    msModel.bodyValues.replace(key, JSONArray.fromObject(objTemp).toArray());
                } else if (objTemp instanceof JSONObject) {
                    throw new Exception("not supper jsonobject.");
                } else {
                    throw new Exception("param unknow type.");
                }
            }
        }

    }

    public void wsBaseAnalyzerOperate(String param, wsTransferMessageModel msModel) throws Exception {
        if (param == null) {
            throw new Exception("origin param is null");
        }
        if (msModel == null || msModel.bodyValues == null) {
            throw new Exception("target paramModel is null");
        }
        ParamBaseModel baseModel = AnalyzeParamBase(param);
        msModel.operate = baseModel.jsonHead.getString("operate");
    }

    public void wsBaseAnalyzeBodyMap(String param, wsTransferMessageModel msModel) {
//        if (param == null) {
//            throw new Exception("origin param is null");
//        }
//        if (msModel == null || msModel.bodyValues == null) {
//            throw new Exception("target paramModel is null");
//        }
//        ParamBaseModel baseModel = AnalyzeParamBase(param);
//        for (String key : msModel.bodyValues.keySet()) {
//            if (baseModel.jsonBody.containsKey(key)) {
//                Object objTemp = baseModel.jsonBody.get(key);
//                if (objTemp instanceof String) {
//                    msModel.bodyValues.replace(key, objTemp);
//                } else if (objTemp instanceof JSONArray) {
//                    msModel.bodyValues.replace(key, JSONArray.fromObject(objTemp).toArray());
//                } else if (objTemp instanceof JSONObject) {
//                    throw new Exception("not supper jsonobject.");
//                } else {
//                    throw new Exception("param unknow type.");
//                }
//            }
//        }
    }

}
