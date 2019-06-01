public class ClientThread extends Thread {

    String name;

    public ClientThread(String name) {
        this.name = name;
    }

    public void run() {
        while(true) {
            System.out.println("Hello from " + name);
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                return;
            }
        }
    }
}
