/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket.transfer;

import common.DBHelper;
import common.RSLogger;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import webSocket.transfer.utile.ChatRoomModel;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.websocket.Session;
import net.sf.json.JSONArray;
import webSocket.WebSocketHelper;
import webSocket.transfer.utile.ChatRoomMemberModel;
import webSocket.transfer.utile.ParamDeployKey;

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

    public static void initialChatRooms() throws Exception {
        //查询当房间信息保存到 chatRooms
        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;
        ChatRoomModel crm = null;
        ChatRoomMemberModel crmm = null;
        try {
            conn = DBHelper.ConnectSybase(ParamDeployKey.paramKey_rsid);
            stmt = conn.createStatement();
            result = stmt.executeQuery("SELECT * FROM chatRoom");
            crm = new ChatRoomModel();
            while (result.next()) {
                crm.crId = result.getString("crId");
                crm.crName = result.getString("crName");
                crm.uId = result.getString("uId");
                crm.relateId = result.getString("relatedId");

                chatRooms.add(crm);

            }
            result.close();
            result = null;
            for (ChatRoomModel chatRoom : chatRooms) {
                result = stmt.executeQuery(String.format("SELECT * FROM chatRoomMember where crId = '%s' and quitFlag=1", chatRoom.crId));
                chatRoom.crMembers = new HashSet<ChatRoomMemberModel>();
                while (result.next()) {
                    crmm = new ChatRoomMemberModel();
                    crmm.mUId = result.getString("mUId");
                    crmm.inviteUId = result.getString("inviteUId");
                    crmm.crId = result.getString("crId");
                    chatRoom.crMembers.add(crmm);
                }
                result.close();
                result = null;
            }
            result.close();
            result = null;

        } catch (SQLException e) {
            RSLogger.ErrorLogInfo("initialChatRooms error" + e.getLocalizedMessage());

        } finally {
            DBHelper.CloseConnection(result, stmt, conn);
        }
    }

    public static JSONArray getChatRoomsJosn() {
        JSONArray jsonArray = new JSONArray();
        for (ChatRoomModel chatRoom : chatRooms) {
            jsonArray.add(chatRoom.toJson());
        }
        return jsonArray;
    }

    public static JSONArray getChatRoomsJosn(String uId) {
        JSONArray jsonArray = new JSONArray();
        for (ChatRoomModel chatRoom : chatRooms) {
            for (ChatRoomMemberModel crMember : chatRoom.crMembers) {
                if (crMember.mUId.equals(uId)) {
                    jsonArray.add(chatRoom.toJson());
                    break;
                }
            }
        }
        return jsonArray;
    }

    public static void addVerifySession(String key, Session session) {
        synchronized (verifySessions) {
            verifySessions.put(key, session);
        }
    }

    public static void removeVerifySession(String key) {
        synchronized (verifySessions) {
            verifySessions.remove(key);
        }
    }

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
            if (keySet.equals(member.mUId)) {
                return member.session = verifySessions.get(keySet);
            }
        }
        return null;
    }

    public static ChatRoomModel putChatRoomMembers(ChatRoomModel roomModel) {
        if (roomModel == null || roomModel.crMembers == null || roomModel.crMembers.isEmpty()) {
            return null;
        }
        ChatRoomModel tempRoom = null;
        for (ChatRoomModel chatRoom : chatRooms) {
            if (chatRoom.crId.equals(roomModel.crId)) {
                tempRoom = chatRoom;
                break;
            }
        }
        if (tempRoom == null) {
            return null;
        }
        for (ChatRoomMemberModel crMember : roomModel.crMembers) {
            synchronized (chatRooms) {
                tempRoom.crMembers.add(crMember);
            }
        }
        return tempRoom;
    }

    public static ChatRoomModel removeChatRoomMembers(ChatRoomModel roomModel) {
        if (roomModel == null || roomModel.crMembers == null || roomModel.crMembers.isEmpty()) {
            return null;
        }
        ChatRoomModel tempRoom = null;
        for (ChatRoomModel chatRoom : chatRooms) {
            if (chatRoom.crId.equals(roomModel.crId)) {
                tempRoom = chatRoom;
                break;
            }
        }
        if (tempRoom == null) {
            return null;
        }
        Iterator<ChatRoomMemberModel> tempcrMembers = tempRoom.crMembers.iterator();
        ChatRoomMemberModel crmm = null;
        for (ChatRoomMemberModel crMember : roomModel.crMembers) {
            while (tempcrMembers.hasNext()) {
                crmm = tempcrMembers.next();
                if (crmm.mUId.equals(crMember.mUId)) {
                    synchronized (chatRooms) {
                        tempcrMembers.remove();
                    }
                }
            }
        }
        return tempRoom;
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

    public static void SendMsgToChatRoom(String cRoomId, String message) throws IOException {
        ChatRoomModel tempRoom = null;
        for (ChatRoomModel chatRoom : chatRooms) {
            if (chatRoom.crId.equals(cRoomId)) {
                tempRoom = chatRoom;
                break;
            }
        }
        SendMsgToChatRoom(tempRoom, null, message);

    }

    public static void SendMsgToChatRoom(ChatRoomModel cRoom, String message) throws IOException {
        SendMsgToChatRoom(cRoom, null, message);

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
            if (crMember.mUId.equals(uIdSend)) {
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

    public static void SendMsgToSpecial(String uId, String message) throws IOException {
        if (uId == null) {
            return;
        }
        for (String keySet : verifySessions.keySet()) {
            if (keySet.equals(uId)) {
                WebSocketHelper.asyncSendTextToClient((Session) verifySessions.get(keySet), message);
                break;
            }
        }
    }

}
