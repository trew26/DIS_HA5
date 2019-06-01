import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class ClientCreator {

    public static void main(String[] args) throws IOException {
        PersistenceManager persistenceManager = PersistenceManager.getInstance();

        persistenceManager.write(1, 1, "QWERTZ");
        persistenceManager.write(2,2,"ASDFGH");
        persistenceManager.write(1,1,"OVERRIDE");

        System.out.println("Hashtable after insert from ClientCreator: \n" + persistenceManager.getBuffer());

        BufferedReader log_reader = persistenceManager.getReader();
        BufferedWriter log_writer = persistenceManager.getWriter();
        log_writer.write("Hello from ClientCreator\n");
        log_writer.flush();

        String line;
        while ((line = log_reader.readLine()) != null) {
            System.out.println("Lines in log: " + line);
        }

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
