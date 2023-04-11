package commons;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class ShirtFactoryTest {

    private static final int NUM_ORDERS_TO_COLLECT = 5;
    private static final int NUM_ORDERS_TO_MAKE = 4;
    private static final int NUM_SHIRTS_TO_MAKE = 3;
    private static final int WAIT_TIME_SECONDS = 100;

    @Test
    public void testThreads() throws InterruptedException {
        ShirtFactory factory = new ShirtFactory();

        // create order collector threads
        for (int i = 0; i < 2; i++) {
            factory.makeOrderCollector("collector" + i);
        }

        // create shirt maker threads
        for (int i = 0; i < 4; i++) {
            factory.makeShirtMaker("shirtMaker" + i);
        }

        // wait for all orders to be collected
        waitForPendingOrders(factory, NUM_ORDERS_TO_COLLECT);

        // wait for all orders to be made
        waitForPendingOrders(factory, NUM_ORDERS_TO_MAKE);

        // wait for all shirts to be made
        waitForPendingOrders(factory, NUM_SHIRTS_TO_MAKE);

        // stop all threads
        stopThreads(factory);

        // assert that all orders have been processed
        assertEquals(0, factory.orders1.pending());
    }

    private void waitForPendingOrders(ShirtFactory factory, int numOrdersToWaitFor) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (factory.orders1.pending() < numOrdersToWaitFor) {
            if (System.currentTimeMillis() - startTime > TimeUnit.SECONDS.toMillis(WAIT_TIME_SECONDS)) {
                throw new RuntimeException("Timed out waiting for pending orders to be processed");
            }
            Thread.sleep(100);
        }
    }

    private void stopThreads(ShirtFactory factory) throws InterruptedException {
        for (OrderCollector1 collector : factory.orderCollector1Set) {
            collector.interrupt();
            collector.join();
        }
    }
}