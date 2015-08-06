/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket.transfer;

import java.io.IOException;
import webSocket.transfer.utile.ChatRoomModel;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.websocket.Session;
import webSocket.WebSocketHelper;
import webSocket.transfer.utile.ChatRoomMemberModel;

/**
 *
 * @author Administrator
 */
public class transferOrigin {

    //两个session 队列。一个open添加的队列
    public final static Set<Session> openSesions = Collections.synchronizedSet(new HashSet<Session>());
    //不考虑这种情况，等连接都认为登陆成功 一个 signIn 验证的队列 。openSession验证通过就会在openSession中移除该队列然后添加到 signIn队列
    public final static Map<String, Session> verifySessions = Collections.synchronizedMap(new HashMap<String, Session>());
    //当前房间的集合
    public final static Set<ChatRoomModel> chatRooms = Collections.synchronizedSet(new HashSet<ChatRoomModel>());

    public static void AddChatRoom(ChatRoomModel room) {
        synchronized (chatRooms) {
            chatRooms.add(room);
        }
    }

    public static void RemoveChatRoomBycrId(String crid) {
        ChatRoomModel tempRoom = null;
        for (ChatRoomModel chatRoom : chatRooms) {
            if (chatRoom.crId.equals(crid)) {
                tempRoom = chatRoom;
                break;
            }
        }
        if (tempRoom == null) {
            return;
        }
        synchronized (chatRooms) {
            chatRooms.remove(tempRoom);
        }
        tempRoom.destorySelf();
        tempRoom = null;
    }

    public static Session getVerifySessionByUId(String uId) {
        for (String keySet : verifySessions.keySet()) {
            if (keySet.equals(uId)) {
                return verifySessions.get(keySet);
            }
        }
        return null;
    }

    public static Session getVerifySessionByChatRoomMember(ChatRoomMemberModel member) {
        for (String keySet : verifySessions.keySet()) {
            if (keySet.equals(member.uId)) {
                return member.session = verifySessions.get(keySet);
            }
        }
        return null;
    }

    /**
     *
     * @param cRoomId
     * @param uIdSend
     * @param message
     * @throws java.io.IOException
     */
    public static void SendMsgToChatRoom(String cRoomId, String uIdSend, String message) throws IOException {
        ChatRoomModel tempRoom = null;
        for (ChatRoomModel chatRoom : chatRooms) {
            if (chatRoom.crId.equals(cRoomId)) {
                tempRoom = chatRoom;
                break;
            }
        }
        SendMsgToChatRoom(tempRoom, uIdSend, message);

    }

    /**
     *
     * @param cRoom
     * @param uIdSend
     * @param message
     * @throws java.io.IOException
     */
    public static void SendMsgToChatRoom(ChatRoomModel cRoom, String uIdSend, String message) throws IOException {
        if (cRoom == null || cRoom.crMembers == null) {
            return;
        }
        for (ChatRoomMemberModel crMember : cRoom.crMembers) {
            if (crMember.session == null) {
                getVerifySessionByChatRoomMember(crMember);
            }
            if (crMember.uId.equals(uIdSend)) {
                break;
            }
            if (crMember.session != null) {
                WebSocketHelper.asyncSendTextToClient(crMember.session, message);
            }
        }

    }

    /**
     *
     * @param member
     * @param message
     * @throws java.io.IOException
     */
    public static void SendMsgToMember(ChatRoomMemberModel member, String message) throws IOException {
        if (member == null) {
            return;
        }
        if (member.session == null) {
            getVerifySessionByChatRoomMember(member);
        }

        if (member.session != null) {
            WebSocketHelper.asyncSendTextToClient(member.session, message);
        }

    }

}
