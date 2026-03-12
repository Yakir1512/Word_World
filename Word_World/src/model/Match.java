package model;
// Match.java (או מחלקה פנימית)
public class Match implements Comparable<Match> {
    public String word;
    public double distance;

    public Match(String word, double distance) {
        this.word = word;
        this.distance = distance;
    }

    // Getters נקיים - בלי לוגיקה
    public String getWord() { return word; }
    public double getDistance() { return distance; }

    @Override
    public int compareTo(Match other) {
        // מיון מהקטן לגדול (הכי קרוב = הכי קטן)
        return Double.compare(this.distance, other.distance);
    }
}
//VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV
//DTO מחלקת ביניים המקשרת בין הווקטור, המילה והמרחק.