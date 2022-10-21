import index.FileIndexer;
import query.QueryManager;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.println("Creazione indice");

        long start = System.nanoTime();
        FileIndexer.createIndex(null);
        long end = System.nanoTime();

        System.out.println("Tempo di esecuzione: " + (end-start)/1000000 + " millisecondi.");

        System.out.println("-------------------------------------------------");

        System.out.println("Motore di ricerca.");
        System.out.println("-------------------------------------------------");
        String goOn = "y";
        while(goOn.equalsIgnoreCase("y")) {
            Scanner scan = new Scanner(System.in);
            System.out.println("Inserisci il campo: ");
            String field = scan.nextLine();
            System.out.println("Inserisci dei termini di query: ");
            String words = scan.nextLine();
            QueryManager.executeQuery(field, words);
            System.out.println("Vuoi continuare? (y,n): ");
            goOn = scan.nextLine();
            System.out.println();
            System.out.println("-------------------------------------------------");
            System.out.println();
        }
        System.out.println("Grazie per aver utilizzato il motore di ricerca");
    }
}
