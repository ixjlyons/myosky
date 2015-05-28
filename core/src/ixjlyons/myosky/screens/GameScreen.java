package ixjlyons.myosky.screens;

import ixjlyons.myosky.PlaneGame;
import ixjlyons.myosky.Processor;
import ixjlyons.myosky.RecordThread.OnReadListener;
import ixjlyons.myosky.actors.Coin;
import ixjlyons.myosky.actors.Plane;
import ixjlyons.myosky.actors.Text;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class GameScreen implements Screen, OnReadListener {

    private static final float GRAVITY = -10;
    private static final float PLANE_START_Y = 100;
    private static final float PLANE_START_X = 50;
    private static final float SCROLL_SPEED = 200;
    private static final float SENSITIVITY = 75;
    
    static enum GameState {
        Start,
        Running,
        GameOver
    }
    
    final PlaneGame game;
    
    private Stage stage;
    private Skin skin;
    private Button prevButton;
    private Plane plane;
    private Image background;
    private Image ground1;
    private Image ground2;
    private Image readyImage;
    private Image gameOverImage;
    private Text scoreText;
    private Array<Coin> coins = new Array<Coin>();
    
    private GameState gameState = GameState.Start;
    private int score = 0;
    
    private Processor processor;
    private float scrollSpeed;
    private float planeSpeed;
    
    private float input = 0.0f;
    private float thresh;
    
    private Task spawnTask = new Task() {
        @Override
        public void run() {
            Coin c = new Coin();
            
            // spawn a coin according to one of two triangular distributions
            // one centered about the lower half of the stage
            // one centered about the upper half of the stage
            boolean low = MathUtils.randomBoolean();
            float y;
            if (low) {
                y = MathUtils.randomTriangular(
                        ground1.getHeight(),
                        stage.getHeight()/2);
            }
            else {
                y = MathUtils.randomTriangular(
                        stage.getHeight()/2,
                        stage.getHeight()-c.getHeight());
            }
            
            c.setPosition(stage.getWidth(), y);
            stage.addActor(c);
            coins.add(c);
            
            if (gameState == GameState.Running) { 
                Timer.schedule(spawnTask, MathUtils.random(2f, 5f));
            }
        }
    };
    
    public GameScreen(final PlaneGame game) {
        this.game = game;
        
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        stage = new Stage(new ExtendViewport(PlaneGame.WIDTH, PlaneGame.HEIGHT));
        initBackground();
        initGround();
        initButtons();
        initPlane();
        initStatusImages();
        initScoreText();
        stage.addActor(background);
        stage.addActor(ground1);
        stage.addActor(ground2);
        stage.addActor(plane);
        stage.addActor(prevButton);
        stage.addActor(readyImage);
        stage.addActor(gameOverImage);
        stage.addActor(scoreText);
    
        game.recordThread.setOnReadListener(this);
        processor = new Processor();
 
        resetWorld();
    }
    
    private void initBackground() {
        background = new Image(new Texture("background.png"));
        background.setPosition(0, 0);
        background.setSize(stage.getWidth(), stage.getHeight());
    }
    
    private void initGround() {
        Texture t = new Texture("sand.png");
        ground1 = new Image(t);
        ground2 = new Image(t);
        ground1.setPosition(0, 0);
        ground2.setPosition(ground1.getRight(), 0);
    }
    
    private void initStatusImages() {
        readyImage = new Image(new Texture("ready.png"));
        readyImage.setPosition(
                (stage.getWidth()-readyImage.getWidth()) / 2,
                (stage.getHeight()-readyImage.getHeight()) / 2);
        readyImage.setVisible(false);
        
        gameOverImage = new Image(new Texture("gameover.png"));
        gameOverImage.setAlign(Align.center);
        gameOverImage.setPosition(stage.getWidth()/2, stage.getHeight()/2);
        gameOverImage.setVisible(false);
    }
    
    private void initScoreText() {
        scoreText = new Text(Gdx.files.internal("arial.fnt"));
        scoreText.setColor(0.3f, 0.3f, 0.3f, 1);
        scoreText.setPosition(20, 20);
    }
    
    private void initButtons() {
        float buttonWidth = 60f;
        float buttonHeight = 50f;
        float padding = 20f;
        
        prevButton = new TextButton("<", skin, "default");
        prevButton.setWidth(buttonWidth);
        prevButton.setHeight(buttonHeight);
        prevButton.setPosition(
               stage.getWidth()-2*buttonWidth-2*padding,
               padding);
        prevButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameState = GameState.Start;
                resetWorld();
                game.prevScreen();
            }
        });
    }
    
    private void initPlane() {
        plane = new Plane();
        plane.setPosition(PLANE_START_X, PLANE_START_Y);
    }
    
    private void resetWorld() {
        spawnTask.cancel();
        score = 0;
        scrollSpeed = 0;
        ground1.setX(0);
        ground2.setX(ground1.getRight());
        plane.setPosition(PLANE_START_X, PLANE_START_Y);
        planeSpeed = 0;
        readyImage.setVisible(true);
        gameOverImage.setVisible(false);
        
        for (Coin c: coins) {
            c.remove();
        }
        coins.clear();
    }
    
    private void updateWorld(float delta) {

        if(gameState == GameState.Running) {
            planeSpeed += (input/thresh)*SENSITIVITY;
        }
        
        if(Gdx.input.justTouched() || input > thresh) {
            if(gameState == GameState.Start) {
                gameState = GameState.Running;
                scrollSpeed = SCROLL_SPEED;
                planeSpeed = 0;
                readyImage.setVisible(false);
                Timer.schedule(spawnTask, 5);
            }
            
            if(gameState == GameState.GameOver) {
                gameState = GameState.Start;
                resetWorld();
            }
        }

        input = 0;
            
        
        if(gameState != GameState.Start) {
            planeSpeed += GRAVITY;
        }
        
        plane.setY(plane.getY() + planeSpeed*delta);
        
        ground1.moveBy(-scrollSpeed*delta, 0);
        ground2.moveBy(-scrollSpeed*delta, 0);
        if (ground1.getRight() < stage.getWidth()) {
            ground2.setX(ground1.getRight());
        }
        if (ground2.getRight() < stage.getWidth()) {
            ground1.setX(ground2.getRight());
        }

        for(Coin c: coins) {
//            if(camera.position.x - r.position.x > 400 + r.image.getRegionWidth()) {
//                boolean isDown = MathUtils.randomBoolean();
//                r.position.x -= 5 * 200;
//                r.position.y = isDown?480-rock.getRegionHeight(): 0;
//                r.image = isDown? rockDown: rock;
//                r.counted = false;
//            }
            
            if(Intersector.overlaps(c.getCircle(), plane.getRectangle()) && !c.getCounted()) {
                score++;
                c.setCounted(true);
            }
            
            c.moveBy(-scrollSpeed*delta, c.getCounted() ? 300*delta : 0);
            
            if(c.getRight() < 0 || c.getY() > stage.getHeight()) {
                coins.removeValue(c, true);
                c.remove();
            }
        }
        
        if(plane.getY() < ground1.getHeight()) {
            planeSpeed = 0;
            plane.setY(ground1.getHeight());
        }
        
        if (plane.getY() > stage.getHeight() - plane.getHeight()) {
            planeSpeed = 0;
            plane.setY(stage.getHeight() - plane.getHeight());
        }
        
        stage.act(delta);
    }
    
    private void drawWorld() {
        scoreText.setText("score: " + score);
        if (gameState == GameState.Running || gameState == GameState.GameOver) {
            scoreText.setVisible(true);
        }
        else {
            scoreText.setVisible(false);
        }
        
        game.getSpriteBatch().begin();
        stage.draw();
        game.getSpriteBatch().end();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        updateWorld(delta);
        drawWorld();
    }
    
    @Override
    public void onRead(float[] data) {
        final float p = processor.update(data);
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                input = p;
            }
        });
    }
    
    public void setThreshold(float thresh) {
        this.thresh = thresh;
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void show() {
        game.recordThread.setOnReadListener(this);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {}
}