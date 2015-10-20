package io.piotrjastrzebski.bteditor.core.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

/**
 * Created by PiotrJ on 12/10/15.
 */
public class BTEPayload extends DragAndDrop.Payload implements Pool.Poolable {
	public static final int TARGET_TRASH = 1;
	public static final int TARGET_ADD = 1 << 1;
	public static final int TARGET_MOVE = 1 << 2;

	protected Label drag;
	protected Label valid;
	protected Label invalid;
	protected int targetMask;

	public BTEPayload (Skin skin) {
		drag = new Label("", skin);
		setDragActor(drag);
		valid = new Label("", skin);
		valid.setColor(Color.GREEN);
		setValidDragActor(valid);
		invalid = new Label("", skin);
		invalid.setColor(Color.RED);
		setInvalidDragActor(invalid);
	}

	public void setDragText (String text) {
		drag.setText(text);
		valid.setText(text);
		invalid.setText(text);
	}

	public BTEPayload addTarget(int target) {
		targetMask |= target;
		return this;
	}

	public boolean hasTarget (int target) {
		return (targetMask & target) != 0;
	}

	@Override public void reset () {
		targetMask = 0;
		vt = null;
		setDragText("<?>");
	}

	public void setAsAdd (ViewTask task) {
		vt = task;
		setDragText(task.getModelTask().getName());
		addTarget(TARGET_ADD);
	}

	protected ViewTask vt;
	public void setAsMove (ViewTask task) {
		vt = task;
		setDragText(task.getModelTask().getName());
		addTarget(TARGET_MOVE);
		addTarget(TARGET_TRASH);
	}

	public ViewTask getViewTask () {
		return vt;
	}
}
