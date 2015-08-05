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
        return true;
    }

    public static void processMessagePoolExecute(Runnable run) {
        processMessagePool.execute(run);
    }

    public static void saveDataPoolExecute(Runnable run) {
        saveDataPool.execute(run);
    }

}
