package io.piotrjastrzebski.bteditor.core.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

/**
 * Created by PiotrJ on 12/10/15.
 */
public abstract class ViewTarget extends DragAndDrop.Target {

	public ViewTarget (Actor actor) {
		super(actor);
	}

	@Override final public boolean drag (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
		ViewSource s = (ViewSource)source;
		ViewPayload p = (ViewPayload)payload;
		return onDrag(s, p, x, y, pointer);
	}

	public abstract boolean onDrag (ViewSource source, ViewPayload payload, float x, float y, int pointer);

	@Override final public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
		ViewSource s = (ViewSource)source;
		ViewPayload p = (ViewPayload)payload;
		onDrop(s, p, x, y, pointer);
	}

	public abstract void onDrop (ViewSource source, ViewPayload payload, float x, float y, int pointer);

	@Override final public void reset (DragAndDrop.Source source, DragAndDrop.Payload payload) {
		ViewSource s = (ViewSource)source;
		ViewPayload p = (ViewPayload)payload;
		onReset(s, p);
	}

	public void onReset (ViewSource source, ViewPayload payload) {
	}
}
