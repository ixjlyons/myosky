package ixjlyons.myoswim;

import ixjlyons.myoswim.screens.CalibrationScreen;
import ixjlyons.myoswim.screens.GameScreen;
import ixjlyons.myoswim.screens.SignalScreen;
import ixjlyons.myoswim.screens.TitleScreen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MyoSwim extends Game {
    
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 800;
    
    public static final float NAV_BUTTON_PADDING = 30;
    public static final float NAV_BUTTON_WIDTH = 80;
    public static final float NAV_BUTTON_HEIGHT = 60;
    
    private TitleScreen titleScreen;
    private SignalScreen signalScreen;
    private CalibrationScreen calibrationScreen;
    private GameScreen gameScreen;
    private Screen currentScreen;
    
    private SpriteBatch spriteBatch;
    private Animation playerAnimation;
    private Animation enemyAnimation;
    private TextureRegion bubbleTexture;
    //private Music music;
    public RecordThread recordThread;
    
    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        
        playerAnimation = Loader.loadPlayer();
        enemyAnimation = Loader.loadEnemy();
        bubbleTexture = Loader.loadBubble();
        
        recordThread = new RecordThread(8000, 600);
        recordThread.start();
        
        titleScreen = new TitleScreen(this);
        signalScreen = new SignalScreen(this);
        calibrationScreen = new CalibrationScreen(this);
        gameScreen = new GameScreen(this);
        
        //music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        //music.setLooping(true);
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
    
    public void prevScreen() {
        if (currentScreen == signalScreen) {
            currentScreen = titleScreen;
        }
        else if (currentScreen == calibrationScreen) {
            currentScreen = signalScreen;
        }
        else if (currentScreen == gameScreen) {
            currentScreen = calibrationScreen;
        }
        
        setScreen(currentScreen);
    }
    
    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
    
    @Override
    public void render() {
        super.render();
    }
    
    @Override
    public void dispose() {
        //music.dispose();
        recordThread.stopRunning();
    }
    
    public Animation getPlayerAnimation() {
        return playerAnimation;
    }
    
    public Animation getEnemyAnimation() {
        return enemyAnimation;
    }
    
    public TextureRegion getBubbleTexture() {
        return bubbleTexture;
    }
}