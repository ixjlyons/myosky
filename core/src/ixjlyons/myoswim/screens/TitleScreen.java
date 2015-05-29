package ixjlyons.myoswim.screens;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.repeat;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import ixjlyons.myoswim.MyoSwim;
import ixjlyons.myoswim.actors.AnimatedActor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class TitleScreen implements Screen {

    final MyoSwim game;
    
    private Stage stage;
    private AnimatedActor player;
    private Skin skin;
    private Image background;
    private Button nextButton;
    private Image title;

    public TitleScreen(final MyoSwim game) {
        this.game = game;
        
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        
        stage = new Stage(new ExtendViewport(MyoSwim.WIDTH, MyoSwim.HEIGHT));
        initBackground();
        initButtons();
        initPlayer();
        initTitle();
        stage.addActor(background);
        stage.addActor(nextButton);
        stage.addActor(player);
        stage.addActor(title);
    }
    
    private void initBackground() {
        background = new Image(new Texture("background.png"));
        background.setPosition(0, 0);
        background.setSize(stage.getWidth(), stage.getHeight());
    }
    
    private void initButtons() {
        nextButton = new TextButton("Begin!", skin, "default");
        nextButton.setWidth(150);
        nextButton.setHeight(100);
        nextButton.setPosition(
                stage.getWidth()/2-nextButton.getWidth()/2,
                stage.getHeight()/10+nextButton.getHeight()/2);
        nextButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.nextScreen();
            }
        });
    }
    
    private void initPlayer() {
        player = new AnimatedActor(game.getPlayerAnimation());
        player.setPosition(-player.getWidth(), stage.getHeight()/2-player.getHeight()/2);
        player.addAction(
                delay(1f,
                    sequence(
                        // move to center of the stage
                        moveTo(
                            stage.getWidth()/2-player.getWidth()/2,
                            stage.getHeight()/2-player.getHeight()/2,
                            2f,
                            Interpolation.sineOut),
                        // "bounce" up and down
                        repeat(RepeatAction.FOREVER,
                            sequence(
                                moveBy(0, -10, 0.5f, Interpolation.sine),
                                moveBy(0, 10, 0.5f, Interpolation.sine))))));
    }
    
    private void initTitle() {
        Texture t = new Texture("title.png");
        t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        title = new Image(t);
        title.setPosition(
                stage.getWidth()/2-title.getWidth()/2,
                3*stage.getHeight()/4f-title.getHeight()/2);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Gdx.graphics.getDeltaTime());
        game.getSpriteBatch().begin();
        stage.draw();
        game.getSpriteBatch().end();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}