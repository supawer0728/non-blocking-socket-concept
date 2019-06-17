package test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;

public class SomeTest {

    @Test
    public void name() throws Exception {

        for (int j = 0; j < 100; j++) {
            List<AtomicInteger> counts = IntStream.range(0, 10).mapToObj(n -> new AtomicInteger()).collect(Collectors.toList());

            Random random = new Random();

            for (int i = 0; i < 1000000; i++) {
                int num = random.nextInt(10);
                counts.get(num).getAndIncrement();
            }

            double 표준편차 = calculateSD(counts);

            System.out.println(counts);
            System.out.println(Math.sqrt(표준편차));
        }
    }

    public static double calculateSD(List<AtomicInteger> counts) {
        double standardDeviation = 0.0;

        double mean = counts.stream().mapToDouble(AtomicInteger::doubleValue).average().orElse(0d);

        for (AtomicInteger num : counts) {
            standardDeviation += Math.pow(num.doubleValue() - mean, 2);
        }

        return Math.sqrt(standardDeviation / counts.size());
    }
}
