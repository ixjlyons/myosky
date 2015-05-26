package ixjlyons.myosky.android;

import ixjlyons.myosky.PlaneGame;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        
        initialize(new PlaneGame(), config);
    }
}