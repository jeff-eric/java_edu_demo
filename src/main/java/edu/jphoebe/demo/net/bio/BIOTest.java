package edu.jphoebe.demo.net.bio;

import java.io.IOException;
import java.util.Random;

/**
 * Test class
 *
 * @author 蒋时华
 * @date 2019/8/28
 */
public class BIOTest {

    //测试主方法
    public static void main(String[] args) throws InterruptedException {
        //运行服务器
        new Thread(() -> {
            try {
                ServerNormal.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        //避免客户端先于服务器启动前执行代码
        Thread.sleep(100);
        //运行客户端
        char operators[] = {'+', '-', '*', '/'};
        Random random = new Random(System.currentTimeMillis());
        new Thread(() -> {
            while (true) {
                //随机产生算术表达式
                String expression = random.nextInt(10) + "" + operators[random.nextInt(4)] + (random.nextInt(10) + 1);
                Client.send(expression);
                try {
                    Thread.sleep(random.nextInt(1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}