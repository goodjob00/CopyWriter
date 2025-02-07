import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Writer extends Thread {
    public final BlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(64);
    public final BlockingQueue<byte[]> queueForReader = new ArrayBlockingQueue<>(1);
    private OutputStream dst;
    private ThreadGroup group;
    private Writer nextWriter;
    private byte[] data;

    public byte[] getData() {
        if (data == null) {
            return new byte[128];
        }
        return data;
    }

    public void setNextWriter(Writer nextWriter) {
        this.nextWriter = nextWriter;
    }

    public Writer(OutputStream dst, ThreadGroup group) {
        this.dst = dst;
        this.group = group;
    }

    @Override
    public void run() {
        try (OutputStream dst0 = dst) {      // 'dst0' for auto-closing
            while (true) {
                byte[] data = queue.take();
                if (nextWriter == null) {
                    queueForReader.put(data);
                } else {
                    nextWriter.queue.put(data);
                }
                dst.write(data, 1, data[0]);
                if (data[0] == -1) {
                    break;
                }  // its last data
            }
        } catch (Exception e) {
            System.out.println(e);
            group.interrupt();
        }
    }
}
