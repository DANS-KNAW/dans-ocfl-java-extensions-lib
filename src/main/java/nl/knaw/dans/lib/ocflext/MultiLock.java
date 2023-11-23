package nl.knaw.dans.lib.ocflext;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MultiLock {
    private int lockCount = 0;

    private final Lock lock = new ReentrantLock();

    /**
     * Acquires the embedded lock.
     */
    public void acquire() {
        lock.lock();
    }

    /**
     * Releases the embedded lock.
     */
    public void release() {
        lock.unlock();
    }

    /**
     * Increments the lock count and acquires the embedded lock if the lock count was 0.
     */
    public synchronized void incrementLock() {
        lockCount++;
        if (lockCount == 1) {
            lock.lock();
        }
    }

    /**
     * Decrements the lock count and releases the embedded lock if the lock count becomes 0.
     */
    public synchronized void decrementLock() {
        lockCount--;
        if (lockCount == 0) {
            lock.notifyAll();
            lock.unlock();
        }
    }
}
