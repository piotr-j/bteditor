package io.piotrjastrzebski.bteditor.core.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by PiotrJ on 12/10/15.
 */
public abstract class ViewSource extends DragAndDrop.Source {
	private Pool<ViewPayload> pool;

	public ViewSource (Actor actor, Pool<ViewPayload> pool) {
		super(actor);
		this.pool = pool;
	}

	@Override final public DragAndDrop.Payload dragStart (InputEvent event, float x, float y, int pointer) {
		ViewPayload payload = pool.obtain();
		return dragStart(event, x, y, pointer, payload);
	}

	public abstract ViewPayload dragStart (InputEvent event, float x, float y, int pointer, ViewPayload out);

	@Override final public void dragStop (InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload,
		DragAndDrop.Target target) {
		ViewPayload p = (ViewPayload)payload;
		ViewTarget t = (ViewTarget)target;
		onDragStop(event, x, y, pointer, p, t);
		pool.free(p);
	}

	public void onDragStop (InputEvent event, float x, float y, int pointer, ViewPayload payload, ViewTarget target) {

	}
}
