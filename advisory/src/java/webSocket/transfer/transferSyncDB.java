/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket.transfer;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.util.Map;
import common.UtileSmart;
import common.model.ExecuteResultParam;
import common.model.ResponseResultCode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import webSocket.transfer.utile.ChatRoomMemberModel;
import webSocket.transfer.utile.ChatRoomModel;
import webSocket.transfer.utile.ParamDeployKey;

/**
 *
 * @author Administrator
 */
public class transferSyncDB {

    public ExecuteResultParam createRoom(ChatRoomModel cRoom, Map<String, Object> mapValues) throws Exception {

        ExecuteResultParam resultParam = null;

        ChatRoomMemberModel crmm = null;
        List<String> sqlList = null;
        try {
            if (cRoom == null) {
                cRoom = new ChatRoomModel();
            }
            cRoom.crId = UtileSmart.getUUID();
            cRoom.crName = UtileSmart.getStringFromMap(mapValues, ParamDeployKey.paramKey_crName);
            cRoom.uId = UtileSmart.getStringFromMap(mapValues, ParamDeployKey.paramKey_uId);
            cRoom.relateId = UtileSmart.getStringFromMap(mapValues, ParamDeployKey.paramKey_relatedId);
            sqlList = new ArrayList<String>();
            sqlList.add(String.format("insert into chatRoom (crId,crName,uId,createDate,relatedId) values ('%s','%s','%S',getdate(),'%s')", cRoom.crId, cRoom.crName, cRoom.uId, cRoom.relateId));
            cRoom.crMembers = new HashSet<ChatRoomMemberModel>();
            crmm = new ChatRoomMemberModel();
            crmm.uId = cRoom.uId;
            crmm.inviteUId = cRoom.uId;

            sqlList.add(String.format("insert into chatRoomMember (crId,mUId,inviteUId)values('%s','%s','%s')", cRoom.crId, crmm.uId, crmm.inviteUId));

            cRoom.crMembers.add(crmm);
            for (String listFromMap : UtileSmart.getListFromMap(mapValues, ParamDeployKey.paramKey_crMember)) {
                crmm = new ChatRoomMemberModel();
                crmm.uId = listFromMap;
                crmm.inviteUId = cRoom.uId;
                sqlList.add(String.format("insert into chatRoomMember (crId,mUId,inviteUId)values('%s','%s','%s')", cRoom.crId, crmm.uId, crmm.inviteUId));
                cRoom.crMembers.add(crmm);
            }
            resultParam = common.DBHelper.ExecuteSql(ParamDeployKey.paramKey_rsid, sqlList);
            return resultParam;
        } finally {
            UtileSmart.FreeObjects(resultParam, sqlList);
        }
    }

    public ExecuteResultParam invalidRoom(ChatRoomModel cRoom, Map<String, Object> mapValues) throws Exception {
        ExecuteResultParam resultParam = null;

        ChatRoomMemberModel crmm = null;
        List<String> sqlList = null;
        try {
            if (cRoom == null) {
                cRoom = new ChatRoomModel();
            }
            cRoom.crId = UtileSmart.getStringFromMap(mapValues, ParamDeployKey.paramKey_crId);

            sqlList = new ArrayList<String>();
            sqlList.add(String.format("insert into chatRoom_hs select * from chatRoom cr where cr.crId='%s'", cRoom.crId));
            sqlList.add(String.format("insert into chatRoomMember_hs select * from chatRoomMember crm where crm.crId='%s'", cRoom.crId));
            sqlList.add(String.format("insert into chatMessage_hs select * from chatMessage cm where cm.crId='%s'", cRoom.crId));
            sqlList.add(String.format("delete chatRoom cr where cr.crId='%s'", cRoom.crId));
            sqlList.add(String.format("delete chatRoomMember crm where crm.crId='%s'", cRoom.crId));
            sqlList.add(String.format("delete chatMessage cm whre cm.crId='%s'", cRoom.crId));

            resultParam = common.DBHelper.ExecuteSql(ParamDeployKey.paramKey_rsid, sqlList);
            return resultParam;
        } finally {
            UtileSmart.FreeObjects(resultParam, sqlList);
        }
    }

    public ExecuteResultParam putMember(ChatRoomModel cRoom, Map<String, Object> mapValues) throws Exception {
        ExecuteResultParam resultParam = null;
        ChatRoomMemberModel crmm = null;
        List<String> sqlList = null;
        try {
            if (cRoom == null) {
                cRoom = new ChatRoomModel();
            }
            cRoom.crId = UtileSmart.getStringFromMap(mapValues, ParamDeployKey.paramKey_crId);
            cRoom.crMembers = new HashSet<ChatRoomMemberModel>();
            sqlList = new ArrayList<String>();
            for (String listFromMap : UtileSmart.getListFromMap(mapValues, ParamDeployKey.paramKey_crMember)) {
                crmm = new ChatRoomMemberModel();
                crmm.uId = listFromMap;
                crmm.inviteUId = UtileSmart.getStringFromMap(mapValues, ParamDeployKey.paramKey_inviteUId);
                sqlList.add(String.format("insert into chatRoomMember (crId,mUId,inviteUId)values('%s','%s','%s')", cRoom.crId, crmm.uId, crmm.inviteUId));
                cRoom.crMembers.add(crmm);
            }
            resultParam = common.DBHelper.ExecuteSql(ParamDeployKey.paramKey_rsid, sqlList);
            return resultParam;
        } finally {
            UtileSmart.FreeObjects(resultParam, sqlList);
        }
    }

    public ExecuteResultParam quitMember(ChatRoomModel cRoom, Map<String, Object> mapValues) throws Exception {
        ExecuteResultParam resultParam = null;
        ChatRoomMemberModel crmm = null;
        List<String> sqlList = null;
        String tempMUId = null;
        try {
            if (cRoom == null) {
                cRoom = new ChatRoomModel();
            }
            cRoom.crId = UtileSmart.getStringFromMap(mapValues, ParamDeployKey.paramKey_crId);
            tempMUId= UtileSmart.getStringFromMap(mapValues, ParamDeployKey.paramKey_crId);
            
            sqlList = new ArrayList<String>();
            for (String listFromMap : UtileSmart.getListFromMap(mapValues, ParamDeployKey.paramKey_crMember)) {
                crmm = new ChatRoomMemberModel();
                crmm.uId = listFromMap;
                crmm.inviteUId = UtileSmart.getStringFromMap(mapValues, ParamDeployKey.paramKey_inviteUId);
                sqlList.add(String.format("insert into chatRoomMember (crId,mUId,inviteUId)values('%s','%s','%s')", cRoom.crId, crmm.uId, crmm.inviteUId));
                cRoom.crMembers.add(crmm);
            }
            resultParam = common.DBHelper.ExecuteSql(ParamDeployKey.paramKey_rsid, sqlList);
            return resultParam;
        } finally {
            UtileSmart.FreeObjects(resultParam, sqlList);
        }
    }

}
