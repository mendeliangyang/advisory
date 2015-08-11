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
import webSocket.transfer.utile.ADUserModel;
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
    public final static Set<ADUserModel> verifySessions = Collections.synchronizedSet(new HashSet<ADUserModel>());
    //当前房间的集合
    public final static Set<ChatRoomModel> chatRooms = Collections.synchronizedSet(new HashSet<ChatRoomModel>());

    /**
     * 初始化 ，聊天系统在，开始工作前，在数据库中获取已经存在的房间信息
     *
     * @throws Exception
     */
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

            while (result.next()) {
                crm = new ChatRoomModel();
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
            if (result != null) {

                result.close();
                result = null;
            }
            common.RSLogger.SetUpLogInfo("transfer websocket service read chatRoom detail success.");
        } catch (SQLException e) {
            RSLogger.ErrorLogInfo("initialChatRooms error" + e.getLocalizedMessage());

        } finally {
            DBHelper.CloseConnection(result, stmt, conn);
        }
    }

    /**
     * 获取当前在线成员信息
     *
     * @return
     */
    public static JSONArray getCurrentOnlineUserDetail() {
        JSONArray jsonArray = new JSONArray();
        for (ADUserModel keySet : verifySessions) {
            if (keySet.session != null) {
                jsonArray.add(keySet.toJson());
            }
        }
        return jsonArray;
    }

    /**
     * get user detail from db
     *
     * @param uId
     * @return
     */
    public static ADUserModel getUserDetailFromDB(String uId) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;
        ADUserModel userModel = null;
        try {
            if (uId == null || uId.isEmpty()) {
                return null;
            }
            conn = DBHelper.ConnectSybase(ParamDeployKey.paramKey_rsid);
            stmt = conn.createStatement();
            result = stmt.executeQuery(String.format("SELECT uId,uType,uBranch,uNickName FROM dbo.AdUser where uId = '%s'", uId));
            if (result.next()) {
                userModel = new ADUserModel();
                userModel.uId = result.getString("uId");
                userModel.uBranch = result.getString("uBranch");
                userModel.uNickName = result.getString("uNickName");
                userModel.uType = result.getString("uType");
            }
            result.close();
            result = null;
            return userModel;
        } catch (SQLException e) {
            RSLogger.wsErrorLogInfo("get user Infomation from db error" + e.getLocalizedMessage());
            return null;
        } catch (Exception ex) {
            RSLogger.wsErrorLogInfo("get dbConnection from dbPool error" + ex.getLocalizedMessage());
            return null;
        } finally {
            DBHelper.CloseConnection(result, stmt, conn);
        }

    }

    /**
     * find userDetail from verifySession by mUId if no exist . find to db.
     *
     * @param uId
     * @return
     */
    public static ADUserModel searchUserDetailFromVerifySessionAndDB(String uId) {
        for (ADUserModel verifySession : verifySessions) {
            if (verifySession.uId.equals(uId)) {
                return verifySession;
            }
        }
        return getUserDetailFromDB(uId);
    }

    /**
     * transform all chatRooms to jsonArray
     *
     * @return
     */
    public static JSONArray getChatRoomsJosn() {
        JSONArray jsonArray = new JSONArray();
        for (ChatRoomModel chatRoom : chatRooms) {
            jsonArray.add(chatRoom.toJson());
        }
        return jsonArray;
    }

    /**
     * get chatRooms json by memberId
     *
     * @param uId
     * @return
     */
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

    /**
     * signIn and send uid , add it to verifySession queue
     *
     * @param key
     * @param session
     * @return
     */
    public static ADUserModel addVerifySession(String key, Session session) {
        ADUserModel userModel = getUserDetailFromDB(key);
        if (userModel == null) {
            //get userInfomation error.
            return null;
        }
        userModel.session = session;
        synchronized (verifySessions) {
            verifySessions.add(userModel);
        }
        return userModel;
    }

    /**
     * sign out ,remove it from verifySession queue
     *
     * @param uId
     */
    public static boolean removeVerifySessionByUId(String uId) {
        synchronized (verifySessions) {
            Iterator keyIterator = verifySessions.iterator();
            while (keyIterator.hasNext()) {
                ADUserModel next = (ADUserModel) keyIterator.next();
                if (next.uId.equals(uId)) {
                    verifySessions.remove(next);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * sign out ,remove it from verifySession queue
     *
     * @param SessionId
     */
    public static ADUserModel removeVerifySessionBySessionId(String SessionId) {
        synchronized (verifySessions) {
            Iterator keyIterator = verifySessions.iterator();
            while (keyIterator.hasNext()) {
                ADUserModel next = (ADUserModel) keyIterator.next();
                if (next.session.getId().equals(SessionId)) {
                    //verifySessions.remove(next);
                    next.session = null;
                    return next;
                }
            }
        }
        return null;
    }

    /**
     * 添加新的房间信息
     *
     * @param room
     */
    public static void AddChatRoom(ChatRoomModel room) {
        synchronized (chatRooms) {
            chatRooms.add(room);
        }
    }

    /**
     * remove chatRoom from chatRooms by chatRoomID
     *
     * @param crid
     */
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

    /**
     * get session from verifySession by uId
     *
     * @param uId
     * @return
     */
    public static Session getVerifySessionByUId(String uId) {
        for (ADUserModel keySet : verifySessions) {
            if (keySet.uId.equals(uId)) {
                return keySet.session;
            }
        }
        return null;
    }

    /**
     * get session from verifySession by chatRoomMember
     *
     * @param member
     * @return
     */
    public static Session getVerifySessionByChatRoomMember(ChatRoomMemberModel member) {
        for (ADUserModel keySet : verifySessions) {
            if (keySet.uId.equals(member.mUId)) {
                return member.session = keySet.session;
            }
        }
        return null;
    }

    /**
     * put chatRoom member
     *
     * @param roomModel
     * @return
     */
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
        for (ADUserModel keySet : verifySessions) {
            if (keySet.uId.equals(uId)) {
                WebSocketHelper.asyncSendTextToClient(keySet.session, message);
                break;
            }
        }
    }

    public static void broadMsgToVerifySession(String message) {
        for (ADUserModel verifySession : verifySessions) {
            WebSocketHelper.asyncSendTextToClient(verifySession.session, message);
        }
    }

    public static void broadMsgToVerifySession( String igonreUId,String message) {
        for (ADUserModel verifySession : verifySessions) {
            if (verifySession.uId.equals(igonreUId)) {
                continue;
            }
            WebSocketHelper.asyncSendTextToClient(verifySession.session, message);
        }
    }

}
