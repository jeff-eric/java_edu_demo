package edu.jphoebe.demo.forkjoin;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

/**
 * 要想使用Fark—Join，类必须继承
 * RecursiveAction（无返回值）
 * RecursiveTask（有返回值）
 */
public class ForkJoin extends RecursiveAction {
    // 每个"小任务"最多只打印50个数
    private static final int MAX = 50;

    private int start;
    private int end;

    ForkJoin(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    protected void compute() {
        // 当end-start的值小于MAX时候，开始打印
        if ((end - start) < MAX) {
            for (int i = start; i < end; i++) {
                System.out.println(Thread.currentThread().getName() + "的i值:"
                        + i);
            }
        } else {
            // 将大任务分解成两个小任务
            int middle = (start + end) / 2;
            ForkJoin left = new ForkJoin(start, middle);
            ForkJoin right = new ForkJoin(middle, end);
            // 并行执行两个小任务
            left.fork();
            right.fork();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 创建包含Runtime.getRuntime().availableProcessors()返回值作为个数的并行线程的ForkJoinPool
        ForkJoinPool forkJoinPool = new ForkJoinPool(6);
        // 提交可分解的PrintTask任务
        forkJoinPool.submit(new ForkJoin(0, 200));
        // 阻塞当前线程直到 ForkJoinPool 中所有的任务都执行结束
        forkJoinPool.awaitTermination(2, TimeUnit.SECONDS);
        // 关闭线程池
        forkJoinPool.shutdown();
    }
}
