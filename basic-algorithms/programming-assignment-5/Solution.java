import java.io.*;
import java.lang.*;
import java.util.*;

public class Solution {
    static int[][] scores;
    
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        String seqOne = input.next();
        String seqTwo = input.next();

        input.close();

        scores = new int[seqOne.length() + 1][seqTwo.length() + 1];
        for (int i = 0; i < scores.length; i++) {
            for (int j = 0; j < scores[0].length; j++) {
                scores[i][j] = 0;
            }
        }

        align(seqOne, seqTwo);
        System.out.println(scores[seqOne.length()][seqTwo.length()]);

        printAlignment(seqOne, seqTwo);
    }

    static void align(String seqOne, String seqTwo) {
        for (int i = 0; i < seqOne.length() + 1; i++) {
            for (int j = 0; j < seqTwo.length() + 1; j++) {
                if (i == 0 && j != 0)
                    scores[i][j] = scores[i][j - 1] - 1;
                else if (i != 0 && j == 0)
                    scores[i][j] = scores[i - 1][j] - 1;
                else if (i != 0 && j != 0) {
                    if (seqOne.charAt(i - 1) == seqTwo.charAt(j - 1))
                        scores[i][j] = Math.max(Math.max(scores[i - 1][j] - 1, scores[i][j - 1] - 1), scores[i - 1][j - 1] + 2);
                    else
                        scores[i][j] = Math.max(Math.max(scores[i - 1][j] - 1, scores[i][j - 1] - 1), scores[i - 1][j - 1] - 2);
                }
            }
        }
    }

    static void printAlignment(String seqOne, String seqTwo) {
        String alignmentOne = "";
        String alignmentTwo = "";

        int i = seqOne.length();
        int j = seqTwo.length();
        while (i > 0 || j > 0) {
            if (i != 0 && scores[i][j] == scores[i - 1][j] - 1) {
                alignmentOne = Character.toString(seqOne.charAt(i - 1)) + alignmentOne;
                alignmentTwo = "-" + alignmentTwo;
                i--;
            }
            else if (j != 0 && scores[i][j] == scores[i][j - 1] - 1) {
                alignmentOne = "-" + alignmentOne;
                alignmentTwo = Character.toString(seqTwo.charAt(j - 1)) + alignmentTwo;
                j--;
            }
            else {
                alignmentOne = Character.toString(seqOne.charAt(i - 1)) + alignmentOne;
                alignmentTwo = Character.toString(seqTwo.charAt(j - 1)) + alignmentTwo;
                i--;
                j--;
            }
        }

        System.out.println(alignmentOne);
        System.out.println(alignmentTwo);
    }
}