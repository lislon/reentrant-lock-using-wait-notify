public class Main {
    public static void main(String[] args) {
        MyReentrantLock lock = new MyReentrantLock();
        lock.lock();
        try {
            System.out.println("Hi, please see tests");
        } finally {
            lock.unlock();
        }
    }
}
