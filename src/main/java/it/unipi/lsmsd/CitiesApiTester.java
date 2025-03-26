package it.unipi.lsmsd;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class CitiesApiTester {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Scegli la funzione da testare:");
            System.out.println("1 - Funzione 1");
            System.out.println("2 - Funzione 2");
            System.out.println("Qualsiasi altro numero per uscire.");

            int scelta = scanner.nextInt();

            switch (scelta) {
                case 1:
                    funzione1();
                    break;
                case 2:
                    funzione2();
                    break;
                default:
                    System.out.println("Uscita dal programma.");
                    scanner.close();
                    return;
            }
        }
    }

    public static void funzione1() {

        System.out.print("Inserisci il nome del comune: ");
        Scanner scanner = new Scanner(System.in);
        String comune = scanner.nextLine().trim();

        // Costruisce l'URL con il parametro
        String apiUrl = "https://axqvoqvbfjpaamphztgd.functions.supabase.co/comuni?nome=" + comune;

        try {
            // Crea la connessione HTTP
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Legge la risposta dell'API
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) { // OK
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Stampa la risposta dell'API
                System.out.println("Risultato API:" + response.toString());


                // Parsare il JSON
                JSONArray jsonArray = new JSONArray(response.toString());
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject comuneData = jsonArray.getJSONObject(i);

                        // Estrai latitudine, longitudine e regione
                        String name = comuneData.getString("nome");
                        String provincia = comuneData.getJSONObject("provincia").getString("nome");
                        String regione = comuneData.getJSONObject("provincia").getString("regione");
                        JSONObject coordinate = comuneData.getJSONObject("coordinate");
                        double latitudine = coordinate.getDouble("lat");
                        double longitudine = coordinate.getDouble("lng");

                        // Stampa i valori estratti
                        System.out.println("Comune " + (i+1) + ":");
                        System.out.println("\tName: " + name);
                        System.out.println("\tProvince: " + provincia);
                        System.out.println("\tRegion: " + regione);
                        System.out.println("\tLatitude: " + latitudine);
                        System.out.println("\tLongitude: " + longitudine);
                    }
                } else {
                    System.out.println("Nessun comune trovato con questo nome.");
                }
            } else {
                System.out.println("Errore API: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("Errore durante la richiesta: " + e.getMessage());
        }
    }

    public static void funzione2() {
        System.out.println("ciao");
    }
}
