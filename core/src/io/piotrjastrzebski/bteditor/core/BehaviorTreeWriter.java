package io.piotrjastrzebski.bteditor.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.badlogic.gdx.ai.btree.decorator.Include;
import com.badlogic.gdx.ai.btree.utils.DistributionAdapters;
import com.badlogic.gdx.ai.utils.random.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.reflect.Annotation;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.util.Comparator;

/**
 * Utility class for serialization of {@link BehaviorTree}s in a format readable by {@link com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser}
 *
 * Created by PiotrJ on 21/10/15.
 */
public class BehaviorTreeWriter {
	/**
	 * Save the tree in parsable format
	 * @param tree behavior tree to save
	 * @param path external file path to save to, can't be a folder
	 */
	public static void save (BehaviorTree tree, String path) {
		FileHandle savePath = Gdx.files.external(path);
		if (savePath.isDirectory()) {
			Gdx.app.error("BehaviorTreeSaver", "save path cannot be a directory!");
			return;
		}
		savePath.writeString(serialize(tree), false);
	}

	/**
	 * Serialize the tree to parser readable format
	 * @param tree tree to serialize
	 * @return serialized tree
	 */
	public static String serialize(Task tree) {
		Array<Class<? extends Task>> classes = new Array<>();
		findClasses(tree, classes);
		classes.sort(new Comparator<Class<? extends Task>>() {
			@Override public int compare (Class<? extends Task> o1, Class<? extends Task> o2) {
				return o1.getSimpleName().compareTo(o2.getSimpleName());
			}
		});

		StringBuilder sb = new StringBuilder("# Alias definitions\n");

		for (Class<? extends Task> aClass : classes) {
			sb.append("import ").append(toAlias(aClass)).append(":\"").append(aClass.getCanonicalName()).append("\"\n");
		}

		sb.append("\nroot\n");
		writeTask(sb, tree, 1);
		return sb.toString();
	}

	private static void writeTask (StringBuilder sb, Task task, int depth) {
		for (int i = 0; i < depth; i++) {
			sb.append("  ");
		}
		sb.append(toAlias(task.getClass()));
		getTaskAttributes(sb, task);
		sb.append("\n");
		// include may have a whole tree as child, ignore it
		if (task instanceof Include) return;
		for (int i = 0; i < task.getChildCount(); i++) {
			writeTask(sb, task.getChild(i), depth + 1);
		}
	}

	private static void getTaskAttributes (StringBuilder sb, Task task) {
		Class<?> aClass = task.getClass();
		Field[] fields = ClassReflection.getFields(aClass);
		for (Field f : fields) {
			Annotation a = f.getDeclaredAnnotation(TaskAttribute.class);
			if (a == null)
				continue;
			TaskAttribute annotation = a.getAnnotation(TaskAttribute.class);
			sb.append(" ");
			getFieldString(sb, task, annotation, f);
		}
	}

	private static void getFieldString (StringBuilder sb, Task task, TaskAttribute ann, Field field) {
		// prefer name from annotation if there is one
		String name = ann.name();
		if (name == null || name.length() == 0) {
			name = field.getName();
		}
		sb.append(name);
		Object o;
		try {
			field.setAccessible(true);
			o = field.get(task);
		} catch (ReflectionException e) {
			Gdx.app.error("", "Failed to get field", e);
			return;
		}
		if (field.getType().isEnum() || field.getType() == String.class) {
			sb.append(":\"").append(o).append("\"");
		} else if (Distribution.class.isAssignableFrom(field.getType())) {
			sb.append(":\"").append(toParseableString((Distribution)o)).append("\"");
		} else {
			sb.append(":").append(o);
		}
	}

	private static DistributionAdapters adapters;
	/**
	 * Attempts to create a parseable string for given distribution
	 * @param distribution distribution to create parsable string for
	 * @return string that can be parsed by distribution classes
	 */
	public static String toParseableString (Distribution distribution) {
		if (distribution == null)
			throw new IllegalArgumentException("Distribution cannot be null");
		if (adapters == null)
			adapters = new DistributionAdapters();
		return adapters.toString(distribution);
	}

	private static void findClasses (Task task, Array<Class<? extends Task>> classes) {
		Class<? extends Task> aClass = task.getClass();
		String cName = aClass.getCanonicalName();
		// ignore task classes from gdx-ai, as they are already accessible by the parser
		if (!cName.startsWith("com.badlogic.gdx.ai.btree.") && !classes.contains(aClass, true)) {
			classes.add(aClass);
		}
		for (int i = 0; i < task.getChildCount(); i++) {
			findClasses(task.getChild(i), classes);
		}
	}

	/**
	 * Create a valid alias name
	 * @param aClass class of task
	 * @return valid alias for the class
	 */
	public static String toAlias (Class<? extends Task> aClass) {
		if (aClass == null) throw new IllegalArgumentException("Class cannot be null");
		String name = aClass.getSimpleName();
		return Character.toLowerCase(name.charAt(0)) + (name.length() > 1 ? name.substring(1) : "");
	}
}
