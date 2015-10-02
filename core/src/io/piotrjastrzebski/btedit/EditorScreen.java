package io.piotrjastrzebski.btedit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.btedit.editor.EditorWindow;

/**
 * Created by EvilEntity on 03/10/2015.
 */
public class EditorScreen extends BaseScreen implements EditorWindow.EditorListener {
	VisTextButton showEditor;
	EditorWindow editor;
	public EditorScreen (BTEdit game) {
		super(game);
		editor = new EditorWindow();
		editor.addEditorListner(this);

		showEditor = new VisTextButton("Editor", "toggle");
		showEditor.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				toggleEditor(showEditor.isChecked());
			}
		});
		root.add(showEditor).left().top().fill();
		toggleEditor(true);
	}

	private void toggleEditor (boolean show) {
		if (show && !editor.onStage()) {
			editor.show(stage);
		} else if (!show && editor.onStage()){
			editor.hide();
		}
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	@Override public void dispose () {
		super.dispose();
		editor.dispose();
	}

	@Override public void onShow () {
		showEditor.setChecked(true);
	}

	@Override public void onHide () {
		showEditor.setChecked(false);
	}
}
