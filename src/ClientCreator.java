import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;

public class ClientCreator {

    public static void main(String[] args) throws IOException {
        PersistenceManager persistenceManager = PersistenceManager.getInstance();
        BufferedReader log_reader = persistenceManager.getReader();
        String line;
        //while ((line = log_reader.readLine()) != null) {
        //    System.out.println(line);
        //}

        Hashtable<Integer, String> table =  persistenceManager.getBuffer();

        Thread t0 = new ClientThread(0);
        Thread t1 = new ClientThread(1);

        t0.start();
        t1.start();

        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {

        }

        t0.interrupt();
        t1.interrupt();
        //testing

        //persistenceManager.safe(persistenceManager.getBuffer());



        persistenceManager.count_unsaved_commits();
     //   persistenceManager.getWinnerTA(10);

        table.forEach(
                (k,v) -> System.out.println("Key : " + k + ", Value : " + v));

        while ((line = log_reader.readLine()) != null) {
            System.out.println("Lines in log: " + line);
        }
    }


}
