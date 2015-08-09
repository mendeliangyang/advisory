/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket.transfer.utile;

import net.sf.json.JSONObject;
import javax.websocket.Session;

/**
 *
 * @author Administrator
 */
public class ADUserModel {

    public String uId = null;
    public String uType = null;
    public String uBranch = null;
    public String uNickName = null;
    public Session session = null;

    public JSONObject toJson() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.accumulate("uId", uId);
        jsonObj.accumulate("uType", uType);
        jsonObj.accumulate("uBranch", uBranch);
        jsonObj.accumulate("uNickName", uNickName);
        return jsonObj;
    }

}
