package math;
//תבנית אסטרטגיה עם EuclideanDistance ועם  AngleDistance
// ממשק זה יאפשר לך להוסיף מטריקות חדשות בקלות בעתיד
public interface DistanceMetric {
    double calculate(double[] v1, double[] v2);
    String getName();
}


//VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV