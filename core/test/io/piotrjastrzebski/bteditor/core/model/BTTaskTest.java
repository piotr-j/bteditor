package io.piotrjastrzebski.bteditor.core.model;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.decorator.AlwaysFail;
import io.piotrjastrzebski.bteditor.core.dog.BarkTask;
import io.piotrjastrzebski.bteditor.core.dog.CareTask;
import io.piotrjastrzebski.bteditor.core.dog.Dog;
import io.piotrjastrzebski.bteditor.core.dog.WalkTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Stuff to test
 * add/insert/remove children
 * execute pending
 * validate
 * listeners
 * init
 * reset
 * reset on init
 * <p>
 * Created by PiotrJ on 15/10/15.
 */
public class BTTaskTest {
	Selector<Dog> selector;
	BarkTask bark;
	WalkTask walk;
	CareTask care;
	AlwaysFail<Dog> alwaysFail;

	BTModel<Dog> model;
	BTTask<Dog> root;

	@Before public void setUp () throws Exception {
		selector = new Selector<>();
		alwaysFail = new AlwaysFail<>();
		bark = new BarkTask();
		walk = new WalkTask();
		care = new CareTask();

		model = new BTModel<>();
		root = new BTTask<>(model);
	}

	@After public void tearDown () throws Exception {

	}

	@Test(expected = IllegalArgumentException.class) public void initNullTask () {
		root.init(null);
		fail();
	}

	@Test public void fresh () {
		assertNull(root.getTask());
		assertNull(root.getType());
		assertEquals(0, root.getChildCount());
		assertFalse(root.isValid());
	}

	@Test public void initSimpleValid () {
		root.init(care);
		assertEquals(care, root.getTask());
		assertEquals(TaskType.LEAF, root.getType());
		assertEquals(0, root.getChildCount());
		assertTrue(root.isValid());
		assertFalse(model.isDirty());
	}

	@Test public void initSimpleInvalidDecorator () {
		root.init(alwaysFail);
		assertEquals(alwaysFail, root.getTask());
		assertEquals(TaskType.DECORATOR, root.getType());
		assertEquals(0, root.getChildCount());
		assertFalse(root.isValid());
		assertFalse(model.isDirty());
	}

	@Test public void initSimpleInvalidBranch () {
		root.init(selector);
		assertEquals(selector, root.getTask());
		assertEquals(TaskType.BRANCH, root.getType());
		assertEquals(0, root.getChildCount());
		assertFalse(root.isValid());
		assertFalse(model.isDirty());
	}

	@Test public void initValid () {
		selector.addChild(care);
		selector.addChild(alwaysFail);
		alwaysFail.addChild(bark);

		root.init(selector);
		assertEquals(selector, root.getTask());
		assertEquals(TaskType.BRANCH, root.getType());
		assertEquals(2, root.getChildCount());
		assertTrue(root.isValid());
		assertTrue(model.isDirty());

		BTTask<Dog> careChild = root.getChild(0);
		BTTask<Dog> failChild = root.getChild(1);

		assertEquals(care, careChild.getTask());
		assertEquals(TaskType.LEAF, careChild.getType());
		assertEquals(0, careChild.getChildCount());
		assertTrue(careChild.isValid());

		assertEquals(alwaysFail, failChild.getTask());
		assertEquals(TaskType.DECORATOR, failChild.getType());
		assertEquals(1, failChild.getChildCount());
		assertTrue(failChild.isValid());

		BTTask<Dog> barkChild = failChild.getChild(0);

		assertEquals(bark, barkChild.getTask());
		assertEquals(TaskType.LEAF, barkChild.getType());
		assertEquals(0, barkChild.getChildCount());
		assertTrue(barkChild.isValid());
	}

	@Test public void initInvalidBranch () {
		selector.addChild(care);
		selector.addChild(alwaysFail);

		root.init(selector);
		assertEquals(selector, root.getTask());
		assertEquals(TaskType.BRANCH, root.getType());
		assertEquals(2, root.getChildCount());
		assertFalse(root.isValid());

		BTTask<Dog> careChild = root.getChild(0);
		BTTask<Dog> failChild = root.getChild(1);

		assertEquals(care, careChild.getTask());
		assertEquals(TaskType.LEAF, careChild.getType());
		assertEquals(0, careChild.getChildCount());
		assertTrue(careChild.isValid());

		assertEquals(alwaysFail, failChild.getTask());
		assertEquals(TaskType.DECORATOR, failChild.getType());
		assertEquals(0, failChild.getChildCount());
		assertFalse(failChild.isValid());
	}

	@Test public void initInvalidDecorator () {
		alwaysFail.addChild(selector);

		root.init(alwaysFail);
		assertEquals(alwaysFail, root.getTask());
		assertEquals(TaskType.DECORATOR, root.getType());
		assertEquals(1, root.getChildCount());
		assertFalse(root.isValid());
		assertTrue(model.isDirty());

		BTTask<Dog> selChild = root.getChild(0);

		assertEquals(selector, selChild.getTask());
		assertEquals(TaskType.BRANCH, selChild.getType());
		assertEquals(0, selChild.getChildCount());
		assertFalse(selChild.isValid());
	}

