package ixjlyons.myosky.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Ground extends Actor {
    
    private Texture texture;
    private float secondLeft = 0;
    
    public Ground() {
        texture = new Texture("ground.png");
        
        setBounds(getX(), getY(), texture.getWidth(), texture.getHeight());
        secondLeft = getRight();
    }
    
    @Override
    public void act(float delta) {
        super.act(delta);

    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(texture, getX(), getY());
        batch.draw(texture, getX()+texture.getWidth(), getY());
    }
}
