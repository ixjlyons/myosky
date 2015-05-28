package ixjlyons.myosky.screens;

import ixjlyons.myosky.PlaneGame;
import ixjlyons.myosky.Processor;
import ixjlyons.myosky.RecordThread.OnReadListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class GameScreen implements Screen, OnReadListener {

    private static final float GRAVITY = -10;
    private static final float PLANE_VELOCITY_X = 200;
    private static final float PLANE_START_Y = 100;
    private static final float PLANE_START_X = 50;
    private static final float SENSITIVITY = 75;
    
    final PlaneGame game;
    
    private Stage stage;
    private Skin skin;
    private Button prevButton;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private Texture background;
    private TextureRegion ground;
    private float groundOffsetX = 0;
    private TextureRegion rock;
    private TextureRegion rockDown;
    private Animation plane;
    private TextureRegion ready;
    private TextureRegion gameOver;
    private BitmapFont font;
    
    private Vector2 planePosition = new Vector2();
    private Vector2 planeVelocity = new Vector2();
    private float planeStateTime = 0;
    private Vector2 gravity = new Vector2();
    private Array<Rock> rocks = new Array<Rock>();
    
    private GameState gameState = GameState.Start;
    private int score = 0;
    private Rectangle rect1 = new Rectangle();
    private Rectangle rect2 = new Rectangle();
    
    private Processor processor;
    
    private float input = 0.0f;
    private float thresh;
    
    public GameScreen(final PlaneGame game) {
        this.game = game;

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();
        
        font = new BitmapFont(Gdx.files.internal("arial.fnt"));
        
        background = new Texture("background.png"); 
        ground = new TextureRegion(new Texture("ground.png"));
        
        rock = new TextureRegion(new Texture("coin.png"));
        rockDown = new TextureRegion(rock);
        rockDown.flip(false, true);
        
        Texture frame1 = new Texture("plane1.png");
        frame1.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        Texture frame2 = new Texture("plane2.png");
        Texture frame3 = new Texture("plane3.png");
        
        ready = new TextureRegion(new Texture("ready.png"));
        gameOver = new TextureRegion(new Texture("gameover.png"));
        
        plane = new Animation(
                0.05f,
                new TextureRegion(frame1),
                new TextureRegion(frame2),
                new TextureRegion(frame3),
                new TextureRegion(frame2));
        plane.setPlayMode(PlayMode.LOOP);
        
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        stage = new Stage(new ExtendViewport(PlaneGame.WIDTH, PlaneGame.HEIGHT));
        initButtons();
        stage.addActor(prevButton);
    
        game.recordThread.setOnReadListener(this);
        processor = new Processor();
        
        resetWorld();
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
    
    private void resetWorld() {
        score = 0;
        groundOffsetX = 0;
        planePosition.set(PLANE_START_X, PLANE_START_Y);
        planeVelocity.set(0, 0);
        gravity.set(0, GRAVITY);
        camera.position.x = 400;
        
        rocks.clear();
        for(int i = 0; i < 5; i++) {
            boolean isDown = MathUtils.randomBoolean();
            rocks.add(new Rock(
                    700 + i * 200,
                    isDown ? 480-rock.getRegionHeight(): 0, (isDown ? rockDown: rock)));
        }
    }
    
    private void updateWorld() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        planeStateTime += deltaTime;
        
        if(gameState == GameState.Running) {
            planeVelocity.add(0, (input/thresh)*SENSITIVITY);
        }
        
        if(Gdx.input.justTouched() || input > thresh) {
            if(gameState == GameState.Start) {
                gameState = GameState.Running;
                planeVelocity.set(PLANE_VELOCITY_X, 0);
            }
            
            if(gameState == GameState.GameOver) {
                gameState = GameState.Start;
                resetWorld();
            }
        }

        input = 0;
            
        if(gameState != GameState.Start) planeVelocity.add(gravity);
        
        planePosition.mulAdd(planeVelocity, deltaTime);
        
        camera.position.x = planePosition.x + 350;      
        if(camera.position.x - groundOffsetX > ground.getRegionWidth() + 400) {
            groundOffsetX += ground.getRegionWidth();
        }
                
        rect1.set(
                planePosition.x + 20,
                planePosition.y,
                plane.getKeyFrames()[0].getRegionWidth() - 20,
                plane.getKeyFrames()[0].getRegionHeight());
        for(Rock r: rocks) {
            if(camera.position.x - r.position.x > 400 + r.image.getRegionWidth()) {
                boolean isDown = MathUtils.randomBoolean();
                r.position.x += 5 * 200;
                r.position.y = isDown?480-rock.getRegionHeight(): 0;
                r.image = isDown? rockDown: rock;
                r.counted = false;
            }
            rect2.set(
                    r.position.x + (r.image.getRegionWidth() - 30) / 2 + 20,
                    r.position.y,
                    20,
                    r.image.getRegionHeight() - 10);
            if(rect1.overlaps(rect2)) {
                gameState = GameState.GameOver;
                planeVelocity.x = 0;                
            }
            if(r.position.x < planePosition.x && !r.counted) {
                score++;
                r.counted = true;
            }
        }
        
        if(planePosition.y < ground.getRegionHeight() - 20) {
            planeVelocity.y = 0;
            planePosition.y = ground.getRegionHeight() - 20;
        }
        
        if (planePosition.y > camera.viewportHeight - plane.getKeyFrame(0).getRegionHeight()) {
            planeVelocity.y = 0;
            planePosition.y = camera.viewportHeight - plane.getKeyFrame(0).getRegionHeight();
        }
    }
    
    private void drawWorld() {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(background, camera.position.x - background.getWidth() / 2, 0);
        for(Rock rock: rocks) {
            batch.draw(rock.image, rock.position.x, rock.position.y);
        }
        batch.draw(ground, groundOffsetX, 0);
        batch.draw(ground, groundOffsetX + ground.getRegionWidth(), 0);
        batch.draw(plane.getKeyFrame(planeStateTime), planePosition.x, planePosition.y);
        batch.end();
        
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();      
        if(gameState == GameState.Start) {
            batch.draw(
                    ready,
                    Gdx.graphics.getWidth() / 2 - ready.getRegionWidth() / 2,
                    Gdx.graphics.getHeight() / 2 - ready.getRegionHeight() / 2);
        }
        if(gameState == GameState.GameOver) {
            batch.draw(
                    gameOver,
                    Gdx.graphics.getWidth() / 2 - gameOver.getRegionWidth() / 2,
                    Gdx.graphics.getHeight() / 2 - gameOver.getRegionHeight() / 2);
        }
        if(gameState == GameState.GameOver || gameState == GameState.Running) {
            font.draw(
                    batch,
                    "" + score,
                    Gdx.graphics.getWidth() / 2,
                    Gdx.graphics.getHeight() - 60);
        }
        batch.end();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        stage.act(delta);
        updateWorld();
        drawWorld();
        stage.draw();
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
    
    static class Rock {
        Vector2 position = new Vector2();
        TextureRegion image;
        boolean counted;
        
        public Rock(float x, float y, TextureRegion image) {
            this.position.x = x;
            this.position.y = y;
            this.image = image;
        }
    }
    
    static enum GameState {
        Start, Running, GameOver
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