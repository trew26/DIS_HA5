public class ClientThread extends Thread {

    PersistenceManager pm = PersistenceManager.getInstance();
    Integer number;

    public ClientThread(Integer number) {
        this.number = number;
    }

    public void run() {
        while(true) {

            int taid = pm.beginTransaction();
            System.out.println(taid);
            //   pm.write();
            pm.write(1, taid, "first");
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                return;
            }
            System.out.println(taid);
            pm.write(1, taid, "second");
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                return;
            }
            System.out.println(taid);
            pm.write(1, taid, "third");
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                return;
            }
        }
    }
}
