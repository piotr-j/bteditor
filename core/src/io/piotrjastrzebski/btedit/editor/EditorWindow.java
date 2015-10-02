package io.piotrjastrzebski.btedit.editor;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.widget.VisWindow;

/**
 * Created by EvilEntity on 03/10/2015.
 */
public class EditorWindow extends VisWindow implements Disposable {
	public EditorWindow () {
		super("BTEdit");
		addCloseButton();

	}

	protected Array<EditorListener> listeners = new Array<EditorListener>();
	public void addEditorListner(EditorListener listener) {
		if (!listeners.contains(listener, true)) {
			listeners.add(listener);
		}
	}

	public void removeEditorListner(EditorListener listener) {
		listeners.removeValue(listener, true);
	}

	public boolean onStage () {
		return hasParent();
	}

	public void show (Stage stage) {
		stage.addActor(this);
		fadeIn();
	}

	public void hide () {
		close();
	}

	public void fadeOut (float time) {
		for (EditorListener listener : listeners) {
			listener.onHide();
		}
		super.fadeOut(time);
	}

	public VisWindow fadeIn (float time) {
		for (EditorListener listener : listeners) {
			listener.onShow();
		}
		return super.fadeIn(time);
	}

	@Override public void dispose () {
		listeners.clear();
	}

	public interface EditorListener {
		void onShow();
		void onHide();
	}
}
