package io.piotrjastrzebski.bteditor.core;

/**
 * Created by PiotrJ on 16/10/15.
 */
public interface Logger {
	void log (String tag, String msg);

	void error (String tag, String msg);

	void error (String tag, String msg, Exception e);
}
