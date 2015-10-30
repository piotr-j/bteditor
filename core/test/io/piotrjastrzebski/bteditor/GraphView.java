package io.piotrjastrzebski.bteditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisLabel;
import io.piotrjastrzebski.bteditor.core.model.ModelTask;
import io.piotrjastrzebski.bteditor.core.model.ModelTree;


/**
 * Created by PiotrJ on 30/10/15.
 */
public class GraphView<E> extends Table implements ModelTree.Listener<E> {
	Node root;
	TextureRegionDrawable line;
	ModelTree model;

	public GraphView (TextureRegionDrawable line, ModelTree<E> model) {
		this.line = line;
		this.model = model;
		model.addListener(this);
		rebuild(model.getRootNode().getTask());
	}

	private void rebuild (Task task) {
		GraphView.Node root = new GraphView.Node(createTaskActor(task), task);
		setRoot(root);
		for (int i = 0; i < task.getChildCount(); i++) {
			Task child = task.getChild(i);
			createNodes(root, child);
		}
	}

	private void createNodes (GraphView.Node parent, Task task) {
		GraphView.Node node = new GraphView.Node(createTaskActor(task), task);
		parent.addNode(node);
		for (int i = 0; i < task.getChildCount(); i++) {
			Task child = task.getChild(i);
			createNodes(node, child);
		}
	}

	private Actor createTaskActor(Task task) {
		return new VisLabel(task.getClass().getSimpleName());
	}

	public void setRoot (Node root) {
		this.root = root;
		add(root);
	}

	public static Color getColor (Task.Status status) {
		if (status == null)
			return Color.GRAY;
		switch (status) {
		case SUCCEEDED:
			return Color.GREEN;
		case RUNNING:
			return Color.ORANGE;
		case FAILED:
			return Color.RED;
		case CANCELLED:
			return Color.PURPLE;
		case FRESH:
		default:
			return Color.GRAY;
		}
	}

	private Node findNode (Task task) {
		return root.findNode(task);
	}

	@Override public void statusChanged (ModelTask task, Task.Status from, Task.Status to) {
		Node node = findNode(task.getTask());
		if (node != null) {
			Actor actor = node.actor;
			actor.clearActions();
			actor.setColor(getColor(task.getStatus()));
			actor.addAction(Actions.color(Color.GRAY, 1.5f, Interpolation.pow3In));
		}
	}

	@Override public void validityChanged (ModelTask task, boolean isValid) {

	}

	@Override public void rebuild () {
		Gdx.app.log("", "rebuild");
		clear();
		rebuild(model.getRootNode().getTask());
	}

	public static class Node extends Table  {
		Actor actor;
		Task task;
		Array<Node> childrenNodes = new Array<>();
		Table children;

		public Node (Actor actor, Task task) {
			this.actor = actor;
			this.task = task;
			actor.setColor(GraphView.getColor(task.getStatus()));
			Table top = new Table();
			add(top).padBottom(25).row();
			top.add(actor);
			children = new Table();
			add(children);
		}

		public void addNode (Node node) {
			childrenNodes.add(node);
			children.add(node).pad(5).expand().top();
		}

		public Node findNode (Task task) {
			if (this.task == task) return this;
			for (Node node : childrenNodes) {
				Node found = node.findNode(task);
				if (found != null) return found;
			}
			return null;
		}
	}

	@Override public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		if (root != null)
			drawConnections(batch, root);
	}

	Vector2 start = new Vector2();
	Vector2 end = new Vector2();
	Vector2 tmp = new Vector2();
	private void drawConnections (Batch batch, Node root) {
		Actor rootActor = root.actor;
		start.set(rootActor.getX() + rootActor.getWidth() / 2, rootActor.getY());
		rootActor.localToAscendantCoordinates(this, start);
		float sx = start.x;
		float sy = start.y;

		for (Node node : root.childrenNodes) {
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
}

