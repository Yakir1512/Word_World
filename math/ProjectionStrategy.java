package math;

import model.WordVector;

// ProjectionStrategy.java
public interface ProjectionStrategy {
    /**
     * @param wv המילה שרוצים לצייר
     * @param axisIndices מערך של אינדקסים (איזה עמודות PCA לקחת: {0,1} לדו-ממד או {0,1,2} לתלת-ממד)
     * @param dimensions גודל המסך/המרחב (רוחב, גובה, ולפעמים עומק)
     * @param minVal הערך המינימלי בכל המרחב (לצורך נרמול)
     * @param maxVal הערך המקסימלי בכל המרחב (לצורך נרמול)
     * @return מערך של קואורדינטות מסך {x, y} או {x, y, z}
     */
    double[] project(WordVector wv, int[] axisIndices, double[] dimensions, 
                     double[] minVals, double[] maxVals);
}
//VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVה