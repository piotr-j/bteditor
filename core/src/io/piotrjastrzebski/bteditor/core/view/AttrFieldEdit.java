package io.piotrjastrzebski.bteditor.core.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.utils.random.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/**
 * Created by PiotrJ on 06/10/15.
 */
public class AttrFieldEdit {
	private final static String TAG = AttrFieldEdit.class.getSimpleName();
	private static TextField.TextFieldFilter digitFieldFilter = new TextField.TextFieldFilter.DigitsOnlyFilter();
	private static TextField.TextFieldFilter digitPeriodFieldFilter = new TextField.TextFieldFilter() {
		@Override public boolean acceptChar (TextField textField, char c) {
			return Character.isDigit(c) || c == '.';
		}
	};

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
		vtf.setTextFieldFilter(digitFieldFilter);
		vtf.setTextFieldListener(new TextField.TextFieldListener() {
			@Override public void keyTyped (TextField textField, char c) {
				String text = vtf.getText();
				if (validateInt(text)) {
					vtf.setColor(Color.WHITE);
					try {
						field.set(object, Integer.valueOf(text));
					} catch (ReflectionException e) {
						Gdx.app.error("Integer validator", "Failed to set field " + field + " to " + vtf.getText(), e);
					}
				} else {
					vtf.setColor(Color.RED);
				}
			}
		});
		addCancelOnESC(vtf);
		return vtf;
	}

	protected static Actor longEditField (final Object object, final Field field, Skin skin) throws ReflectionException {
		int value = (int)field.get(object);
		final TextField vtf = new TextField("", skin);
		vtf.setText(Integer.toString(value));
		vtf.setTextFieldFilter(digitFieldFilter);
		vtf.setTextFieldListener(new TextField.TextFieldListener() {
			@Override public void keyTyped (TextField textField, char c) {
				String text = vtf.getText();
				if (validateLong(text)) {
					vtf.setColor(Color.WHITE);
					try {
						field.set(object, Long.valueOf(text));
					} catch (ReflectionException e) {
						Gdx.app.error("Integer validator", "Failed to set field " + field + " to " + vtf.getText(), e);
					}
				} else {
					vtf.setColor(Color.RED);
				}
			}
		});
		addCancelOnESC(vtf);
		return vtf;
	}

	protected static Actor floatEditField (final Object object, final Field field, Skin skin) throws ReflectionException {
		float value = (float)field.get(object);
		final TextField vtf = new TextField("", skin);
		vtf.setText(Float.toString(value));
		vtf.setTextFieldFilter(digitPeriodFieldFilter);
		vtf.setTextFieldListener(new TextField.TextFieldListener() {
			@Override public void keyTyped (TextField textField, char c) {
				String text = vtf.getText();
				if (validateFloat(text)) {
					vtf.setColor(Color.WHITE);
					try {
						field.set(object, Float.valueOf(text));
					} catch (ReflectionException e) {
						Gdx.app.error("Float validator", "Failed to set field " + field + " to " + vtf.getText(), e);
					}
				} else {
					vtf.setColor(Color.RED);
				}
			}
		});
		addCancelOnESC(vtf);
		return vtf;
	}

	protected static Actor doubleEditField (final Object object, final Field field, Skin skin) throws ReflectionException {
		float value = (float)field.get(object);
		final TextField vtf = new TextField("", skin);
		vtf.setText(Float.toString(value));
		vtf.setTextFieldFilter(digitPeriodFieldFilter);
		vtf.setTextFieldListener(new TextField.TextFieldListener() {
			@Override public void keyTyped (TextField textField, char c) {
				String text = vtf.getText();
				if (validateDouble(text)) {
					vtf.setColor(Color.WHITE);
					try {
						field.set(object, Float.valueOf(text));
					} catch (ReflectionException e) {
						Gdx.app.error("Float validator", "Failed to set field " + field + " to " + vtf.getText(), e);
					}
				} else {
					vtf.setColor(Color.RED);
				}
			}
		});
		addCancelOnESC(vtf);
		return vtf;
	}

	protected static Actor stringEditField (final Object object, final Field field, Skin skin) throws ReflectionException {
		String value = (String)field.get(object);
		final TextField tf = new TextField(value, skin);
		tf.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				String text = tf.getText();
				if (text.length() == 0) {
					tf.setColor(Color.RED);
				} else {
					tf.setColor(Color.WHITE);
					try {
						field.set(object, text);
					} catch (ReflectionException e) {
						Gdx.app.error("String validator", "Failed to set field " + field + " to " + text, e);
					}
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

	private static boolean validateInt(String str) {
		try {
			Integer val = Integer.valueOf(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private static boolean validateLong(String str) {
		try {
			Long val = Long.valueOf(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private static boolean validateFloat(String str) {
		try {
			Float val = Float.valueOf(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private static boolean validateDouble(String str) {
		try {
			Double val = Double.valueOf(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

//	private static void addFields (final Object object, final Field field, IDWrapper sel, Table fields, Skin skin)
//		throws ReflectionException {
//		if (sel.dist instanceof ConstantIntegerDistribution) {
//			fields.add(new Label("iValue", skin));
//
//		}
//		if (sel.dist instanceof TriangularIntegerDistribution) {
//			fields.add(new Label("iLow", skin));
//
//			fields.add(new Label("iHigh", skin));
//
//			fields.add(new Label("fMode", skin));
//
//		}
//		if (sel.dist instanceof UniformIntegerDistribution) {
//			fields.add(new Label("iLow", skin));
//
//			fields.add(new Label("iHigh", skin));
//
//		}
//	}

	protected static Actor distEditField (final Object object, final Field field, final Skin skin) throws ReflectionException {
		// how do implement this crap? multiple inputs probably for each value in the thing
		Distribution dist = (Distribution)field.get(object);
		final Table cont = new Table();
		Class type = field.getType();
		// add new edit fields, per type of distribution
		// if field type is one of the abstract classes, we want to be able to pick dist we want
		if (type == IntegerDistribution.class) {
			final Table fields = new Table();
			final SelectBox<IDWrapper> sb = new SelectBox<>(skin);
			cont.add(new Label("Integer Distribution", skin)).row();
			cont.add(sb).row();
			cont.add(fields);
			final IDWrapper cid = new CIDWrapper(object, field);
			final IDWrapper tid = new TIDWrapper(object, field);
			final IDWrapper uid = new UIDWrapper(object, field);
			IDWrapper actual = null;
			if (dist instanceof ConstantIntegerDistribution) {
				cid.set((IntegerDistribution)dist);
				cid.createEditFields(fields, skin);
				actual = cid;
			}
			if (dist instanceof TriangularIntegerDistribution) {
				tid.set((IntegerDistribution)dist);
				tid.createEditFields(fields, skin);
				actual = tid;
			}
			if (dist instanceof UniformIntegerDistribution) {
				uid.set((IntegerDistribution)dist);
				uid.createEditFields(fields, skin);
				actual = uid;
			}
			sb.setItems(cid, tid, uid);
			sb.setSelected(actual);
//			addFields(object, field, actual, fields, skin);
			sb.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					IDWrapper selected = sb.getSelection().getLastSelected();
					try {
						field.set(object, selected.create());
					} catch (ReflectionException e) {
						Gdx.app.error("Boolean validator", "Failed to set field " + field + " to " + selected, e);
					}
					fields.clear();
					selected.createEditFields(fields, skin);
//					try {
//						addFields(object, field, selected, fields, skin);
//					} catch (ReflectionException e) {
//						e.printStackTrace();
//					}

				}
			});

			return cont;
		}
		if (type == LongDistribution.class) {
			Gdx.app.log("", "f int dist");
//			ConstantLongDistribution;
//			TriangularLongDistribution;
//			UniformLongDistribution;

		}
		if (type == FloatDistribution.class) {
			Gdx.app.log("", "f int dist");
//			ConstantFloatDistribution;
//			GaussianFloatDistribution;
//			TriangularFloatDistribution;
//			UniformFloatDistribution;

		}
		if (type == DoubleDistribution.class) {
			Gdx.app.log("", "f int dist");
//			ConstantDoubleDistribution;
//			GaussianDoubleDistribution;
//			TriangularDoubleDistribution;
//			UniformDoubleDistribution;

		}
		// if not we cant pick the type, just edit existing distribution

		/*
		if (type == ConstantIntegerDistribution.class) {
			Gdx.app.log("", "f const int dist");
		}
		if (type == UniformIntegerDistribution.class) {
			Gdx.app.log("", "f uni int dist");
		}
		if (dist instanceof ConstantIntegerDistribution) {
			ConstantIntegerDistribution cid = (ConstantIntegerDistribution)dist;
//			return "constant," + ((ConstantIntegerDistribution)dist).getValue();
			Gdx.app.log("", "const int dist");

		}
		if (dist instanceof ConstantLongDistribution) {
			ConstantLongDistribution cld = (ConstantLongDistribution)dist;
//			return "constant," + ((ConstantLongDistribution)dist).getValue();

		}
		if (dist instanceof ConstantFloatDistribution) {
			ConstantFloatDistribution cfd = (ConstantFloatDistribution)dist;
//			return "constant," + ((ConstantFloatDistribution)dist).getValue();

		}
		if (dist instanceof ConstantDoubleDistribution) {
			ConstantDoubleDistribution cdd = (ConstantDoubleDistribution)dist;
//			return "constant," + ((ConstantDoubleDistribution)dist).getValue();

		}
		if (dist instanceof GaussianFloatDistribution) {
			GaussianFloatDistribution gfd = (GaussianFloatDistribution)dist;
//			return "gaussian," + gfd.getMean() + "," + gfd.getStandardDeviation();

		}
		if (dist instanceof GaussianDoubleDistribution) {
			GaussianDoubleDistribution gdd = (GaussianDoubleDistribution)dist;
//			return "gaussian," + gdd.getMean() + ","+ gdd.getStandardDeviation();

		}
		if (dist instanceof TriangularIntegerDistribution) {
			TriangularIntegerDistribution tid = (TriangularIntegerDistribution)dist;
//			return "triangular," + tid.getLow() + "," + tid.getHigh() + "," + tid.getMode();

		}
		if (dist instanceof TriangularLongDistribution) {
			TriangularLongDistribution tld = (TriangularLongDistribution)dist;
//			return "triangular," + tld.getLow() + "," + tld.getHigh() + "," + tld.getMode();

		}
		if (dist instanceof TriangularFloatDistribution) {
			TriangularFloatDistribution tfd = (TriangularFloatDistribution)dist;
//			return "triangular," + tfd.getLow() + "," + tfd.getHigh() + "," + tfd.getMode();

		}
		if (dist instanceof TriangularDoubleDistribution) {
			TriangularDoubleDistribution tdd = (TriangularDoubleDistribution)dist;
//			return "triangular," + tdd.getLow() + "," + tdd.getHigh() + "," + tdd.getMode();

		}
		if (dist instanceof UniformIntegerDistribution) {
			UniformIntegerDistribution uid = (UniformIntegerDistribution)dist;
//			return "uniform," + uid.getLow() + "," + uid.getHigh();
			Gdx.app.log("", "uni int dist");

		}
		if (dist instanceof UniformLongDistribution) {
			UniformLongDistribution uld = (UniformLongDistribution)dist;
//			return "uniform," + uld.getLow() + "," + uld.getHigh();

		}
		if (dist instanceof UniformFloatDistribution) {
			UniformFloatDistribution ufd = (UniformFloatDistribution)dist;
//			return "uniform," + ufd.getLow() + "," + ufd.getHigh();

		}
		if (dist instanceof UniformDoubleDistribution) {
			UniformDoubleDistribution udd = (UniformDoubleDistribution)dist;
//			return "uniform," + udd.getLow() + "," + udd.getHigh();

		}
		if (dist instanceof IntegerDistribution) {
			Gdx.app.log("", "int dist");
		}
		if (dist instanceof LongDistribution) {

		}
		if (dist instanceof FloatDistribution) {

		}
		if (dist instanceof DoubleDistribution) {
			Gdx.app.log("", "double dist");
		}
		*/
		return cont;
	}

	private static abstract class IDWrapper {
		protected Object owner;
		protected Field field;
		public IDWrapper (Object owner, Field field) {
			this.owner = owner;
			this.field = field;
		}

		protected final void updateOwner() {
			try {
				field.set(owner, create());
			} catch (ReflectionException e) {
				e.printStackTrace();
			}
		}

		public abstract IntegerDistribution create();

		public abstract void set (IntegerDistribution dist);

		public abstract void createEditFields (Table fields, Skin skin);
	}

	private static class CIDWrapper extends IDWrapper {
		protected int value;

		public CIDWrapper (Object owner, Field field) {
			super(owner, field);
		}

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("value", skin)).padRight(10);
			final TextField vtf = new TextField("", skin);
			vtf.setText(Integer.toString(value));
			vtf.setTextFieldFilter(digitFieldFilter);
			vtf.setTextFieldListener(new TextField.TextFieldListener() {
				@Override public void keyTyped (TextField textField, char c) {
					String text = vtf.getText();
					if (validateInt(text)) {
						vtf.setColor(Color.WHITE);
						value = Integer.valueOf(text);
						updateOwner();
					} else {
						vtf.setColor(Color.RED);
					}
				}
			});
			addCancelOnESC(vtf);
			fields.add(vtf);
		}

		public IntegerDistribution create() {
			return new ConstantIntegerDistribution(value);
		}

		@Override public void set (IntegerDistribution dist) {
			if (dist instanceof ConstantIntegerDistribution) {
				ConstantIntegerDistribution cid = (ConstantIntegerDistribution)dist;
				value = cid.getValue();
			}
		}

		@Override public String toString () {
			return "Constant";
		}
	}

	private static class TIDWrapper extends IDWrapper {
		protected int low;
		protected int high;
		protected float mode;

		public TIDWrapper (Object owner, Field field) {
			super(owner, field);
		}

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("low", skin)).padRight(10);
			final TextField ltf = new TextField("", skin);
			ltf.setText(Integer.toString(low));
			ltf.setTextFieldFilter(digitFieldFilter);
			ltf.setTextFieldListener(new TextField.TextFieldListener() {
				@Override public void keyTyped (TextField textField, char c) {
					String text = ltf.getText();
					if (validateInt(text)) {
						ltf.setColor(Color.WHITE);
						low = Integer.valueOf(text);
						updateOwner();
					} else {
						ltf.setColor(Color.RED);
					}
				}
			});
			addCancelOnESC(ltf);
			fields.add(ltf).row();

			fields.add(new Label("high", skin)).padRight(10);
			final TextField htf = new TextField("", skin);
			htf.setText(Integer.toString(high));
			htf.setTextFieldFilter(digitFieldFilter);
			htf.setTextFieldListener(new TextField.TextFieldListener() {
				@Override public void keyTyped (TextField textField, char c) {
					String text = htf.getText();
					if (validateInt(text)) {
						htf.setColor(Color.WHITE);
						high = Integer.valueOf(text);
						updateOwner();
					} else {
						htf.setColor(Color.RED);
					}
				}
			});
			addCancelOnESC(htf);
			fields.add(htf).row();

			fields.add(new Label("mode", skin)).padRight(10);
			final TextField mtf = new TextField("", skin);
			mtf.setText(Float.toString(mode));
			mtf.setTextFieldFilter(digitPeriodFieldFilter);
			mtf.setTextFieldListener(new TextField.TextFieldListener() {
				@Override public void keyTyped (TextField textField, char c) {
					String text = mtf.getText();
					if (validateFloat(text)) {
						mtf.setColor(Color.WHITE);
						mode = Float.valueOf(text);
						updateOwner();
					} else {
						mtf.setColor(Color.RED);
					}
				}
			});
			addCancelOnESC(mtf);
			fields.add(mtf);
		}

		public IntegerDistribution create() {
			return new TriangularIntegerDistribution(low, high, mode);
		}

		@Override public void set (IntegerDistribution dist) {
			if (dist instanceof TriangularIntegerDistribution) {
				TriangularIntegerDistribution tid = (TriangularIntegerDistribution)dist;
				low = tid.getLow();
				high = tid.getHigh();
				mode = tid.getMode();
			}
		}

		@Override public String toString () {
			return "Triangular";
		}
	}

	private static class UIDWrapper extends IDWrapper {
		protected int low;
		protected int high;

		public UIDWrapper (Object owner, Field field) {
			super(owner, field);
		}

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("low", skin)).padRight(10);
			final TextField ltf = new TextField("", skin);
			ltf.setText(Integer.toString(low));
			ltf.setTextFieldFilter(digitFieldFilter);
			ltf.setTextFieldListener(new TextField.TextFieldListener() {
				@Override public void keyTyped (TextField textField, char c) {
					String text = ltf.getText();
					if (validateInt(text)) {
						ltf.setColor(Color.WHITE);
						low = Integer.valueOf(text);
						updateOwner();
					} else {
						ltf.setColor(Color.RED);
					}
				}
			});
			addCancelOnESC(ltf);
			fields.add(ltf).row();

			fields.add(new Label("high", skin)).padRight(10);
			final TextField htf = new TextField("", skin);
			htf.setText(Integer.toString(high));
			htf.setTextFieldFilter(digitFieldFilter);
			htf.setTextFieldListener(new TextField.TextFieldListener() {
				@Override public void keyTyped (TextField textField, char c) {
					String text = htf.getText();
					if (validateInt(text)) {
						htf.setColor(Color.WHITE);
						high = Integer.valueOf(text);
						updateOwner();
					} else {
						htf.setColor(Color.RED);
					}
				}
			});
			addCancelOnESC(htf);
			fields.add(htf).row();
		}

		public IntegerDistribution create() {
			return new UniformIntegerDistribution(low, high);
		}

		@Override public void set (IntegerDistribution dist) {
			if (dist instanceof UniformIntegerDistribution) {
				UniformIntegerDistribution uid = (UniformIntegerDistribution)dist;
				low = uid.getLow();
				high = uid.getHigh();
			}
		}

		@Override public String toString () {
			return "Uniform";
		}
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
