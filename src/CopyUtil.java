import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class CopyUtil {
    public static void copy(final InputStream src, final OutputStream dst, int countWriters) throws IOException {
        // reader-to-writer byte[]-channel
        final BlockingQueue<byte[]> buffer = new ArrayBlockingQueue<>(64);
        final BlockingQueue<byte[]> bufferBack = new ArrayBlockingQueue<>(64);
        final Writer[] writers = new Writer[countWriters];
        // exception-channel from reader/writer threads?
        final AtomicReference<Throwable> ex = new AtomicReference<>();
        final ThreadGroup group = new ThreadGroup("read-write") {
            public void uncaughtException(Thread t, Throwable e) {
                ex.set(e);
            }
        };
        // reader from 'src'
        Thread reader = new Thread(group, () -> {
            try (InputStream src0 = src) {              // 'src0' for auto-closing
                for (int i = 0; i < 64; i++) {
                    bufferBack.put(new byte[128]);
                }
                while (true) {
//                    byte[] data = bufferBack.take();        // new data buffer
                    byte[] data = new byte[128];        // new data buffer
                    int count = src.read(data, 1, 127); // read up to 127 bytes
                    data[0] = (byte) count;             // 0-byte is length-field
                    buffer.put(data);                   // send to writer
                    for (int i = 0; i < countWriters; i++) {
                        writers[i].queue.put(buffer);
                    }
                    if (count == -1) {
                        break;
                    }           // src empty
                }
            } catch (Exception e) {
                group.interrupt();
            }  // interrupt writer
        });
        reader.start();

        // writer to 'dst'
        for (int i = 0; i < countWriters; i++) {
            Writer writer = new Writer(dst, group, bufferBack);
            writer.start();

            writers[i] = writer;
        }

//        try {
//            for (Writer i : writers) {
//                i.join();
//            }
//        } catch (Exception e) {
//            ex.set(e);
//        }
        if (ex.get() != null) {
            throw new IOException(ex.get());
        }
    }
}
