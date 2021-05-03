import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/*
CONFIG INFO
1st Line: Whether to turn autoDoc webscraping on
2nd Line: Whether to get each unknown word's definition (if autoDic is on)
3rd Line: Whether to get micro 1T Sentences
4th Line: Whether to remove new lines (useful for subs)


ADDITIONAL INFO
* in the beginning of a word means the fundamental word exists already
** means that the word was already shown in the same session

Frequency Stars info:
★★★★★: 0 - 1.5k ★★★★: 1.5k - 5k ★★★: 5k - 15k ★★: 15k - 30k ★: 30k - 60k __: 60k+
 */

//make sure that one does not have a huge corpus because all unknown words will automatically be added to the knownWords list
//unless you turn it off

//2020.10.10 - 0.0.1 - T+1 Sentence Finder (was happy that it works)
//2020.10.13 - 0.3   - T+2 additional option for curiosity and fixed bugs with regex and sentence and word division and added CEFR level reading
// I can clean this code up easily by creating functions for repeating code blocks in the main function
//2020.10.24 - 0.4   - Removed duplicates added for saving to file and added another case for sentence splitting
//2020.10.28 - 0.5   - Big Update - Added top 4 unknown word frequency list before the 1T sentences with later option to save them (also added \n replaced be space)
//2020.10.30 - 0.6   - Random Passage - Added that you can request a random "paragraph" from the corpus for study or whatnot
//2020.10.31 - 0.6.5 - Encoding and Bug Fix - Made sure that the console can see German quotes instead of "?" and added try.catch to top freq words
//2020.11.02 - 2.9   - Percentage Comprehension - Added the indication of the measure of how many unknows words / known words in the corpus
//2020.11.09 - 0.9.1 - No more BOM - changed the corpus from utf-8-bom to utf-8 and it fixed the '?' at the beginning, it is a BOM marker of some kind
//2020.11.14 - 1.0   - Huge Update - Added autodictionary that scrapes dwds.net, takes long time thought. Fixed no caps in the challenged word (saved in lowercase)
//2020.11.15 - 1.0.1 - I made the autoDic work finally, changed the bat to java -cp .;jsoup.jar Main
//2020.11.22 - 1.1   - Webscrape Clean-up - I made some changes to webscrape so it outputs in better formats; also added * marking if two same words are found in 1T
//2020.11.22 - 1.1   - Micro-1T Sentences! - how there is an option to split up sentences into clauses so that there are more poss. for 1T match - I learned how to use the debugger; love this project
//2020.12.05 - 1.2   - Marked Ground Words - words that have an existing ground word will be marked '*' while repetition will be "**"; it will be slower i think
//2020.12.06 - 1.3   - Probable Known Word Adder, List of Unknown Word - adds words that have existing ground forms to known; list of unknown words make each word its own sentence
//2020.12.10 - 1.4   - Last char Deleted Bug Fix - There was a bug that would delete the last char of defs, found that it's regex's fault
//2020.12.26 - 1.5   - Decimal in Percentage, Switched Order of Micro1T & Each Unk. Wor., Instead of 2T; you get Micro 1T instead, added beep at the end of a job
//2020.12.30 - 1.6   - Example Sentence added at end & II. new line fix in autoDic & added option to remove newLines (useful for subs)
//2021.01.19 - 1.7   - Frequency stars added & Faster Preliminary loading - by skipping very common words. Freq. powered by Duden (not sure if slower now) & also improved the cmd
//2021.01.26 - 1.8.5 - Removed ':' as sentence seperator
//2021.01.29 - 1.8   - AnkiConnect Prototype - Adding simple sentence and definition directly to anki now possible! Tomorrow will have more improvements
//2021.01.30 - 2.0   - HUGE UPDATE - AnkiConnect & More - Now bolds target words, gives definitions as usual, fills Bild (no image) text, asks for Quelle; Micro sentences are removed and automatically adds everything to Anki except words with no defs; bug fixes
//2021.01.31 - 2.1   - UPDATE CONTINUED - Now showintg loading ETA and bar, bug fixes with splitter & more
//2021.02.01 - 2.1.1 - Fixed bolding not working if there are 2-3 punctuations after a word
//2021.02.04 - 2.1.2 - Bug fix on bolding
//2021.02.07 - 2.1.4 - Bug fix on bolding, added loaBeforeAdding boolean for debug
//2021.02.12 - 2.1.11 - Bug fix on Defs not having line break; some keywords not in brackets; fixed some def format issue with bullet points; added ':' as sent. sep.; fixed creation of two sentences; fixed Dr. seperating; quote is seperators now; percentage now goes to 100%
//2021.02.13 - 2.2.3 - Added Prelecture Plan to be able to prepare to have enough comprehension before reading a tough book; a lot of bug fixes for the addition itself
//2021.04.19 - 2.2.5 - Found that umlauts in jar app don't work and must manually set &auml; etc. Made output list as a aligned table, brackets load with stars now; table view of top unknown words too
//2021.04.20 - 2.2.6 - Added [%] in output so that user knows that it is an option to skip pre-loading

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner input = new Scanner(System.in);

        boolean showLevel = false; //gives your known words count and CEFR level
        boolean autoDic = openConfig(0); //automatically gives definitions
//        boolean micro1T = openConfig(2); //cuts sentences into clauses
        boolean eachWord = openConfig(1); //cuts each word into sentences
        boolean removeNewLine = openConfig(3);
        boolean loadBeforeAdding = true; //wait in a loading screen, to add potential known words to database

        //find the top 4 unknown common words in the text (doesn't matter if there is a 1T sentence)
        String corpus = getCorpus();
        String[] sentences = stringToSentences(corpus, false, eachWord, removeNewLine);
        ArrayList<String> puncLessSentences = removePuncCaps(sentences);
