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
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
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

	protected static Actor createEditField (final Object object, final Field field, boolean required, Skin skin) throws ReflectionException {
		Class fType = field.getType();
		if (fType == float.class) {
			return AttrFieldEdit.floatEditField(object, field, required, skin);
		} else if (fType == double.class) {
			return AttrFieldEdit.doubleEditField(object, field, required, skin);
		} else if (fType == int.class) {
			return AttrFieldEdit.integerEditField(object, field, required, skin);
		} else if (fType == long.class) {
			return AttrFieldEdit.longEditField(object, field, required, skin);
		} else if (fType == String.class) {
			return AttrFieldEdit.stringEditField(object, field, required, skin);
		} else if (fType == boolean.class) {
			return AttrFieldEdit.booleanEditField(object, field, required, skin);
		} else if (fType.isEnum()) {
			return AttrFieldEdit.enumEditField(object, field, required, skin);
		} if (Distribution.class.isAssignableFrom(fType)) {
			return AttrFieldEdit.distEditField(object, field, required, skin);
		} else {
			Gdx.app.error(TAG, "Not supported field type " + fType + " in " + object);
			return null;
		}
	}

	protected static Actor integerEditField (final Object object, final Field field, final boolean required, Skin skin) throws ReflectionException {
		return valueEditField(skin, new IntField() {
			@Override public int getInt () {
				try {
					return (int)field.get(object);
				} catch (ReflectionException e) {
					e.printStackTrace();
				}
				return 0;
			}

			@Override public void setInt (int val) {
				try {
					field.set(object, val);
				} catch (ReflectionException e) {
					Gdx.app.error("Float validator", "Failed to set field " + field + " to " + val, e);
				}
			}
		});
	}

	protected static Actor longEditField (final Object object, final Field field, final boolean required, Skin skin) throws ReflectionException {
		return valueEditField(skin, new LongField() {
			@Override public long getLong () {
				try {
					return (long)field.get(object);
				} catch (ReflectionException e) {
					e.printStackTrace();
				}
				return 0;
			}

			@Override public void setLong (long val) {
				try {
					field.set(object, val);
				} catch (ReflectionException e) {
					Gdx.app.error("Float validator", "Failed to set field " + field + " to " + val, e);
				}
			}
		});
	}

	protected static Actor floatEditField (final Object object, final Field field, final boolean required, Skin skin) throws ReflectionException {
		return valueEditField(skin, new FloatField() {
			@Override public float getFloat () {
				try {
					return (float)field.get(object);
				} catch (ReflectionException e) {
					e.printStackTrace();
				}
				return 0;
			}

			@Override public void setFloat (float val) {
				try {
					field.set(object, val);
				} catch (ReflectionException e) {
					Gdx.app.error("Float validator", "Failed to set field " + field + " to " + val, e);
				}
			}
		});
	}

	protected static Actor doubleEditField (final Object object, final Field field, final boolean required, Skin skin) throws ReflectionException {
		return valueEditField(skin, new DoubleField() {
			@Override public double getDouble () {
				try {
					return (double)field.get(object);
				} catch (ReflectionException e) {
					e.printStackTrace();
				}
				return 0;
			}

			@Override public void setDouble (double val) {
				try {
					field.set(object, val);
				} catch (ReflectionException e) {
					Gdx.app.error("Float validator", "Failed to set field " + field + " to " + val, e);
				}
			}
		});
	}

	protected static Actor stringEditField (final Object object, final Field field, final boolean required, Skin skin) throws ReflectionException {
		String value = (String)field.get(object);
		final TextField tf = new TextField(value, skin);
		tf.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				String text = tf.getText();
				if (text.length() == 0 && required) {
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

	protected static Actor enumEditField (final Object object, final Field field, final boolean required, Skin skin) throws ReflectionException {
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

	protected static Actor booleanEditField (final Object object, final Field field, final boolean required, Skin skin) throws ReflectionException {
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
			int val = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private static boolean validateLong(String str) {
		try {
			long val = Long.parseLong(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private static boolean validateFloat(String str) {
		try {
			float val = Float.parseFloat(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private static boolean validateDouble(String str) {
		try {
			double val = Double.parseDouble(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private static void createStuff (String text, final Object object, final Field field, Distribution dist, Table cont, Skin skin,
		Class<? extends DWrapper>[] classes) {
		final Table fields = new Table();
		final SelectBox<DWrapper> sb = new SelectBox<>(skin);
		cont.add(new Label(text, skin)).row();
		cont.add(sb).row();
		cont.add(fields);

		DWrapper actual = null;
		final DWrapper[] wrappers = new DWrapper[classes.length];
		for (int i = 0; i < classes.length; i++) {
			Class<? extends DWrapper> aClass = classes[i];
			try {
				Constructor constructor = ClassReflection.getDeclaredConstructor(aClass);
				constructor.setAccessible(true);
				DWrapper wrapper = (DWrapper)constructor.newInstance();
				wrapper.init(object, field);
				wrappers[i] = wrapper;
				if (wrapper.isWrapperFor(dist)) {
					actual = wrapper;
				}
			} catch (ReflectionException e) {
				e.printStackTrace();
			}
		}

		if (actual == null) {
			Gdx.app.error(text, "Wrapper missing for " + dist);
			return;
		}
		actual.set(dist);
		actual.createEditFields(fields, skin);

		sb.setItems(wrappers);
		sb.setSelected(actual);
		sb.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				DWrapper selected = sb.getSelection().getLastSelected();
				try {
					field.set(object, selected.create());
				} catch (ReflectionException e) {
					Gdx.app.error("Boolean validator", "Failed to set field " + field + " to " + selected, e);
				}
				fields.clear();
				selected.createEditFields(fields, skin);
			}
		});
	}

	protected static Actor distEditField (final Object object, final Field field, boolean required, final Skin skin) throws ReflectionException {
		// how do implement this crap? multiple inputs probably for each value in the thing
		Distribution dist = (Distribution)field.get(object);
		final Table cont = new Table();
		Class type = field.getType();

		// add new edit fields, per type of distribution
		// if field type is one of the abstract classes, we want to be able to pick dist we want
		if (type == IntegerDistribution.class) {
			createStuff("Integer distribution", object, field, dist, cont, skin, new Class[]{CIDWrapper.class, TIDWrapper.class, UIDWrapper.class});
			return cont;
		}

		if (type == LongDistribution.class) {
			createStuff("Long distribution", object, field, dist, cont, skin, new Class[]{CLDWrapper.class, TLDWrapper.class, ULDWrapper.class});
			return cont;
		}
		if (type == FloatDistribution.class) {
			createStuff("Float distribution", object, field, dist, cont, skin, new Class[]{CFDWrapper.class, TFDWrapper.class, UFDWrapper.class, GFDWrapper.class});
			return cont;
		}
		if (type == DoubleDistribution.class) {
			createStuff("Double distribution", object, field, dist, cont, skin, new Class[]{CDDWrapper.class, TDDWrapper.class, UDDWrapper.class, GDDWrapper.class});
			return cont;
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

	private static Actor valueEditField (Skin skin, ValueField valueField) {
		final TextField vtf = new TextField("", skin);
		vtf.setText(valueField.get());
		vtf.setTextFieldFilter(valueField.getFilter());
		vtf.setTextFieldListener(new TextField.TextFieldListener() {
			@Override public void keyTyped (TextField textField, char c) {
				String text = vtf.getText();
				if (valueField.isValid(text)) {
					vtf.setColor(Color.WHITE);
					valueField.set(text);
				} else {
					vtf.setColor(Color.RED);
				}
			}
		});
		addCancelOnESC(vtf);
		return vtf;
	}

	private static abstract class ValueField {
		protected abstract String get();
		protected abstract boolean isValid(String val);
		protected abstract void set(String val);
		protected abstract TextField.TextFieldFilter getFilter ();
	}

	private static abstract class IntField extends ValueField {
		@Override protected boolean isValid (String val) {
			return validateInt(val);
		}

		@Override public TextField.TextFieldFilter getFilter () {
			return digitFieldFilter;
		}

		@Override final protected String get () {
			return Integer.toString(getInt());
		}

		@Override final protected void set (String val) {
			setInt(Integer.parseInt(val));
		}

		public abstract int getInt ();
		public abstract void setInt (int val);
	}

	private static abstract class LongField extends ValueField {
		@Override protected boolean isValid (String val) {
			return validateLong(val);
		}

		@Override public TextField.TextFieldFilter getFilter () {
			return digitFieldFilter;
		}

		@Override final protected String get () {
			return Long.toString(getLong());
		}

		@Override final protected void set (String val) {
			setLong(Long.parseLong(val));
		}

		public abstract long getLong ();
		public abstract void setLong (long val);
	}

	private static abstract class FloatField extends ValueField {
		@Override protected boolean isValid (String val) {
			return validateFloat(val);
		}

		@Override final protected String get () {
			return Float.toString(getFloat());
		}

		@Override final protected void set (String val) {
			setFloat(Float.parseFloat(val));
		}

		@Override public TextField.TextFieldFilter getFilter () {
			return digitPeriodFieldFilter;
		}

		public abstract float getFloat ();
		public abstract void setFloat (float val);
	}

	private static abstract class DoubleField extends ValueField {
		@Override protected boolean isValid (String val) {
			return validateDouble(val);
		}

		@Override public TextField.TextFieldFilter getFilter () {
			return digitPeriodFieldFilter;
		}

		@Override final protected String get () {
			return Double.toString(getDouble());
		}

		@Override final protected void set (String val) {
			setDouble(Double.parseDouble(val));
		}

		public abstract double getDouble ();
		public abstract void setDouble (double val);
	}

	protected static abstract class DWrapper {
		protected Object owner;
		protected Field field;

		protected DWrapper () {}

		protected final void updateOwner() {
			try {
				field.set(owner, create());
			} catch (ReflectionException e) {
				e.printStackTrace();
			}
		}

		public abstract Distribution create();

		public abstract void set (Distribution dist);

		public abstract void createEditFields (Table fields, Skin skin);

		public abstract boolean isWrapperFor (Distribution distribution);

		public DWrapper init (Object owner, Field field) {
			this.owner = owner;
			this.field = field;
			return this;
		}
	}

	protected static class CIDWrapper extends DWrapper {
		protected int value;

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("value", skin)).padRight(10);
			fields.add(valueEditField(skin, new IntField(){
				@Override public int getInt () {
					return value;
				}

				@Override public void setInt (int val) {
					value = val;
					updateOwner();
				}
			}));
		}

		@Override public boolean isWrapperFor (Distribution distribution) {
			return distribution instanceof ConstantIntegerDistribution;
		}

		public IntegerDistribution create() {
			return new ConstantIntegerDistribution(value);
		}

		@Override public void set (Distribution dist) {
			if (dist instanceof ConstantIntegerDistribution) {
				ConstantIntegerDistribution id = (ConstantIntegerDistribution)dist;
				value = id.getValue();
			}
		}

		@Override public String toString () {
			return "Constant";
		}
	}

	protected static class TIDWrapper extends DWrapper {
		protected int low;
		protected int high;
		protected float mode;

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("low", skin)).padRight(10);
			fields.add(valueEditField(skin, new IntField(){
				@Override public int getInt () {
					return low;
				}

				@Override public void setInt (int val) {
					low = val;
					updateOwner();
				}
			})).row();

			fields.add(new Label("high", skin)).padRight(10);
			fields.add(valueEditField(skin, new IntField(){
				@Override public int getInt () {
					return high;
				}

				@Override public void setInt (int val) {
					high = val;
					updateOwner();
				}
			})).row();;

			fields.add(new Label("mode", skin)).padRight(10);
			fields.add(valueEditField(skin, new FloatField(){
				@Override public float getFloat () {
					return mode;
				}

				@Override public void setFloat (float val) {
					mode = val;
					updateOwner();
				}
			}));
		}

		public IntegerDistribution create() {
			return new TriangularIntegerDistribution(low, high, mode);
		}

		@Override public boolean isWrapperFor (Distribution distribution) {
			return distribution instanceof TriangularIntegerDistribution;
		}

		@Override public void set (Distribution dist) {
			if (dist instanceof TriangularIntegerDistribution) {
				TriangularIntegerDistribution id = (TriangularIntegerDistribution)dist;
				low = id.getLow();
				high = id.getHigh();
				mode = id.getMode();
			}
		}

		@Override public String toString () {
			return "Triangular";
		}
	}

	protected static class UIDWrapper extends DWrapper {
		protected int low;
		protected int high;

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("low", skin)).padRight(10);
			fields.add(valueEditField(skin, new IntField() {
				@Override public int getInt () {
					return low;
				}

				@Override public void setInt (int val) {
					low = val;
					updateOwner();
				}
			})).row();;

			fields.add(new Label("high", skin)).padRight(10);
			fields.add(valueEditField(skin, new IntField() {
				@Override public int getInt () {
					return high;
				}

				@Override public void setInt (int val) {
					high = val;
					updateOwner();
				}
			}));
		}

		public IntegerDistribution create() {
			return new UniformIntegerDistribution(low, high);
		}

		@Override public boolean isWrapperFor (Distribution distribution) {
			return distribution instanceof UniformIntegerDistribution;
		}

		@Override public void set (Distribution dist) {
			if (dist instanceof UniformIntegerDistribution) {
				UniformIntegerDistribution id = (UniformIntegerDistribution)dist;
				low = id.getLow();
				high = id.getHigh();
			}
		}

		@Override public String toString () {
			return "Uniform";
		}
	}

	protected static class CLDWrapper extends DWrapper {
		protected long value;

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("value", skin)).padRight(10);
			fields.add(valueEditField(skin, new LongField(){
				@Override public long getLong () {
					return value;
				}

				@Override public void setLong (long val) {
					value = val;
					updateOwner();
				}
			}));
		}

		public Distribution create() {
			return new ConstantLongDistribution(value);
		}

		@Override public boolean isWrapperFor (Distribution distribution) {
			return distribution instanceof ConstantLongDistribution;
		}

		@Override public void set (Distribution dist) {
			if (dist instanceof ConstantLongDistribution) {
				ConstantLongDistribution ld = (ConstantLongDistribution)dist;
				value = ld.getValue();
			}
		}

		@Override public String toString () {
			return "Constant";
		}
	}

	protected static class TLDWrapper extends DWrapper {
		protected long low;
		protected long high;
		protected double mode;

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("low", skin)).padRight(10);
			fields.add(valueEditField(skin, new LongField(){
				@Override public long getLong () {
					return low;
				}

				@Override public void setLong (long val) {
					low = val;
					updateOwner();
				}
			})).row();

			fields.add(new Label("high", skin)).padRight(10);
			fields.add(valueEditField(skin, new LongField(){
				@Override public long getLong () {
					return high;
				}

				@Override public void setLong (long val) {
					high = val;
					updateOwner();
				}
			})).row();

			fields.add(new Label("mode", skin)).padRight(10);
			fields.add(valueEditField(skin, new DoubleField(){
				@Override public double getDouble () {
					return mode;
				}

				@Override public void setDouble (double val) {
					mode = val;
					updateOwner();
				}
			}));
		}

		@Override public Distribution create() {
			return new TriangularLongDistribution(low, high, mode);
		}

		@Override public boolean isWrapperFor (Distribution distribution) {
			return distribution instanceof TriangularLongDistribution;
		}

		@Override public void set (Distribution dist) {
			if (dist instanceof TriangularLongDistribution) {
				TriangularLongDistribution ld = (TriangularLongDistribution)dist;
				low = ld.getLow();
				high = ld.getHigh();
				mode = ld.getMode();
			}
		}

		@Override public String toString () {
			return "Triangular";
		}
	}

	protected static class ULDWrapper extends DWrapper {
		protected long low;
		protected long high;

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("low", skin)).padRight(10);
			fields.add(valueEditField(skin, new LongField(){
				@Override public long getLong () {
					return low;
				}

				@Override public void setLong (long val) {
					low = val;
					updateOwner();
				}
			})).row();

			fields.add(new Label("high", skin)).padRight(10);
			fields.add(valueEditField(skin, new LongField(){
				@Override public long getLong () {
					return high;
				}

				@Override public void setLong (long val) {
					high = val;
					updateOwner();
				}
			})).row();
		}

		@Override public Distribution create() {
			return new UniformLongDistribution(low, high);
		}

		@Override public boolean isWrapperFor (Distribution distribution) {
			return distribution instanceof UniformLongDistribution;
		}

		@Override public void set (Distribution dist) {
			if (dist instanceof UniformLongDistribution) {
				UniformLongDistribution ld = (UniformLongDistribution)dist;
				low = ld.getLow();
				high = ld.getHigh();
			}
		}

		@Override public String toString () {
			return "Uniform";
		}
	}

	protected static class CFDWrapper extends DWrapper {
		protected float value;

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("value", skin)).padRight(10);
			fields.add(valueEditField(skin, new FloatField(){
				@Override public float getFloat () {
					return value;
				}

				@Override public void setFloat (float val) {
					value = val;
					updateOwner();
				}
			}));
		}

		public Distribution create() {
			return new ConstantFloatDistribution(value);
		}

		@Override public boolean isWrapperFor (Distribution distribution) {
			return distribution instanceof ConstantFloatDistribution;
		}

		@Override public void set (Distribution dist) {
			if (dist instanceof ConstantFloatDistribution) {
				ConstantFloatDistribution fd = (ConstantFloatDistribution)dist;
				value = fd.getValue();
			}
		}

		@Override public String toString () {
			return "Constant";
		}
	}

	protected static class TFDWrapper extends DWrapper {
		protected float low;
		protected float high;
		protected float mode;

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("low", skin)).padRight(10);
			fields.add(valueEditField(skin, new FloatField() {
				@Override public float getFloat () {
					return low;
				}

				@Override public void setFloat (float val) {
					low = val;
					updateOwner();
				}
			})).row();

			fields.add(new Label("high", skin)).padRight(10);
			fields.add(valueEditField(skin, new FloatField() {
				@Override public float getFloat () {
					return high;
				}

				@Override public void setFloat (float val) {
					high = val;
					updateOwner();
				}
			})).row();

			fields.add(new Label("mode", skin)).padRight(10);
			fields.add(valueEditField(skin, new FloatField(){
				@Override public float getFloat () {
					return mode;
				}

				@Override public void setFloat (float val) {
					mode = val;
					updateOwner();
				}
			}));
		}

		@Override public Distribution create() {
			return new TriangularFloatDistribution(low, high, mode);
		}

		@Override public boolean isWrapperFor (Distribution distribution) {
			return distribution instanceof TriangularFloatDistribution;
		}

		@Override public void set (Distribution dist) {
			if (dist instanceof TriangularFloatDistribution) {
				TriangularFloatDistribution fd = (TriangularFloatDistribution)dist;
				low = fd.getLow();
				high = fd.getHigh();
				mode = fd.getMode();
			}
		}

		@Override public String toString () {
			return "Triangular";
		}
	}

	protected static class UFDWrapper extends DWrapper {
		protected float low;
		protected float high;

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("low", skin)).padRight(10);
			fields.add(valueEditField(skin, new FloatField(){
				@Override public float getFloat () {
					return low;
				}

				@Override public void setFloat (float val) {
					low = val;
					updateOwner();
				}
			})).row();

			fields.add(new Label("high", skin)).padRight(10);
			fields.add(valueEditField(skin, new FloatField(){
				@Override public float getFloat () {
					return high;
				}

				@Override public void setFloat (float val) {
					high = val;
					updateOwner();
				}
			})).row();
		}

		@Override public Distribution create() {
			return new UniformFloatDistribution(low, high);
		}

		@Override public boolean isWrapperFor (Distribution distribution) {
			return distribution instanceof UniformFloatDistribution;
		}

		@Override public void set (Distribution dist) {
			if (dist instanceof UniformFloatDistribution) {
				UniformFloatDistribution fd = (UniformFloatDistribution)dist;
				low = fd.getLow();
				high = fd.getHigh();
			}
		}

		@Override public String toString () {
			return "Uniform";
		}
	}

	protected static class GFDWrapper extends DWrapper {
		protected float mean;
		protected float std;

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("mean", skin)).padRight(10);
			fields.add(valueEditField(skin, new FloatField(){
				@Override public float getFloat () {
					return mean;
				}

				@Override public void setFloat (float val) {
					mean = val;
					updateOwner();
				}
			})).row();

			fields.add(new Label("STD", skin)).padRight(10);
			fields.add(valueEditField(skin, new FloatField(){
				@Override public float getFloat () {
					return std;
				}

				@Override public void setFloat (float val) {
					std = val;
					updateOwner();
				}
			}));
		}

		@Override public Distribution create() {
			return new GaussianFloatDistribution(mean, std);
		}

		@Override public boolean isWrapperFor (Distribution distribution) {
			return distribution instanceof GaussianFloatDistribution;
		}

		@Override public void set (Distribution dist) {
			if (dist instanceof GaussianFloatDistribution) {
				GaussianFloatDistribution fd = (GaussianFloatDistribution)dist;
				mean = fd.getMean();
				std = fd.getStandardDeviation();
			}
		}

		@Override public String toString () {
			return "Gaussian";
		}
	}

	protected static class CDDWrapper extends DWrapper {
		protected double value;

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("value", skin)).padRight(10);
			fields.add(valueEditField(skin, new DoubleField(){
				@Override public double getDouble () {
					return value;
				}

				@Override public void setDouble (double val) {
					value = val;
					updateOwner();
				}
			}));
		}

		public Distribution create() {
			return new ConstantDoubleDistribution(value);
		}

		@Override public boolean isWrapperFor (Distribution distribution) {
			return distribution instanceof ConstantDoubleDistribution;
		}

		@Override public void set (Distribution dist) {
			if (dist instanceof ConstantDoubleDistribution) {
				ConstantDoubleDistribution fd = (ConstantDoubleDistribution)dist;
				value = fd.getValue();
			}
		}

		@Override public String toString () {
			return "Constant";
		}
	}

	protected static class TDDWrapper extends DWrapper {
		protected double low;
		protected double high;
		protected double mode;

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("low", skin)).padRight(10);
			fields.add(valueEditField(skin, new DoubleField(){
				@Override public double getDouble () {
					return low;
				}

				@Override public void setDouble (double val) {
					low = val;
					updateOwner();
				}
			})).row();

			fields.add(new Label("high", skin)).padRight(10);
			fields.add(valueEditField(skin, new DoubleField(){
				@Override public double getDouble () {
					return high;
				}

				@Override public void setDouble (double val) {
					high = val;
					updateOwner();
				}
			})).row();

			fields.add(new Label("mode", skin)).padRight(10);
			fields.add(valueEditField(skin, new DoubleField(){
				@Override public double getDouble () {
					return mode;
				}

				@Override public void setDouble (double val) {
					mode = val;
					updateOwner();
				}
			}));
		}

		@Override public Distribution create() {
			return new TriangularDoubleDistribution(low, high, mode);
		}

		@Override public boolean isWrapperFor (Distribution distribution) {
			return distribution instanceof TriangularDoubleDistribution;
		}

		@Override public void set (Distribution dist) {
			if (dist instanceof TriangularDoubleDistribution) {
				TriangularDoubleDistribution dd = (TriangularDoubleDistribution)dist;
				low = dd.getLow();
				high = dd.getHigh();
				mode = dd.getMode();
			}
		}

		@Override public String toString () {
			return "Triangular";
		}
	}

	protected static class UDDWrapper extends DWrapper {
		protected double low;
		protected double high;

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("low", skin)).padRight(10);
			fields.add(valueEditField(skin, new DoubleField(){
				@Override public double getDouble () {
					return low;
				}

				@Override public void setDouble (double val) {
					low = val;
					updateOwner();
				}
			})).row();

			fields.add(new Label("high", skin)).padRight(10);
			fields.add(valueEditField(skin, new DoubleField(){
				@Override public double getDouble () {
					return high;
				}

				@Override public void setDouble (double val) {
					high = val;
					updateOwner();
				}
			}));
		}

		@Override public Distribution create() {
			return new UniformDoubleDistribution(low, high);
		}

		@Override public boolean isWrapperFor (Distribution distribution) {
			return distribution instanceof UniformDoubleDistribution;
		}

		@Override public void set (Distribution dist) {
			if (dist instanceof UniformDoubleDistribution) {
				UniformDoubleDistribution dd = (UniformDoubleDistribution)dist;
				low = dd.getLow();
				high = dd.getHigh();
			}
		}

		@Override public String toString () {
			return "Uniform";
		}
	}

	protected static class GDDWrapper extends DWrapper {
		protected double mean;
		protected double std;

		@Override public void createEditFields (Table fields, Skin skin) {
			fields.add(new Label("mean", skin)).padRight(10);
			fields.add(valueEditField(skin, new DoubleField(){
				@Override public double getDouble () {
					return mean;
				}

				@Override public void setDouble (double val) {
					mean = val;
					updateOwner();
				}
			})).row();

			fields.add(new Label("STD", skin)).padRight(10);
			fields.add(valueEditField(skin, new DoubleField(){
				@Override public double getDouble () {
					return std;
				}

				@Override public void setDouble (double val) {
					std = val;
					updateOwner();
				}
			}));
		}

		@Override public Distribution create() {
			return new GaussianDoubleDistribution(mean, std);
		}

		@Override public boolean isWrapperFor (Distribution distribution) {
			return distribution instanceof GaussianDoubleDistribution;
		}

		@Override public void set (Distribution dist) {
			if (dist instanceof GaussianDoubleDistribution) {
				GaussianDoubleDistribution dd = (GaussianDoubleDistribution)dist;
				mean = dd.getMean();
				std = dd.getStandardDeviation();
			}
		}

		@Override public String toString () {
			return "Gaussian";
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
