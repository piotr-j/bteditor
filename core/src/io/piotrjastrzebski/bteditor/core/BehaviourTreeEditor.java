package io.piotrjastrzebski.bteditor.core;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.bteditor.core.model.BTModel;
import io.piotrjastrzebski.bteditor.core.view.AttrEdit;
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
	private Skin skin;

	private Array<TaskNode> nodes = new Array<>();
	private Table tasks;
	private Label trash;

	private BTModel<E> model;
	private ViewTree<E> view;
	private AttrEdit edit;

	public BehaviourTreeEditor (Skin skin, Drawable white) {
		this(skin, white, 1);
	}

	public BehaviourTreeEditor (Skin skin, Drawable white, float scale) {
		super();
		this.skin = skin;
		model = new BTModel<>();
		debugAll();
		trash = new Label("Trash -> [_]", skin);
		add(trash).colspan(3);
		row();
		edit = new AttrEdit(skin);
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

	public BTModel<E> getModel () {
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
