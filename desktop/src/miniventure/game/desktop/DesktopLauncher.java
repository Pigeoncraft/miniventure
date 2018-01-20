package miniventure.game.desktop;

import miniventure.game.GameCore;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Miniventure";
		config.width = 800;
		config.height = 450;
		new LwjglApplication(new GameCore(), config);
	}
}
