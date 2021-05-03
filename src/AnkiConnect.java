import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AnkiConnect {

    public static void addToAnki(String Wort, String Bild, String Bedeutung, String Quelle) throws Exception {

        try {
            URL url = new URL("http://localhost:8765"); //Creates URL Object
            HttpURLConnection con = (HttpURLConnection) url.openConnection(); //Opens Connection
            con.setRequestMethod("POST"); //Sets the Request Method
            con.setRequestProperty("Content-Type", "application/json; utf-8"); //Set the Request Content-Type Header Parameter
            con.setRequestProperty("Accept", "application/json"); //Set Response Format Type
            con.setDoOutput(true); //Ensure the Connection Will Be Used to Send Content

            ///makes new line html compatible
            Wort = nToBr(Wort);
            Wort = slashToQuotes(Wort);
//            Wort = umlautToUmlaut(Wort);
            Bild = nToBr(Bild);
            Bild = slashToQuotes(Bild);
//            Bild = umlautToUmlaut(Bild);
            Bedeutung = nToBr(Bedeutung);
            Bedeutung = slashToQuotes(Bedeutung);
//            Bedeutung = umlautToUmlaut(Bedeutung);
            Quelle = nToBr(Quelle);
            Quelle = slashToQuotes(Quelle);
//            Quelle = umlautToUmlaut(Quelle);

            //Create the Request Body (formated in json)
            String jsonInputString = "{\"action\":\"addNote\",\"version\":6,\"params\":{\"note\":{\"deckName\":\"Deutsch::Satzkarten\",\"modelName\":\"0. 1Karten mit S\\u00e4tzen (TTS)\",\"fields\":{\"Wort\":\""+Wort+"\",\"(^ Hauptsatz) Bild\":\""+Bild+"\",\"Bedeutung\":\""+Bedeutung+"\",\"Quelle\":\""+Quelle+"\"}}}}";

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            //Read the Response from Input Stream
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
//                System.out.println(response.toString());
                if(response.toString().equals("null")) {
                    System.out.println("Fehler: HTML-Code der Karte prüfen");
                }
            }
        } catch (Exception e) {
            throw new Exception("Fehler: Prüfen, ob Anki geöffnet ist", e);
        }
    }

    public static String nToBr(String toChange) {
        return toChange.replace("\n","<br>");
    }
    public static String slashToQuotes(String toChange) {
        return toChange.replace("\"","&quot;");
    }
    public static String umlautToUmlaut(String toChange) {
        toChange = toChange.replace("ä","&auml;");
        toChange = toChange.replace("ö","&ouml;");
        toChange = toChange.replace("ü","&uuml;");
        toChange = toChange.replace("Ä","&Auml;");
        toChange = toChange.replace("Ö","&Ouml;");
        toChange = toChange.replace("Ü","&Uuml;");
        toChange = toChange.replace("ß","&szlig;");
        return toChange;
    }

    public static void checkAnkiOpen() throws IOException {

        while (true) {
            try {
                URL url = new URL("http://localhost:8765");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                break;
//            int code = connection.getResponseCode();
            } catch (Exception e) {
                System.out.print("Fehler: Bitte Anki öffnen (zum Fortsetzen [Enter] drücken) ");
                System.in.read();
            }
        }
    }

}
