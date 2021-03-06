package io.piotrjastrzebski.bteditor.core.model;

import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.Decorator;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.decorator.Include;

/**
 * Created by EvilEntity on 14/10/2015.
 */
public enum ModelTaskType {
	BRANCH("Branch", 1, Integer.MAX_VALUE),
	DECORATOR("Decorator", 1, 1),
	INCLUDE("Include", 0, 1),
	LEAF("Leaf", 0, 0);

	private String name;
	private int minChildren;
	private int maxChildren;

	ModelTaskType (String name, int min, int max) {
		this.name = name;
		minChildren = min;
		maxChildren = max;
	}

	/**
	 * Check if this count is a valid for this {@link ModelTaskType}
	 *
	 * @param count count to check
	 * @return if count is within min and max
	 */
	public boolean isValid (int count) {
		return minChildren <= count && count <= maxChildren;
	}

	/**
	 * Get TaskType for given Task
	 */
	public static ModelTaskType valueFor (Task task) {
		if (task instanceof Include) {
			return ModelTaskType.INCLUDE;
		}
		if (task instanceof Decorator) {
			return ModelTaskType.DECORATOR;
		}
		if (task instanceof BranchTask) {
			return ModelTaskType.BRANCH;
		}
		return ModelTaskType.LEAF;
	}

	@Override public String toString () {
		return name + "{" +
			minChildren +
			// do we pretend that oo is infinity?
			", " + (maxChildren == Integer.MAX_VALUE ? "oo" : maxChildren) +
			'}';
	}
}
