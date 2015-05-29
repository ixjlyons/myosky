package ixjlyons.myosky;

public class Processor {

    /** Number of samples to use in moving average filter */
    public static final int M = 3;

    private MovingAverageFilter filter;
    
    public Processor() {
        filter = new MovingAverageFilter(M);
    }
    
    public float update(float[] data) {
        float out = 0;
        
        for (int i = 0; i < data.length; i++) {
            out += data[i]*data[i];
        }
        out = (float)Math.sqrt(out/data.length);
        out = filter.update(out);
        
        return out;
    }
}
