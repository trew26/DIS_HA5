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
    private String stored_lsn = "0";

    private PersistenceManager() throws IOException {
        this.buffer = new Hashtable<>();

        FileReader log_reader = new FileReader("log.txt");
        this.buffered_log_reader = new BufferedReader(log_reader);

        FileWriter log_writer = new FileWriter("log.txt", true);
        this.buffered_log_writer = new BufferedWriter(log_writer);
    }

    public static synchronized PersistenceManager getInstance() throws IOException {
        if (PersistenceManager.instance == null) {
            PersistenceManager.instance = new PersistenceManager();
        }
        return PersistenceManager.instance;
    }

    public synchronized void write(String taid, String pageid, String lsn, String data) {
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
        String id = String.format("%02d", new_id);
        try {
            this.buffered_log_writer.write("BOT " + id + " " + this.value() + "\n");
            this.buffered_log_writer.flush();
        } catch (Exception e) {
            System.out.println("Writing to log failed, Exception: " + e);
        }

        return id;
    }

    //commits the stored pages to the buffer
    public synchronized void commit(String taid) {

        Set<Integer> keys = getKeysbyValue(this.buffer, taid);
        try {
            FileWriter log_writer = new FileWriter("log.txt", true);
            this.buffered_log_writer = new BufferedWriter(log_writer);
            buffered_log_writer.write("COMMIT " + taid + " " + this.value() + "\n");
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
            this.stored_lsn = lsn;
        }
    }

    public Integer count_commits() {
        int _counter = 0;
        int log_index = -1;
        int second_log_index = -1;
        int latest_commit_lsn = 0;
        try {
            BufferedReader log_reader =  new BufferedReader(new FileReader("log.txt"));
            String line;
            // read a line from the log
            while ((line = log_reader.readLine()) != null) {
                log_index++;
                List<String> items = Arrays.asList(line.split(","));
                if (line.contains("COMMIT")) {
                    //find the latest lsn of the transaction that is committed in the current log entry
                    int taid = Integer.parseInt(items.get(1));
                    String line2;
                    while ((line2 = log_reader.readLine()) != null && second_log_index < log_index) {
                        second_log_index++;
                        List<String> items2 = Arrays.asList(line.split(","));
                        if (items2.size() > 2) {
                            int taid2 = Integer.parseInt(items2.get(0));
                            if (taid == taid2) {
                                latest_commit_lsn =  Integer.parseInt(items2.get(2));
                            }
                        }
                    }

                    if (latest_commit_lsn > Integer.parseInt(this.stored_lsn)) {
                        _counter++;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to read log. Exception: " + e);
        }

        return _counter;
    }

    public void getWinnerTA(int fail_lsn){
        int _counter = 0;
        int log_index = 0;
        try {
            BufferedReader log_reader = this.getReader();
            String line;
            // read a line from the log
            while ((line = log_reader.readLine()) != null) {
                log_index++;

                List<String> items = Arrays.asList(line.split(","));
                if (line.contains("COMMIT")) {
                    //find the latest lsn of the transaction that is committed in the current log entry
                    int taid = Integer.parseInt(items.get(1));

                    _counter++;
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to read log. Exception: " + e);
        }
    }

}