	@Test public void addValid () {
		selector.addChild(care);
		root.init(selector);
		BTTask<Dog> tBark = model.obtain();
		tBark.init(bark);

		root.addChild(tBark);
		root.validate();
		assertTrue(root.isValid());
		assertTrue(model.isDirty());

		Task<Dog> sel = root.getTask();
		assertEquals(1, sel.getChildCount());
		assertEquals(care, sel.getChild(0));

		model.executePending();

		assertEquals(2, sel.getChildCount());
		assertEquals(bark, sel.getChild(1));

		assertTrue(root.isValid());
		assertFalse(model.isDirty());
	}

	@Test public void addInvalid () {
		root.init(alwaysFail);

		BTTask<Dog> tBark = model.obtain();
		tBark.init(bark);

		root.addChild(tBark);
		root.validate();
		assertTrue(root.isValid());
		assertTrue(model.isDirty());

		Task<Dog> fail = root.getTask();
		assertEquals(0, fail.getChildCount());

		model.executePending();

		assertEquals(1, fail.getChildCount());
		assertEquals(bark, fail.getChild(0));

		assertTrue(root.isValid());
		assertFalse(model.isDirty());
	}

	@Test public void addValidComplex () {
		selector.addChild(care);
		root.init(selector);

		BTTask<Dog> tFail = model.obtain();
		tFail.init(alwaysFail);

		BTTask<Dog> tBark = model.obtain();
		tBark.init(bark);
		tFail.addChild(tBark);

		root.addChild(tFail);
		root.validate();

		assertTrue(root.isValid());
		assertTrue(model.isDirty());

		Task<Dog> rt = root.getTask();
		assertEquals(1, rt.getChildCount());
		assertEquals(care, rt.getChild(0));

		model.executePending();

		assertEquals(2, rt.getChildCount());
		assertEquals(alwaysFail, rt.getChild(1));

		assertTrue(root.isValid());
		assertFalse(model.isDirty());
	}

	@Test public void insertStart () {
		root.init(selector);
		root.addChild(care);
		root.addChild(walk);
		model.executePending();

		root.insertChild(0, bark);
		model.executePending();

		assertTrue(root.isValid());
		assertEquals(3, root.getChildCount());
		assertEquals(bark, root.getChild(0).getTask());
	}

	@Test public void insertMid () {
		root.init(selector);
		root.addChild(care);
		root.addChild(walk);
		model.executePending();

		root.insertChild(1, bark);
		model.executePending();

		assertTrue(root.isValid());
		assertEquals(3, root.getChildCount());
		assertEquals(bark, root.getChild(1).getTask());
	}

	@Test public void insertEnd () {
		root.init(selector);
		root.addChild(care);
		root.addChild(walk);
		model.executePending();

		root.insertChild(2, bark);
		model.executePending();

		assertTrue(root.isValid());
		assertEquals(3, root.getChildCount());
		assertEquals(bark, root.getChild(2).getTask());
	}

	@Test public void remove () {
		selector.addChild(bark);
		root.init(selector);
		model.executePending();

		assertTrue(root.isValid());
		assertFalse(model.isDirty());

		BTTask<Dog> tSel = root.getChild(0);
		root.removeChild(tSel);

		assertFalse(root.isValid());
		assertEquals(0, root.getChildCount());
		// root is not valid, does nothing
		model.executePending();
		assertFalse(root.isValid());
		root.addChild(care);
		model.executePending();
		assertEquals(1, root.getChildCount());
		assertTrue(root.isValid());
	}

	@Test public void removeFromChild () {
		selector.addChild(bark);
		selector.addChild(care);
		alwaysFail.addChild(selector);
		root.init(alwaysFail);
		model.executePending();

		assertTrue(root.isValid());
		assertFalse(model.isDirty());
		assertEquals(1, root.getChildCount());
		BTTask<Dog> tSel = root.getChild(0);
		assertEquals(2, tSel.getChildCount());

		BTTask<Dog> tBark = tSel.getChild(0);
		assertEquals(bark, tBark.getTask());

		tSel.removeChild(tBark);
		root.validate();
		assertTrue(root.isValid());
		assertTrue(model.isDirty());
		model.executePending();
		assertFalse(model.isDirty());

		assertEquals(1, tSel.getChildCount());

		tSel.removeChild(0);
		root.validate();

		assertFalse(root.isValid());
		assertTrue(model.isDirty());

		// cant execute as root is not valid
		model.executePending();
		assertFalse(model.isDirty());

		tSel.addChild(walk);
		model.executePending();
		root.validate();

		assertTrue(root.isValid());
		assertFalse(model.isDirty());
	}

	@Test public void reset () {
		selector.addChild(care);
		selector.addChild(alwaysFail);
		alwaysFail.addChild(bark);
		root.init(selector);
		root.reset();
		assertNull(root.getTask());
		assertNull(root.getType());
		assertEquals(0, root.getChildCount());
		assertFalse(root.isValid());
	}

	@Test public void resetFresh () {
		root.reset();
		assertNull(root.getTask());
		assertNull(root.getType());
		assertEquals(0, root.getChildCount());
		assertFalse(root.isValid());
	}
}
