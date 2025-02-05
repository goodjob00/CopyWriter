import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Test {
    public static void main(String[] args) throws IOException {
        Random rnd = new Random(0);
        byte[] testData = new byte[64 * 100000];
        rnd.nextBytes(testData);
        ByteArrayOutputStream dst = new ByteArrayOutputStream();
        CopyUtil.copy(new ByteArrayInputStream(testData), dst, 3);
        if (!Arrays.equals(testData, dst.toByteArray())) {
            throw new AssertionError("Lab decision wrong!");
        } else {
            System.out.println(Arrays.equals(testData, dst.toByteArray()));

            System.out.println("OK!");
        }
    }
}