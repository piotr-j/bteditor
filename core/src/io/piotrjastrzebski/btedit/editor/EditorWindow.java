package io.piotrjastrzebski.btedit.editor;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.widget.*;

/**
 * Created by EvilEntity on 03/10/2015.
 */
public class EditorWindow extends VisWindow implements Disposable {
	MenuBar menuBar;

	VisTable root;
	public EditorWindow () {
		super("BTEdit");
		addCloseButton();
		setResizable(true);
		setSize(500, 500);

		add(createMenus()).fillX().expandX();
		row();
		add(root = new VisTable(true)).fill().expand();

	}

	private Table createMenus () {
		menuBar = new MenuBar();
		Menu mainMenu = new Menu("Main");
		mainMenu.addItem(new MenuItem("Item A"));
		mainMenu.addItem(new MenuItem("Item B"));
		mainMenu.addSeparator();
		mainMenu.addItem(new MenuItem("Item C"));

		return menuBar.getTable();
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
