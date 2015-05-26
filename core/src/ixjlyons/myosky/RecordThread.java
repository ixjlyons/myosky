package ixjlyons.myosky;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioRecorder;

public class RecordThread extends Thread {
    
    private OnReadListener listener;
    private AudioRecorder recorder;
    private short[] audioData;
    private float[] floatData;
    boolean running = false;
    boolean doneRunning = false;
    
    public RecordThread(int sampleRate, int samplesPerRead) {
        recorder = Gdx.audio.newAudioRecorder(sampleRate, true);
        audioData = new short[samplesPerRead];
        floatData = new float[samplesPerRead];
    }
    
    @Override
    public void run() {
        running = true;
        doneRunning = false;
        while (running) {
            recorder.read(audioData, 0, audioData.length);
            
            normalize();
            
            if (listener != null) {
                listener.onRead(floatData);
            }
        }
        doneRunning = true;
        recorder.dispose();
    }
    
    private void normalize() {
        for (int n = 0; n < audioData.length; n++) {
            floatData[n] = audioData[n] / 32678.0f;
        }
    }
    
    public void stopRunning() {
        running = false;
    }
    
    public void setOnReadListener(OnReadListener listener) {
        this.listener = listener;
    }
    
    public interface OnReadListener {
        public void onRead(float[] data);
    }
}