package io.piotrjastrzebski.bteditor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.*;
import com.badlogic.gdx.ai.btree.leaf.Failure;
import com.badlogic.gdx.ai.btree.leaf.Success;
import com.badlogic.gdx.ai.btree.leaf.Wait;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import io.piotrjastrzebski.bteditor.core.BehaviourTreeEditor;
import io.piotrjastrzebski.bteditor.core.dog.*;
import org.lwjgl.opengl.Display;

/**
 * Created by EvilEntity on 20/10/2015.
 */
public class EditorTest extends ApplicationAdapter implements InputProcessor {
	private static final String TAG = EditorTest.class.getSimpleName();

	private SpriteBatch batch;
	private Stage stage;
	private ScreenViewport viewport;

	private BehaviorTree<Dog> tree;
	private BehaviourTreeEditor<Dog> editor;

	public EditorTest () {}

	@Override public void create () {
		boolean highRes = Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) > 1980;
		if (highRes || Display.getPixelScaleFactor() > 1.5f) {
			VisUI.load(VisUI.SkinScale.X2);
		} else {
			VisUI.load(VisUI.SkinScale.X1);
		}
		Skin skin = VisUI.getSkin();

		batch = new SpriteBatch();
		viewport = new ScreenViewport(new OrthographicCamera());

		stage = new Stage(viewport, batch);
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));

		Window editorWindow = new Window("Editor", skin);
		stage.addActor(editorWindow);
		editorWindow.setSize(600, 550);
		editorWindow.setResizable(true);
		editorWindow.setPosition(
			stage.getWidth()/2 - editorWindow.getWidth()/2,
			stage.getHeight()/2 - editorWindow.getHeight()/2
		);

		tree = new BehaviorTree<>(createDogBehavior());
		tree.setObject(new Dog("Dog A"));

		editor = new BehaviourTreeEditor<>(skin, skin.getDrawable("white"));
		editorWindow.add(editor).expand().fill();

		editor.initialize(tree);
		editor.addTaskClass(Sequence.class);
		editor.addTaskClass(Selector.class);
		editor.addTaskClass(Parallel.class);
		editor.addTaskClass(AlwaysFail.class);
		editor.addTaskClass(AlwaysSucceed.class);
//		editor.addTaskClass(Include.class);
		editor.addTaskClass(Invert.class);
//		editor.addTaskClass(Random.class);
		editor.addTaskClass(Repeat.class);
		editor.addTaskClass(SemaphoreGuard.class);
		editor.addTaskClass(UntilFail.class);
		editor.addTaskClass(UntilSuccess.class);
		editor.addTaskClass(Wait.class);
		editor.addTaskClass(Success.class);
		editor.addTaskClass(Failure.class);

		editor.addTaskClass(BarkTask.class);
		editor.addTaskClass(CareTask.class);
		editor.addTaskClass(MarkTask.class);
		editor.addTaskClass(RestTask.class);
		editor.addTaskClass(WalkTask.class);
	}

	@Override public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	@Override public void resize (int width, int height) {
		viewport.update(width, height, true);
	}

	@Override public void dispose () {
		batch.dispose();
	}

	private static Task<Dog> createDogBehavior () {
		/* this is eq tree to one made in code below
		selector
		  parallel
			 care
			 always
				rest
		  sequence
			 bark
			 walk
			 bark
			 mark
		 */

		Selector<Dog> selector = new Selector<>();

		Parallel<Dog> parallel = new Parallel<>();
		selector.addChild(parallel);

		CareTask care = new CareTask();
		parallel.addChild(care);
		parallel.addChild(new AlwaysFail<>(new RestTask()));

		Sequence<Dog> sequence = new Sequence<>();
		selector.addChild(sequence);

		sequence.addChild(new BarkTask());
		sequence.addChild(new WalkTask());
		sequence.addChild(new BarkTask());
		sequence.addChild(new MarkTask());

		return selector;
	}

	@Override public boolean keyDown (int keycode) {
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		return false;
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		return false;
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		return false;
	}

	@Override public boolean scrolled (int amount) {
		return false;
	}

	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 800;
		config.height = 600;
		config.useHDPI = true;
		new LwjglApplication(new EditorTest(), config);
	}
}
