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
            pm.write(1, taid, "first" + taid);
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                return;
            }
            System.out.println(taid);
            pm.write(2, taid, "second" + taid);
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                return;
            }
            System.out.println(taid);
            pm.write(3, taid, "third" + taid);
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                return;
            }
            System.out.println("Hashtable after insert from ClientCreator: \n" +taid + " " + pm.getBuffer());
        }
    }
}
