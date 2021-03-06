package edu.jphoebe.demo.lock;

import cn.auntec.framework.components.util.executor.ThreadPoolUtils;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 蒋时华
 * @date 2021-03-29 14:07:13
 */
public class CyclicBarrierTest {


    public static void simple() {
        int thread = 10;
        final ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtils.newFixedThreadPool(thread);
        CyclicBarrier cyclicBarrier = new CyclicBarrier(thread / 2, () -> {
            System.out.println("aaaa");
        });

        for (int i = 0; i < thread; i++) {
            final int finalI = i;
            threadPoolExecutor.execute(() -> {
                System.out.println(finalI + "。。。。。。准备工作");
                try {
                    cyclicBarrier.await();
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.println(finalI + "完成工作");
            });
        }
        System.out.println("全部准备完毕，开始工作: ");

    }

    public static void main(String[] args) {
        CyclicBarrierTest.simple();
    }

}
