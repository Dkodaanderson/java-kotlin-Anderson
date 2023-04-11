package commons;
//
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Objects;
//import java.util.Set;
//
//class Order {
//    private String customer;
//
//    public String getCustomer() {
//        return customer;
//    }
//
//    public String getItem() {
//        return item;
//    }
//
//    private String item;
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof Order)) return false;
//        Order order = (Order) o;
//        return getCustomer().equals(order.getCustomer()) && getItem().equals(order.getItem());
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(getCustomer(), getItem());
//    }
//
//    public Order(String customer, String item) {
//        this.customer = customer;
//        this.item = item;
//    }
//}
//
//class Orders {
//    private List<Order> orderList = new LinkedList<Order>();  // shared resource
//    private Object lock = new Object();
//    int pending() {
//        synchronized (lock) {
//            return orderList.size();
//        }
//    }
//    void addOrder(Order order) {
//        synchronized (lock) {
//            orderList.add(order);
//            lock.notifyAll();
//        }
//    }
//    boolean isEmpty() {
//        synchronized (lock) {
//            return orderList.isEmpty();
//        }
//    }
//    Order getNextOrder() {
//        synchronized (lock) {
//            if (orderList.isEmpty()) return null;
//            Order order = orderList.remove(0);
//            lock.notifyAll();
//            return order;
//        }
//    }
//
//    void waitForChange() {
//        synchronized (lock) {
//            try {
//                lock.wait();
//            } catch (InterruptedException e) {
//            }
//        }
//    }
//}
//
//class OrderCollector extends Thread {
//    private String name;
//    private CapFactory factory;
//
//    OrderCollector(CapFactory factory, String name) { this.factory = factory; this.name = name ; }
//    // @Override
//    public void run() {
//        System.out.println("OrderCollector " + name + " running.");
//        for (int i=0; i<4_000_000; ++i) {
//            if (factory.orders.pending() > 1_000) {
//                factory.orders.waitForChange();
//                continue;
//            }
//            String customer = "customer # " + i + " from collector " + name;
//            String item = "fozzle # " + i;
//            Order order = new Order(customer, item);
//            factory.orders.addOrder(order);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        System.out.println("OrderCollector " + name + " done.");
//    }
//}
//
//class OrderMaker extends Thread {
//    private String name;
//    private CapFactory factory;
//
//    OrderMaker(CapFactory factory, String name) { this.factory = factory; this.name = name ; }
//    // @Override
//    public void run() {
//        System.out.println("OrderMaker " + name + " running.");
//        while (true) {
//            if (! factory.orders.isEmpty()) {
//                // another maker may sneak in here
//                Order order = factory.orders.getNextOrder();
//                if (order == null) continue;
//                try {
//                    System.out.println("making order " + order);
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//                System.out.println(name + " made " + order.getItem() + " for " + order.getCustomer());
//            } else {
//                factory.orders.waitForChange();
//            }
//        }
//        // System.out.println("OrderMaker " + name + " done.");
//    }
//}
//
//
//public class CapFactory {
//    Orders orders = new Orders();
//
//    Set<OrderCollector> orderCollectorSet = new HashSet<>();
//    Set<OrderMaker> orderMakerSet = new HashSet<>();
//    void makeOrderCollector(String name) {
//        OrderCollector collector = new OrderCollector(this, name);
//        collector.start();
//        orderCollectorSet.add(collector);
//    }
//
//    void makeOrderMaker(String name) {
//        OrderMaker maker = new OrderMaker(this, name);
//        maker.start();
//        orderMakerSet.add(maker);
//    }
//    // this is the main entry point for a standard java desktop application.
//    // to use this in android studio, you have to modify the gradle.xml file in the .idea directory
//    // as in https://github.com/wmacevoy/java-kotlin-wmacevoy/blob/main/Threads/idea/gradle.xml
//    //
//    // Then create a new run configuration: Application, JDK 11, commons.CapFactory main, class
//    // path of Threads.main this should be enough to run this application as a desktop command line
//    // application.
//    public static void main(String[] args) {
//        CapFactory factory = new CapFactory();
//        factory.run();
//    }
//
//    void run() {
//        makeOrderCollector("alice");
//        makeOrderCollector("henry");
//        makeOrderCollector("bob");
//
//        makeOrderMaker("Yiorgos");
//        makeOrderMaker("Constantine");
//        makeOrderMaker("Yiorgos 2");
//        makeOrderMaker("Constantine 2");
//    }
//}