//        ArrayList<String> commonWordsAndFreq = unknownCommonWordFinder(puncLessSentences);
        ArrayList<String> topUnkownWords = unknownCommonWordFinder(puncLessSentences);
        System.out.println();

        //get Quelle input to add to Anki
        setQuelle();

        //checks whether user wants a pre-lecture plan
        String checkForPlan = String.valueOf(GlobalVar.Quelle.charAt(0));
        int targetPer = 0;
        if (checkForPlan.equals("*")) {
            GlobalVar.Quelle = removeFirstChar(GlobalVar.Quelle);

            System.out.print("Zielverständlichkeit in Prozent eingeben (Keine Nachkommastellen!): ");
            targetPer = input.nextInt();
            System.out.println("Vorlesungsplan angefordert, bitte warten.");
        } else if (checkForPlan.equals("%")) {
            GlobalVar.Quelle = removeFirstChar(GlobalVar.Quelle);
            loadBeforeAdding = false;
        }
        System.out.println();

        //checks if anki is open (it is important that anki is open for AnkiConnect
        AnkiConnect.checkAnkiOpen();

        for (int x = 0; x < 100; x++) {
            int b = x+1;
//            String corpus = getCorpus();
//            String[] sentences = stringToSentences(corpus);
//            ArrayList<String> puncLessSentences = removePuncCaps(sentences);
//        ArrayList<String> T1Sentences = T1SentenceGen(sentences,puncLessSentences);
//            if (!micro1T && !eachWord && x == 0) {

            String formatedTime = "";
            if (loadBeforeAdding) {

                long timeElapsed = (long) round(addProbableKnownWords(puncLessSentences), 0); //preliminary loading (adds potential known words) (slow)
                //gives total time for it in ms, format to
                formatedTime = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(timeElapsed),
                        TimeUnit.MILLISECONDS.toMinutes(timeElapsed) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeElapsed)), // The change is in this line
                        TimeUnit.MILLISECONDS.toSeconds(timeElapsed) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeElapsed)));
            }

            ArrayList<String> T1SentAndWord = T1SentenceGen(sentences, puncLessSentences);

            //ArrayList<String> T1Sentences = new ArrayList<>();
            ArrayList<String> unknownWords = new ArrayList<>();
            ArrayList<String> delayedWords = new ArrayList<>();
            int sentenceAmount = T1SentAndWord.size() / 2;
            if (showLevel) {
                int knownWordsTotal = Points.learnedWordsNum() + sentenceAmount;
                System.out.println("Niveau: " + knownWordsTotal + " (" + Points.levelTracker(sentenceAmount) + ")");
            }

            if (checkForPlan.equals("*")) {
                Prelecture.prelecture(targetPer, puncLessSentences, sentences);

                System.out.println("\nMit [Eingabe] fortfahren ");
                System.in.read();
            }

            if (loadBeforeAdding) {
                System.out.println(b + ". Generierte 1T-Sätze (" + sentenceAmount + ") - " + "Verstrichene Zeit: " + formatedTime + "               ");
            }
            System.out.println("=====================");

            for (int i = 0; i < T1SentAndWord.size(); i += 2) {
                //T1Sentences.add(T1SentAndWord.get(i));
//                System.out.println(T1SentAndWord.get(i)); //wasprinting
                if (!stringContainsItemFromList(T1SentAndWord.get(i + 1), unknownWords)) {
                    String boldSentence = boldTarget(T1SentAndWord.get(i), T1SentAndWord.get(i+1));

                    GlobalVar.Satz = boldSentence;
                    GlobalVar.Bedeutung = "";

                    String baseWord = null;
                    if (autoDic) { //looks at ground word to see if word is already added
                            baseWord = WebScrape.getGroundWord(T1SentAndWord.get(i + 1));
                    }

//                if (stringContainsItemFromList(T1SentAndWord.get(i + 1), unknownWords)) {
//                    System.out.print("**" + T1SentAndWord.get(i + 1)); //if the word shows up again; then marked
//                    WebScrape.getFrequency(baseWord); //gives stars based on frequency
//                } else if (eachWord && autoDic && stringContainsItemFromList(baseWord, openKnownWords())) {
////                    System.out.print("*" + T1SentAndWord.get(i + 1)); //if the word's base is already added; then marked
////                    WebScrape.getFrequency(baseWord);
//                } else {
////                    System.out.print(T1SentAndWord.get(i + 1)); //wasprinting
////                    WebScrape.getFrequency(baseWord);
//                }
//                unknownWords.add(T1SentAndWord.get(i + 1));

//                if (autoDic) {
//                    String groundWord = WebScrape.getDef(T1SentAndWord.get(i + 1)); //returns baseWord but also prints def (slow)
//                    if (!groundWord.equals(T1SentAndWord.get(i + 1))) {
//                        if (!stringContainsItemFromList(groundWord, openKnownWords())) {
//                            unknownWords.add(groundWord);
//                        }
//                    }
//                }

                //everything with //wasprinting was previously printing text before AnkiConnect
                    String star = WebScrape.getFrequency(baseWord);

                    System.out.printf("%-20s%-16s%-10s", "\"" + baseWord + "\"", "wurde hinzugefügt (", star +")");
//                    System.out.print("\"" + baseWord + "\" wurde hinzugefügt (");
//                    WebScrape.getFrequency(baseWord);
//                    System.out.println(")");
                    System.out.println();

                    String groundWord = WebScrape.getDef(T1SentAndWord.get(i + 1)); //returns baseWord but also prints def (slow)
                    setBild(); //sets Bild default output from bedeutung; also judges whether bedeutung has a full def or not

                    if (!GlobalVar.isDeadBed) {
                        unknownWords.add(T1SentAndWord.get(i + 1));
                        if (!groundWord.equals(T1SentAndWord.get(i + 1))) {
                            if (!stringContainsItemFromList(groundWord, openKnownWords())) {
                                unknownWords.add(groundWord);
                            }
                        }

                        AnkiConnect.addToAnki(GlobalVar.Satz, GlobalVar.Bild, GlobalVar.Bedeutung, GlobalVar.Quelle);
                    } else {
                        //creates delayed card for review whether addition is sufficient
                        GlobalVar.delayedCards.add(GlobalVar.Satz);
                        GlobalVar.delayedCards.add(GlobalVar.Bild);
                        GlobalVar.delayedCards.add(GlobalVar.Bedeutung);
                        GlobalVar.delayedCards.add(GlobalVar.Quelle);
                        delayedWords.add(T1SentAndWord.get(i + 1));

                        GlobalVar.isDeadBed = false;
                    }
                }

//                System.out.println("\n");
            }
            java.awt.Toolkit.getDefaultToolkit().beep();
            System.out.println("=====================");
            updateKnownWords(unknownWords); //adds all the unknown words to the known word list from TopWords Project
