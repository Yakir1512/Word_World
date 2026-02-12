package engine;

import model.*;
import math.DistanceMetric;
import java.util.*;

public class SemanticSearcher {
    private Collection<WordVector> vocabulary; // המאגר לחיפוש
    private DistanceMetric metric;             // הסרגל למדידה

    public SemanticSearcher(Collection<WordVector> vocabulary, DistanceMetric metric) {
        this.vocabulary = vocabulary;
        this.metric = metric;
    }

    // אפשרות להחליף סרגל באמצע הדרך (אוקלידי/קוסינוס)
    public void setMetric(DistanceMetric metric) {
        this.metric = metric;
    }

    // חיפוש השכנים הקרובים ביותר לווקטור מסוים
   // בתוך SemanticSearcher.java
    public List<Match> findNearest(double[] targetVector, int k) {
        List<Match> allMatches = new ArrayList<>();
        
        for (WordVector wv : vocabulary) { // מניח שיש לו גישה למילון
            // שימוש במטריקה הנוכחית (אוקלידית או קוסינוס)
            double dist = metric.calculate(targetVector, wv.getFullVector());
            allMatches.add(new Match(wv.getWord(), dist));
        }
        
        Collections.sort(allMatches);

        // מחזירים את אובייקטי ה-Match עצמם (עד k התוצאות הראשונות)
        // זה נותן לנו גמישות להחליט אח"כ מה להציג ב-UI
        if (k >= allMatches.size()) return allMatches;
        return allMatches.subList(0, k);
    }
}
//VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV