package ixjlyons.myosky.screens;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateTo;
import ixjlyons.myosky.MyoSwim;
import ixjlyons.myosky.Processor;
import ixjlyons.myosky.RecordThread.OnReadListener;
import ixjlyons.myosky.actors.AnimatedActor;
import ixjlyons.myosky.actors.Bubble;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class GameScreen implements Screen, OnReadListener {

    private static final boolean DEBUG_MARGINS = false;
    
    private static final float GRAVITY = -10;
    private static final float PLANE_START_Y = 100;
    private static final float PLANE_START_X = 50;
    private static final float INITIAL_SCROLL_SPEED = 200;
    private static final float SENSITIVITY = 75;
    private static final float SPAWN_DELAY = 5;
    private static final float[] SPAWN_RANGE = {1, 4};
    private static final float INITIAL_COIN_PROBABILITY = 1;
    private static final float MIN_COIN_PROBABILITY = 0.4f;
    private static final int LEVEL_UP_SCORE = 5;
    
    static enum GameState {
        Start,
        Running,
        GameOver
    }
    
    final MyoSwim game;
    
    private ShapeRenderer shapeRenderer;
    private Stage stage;
    private Skin skin;
    private Button prevButton;
    private AnimatedActor player;
    private Image background;
    private Image ground1;
    private Image ground2;
    private Image readyImage;
    private Image gameOverImage;
    private Label scoreText;
    private Array<Bubble> coins = new Array<Bubble>();
    private Array<AnimatedActor> enemies = new Array<AnimatedActor>();
    
    private GameState gameState = GameState.Start;
    private int score = 0;
    private int level = 1;
    private float coinProbability;
    
    private Processor processor;
    private float scrollSpeed;
    private float planeSpeed;
    
    private float input = 0.0f;
    private float thresh;
    
    private Task spawnTask = new Task() {
        @Override
        public void run() {
            boolean coin = MathUtils.randomBoolean(coinProbability);
            if (coin) {
                Bubble c = new Bubble(game.getBubbleTexture());
                
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
            }
            else {
                AnimatedActor enemy = new AnimatedActor(game.getEnemyAnimation());
                enemy.setMargins(0, 50, 120, 40);
                float y = MathUtils.random(ground1.getHeight(), stage.getHeight()-enemy.getHeight());
                enemy.setPosition(stage.getWidth(), y);
                stage.addActor(enemy);
                enemies.add(enemy);
            }
            
            if (gameState == GameState.Running) { 
                Timer.schedule(
                        spawnTask,
                        MathUtils.random(SPAWN_RANGE[0], SPAWN_RANGE[1]));
            }
        }
    };
    
    public GameScreen(final MyoSwim game) {
        this.game = game;
        shapeRenderer = new ShapeRenderer();
        
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        stage = new Stage(new ExtendViewport(MyoSwim.WIDTH, MyoSwim.HEIGHT));
        initBackground();
        initGround();
        initButtons();
        initPlayer();
        initStatusImages();
        initScoreText();
        stage.addActor(background);
        stage.addActor(ground1);
        stage.addActor(ground2);
        stage.addActor(player);
        stage.addActor(readyImage);
        stage.addActor(gameOverImage);
        stage.addActor(scoreText);
        stage.addActor(prevButton);

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
    
    private void initPlayer() {
        player = new AnimatedActor(game.getPlayerAnimation());
        player.setMargins(0, 15, 0, 10);
        player.setPosition(PLANE_START_X, PLANE_START_Y);
    }
    
    private void initStatusImages() {
        readyImage = new Image(new Texture("ready.png"));
        readyImage.setPosition(
                (stage.getWidth()-readyImage.getWidth())/2,
                (stage.getHeight()-readyImage.getHeight())/2);
        readyImage.setVisible(false);
        
        gameOverImage = new Image(new Texture("gameover.png"));
        gameOverImage.setPosition(
                (stage.getWidth()-gameOverImage.getWidth())/2,
                (stage.getHeight()-gameOverImage.getHeight())/2);
        gameOverImage.setVisible(false);
    }
    
    private void initScoreText() {
        scoreText = new Label(
                "score: 0\nlevel: 1",
                skin,
                "large-font",
                new Color(0.3f, 0.3f, 0.3f, 1));
        scoreText.setAlignment(Align.left);
        scoreText.setPosition(20, stage.getHeight()-scoreText.getHeight()-20);
    }
    
    private void initButtons() {
        prevButton = new TextButton("<", skin, "default");
        prevButton.setWidth(MyoSwim.NAV_BUTTON_WIDTH);
        prevButton.setHeight(MyoSwim.NAV_BUTTON_HEIGHT);
        prevButton.setPosition(
               stage.getWidth()-2*prevButton.getWidth()-2*MyoSwim.NAV_BUTTON_PADDING,
               MyoSwim.NAV_BUTTON_PADDING);
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
        spawnTask.cancel();
        score = 0;
        level = 1;
        coinProbability = INITIAL_COIN_PROBABILITY;
        scrollSpeed = 0;
        ground1.setX(0);
        ground2.setX(ground1.getRight());
        player.clearActions();
        player.setPosition(PLANE_START_X, PLANE_START_Y);
        player.setRotation(0);
        planeSpeed = 0;
        readyImage.setVisible(true);
        gameOverImage.setVisible(false);
        
        for (Bubble c: coins) {
            c.remove();
        }
        coins.clear();
        
        for (AnimatedActor enemy: enemies) {
            enemy.remove();
        }
        enemies.clear();
    }
    
    private void updateWorld(float delta) {
        
        if (gameState == GameState.Start) {
            if (Gdx.input.justTouched() || input > thresh) {
                gameState = GameState.Running;
                scrollSpeed = INITIAL_SCROLL_SPEED;
                planeSpeed = 0;
                readyImage.setVisible(false);
                Timer.schedule(spawnTask, SPAWN_DELAY);
            }
        }
        
        else if (gameState == GameState.Running) { 
            planeSpeed += (input/thresh)*SENSITIVITY;
            planeSpeed += GRAVITY;
            
            player.setY(player.getY() + planeSpeed*delta);
            
            ground1.moveBy(-scrollSpeed*delta, 0);
            ground2.moveBy(-scrollSpeed*delta, 0);
            if (ground1.getRight() < stage.getWidth()) {
                ground2.setX(ground1.getRight());
            }
            if (ground2.getRight() < stage.getWidth()) {
                ground1.setX(ground2.getRight());
            }
    
            for(Bubble c: coins) {
                if (c.getX() < player.getRight()) {
                    if(Intersector.overlaps(c.getCircle(), player.getRectangle())
                            && !c.getCounted()) {
                        incrementScore(2);
                        c.setCounted(true);
                    }
                }
                
                c.moveBy(-scrollSpeed*delta, c.getCounted() ? 300*delta : 0);
                
                if(c.getRight() < 0 || c.getY() > stage.getHeight()) {
                    coins.removeValue(c, true);
                    c.remove();
                }
            }
            
            for (AnimatedActor enemy: enemies) {
                if (enemy.getX() < player.getRight()) {
                    if (Intersector.overlaps(enemy.getRectangle(), player.getRectangle())) {
                        scrollSpeed = 0;
                        gameState = GameState.GameOver;
                        gameOverImage.setVisible(true);
                        
                        // make the player fish belly up
                        player.setOrigin(Align.center);
                        player.addAction(
                                sequence(
                                        rotateTo(180, 1),
                                        moveTo(
                                                player.getX(),
                                                stage.getHeight()+player.getHeight(),
                                                3*(1 - player.getY()/stage.getHeight()))));
                        break;
                    }
                }
                
                enemy.moveBy(-scrollSpeed*delta, 0);
                
                if (enemy.getRight() < 0) {
                    enemies.removeValue(enemy, true);
                    enemy.remove();
                    incrementScore(1);
                }
            }
            
            if(player.getY() < ground1.getHeight()) {
                planeSpeed = 0;
                player.setY(ground1.getHeight());
            }
            
            if (player.getY() > stage.getHeight() - player.getHeight()) {
                planeSpeed = 0;
                player.setY(stage.getHeight() - player.getHeight());
            }
        }
        
        else {
            boolean bellyingUp = (player.getY() < stage.getHeight());
            if (!bellyingUp && (Gdx.input.justTouched() || input > thresh)) {
                gameState = GameState.Start;
                resetWorld();
            }
        }
        
        input = 0;
        
        stage.act(delta);
    }
    
    private void drawWorld() {
        scoreText.setText("score: " + score + "\nlevel: " + level);
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
        
        if (DEBUG_MARGINS) {
            shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
            shapeRenderer.begin(ShapeType.Line);
            drawRectangle(player.getRectangle());
            for (AnimatedActor enemy: enemies) {
                drawRectangle(enemy.getRectangle());
            }
            for (Bubble coin: coins) {
                drawCircle(coin.getCircle());
            }
            shapeRenderer.end();
        }
    }
    
    private void incrementScore(int increment) {
        score += increment;
        if (score % LEVEL_UP_SCORE == 0 || score % LEVEL_UP_SCORE == 1) {
            level++;
            coinProbability *= 0.75f;
            if (coinProbability < MIN_COIN_PROBABILITY) {
                coinProbability = MIN_COIN_PROBABILITY;
            }
            scrollSpeed *= 1.5f;
        }
    }
    
    private void drawRectangle(Rectangle r) {
        shapeRenderer.rect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }
    
    private void drawCircle(Circle c) {
        shapeRenderer.circle(c.x, c.y, c.radius);
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