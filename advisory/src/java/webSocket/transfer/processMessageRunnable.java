/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket.transfer;

import common.FormationResult;
import common.UtileSmart;
import common.model.ExecuteResultParam;
import common.model.ResponseResultCode;
import webSocket.transfer.utile.wsTransferAnalyzerParam;
import webSocket.transfer.utile.wsTransferMessageModel;
import webSocket.transfer.utile.ChatRoomModel;
import java.util.HashMap;
import webSocket.WebSocketHelper;
import webSocket.transfer.utile.ParamDeployKey;

/**
 *
 * @author Administrator
 */
public class processMessageRunnable implements Runnable {

    wsTransferMessageModel msgModel = null;
    wsTransferAnalyzerParam analyzerParam = new wsTransferAnalyzerParam();
    transferSyncDB syncDB = new transferSyncDB();
    FormationResult formationResult = new FormationResult();

    public processMessageRunnable(wsTransferMessageModel pMsgModel) {
        msgModel = pMsgModel;
    }

    @Override
    public void run() {
        if (msgModel == null) {
            return;
        }
        String resultStr = null;
        try {
            switch (msgModel.operate) {
                case "createRoom":
                    proess_createRoom(resultStr);
                    break;
                case "invalidRoom":
                    process_invalidRoom(resultStr);
                    break;
                case "putMember":
                    process_putMember();
                    break;
                case "quitMember":
                    process_quitMember();
                    break;
                case "sendMsg":
                    process_sendMsg();
                    break;
                case "sginIn":
                    process_sginIn();
                    break;
                case "sginOut":
                    process_sendMsg();
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            common.RSLogger.wsErrorLogInfo(ex.getLocalizedMessage(), ex);
        }

    }

    public void proess_createRoom(String resultStr) throws Exception {
        msgModel.bodyValues = new HashMap<String, Object>();
        msgModel.bodyValues.put(ParamDeployKey.paramKey_uId, null);
        msgModel.bodyValues.put(ParamDeployKey.paramKey_crName, null);
        msgModel.bodyValues.put(ParamDeployKey.paramKey_relatedId, null);
        msgModel.bodyValues.put(ParamDeployKey.paramKey_crMember, null);
        analyzerParam.wsBaseAnalyzeBodyMap(msgModel);
        ChatRoomModel cRoom = new ChatRoomModel();
        //保存房间信息到数据库 获取新的roomId
        ExecuteResultParam resultParam = syncDB.createRoom(cRoom, msgModel.bodyValues);
        if (resultParam == null || resultParam.ResultCode < 0) {
            //房间创建失败
            resultStr = formationResult.formationWSTransferResult(ResponseResultCode.Error, resultParam != null ? resultParam.errMsg : "unkonw error", msgModel.operate, null);

        } else {
            transferOrigin.AddChatRoom(cRoom);
            resultStr = formationResult.formationWSTransferResult(ResponseResultCode.Success, null, msgModel.operate, null);
        }

        transferOrigin.SendMsgToChatRoom(cRoom, UtileSmart.getStringFromMap(msgModel.bodyValues, ParamDeployKey.paramKey_uId), resultStr);

    }

    public void process_invalidRoom(String resultStr) throws Exception {
        msgModel.bodyValues = new HashMap<String, Object>();
        msgModel.bodyValues.put(ParamDeployKey.paramKey_crId, null);
        analyzerParam.wsBaseAnalyzeBodyMap(msgModel);
        ChatRoomModel cRoom = new ChatRoomModel();
        //保存房间信息到数据库 获取新的roomId
        ExecuteResultParam resultParam = syncDB.createRoom(cRoom, msgModel.bodyValues);
        if (resultParam == null || resultParam.ResultCode < 0) {
            resultStr = formationResult.formationWSTransferResult(ResponseResultCode.Error, resultParam != null ? resultParam.errMsg : "unkonw error", msgModel.operate, null);
        } else {
            transferOrigin.RemoveChatRoomBycrId(UtileSmart.getStringFromMap(msgModel.bodyValues, ParamDeployKey.paramKey_crId));
            resultStr = formationResult.formationWSTransferResult(ResponseResultCode.Success, null, msgModel.operate, null);
        }
        transferOrigin.SendMsgToChatRoom(cRoom, UtileSmart.getStringFromMap(msgModel.bodyValues, ParamDeployKey.paramKey_uId), resultStr);
    }   

    public void process_putMember() {
        
    }

    public void process_quitMember() {
    }

    public void process_sendMsg() {

    }

    public void process_sginIn() {

    }

    public void process_sginOut() {

    }
}
