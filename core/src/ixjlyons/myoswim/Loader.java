package ixjlyons.myoswim;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

public class Loader {

    public static Animation loadPlayer() {
        Texture t1 = new Texture("player_fish1.png");
        t1.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        Texture t2 = new Texture("player_fish2.png");
        t2.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        Texture t3 = new Texture("player_fish3.png");
        t3.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        
        Animation animation = new Animation(
                0.2f,
                new TextureRegion(t1),
                new TextureRegion(t2),
                new TextureRegion(t3),
                new TextureRegion(t2));
        animation.setPlayMode(PlayMode.LOOP);
        
        return animation;
    }
    
    public static Animation loadEnemy() {
        Texture t1 = new Texture("enemy_fish1.png");
        t1.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        Texture t2 = new Texture("enemy_fish2.png");
        t2.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        Texture t3 = new Texture("enemy_fish3.png");
        t3.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        
        Animation animation = new Animation(
                0.2f,
                new TextureRegion(t1),
                new TextureRegion(t2),
                new TextureRegion(t3),
                new TextureRegion(t2));
        animation.setPlayMode(PlayMode.LOOP);
        
        return animation;
    }
    
    public static TextureRegion loadBubble() {
        TextureRegion t = new TextureRegion(new Texture("bubble.png"));
        return t;
    }
}