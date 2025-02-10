import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Test {
    public static void main(String[] args) throws IOException {
        Random rnd = new Random(0);
        byte[] testData = new byte[10000];
        rnd.nextBytes(testData);
        ByteArrayOutputStream[] dst = {
                new ByteArrayOutputStream(),
                new ByteArrayOutputStream(),
                new ByteArrayOutputStream(),
                new ByteArrayOutputStream(),
        };
        CopyUtil.copy(new ByteArrayInputStream(testData), dst);
        for (int i = 0; i < dst.length; i++) {
            if (!Arrays.equals(testData, dst[i].toByteArray())) {
                throw new AssertionError("Lab decision wrong!");
            } else {
                System.out.println("OK!");
            }
        }
    }
}