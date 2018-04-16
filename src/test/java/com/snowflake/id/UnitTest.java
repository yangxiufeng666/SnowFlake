package com.snowflake.id;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: Mr.Yangxiufeng
 * Date: 2018-04-16
 * Time: 17:39
 */
public class UnitTest {
    @Test
    public void name() {
        try {
            int times = 0, maxTimes = 1000;
            IDSequence sequence = new IDSequence(0, 0);
            for (int i = 0; i < maxTimes; i++) {
                long id = sequence.nextId();
                if(id%2==0){
                    times++;
                }
                Thread.sleep(10);
            }
            System.out.println("偶数:" + times + ",奇数:" + (maxTimes - times) + "!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testNextId() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(10);
        final HashSet idSet = new HashSet<Object>();
        Collections.synchronizedCollection(idSet);
        long start = System.currentTimeMillis();
        System.out.println("***** start generate id ******");
        for (int i = 0; i < 10; i++)
            es.execute(new Runnable() {
                public void run() {
                    for (int j = 0; j < 5000; j++) {
                        Long id= IdWorker.getIdSequence();
                        System.out.println(id);
                        synchronized (idSet){
                            idSet.add(id);
                        }
                    }
                }
            });
        es.shutdown();
        es.awaitTermination(5, TimeUnit.MINUTES);
        long end = System.currentTimeMillis();
        System.out.println("***** end generate id *****");
        System.out.println("***** cost " + (end-start) + " ms!");
    }
}
