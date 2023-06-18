package ixjlyons.myoswim.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import ixjlyons.myoswim.MyoSwim;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setResizable(true);
		config.setForegroundFPS(30);
		config.setTitle("MyoSwim");
		new Lwjgl3Application(new MyoSwim(), config);
	}
}
