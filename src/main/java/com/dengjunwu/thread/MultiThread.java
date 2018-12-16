package com.dengjunwu.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Created by dengjunwu on 2018/8/24.
 */
@Slf4j
public class MultiThread {
    //total  request
    private static int totalRequest = 20000;
    //accept request
    private static int fixedRequest = 100;

    //resource
    private static int resource = 0;

    public static void main(String[] args) {
        final CountDownLatch countDownLatch
                = new CountDownLatch(totalRequest);
        final Semaphore semaphore
                = new Semaphore(fixedRequest);

        ExecutorService es = Executors.newCachedThreadPool();
        for (int i = 0; i < totalRequest; i++){
            es.execute(() -> {
                try {
                    semaphore.acquire();
                    cal();
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            });
        }

        //wait main thread to print
        try {
            countDownLatch.await();
            es.shutdown();
            log.info("result : {}", resource);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void cal() {
        resource ++;
    }
}
