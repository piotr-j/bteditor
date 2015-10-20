package io.piotrjastrzebski.bteditor.core;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Created by EvilEntity on 03/10/2015.
 */
public class EditorScreen extends BaseScreen {
	VisTextButton showEditor;

	public EditorScreen (BTEdit game) {
		super(game);

		showEditor = new VisTextButton("Editor", "toggle");
		showEditor.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				toggleEditor(showEditor.isChecked());
			}
		});
		root.add(showEditor).left().top().fill();
		toggleEditor(true);
	}

	private void toggleEditor (boolean show) {

	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	@Override public void dispose () {
		super.dispose();
	}
}
