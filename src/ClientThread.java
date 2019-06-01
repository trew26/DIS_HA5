import java.util.Random;

public class ClientThread extends Thread {

    PersistenceManager pm = PersistenceManager.getInstance();
    Integer number;

    public ClientThread(Integer number) {
        this.number = number;
    }

    private int getPageID(){
        int pageID_low = number*10;
        int pageID_high = pageID_low + 10;
        Random r = new Random();
        return r.nextInt(pageID_high - pageID_low) + pageID_low;
    }
    public void run() {
        while(true) {

            int taid = pm.beginTransaction();
            System.out.println(taid);
            //   pm.write();
            pm.write(getPageID(), taid, "first" + taid);
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                return;
            }
            System.out.println(taid);
            pm.write(getPageID(), taid, "second" + taid);
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                return;
            }
            System.out.println(taid);
            pm.write(getPageID(), taid, "third" + taid);
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
