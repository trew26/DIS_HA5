public class ClientThread extends Thread {

    Integer number;

    public ClientThread(Integer number) {
        this.number = number;
    }

    public void run() {
        while(true) {
            System.out.println(number);
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                return;
            }
        }
    }
}
