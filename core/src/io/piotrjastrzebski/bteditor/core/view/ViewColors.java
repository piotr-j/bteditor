package io.piotrjastrzebski.bteditor.core.view;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.graphics.Color;

/**
 * Created by PiotrJ on 31/10/15.
 */
public class ViewColors {
	public final static Color COLOR_SUCCEEDED = new Color(Color.GREEN);
	public final static Color COLOR_RUNNING = new Color(Color.ORANGE);
	public final static Color COLOR_FAILED = new Color(Color.RED);
	public final static Color COLOR_CANCELLED = new Color(Color.YELLOW);
	public final static Color COLOR_FRESH = new Color(Color.GRAY);

	public static Color getColor (Task.Status status) {
		if (status == null)
			return Color.GRAY;
		switch (status) {
		case SUCCEEDED:
			return COLOR_SUCCEEDED;
		case RUNNING:
			return COLOR_RUNNING;
		case FAILED:
			return COLOR_FAILED;
		case CANCELLED:
			return COLOR_CANCELLED;
		case FRESH:
		default:
			return COLOR_FRESH;
		}
	}
}
