import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.round;

public class Prelecture {

    public static void prelecture(int targetPer, ArrayList<String> puncLessSentences, String[] sentences) throws IOException {

//        int targetPer = 90; //target comprehensibility for a given text
        ArrayList<String> allWords = new ArrayList<>();
        ArrayList<String> knownWords = Main.openKnownWords();
        ArrayList<String> forSave = new ArrayList<>();
        GlobalVar.Quelle = "<i>Vorlesung</i>: "+GlobalVar.Quelle;

        //get all words in text to array
        for (int i = 0; i < puncLessSentences.size(); i++) {
//            puncLessSentences.set(i, puncLessSentences.get(i).toLowerCase());
            String[] words = puncLessSentences.get(i).split(" ");
            allWords.addAll(Arrays.asList(words));
        }

        Set<String> noDupWords = new HashSet<>(allWords);
        ArrayList<String> noDupAllWords = new ArrayList<>(noDupWords);
        ArrayList<String> realAllWords = new ArrayList<>();
        ArrayList<String> noDupAllWordsNum = new ArrayList<>();
        float totalWordsAmount = allWords.size();

        //delete known words
        for (String word : noDupAllWords) {
            if (!Main.stringContainsItemFromList(word, knownWords)) {
                realAllWords.add(word);
            }
        }

        //weighted w freq dele known words
        ArrayList<String> unknownWordsWDup = new ArrayList<>();
        for (String word : allWords) {
            if (!Main.stringContainsItemFromList(word, knownWords)) {
                unknownWordsWDup.add(word);
            }
        }

        float totalUnknownWordsAmount = unknownWordsWDup.size();
        float totalKnownWordsAmount = totalWordsAmount-totalUnknownWordsAmount;

        //calculate % of known text - 80% is not knowing 1 word in 4 sentences in each page of a novel
        float knownTextPer = 100 - ((totalUnknownWordsAmount / totalWordsAmount) * 100);

        //to get the amount of words in the corpus
        int allWordsInCorpus = 0;
        for (int i = 0; i < puncLessSentences.size(); i ++) {
            String[] words = puncLessSentences.get(i).split(" ");
            int totalWords = words.length;
            allWordsInCorpus += totalWords;
        }

//        float oasisPages = allCharsInCorpus/1048;
//        float unknownWordsPerPage = totalUnknownWordsAmount/oasisPages;

//        System.out.println("Unbek. Wort pro Seite: "+round(unknownWordsPerPage * 100.0)/100.0);
        System.out.println("Unbek. Wörter:         "+round(totalUnknownWordsAmount)+"/"+round(allWordsInCorpus));
        System.out.println("Bek. Wörter:           "+round(totalKnownWordsAmount)+"/"+round(allWordsInCorpus));
        System.out.println("==== Vorlesungsplan (Ziel: " + round(knownTextPer) + "% ➔ "+targetPer+"%) ====");
        GlobalVar.prelecturePer += knownTextPer;

        if (knownTextPer >= targetPer) {
            System.out.println("Fehler: Zielprozentwert ist niedriger als aktueller Prozentsatz");
        }

        //===

//        System.out.println("Top Unbekannte Wörter:");
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
        for (int q = 0; q < noDupAllWordsNum.size(); q++) {
            int max = 1;
            int index = 0;
            for (int e = 1; e < noDupAllWordsNum.size(); e += 2) {
                int num = 0;
                try {
                    num = Integer.parseInt(noDupAllWordsNum.get(e));
                } catch (Exception r) {
                    break;
                }
                if (num > max) {
                    index = e;
                    max = num;
                }
            }

            try {
                if (GlobalVar.prelecturePer >= targetPer) {
                    break;
                }

                String word = noDupAllWordsNum.get(index - 1);
                GlobalVar.prelecturePer = newPerIfWordKnown(max, puncLessSentences);
//                System.out.println("  " + (q + 1) + ". " + word + " " + max + " ➔" +GlobalVar.prelecturePer+"% "+getWordsTValue(word,sentences)+"T");
                System.out.printf("%-7s%-21s%-3s%-10s%-1s","  "+(q+1)+".",word,max," ➔" +round(GlobalVar.prelecturePer*10.0)/10.0+"%",getWordsTValue(word,sentences)+"T");
                System.out.println();
                addPrelectureToAnki(word,sentences); // adds to anki
                forSave.add(noDupAllWordsNum.get(index - 1));
                noDupAllWordsNum.remove(index);
                noDupAllWordsNum.remove(index - 1);
            } catch (Exception e) {
                try {
                    int maxT = 1;
                    int in = q+1;
                    while (maxT < 21) {
                        try {
                            for (int i = q + 2; i < noDupAllWordsNum.size() + q + 1; i += 2) {

                                if (GlobalVar.prelecturePer >= targetPer) {
                                    break;
                                }

                                String word = noDupAllWordsNum.get(i - 1);
                                if (getWordsTValue(word, sentences) == maxT) {
                                    GlobalVar.prelecturePer = newPerIfWordKnown(1, puncLessSentences);
//                                    System.out.println("  " + in + ". " + word + " " + 1 + " ➔" + GlobalVar.prelecturePer + "% " + getWordsTValue(word, sentences) + "T");
                                    System.out.printf("%-7s%-21s%-3s%-10s%-1s","  " + in + ".",word,1," ➔" +round(GlobalVar.prelecturePer*10.0)/10.0+"%",getWordsTValue(word,sentences)+"T");
                                    System.out.println();
                                    addPrelectureToAnki(word,sentences); //adds to anki
                                    noDupAllWordsNum.remove(i);
                                    noDupAllWordsNum.remove(i + 1);
                                    in++;
                                }
                            }
                        } catch (Exception a) {

                        }
                        maxT++;
                    }
//                break;
                } catch (Exception t) {
                    break;
                }
            }
        }
        java.awt.Toolkit.getDefaultToolkit().beep();
        System.out.println("Vorlesungswörter gespeichert. Los Überprüfung!");

    }

    public static float newPerIfWordKnown(int wordAmount, ArrayList<String> puncLessSentences) throws IOException {
        ArrayList<String> allWords = new ArrayList<>();
        ArrayList<String> knownWords = Main.openKnownWords();
//        ArrayList<String> forSave = new ArrayList<>();

        //get all words in text to array
        for (int i = 0; i < puncLessSentences.size(); i++) {
//            puncLessSentences.set(i, puncLessSentences.get(i).toLowerCase());
            String[] words = puncLessSentences.get(i).split(" ");
            allWords.addAll(Arrays.asList(words));
        }

        float totalWordsAmount = allWords.size();

        //weighted w freq dele known words
        ArrayList<String> unknownWordsWDup = new ArrayList<>();
        for (String word : allWords) {
            if (!Main.stringContainsItemFromList(word, knownWords)) {
                unknownWordsWDup.add(word);
            }
        }

        //calculate % when the topUnknownWord would be added
        float totalUnknownWordsAmount = unknownWordsWDup.size()-wordAmount;
        return 100 - ((totalUnknownWordsAmount / totalWordsAmount) * 100);
    }

    public static String getWordsSentence(String word, String[] sentences) {
        //gets sentence that the word is part of
        for (String sentence : sentences) {
            if (sentence.contains(word)) {
                return sentence;
            }
        }
        return null;
    }

    public static int getWordsTValue(String word, String[] sentences) throws IOException {
        ArrayList<String> knownWords = Main.openKnownWords();

        //gets sentence that the word is part of
        String sentence = getWordsSentence(word, sentences);

        //get T value of the sentence
        int totalWordsAmount = 0;
        String[] words = sentence.split(" ");
        int score = 0;
        int totalWords = words.length;
        totalWordsAmount += totalWords;
        String unknownWord = "";
        for (int j = 0; j < words.length; j ++) {
            words[j] = removePunc(words[j]); //removes punctuations
            if (!Main.stringContainsItemFromList(words[j], knownWords)) {
                score += 1;
            }
        }
        return score;
    }

    public static void determinePerTarget() {
        int target = 80;
    }

    public static String removePunc(String n) {
        String h = n.replaceAll("\\p{Punct}", ""); //punc (has a space because of dashed-words
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
        h = h.trim().replaceAll(" +", "");
        return h;
    }

    public static void addPrelectureToAnki(String prelectureWord, String[] sentences) throws Exception {

        //gets sentence that the word is part of
        String sentence = getWordsSentence(prelectureWord, sentences);

        ArrayList<String> unknownWords = new ArrayList<>();
        String boldSentence = Main.boldTarget(sentence, prelectureWord);

        GlobalVar.Satz = boldSentence;
        GlobalVar.Bedeutung = "";

        String groundWord = WebScrape.getDef(prelectureWord); //returns baseWord but also prints def (slow)
        Main.setBild(); //sets Bild default output from bedeutung; also judges whether bedeutung has a full def or not

        unknownWords.add(prelectureWord);
        if (!groundWord.equals(prelectureWord)) {
            if (!Main.stringContainsItemFromList(groundWord, Main.openKnownWords())) {
                unknownWords.add(groundWord);
            }
        }
        AnkiConnect.addToAnki(GlobalVar.Satz, GlobalVar.Bild, GlobalVar.Bedeutung, GlobalVar.Quelle);
        Main.updateKnownWords(unknownWords);
    }

}
