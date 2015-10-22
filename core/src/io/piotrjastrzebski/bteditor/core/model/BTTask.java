package io.piotrjastrzebski.bteditor.core.model;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by PiotrJ on 15/10/15.
 */
public class BTTask<E> implements Pool.Poolable {
	private Task<E> task;
	private TaskType type;
	private Array<BTTask<E>> children;
	private boolean isValid;
	private BTModel<E> model;
	private BTTask<E> parent;
	private ValidChangeListener<E> changeListener;

	public BTTask (BTModel<E> model) {
		this.model = model;
		children = new Array<>();
	}

	public void init (Task<E> task) {
		if (task == null)
			throw new IllegalArgumentException("Task cannot be null");
		if (this.task != null)
			reset();
		this.task = task;
		type = TaskType.valueFor(task);
		for (int i = 0; i < task.getChildCount(); i++) {
			addChild(task.getChild(i));
		}
		validate();
	}

	public int addChild (Task<E> task) {
		BTTask<E> child = model.obtain();
		child.init(task);
		return addChild(child);
	}

	public int addChild (BTTask<E> child) {
		child.parent = this;
		children.add(child);
		model.addTaskAction(TaskAction.add(task, child.getTask()));
		validate();
		return children.size - 1;
	}

	public int insertChild (int index, Task<E> task) {
		BTTask<E> child = model.obtain();
		child.init(task);
		return insertChild(index, child);
	}

	public int insertChild (int index, BTTask<E> child) {
		child.parent = this;
		children.insert(index, child);
		model.addTaskAction(TaskAction.insert(task, child.getTask(), index));
		validate();
		return children.size - 1;
	}

	public BTTask<E> removeChild (int i) {
		return removeChild(getChild(i));
	}

	public BTTask<E> removeChild (BTTask<E> child) {
		model.addTaskAction(TaskAction.remove(task, child.getTask()));
		children.removeValue(child, true);
		child.parent = null;
		validate();
		return child;
	}

	public boolean validate () {
		// check if we have correct amount of children
		boolean valid = type.isValid(children.size);
		for (BTTask<E> child : children) {
			if (!child.validate()) {
				valid = false;
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

	protected BTTask<E> find (Task<E> target) {
		if (task == target)
			return this;
		for (BTTask<E> child : children) {
			BTTask<E> found = child.find(target);
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
		for (BTTask<E> child : children) {
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

	public BTTask<E> getChild (int i) {
		return children.get(i);
	}

	public boolean isValid () {
		return isValid;
	}

	public TaskType getType () {
		return type;
	}

	public BTTask<E> getParent () {
		return parent;
	}

	protected void setChangeListener (ValidChangeListener<E> listener) {
		this.changeListener = listener;
	}

	protected interface ValidChangeListener<E> {
		void validChanged (BTTask<E> task, boolean isValid);
	}
}
