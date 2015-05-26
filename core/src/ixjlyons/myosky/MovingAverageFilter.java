package ixjlyons.myosky;

public class MovingAverageFilter {
    
    private float[] inputs;
    private int m;
    
    public MovingAverageFilter(int m) {
        this.m = m;
        
        inputs = new float[m];
    }
    
    public float update(float x) {
        for (int i = 0; i < m-1; i++) {
            inputs[i] = inputs[i+1];
        }
        inputs[m-1] = x;
        
        float out = 0;
        for (int i = 0; i < m; i++) {
            out += inputs[i];
        }
        out /= m;
        
        return out;
    }
}
