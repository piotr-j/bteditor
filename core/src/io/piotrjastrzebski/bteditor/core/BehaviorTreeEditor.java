package io.piotrjastrzebski.bteditor.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibrary;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibraryManager;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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
public class BehaviorTreeEditor<E> extends Table implements ViewTree.ViewTaskSelectedListener<E> {
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

	private IPersist<E> persist;

	private TextButton saveBtn;
	private TextButton saveAsBtn;
	private TextButton loadBtn;
	private TextButton pauseBtn;
	private TextButton stepBtn;
	private RelativeFileHandleResolver resolver;

	public BehaviorTreeEditor (Skin skin, Drawable white) {
		this(skin, white, 1);
	}

	public BehaviorTreeEditor (Skin skin, Drawable white, float scale) {
		super();
		this.skin = skin;
		// we need to set custom resolver so we can load includes that are not from cwd
		resolver = new RelativeFileHandleResolver();
		BehaviorTreeLibraryManager.getInstance().setLibrary(new BehaviorTreeLibrary(resolver));

		model = new ModelTree<>();
		debugAll();
		trash = new Label("Trash -> [_]", skin);
		add(createTopMenu()).colspan(3);
		row();
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

	private Table createTopMenu() {
		Table topMenu = new Table();
		topMenu.defaults().pad(5);
		saveBtn = new TextButton("Save", skin);
		saveBtn.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				if (persist == null) {
					logger.error("BTE", "You need to set IPersist before you can save a tree");
					return;
				}
				if (stepBtn.isDisabled()) return;
				persist.onSave(BehaviorTreeWriter.serialize(model.getBehaviorTree()));
			}
		});
		topMenu.add(saveBtn);
		saveAsBtn = new TextButton("Save As...", skin);
		saveAsBtn.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				if (persist == null) {
					logger.error("BTE", "You need to set IPersist before you can save as a tree");
					return;
				}
				if (saveAsBtn.isDisabled()) return;
				persist.onSaveAs(BehaviorTreeWriter.serialize(model.getBehaviorTree()));
			}
		});
		topMenu.add(saveAsBtn);
		loadBtn = new TextButton("Load...", skin);
		loadBtn.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				if (persist == null) {
					logger.error("BTE", "You need to set IPersist before you can load a tree");
					return;
				}
				persist.onLoad();
			}
		});
		topMenu.add(loadBtn);
		pauseBtn = new TextButton("Pause", skin, "toggle");
		pauseBtn.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				if (pauseBtn.isDisabled()) return;
				if (pauseBtn.isChecked()) {
					pauseBtn.setText("Resume");
				} else {
					pauseBtn.setText("Pause");
				}
			}
		});
		topMenu.add(pauseBtn);
		stepBtn = new TextButton("Step", skin);
		stepBtn.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				if (stepBtn.isDisabled()) return;
				// step only if the tree is paused
				if (!pauseBtn.isChecked()) return;
				model.step();
			}
		});
		topMenu.add(stepBtn);
		disableSaveBtns(true);
		return topMenu;
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

	public void setPersist(IPersist<E> persist) {
		this.persist = persist;
		disableSaveBtns(persist == null);
	}

	private void disableSaveBtns(boolean disable) {
		saveBtn.setDisabled(disable);
		saveAsBtn.setDisabled(disable);
	}

	/**
	 * Initialize the editor with this tree
	 *
	 * @param tree to initialize with
	 */
	public void initialize (BehaviorTree<E> tree, String root) {
		resolver.setRoot(root);
		Gdx.app.log("", "root = " + root);
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
		if (timer > delay && !pauseBtn.isChecked()) {
			timer -= delay;
			model.step();
		}
		checkValidity(model.isValid());
	}

	private boolean wasValid;
	private void checkValidity (boolean valid) {
		if (wasValid != valid) {
			wasValid = valid;
			toggleButtons(!valid);
		}
	}

	private void toggleButtons(boolean disabled) {
		disableSaveBtns(disabled || persist == null);

		pauseBtn.setDisabled(disabled);
		stepBtn.setDisabled(disabled);
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