//            System.out.println(" gespeichert.");

            if (GlobalVar.delayedCards.size() != 0) {
                System.out.println("Wörter ohne Definitionen:");

                //shows delayed cards by list
                int n = 1;
                for (int i = 0; i < GlobalVar.delayedCards.size(); i += 4) {
                    System.out.println(" " + n + ". " + GlobalVar.delayedCards.get(i + 1));
                    n++;
                }

                System.out.print("Wie alle speichern: ([1] Nur Datei, [2] Anki, [0] Nicht speichern) ");
                int saveMethod = input.nextInt();

                if (saveMethod == 1) {
                    updateKnownWords(delayedWords); //adds delayedWords to file
                    ArrayList<String> delayedWordRoots = new ArrayList<>();

                    for (int w = 0; w < delayedWords.size(); w ++) { //checks for roots
                        String baseWord1 = WebScrape.getGroundWord(delayedWords.get(w));
                        if (!baseWord1.equals(delayedWords.get(w))) {
                            if (!stringContainsItemFromList(baseWord1, openKnownWords())) {
                                delayedWordRoots.add(baseWord1);
                            }
                        }
                    }
                    updateKnownWords(delayedWordRoots); //adds the roots to file

                    System.out.println(" in Datei gespeichert.");
                } else if (saveMethod == 2) { //Anki implies that it is also saved in file
                    for (int p = 0; p < GlobalVar.delayedCards.size(); p += 4) {
                        AnkiConnect.addToAnki(GlobalVar.delayedCards.get(p), GlobalVar.delayedCards.get(p + 1), GlobalVar.delayedCards.get(p + 2), GlobalVar.delayedCards.get(p + 3));
                    }
                    updateKnownWords(delayedWords);

                    ArrayList<String> delayedWordRoots = new ArrayList<>();

                    for (int w = 0; w < delayedWords.size(); w ++) { //checks for roots
                        String baseWord1 = WebScrape.getGroundWord(delayedWords.get(w));
                        if (!baseWord1.equals(delayedWords.get(w))) {
                            if (!stringContainsItemFromList(baseWord1, openKnownWords())) {
                                delayedWordRoots.add(baseWord1);
                            }
                        }
                    }
                    updateKnownWords(delayedWordRoots); //adds the roots to file

                    System.out.println(" in Datei & Anki gespeichert.");
                }
            }


//            System.out.print("Die Wörter in der Liste speichern? ([1] Ja / [2] Nein): ");
//            String antwort = input.next();
//
//            if (antwort.equals("1")) {
//                updateKnownWords(unknownWords); //adds all the unknown words to the known word list from TopWords Project
//                System.out.println(" gespeichert.");
//            }

            //2T sentences
//            ArrayList<String> T2SentAndWord = T2SentenceGen(sentences, puncLessSentences);
//            ArrayList<String> T2unknownWords = new ArrayList<>();
//
//            int T2sentenceAmount = T2SentAndWord.size() / 3;
//            System.out.println("\n"+b+". Generierte 2T-Sätze (" + T2sentenceAmount + "): ");
//            System.out.println("=====================\n");
//            for (int i = 0; i < T2SentAndWord.size(); i += 3) {
//                //T1Sentences.add(T1SentAndWord.get(i));
//                System.out.println(T2SentAndWord.get(i));
//                T2unknownWords.add(T2SentAndWord.get(i + 1));
//                T2unknownWords.add(T2SentAndWord.get(i + 2));
//                System.out.println(T2SentAndWord.get(i + 1) + ", " + T2SentAndWord.get(i + 2) + "\n");
//            }
//            System.out.println("=====================");
//            System.out.print("Die zweiten Wörter in der Liste speichern? ([1] Ja / [2] Nein): ");
//            String antwort3 = input.next();
//            if (antwort3.equals("1")) {
//                updateKnownWords(T2unknownWords);
//                System.out.println(" gespeichert.");
////            }
//            }

