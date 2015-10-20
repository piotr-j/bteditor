package io.piotrjastrzebski.bteditor.core.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by PiotrJ on 12/10/15.
 */
public abstract class BTESource extends DragAndDrop.Source {
	private Pool<BTEPayload> pool;

	public BTESource (Actor actor, Pool<BTEPayload> pool) {
		super(actor);
		this.pool = pool;
	}

	@Override final public DragAndDrop.Payload dragStart (InputEvent event, float x, float y, int pointer) {
		BTEPayload payload = pool.obtain();
		return dragStart(event, x, y, pointer, payload);
	}

	public abstract BTEPayload dragStart (InputEvent event, float x, float y, int pointer, BTEPayload out);

	@Override final public void dragStop (InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload,
		DragAndDrop.Target target) {
		BTEPayload p = (BTEPayload)payload;
		BTETarget t = (BTETarget)target;
		onDragStop(event, x, y, pointer, p, t);
		pool.free(p);
	}

	public void onDragStop (InputEvent event, float x, float y, int pointer, BTEPayload payload, BTETarget target) {

	}
}
