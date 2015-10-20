package io.piotrjastrzebski.bteditor;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import io.piotrjastrzebski.bteditor.core.BTEdit;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 800;
		config.height = 600;
		config.useHDPI = true;
		DesktopBridge bridge = new DesktopBridge();
		new LwjglApplication(new BTEdit(bridge), config);
	}
}
