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

            int taid = pm.beginTransaction();
            System.out.println(taid);

            pm.write(getRandom(pageID_high, pageID_low), taid, pm.value(), "first   " + taid);
            try {
                Thread.sleep(sl_milli);
            }
            catch (InterruptedException e) {
                return;
            }
            System.out.println(taid);
            pm.write(getRandom(pageID_high, pageID_low), taid, pm.value(), "second   " + taid);
            try {
                Thread.sleep(sl_milli);
            }
            catch (InterruptedException e) {
                return;
            }
            System.out.println(taid);
            pm.write(getRandom(pageID_high, pageID_low), taid, pm.value(), "third   " + taid);
            try {
                Thread.sleep(sl_milli);
            }
            catch (InterruptedException e) {
                return;
            }
            // pm.commit(taid);
        }
    }

    private int getRandom(int high, int low){
        Random r = new Random();
        return r.nextInt(high - low) + low;
    }
}
