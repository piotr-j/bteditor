package io.piotrjastrzebski.bteditor.core;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.bteditor.core.model.ModelTree;
import io.piotrjastrzebski.bteditor.core.view.ViewTaskAttributeEdit;
import io.piotrjastrzebski.bteditor.core.view.ViewTask;
import io.piotrjastrzebski.bteditor.core.view.ViewTree;

/**
 * Main entry point for the editor
 * <p>
 * E - type of blackboard in the tree
 * <p>
 * Created by PiotrJ on 20/06/15.
 */
public class BehaviourTreeEditor<E> extends Table implements ViewTree.ViewTaskSelectedListener<E> {
	public static Logger NULL_LOGGER = new Logger() {
		@Override public void log (String tag, String msg) {}
		@Override public void error (String tag, String msg) {}
		@Override public void error (String tag, String msg, Exception e) {}
	};
	private Skin skin;

	private Array<TaskNode> nodes = new Array<>();
	private Table tasks;
	private Label trash;

	private ModelTree<E> model;
	private ViewTree<E> view;
	private ViewTaskAttributeEdit edit;
	private Logger logger = NULL_LOGGER;

	public BehaviourTreeEditor (Skin skin, Drawable white) {
		this(skin, white, 1);
	}

	public BehaviourTreeEditor (Skin skin, Drawable white, float scale) {
		super();
		this.skin = skin;
		model = new ModelTree<>();
		debugAll();
		trash = new Label("Trash -> [_]", skin);
		add(trash).colspan(3);
		row();
		edit = new ViewTaskAttributeEdit(skin);
		view = new ViewTree<>(skin, white, scale);
		view.addListener(this);
		view.addTrash(trash);
		view.setShortStatuses(true);
		add(view).expand().fill();
		tasks = new Table();
		add(edit).expand().fill();
		Table paneCont = new Table();
		paneCont.add(new Label("DragAndDrop", skin)).row();
		ScrollPane pane = new ScrollPane(tasks);
		paneCont.add(pane);
		add(paneCont).expand().fill().top();
	}

	public void setLogger (Logger logger) {
		if (logger == null) {
			this.logger = NULL_LOGGER;
		} else {
			this.logger = logger;
		}
		model.setLogger(logger);
		view.setLogger(logger);
	}

	/**
	 * Initialize the editor with this tree
	 *
	 * @param tree to initialize with
	 */
	public void initialize (BehaviorTree<E> tree) {
		model.init(tree);
		view.init(model);
	}

	@Override public void selected (ViewTask<E> task) {
		edit.startEdit(task.getModelTask().getTask());
	}

	@Override public void deselected () {
		edit.stopEdit();
	}

	/**
	 * Reset the editor to initial state
	 */
	public void reset () {
		view.reset();
		model.reset();
	}

	private float delay = 1;
	private float timer;

	@Override public void act (float delta) {
		super.act(delta);
		timer += delta;
		if (timer > delay) {
			timer -= delay;
			model.step();
		}
	}

	public void setStepDelay (float delay) {
		this.delay = delay;
	}

	public void addTaskClass (Class<? extends Task> aClass) {
		model.getTaskLibrary().add(aClass);
		TaskNode node = new TaskNode(aClass, skin);
		nodes.add(node);
		view.addSource(node, aClass);
		tasks.add(node).row();
	}

	public ModelTree<E> getModel () {
		return model;
	}

	public ViewTree<E> getView () {
		return view;
	}

	private static class TaskNode extends Label {
		public Class<? extends Task> taskClass;

		public TaskNode (Class<? extends Task> taskClass, Skin skin) {
			super(taskClass.getSimpleName(), skin);
			this.taskClass = taskClass;
		}
	}
}
