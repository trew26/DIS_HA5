public class PersistenceManager {

    // Eine (versteckte) Klassenvariable vom Typ der eigene Klasse
    private static PersistenceManager instance;
    // Verhindere die Erzeugung des Objektes über andere Methoden
    private PersistenceManager () {}

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

}
