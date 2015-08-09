/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket.transfer;

import common.FormationResult;
import common.UtileSmart;
import common.model.ExecuteResultParam;
import javax.websocket.Session;
import common.model.ResponseResultCode;
import webSocket.transfer.utile.wsTransferAnalyzerParam;
import webSocket.transfer.utile.wsTransferMessageModel;
import webSocket.transfer.utile.ChatRoomModel;
import java.util.HashMap;
import webSocket.transfer.utile.ADUserModel;
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
    Session currentSession;

    public processMessageRunnable(String strMsg, Session session) throws Exception {
        msgModel = analyzerParam.wsBaseAnalyzerOperate(strMsg);
        currentSession = session;
    }

    @Override
    public void run() {
        if (msgModel == null) {
            return;
        }
        String resultStr = null;
        try {
            switch (msgModel.operate) {
                case wsTransferOperateDefinite.Operate_createRoom:
                    proess_createRoom(resultStr);
                    break;
                case wsTransferOperateDefinite.Operate_invalidRoom:
                    process_invalidRoom(resultStr);
                    break;
                case wsTransferOperateDefinite.Operate_putMember:
                    process_putMember(resultStr);
                    break;
                case wsTransferOperateDefinite.Operate_quitMember:
                    process_quitMember(resultStr);
                    break;
                case wsTransferOperateDefinite.Operate_sendMsg:
                    process_sendMsg();
                    break;
                case wsTransferOperateDefinite.Operate_signIn:
                    process_sginIn();
                    break;
                case wsTransferOperateDefinite.Operate_signOut:
                    //process_sendMsg();
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            common.RSLogger.wsErrorLogInfo(ex.getLocalizedMessage(), ex);
        }

    }

    private void proess_createRoom(String resultStr) throws Exception {
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
            resultStr = formationResult.formationWSTransferResult(ResponseResultCode.Error, resultParam != null ? resultParam.errMsg : "unkonw error", msgModel.operate);
            webSocket.WebSocketHelper.asyncSendTextToClient(currentSession, resultStr);
        } else {
            transferOrigin.AddChatRoom(cRoom);
            resultStr = formationResult.formationWSTransferResult(ResponseResultCode.Success, null, msgModel.operate, cRoom.toJson());
            transferOrigin.SendMsgToChatRoom(cRoom, resultStr);
        }

    }

    private void process_invalidRoom(String resultStr) throws Exception {
        msgModel.bodyValues = new HashMap<String, Object>();
        msgModel.bodyValues.put(ParamDeployKey.paramKey_crId, null);
        analyzerParam.wsBaseAnalyzeBodyMap(msgModel);
        ChatRoomModel cRoom = new ChatRoomModel();
        //保存房间信息到数据库 获取新的roomId
        ExecuteResultParam resultParam = syncDB.invalidRoom(cRoom, msgModel.bodyValues);
        if (resultParam == null || resultParam.ResultCode < 0) {
            resultStr = formationResult.formationWSTransferResult(ResponseResultCode.Error, resultParam != null ? resultParam.errMsg : "unkonw error", msgModel.operate);
            webSocket.WebSocketHelper.asyncSendTextToClient(currentSession, resultStr);
        } else {
            resultStr = formationResult.formationWSTransferResult(ResponseResultCode.Success, null, msgModel.operate, cRoom.toJson());
            transferOrigin.SendMsgToChatRoom(cRoom.crId, resultStr);
            transferOrigin.RemoveChatRoomBycrId(cRoom.crId);
        }

    }

    private void process_putMember(String resultStr) throws Exception {
        msgModel.bodyValues = new HashMap<String, Object>();
        msgModel.bodyValues.put(ParamDeployKey.paramKey_crId, null);
        msgModel.bodyValues.put(ParamDeployKey.paramKey_crMember, null);
        msgModel.bodyValues.put(ParamDeployKey.paramKey_inviteUId, null);
        analyzerParam.wsBaseAnalyzeBodyMap(msgModel);
        ChatRoomModel cRoom = new ChatRoomModel();
        //保存房间信息到数据库 获取新的roomId
        ExecuteResultParam resultParam = syncDB.putMember(cRoom, msgModel.bodyValues);
        if (resultParam == null || resultParam.ResultCode < 0) {
            resultStr = formationResult.formationWSTransferResult(ResponseResultCode.Error, resultParam != null ? resultParam.errMsg : "unkonw error", msgModel.operate);
            webSocket.WebSocketHelper.asyncSendTextToClient(currentSession, resultStr);
        } else {
            cRoom = transferOrigin.putChatRoomMembers(cRoom);
            resultStr = formationResult.formationWSTransferResult(ResponseResultCode.Success, null, msgModel.operate, cRoom.toJson());
            transferOrigin.SendMsgToChatRoom(transferOrigin.putChatRoomMembers(cRoom), resultStr);
        }
    }

    private void process_quitMember(String resultStr) throws Exception {
        msgModel.bodyValues = new HashMap<String, Object>();
        msgModel.bodyValues.put(ParamDeployKey.paramKey_mUId, null);
        msgModel.bodyValues.put(ParamDeployKey.paramKey_uId, null);
        msgModel.bodyValues.put(ParamDeployKey.paramKey_crId, null);
        analyzerParam.wsBaseAnalyzeBodyMap(msgModel);
        ChatRoomModel cRoom = new ChatRoomModel();
        //保存房间信息到数据库 获取新的roomId
        ExecuteResultParam resultParam = syncDB.quitMember(cRoom, msgModel.bodyValues);
        if (resultParam == null || resultParam.ResultCode < 0) {
            resultStr = formationResult.formationWSTransferResult(ResponseResultCode.Error, resultParam != null ? resultParam.errMsg : "unkonw error", msgModel.operate);
            webSocket.WebSocketHelper.asyncSendTextToClient(currentSession, resultStr);
        } else {
            transferOrigin.removeChatRoomMembers(cRoom);
            cRoom = transferOrigin.removeChatRoomMembers(cRoom);
            if (cRoom == null) {
                //task failed 
                resultStr = formationResult.formationWSTransferResult(ResponseResultCode.Error, "unkonw error, please contact manager. ", msgModel.operate);
                webSocket.WebSocketHelper.asyncSendTextToClient(currentSession, resultStr);
            } else {
                resultStr = formationResult.formationWSTransferResult(ResponseResultCode.Success, null, msgModel.operate, cRoom.toJson());
                //如果是当前退出的是本人，需要发送消息到本人，如果当前退出的不是本人，需要通知退出的那个人消息。 //根据muid 查找对应的 session发送消息
                if (UtileSmart.getStringFromMap(msgModel.bodyValues, ParamDeployKey.paramKey_uId).equals(UtileSmart.getStringFromMap(msgModel.bodyValues, ParamDeployKey.paramKey_mUId))) {
                    //发送消息到本人
                    webSocket.WebSocketHelper.asyncSendTextToClient(currentSession, resultStr);
                } else {
                    //发送消息到指定用户
                    transferOrigin.SendMsgToSpecial(ParamDeployKey.paramKey_mUId, resultStr);
                }

                //发送消息到房间其他人
                transferOrigin.SendMsgToChatRoom(transferOrigin.putChatRoomMembers(cRoom), resultStr);
            }

        }
    }

    private void process_sendMsg() throws Exception {
        msgModel.bodyValues = new HashMap<String, Object>();
        msgModel.bodyValues.put(ParamDeployKey.paramKey_uIdSend, null);
        msgModel.bodyValues.put(ParamDeployKey.paramKey_uIdReceive, null);
        msgModel.bodyValues.put(ParamDeployKey.paramKey_crId, null);
        msgModel.bodyValues.put(ParamDeployKey.paramKey_message, null);
        analyzerParam.wsBaseAnalyzeBodyMap(msgModel);
        String resultStr = null, receiveFlag = null;
        receiveFlag = UtileSmart.tryGetStringFromMap(msgModel.bodyValues, ParamDeployKey.paramKey_crId);
        if (receiveFlag != null) {
            //send message to room , 
            //ChatRoomModel cRoom = new ChatRoomModel();
            return;
        }
        receiveFlag = UtileSmart.tryGetStringFromMap(msgModel.bodyValues, ParamDeployKey.paramKey_uIdReceive);
        if (receiveFlag != null) {
            //send message to single user

            return;
        }

        ChatRoomModel cRoom = new ChatRoomModel();
        //保存房间信息到数据库 获取新的roomId
        ExecuteResultParam resultParam = syncDB.quitMember(cRoom, msgModel.bodyValues);
        if (resultParam == null || resultParam.ResultCode < 0) {
            resultStr = formationResult.formationWSTransferResult(ResponseResultCode.Error, resultParam != null ? resultParam.errMsg : "unkonw error", msgModel.operate);
        } else {
            transferOrigin.RemoveChatRoomBycrId(UtileSmart.getStringFromMap(msgModel.bodyValues, ParamDeployKey.paramKey_crId));
            resultStr = formationResult.formationWSTransferResult(ResponseResultCode.Success, null, msgModel.operate);
        }
        transferOrigin.SendMsgToChatRoom(cRoom, UtileSmart.getStringFromMap(msgModel.bodyValues, ParamDeployKey.paramKey_uId), resultStr);

    }

    private void process_sginIn() throws Exception {
        msgModel.bodyValues = new HashMap<String, Object>();
        msgModel.bodyValues.put(ParamDeployKey.paramKey_uId, null);
        analyzerParam.wsBaseAnalyzeBodyMap(msgModel);
        //openSessions record sgin user
        ADUserModel userModel = transferOrigin.addVerifySession(UtileSmart.getStringFromMap(msgModel.bodyValues, ParamDeployKey.paramKey_uId), currentSession);

        webSocket.WebSocketHelper.asyncSendTextToClient(currentSession, formationResult.formationWSTransferResult(ResponseResultCode.Success, null, msgModel.operate, transferOrigin.getChatRoomsJosn(UtileSmart.getStringFromMap(msgModel.bodyValues, ParamDeployKey.paramKey_mUId))));

        transferOrigin.broadMsgToVerifySession(formationResult.formationWSTransferResult(ResponseResultCode.Error, null, wsTransferOperateDefinite.Operate_signInNotify, userModel.toJson()));
    }

    private void process_sginOut() throws Exception {
        msgModel.bodyValues = new HashMap<String, Object>();
        msgModel.bodyValues.put(ParamDeployKey.paramKey_uId, null);
        analyzerParam.wsBaseAnalyzeBodyMap(msgModel);
        //verifySession中的信息
        if (transferOrigin.removeVerifySessionByUId(UtileSmart.getStringFromMap(msgModel.bodyValues, ParamDeployKey.paramKey_uId))) {
            //transferOrigin.broadMsgToVerifySession(formationResult.formationWSTransferResult(ResponseResultCode.Success, null, wsTransferOperateDefinite.Operate_signOut, transferOrigin.(UtileSmart.getStringFromMap(msgModel.bodyValues, ParamDeployKey.paramKey_mUId)));
        }
    }
}
