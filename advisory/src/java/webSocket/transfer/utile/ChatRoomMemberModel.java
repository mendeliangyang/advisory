/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket.transfer.utile;

import javax.websocket.Session;
import net.sf.json.JSONObject;
import webSocket.transfer.transferOrigin;

/**
 *
 * @author Administrator
 */
public class ChatRoomMemberModel {

    public ADUserModel adUser = null;
    public String mUId = null;
    public Session session = null;
    public String inviteUId = null;
    public String crId = null;

    public JSONObject toJson() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.accumulate("crId", crId);
        jsonObj.accumulate("mUId", mUId);
        jsonObj.accumulate("inviteUId", inviteUId);
        if (adUser == null) {
            //first  find userDetail from verifySession by mUId if no exist. find to db.
            adUser = transferOrigin.searchUserDetailFromVerifySessionAndDB(mUId);
        }
        jsonObj.accumulate("UserDetail", adUser == null ? null : adUser.toJson());
        return jsonObj;
    }

    public void destory() {
        adUser = null;
        mUId = null;
        session = null;
        inviteUId = null;
        crId = null;
    }
}
