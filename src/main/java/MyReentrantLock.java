/*
 * Unfair simple implementation of ReentrantLock
 */
public class MyReentrantLock {
    volatile int lockDepth = 0;
    Thread owningThread = null;

    synchronized void lock() {
        if (owningThread != Thread.currentThread()) {
            while (lockDepth > 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
            owningThread = Thread.currentThread();
        }

        lockDepth++;
    }

    synchronized void unlock() {
        if (lockDepth > 0){
            lockDepth--;
            notify();
        } else {
            throw new IllegalMonitorStateException();
        }
    }

    boolean isLocked() {
        return lockDepth > 0;
    }
}
