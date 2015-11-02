package io.piotrjastrzebski.bteditor.core.view;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.badlogic.gdx.ai.btree.decorator.Include;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.reflect.Annotation;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import io.piotrjastrzebski.bteditor.core.model.ModelTask;

/**
 * View for attribute editing on tasks
 * Created by PiotrJ on 21/10/15.
 */
public class ViewTaskAttributeEdit extends Table {
	private Skin skin;
	private Label top;
	private Label name;

	public ViewTaskAttributeEdit (Skin skin) {
		super();
		this.skin = skin;
		add(top = new Label("Edit task", skin)).row();
		add(name = new Label("<?>", skin));
		row();
	}

	public void startEdit (ModelTask task) {
		stopEdit();
		name.setText(task.getClass().getSimpleName());
		addTaskAttributes(task.getTask());
	}

	private void addTaskAttributes (Task task) {
		Class<?> aClass = task.getClass();
		Field[] fields = ClassReflection.getFields(aClass);
		int added = 0;
		for (Field f : fields) {
			Annotation a = f.getDeclaredAnnotation(TaskAttribute.class);
			if (a == null)
				continue;
			TaskAttribute annotation = a.getAnnotation(TaskAttribute.class);
			addField(task, annotation, f);
			added++;
		}
		if (added == 0) {
			add(new Label("No TaskAttributes", skin));
		}
	}

	private void addField (Task task, TaskAttribute ann, Field field) {
		// prefer name from annotation if there is one
		String name = ann.name();
		if (name == null || name.length() == 0) {
			name = field.getName();
		}
		Table cont = new Table();
		cont.add(new Label(name, skin)).row();
		// include is magic, need magic handling
		if (task instanceof Include && name.equals("subtree")) {
			try {
				cont.add(AttrFieldEdit.createPathEditField(task, field, ann.required(), skin));
			} catch (ReflectionException e) {
				e.printStackTrace();
				cont.add(new Label("<Failed>", skin));
			}
		} else {
			try {
				cont.add(AttrFieldEdit.createEditField(task, field, ann.required(), skin));
			} catch (ReflectionException e) {
				e.printStackTrace();
				cont.add(new Label("<Failed>", skin));
			}
		}
		add(cont).row();
	}

	public void stopEdit () {
		clear();
		add(top).row();
		add(name).row();
		name.setText("<?>");
	}
}
