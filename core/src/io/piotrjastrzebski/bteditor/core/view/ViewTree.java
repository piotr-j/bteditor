package io.piotrjastrzebski.bteditor.core.view;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import io.piotrjastrzebski.bteditor.core.BehaviourTreeEditor;
import io.piotrjastrzebski.bteditor.core.Logger;
import io.piotrjastrzebski.bteditor.core.model.ModelTree;
import io.piotrjastrzebski.bteditor.core.model.ModelTask;

/**
 * Created by EvilEntity on 10/10/2015.
 */
public class ViewTree<E> extends Tree implements Pool.Poolable, ModelTree.Listener<E> {
	private static final String TAG = ViewTree.class.getSimpleName();
	protected Pool<ViewTask<E>> vtPool;
	protected ModelTree<E> model;

	protected DragAndDrop dad;
	private Actor separator;
	private Pool<ViewPayload> payloadPool;
	private boolean shortStatuses;
	private Logger logger = BehaviourTreeEditor.NULL_LOGGER;

	public ViewTree (Skin skin, Drawable white) {
		this(skin, white, 1);
	}

	public ViewTree (Skin skin, Drawable white, float scale) {
		super(skin);
		// remove y spacing so we dont have gaps for DaD
		setYSpacing(0);
		this.separator = new Image(white);
		this.separator.setColor(Color.GREEN);
		this.separator.setHeight(4 * scale);
		this.separator.setVisible(false);
		addActor(separator);

		payloadPool = new Pool<ViewPayload>() {
			@Override protected ViewPayload newObject () {
				return new ViewPayload(skin);
			}
		};

		dad = new DragAndDrop();
		vtPool = new Pool<ViewTask<E>>() {
			@Override protected ViewTask<E> newObject () {
				return new ViewTask<>(ViewTree.this, skin, white);
			}
		};
		// single selection makes thing simpler for edit
		getSelection().setMultiple(false);
		addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				ViewTree.this.changed((ViewTask<E>)getSelection().getLastSelected());
			}
		});
	}

	public void update (float delta) {
		if (viewRoot == null)
			return;
		viewRoot.update(delta);
	}

	private void changed (ViewTask<E> selection) {
		if (selection != null) {
			for (ViewTaskSelectedListener<E> listener : listeners) {
				listener.selected(selection);
			}
		} else {
			for (ViewTaskSelectedListener<E> listener : listeners) {
				listener.deselected();
			}
		}
	}

	ViewTask<E> viewRoot;

	public void init (ModelTree<E> model) {
		if (this.model != null)
			reset();
		this.model = model;
		model.addListener(this);
		ModelTask<E> root = model.getRootNode();
		add(viewRoot = initVT(root));
		for (int i = 0; i < root.getChildCount(); i++) {
			addViewTask(viewRoot, root.getChild(i));
		}
		expandAll();
	}

	protected void addViewTask (ViewTask<E> parent, ModelTask<E> task) {
		ViewTask<E> node = initVT(task);
		parent.add(node);
		for (int i = 0; i < task.getChildCount(); i++) {
			addViewTask(node, task.getChild(i));
		}
	}

	protected ViewTask<E> initVT (ModelTask<E> task) {
		ViewTask<E> out = vtPool.obtain();
		out.init(task);
		return out;
	}

	protected void freeVT (ViewTask<E> vt) {
		vtPool.free(vt);
	}

	@Override public void reset () {
		if (model != null) {
			model.removeListener(this);
		}
		model = null;
		// TODO fix pooling for vts
		remove(viewRoot);
		freeVT(viewRoot);
		vtPool.clear();
	}

	protected Array<ViewTaskSelectedListener<E>> listeners = new Array<>();

	public void addListener (ViewTaskSelectedListener<E> listener) {
		if (!listeners.contains(listener, true)) {
			listeners.add(listener);
		}
	}

	public void removeListener (ViewTaskSelectedListener<E> listener) {
		listeners.removeValue(listener, true);
	}

	// TODO use model for this crap
	protected ObjectMap<Class<? extends Task>, Task<E>> classToTask = new ObjectMap<>();

	/**
	 * register given actor as source with task that can be added to the tree
	 */
	public void addSource (Actor source, final Class<? extends Task> task) {
		if (classToTask.containsKey(task)) {
			logger.log(TAG, "Task class already added: " + task);
			return;
		}
		try {
			Task instance = ClassReflection.newInstance(task);
			classToTask.put(task, instance);
		} catch (ReflectionException e) {
			logger.error(TAG, "Failed to instantience task " + task, e);
			return;
		}
		dad.addSource(new ViewSource(source, getPayloadPool()) {
			@Override public ViewPayload dragStart (InputEvent event, float x, float y, int pointer, ViewPayload out) {
				// TODO should this create a node for this already?
				ModelTask<E> mt = model.obtain();
				Task<E> eTask = model.getTaskLibrary().get(task);
				mt.init(eTask);
				ViewTask<E> vt = initVT(mt);
				out.setAsAdd(vt);
				return out;
			}

			@Override public void onDragStop (InputEvent event, float x, float y, int pointer, ViewPayload payload,
				ViewTarget target) {
				ViewTask vt = payload.getViewTask();
				// vt wasnt added
				if (vt.getParent() == null) {
					model.free(vt.getModelTask());
					freeVT(vt);
				}
			}
		});
	}

	public void addTrash (Actor trash) {
		dad.addTarget(new ViewTarget(trash) {
			@Override public boolean onDrag (ViewSource source, ViewPayload payload, float x, float y, int pointer) {
				return payload.hasTarget(ViewPayload.TARGET_TRASH);
			}

			@Override public void onDrop (ViewSource source, ViewPayload payload, float x, float y, int pointer) {
				// TODO confirm?
				trash(payload.getViewTask());
			}
		});
	}

	public Actor getSeparator () {
		return separator;
	}

	/**
	 * @return if we can add new vt to target at given drop point
	 */
	public boolean canAddTo (ViewTask<E> vt, ViewTask<E> target, DropPoint to) {
		// we cant add to own children, thats about it
		// some thing might result in broken tree, but it will be indicated
		// TODO add check add mt mt to model?
		if (!model.checkAdd(target.getModelTask(), vt.getModelTask().getTask()) && to.equals(DropPoint.MIDDLE)) {
			return false;
		}
		return vt.findNode(target) == null;
	}

	/**
	 * Add new node to target at dp
	 */
	public void addTo (ViewTask<E> vt, ViewTask<E> target, DropPoint to) {
		// TODO do we want to double check?
		if (!canAddTo(vt, target, to)) {
			logger.log(TAG, target + " cant be added to " + vt + " at " + to);
			return;
		}
		// TODO change model as well
		// if the view task is already in the tree, remove it
		vt.remove();

		ModelTask<E> toAdd = vt.getModelTask();
		if (toAdd.getParent() != null) {
			model.remove(toAdd);
		}

		ViewTask<E> parent = (ViewTask<E>)target.getParent();
		ModelTask<E> targetMT = target.getModelTask();
		switch (to) {
		case ABOVE:
			// insert vt before target
			if (parent != null) {
				parent.insert(target.getIndexInParent(), vt);
				model.insert(parent.getModelTask(), toAdd, targetMT.getIndexInParent());
			} else {
				logger.error(TAG, "Null parent in addTo above !" + target);
			}
			break;
		case MIDDLE:
			// add vt to target
			target.add(vt);
			vt.init(toAdd);
			model.add(targetMT, toAdd);
			break;
		case BELOW:
			// insert vt after target
			if (parent != null) {
				parent.insert(target.getIndexInParent() + 1, vt);
				model.insert(parent.getModelTask(), toAdd, targetMT.getIndexInParent() + 1);
			} else {
				logger.error(TAG, "Null parent in addTo below !" + target);
			}
			break;
		}
		// parent is null when adding to the root
		if (parent != null)
			parent.expandAll();
	}

	public void trash (ViewTask<E> vt) {
		model.remove(vt.getModelTask());
		freeVT(vt);
		remove(vt);
	}

	@Override public void statusChanged (ModelTask<E> task, Task.Status from, Task.Status to) {
		ViewTask<E> vt = find(viewRoot, task);
		if (vt == null) {
			logger.log(TAG, "VT for" + task + " in statusChanged not found!");
			return;
		}
		vt.statusChanged(from, to);
	}

	@Override public void validityChanged (ModelTask<E> task, boolean isValid) {
		ViewTask<E> vt = find(viewRoot, task);
		if (vt == null) {
			logger.log(TAG, "VT for" + task + " int validChanged not found!");
			return;
		}
		vt.validChanged(isValid);
	}

	private ViewTask<E> find (ViewTask<E> parent, ModelTask<E> task) {
		// do we want ref only here?
		if (parent.getModelTask() == task) {
			return parent;
		}
		for (Tree.Node node : parent.getChildren()) {
			ViewTask<E> found = find((ViewTask<E>)node, task);
			if (found != null)
				return found;
		}
		return null;
	}

	public Pool<ViewPayload> getPayloadPool () {
		return payloadPool;
	}

	public void setShortStatuses (boolean shortStatuses) {
		this.shortStatuses = shortStatuses;
	}

	public boolean getShortStatuses () {
		return shortStatuses;
	}

	public void setLogger (Logger logger) {
		this.logger = logger;
		AttrFieldEdit.setLogger(logger);
	}

	public interface ViewTaskSelectedListener<E> {
		void selected (ViewTask<E> task);

		void deselected ();
	}

	enum DropPoint {
		ABOVE, MIDDLE, BELOW
	}
}
