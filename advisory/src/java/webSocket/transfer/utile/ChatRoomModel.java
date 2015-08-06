/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket.transfer.utile;

import java.util.Set;

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
