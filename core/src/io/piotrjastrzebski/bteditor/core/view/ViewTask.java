package io.piotrjastrzebski.bteditor.core.view;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Pool;
import io.piotrjastrzebski.bteditor.core.model.ModelTask;
import io.piotrjastrzebski.bteditor.core.view.ViewTree.DropPoint;

/**
 * Created by EvilEntity on 10/10/2015.
 */
public class ViewTask<E> extends Tree.Node implements Pool.Poolable {
	protected ModelTask<E> task;
	protected Label name;
	protected Label status;
	protected DragAndDrop dad;
	protected Table container;

	protected ViewSource source;
	protected ViewTarget target;

	protected Actor separator;
	protected Drawable containerBG;
	protected ViewTree<E> owner;
	protected Skin skin;

	public ViewTask (final ViewTree<E> owner, Skin skin, Drawable bg) {
		super(new Table(skin));
		this.skin = skin;
		this.owner = owner;
		// object is used to find this node in tree
		setObject(this);
		separator = owner.getSeparator();
		dad = owner.dad;
		container = (Table)getActor();
		name = new Label("", skin);
		status = new Label("", skin);
		container.add(name).pad(2, 0, 2, 10);
		container.add(status);
		// dad prefers touchable things, we want entire node to be a valid target
		container.setTouchable(Touchable.enabled);
		containerBG = bg;
		container.setColor(Color.GREEN);

		source = new ViewSource(getActor(), owner.getPayloadPool()) {
			@Override public ViewPayload dragStart (InputEvent event, float x, float y, int pointer, ViewPayload out) {
				out.setAsMove(ViewTask.this);
				return out;
			}
		};

		target = new ViewTarget(getActor()) {
			@Override public boolean onDrag (ViewSource source, ViewPayload payload, float x, float y, int pointer) {
				Actor actor = getActor();
				DropPoint dropPoint = getDropPoint(actor, y);
				boolean isValid = owner.canAddTo(payload.getViewTask(), ViewTask.this, dropPoint);
				updateSeparator(dropPoint, isValid, separator, container);
				return isValid;
			}

			@Override public void onDrop (ViewSource source, ViewPayload payload, float x, float y, int pointer) {
				// TODO execute proper action
				DropPoint dropPoint = getDropPoint(getActor(), y);
				owner.addTo(payload.getViewTask(), ViewTask.this, dropPoint);
			}

			@Override public void onReset (ViewSource source, ViewPayload payload) {
				updateSeparator(null, true, separator, container);
			}
		};
	}

	public ViewTask<E> init (ModelTask<E> task) {
		this.task = task;
		dad.addSource(source);
		dad.addTarget(target);
		name.setText(task.getName());
		statusChanged(null, task.getStatus());
		validChanged(task.isValid());
		return this;
	}

	public void update (float delta) {
		for (Tree.Node node : getChildren()) {
			((ViewTask)node).update(delta);
		}
	}

	@Override public void reset () {
		name.setText("");
		task = null;
		dad.removeSource(source);
		dad.removeTarget(target);
		for (Tree.Node node : getChildren()) {
			owner.freeVT((ViewTask<E>)node);
		}
	}

	public static final float DROP_MARGIN = 0.25f;

	private DropPoint getDropPoint (Actor actor, float y) {
		float a = y / actor.getHeight();
		if (a < DROP_MARGIN) {
			return DropPoint.BELOW;
		} else if (a > 1 - DROP_MARGIN) {
			return DropPoint.ABOVE;
		}
		return DropPoint.MIDDLE;
	}

	@Override public String toString () {
		return "ViewTask{" +
			"label=" + name.getText() +
			'}';
	}

	protected float fadeTime = 1.5f;

	protected void statusChanged (Task.Status from, Task.Status to) {
		if (owner.getShortStatuses()) {
			status.setText("" + to.toString().charAt(0));
		} else {
			status.setText(to.toString());
		}
		status.setColor(ViewColors.getColor(to));
		status.clearActions();
		status.addAction(Actions.color(Color.GRAY, fadeTime, Interpolation.pow3In));
	}

	protected void validChanged (boolean valid) {
		if (valid) {
			name.setColor(Color.WHITE);
		} else {
			// TODO some sort of a hint?
			name.setColor(Color.RED);
		}
	}

	protected int getIndexInParent () {
		Tree.Node parent = getParent();
		if (parent == null)
			return 0;
		return parent.getChildren().indexOf(this, true);
	}

	public ModelTask<E> getModelTask () {
		return task;
	}

	private static void updateSeparator (DropPoint dropPoint, boolean isValid, Actor sep, Table cont) {
		sep.setVisible(false);
		cont.setBackground((Drawable)null);
		Color color = isValid ? Color.GREEN : Color.RED;
		sep.setColor(color);
		cont.setColor(color);
		// if dp is null we just hide the separator
		if (dropPoint == null)
			return;

		sep.setWidth(cont.getWidth());
		switch (dropPoint) {
		case ABOVE:
			sep.setVisible(true);
			sep.setPosition(cont.getX(), cont.getY() + cont.getHeight() - sep.getHeight());
			break;
		case MIDDLE:
			cont.setBackground("white");
			break;
		case BELOW:
			sep.setVisible(true);
			sep.setPosition(cont.getX(), cont.getY());
			break;
		}
	}

	public ViewTask<E> createClone () {
		return owner.initVT(task.createClone());
	}
}
