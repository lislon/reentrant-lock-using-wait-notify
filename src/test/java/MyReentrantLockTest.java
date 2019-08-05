import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


@Timeout(5)
class MyReentrantLockTest {

    private final MyReentrantLock lock = new MyReentrantLock();
    private int counter = 0;
    private volatile boolean isThreadBeforeLock = false;
    private volatile boolean isThreadAfterLock = false;

    @Test
    @DisplayName("isLocked() method should display lock/unlock status")
    void lockedFlagIsTrue() {
        assertFalse(lock.isLocked());
        lock.lock();
        assertTrue(lock.isLocked());
    }

    @Test
    @DisplayName("lock() is blocks on other thread")
    void blocksOnLock() throws Exception {
        lock.lock();
        Thread thread = new Thread(() -> {
            isThreadBeforeLock = true;
            lock.lock();
            isThreadAfterLock = true;
        });
        thread.start();
        while (!isThreadBeforeLock) {
            thread.join(100);
        }
        assertFalse(isThreadAfterLock);
    }

    @Test
    @DisplayName("lock() unblocks when unlock() called from another thread")
    void testLockIsUnlocked() throws Exception {
        lock.lock();
        Thread thread = new Thread(() -> {
            isThreadBeforeLock = true;
            lock.lock();
            isThreadAfterLock = true;
        });
        thread.start();
        while (!isThreadBeforeLock) {
            thread.join(100);
        }
        lock.unlock();
        while (!isThreadAfterLock) {
            thread.join(100);
        }
    }

    @Test
    @DisplayName("lock() can be reentered in same thread")
    void testCanLockTwiceOnSameThread() throws Exception {
        lock.lock();
        lock.lock();
    }

    @Test
    @DisplayName("unlock() respects that multiply locks are being called beforehand")
    void testRespectsLockDepth() throws Exception {
        lock.lock();
        lock.lock();
        lock.unlock();
        assertTrue(lock.isLocked());
    }

    @Test
    @DisplayName("unlock() without a lock leads to IllegalMonitorException")
    void testUnlockWithNoLock() throws Exception {
        assertThrows(IllegalMonitorStateException.class, () -> lock.unlock());
    }

    @Test
    @DisplayName("test that critical section implementation is possible with lock+unlock methods calls.")
    void stressTestIncrementCounter() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int ITERATIONS = 200;
        for (int i = 0; i < ITERATIONS; i++) {
            executorService.submit(() -> {
                lock.lock();
                try {
                    counter = unsafeIncrement(counter);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                } finally {
                    lock.unlock();
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        assertTrue(executorService.isTerminated());
        assertEquals(ITERATIONS, counter);
    }

    private int unsafeIncrement(int counter) throws InterruptedException {
        Thread.sleep(1);
        return counter + 1;
    }
}