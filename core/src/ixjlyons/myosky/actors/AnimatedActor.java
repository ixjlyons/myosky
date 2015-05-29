package ixjlyons.myosky.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class AnimatedActor extends Actor {

    private Animation animation;
    private float stateTime;
    private Rectangle rectangle = new Rectangle();
    
    private float leftMargin = 0;
    private float topMargin = 0;
    private float rightMargin = 0;
    private float bottomMargin = 0;
    
    public AnimatedActor(Animation animation) {
        this.animation = animation;
        
        setBounds(
                getX(),
                getY(),
                animation.getKeyFrame(0).getRegionWidth(),
                animation.getKeyFrame(0).getRegionHeight());
    }
    
    public void setMargins(float left, float top, float right, float bottom) {
        leftMargin = left;
        topMargin = top;
        rightMargin = right;
        bottomMargin = bottom;
    }
    
    public Rectangle getRectangle() {
        return rectangle.set(
                getX()+leftMargin,
                getY()+bottomMargin,
                getWidth()-leftMargin-rightMargin,
                getHeight()-topMargin-bottomMargin);
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
