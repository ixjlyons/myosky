package ixjlyons.myosky.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Plane extends Actor {

    private Animation animation;
    private float stateTime;
    private Rectangle rectangle = new Rectangle();
    
    public Plane() {
        Texture frame1 = new Texture("plane1.png");
        frame1.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        Texture frame2 = new Texture("plane2.png");
        Texture frame3 = new Texture("plane3.png");
        
        animation = new Animation(
                0.05f,
                new TextureRegion(frame1),
                new TextureRegion(frame2),
                new TextureRegion(frame3),
                new TextureRegion(frame2));
        animation.setPlayMode(PlayMode.LOOP);
        
        setBounds(getX(), getY(), frame1.getWidth(), frame1.getHeight());
    }
    
    public Rectangle getRectangle() {
        return rectangle.set(getX(), getY(), getWidth(), getHeight());
    }
    
    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(
                animation.getKeyFrame(stateTime),
                getX(), getY(),
                getOriginX(), getOriginY(),
                getWidth(), getHeight(),
                getScaleX(), getScaleY(),
                getRotation());
    }
    
}
