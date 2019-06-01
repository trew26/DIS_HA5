import java.nio.file.FileSystemNotFoundException;
import java.util.Hashtable;

import java.io.File;

public class PersistenceManager {

    private Hashtable<Integer, String> buffer;

    // Eine (versteckte) Klassenvariable vom Typ der eigene Klasse
    private static PersistenceManager instance;
    // Verhindere die Erzeugung des Objektes über andere Methoden
    private PersistenceManager () {
        this.buffer = new Hashtable<>();
    }

    // Eine Zugriffsmethode auf Klassenebene, welches dir '''einmal''' ein konkretes
    // Objekt erzeugt und dieses zurückliefert.
    // Durch 'synchronized' wird sichergestellt dass diese Methode nur von einem Thread
    // zu einer Zeit durchlaufen wird. Der nächste Thread erhält immer eine komplett
    // initialisierte Instanz.
    public static synchronized PersistenceManager getInstance () {
        if (PersistenceManager.instance == null) {
            PersistenceManager.instance = new PersistenceManager ();
        }
        return PersistenceManager.instance;
    }

    public void write (int pageid, int taid, String data) {
        //save comma separated string as log entry
        String csv = "" + pageid + "," + taid + "," + data;
        this.buffer.put(pageid, csv);


    }

    public Hashtable getBuffer() {
        return this.buffer;
    }

    private int id = 1;
    public synchronized int beginTransaction(){
        return id++;
    }

    public void commit(int taid){
        File folder = new File("/Users/you/folder/");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                System.out.println(file.getName());{
                }
            }
        }
    }
}
