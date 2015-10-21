package io.piotrjastrzebski.bteditor.core.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.badlogic.gdx.ai.utils.random.Distribution;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.reflect.Annotation;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/**
 * View for attribute editing on tasks
 * Created by PiotrJ on 21/10/15.
 */
public class AttrEdit extends Table {
	private Skin skin;
	private Label name;

	public AttrEdit (Skin skin) {
		super();
		this.skin = skin;
		name = new Label("<?>", skin);
		add(name);
		row();
	}

	public void startEdit (Task task) {
		stopEdit();
		name.setText(task.getClass().getSimpleName());
		addTaskAttributes(task);
	}

	private void addTaskAttributes (Task task) {
		Class<?> aClass = task.getClass();
		Field[] fields = ClassReflection.getFields(aClass);
		for (Field f : fields) {
			Annotation a = f.getDeclaredAnnotation(TaskAttribute.class);
			if (a == null)
				continue;
			TaskAttribute annotation = a.getAnnotation(TaskAttribute.class);
			addField(task, annotation, f);
		}
	}

	private void addField (Task task, TaskAttribute ann, Field field) {
		// prefer name from annotation if there is one
		String name = ann.name();
		if (name == null || name.length() == 0) {
			name = field.getName();
		}
		Table cont = new Table();
		cont.add(new Label(name, skin)).padRight(5);
		try {
			cont.add(AttrFieldEdit.createEditField(task, field, skin));
		} catch (ReflectionException e) {
			e.printStackTrace();
			cont.add(new Label("<Failed>", skin));
		}
		add(cont).row();
	}

	public void stopEdit () {
		clear();
		add(name).row();
		name.setText("<?>");
	}
}
