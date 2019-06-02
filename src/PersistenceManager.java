import java.io.*;
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
    private int commit_counter = 0;
    private static PersistenceManager instance;
    private String stored_lsn = "0";

    private PersistenceManager() throws IOException {
        this.buffer = new Hashtable<>();

        FileReader log_reader = new FileReader("log.txt");
        this.buffered_log_reader = new BufferedReader(log_reader);

        FileWriter log_writer = new FileWriter("log.txt", false);
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

        int unsaved_commits = this.count_unsaved_commits();
        if (unsaved_commits >= 5) {
            this.safe(buffer);
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


    public void safe(Hashtable buffer) {
        Set<Entry<Integer, String>> entrySet = buffer.entrySet();

        for (Entry<Integer, String> entry : entrySet) {

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
                System.out.println("Saved: " + entry.getValue());

            } catch (IOException e) {
                System.out.println("Failed to create BufferedWriter. Exception: " + e);
            }
            this.stored_lsn = lsn;
        }
    }

    public Integer count_unsaved_commits() {
        int uncommited_tas = 0;
        int log_index = 0;
        try {
            BufferedReader log_reader = new BufferedReader(new FileReader("log.txt"));
            String line;
            while ((line = log_reader.readLine()) != null) {
                log_index++;
                if (line.contains("COMMIT")) {  // if the current line is a commit

                    List<String> items = Arrays.asList(line.split(" "));
                    int taid = Integer.parseInt(items.get(1));

                    // find last lsn of that ta before this commit
                    int latest_commit_lsn = find_latest_ta_lsn(taid, log_index);

                    // if this lsn is greater than the last lsn that has been stored, this is a commit that has not yet been saved
                    if (latest_commit_lsn > Integer.parseInt(this.stored_lsn)) {
                        uncommited_tas++;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to read log. Exception: " + e);
        }
        return uncommited_tas;
    }

    // find the latest lsn of a ta in the log, that is smaller than some index
    public int find_latest_ta_lsn(int taid, int line_limit) {
        int log_index = 0;
        int latest_ta_lsn = 0;
        try {
            BufferedReader log_reader = new BufferedReader(new FileReader("log.txt"));
            String line;

            // go through the part of the log that has been parsed up to the point of line_limit
            while ((line = log_reader.readLine()) != null && log_index < line_limit) {
                log_index++;
                List<String> items2 = Arrays.asList(line.split(","));

                if (items2.size() > 2) {  // the line contains ta information and lsn
                    int curr_taid = Integer.parseInt(items2.get(0));
                    if (taid == curr_taid) {  // if its the ta we are interested in update latest lsbn value
                        latest_ta_lsn = Integer.parseInt(items2.get(2));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to read log. Exception: " + e);
        }
        return latest_ta_lsn;
    }

    public void getWinnerTA(int fail_lsn) {
        int _counter = 0;
        int log_index = 0;
        try {
            BufferedReader log_reader = new BufferedReader(new FileReader("log.txt"));
            String line;
            // read a line from the log
            while ((line = log_reader.readLine()) != null) {
                if (line.contains("COMMIT")) {

                    String[] splitted = line.split("\\s+");
                    String lsn = splitted[2];
                    int iLSN = Integer.parseInt(lsn);
                    String taid = splitted[1];
                    int iTAID = Integer.parseInt(taid);
                    //falls der fehler vor dem commit auftritt
                    if (iLSN > fail_lsn) {
                        this.redo(iTAID, fail_lsn, iLSN);

                    }
                }

            }
        } catch (Exception e) {
            System.out.println("Failed to read log. Exception: " + e);
        }
    }

    public void redo(int taid, int fail_lsn, int commit_lsn) {
        try {
            BufferedReader log_reader_redo = new BufferedReader(new FileReader("log.txt"));
            String line;
            // read a line from the log
            while ((line = log_reader_redo.readLine()) != null) {
                if (!(line.contains("COMMIT") || line.contains("BOT") || line.contains("REDO"))) {
                    String[] splitted = line.split(",");
                    String sTAID = splitted[0];
                    int iTAID = Integer.parseInt(sTAID);
                    String sPID = splitted[1];
                    String lsn = splitted[2];
                    int iLSN = Integer.parseInt(lsn);
                    String data = splitted[3];
                    //if log entry is before the commit lsn and after the fail lsn
                    //otherwise it's the correctes log entry
                    if (iLSN > fail_lsn && commit_lsn > iLSN && iTAID == taid) {
                        try {
                            FileWriter log_writer = new FileWriter("log.txt", true);
                            this.buffered_log_writer = new BufferedWriter(log_writer);
                            buffered_log_writer.write("REDO " + iLSN + "\n");
                            buffered_log_writer.flush();
                        } catch (Exception e) {
                            System.out.println("Fehler");
                        }
                        this.write(sTAID, sPID, this.value(), data);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Failed to read log. Exception: " + e);
        }
    }

 /*   public void redo(int taid, int fail_lsn) {
        int _counter = 0;
        int log_index = 0;
        Hashtable<Integer, String> table = new Hashtable<>();
        try {
            BufferedReader log_reader =  new BufferedReader(new FileReader("log.txt"));
            String line;
            // read a line from the log
            while ((line = log_reader.readLine()) != null) {
                if (!(line.contains("COMMIT") || line.contains("BOT") || line.contains("REDO"))) {
                    String[] splitted = line.split(",");
                    String sTAID = splitted[0];
                    int iTAID = Integer.parseInt(sTAID);
                    String sPID = splitted[1];
                    String lsn = splitted[2];
                    int iLSN = Integer.parseInt(lsn);
                    String data = splitted[3];
                    if (iLSN > fail_lsn && iTAID == taid) {
                        //save comma separated string as log entry
                        String csv = "" + taid + "," + sPID + "," + this.value() + "," + data;
                        int ipid = Integer.parseInt(sPID);
                        table.put(ipid, csv);
                        try {
                            FileWriter log_writer = new FileWriter("log.txt", true);
                            this.buffered_log_writer = new BufferedWriter(log_writer);
                            buffered_log_writer.write("REDO, " + lsn + "," + csv + "\n");
                            buffered_log_writer.flush();
                        } catch (Exception e) {
                            System.out.println("Fehler");
                        }
                    }
                }
            }
            this.safe(table);

        } catch (Exception e) {
            System.out.println("Failed to read log. Exception: " + e);
        }
    } */
}
