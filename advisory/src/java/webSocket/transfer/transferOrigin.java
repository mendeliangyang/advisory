/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket.transfer;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.websocket.Session;

/**
 *
 * @author Administrator
 */
public class transferOrigin {
     //两个session 队列。一个open添加的队列
     public final static Set<Session> openSesion = Collections.synchronizedSet(new HashSet<Session>());
    //不考虑这种情况，等连接都认为登陆成功 一个 signIn 验证的队列 。openSession验证通过就会在openSession中移除该队列然后添加到 signIn队列
     public final static Map<ChatRoomModel, Set<ChatRoomMemberModel>> chatRooms = Collections.synchronizedMap(new HashMap<ChatRoomModel, Set<ChatRoomMemberModel>>());
}
