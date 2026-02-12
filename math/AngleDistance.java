package math;

//excelent example of an open closed principle.
public class AngleDistance implements DistanceMetric {
    @Override
    public double calculate(double[] v1, double[] v2) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];       // מכפלה סקלרית
            normA += Math.pow(v1[i], 2);       // סכום ריבועי וקטור א'
            normB += Math.pow(v2[i], 2);       // סכום ריבועי וקטור ב'
        }

        // חישוב המכנה (מכפלת האורכים)
        double divisor = Math.sqrt(normA) * Math.sqrt(normB);

        // מניעת חלוקה באפס במקרה של וקטור ריק
        if (divisor == 0) return 0;

        // חישוב הקוסינוס
        double cosineSimilarity = dotProduct / divisor;

        /* * שים לב: Cosine Similarity מחזיר 1 כשהמילים זהות (זווית 0).
         * מאחר והמערכת שלך מחפשת "מרחק" (Distance), אנחנו נחזיר (1 מינוס הדמיון).
         * כך: דמיון 1 (זהה) יהפוך למרחק 0.
         */
        return 1.0 - cosineSimilarity;
    }

    @Override
    public String getName() { return "Angle Distance"; }
}