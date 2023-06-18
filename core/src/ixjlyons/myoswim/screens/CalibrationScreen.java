package ixjlyons.myoswim.screens;

import ixjlyons.myoswim.MyoSwim;
import ixjlyons.myoswim.Processor;
import ixjlyons.myoswim.RecordThread.OnReadListener;
import ixjlyons.myoswim.actors.SignalViewer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
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

public class CalibrationScreen implements Screen, OnReadListener {
    
    public static final String TEXT = 
            "This is the smoothed, root mean square (RMS) signal." +
            "\n" +
            "Touch the screen to set the calibration level.";
    final MyoSwim game;

    private Skin skin;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    
    private float[] inputData = new float[100];
    private Processor processor;
    private float thresh = -1;
    
    private Stage stage;
    private Image background;
    private Label text;
    private Button nextButton;
    private Button prevButton;
    private SignalViewer signalViewer;
    private Vector3 touchPoint;
    
    public CalibrationScreen(final MyoSwim game) {
        this.game = game;

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        
        camera = new OrthographicCamera();
        camera.setToOrtho(false, MyoSwim.WIDTH, MyoSwim.HEIGHT);
        
        shapeRenderer = new ShapeRenderer();
        
        stage = new Stage(new ExtendViewport(MyoSwim.WIDTH, MyoSwim.HEIGHT));
        initBackground();
        initText();
        initButtons();
        stage.addActor(background);
        stage.addActor(text);
        stage.addActor(nextButton);
        stage.addActor(prevButton);

        signalViewer = new SignalViewer(0, stage.getHeight(), stage.getWidth(), 0);
        
        processor = new Processor();
        touchPoint = new Vector3();
    }
    
    private void initBackground() {
        background = new Image(new Texture("background.png"));
        background.setPosition(0, 0);
        background.setSize(stage.getWidth(), stage.getHeight());
    }
    
    private void initText() {
        text = new Label(TEXT, skin, "large-font", new Color(0.3f, 0.3f, 0.3f, 1));
        text.setAlignment(Align.center);
        text.setPosition(
                stage.getWidth()/2-text.getWidth()/2,
                3*stage.getHeight()/4-text.getHeight()/2);
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
                game.prevScreen();
            }
        });
        
        nextButton = new TextButton(">", skin, "default");
        nextButton.setWidth(MyoSwim.NAV_BUTTON_WIDTH);
        nextButton.setHeight(MyoSwim.NAV_BUTTON_HEIGHT);
        nextButton.setPosition(
                stage.getWidth()-nextButton.getWidth()-MyoSwim.NAV_BUTTON_PADDING,
                MyoSwim.NAV_BUTTON_PADDING);
        nextButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (thresh != -1) {
                    game.nextScreen();
                }
            }
        });
    }
    
    @Override
    public void onRead(float[] data) {
        final float input = processor.update(data);
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                updateDataBuffer(input);
            }
        });
    }
    
    private void updateDataBuffer(float input) {
        for (int i = 0; i < inputData.length-1; i++) {
            inputData[i] = inputData[i+1];
        }
        inputData[inputData.length-1] = input;
        
        signalViewer.setData(inputData);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glLineWidth(3);

        if (Gdx.input.isTouched()) {
            touchPoint.set(0, Gdx.input.getY(), 0);
            thresh = signalViewer.setThresh(camera.unproject(touchPoint).y);
        }
        
        stage.act();
        stage.draw();
        
        camera.update();
        
        if (inputData == null) {
            return;
        }
        
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Line);
        signalViewer.draw(shapeRenderer);

        shapeRenderer.end();
    }
    
    public float getThreshold() {
        return thresh;
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
    }

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