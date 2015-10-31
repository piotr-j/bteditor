package io.piotrjastrzebski.bteditor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.*;
import com.badlogic.gdx.ai.btree.decorator.*;
import com.badlogic.gdx.ai.btree.leaf.Failure;
import com.badlogic.gdx.ai.btree.leaf.Success;
import com.badlogic.gdx.ai.btree.leaf.Wait;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.ai.utils.random.UniformIntegerDistribution;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import io.piotrjastrzebski.bteditor.core.BehaviorTreeEditor;
import io.piotrjastrzebski.bteditor.core.IPersist;
import io.piotrjastrzebski.bteditor.core.dog.*;

import java.io.Reader;

/**
 * Created by EvilEntity on 20/10/2015.
 */
public class EditorTest extends ApplicationAdapter implements InputProcessor, IPersist<Dog> {
	private static final String TAG = EditorTest.class.getSimpleName();

	private Skin skin;
	private SpriteBatch batch;
	private Stage stage;
	private ScreenViewport viewport;

	private BehaviorTree<Dog> tree;
	private BehaviorTreeEditor<Dog> editor;
	private FileChooser saveAsFC;
	private FileChooser loadFC;

	public EditorTest () {
	}

	@Override public void create () {
		// need vis for file picker
		VisUI.load();

		skin = VisUI.getSkin();
		skin = new Skin(Gdx.files.internal("uiskin.json"));
		batch = new SpriteBatch();
		viewport = new ScreenViewport(new OrthographicCamera());

		stage = new Stage(viewport, batch);
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));

		Window editorWindow = new Window("Editor", skin);
		stage.addActor(editorWindow);
		editorWindow.setSize(600, 550 );
		editorWindow.setResizable(true);
		editorWindow
			.setPosition(stage.getWidth() / 2 - editorWindow.getWidth() / 2, stage.getHeight() / 2 - editorWindow.getHeight() / 2);

		tree = new BehaviorTree<>(createDogBehavior());
		tree.setObject(new Dog("Dog A"));

		editor = new BehaviorTreeEditor<>(skin, skin.getDrawable("white"));
//		editor.setLogger(new Logger() {
//			@Override public void log (String tag, String msg) {
//				Gdx.app.log(tag, msg);
//			}
//
//			@Override public void error (String tag, String msg) {
//				Gdx.app.error(tag, msg);
//			}
//
//			@Override public void error (String tag, String msg, Exception e) {
//				Gdx.app.error(tag, msg, e);
//			}
//		});
		editorWindow.add(editor).expand().fill();

		editor.initialize(tree, null);
		editor.addTaskClass("branch", Sequence.class);
		editor.addTaskClass("branch", Selector.class);
		editor.addTaskClass("branch", Parallel.class);
		editor.addTaskClass("branch", RandomSelector.class);
		editor.addTaskClass("branch", RandomSequence.class);

		editor.addTaskClass("decorator", AlwaysFail.class);
		editor.addTaskClass("decorator", AlwaysSucceed.class);
		editor.addTaskClass("decorator", Include.class);
		editor.addTaskClass("decorator", Invert.class);
		editor.addTaskClass("decorator", Random.class);
		editor.addTaskClass("decorator", Repeat.class);
		editor.addTaskClass("decorator", SemaphoreGuard.class);
		editor.addTaskClass("decorator", UntilFail.class);
		editor.addTaskClass("decorator", UntilSuccess.class);
		editor.addTaskClass("decorator", Wait.class);
		editor.addTaskClass("decorator", Success.class);
		editor.addTaskClass("decorator", Failure.class);

		editor.addTaskClass("custom", BarkTask.class);
		editor.addTaskClass("custom", CareTask.class);
		editor.addTaskClass("custom", MarkTask.class);
		editor.addTaskClass("custom", RestTask.class);
		editor.addTaskClass("custom", WalkTask.class);

		editor.setPersist(this);

		// TODO proper error handling
		FileChooser.setFavoritesPrefsName("io,piotrjastrzebski.bteditor");

		saveAsFC = new FileChooser(FileChooser.Mode.SAVE);
		saveAsFC.setDirectory(Gdx.files.getLocalStoragePath());
		saveAsFC.setListener(new FileChooserAdapter() {
			@Override public void selected (FileHandle file) {
				Gdx.app.log("", "save " + file.file().getAbsolutePath());
				saveFH = file;
				saveBT(treeToSave, saveFH);
			}
		});

		loadFC = new FileChooser(FileChooser.Mode.OPEN);
		loadFC.setDirectory(Gdx.files.getLocalStoragePath());
		loadFC.setListener(new FileChooserAdapter() {
			@Override public void selected (FileHandle file) {
				Gdx.app.log("", "load " + file.file().getAbsolutePath());
				loadBT(file);
			}
		});
	}

	private void loadBT (FileHandle file) {
		Reader reader = null;
		try {
			reader = file.reader();
			BehaviorTreeParser<Dog> parser = new BehaviorTreeParser<Dog>(BehaviorTreeParser.DEBUG_NONE) {
				protected BehaviorTree<Dog> createBehaviorTree (Task<Dog> root, Dog object) {
					if (debug > BehaviorTreeParser.DEBUG_LOW) printTree(root, 0);
					return new BehaviorTree<>(root, object);
				}
			};
			tree = (BehaviorTree<Dog>)parser.parse(reader, null).cloneTask();
			tree.setObject(new Dog("Dog A"));
			editor.initialize(tree, file.file().getParent());
		} catch (SerializationException e) {
			Gdx.app.error("ET", "Failed to load " + file.file().getAbsolutePath(), e);
		} finally {
			StreamUtils.closeQuietly(reader);
		}
	}

	private void saveBT(String tree, FileHandle file) {
		file.writeString(tree, false);
	}

	private FileHandle saveFH;
	@Override public void onSave (String tree) {
		if (saveFH == null) {
			onSaveAs(tree);
		} else {
			saveBT(tree, saveFH);
		}
	}

	private String treeToSave;
	@Override public void onSaveAs (String tree) {
		treeToSave = tree;
		stage.addActor(saveAsFC.fadeIn());
	}

	@Override public void onLoad () {
		Gdx.app.log("ET", "onLoad " + tree);
		stage.addActor(loadFC.fadeIn());
	}

	@Override public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		GdxAI.getTimepiece().update(Gdx.graphics.getDeltaTime());
	}

	@Override public void resize (int width, int height) {
		viewport.update(width, height, true);
	}

	@Override public void dispose () {
		batch.dispose();
		skin.dispose();
		VisUI.dispose();
	}

	private static Task<Dog> createDogBehavior () {
		/* this is eq tree to one made in code below
		selector
		  parallel
			 care
			 always
				rest
		  sequence
			 bark times:"uniform,1,5"
			 walk
			 bark
			 mark
		 */

		Selector<Dog> selector = new Selector<>();

		Parallel<Dog> parallel = new Parallel<>();
		selector.addChild(parallel);

		CareTask care = new CareTask();
		care.urgentProb = 0.3f;
		parallel.addChild(care);
		parallel.addChild(new AlwaysFail<>(new RestTask()));

		Sequence<Dog> sequence = new Sequence<>();
		selector.addChild(sequence);
		BarkTask barkTask = new BarkTask();
		barkTask.times = new UniformIntegerDistribution(1, 3);
		sequence.addChild(barkTask);
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
		new LwjglApplication(new EditorTest(), config);
	}
}
