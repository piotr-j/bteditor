/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package io.piotrjastrzebski.bteditor.core.dog;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import io.piotrjastrzebski.bteditor.core.TaskComment;

/** @author implicit-invocation
 * @author davebaol */
public class CareTask extends LeafTask<Dog> implements TaskComment {

	@TaskAttribute(required=true)
	public float urgentProb = 0.8f;

	@TaskAttribute(required=true)
	public String name;

	@Override
	public Status execute () {
		if (Math.random() < urgentProb) {
			return Status.SUCCEEDED;
		} else {
			Dog dog = getObject();
			dog.brainLog("GASP - Something urgent :/");
			return Status.FAILED;
		}
	}

	@Override
	protected Task<Dog> copyTo (Task<Dog> task) {
		CareTask care = (CareTask)task;
		care.urgentProb = urgentProb;

		return task;
	}

	@Override public String getComment () {
		return "Need a piss!!! Need a piss!!! Need a piss!!! Need a piss!!! Need a piss!!! Need a piss!!! ";
	}
}
