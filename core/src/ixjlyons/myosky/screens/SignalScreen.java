package ixjlyons.myosky.screens;

import ixjlyons.myosky.PlaneGame;
import ixjlyons.myosky.RecordThread.OnReadListener;
import ixjlyons.myosky.actors.SignalViewer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class SignalScreen implements Screen, OnReadListener {
    
    public static final String TEXT = "This is the raw EMG signal";
    
    final PlaneGame game;
    
    private Skin skin;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    
    private SignalViewer signalViewer;
    private Stage stage;
    private Image background;
    private Button nextButton;
    private Button prevButton;
    private Label text;
    
    public SignalScreen(final PlaneGame game) {
        this.game = game;

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        camera = new OrthographicCamera();
        camera.setToOrtho(false, PlaneGame.WIDTH, PlaneGame.HEIGHT);
        
        shapeRenderer = new ShapeRenderer();
        
        stage = new Stage(new ExtendViewport(PlaneGame.WIDTH, PlaneGame.HEIGHT));
        initBackground();
        initText();
        initButtons();
        stage.addActor(background);
        stage.addActor(text);
        stage.addActor(nextButton);
        stage.addActor(prevButton);
        
        signalViewer = new SignalViewer(0, stage.getHeight(), stage.getWidth(), 0);
    }
    
    private void initBackground() {
        background = new Image(new Texture("background.png"));
        background.setPosition(0, 0);
        background.setSize(stage.getWidth(), stage.getHeight());
    }
    
    private void initButtons() {
        prevButton = new TextButton("<", skin, "default");
        prevButton.setWidth(PlaneGame.NAV_BUTTON_WIDTH);
        prevButton.setHeight(PlaneGame.NAV_BUTTON_HEIGHT);
        prevButton.setPosition(
               stage.getWidth()-2*prevButton.getWidth()-2*PlaneGame.NAV_BUTTON_PADDING,
               PlaneGame.NAV_BUTTON_PADDING);
        prevButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.prevScreen();
            }
        });
        
        nextButton = new TextButton(">", skin, "default");
        nextButton.setWidth(PlaneGame.NAV_BUTTON_WIDTH);
        nextButton.setHeight(PlaneGame.NAV_BUTTON_HEIGHT);
        nextButton.setPosition(
                stage.getWidth()-nextButton.getWidth()-PlaneGame.NAV_BUTTON_PADDING,
                PlaneGame.NAV_BUTTON_PADDING);
        nextButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.nextScreen();
            }
        });
    }
    
    private void initText() {
        text = new Label(TEXT, skin, "large-font", new Color(0.3f, 0.3f, 0.3f, 1));
        text.setAlignment(Align.center);
        text.setPosition(
                stage.getWidth()/2-text.getWidth()/2,
                3*stage.getHeight()/4-text.getHeight()/2);
    }
    
    @Override
    public void onRead(final float[] data) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                updateDataBuffer(data);
            }
        });
    }

    private void updateDataBuffer(float[] data) {
        signalViewer.setData(data);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glLineWidth(3);
        
        stage.act();
        stage.draw();
        
        camera.update();
        
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Line);
        signalViewer.draw(shapeRenderer);
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        game.recordThread.setOnReadListener(this);
    }

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        stage.dispose();
    }
}
