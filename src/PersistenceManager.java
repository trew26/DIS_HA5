import java.io.*;
import java.nio.file.FileSystemNotFoundException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.*;

public class PersistenceManager {

    private Hashtable<Integer, String> buffer;
    private BufferedReader buffered_log_reader;
    private BufferedWriter buffered_log_writer;
    private int id = 1;
    private int c = 0;
    private static PersistenceManager instance;

    // Verhindere die Erzeugung des Objektes über andere Methoden
    private PersistenceManager() throws IOException {
        this.buffer = new Hashtable<>();

        FileReader log_reader = new FileReader("log.txt");
        this.buffered_log_reader = new BufferedReader(log_reader);

        FileWriter log_writer = new FileWriter("log.txt", true);
        this.buffered_log_writer = new BufferedWriter(log_writer);
    }

    // Eine Zugriffsmethode auf Klassenebene, welches dir '''einmal''' ein konkretes
    // Objekt erzeugt und dieses zurückliefert.
    // Durch 'synchronized' wird sichergestellt dass diese Methode nur von einem Thread
    // zu einer Zeit durchlaufen wird. Der nächste Thread erhält immer eine komplett
    // initialisierte Instanz.
    public static synchronized PersistenceManager getInstance() throws IOException {
        if (PersistenceManager.instance == null) {
            PersistenceManager.instance = new PersistenceManager();
        }
        return PersistenceManager.instance;
    }

    public void write(String taid, String pageid, String lsn, String data) {
        //save comma separated string as log entry
        String csv = "" + taid + "," + pageid + "," + lsn + "," + data;
        int ipid = Integer.parseInt(pageid);
        this.buffer.put(ipid, csv);
        try {
            FileWriter log_writer = new FileWriter("log.txt", true);
            this.buffered_log_writer = new BufferedWriter(log_writer);
            buffered_log_writer.write(csv + "\n");
            buffered_log_writer.flush();
        } catch (Exception e) {
            System.out.println("Fehler");
        }
    }

    public Hashtable getBuffer() {
        return this.buffer;
    }

    public BufferedReader getReader() {
        return this.buffered_log_reader;
    }

    public BufferedWriter getWriter() {
        return this.buffered_log_writer;
    }

    public synchronized String beginTransaction() {
        Integer new_id = id++;

        try {
            this.buffered_log_writer.write("BOT " + new_id + "\n");
            this.buffered_log_writer.flush();
        } catch (Exception e) {
            System.out.println("Writing to log failed, Exception: " + e);
        }

        return String.format("%02d", new_id);
    }

    //commits the stored pages to the buffer
    public void commit(String taid) {

        Set<Integer> keys = getKeysbyValue(this.buffer, taid);
        try {
            FileWriter log_writer = new FileWriter("log.txt", true);
            this.buffered_log_writer = new BufferedWriter(log_writer);
            buffered_log_writer.write(taid +" comitted \n");
            buffered_log_writer.flush();
        } catch (Exception e) {
            System.out.println("Fehler");
        }
    }

    //return the current LSN value
    public synchronized String value() {
        return String.format("%02d", c++);
    }

    public Set<Integer> getKeysbyValue(Hashtable<Integer, String> table, String value) {
        Set<Integer> keys = new HashSet<Integer>();
        for (Map.Entry<Integer, String> entry : table.entrySet()) {
            if (entry.getValue().substring(0, 2).equals(value)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    public void safe (Hashtable buffer) {
        Set<Entry<Integer, String>> entrySet = buffer.entrySet();

        for(Entry<Integer, String> entry : entrySet) {

            //System.out.println("KEY: " + entry.getKey() + ", VALUE: " + entry.getValue());

            // load the data from the current entry of the hashtable
            List<String> items = Arrays.asList(entry.getValue().split("\\s*,\\s*"));
            String taid = items.get(0);
            String pageid = items.get(1);
            String lsn = items.get(2);
            String data = items.get(3);

            // create the file in the storage folder and write data
            try {
                FileWriter log_writer = new FileWriter("storage/" + pageid + ".txt");
                BufferedWriter buffered_storage_writer = new BufferedWriter(log_writer);
                buffered_storage_writer.write(data);
                buffered_storage_writer.close();

            } catch (IOException e) {
                System.out.println("Failed to create BufferedWriter. Exception: " + e);
            }
        }
    }

}
