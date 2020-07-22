package edu.jphoebe.demo.net.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * buffer
 * 这些方法中大部分是对mark、position、limit、capacity的操作。
 * 对于数组来说，需要以下一些重要元素，比如数组大小（capacity）。
 * 此时如果是对数组的读取操作时，需要表明当前读到了哪个位置（position），总共可以读到哪个位置（limit），也就是当前数组中有几个元素。
 * 此时如果是写操作，那么需要知道现在写到了哪个位置（position），最大可以写到哪个位置（limit）。
 * 最后为了实现可重复读，产生一个备忘位置，即标记（mark）。
 * <p>
 * 直接缓冲区与非直接缓冲区(ByteBuffer)：
 * *  1.非直接缓冲区：
 * 优点：在虚拟机内存中创建，易回收
 * 缺点：但占用虚拟机内存开销，处理中有复制过程。
 * *  2.直接缓冲区：
 * 优点：在虚拟机内存外，开辟的内存，IO操作直接进行,没有再次复制
 * 缺点：创建和销毁开销大，没有管理权(基于系统的物理内存没有分代回收机制)
 * <p>
 * JVM创建一个缓冲区的时候，实际上做了如下几件事：
 * 1.JVM确保Heap区域内的空间足够，如果不够则使用触发GC在内的方法获得空间;
 * 2.获得空间之后会找一组堆内的连续地址分配数组, 这里需要注意的是，在物理内存上，这些字节是不一定连续的;
 * 3.对于不涉及到IO的操作，这样的处理没有任何问题，但是当进行IO操作的时候就会出现一点性能问题.
 * <p>
 * 所有的IO操作都需要操作系统进入内核态才行，而JVM进程属于用户态进程, 当JVM需要把一个缓冲区写到某个Channel或Socket的时候，需要切换到内核态.
 * 而内核态由于并不知道JVM里面这个缓冲区存储在物理内存的什么地址，并且这些物理地址并不一定是连续的(或者说不一定是IO操作需要的块结构)，
 * 所以在切换之前JVM需要把缓冲区复制到物理内存一块连续的内存上, 然后由内核去读取这块物理内存，整合成连续的、分块的内存.
 *
 * @author 蒋时华
 * @date 2019/8/28
 */
public class Client {
    private static String DEFAULT_HOST = "127.0.0.1";
    private static int DEFAULT_PORT = 12345;
    private static ClientHandle clientHandle;

    public static void start() {
        start(DEFAULT_HOST, DEFAULT_PORT);
    }

    public static synchronized void start(String ip, int port) {
        if (clientHandle != null)
            clientHandle.stop();
        clientHandle = new ClientHandle(ip, port);
        new Thread(clientHandle, "Server").start();
    }

    //向服务器发送消息
    public static boolean sendMsg(String msg) throws Exception {
        if (msg.equals("q")) return false;
        clientHandle.sendMsg(msg);
        return true;
    }
}

class ClientHandle implements Runnable {
    private String host;
    private int port;
    private Selector selector;

    private SocketChannel socketChannel;

    private volatile boolean started;

    public ClientHandle(String ip, int port) {
        this.host = ip;
        this.port = port;
        try {
            //创建选择器
            selector = Selector.open();
            //打开监听通道
            socketChannel = SocketChannel.open();
            //如果为 true，则此通道将被置于阻塞模式；如果为 false，则此通道将被置于非阻塞模式
            socketChannel.configureBlocking(false);//开启非阻塞模式
            started = true;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void stop() {
        started = false;
    }

    @Override
    public void run() {
        try {
            doConnect();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        //循环遍历selector
        while (started) {
            try {
                //无论是否有读写事件发生，selector每隔1s被唤醒一次
                selector.select(1000);
                //阻塞,只有当至少一个注册的事件发生的时候才会继续.
//				selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                SelectionKey key = null;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    try {
                        handleInput(key);
                    } catch (Exception e) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        //selector关闭后会自动释放里面管理的资源
        if (selector != null)
            try {
                selector.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            SocketChannel sc = (SocketChannel) key.channel();
            if (key.isConnectable()) {
                if (sc.finishConnect()) ;
                else System.exit(1);
            }
            //读消息
            if (key.isReadable()) {
                //创建ByteBuffer，并开辟一个1M的缓冲区
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                //读取请求码流，返回读取到的字节数
                int readBytes = sc.read(buffer);
                //读取到字节，对字节进行编解码
                if (readBytes > 0) {
                    //将缓冲区当前的limit设置为position=0，用于后续对缓冲区的读取操作
                    buffer.flip();
                    //根据缓冲区可读字节数创建字节数组
                    byte[] bytes = new byte[buffer.remaining()];
                    //将缓冲区可读字节数组复制到新建的数组中
                    buffer.get(bytes);
                    String result = new String(bytes, "UTF-8");
                    System.out.println("客户端收到消息：" + result);
                }
                //没有读取到字节 忽略
//				else if(readBytes==0);
                //链路已经关闭，释放资源
                else if (readBytes < 0) {
                    key.cancel();
                    sc.close();
                }
            }
        }
    }

    //异步发送消息
    private void doWrite(SocketChannel channel, String request) throws IOException {
        //将消息编码为字节数组
        byte[] bytes = request.getBytes();
        //根据数组容量创建ByteBuffer
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        //将字节数组复制到缓冲区
        writeBuffer.put(bytes);
        //flip操作
        writeBuffer.flip();
        //发送缓冲区的字节数组
        channel.write(writeBuffer);
        //****此处不含处理“写半包”的代码
        // TODO: 2019/8/28 因为应答消息的发送，
        //  SocketChannel也是异步非阻塞的，
        //  所以不能保证一次能吧需要发送的数据发送完，
        //  此时就会出现写半包的问题。我们需要注册写操作，
        //  不断轮询Selector将没有发送完的消息发送完毕，
        //  然后通过Buffer的hasRemain()方法判断消息是否发送完成。
    }

    private void doConnect() throws IOException {
        if (socketChannel.connect(new InetSocketAddress(host, port))) ;
        else socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }

    public void sendMsg(String msg) throws Exception {
        socketChannel.register(selector, SelectionKey.OP_WRITE);
        doWrite(socketChannel, msg);

    }
}
