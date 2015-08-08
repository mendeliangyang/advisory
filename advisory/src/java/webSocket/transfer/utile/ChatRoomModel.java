/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket.transfer.utile;

import java.util.Set;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Administrator
 */
public class ChatRoomModel {

    public String crId = null;
    public String crName = null;
    public String uId = null;
    public String relateId = null;

    public Set<ChatRoomMemberModel> crMembers = null;

    public JSONObject toJson() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.accumulate("crId", crId);
        jsonObj.accumulate("crName", crName);
        jsonObj.accumulate("uId", uId);
        jsonObj.accumulate("relateId", relateId);
        JSONArray jsonArr = new JSONArray();
        if (crMembers != null) {
            for (ChatRoomMemberModel crMember : crMembers) {
                jsonArr.add(crMember.toJson());
            }
        }
        jsonObj.accumulate("members", jsonArr);
        return jsonObj;

    }

    public void destorySelf() {
        if (crMembers != null) {
            for (ChatRoomMemberModel crMember : crMembers) {
                crMember.destory();
            }
            crMembers.clear();
        }
        crMembers = null;
        crId = null;
        crName = null;
        uId = null;
        relateId = null;

    }

}
