import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Hashtable;

public class ClientThread extends Thread {

    PersistenceManager pm = PersistenceManager.getInstance();
    Integer number;

    public ClientThread(Integer number) throws IOException {
        this.number = number;
    }

    public void run() {
        int sl_milli = 100;
        int pageID_low = number*10;
        int pageID_high = pageID_low + 10;
        while (true){

            String taid = pm.beginTransaction();
            System.out.println(taid);

            pm.write(taid, getRandom(pageID_high, pageID_low), pm.value(), "first");
            try {
                Thread.sleep(sl_milli);
            }
            catch (InterruptedException e) {
                return;
            }
            System.out.println(taid);
            pm.write(taid, getRandom(pageID_high, pageID_low), pm.value(), "second");
            try {
                Thread.sleep(sl_milli);
            }
            catch (InterruptedException e) {
                return;
            }
            System.out.println(taid);
            pm.write(taid, getRandom(pageID_high, pageID_low), pm.value(), "third");
            try {
                Thread.sleep(sl_milli);
            }
            catch (InterruptedException e) {
                return;
            }
            // pm.commit(taid);
        }
    }

    private String getRandom(int high, int low){
        Random r = new Random();
        int pid = r.nextInt(high - low) + low;
        return String.format("%02d", pid);
    }
}
