import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Writer extends Thread {
    public final BlockingQueue<byte[]> localQueue = new ArrayBlockingQueue<>(200);
    public BlockingQueue<byte[]> nextQueue;
    private OutputStream dst;
    private ThreadGroup group;

    public Writer(OutputStream dst, ThreadGroup group) {
        this.dst = dst;
        this.group = group;
    }

    @Override
    public void run() {
        try (OutputStream dst0 = dst) {      // 'dst0' for auto-closing
            while (true) {
                byte[] data = localQueue.take();

                if (data[0] == -1) {
                    nextQueue.put(data);
                    break;
                }
                nextQueue.put(data);
                dst.write(data, 1, data[0]);
            }
        } catch (Exception e) {
            System.out.println(e);
            group.interrupt();
        }
    }
}
