package io.piotrjastrzebski.bteditor.core.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.decorator.Include;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by PiotrJ on 15/10/15.
 */
public class ModelTask<E> implements Pool.Poolable {
	private Task<E> task;
	private ModelTaskType type;
	private Array<ModelTask<E>> children;
	private boolean isValid;
	private ModelTree<E> model;
	private ModelTask<E> parent;
	private ValidChangeListener<E> changeListener;
	private String comment;

	public ModelTask (ModelTree<E> model) {
		this.model = model;
		children = new Array<>();
	}

	public void init (Task<E> task) {
		if (task == null)
			throw new IllegalArgumentException("Task cannot be null");
		if (this.task != null)
			reset();
		this.task = task;
		type = ModelTaskType.valueFor(task);
		for (int i = 0; i < task.getChildCount(); i++) {
			addChild(task.getChild(i));
		}
		validate();
	}

	public int addChild (Task<E> task) {
		ModelTask<E> child = model.obtain();
		child.init(task);
		return addChild(child);
	}

	public int addChild (ModelTask<E> child) {
		child.parent = this;
		children.add(child);
		model.addTaskAction(ModelTaskAction.add(task, child.getTask()));
		validate();
		return children.size - 1;
	}

	public int insertChild (int index, Task<E> task) {
		ModelTask<E> child = model.obtain();
		child.init(task);
		return insertChild(index, child);
	}

	public int insertChild (int index, ModelTask<E> child) {
		child.parent = this;
		children.insert(index, child);
		model.addTaskAction(ModelTaskAction.insert(task, child.getTask(), index));
		validate();
		return children.size - 1;
	}

	public ModelTask<E> removeChild (int i) {
		return removeChild(getChild(i));
	}

	public ModelTask<E> removeChild (ModelTask<E> child) {
		model.addTaskAction(ModelTaskAction.remove(task, child.getTask()));
		children.removeValue(child, true);
		child.parent = null;
		validate();
		return child;
	}

	private String lastSubtree;
	public boolean validate () {
		// check if we have correct amount of children
		boolean valid = type.isValid(children.size);
		// include is magic, need some custom handling
		if (task instanceof Include) {
			String subtree = ((Include)task).subtree;
			if (subtree == null || subtree.length() == 0) {
				valid = false;
			} else {
				if (!subtree.equals(lastSubtree)) {
					lastSubtree = subtree;
					// TODO this is dumb
					FileHandle fh = Gdx.files.internal(subtree);
					valid = fh.exists() && !fh.isDirectory();
//					if (valid) {
//						task = new Include<>(subtree, true);
//						init(task);
//						model.requestRebuild();
//					}
				}
			}
		}
		if (valid) {
			for (ModelTask<E> child : children) {
				if (!child.validate()) {
					valid = false;
				}
			}
		}
		setValid(valid);
		return isValid;
	}

	public void setValid (boolean newValid) {
		if (isValid != newValid) {
			isValid = newValid;
			// notify that valid status changed
			if (changeListener != null)
				changeListener.validChanged(this, isValid);
		}
	}

	protected ModelTask<E> find (Task<E> target) {
		if (task == target)
			return this;
		for (ModelTask<E> child : children) {
			ModelTask<E> found = child.find(target);
			if (found != null)
				return found;
		}
		return null;
	}

	public int getIndexInParent () {
		if (parent == null)
			return -1;
		for (int i = 0; i < parent.getChildCount(); i++) {
			if (parent.getChild(i) == this) {
				return i;
			}
		}
		return -1;
	}

	@Override public void reset () {
		// todo free pooled
		for (ModelTask<E> child : children) {
			model.free(child);
		}
		children.clear();
		task = null;
		type = null;
		isValid = false;
		parent = null;
	}

	@Override public String toString () {
		return "BTTask{" +
			"task=" + (task != null ? task.getClass().getSimpleName() : "null") +
			", type=" + type +
			", valid=" + isValid +
			", children=" + getChildCount() +
			'}';
	}

	public Task.Status getStatus () {
		return task.getStatus();
	}

	public String getName () {
		return task.getClass().getSimpleName();
	}

	public Task<E> getTask () {
		return task;
	}

	public int getChildCount () {
		return children.size;
	}

	public ModelTask<E> getChild (int i) {
		return children.get(i);
	}

	public boolean isValid () {
		return isValid;
	}

	public ModelTaskType getType () {
		return type;
	}

	public ModelTask<E> getParent () {
		return parent;
	}

	protected void setChangeListener (ValidChangeListener<E> listener) {
		this.changeListener = listener;
	}

	public ModelTask<E> createClone () {
		ModelTask<E> obtain = (ModelTask<E>)model.obtain();
		obtain.init(task.cloneTask());
		obtain.setComment(getComment());
		return obtain;
	}

	public boolean hasComment () {
		return comment != null;
	}

	public String getComment () {
		return comment;
	}

	public void setComment (String comment) {
		this.comment = comment;
	}

	protected interface ValidChangeListener<E> {
		void validChanged (ModelTask<E> task, boolean isValid);
	}
}
