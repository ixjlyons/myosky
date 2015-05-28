package ixjlyons.myosky.actors;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Text extends Actor {

    private BitmapFont font;
    private String text = "";

    public Text(FileHandle fileHandle){
        font = new BitmapFont(fileHandle);
    }
    
    public void setColor(float r, float g, float b, float a) {
        font.setColor(r, g, b, a);
    }
    
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
         font.draw(batch, text, getX(), getY()+font.getCapHeight());
    }
}