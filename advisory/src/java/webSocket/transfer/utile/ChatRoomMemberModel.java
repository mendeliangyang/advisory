/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket.transfer.utile;

import javax.websocket.Session;

/**
 *
 * @author Administrator
 */
public class ChatRoomMemberModel {

    public ADUserModel adUser = null;
    public String uId = null;
    public Session session = null;
    public String inviteUId = null;

    public void destory() {
        adUser = null;
        uId = null;
        session = null;
        inviteUId = null;
    }
}
