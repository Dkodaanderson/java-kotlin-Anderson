package commons;


import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class Order1 {
    private String customer;
    private String item;

    public Order1(String customer, String item) {
        this.customer = customer;
        this.item = item;
    }

    public String getCustomer() {
        return customer;
    }

    public String getItem() {
        return item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order1)) return false;
        Order1 order1 = (Order1) o;
        return Objects.equals(getCustomer(), order1.getCustomer()) && Objects.equals(getItem(), order1.getItem());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCustomer(), getItem());
    }
}

class Orders1 {
    private List<Order1> order1List = new LinkedList<>(); // shared resource
    private Map<Order1, Integer> orderCountMap = new HashMap<>(); // shared resource
    private Object lock = new Object();

    int pending() {
        synchronized (lock) {
            return order1List.size();
        }
    }

    void addOrder(Order1 order1) {
        synchronized (lock) {
            order1List.add(order1);
            orderCountMap.putIfAbsent(order1, 0);
            orderCountMap.computeIfPresent(order1, (o, count) -> count + 1);
            lock.notifyAll();
        }
    }

    boolean isEmpty() {
        synchronized (lock) {
            return order1List.isEmpty();
        }
    }

    Order1 getNextOrder() {
        synchronized (lock) {
            if (order1List.isEmpty()) {
                return null;
            }
            Order1 order1 = order1List.remove(0);
            orderCountMap.computeIfPresent(order1, (o, count) -> count - 1);
            lock.notifyAll();
            return order1;
        }
    }

    void waitForChange() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    int getOrderCount(Order1 order1) {
        synchronized (lock) {
            return orderCountMap.getOrDefault(order1, 0);
        }
    }
}

class OrderCollector1 extends Thread {
    private String name;
    private ShirtFactory factory;


    OrderCollector1(ShirtFactory factory, String name) {
        this.factory = factory;
        this.name = name;
    }

    @Override
    public void run() {
        System.out.println("OrderCollector " + name + " running.");
        for (int i = 0; i < 4; ++i) {
            if (factory.orders1.getOrderCount(new Order1("customer # " + i + " from collector " + name, "shirt # " + i)) > 1_000) {
                factory.orders1.waitForChange();
                continue;
            }
            Order1 order1 = new Order1("customer # " + i + " from collector " + name, "shirt # " + i);
            factory.orders1.addOrder(order1);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
        System.out.println("OrderCollector " + name + " done.");
    }
}

class OrderMaker1 extends Thread {
    private String name;
    private ShirtFactory factory;

    OrderMaker1(ShirtFactory factory, String name) {
        this.factory = factory;
        this.name = name;
    }

    @Override
    public void run(){
        System.out.println("OrderCollector " + name + " running.");
        for (int i = 0; i < 4; ++i) {
            if (factory.orders1.pending() > 1_000) {
                factory.orders1.waitForChange();
                continue;
            }
            String customer = "customer # " + i + " from collector " + name;
            String item = "shirt # " + i;
            Order1 order1 = new Order1(customer, item);
            factory.orders1.addOrder(order1);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("OrderCollector " + name + " done.");
    }
}

class ShirtMaker extends Thread {
    private String name;
    private ShirtFactory factory;
    ShirtMaker(ShirtFactory factory, String name) {
        this.factory = factory;
        this.name = name;
    }

    //@Override
    public void run() {
        System.out.println("ShirtMaker " + name + " running.");
        while (true) {
            if (!factory.orders1.isEmpty()) {
                Order1 order1 = factory.orders1.getNextOrder();
                if (order1 == null)
                    continue;
                try {
                    System.out.println("making order " + order1);
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(name + " made " + order1.getItem() + " for " + order1.getCustomer());
            } else {
                factory.orders1.waitForChange();
            }
        }
    }
}

public class ShirtFactory {
    Orders1 orders1 = new Orders1();

    Set<OrderCollector1> orderCollector1Set = new HashSet<>();
    Set<ShirtMaker> shirtMakerSet = new HashSet<>();

    void makeOrderCollector(String name) {
        OrderCollector1 collector = new OrderCollector1(this, name);
        collector.start();
        orderCollector1Set.add(collector);
    }

    void makeShirtMaker(String name) {
        ShirtMaker maker = new ShirtMaker(this, name);
        maker.start();
        shirtMakerSet.add(maker);
    }

    public static void main(String[] args) {
        ShirtFactory factory = new ShirtFactory();
        factory.run();
    }

    void run() {
        makeOrderCollector("aliceShirt");
        makeOrderCollector("henryShirt");
        makeOrderCollector("bobShirt");

        makeShirtMaker("YiorgosShirt");
        makeShirtMaker("ConstantineShirt");
        makeShirtMaker("YiorgosShirt 2");
        makeShirtMaker("ConstantineShirt 2");
    }

}

