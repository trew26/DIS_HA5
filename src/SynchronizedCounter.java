public class SynchronizedCounter {
    private int c = 0;

    public synchronized void increment() {
        c++;
    }

    public synchronized void decremenr() {
        c--;
    }

    public synchronized int value() {
        return c;
    }
}
