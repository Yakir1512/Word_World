package app;

import model.WordVector;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BackupVec {

    /**
     * מייצרת רשימת ווקטורים רנדומליים למקרה של תקלה בטעינה.
     * @param count מספר המילים לייצור
     * @param dimensions מספר הממדים לכל ווקטור
     * @return רשימה של WordVector
     */
    public static List<WordVector> generate(int count, int dimensions) {
        List<WordVector> mockData = new ArrayList<>();
        Random rand = new Random();

        for (int i = 1; i <= count; i++) {
            String word = "MockWord_" + i;
            double[] vector = new double[dimensions];
            
            // מילוי ערכים רנדומליים בין 1.0- ל-1.0
            for (int d = 0; d < dimensions; d++) {
                vector[d] = -1.0 + (rand.nextDouble() * 2.0);
            }
            
            mockData.add(new WordVector(word, vector));
        }
        return mockData;
    }
}
