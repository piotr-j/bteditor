package io.piotrjastrzebski.bteditor.core.model;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import io.piotrjastrzebski.bteditor.core.BehaviorTreeEditor;
import io.piotrjastrzebski.bteditor.core.Logger;

/**
 * Model that represents {@link BehaviorTree} so it can be edited
 * <p>
 * Stuff we want to do:
 * modify the tree
 * - remove any node
 * - move any node to a another valid node, ie not inside own children
 * - add new node at specified place
 * validate the tree, node child count must be valid before the tree is running
 * if model is valid, update the underlying {@link BehaviorTree}
 * notify observers that node status changed, valid, position in tree, removed etc
 * edit params of {@link Task}s
 * <p>
 * Created by EvilEntity on 14/10/2015.
 */
public class ModelTree<E> implements Pool.Poolable, BehaviorTree.Listener<E>, ModelTask.ValidChangeListener<E> {
	private final static String TAG = ModelTree.class.getSimpleName();

	private TaskLibrary<E> taskLibrary;
	private BehaviorTree<E> bt;
	private boolean valid;
	private boolean dirty;
	private ModelTask<E> root;
	private Array<ModelTaskAction> pending = new Array<>();
	protected Logger logger = BehaviorTreeEditor.NULL_LOGGER;

	public ModelTree () {
		taskLibrary = new TaskLibrary<>();
	}

	public void init (BehaviorTree<E> bt) {
		if (this.bt != null)
			reset();
		this.bt = bt;
		getTaskLibrary().initFrom(bt);
		// TODO pool all the things
		root = obtain();
		root.init(bt.getChild(0));
		valid = root.isValid();
		if (valid) executePending();
		bt.addListener(this);
	}

	public void step () {
		if (!isValid()) {
			logger.log(TAG, "invalid");
			return;
		}
		if (dirty) {
			logger.log(TAG, "dirty, reset bt");
			dirty = false;
			bt.reset();
		}
		bt.step();
	}

	public boolean checkAdd (ModelTask<E> target, Task<E> task) {
		return checkAdd(target, task.getClass());
	}

	public boolean checkAdd (ModelTask<E> target, Class<? extends Task> task) {
		// we allow adding if target is not a Leaf
		return !ModelTaskType.LEAF.equals(target.getType());
	}

	public ModelTask<E> add (ModelTask<E> target, Class<? extends Task> task) {
		Task<E> eTask = taskLibrary.get(task);
		if (eTask == null) {
			logger.error(TAG, task + " is not a registered task type, register it via TaskLibrary#add()}");
			return null;
		}
		return add(target, eTask);
	}

	public ModelTask<E> add (ModelTask<E> target, Task<E> task) {
		ModelTask<E> node = obtain();
		node.init(task);
		return add(target, node);
	}

	public ModelTask<E> add (ModelTask<E> target, ModelTask<E> task) {
		if (!checkAdd(target, task.getTask())) {
			logger.error(TAG, task + " is not a valid add target to " + target);
			return null;
		}
		target.addChild(task);
		validate();
		dirty = true;
		return task;
	}

	public boolean checkInsert (ModelTask<E> target, Task<E> task, int at) {
		return checkInsert(target, task.getClass(), at);
	}

	public boolean checkInsert (ModelTask<E> target, Class<? extends Task> task, int at) {
		// we allow inserting if target is not a Leaf
		return !ModelTaskType.LEAF.equals(target.getType());
	}

	public ModelTask<E> insert (ModelTask<E> target, Class<? extends Task> task, int at) {
		Task<E> eTask = taskLibrary.get(task);
		if (eTask == null) {
			logger.error(TAG, task + " is not a registered task type, register it via TaskLibrary#add()}");
			return null;
		}
		return insert(target, eTask, at);
	}

	public ModelTask<E> insert (ModelTask<E> target, Task<E> task, int at) {
		ModelTask<E> node = obtain();
		node.init(task);
		return insert(target, node, at);
	}

	public ModelTask<E> insert (ModelTask<E> target, ModelTask<E> task, int at) {
		if (!checkInsert(target, task.getTask(), at)) {
			logger.error(TAG, task + " is not a valid insert target to " + target + " at " + at);
			return null;
		}
		target.insertChild(at, task);
		task.validate();
		dirty = true;
		return task;
	}

	public ModelTask<E> remove (Task<E> target) {
		ModelTask<E> task = findBTTask(target);
		if (task == null) {
			logger.error(TAG, target + " not in the mode!");
			return null;
		}
		return remove(task);
	}

	public ModelTask<E> remove (ModelTask<E> target) {
		ModelTask<E> parent = target.getParent();
		// root has null parent
		if (parent == null) {
			if (target == root) {
				reset();
			} else {
				logger.error(TAG, "Target is not part of the model!");
			}
			return target;
		}
		parent.removeChild(target);
		validate();
		dirty = true;
		return target;
	}

	protected void addTaskAction(ModelTaskAction action) {
		pending.add(action);
	}

	private ModelTask<E> findBTTask (Task<E> target) {
		return root.find(target);
	}

	public ModelTask<E> obtain () {
		ModelTask<E> ebtTask = new ModelTask<>(this);
		ebtTask.setChangeListener(this);
		return ebtTask;
	}

	public void free (ModelTask<E> task) {
		task.reset();
	}

	public boolean validate () {
		if (root == null) {
			return valid = false;
		}
		valid = root.validate();
		if (valid) {
			// execute pending, modify wrapped behavior tree
			executePending();
		}
		return valid;
	}

	protected void executePending () {
		for (ModelTaskAction modelTaskAction : pending) {
			modelTaskAction.execute();
		}
		pending.clear();
	}

	public boolean isDirty () {
		return pending.size > 0;
	}

	public boolean isValid () {
		validate();
		return valid;
	}

	Array<Listener<E>> listeners = new Array<>();

	public void addListener (Listener<E> listener) {
		if (!listeners.contains(listener, true)) {
			listeners.add(listener);
		}
	}

	public void removeListener (Listener<E> listener) {
		listeners.removeValue(listener, true);
	}

	@Override public void statusUpdated (Task<E> task, Task.Status previousStatus) {
		ModelTask<E> modelTask = findBTTask(task);
		if (modelTask == null) {
			logger.error(TAG, "ModelTask not found for " + task);
			return;
		}
		for (Listener<E> listener : listeners) {
			listener.statusChanged(modelTask, previousStatus, modelTask.getStatus());
		}
	}

	@Override public void childAdded (Task<E> task, int index) {

	}

	@Override public void validChanged (ModelTask<E> task, boolean isValid) {
		for (Listener<E> listener : listeners) {
			listener.validityChanged(task, isValid);
		}
	}

	@Override public void reset () {
		if (root != null)
			root.reset();
		root = null;
		valid = false;
		if (bt != null) {
			bt.removeListener(this);
			bt = null;
		}
	}

	@Override public String toString () {
		return "BTModel{" +
			"bt=" + bt +
			", valid=" + valid +
			'}';
	}

	public TaskLibrary<E> getTaskLibrary () {
		return taskLibrary;
	}

	public ModelTask<E> getRootNode () {
		return root;
	}

	public void setLogger (Logger logger) {
		this.logger = logger;
		ModelTaskAction.setLogger(logger);
	}

	public BehaviorTree getBehaviorTree () {
		return bt;
	}

	public interface Listener<E> {
		void statusChanged (ModelTask<E> task, Task.Status from, Task.Status to);

		void validityChanged (ModelTask<E> task, boolean isValid);
	}
}
