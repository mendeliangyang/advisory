/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket.transfer.utile;

import javax.websocket.Session;
import net.sf.json.JSONObject;

/**
 *
 * @author Administrator
 */
public class ChatRoomMemberModel {

    // public ADUserModel adUser = null;
    public String mUId = null;
    public Session session = null;
    public String inviteUId = null;
    public String crId = null;

    public JSONObject toJson() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.accumulate("crId", crId);
        jsonObj.accumulate("mUId", mUId);
        jsonObj.accumulate("inviteUId", inviteUId);
        return jsonObj;
    }

    public void destory() {
        // adUser = null;
        mUId = null;
        session = null;
        inviteUId = null;
        crId = null;
    }
}
