public class ClientCreator {

    public static void main(String[] args) {
        Thread t0 = new ClientThread(0);
        Thread t1 = new ClientThread(1);

        t0.start();
        t1.start();

        try {
            Thread.sleep(10000);
        }
        catch (InterruptedException e) {

        }

        t0.interrupt();
        t1.interrupt();
    }

}
