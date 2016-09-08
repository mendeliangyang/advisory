/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket.transfer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Administrator
 */
public class transferThreadPool {

    private static ExecutorService processMessagePool;
    private static ExecutorService saveDataPool;

    public static boolean initialTheadPool() {
        processMessagePool = Executors.newCachedThreadPool();// Executors.newFixedThreadPool(10);//
        saveDataPool = Executors.newFixedThreadPool(4);
        //wsSingleThreadWrite = Executors.newSingleThreadExecutor();
        common.RSLogger.SetUpLogInfo("transfer websocket service initial success.");
        return true;
    }

    public static void processMessagePoolExecute(Runnable run) {
        processMessagePool.execute(run);
    }

    public static void saveSingleMessageExecute(final String uIdReceive,final String uIdSend,final String message) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                transferSyncDB.saveSingleMessage(uIdReceive, uIdSend, message);
            }
        };
        saveDataPool.execute(run);
    }

    public static void saveRoomMessageExecute(final String crId,final String uIdSend,final String message) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                transferSyncDB.saveRoomMessage(crId, uIdSend, message);
            }
        };
        saveDataPool.execute(run);
    }

}
