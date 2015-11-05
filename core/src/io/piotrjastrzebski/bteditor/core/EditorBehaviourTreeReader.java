package io.piotrjastrzebski.bteditor.core;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.utils.ObjectMap;
import io.piotrjastrzebski.bteditor.core.model.ModelTask;

/**
 * TODO add comments to model tasks somehow, and save them when creating serialized tree
 *
 * Created by PiotrJ on 03/11/15.
 */
public class EditorBehaviourTreeReader<E> extends BehaviorTreeParser.DefaultBehaviorTreeReader<E> {

	ObjectMap<Task, String> taskToComment;
	public EditorBehaviourTreeReader () {
		super(true);
		taskToComment = new ObjectMap<>();
	}

	@Override public void parse (char[] data, int offset, int length) {
		taskToComment.clear();
		super.parse(data, offset, length);
	}

	@Override protected void startStatement (int indent, String name) {
		super.startStatement(indent, name);
//		System.out.println(lineNumber + ": S " + name);
	}

	@Override protected void comment (String text) {
		super.comment(text);
//		System.out.println(lineNumber + ": E " + text);
		if (prevTask != null) {
//			System.out.println("T " + prevTask.task.getClass().getSimpleName());
			// remove whitespace on the ends
			taskToComment.put(prevTask.task, text.trim());
		}
	}

	public void addComments(ModelTask modelTask) {
		// TODO make this more clever, so it works on cloned tree. Perhaps record position of the task in the tree?
		String comment = taskToComment.get(modelTask.getTask());
		if (comment != null) {
			modelTask.setComment(comment);
		}
		for (int i = 0; i < modelTask.getChildCount(); i++) {
			addComments(modelTask.getChild(i));
		}
	}
}
