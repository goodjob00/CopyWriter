import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class CopyUtil {
    public static void copy(final InputStream src, final OutputStream[] dst) throws IOException {
        final Writer[] writers = new Writer[dst.length];
        // exception-channel from reader/writer threads?
        final BlockingQueue<byte[]> ringQueue = new ArrayBlockingQueue<>(100);
        final AtomicReference<Throwable> ex = new AtomicReference<>();
        final ThreadGroup group = new ThreadGroup("read-write") {
            public void uncaughtException(Thread t, Throwable e) {
                ex.set(e);
            }
        };

        // writer to 'dst'
        for (int i = 0; i < dst.length; i++) {
            Writer writer = new Writer(dst[i], group);
            writers[i] = writer;
            writers[i].start();

        }

        for (int j = 0; j < writers.length - 1; j++) {
            writers[j].nextQueue = writers[j+1].localQueue;
        }

        // reader from 'src'
        Thread reader = new Thread(group, () -> {
            try (InputStream src0 = src) {              // 'src0' for auto-closing
                writers[writers.length-1].nextQueue = ringQueue;
                ringQueue.put(new byte[128]);
                while (true) {
                    byte[] data = ringQueue.take();        // new data buffer
                    int count = src.read(data, 1, 127); // read up to 127 bytes
                    data[0] = (byte) count;             // 0-byte is length-field
                    writers[0].localQueue.put(data);
                    if (count == -1) {
                        break;
                    }           // src empty
                }
            } catch (Exception e) {
                group.interrupt();
            }  // interrupt writer
        });
        reader.start();

        try {
            reader.join();
            for (Writer i : writers) {
                i.join();
            }
        } catch (Exception e) {
            ex.set(e);
        }
        if (ex.get() != null) {
            throw new IOException(ex.get());
        }
    }
}