//            //Micro 1T Sentences as Second Option
//            System.out.println();
//            if (!eachWord) {
//                String[] sentencesMicro = stringToSentences(corpus, true, false, removeNewLine);
//                ArrayList<String> puncLessSentencesMicro = removePuncCaps(sentencesMicro);
//                ArrayList<String> T1SentAndWordMicro = T1SentenceGen(sentencesMicro, puncLessSentencesMicro);
//
//                ArrayList<String> unknownWordsMicro = new ArrayList<>();
//                int sentenceAmountMicro = T1SentAndWordMicro.size() / 2;
//
//                System.out.println(b + ". Generierte Mikro 1T-Sätze (" + sentenceAmountMicro + "): ");
//                System.out.println("=====================\n");
//
//                for (int i = 0; i < T1SentAndWordMicro.size(); i += 2) {
//                    //T1Sentences.add(T1SentAndWord.get(i));
//                    System.out.println(T1SentAndWordMicro.get(i));
//
////                    String baseWord = null;
////                    if (autoDic) { //looks at ground word to see if word is already added
////                        baseWord = WebScrape.getGroundWord(T1SentAndWordMicro.get(i + 1));
////                    }
//
//                    if (stringContainsItemFromList(T1SentAndWordMicro.get(i + 1), unknownWordsMicro)) {
//                        System.out.println("**" + T1SentAndWordMicro.get(i + 1)); //if the word shows up again; then marked
//                    } else {
//                        System.out.println(T1SentAndWordMicro.get(i + 1));
//                    }
//                    unknownWordsMicro.add(T1SentAndWordMicro.get(i + 1));
//
//                    if (autoDic) {
//                        String groundWord = WebScrape.getDef(T1SentAndWordMicro.get(i + 1));
//                        if (!groundWord.equals(T1SentAndWordMicro.get(i + 1))) {
//                            if (!stringContainsItemFromList(groundWord, openKnownWords())) {
//                                unknownWordsMicro.add(groundWord);
//                            }
//                        }
//                    }
//
//                    System.out.println("\n");
//                }
//                java.awt.Toolkit.getDefaultToolkit().beep();
//                System.out.println("=====================");
//                System.out.print("Die Wörter in der Liste speichern? ([1] Ja / [2] Nein): ");
//                String antwortMicro = input.next();
//
//                if (antwortMicro.equals("1")) {
//                    updateKnownWords(unknownWordsMicro); //adds all the unknown words to the known word list from TopWords Project
//                    System.out.println(" gespeichert.");
//                }
//            }


            System.out.print("Los! Weiter lernen! ([jede Taste] Wiederholen) / [3] Top-Wörter speichern / [4] Zufälliger Auszug: ");
            String wait = input.next();
            if (wait.equals("3")) {
                updateKnownWords(topUnkownWords);
                System.out.println(" gespeichert.");
            } else if (wait.equals("4")) {
                randomPassage(sentences);
            }
        }

    }

    public static String getCorpus() throws IOException {
        File file = new File("C:\\Users\\Victor\\IdeaProjects\\T1SentencesFinder\\corpus.txt");
        Path path = Paths.get(file.getPath());

        String corpus = Files.readString(path, StandardCharsets.UTF_8);
        return corpus;
    }

    public static String[] stringToSentences(String corpus, boolean doMicro1T, boolean eachWord, boolean removeNewLine) {
//        String[] sentences = corpus.split("(?<=[a-z])\\.\\s+");
//        String[] sentences = corpus.split("\n|(?<=[a-z]\\.)\\s+|(?<=[a-z]\\?)\\s+|(?<=[a-z]\\!)\\s+|(?<=[a-z]\\:)\\s+|(?<=\"\\.)\\s+|(?<=\\.\")\\s+|(?<=\\.\\))\\s+|(?<=\\)\\.)\\s+|(?<=\\.«)\\s+"); //all major punc and keeping it
        //String[] sentences = corpus.split("(?:[\\u00A0\\u2007\\u202F\\p{javaWhitespace}&&[^\\n\\r]])*(\\n\\r|\\r\\n|\\n|\\r)(?:(?:[\\u00A0\\u2007\\u202F\\p{javaWhitespace}&&[^\\n\\r]])*\\1)+");
        String[] sentences; //all major punc and keeping it

        if (removeNewLine) {
            corpus = corpus.replaceAll("\n", " ");
            corpus = corpus.replaceAll("\r", "");
        }

        corpus = corpus.replaceAll("\t", ""); //all tabs are to space (especially if in a sent)

        if (eachWord) {
            sentences = corpus.split("\\s+|\n"); //all words will be divided as one sentence (to find all definitions of all unknown words
        } else {
            corpus = corpus.replace("Mr.","Mrⱶ");
            corpus = corpus.replace("Dr.","Drⱶ");
            corpus = corpus.replace("St.","Stⱶ");
            corpus = corpus.replace("z.B.","zⱶBⱶ");
            corpus = corpus.replace("Ms.","Msⱶ");
            corpus = corpus.replace("Mrs.","Mrsⱶ");
            corpus = corpus.replace("a. a. O.","aⱶ aⱶ Oⱶ");
            corpus = corpus.replace("Abb.","Abbⱶ");
            corpus = corpus.replace("Abh.","Abhⱶ");
            corpus = corpus.replace("Abk.","Abkⱶ");
            corpus = corpus.replace("allg.","allgⱶ");
            corpus = corpus.replace("bes.","besⱶ");
            corpus = corpus.replace("bez.","bezⱶ");
            corpus = corpus.replace("Bez.","Bezⱶ");
            corpus = corpus.replace("bzw.","bzwⱶ");
            corpus = corpus.replace("eigtl.","eigtlⱶ");
            corpus = corpus.replace("erg.","ergⱶ");
            corpus = corpus.replace("geb.","gebⱶ");
            corpus = corpus.replace("gegr.","gegrⱶ");
            corpus = corpus.replace("Ggs.","Ggsⱶ");
            corpus = corpus.replace("i. e. S.","iⱶ eⱶ Sⱶ");
            corpus = corpus.replace("i. w. S.","iⱶ wⱶ Sⱶ");
            corpus = corpus.replace("jmd.","jmdⱶ");
            corpus = corpus.replace("jmdm.","jmdmⱶ");
            corpus = corpus.replace("jmdn.","jmdnⱶ");
            corpus = corpus.replace("jmds.","jmdsⱶ");
            corpus = corpus.replace("o. Ä.","oⱶ Äⱶ");
            corpus = corpus.replace("scherzh.","scherzhⱶ");
            corpus = corpus.replace("u. a.","uⱶ aⱶ");
            corpus = corpus.replace("u. Ä.","uⱶ Äⱶ");
            corpus = corpus.replace("übertr.","übertrⱶ");
            corpus = corpus.replace("u. dgl.","uⱶ dglⱶ");
            corpus = corpus.replace("ugs.","ugsⱶ");
            corpus = corpus.replace("urspr.","ursprⱶ");
            corpus = corpus.replace("usw.","uswⱶ");
            corpus = corpus.replace("zz./zzt.","zzⱶ/zztⱶ");
            corpus = corpus.replace("Adv.","Advⱶ");
            corpus = corpus.replace("ahd.","ahdⱶ");
            corpus = corpus.replace("Akk.","Akkⱶ");
            corpus = corpus.replace("Buchw.","Buchwⱶ");
            corpus = corpus.replace("Dat.","Datⱶ");
            corpus = corpus.replace("dt.","dtⱶ");
            corpus = corpus.replace("Ez.","Ezⱶ");
            corpus = corpus.replace("Gen.","Genⱶ");
            corpus = corpus.replace("Gramm.","Grammⱶ");
            corpus = corpus.replace("idg.","idgⱶ");
            corpus = corpus.replace("intr.","intrⱶ");
            corpus = corpus.replace("Konj.","Konjⱶ");
            corpus = corpus.replace("Kunstw.","Kunstwⱶ");
            corpus = corpus.replace("Kurzw.","Kurzwⱶ");
            corpus = corpus.replace("Lit.","Litⱶ");
            corpus = corpus.replace("mhd.","mhdⱶ");
            corpus = corpus.replace("Mz.","Mzⱶ");
            corpus = corpus.replace("nddt.","nddtⱶ");
            corpus = corpus.replace("Nom.","Nomⱶ");
            corpus = corpus.replace("Part. II ","Partⱶ II ");
            corpus = corpus.replace("Pl.","Plⱶ");
            corpus = corpus.replace("Präp.","Präpⱶ");
            corpus = corpus.replace("Pron.","Pronⱶ");
            corpus = corpus.replace("refl.","reflⱶ");
            corpus = corpus.replace("Sing.","Singⱶ");
            corpus = corpus.replace("Spr.","Sprⱶ");
            corpus = corpus.replace("Sprachw.","Sprachwⱶ");
            corpus = corpus.replace("tr.","trⱶ");
            corpus = corpus.replace("Zus.","Zusⱶ");


            sentences = corpus.split("\n|(?<=[a-z]\\.{1,3})\\s+|(?<=[a-z]\\?)\\s+|(?<=[a-z]\\!)\\s+|(?<=\"\\.)\\s+|(?<=\\.\")\\s+|(?<=\\.\\))\\s+|(?<=\\)\\.)\\s+|(?<=\\.«)\\s+|(?<=\\!«)\\s+|(?<=\\?«)\\s+|(?<=[a-z]\\:)|(?<=\\.»)\\s+|(?<=\\!»)\\s+|(?<=\\?»)\\s+"); //all major punc and keeping it
//            sentences = corpus.split("\n|(?<=[a-z]\\.)\\s+|(?<=[a-z]\\?)\\s+|(?<=[a-z]\\!)\\s+|(?<=[a-z]\\:)\\s+|(?<=\"\\.)\\s+|(?<=\\.\")\\s+|(?<=\\.\\))\\s+|(?<=\\)\\.)\\s+|(?<=\\.«)\\s+");
            for (int v = 0; v < sentences.length; v++) {
                sentences[v] = sentences[v].replaceAll("ⱶ",".");
            }
        }

//        for(String n : sentences) {
//            System.out.println(n);
//        }

        ArrayList<String> arraySentence = new ArrayList<>();
        if (doMicro1T && !eachWord) {
            String[] kons = {" dann "," und "," dass "," aber "," oder "," wie "," wenn "," wenn "," als "," weil "," während "," wo "," nach "," so "," durch "," seit "," bis "," ob "," vor "," obwohl "," sowie "," wie "," einmal "," außer "," inzwischen "};
            for (String sentence : sentences) {
//                System.out.println("hello");
                for (String kon : kons) {
                    if (sentence.toLowerCase().contains(kon)) {
                        int index = sentence.indexOf(kon);
                        try { //sometimes it doesn't work
                            String firstDiv = sentence.substring(0, index);
                            String secondDiv = sentence.substring(index, sentence.length());
                            arraySentence.add(firstDiv + " ..."); //add the ...
                            arraySentence.add("..." + secondDiv);
                        } catch (Exception e) {

                        }
                    }
                }
            }
            String[] sents = arraySentence.toArray(sentences);
//            for(String n : sents) {
//                System.out.println(n);
//            }
            System.out.println("Mikro-Sätze: "+sents.length);
            return sents;
        } else {
            System.out.println("Sätze:  "+sentences.length);
            return sentences;
        }

    }

    public static ArrayList<String> removePuncCaps(String[] sentences) {
        ArrayList<String> puncLessSentences = new ArrayList<>();

        for (String n : sentences) {
            if (n != null) {
                String h = n.replaceAll("\\p{Punct}", " "); //punc (has a space because of dashed-words
                h = h.replaceAll("\\d", ""); //remove numbers
                h = h.replaceAll("–", " "); //dashes removed
                h = h.replaceAll("-", " "); //my dashes removed
                h = h.replaceAll("»", ""); //quotes removed
                h = h.replaceAll("«", "");
                h = h.replaceAll("\\(", ""); //brackets
                h = h.replaceAll("\\)", ""); //brackets
                h = h.replaceAll("\"", ""); //quotes
                h = h.replaceAll("\n", ""); //paragraph line
                h = h.replaceAll("♪", ""); //music emoji
                h = h.replaceAll("„", ""); //german quote
                h = h.replaceAll("“", ""); //german quote
                h = h.replaceAll("‹", ""); //german guillmet single quote
                h = h.replaceAll("›", ""); //german guillmet single quote
                h = h.replaceAll("…", ""); //three condensed dots
                h = h.replaceAll("\t", ""); //tab
//            h = h.replaceAll("\\uFEFF",""); //removes mysterious '?' - doesn't work - just remove BOM
                h = h.trim().replaceAll(" +", " "); //makes each space into one exactly
//            h = h.toLowerCase();
                puncLessSentences.add(h);
            }
        }

        //to get the amount of words in the corpus
        int totalWordsAmount = 0;
        for (int i = 0; i < puncLessSentences.size(); i ++) {
            String[] words = puncLessSentences.get(i).split(" ");
            int totalWords = words.length;
            totalWordsAmount += totalWords;
        }
        System.out.println("Wörter: "+totalWordsAmount);
//        for(String n : puncLessSentences) {
//            System.out.println(n);
//        }
        return puncLessSentences;

    }

    public static ArrayList<String> T1SentenceGen(String[] sentences, ArrayList<String> puncLessSentences) throws IOException {
        ArrayList<String> knownWords = openKnownWords();
//        ArrayList<String> T1Sentences = new ArrayList<>();
        ArrayList<String> T1SentAndWord = new ArrayList<>();
        int totalWordsAmount = 0;

        for (int i = 0; i < puncLessSentences.size(); i ++) {
            String[] words = puncLessSentences.get(i).split(" ");
            int score = 0;
            int totalWords = words.length;
            totalWordsAmount += totalWords;
            String unknownWord = "";
            for (int j = 0; j < words.length; j ++) {
                if (stringContainsItemFromList(words[j], knownWords)) {
                    score += 1;
                } else {
                    unknownWord = words[j];
                }
                //System.out.println(score)
            }
            //System.out.println(score+"/"+totalWords);
            if (totalWords == (score+1)) {
//                T1Sentences.add(sentences[i]);

                T1SentAndWord.add(sentences[i]);
                T1SentAndWord.add(unknownWord);
            }
        }
//        return T1Sentences;
        return T1SentAndWord;
    }

    public static double addProbableKnownWords(ArrayList<String> puncLessSentences) throws IOException {

//        System.out.print("Wird geladen... (");

        ArrayList<String> probableKnownWords = new ArrayList<>();
        ArrayList<String> knownWords = openKnownWords();
        ArrayList<String> commonWords = openCommonWords();
        double progressPer = (((double) 1/puncLessSentences.size())*100);
        float per = (float) progressPer;
        float per1 = (float) progressPer;

        //creates stopwatch, records time
        ArrayList<Double> timesTaken = new ArrayList<>();
        double start = 0;

        double finish = 0;


        for (int i = 0; i < puncLessSentences.size(); i ++) {

            if (i != 0) {
                double d = finish - start;
//            System.out.println(d);
                timesTaken.add(d);
                double allTimes = 0;

                //calculates ETA based on average of time taken/per
                for (int n = 0; n < timesTaken.size(); n++) {
                    allTimes += timesTaken.get(n);
                }
                double average = allTimes / timesTaken.size();
                double loopsLeft = puncLessSentences.size() - i;
                double timeToGo = average * loopsLeft;
//            System.out.println("Time Remaining: "+timeToGo);

                //current time+time remaining = ETA done
                double currentTime = System.currentTimeMillis();
                double eta = round(currentTime + timeToGo, 0);
//                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                Date resultdate = new Date((long) eta);

                String bar = loadingBar(round(per,1));

                System.out.print("Wird geladen... "+bar+" ("+round(per,1)+"%) ");
                System.out.print("ETA: "+sdf.format(resultdate));

            } else {
                String bar = loadingBar(round(per,1));
                System.out.print("Wird geladen... "+bar+" ("+round(per,1)+"%) ");
            }
            start = System.currentTimeMillis();
            String[] words = puncLessSentences.get(i).split(" ");

            System.out.print("\r"); //resets pointer to front

            per += per1;

            for (int j = 0; j < words.length; j ++) {
//                String baseWord = WebScrape.getGroundWord(words[j]); //gets baseWord from dwds (slow)
//
//                if (!stringContainsItemFromList(words[j], knownWords) && stringContainsItemFromList(baseWord, openKnownWords())) {
//                    probableKnownWords.add(words[j]);
//                } else if (stringContainsItemFromList(words[j], knownWords) && !stringContainsItemFromList(baseWord, openKnownWords())) {
//                    if (!baseWord.equals("aoeu")) { //usually names end up here
//                        probableKnownWords.add(baseWord);
//                    }
//                }

                if (!stringContainsItemFromList(words[j], commonWords)) {
                    String baseWord = WebScrape.getGroundWord(words[j]); //gets baseWord from dwds (slow)

                    if (!stringContainsItemFromList(words[j], knownWords) && stringContainsItemFromList(baseWord, openKnownWords())) {
                        probableKnownWords.add(words[j]);
                    } else if (stringContainsItemFromList(words[j], knownWords) && !stringContainsItemFromList(baseWord, openKnownWords())) {
                        if (!baseWord.equals("aoeu")) { //usually names end up here
                            probableKnownWords.add(baseWord);
                        }
                    }
                }

            }

            finish = System.currentTimeMillis();

        }

        System.out.print("\r");
        updateKnownWords(probableKnownWords);

        double totalTime = 0;
        for (int j = 0; j < timesTaken.size(); j ++) {
            totalTime += timesTaken.get(j);
        }
        return totalTime;

    }

    public static ArrayList<String> openKnownWords() throws IOException {
        URL url = new URL("file:///C:/Users/Victor/IdeaProjects/T1SentencesFinder/learnedWords.txt");
        InputStream in1 = url.openStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(in1, StandardCharsets.UTF_8));
        String str;
        ArrayList<String> list = new ArrayList<String>();
        while((str = in.readLine()) != null){
            list.add(str);
        }
        return list;
    }

    public static ArrayList<String> openCommonWords() throws IOException {
        URL url = new URL("file:///C:/Users/Victor/IdeaProjects/T1SentencesFinder/commonWords.txt");
        InputStream in1 = url.openStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(in1, StandardCharsets.UTF_8));
        String str;
        ArrayList<String> list = new ArrayList<String>();
        while((str = in.readLine()) != null){
            list.add(str);
        }
        return list;
    }

    public static boolean stringContainsItemFromList(String inputStr, ArrayList<String> items) {
        for(int i =0; i < items.size(); i++)
        {
            if(inputStr.equalsIgnoreCase(items.get(i))) {
                return true;
            }
        }
        return false;
    }

    public static void updateKnownWords(ArrayList<String> newWords) throws IOException {


        ArrayList<String> newWords1 = new ArrayList<>(); //removes duplicates
        for (String s : newWords) {
            s = s.toLowerCase();
            if (!newWords1.contains(s)) {
                newWords1.add(s);
            }
        }

//        for (String s : newWords1) {
//            System.out.println(s);
//        }

        URL url = new URL("file:///C:/Users/Victor/IdeaProjects/T1SentencesFinder/learnedWords.txt");
        InputStream in1 = url.openStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(in1, StandardCharsets.UTF_8));
        String str5;

        ArrayList<String> originalNewWords = new ArrayList<String>();
        while((str5 = in.readLine()) != null){
            originalNewWords.add(str5);
        }
        ArrayList<String> allNewWords = new ArrayList<String>();
        allNewWords.addAll(originalNewWords);
        allNewWords.addAll(newWords1);

        OutputStream os1 = new FileOutputStream("C:\\Users\\Victor\\IdeaProjects\\T1SentencesFinder\\learnedWords.txt");
        OutputStreamWriter outputStreamWriter1 = new OutputStreamWriter(os1, StandardCharsets.UTF_8);
        for(String str2: allNewWords) {
            outputStreamWriter1.write(str2+"\n");
        }
        outputStreamWriter1.flush();
        outputStreamWriter1.close();
    }

