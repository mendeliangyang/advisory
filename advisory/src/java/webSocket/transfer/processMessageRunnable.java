/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket.transfer;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Administrator
 */
public class processMessageRunnable implements Runnable {

    wsTransferMessageModel msgModel = null;

    public processMessageRunnable(wsTransferMessageModel pMsgModel) {
        msgModel = pMsgModel;
    }

    @Override
    public void run() {
        if (msgModel == null) {
            return;
        }
        switch (msgModel.operate) {
            case "createRoom":
                proess_createRoom();
                break;
            case "invalidRoom":
                process_invalidRoom();
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

    }

    public void proess_createRoom() {
        
//        msgModel.bodyValues
        
        //保存房间信息到数据库
        ChatRoomModel room = new ChatRoomModel();
        Set<ChatRoomMemberModel> roomMember = new HashSet<ChatRoomMemberModel>();
        roomMember.add(null);
        //会话队列创建新的room
        synchronized (transferOrigin.chatRooms) {
            transferOrigin.chatRooms.put(room, roomMember);
        }
    }

    public void process_invalidRoom() {
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
