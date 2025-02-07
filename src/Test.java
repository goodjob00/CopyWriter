import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Test {
    public static void main(String[] args) throws IOException {
        Random rnd = new Random(0);
        byte[] testData = new byte[30];
        rnd.nextBytes(testData);
        ByteArrayOutputStream[] dst = {
                new ByteArrayOutputStream(),
                new ByteArrayOutputStream(),
//                new ByteArrayOutputStream(),
//                new ByteArrayOutputStream(),
        };
        CopyUtil.copy(new ByteArrayInputStream(testData), dst);
        for (int i = 0; i < dst.length; i++) {
            if (!Arrays.equals(testData, dst[i].toByteArray())) {
//                System.out.println(testData.length);
                byte[] dss = dst[i].toByteArray();
//                for (int j = 0; j < testData.length; j++) {
//                    if (dss[j] != testData[j]) {
//                        System.out.println(dss[j] + " " + testData[j]);
//                    }
//                }
//                System.out.println();
                if (dss.length != testData.length) System.out.println(dss.length + " " + testData.length);
                throw new AssertionError("Lab decision wrong!");
            } else {
                System.out.println(i);
                System.out.println("OK!");
            }
        }
    }
}