//    public static ArrayList<String> T2SentenceGen(String[] sentences, ArrayList<String> puncLessSentences) throws IOException {
//        ArrayList<String> knownWords = openKnownWords();
//        ArrayList<String> T2SentAndWord = new ArrayList<>();
//
//        for (int i = 0; i < puncLessSentences.size(); i ++) {
//            String[] words = puncLessSentences.get(i).split(" ");
//            int score = 0;
//            int totalWords = words.length;
//            ArrayList<String> unknownWords = new ArrayList<>();
//            for (int j = 0; j < words.length; j ++) {
//                if (stringContainsItemFromList(words[j], knownWords)) {
//                    score += 1;
//                } else {
//                    unknownWords.add(words[j]);
//                }
//                //System.out.println(score)
//            }
//            //System.out.println(score+"/"+totalWords);
//            if (totalWords == (score+2)) {
////                T1Sentences.add(sentences[i]);
//
//                T2SentAndWord.add(sentences[i]);
//                T2SentAndWord.addAll(unknownWords);
//            }
//        }
////        return T1Sentences;
//        return T2SentAndWord;
//    }

    private static ArrayList<String> unknownCommonWordFinder(ArrayList<String> puncLessSentences) throws IOException {

        ArrayList<String> allWords = new ArrayList<>();
        ArrayList<String> knownWords = openKnownWords();
        ArrayList<String> forSave = new ArrayList<>();

        //get all words in text to array
        for (int i = 0; i < puncLessSentences.size(); i ++) {
//            puncLessSentences.set(i, puncLessSentences.get(i).toLowerCase());
            String[] words = puncLessSentences.get(i).split(" ");
            allWords.addAll(Arrays.asList(words));
        }

        Set<String> noDupWords = new HashSet<>(allWords);
        ArrayList<String> noDupAllWords = new ArrayList<>(noDupWords);
        ArrayList<String> realAllWords = new ArrayList<>();
        ArrayList<String> noDupAllWordsNum = new ArrayList<>();
//        float totalWordsAmount = noDupAllWords.size();

        //delete known words
        for (String word : noDupAllWords) {
            if (!stringContainsItemFromList(word,knownWords)) {
                realAllWords.add(word);
            }
        }

        //weighted w freq dele known words
        ArrayList<String> unknownWordsWDup = new ArrayList<>();
        for (String word : allWords) {
            if (!stringContainsItemFromList(word, knownWords)) {
                unknownWordsWDup.add(word);
            }
        }
        float totalWordsAmount = allWords.size();
        float totalUnknownWordsAmount = unknownWordsWDup.size();

        //calculate % of known text - 80% is not knowing 1 word in 4 sentences in each page of a novel
        float knownTextPer = 100 - ((totalUnknownWordsAmount / totalWordsAmount) * 100);
        System.out.println("Verständnis: "+round(knownTextPer,1)+"%");

        System.out.println("Top Unbekannte Wörter:");
        //calculate frequencies
        for (int i = 0; i < realAllWords.size(); i++) {
            int freqCount = 0;
            for (int x = 0; x < allWords.size(); x++) {
                if (realAllWords.get(i).equals(allWords.get(x))) {
                    freqCount += 1;
                }
            }
            noDupAllWordsNum.add(realAllWords.get(i));
            noDupAllWordsNum.add(String.valueOf(freqCount));
        }

        //get top freq
        for (int q = 0; q < 4; q ++) {
            int max = 1;
            int index = 0;
            for (int e = 1; e < noDupAllWordsNum.size(); e += 2) {
                int num = Integer.parseInt(noDupAllWordsNum.get(e));
                if (num > max) {
                    index = e;
                    max = num;
                }
            }

            try {
                System.out.printf("%-4s%-14s%-10d", "  " + (q + 1) + ". ", noDupAllWordsNum.get(index - 1), max);
                System.out.println();
//                System.out.println("  " + (q + 1) + ". " + noDupAllWordsNum.get(index - 1) + " " + max);
                forSave.add(noDupAllWordsNum.get(index - 1));
                noDupAllWordsNum.remove(index);
                noDupAllWordsNum.remove(index - 1);
            } catch (Exception e) {

            }
        }

//        for (String n : forSave) {
//            System.out.println(n);
//        }

        return forSave;
    }

    public static void randomPassage(String[] sentences) {
        Random ran = new Random();
        Scanner input = new Scanner(System.in);
        int getPlace = ran.nextInt(sentences.length)-4;
        int paraSize = ran.nextInt(4)+4;
//        ArrayList<String> selectedPara = new ArrayList<>();
        System.out.print("Auszug ("+paraSize+"): ");
        try {
            for (int t = getPlace; t < (getPlace + paraSize); t++) {
                System.out.print(sentences[t] + " ");
            }
        } catch(Exception e) {
            System.out.print("/Fehlgeschlagen/");
        }
        System.out.println();
        String wait = input.next();
    }

    public static boolean openConfig(int index) throws IOException {
        URL url = new URL("file:///C:/Users/Victor/IdeaProjects/T1SentencesFinder/config.txt");
        InputStream in1 = url.openStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(in1, StandardCharsets.UTF_8));
        String str;
        ArrayList<String> list = new ArrayList<String>();
        while((str = in.readLine()) != null){
            list.add(str);
        }
        if (list.get(index).equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static String boldTarget(String sentence, String targetWord) {
        // Extract words
        String[] words = sentence.split("\\s+|[-]");

        // Manipulate them
        for (int i = 0; i < words.length; i++) {

            String punclessWordLast = "";
            String punclessWordFirst = "";

            if (!words[i].equals("")) {
                try {
                    punclessWordFirst = removeFirstChar(words[i]);
                } catch (Exception e) {
                    punclessWordFirst = "xxxx";
                }
                punclessWordLast = words[i].substring(0, words[i].length() - 1);
                String punclessWordBoth = removeFirstChar(punclessWordLast);

                String punclessWordTwiceLast = "";
                String punclessWordThriceLast = "";
                try {
                    punclessWordTwiceLast = words[i].substring(0, words[i].length() - 2);
                    punclessWordThriceLast = words[i].substring(0, words[i].length() - 3);
                } catch (Exception ignored) {

                }

                if (words[i].substring(1).matches("\\p{Punct}|[–»«♪„“‹›…]") && punclessWordFirst.equals(targetWord)) {
                    //first char

                    String punc = words[i].substring(0, 1); //gets first char
                    words[i] = removeFirstChar(words[i]); //removes first char

                    words[i] = "<b>" + words[i] + "</b>"; //bolds
                    words[i] = punc + words[i]; //brings first char back


                } else if (words[i].substring(words[i].length() - 1).matches("\\p{Punct}|[–»«♪„“‹›…]") && punclessWordLast.equals(targetWord)) {
                    //last char

                    String punc = words[i].substring(words[i].length() - 1); //gets last char
                    words[i] = words[i].substring(0, words[i].length() - 1); //removes it

                    words[i] = "<b>" + words[i] + "</b>"; //bolds
                    words[i] = words[i] + punc; //brings last char back

                } else if (punclessWordBoth.equals(targetWord)) {
                    // both
                    String puncfirst = words[i].substring(0, 1); //gets first char
                    words[i] = removeFirstChar(words[i]); //removes first char
                    String punclast = words[i].substring(words[i].length() - 1); //gets last char
                    words[i] = words[i].substring(0, words[i].length() - 1); //removes it

                    words[i] = "<b>" + words[i] + "</b>"; //bolds
                    words[i] = puncfirst + words[i] + punclast; //brings both chars back

                } else if (words[i].equals(targetWord)) {
                    //none
                    words[i] = "<b>" + words[i] + "</b>"; //bolds
                } else if (punclessWordTwiceLast.equals(targetWord)) { //removes last char twice if there are double punc
                    String punc = words[i].substring(words[i].length() - 2); //gets twice last char
                    words[i] = words[i].substring(0, words[i].length() - 2); //removes it

                    words[i] = "<b>" + words[i] + "</b>"; //bolds
                    words[i] = words[i] + punc; //brings last char back
                } else if (punclessWordThriceLast.equals(targetWord)) { //removes last char twice if there are double punc
                    String punc = words[i].substring(words[i].length() - 3); //gets thrice last char
                    words[i] = words[i].substring(0, words[i].length() - 3); //removes it

                    words[i] = "<b>" + words[i] + "</b>"; //bolds
                    words[i] = words[i] + punc; //brings last char back
                }
            }


//            if(words[i].matches("\\p{Punct}")) {
//                hasPunc = true;
//                punclessWord = words[i].substring(0, words[i].length() - 1);
//            }
//
//            if (words[i].equals(targetWord) && !hasPunc) {
//                words[i] = "<b>" + words[i] + "</b>"; //bolds
//            } else if (punclessWord.equals(targetWord) && hasPunc) {
//                String punc = words[i].substring(words[i].length() - 1); //gets last char
//                words[i] = words[i].substring(0, words[i].length() - 1); //removes it
//
//                words[i] = "<b>" + words[i] + "</b>"; //bolds
//                words[i] = words[i]+punc; //brings last char back
//            }
        }

        // Put them back as sentence
        StringJoiner sj = new StringJoiner(" ");
        for (String word : words) {
            sj.add(word);
        }

        String boldSentence = sj.toString();
        return boldSentence;
    }

    public static void setBild() {
        String Bedeutung = GlobalVar.Bedeutung;
        String[] bedArray = Bedeutung.split(":");
        String bildWord = "";
        try {
            bildWord = bedArray[0];
        } catch (ArrayIndexOutOfBoundsException ignored) {

        }

        try {
            if (bedArray[1].equals(" ")) {
                GlobalVar.isDeadBed = true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            GlobalVar.isDeadBed = true;
        }

        if(bildWord.contains(".")) {
            int index = bildWord.indexOf(".");
            GlobalVar.Bild = bildWord.substring(index+2, bildWord.length());
        } else {
            GlobalVar.Bild = bildWord;
        }


    }

    public static void setQuelle() {
        Scanner input = new Scanner(System.in);
        System.out.print("Setze Quelle für die Karten: ([*] für Vplan, [%] Laden übrsprgn) ");
        GlobalVar.Quelle = input.nextLine();
    }

    public static String removeFirstChar(String s){
        try {
            return s.substring(1);
        } catch (Exception e) {
            return s;
        }
    }

    public static String loadingBar(double percentage) {
        //31 squares
        double percent = percentage;
        double numberOfFullSquares = round(percent/3.125,0);
        String bar = "[";

//        System.out.print("[");
        for (int i = 0; i < numberOfFullSquares; i++) {
//            System.out.print("█");
            bar += "█";
        }

        for (int a = 0; a < 31-numberOfFullSquares; a ++) {
//            System.out.print("░");
            bar += "░";
        }

//        System.out.print("]");
        bar += "]";

        return bar;

//        System.out.print("[░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░]");
//        System.out.print("[████████████████████████████████]");
    }
}
