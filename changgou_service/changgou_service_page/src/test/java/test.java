import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class test {

    AtomicInteger num = new AtomicInteger(0);

    @Test
    public void test1() throws InterruptedException {

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 5000; i++) {
                    num.getAndIncrement();
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 5000; i++) {
                    num.getAndDecrement();
                }
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(num);
    }
}
