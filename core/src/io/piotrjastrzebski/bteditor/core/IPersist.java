package io.piotrjastrzebski.bteditor.core;

import com.badlogic.gdx.ai.btree.BehaviorTree;

/**
 * Created by PiotrJ on 16/10/15.
 */
public interface IPersist<E> {
	public void onSave(String tree);
	public void onSaveAs(String tree);
	public BehaviorTree<E> onLoad();
}
