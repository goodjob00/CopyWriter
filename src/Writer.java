import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Writer extends Thread {
    public final BlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(64);
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

    public void setData(byte[] data) {
        this.data = data;
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
                if (data == null) continue;
                nextWriter.setData(data);
                dst.write(data, 1, data[0]); //
                if (data[0] == -1) {
                    break;
                }  // its last data
                System.out.println(data);
            }
        } catch (Exception e) {
            group.interrupt();
        }
    }
}
