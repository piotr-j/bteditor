package io.piotrjastrzebski.bteditor.core.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.utils.random.Distribution;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/**
 * Created by PiotrJ on 06/10/15.
 */
public class AttrFieldEdit {
	private final static String TAG = AttrFieldEdit.class.getSimpleName();

	protected static Actor createEditField (final Object object, final Field field, Skin skin) throws ReflectionException {
		Class fType = field.getType();
		if (fType == float.class) {
			return AttrFieldEdit.floatEditField(object, field, skin);
		} else if (fType == int.class) {
			return AttrFieldEdit.integerEditField(object, field, skin);
		} else if (fType == String.class) {
			return AttrFieldEdit.stringEditField(object, field, skin);
		} else if (fType == boolean.class) {
			return AttrFieldEdit.booleanEditField(object, field, skin);
		} else if (fType.isEnum()) {
			return AttrFieldEdit.enumEditField(object, field, skin);
		} if (Distribution.class.isAssignableFrom(fType)) {
			return AttrFieldEdit.distEditField(object, field, skin);
		} else {
			Gdx.app.error(TAG, "Not supported field type " + fType + " in " + object);
			return null;
		}
	}

	protected static Actor integerEditField (final Object object, final Field field, Skin skin) throws ReflectionException {
		int value = (int)field.get(object);
		final TextField vtf = new TextField("", skin);
		vtf.setText(Integer.toString(value));
//		vtf.setTextFieldListener(new VisTextField.TextFieldListener() {
//			@Override public void keyTyped (VisTextField textField, char c) {
//				vtf.validateInput();
//				if (vtf.isInputValid()) {
//					int value;
//					try {
//						value = Integer.parseInt(vtf.getText());
//					} catch (NumberFormatException e) {
//						return;
//					}
//
//					try {
//						field.set(object, value);
//					} catch (ReflectionException e) {
//						Gdx.app.error("Integer validator", "Failed to set field " + field + " to " + vtf.getText(), e);
//					}
//				}
//			}
//		});
		addCancelOnESC(vtf);
		return vtf;
	}

	protected static Actor floatEditField (final Object object, final Field field, Skin skin) throws ReflectionException {
		float value = (float)field.get(object);
		final TextField vtf = new TextField("", skin);
		vtf.setText(Float.toString(value));
//		vtf.setTextFieldListener(new VisTextField.TextFieldListener() {
//			@Override public void keyTyped (VisTextField textField, char c) {
//				vtf.validateInput();
//				if (vtf.isInputValid()) {
//					float value;
//					try {
//						value = Float.parseFloat(vtf.getText());
//					} catch (NumberFormatException e) {
//						return;
//					}
//					try {
//						field.set(object, value);
//					} catch (ReflectionException e) {
//						Gdx.app.error("Float validator", "Failed to set field " + field + " to " + vtf.getText(), e);
//					}
//				}
//			}
//		});
		addCancelOnESC(vtf);
		return vtf;
	}

	protected static Actor stringEditField (final Object object, final Field field, Skin skin) throws ReflectionException {
		String value = (String)field.get(object);
		final TextField tf = new TextField(value, skin);
		tf.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				try {
					field.set(object, tf.getText());
				} catch (ReflectionException e) {
					Gdx.app.error("String validator", "Failed to set field " + field + " to " + tf.getText(), e);
				}
			}
		});
		addCancelOnESC(tf);
		return tf;
	}

	protected static Actor enumEditField (final Object object, final Field field, Skin skin) throws ReflectionException {
		Object[] values = field.getType().getEnumConstants();
		final SelectBox<Object> sb = new SelectBox<>(skin);
		sb.setItems(values);
		sb.setSelected(field.get(object));
		sb.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				Object selected = sb.getSelection().getLastSelected();
				try {
					field.set(object, selected);
				} catch (ReflectionException e) {
					Gdx.app.error("Enum validator", "Failed to set field " + field + " to " + selected, e);
				}
			}
		});
		return sb;
	}

	protected static Actor booleanEditField (final Object object, final Field field, Skin skin) throws ReflectionException {
		final SelectBox<Object> sb = new SelectBox<>(skin);
		sb.setItems(true, false);
		sb.setSelected(field.get(object));
		sb.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				Object selected = sb.getSelection().getLastSelected();
				try {
					field.set(object, selected);
				} catch (ReflectionException e) {
					Gdx.app.error("Boolean validator", "Failed to set field " + field + " to " + selected, e);
				}
			}
		});
		return sb;
	}

	protected static Actor distEditField (Object object, Field field, Skin skin) {
		return null;
	}

	private static void addCancelOnESC (final Actor actor) {
		actor.addListener(new InputListener() {
			@Override public boolean keyDown (InputEvent event, int keycode) {
				if (keycode == Input.Keys.ESCAPE) {
					actor.getStage().setKeyboardFocus(null);
				}
				return false;
			}
		});
	}
}
