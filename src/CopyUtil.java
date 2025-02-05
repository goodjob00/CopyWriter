import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class CopyUtil {
    public static void copy(final InputStream src, final OutputStream[] dst) throws IOException {
        final Writer[] writers = new Writer[dst.length];
        // exception-channel from reader/writer threads?
        final AtomicReference<Throwable> ex = new AtomicReference<>();
        final ThreadGroup group = new ThreadGroup("read-write") {
            public void uncaughtException(Thread t, Throwable e) {
                ex.set(e);
            }
        };

        // writer to 'dst'
        for (int i = 0; i < dst.length; i++) {
            Writer writer = new Writer(dst[i], group);
            writer.start();

            writers[i] = writer;
        }

        for (int j = 1; j < writers.length - 1; j++) {
            writers[j-1] = writers[j];
        }

        // reader from 'src'
        Thread reader = new Thread(group, () -> {
            writers[writers.length-1].setData(new byte[128]);
            try (InputStream src0 = src) {              // 'src0' for auto-closing
                while (true) {
                    byte[] data = writers[writers.length - 1].getData();        // new data buffer
                    int count = src.read(data, 1, 127); // read up to 127 bytes
                    data[0] = (byte) count;             // 0-byte is length-field

                    writers[0].setData(data);
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
