package ixjlyons.myosky;

import ixjlyons.myosky.screens.CalibrationScreen;
import ixjlyons.myosky.screens.GameScreen;
import ixjlyons.myosky.screens.SignalScreen;
import ixjlyons.myosky.screens.TitleScreen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;

public class PlaneGame extends Game {

    private TitleScreen titleScreen;
    private SignalScreen signalScreen;
    private CalibrationScreen calibrationScreen;
    private GameScreen gameScreen;
    private Screen currentScreen;
    
    private Music music;
    public RecordThread recordThread;
    
    @Override
    public void create() {
        recordThread = new RecordThread(8000, 600);
        recordThread.start();
        
        titleScreen = new TitleScreen(this);
        signalScreen = new SignalScreen(this);
        calibrationScreen = new CalibrationScreen(this);
        gameScreen = new GameScreen(this);
        
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        music.setLooping(true);
        //music.play();

        currentScreen = titleScreen;
        setScreen(titleScreen);
    }
    
    public void nextScreen() {
        if (currentScreen == titleScreen) {
            currentScreen = signalScreen;
        }
        else if (currentScreen == signalScreen) {
            currentScreen = calibrationScreen;
        }
        else if (currentScreen == calibrationScreen) {
            gameScreen.setThreshold(calibrationScreen.getThreshold());
            currentScreen = gameScreen;
        }
        
        setScreen(currentScreen);
    }
    
    @Override
    public void render() {
        super.render();
    }
    
    @Override
    public void dispose() {
        music.dispose();
        recordThread.stopRunning();
    }
}