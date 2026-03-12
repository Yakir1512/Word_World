package app;

public class Viewport {
    public final int[] axes;
    public final double[] minVals;
    public final double[] maxVals;

    public Viewport(int[] axes, double[] minVals, double[] maxVals) {
        this.axes = axes;
        this.minVals = minVals;
        this.maxVals = maxVals;
    }
}