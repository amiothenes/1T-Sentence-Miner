import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Points {
    public static String levelTracker(int knownWordsAmount) throws IOException {
        String[] levels = {"A1.0","A1.1","A1.2","A1.3","A1.4","A1.5","A2.1","A2.2","A2.3","A2.4","B1.1","B1.2","B1.3",
                "B1.4","B1.5","B1.6","B1.7","B1.8","B2.1","B2.2","B2.3","B2.4","B2.5","B2.6","B2.7","B2.8","B2.9",
                "B2.10","B2.11","B2.12","C1.1","C1.2","C1.3","C1.4","C1.5","C1.6","C1.7","C1.8","!C2!"};
//        int[] lm = {250,500,750,1000,1250,1500,1750,2000,2250,2500,2750,3000,3250,3500,3750,4000,4250,4500,
//                4750,5000,5250,5500,5750,6000,6250,6500,6750,7000,7250,7500,7750,8000,8250,8500,8750,9000,9250,9500,9750,10000}; //level marker
        int[] lm = {300,600,900,1200,1500,1800,2100,2400,2700,3000,3300,3600,3900,4200,4500,4800,5100,5400,5700,6000,
                6300,6600,6900,7200,7500,7800,8100,8400,8700,9000,9300,9600,9900,10200,10500,10800,11100,11400};
        String currentLevel = "";
        int point = learnedWordsNum()+knownWordsAmount;

        if (point <= lm[0]) {
            currentLevel = levels[0];
        } else if (point <= lm[1]) {
            currentLevel = levels[1];
        } else if (point <= lm[2]) {
            currentLevel = levels[2];
        } else if (point <= lm[3]) {
            currentLevel = levels[3];
        } else if (point <= lm[4]) {
            currentLevel = levels[4];
        } else if (point <= lm[5]) {
            currentLevel = levels[5];
        } else if (point <= lm[6]) {
            currentLevel = levels[6];
        } else if (point <= lm[7]) {
            currentLevel = levels[7];
        } else if (point <= lm[8]) {
            currentLevel = levels[8];
        } else if (point <= lm[9]) {
            currentLevel = levels[9];
        } else if (point <= lm[10]) {
            currentLevel = levels[10];
        } else if (point <= lm[11]) {
            currentLevel = levels[11];
        } else if (point <= lm[12]) {
            currentLevel = levels[12];
        } else if (point <= lm[13]) {
            currentLevel = levels[13];
        } else if (point <= lm[14]) {
            currentLevel = levels[14];
        } else if (point <= lm[15]) {
            currentLevel = levels[15];
        } else if (point <= lm[16]) {
            currentLevel = levels[16];
        } else if (point <= lm[17]) {
            currentLevel = levels[17];
        } else if (point <= lm[18]) {
            currentLevel = levels[18];
        } else if (point <= lm[19]) {
            currentLevel = levels[19];
        } else if (point <= lm[20]) {
            currentLevel = levels[20];
        } else if (point <= lm[21]) {
            currentLevel = levels[21];
        } else if (point <= lm[22]) {
            currentLevel = levels[22];
        } else if (point <= lm[23]) {
            currentLevel = levels[23];
        } else if (point <= lm[24]) {
            currentLevel = levels[24];
        } else if (point <= lm[25]) {
            currentLevel = levels[25];
        } else if (point <= lm[26]) {
            currentLevel = levels[26];
        } else if (point <= lm[27]) {
            currentLevel = levels[27];
        } else if (point <= lm[28]) {
            currentLevel = levels[28];
        } else if (point <= lm[29]) {
            currentLevel = levels[29];
        } else if (point <= lm[30]) {
            currentLevel = levels[30];
        } else if (point <= lm[31]) {
            currentLevel = levels[31];
        } else if (point <= lm[32]) {
            currentLevel = levels[32];
        } else if (point <= lm[33]) {
            currentLevel = levels[33];
        } else if (point <= lm[34]) {
            currentLevel = levels[34];
        } else if (point <= lm[35]) {
            currentLevel = levels[35];
        } else if (point <= lm[36]) {
            currentLevel = levels[36];
        } else if (point <= lm[37]) {
            currentLevel = levels[37];
        } else  {
            currentLevel = levels[38]; //C2
        }

        int nextLVL = 0;
        int thisLVL = 0;
        for (int i = 0; i < levels.length-1; i++) {
            if (levels[i].equals(currentLevel)) {
                nextLVL = i+1;
                thisLVL = i;
                break;
            }
        }

        return currentLevel;
    }

    public static int learnedWordsNum() throws IOException {
        //add to new words
        URL url = new URL("file:///C:/Users/Victor/IdeaProjects/TopWordGiver/learnedWords.txt");
        InputStream in1 = url.openStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(in1, StandardCharsets.UTF_8));
        String str5;

        ArrayList<String> originalNewWords = new ArrayList<String>();
        while((str5 = in.readLine()) != null){
            originalNewWords.add(str5);
        }
        int numTotal = originalNewWords.size();
        return numTotal;
    }
}
