package io.piotrjastrzebski.bteditor.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

/**
 * Created by PiotrJ on 23/10/15.
 */
public class RelativeFileHandleResolver implements FileHandleResolver {
	private String root;

	@Override public FileHandle resolve (String fileName) {
		if (root != null) {

		}
		return Gdx.files.internal(fileName);
	}

	public void setRoot (String root) {
		this.root = root;
	}
}
