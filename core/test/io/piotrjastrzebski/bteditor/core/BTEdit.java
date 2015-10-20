package io.piotrjastrzebski.bteditor.core;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.kotcrab.vis.ui.VisUI;

public class BTEdit extends Game {
	SpriteBatch batch;
	ShapeRenderer renderer;
	PlatformBridge bridge;

	public BTEdit (PlatformBridge bridge) {
		this.bridge = bridge;
	}

	@Override
	public void create () {
		// TODO better high res check
		boolean highRes = Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) > 1980;
		if (highRes || bridge.getPixelScaleFactor() > 1.5f) {
			VisUI.load(VisUI.SkinScale.X2);
		} else {
			VisUI.load(VisUI.SkinScale.X1);
		}
		batch = new SpriteBatch();
		renderer = new ShapeRenderer();

//		setScreen(new EditorScreen(this));
		setScreen(new BTEditTest(this));
	}

	@Override public void dispose () {
		super.dispose();
		batch.dispose();
		renderer.dispose();
		VisUI.dispose();
	}

	public SpriteBatch getBatch () {
		return batch;
	}

	public ShapeRenderer getRenderer () {
		return renderer;
	}
}
