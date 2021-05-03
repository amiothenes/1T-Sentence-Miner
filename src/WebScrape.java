import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class WebScrape {
    public static String getDef(String word) {
        String url = "https://www.dwds.de/?q="+word+"&from=wb";
//        String url = "https://www.dwds.de/?q=Bauch&from=wb";
        String groundWord = "";

        try {

            final Document document = Jsoup.connect(url).get();

//            for (Element element : document.select("div.bedeutungsuebersicht")) {
//                if (element.select("ol"))
//            }

            //word and info

            ArrayList<String> allInfos = new ArrayList<>();
            for (Element infos : document.select("span.dwdswb-ft-blocktext")) {
                allInfos.add(infos.text());
//                System.out.println(infos.text()+";");
            }

            String[] infos = allInfos.get(0).split(" ");
            String pofs = infos[0];
            String shpofs = "";
            if (pofs.equals("Adjektiv")) {
                shpofs = "a. ";
            } else if (pofs.equals("Substantiv")) {
//                shpofs = "s";
                if (infos[1].equals("(Neutrum)")) {
                    shpofs = "das ";
                } else if (infos[1].equals("(Femininum)")) {
                    shpofs = "die ";
                } else if (infos[1].equals("(Maskulinum)")) {
                    shpofs = "der ";
                } else if (infos[1].equals("(Femininum,") && infos[2].equals("Maskulinum)")) {
                    shpofs = "die-der ";
                } else if (infos[1].equals("(Maskulinum,") && infos[2].equals("Femininum)")) {
                    shpofs = "der-die ";
                } else if (infos[1].equals("(Neutrum,") && infos[2].equals("Maskulinum)")) {
                    shpofs = "das-der ";
                } else if (infos[1].equals("(Neutrum,") && infos[2].equals("Femininum)")) {
                    shpofs = "das-die ";
                }
            } else if (pofs.equals("Verb")) {
                shpofs = "v. ";
            } else if (pofs.equals("Konjunktion")) {
                shpofs = "c. ";
            } else if (pofs.equals("Adverb")) {
                shpofs = "av. ";
            } else if (pofs.equals("Präposition")) {
                shpofs = "p. ";
            } else if (pofs.equals("partizipiales")) {
                shpofs = "pa. ";
            } else {
                shpofs = pofs+". ";
            }
//            System.out.print(shpofs); //wasprinting
            GlobalVar.Bedeutung += shpofs;

            ArrayList<String> allWords = new ArrayList<>();
            for (Element words : document.getElementsByTag("b")) {
                allWords.add(words.text());
//                System.out.println(words.text());
            }

//            System.out.print(allWords.get(0)+": "); //wasprinting
            GlobalVar.Bedeutung += allWords.get(0)+": ";
            groundWord = allWords.get(0);

            //defs
            ArrayList<String> allDefs = new ArrayList<>();

            for (Element defs : document.getElementsByTag("ol")) {
                allDefs.add(defs.text());
//                System.out.println(defs.text()+";");
            }

            //adds synonym where there would be otherwise nothing
            for (Element defs : document.getElementsByClass("dwdswb-verweise")) {
                allDefs.add(defs.text());
            }

            boolean wasEmpty = false;
            if (allDefs.isEmpty()) {
                for (Element defs : document.getElementsByClass("dwdswb-lesart-def")) {
                    allDefs.add(defs.text());
//                System.out.println(defs.text()+";");
                }
                wasEmpty = true;
            }
//            if (allDefs.isEmpty()) {
//                System.out.println("ASNOHEUSNATODESU");
//                for (Element element : document.select("div.bedeutungsuebersicht")) {
//                    allDefs.add(element.text());
//                }
//                wasEmpty = true;
//            }

            ArrayList<String> allExSentences = new ArrayList<>();

//            //the sentences at the bottom
//            for (Element exSens : document.getElementsByClass("dwds-gb-list")) {
//                allExSentences.add(exSens.text());
//            }
//            String firstExSens = null;
//            try {
//                String[] exSens = allExSentences.get(0).split("(?<=[a-z]\\.)");
//                firstExSens = exSens[0];
//            } catch (Exception e) {
//                firstExSens = null;
//            }

            //the example of the first def
            for (Element exSens : document.getElementsByClass("dwdswb-kompetenzbeispiel")) {
                allExSentences.add(exSens.text());
            }

            String firstExSens = null;
            try {
                firstExSens = allExSentences.get(0)+";\n"+allExSentences.get(1);
            } catch (Exception e) {
                try {
                    firstExSens = allExSentences.get(0);
                } catch (Exception f) {
                    firstExSens = null;
                }
            }

//            System.out.println(allDefs.get(1));
            String[] splitDefs = {};
            if (wasEmpty) {
                splitDefs = allDefs.get(0).split("(?=[1-9])|\\s(?=a\\))|\\s(?=b\\))|\\s(?=c\\))|\\s(?=d\\))|\\s(?=e\\))");
            } else {
                String longString = allDefs.get(1);

//                longString = longString.trim().replaceAll("\\s{2,}", " ");
                longString = longString.replace("⟨","{");
                longString = longString.replace("⟩","}");
                longString = longString.replace("[bildlich] ...", "");
                longString = longString.replace("[übertragen] ...", "");
                longString = longString.replace("[salopp] ...", "");
                longString = longString.replace("[gehoben] ...", "");
                longString = longString.replace("[salopp, übertragen] ...", "");
                longString = longString.replace("[gehoben, übertragen] ...", "");
                longString = longString.replace("[papierdeutsch] ...", "");
                longString = longString.replace(" umgangssprachlich ", " [umgangssprachlich] ");
                longString = longString.replace(" bildlich ", " [bildlich] ");
                longString = longString.replace(" salopp ", " [salopp] ");
                longString = longString.replace(" gehoben ", " [gehoben] ");
                longString = longString.replace(" veraltend ", " [veraltend] ");
                longString = longString.replace(" dichterisch ", " [dichterisch] ");
                longString = longString.replace("[umgangssprachlich] ...", "");
                longString = longString.replace(" salopp, abwertend ", " [salopp, abwertend] ");
                longString = longString.replace("landschaftlich  ", "[landschaftlich ] ");
                longString = longString.replaceAll("[a-z]\\)\\s[...]", "");
                longString = longString.replace("häufig im Partizip I", "");
                longString = longString.replace("oft im Partizip II", "");
                longString = longString.replace("(1)", "(erste bedeutung)");
                longString = longString.replace("(2)", "(zweite Bedeutung)");
                longString = longString.replace("(3)", "(dritte Bedeutung)");

                longString = longString.trim().replaceAll("\\s{2,}", " ");
                longString = longString.replace(". [", ".|["); //to skip the regex splitting
                longString = longString.replace(". {", ".|{");
                longString = longString.replace("} [", "}|[");
                longString = longString.replace("] {", "]|{");
                longString = longString.replace(") {", ")|{");
                longString = longString.replace(") [", ")|[");

                splitDefs = longString.split("(?=[1-9])|\\s(?=a\\))|\\s(?=b\\))|\\s(?=c\\))|\\s(?=d\\))|\\s(?=e\\))|\\s(?=\\[)|\\s(?=\\{)|\\s(?=II.)|\\s(?=●)");
//                splitDefs = longString.split("(?=[1-9])|\\s(?=a\\))|\\s(?=b\\))|\\s(?=c\\))|\\s(?=d\\))|\\s(?=e\\))|[a-zA-Z]\\s(?=\\[)|[a-zA-Z]\\s(?=\\{)");
            }
//            splitDefs = defs.text().split("(?<=[b)])");
//            splitDefs = defs.text().split("(?<=\\s^[0-9])");
            for (String aDef : splitDefs) {
                String firstchar = String.valueOf(aDef.charAt(0));
//                    String lastchar = String.valueOf(aDef.charAt(aDef.length()-1));
                if(aDef.endsWith(" ")) {
                    aDef= aDef.substring(0, aDef.length() - 1); //deletes characters that are not spaces
                    if(aDef.endsWith(" ")) {
                        aDef= aDef.substring(0, aDef.length() - 1); //second time
                    }
                }
                aDef = aDef.replace(".|[", ". ["); //to skip the regex splitting
                aDef = aDef.replace(".|{", ". {");
                aDef = aDef.replace("}|[", "} [");
                aDef = aDef.replace("]|{", "] {");
                aDef = aDef.replace(")|{", ") {");
                aDef = aDef.replace(")|[", ") [");

//                aDef = aDef.replace("⟨","{");
//                aDef = aDef.replace("⟩","}");
//                aDef = aDef.replace("[bildlich] ...", "");
//                aDef = aDef.replace("[übertragen] ...", "");
//                aDef = aDef.replace("[salopp] ...", "");
//                aDef = aDef.replace("[gehoben] ...", "");
//                aDef = aDef.replace("[salopp, übertragen] ...", "");
//                aDef = aDef.replace("[papierdeutsch] ...", "");
//                aDef = aDef.replace(" umgangssprachlich ", " [umgangssprachlich] ");
//                aDef = aDef.replace(" bildlich ", " [bildlich] ");
//                aDef = aDef.replace(" salopp ", " [salopp] ");
//                aDef = aDef.replace(" gehoben ", " [gehoben] ");
//                aDef = aDef.replace("[umgangssprachlich] ...", "");
                if (firstchar.equals("1") || firstchar.equals("2") || firstchar.equals("3") || firstchar.equals("4") ||
                        firstchar.equals("5") || firstchar.equals("6") || firstchar.equals("7") || firstchar.equals("8") ||
                        firstchar.equals("9") || firstchar.equals("I") || Arrays.asList(splitDefs).indexOf(aDef) == 0) { //I added Arrays.asList on 2021.01.01
//                    System.out.println(""+aDef+";"); //wasprinting
                    GlobalVar.Bedeutung += ""+aDef+";\n";
                } else if(aDef.equalsIgnoreCase(" ")||aDef.equalsIgnoreCase("")||aDef.equalsIgnoreCase("  ") ||
                        aDef.equalsIgnoreCase("bildlich") || aDef.equalsIgnoreCase("2. ") || aDef.equalsIgnoreCase("3. "))  {

                } else {
//                    System.out.println(" "+aDef+";"); //wasprinting
                    GlobalVar.Bedeutung += " "+aDef+";\n";
                }
//                System.out.println(" "+aDef);
            }

            //Example Sentence at the end
            if (firstExSens != null && firstExSens.length() != 0) {
//                System.out.println("\nz.B.: " + firstExSens); //wasprinting
                GlobalVar.Bedeutung += "\nz.B.: " + firstExSens;
            }

//            System.out.println(document.outerHtml());

//            if (allDefs.isEmpty()) {
//                System.out.println("n/a\n");
//            }

        } catch (Exception ex) {
//            ex.printStackTrace();

            try {
                final Document document = Jsoup.connect(url).get();
                //defs
                ArrayList<String> allDefs = new ArrayList<>();
                for (Element defs : document.getElementsByClass("dwdswb-lesart-def")) {
                    allDefs.add(defs.text());
                }
                for (String wordss : allDefs) {
//                    System.out.println(wordss); //wasprinting

                    wordss = wordss.replace("⟨","{");
                    wordss = wordss.replace("⟩","} ");
                    wordss = wordss.replace("[bildlich] ...", "");
                    wordss = wordss.replace("[übertragen] ...", "");
                    wordss = wordss.replace("[salopp] ...", "");
                    wordss = wordss.replace("[gehoben] ...", "");
                    wordss = wordss.replace("[salopp, übertragen] ...", "");
                    wordss = wordss.replace("[gehoben, übertragen] ...", "");
                    wordss = wordss.replace("[papierdeutsch] ...", "");
                    wordss = wordss.replace("umgangssprachlich ", "[umgangssprachlich] ");
                    wordss = wordss.replace("bildlich ", "[bildlich] ");
                    wordss = wordss.replace("salopp ", "[salopp] ");
                    wordss = wordss.replace("gehoben ", "[gehoben] ");
                    wordss = wordss.replace("veraltend ", "[veraltend] ");
                    wordss = wordss.replace("dichterisch ", "[dichterisch] ");
                    wordss = wordss.replace("salopp, abwertend ", "[salopp, abwertend] ");
                    wordss = wordss.replace("landschaftlich  ", "[landschaftlich ] ");
                    wordss = wordss.replace("[umgangssprachlich] ...", "");
                    wordss = wordss.replaceAll("[a-z]\\)\\s[...]", "");
                    wordss = wordss.replace("häufig im Partizip I", "");
                    wordss = wordss.replace("oft im Partizip II", "");
                    wordss = wordss.replace("(1)", "(erste bedeutung)");
                    wordss = wordss.replace("(2)", "(zweite Bedeutung)");
                    wordss = wordss.replace("(3)", "(dritte Bedeutung)");

                    if (allDefs.indexOf(wordss) != allDefs.size()-1 && allDefs.size() != 1) {
                        GlobalVar.Bedeutung += wordss + ";\n";
                    } else {
                        GlobalVar.Bedeutung += wordss + ";";
                    }
                }
//                System.out.println(allDefs.get(1));
            } catch (Exception ex1) {
//                ex.printStackTrace();
//                System.out.println("{Keine Wortdefinitionen}"); //wasprinting
                GlobalVar.Bedeutung += "{Keine Wortdefinitionen}";
            }

        }
        return groundWord;
    }

    public static String getGroundWord(String word) throws IOException {

        String groundWord = null;
        try {
            //looks at ground word to see if word is already added
            String url = "https://www.dwds.de/?q="+word+"&from=wb";
            final Document document = Jsoup.connect(url).get();

            ArrayList<String> allWords = new ArrayList<>();
            for (Element words : document.getElementsByTag("b")) {
                allWords.add(words.text());
            }

            groundWord = allWords.get(0);
        } catch (Exception e) {

//            try {
//                //captitalize the first character
//                word = word.substring(0, 1).toUpperCase() + word.substring(1);
//                //looks at ground word to see if word is already added
//                String url = "https://www.dwds.de/?q="+word+"&from=wb";
//                final Document document = Jsoup.connect(url).get();
//
//                ArrayList<String> allWords = new ArrayList<>();
//                for (Element words : document.getElementsByTag("b")) {
//                    allWords.add(words.text());
//                }
//
//            } catch (Exception f) {
//                groundWord = "aoeu";
//            }
            groundWord = "aoeu"; //for me to know that there is no ground word found

        }

        if (groundWord == null) { //sometimes the website doesn't react
            groundWord = "aoeu";
        }

        return groundWord;
    }

    public static String getFrequency(String baseWord) throws IOException {

        if (baseWord != null) {

            //change ä to ae
            baseWord = baseWord.replace("ä", "ae");
            baseWord = baseWord.replace("ö", "oe");
            baseWord = baseWord.replace("ü", "ue");
            try {
                String url = "https://www.duden.de/rechtschreibung/" + baseWord;
                final Document document = Jsoup.connect(url).get();

                ArrayList<String> allWords = new ArrayList<>();
                for (Element words : document.getElementsByClass("shaft")) {
                    allWords.add(words.text());
                }

                if (allWords.size() != 0) {

                    if (allWords.get(0).equals("▒▒▒▒▒")) { //very frequent
//                        System.out.print("★★★★★");
                        return "★★★★★";
                    } else if (allWords.get(0).equals("▒░░░░")) {
//                        System.out.print("★");
                        return "★";
                    } else if (allWords.get(0).equals("▒▒░░░")) {
//                        System.out.print("★★");
                        return "★★";
                    } else if (allWords.get(0).equals("▒▒▒░░")) {
//                        System.out.print("★★★");
                        return "★★★";
                    } else if (allWords.get(0).equals("▒▒▒▒░")) {
//                        System.out.print("★★★★");
                        return "★★★★";
                    } else { //very unfrequent
//                        System.out.print("");
                        return " ";
                    }
                } else {
                    return " ";
                }
            } catch (Exception e) {
                return "";
            }




        }
        return "";
    }

}
