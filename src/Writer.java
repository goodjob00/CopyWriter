import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Writer extends Thread {
    public final BlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(64);
    private OutputStream dst;
    private ThreadGroup group;
    private BlockingQueue<byte[]> bufferBack;

    public Writer(OutputStream dst, ThreadGroup group, BlockingQueue<byte[]> bufferBack) {
        this.dst = dst;
        this.group = group;
        this.bufferBack = bufferBack;
    }

    @Override
    public void run() {
        try (OutputStream dst0 = dst) {      // 'dst0' for auto-closing
            while (true) {
                byte[] data = queue.take(); // get new data from reader
//                System.out.println(data);
                if (data[0] == -1) {
                    break;
                }  // its last data
                dst.write(data, 1, data[0]); //
//                bufferBack.add(data);
            }
        } catch (Exception e) {
            group.interrupt();
        }
    }
}
