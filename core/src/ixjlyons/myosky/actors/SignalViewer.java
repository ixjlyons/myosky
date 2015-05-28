package ixjlyons.myosky.actors;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class SignalViewer {
    
    public static final float[] YAXIS_COLOR = {
        0, 0, 0, 1
    };
    
    public static final float[] SIGNAL_COLOR = {
        0x50 / 256f,
        0xce / 256f,
        0xa2 / 256f,
        1f
    };
    
    public static final float[] THRESH_COLOR = {
        1, 0, 0, 1
    };

    private float left, top, right, bottom;
    private float xcenter, ycenter;
    private float width, height;
    private float xinc;
    private float thresh = -1;
    private float[] data;
    
    public SignalViewer(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        
        width = right - left;
        height = top - bottom;
        xcenter = left + width/2;
        ycenter = bottom + height/2;
    }
    
    public void setData(float[] data) {
        this.data = data;
        xinc = width / data.length;
    }
    
    public float setThresh(float y) {
        if (y > ycenter) {
            thresh = (y-ycenter) / (height/2);
        }
        
        return thresh;
    }
    
    public void draw(ShapeRenderer renderer) {
        // y axis line
        renderer.setColor(YAXIS_COLOR[0], YAXIS_COLOR[1], YAXIS_COLOR[2], YAXIS_COLOR[3]);
        renderer.line(left, ycenter, right, ycenter);
        
        // signal lines
        if (data == null) {
            return;
        }
        renderer.setColor(SIGNAL_COLOR[0], SIGNAL_COLOR[1], SIGNAL_COLOR[2], SIGNAL_COLOR[3]);
        for (int i = 0; i < data.length-1; i++) {
            renderer.line(
                    i*xinc + left,
                    (height/2)*data[i] + ycenter,
                    (i+1)*xinc + left,
                    (height/2)*data[i+1] + ycenter);
        }
        
        // threshold line
        if (thresh == -1) {
            return;
        }
        renderer.setColor(1, 0, 0, 1);
        renderer.line(left, ycenter+thresh*height/2, right, ycenter+thresh*height/2);
    }
}
