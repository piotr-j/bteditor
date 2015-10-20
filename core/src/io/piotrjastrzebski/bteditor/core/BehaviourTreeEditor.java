package io.piotrjastrzebski.bteditor.core;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Main entry point for the editor
 *
 * E - type of blackboard in the tree
 *
 * Created by PiotrJ on 20/06/15.
 */
public class BehaviourTreeEditor<E> extends Table {
	private Skin skin;
	private Drawable white;

	public BehaviourTreeEditor (Skin skin, Drawable white) {
		super();
		this.skin = skin;
		this.white = white;

	}

	/**
	 * Initialize the editor with this tree
	 *
	 * @param tree to initialize with
	 */
	public void initialize (BehaviorTree<E> tree) {

	}

	/**
	 * Reset the editor to initial state
	 */
	public void reset () {

	}

	@Override public void act (float delta) {
		super.act(delta);

	}

	public void setStepDelay(float delay) {

	}
}
