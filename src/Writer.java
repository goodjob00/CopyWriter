import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Writer extends Thread {
    public final BlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(64);
    private OutputStream dst;
    private ThreadGroup group;
    private Writer previusWriter;
    private byte[] data;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setNextWriter(Writer nextWriter) {
        this.previusWriter = nextWriter;
    }

    public Writer(OutputStream dst, ThreadGroup group) {
        this.dst = dst;
        this.group = group;
    }

    @Override
    public void run() {
        try (OutputStream dst0 = dst) {      // 'dst0' for auto-closing
            while (true) {
                data = previusWriter.getData(); // get new data from reader
                System.out.println(data);
                if (data[0] == -1) {
                    break;
                }  // its last data
                dst.write(data, 1, data[0]); //
            }
        } catch (Exception e) {
            group.interrupt();
        }
    }
}
