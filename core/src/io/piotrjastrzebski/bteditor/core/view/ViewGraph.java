package io.piotrjastrzebski.bteditor.core.view;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.bteditor.core.model.ModelTask;
import io.piotrjastrzebski.bteditor.core.model.ModelTree;

/**
 * Created by PiotrJ on 30/10/15.
 */
public class ViewGraph<E> extends Table implements ModelTree.Listener<E> {
	protected Skin skin;
	protected TextureRegionDrawable line;
	protected Node<E> root;
	protected ModelTree<E> model;

	public ViewGraph (TextureRegionDrawable line, Skin skin) {
		this.line = line;
		this.skin = skin;
	}

	public void init (ModelTree<E> model) {
		rebuild(model);
//		debugAll();
	}

	@Override public void reset () {
		super.reset();
		if (model != null) model.removeListener(this);
	}

	private void rebuild (ModelTree<E> model) {
		if (root != null) clear();
		this.model = model;
		model.addListener(this);
		ModelTask<E> rootTask = model.getRootNode();
		root = new Node<>(createTaskActor(rootTask), rootTask, skin);
		add(root).expand().fillX().top();
		for (int i = 0; i < rootTask.getChildCount(); i++) {
			ModelTask<E> child = rootTask.getChild(i);
			createNodes(root, child);
		}
	}

	private void createNodes (Node<E> parent, ModelTask<E> task) {
		Node<E> node = new Node<>(createTaskActor(task), task, skin);
		parent.addNode(node);
		for (int i = 0; i < task.getChildCount(); i++) {
			ModelTask<E> child = task.getChild(i);
			createNodes(node, child);
		}
	}

	private Actor createTaskActor(ModelTask<E> task) {
		return new Label(task.getName(), skin);
	}

	@Override public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		if (root != null)
			drawConnections(batch, root);
	}

	private Vector2 start = new Vector2();
	private Vector2 end = new Vector2();
	private Vector2 tmp = new Vector2();
	private void drawConnections (Batch batch, Node<E> root) {
		if (!root.childrenVisible()) return;
		Actor rootActor = root.actor;
		start.set(rootActor.getX() + rootActor.getWidth() / 2, rootActor.getY());
		rootActor.localToAscendantCoordinates(this, start);
		float sx = start.x;
		float sy = start.y;

		for (Node<E> node : root.childrenNodes) {
			Actor nodeActor = node.actor;
			end.set(nodeActor.getX() + nodeActor.getWidth() / 2, nodeActor.getY() + nodeActor.getHeight());
			nodeActor.localToAscendantCoordinates(this, end);

			float len = tmp.set(end).sub(sx, sy).len();
			float angle = tmp.angle();
			batch.setColor(node.actor.getColor());
			line.draw(batch, sx, sy, 0, 1.5f, len, 3, 1, 1, angle);
			drawConnections(batch, node);
		}
	}

	private Node<E> findNode (Task<E> task) {
		return root.findNode(task);
	}

	@Override public void statusChanged (ModelTask<E> task, Task.Status from, Task.Status to) {
		Node<E> node = findNode(task.getTask());
		if (node != null) {
			Actor actor = node.actor;
			actor.clearActions();
			actor.setColor(ViewColors.getColor(task.getStatus()));
			actor.addAction(Actions.color(Color.GRAY, 1.5f, Interpolation.pow3In));
		}
	}

	@Override public void validityChanged (ModelTask<E> task, boolean isValid) {

	}

	@Override public void rebuild () {
		clear();
		rebuild(model);
		invalidateHierarchy();
	}

	@Override public void clear () {
		super.clear();
		root = null;
		model.removeListener(this);
	}

	public static class Node<E> extends Table  {
		protected Actor actor;
		protected ModelTask<E> task;
		protected Array<Node<E>> childrenNodes = new Array<>();
		protected Table top;
		protected TextButton hide;
		protected Table children;

		public Node (Actor actor, ModelTask<E> task, Skin skin) {
			this.actor = actor;
			this.task = task;
			actor.setColor(ViewColors.getColor(task.getStatus()));
			top = new Table();
			add(top).row();
			top.add(actor);
			// NOTE would be lovely if we could make this work, depth is messed up like that.
			add().pad(15).row();//.fill().expand().row();
			children = new Table();
			add(children).expand().fill();

			hide = new TextButton("<", skin);
			hide.addListener(new ClickListener() {
				@Override public void clicked (InputEvent event, float x, float y) {
					if (children.getParent() == null) {
						add(children).expand().fill();
						hide.setText("<");
					} else {
						clear();
						add(top).row();
						add().pad(15).row();
						hide.setText(">");
					}
				}
			});
		}

		public boolean childrenVisible () {
			return children.getParent() != null;
		}

		public void addNode (Node<E> node) {
			childrenNodes.add(node);
			children.add(node).expand().fillX().top().pad(5);
			if (hide.getParent() == null)
				top.add(hide).padLeft(5);
		}

		public Node<E> findNode (Task<E> task) {
			if (this.task.getTask() == task) return this;
			for (Node<E> node : childrenNodes) {
				Node<E> found = node.findNode(task);
				if (found != null) return found;
			}
			return null;
		}
	}
}

