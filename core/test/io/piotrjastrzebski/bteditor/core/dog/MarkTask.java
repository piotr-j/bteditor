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
import com.badlogic.gdx.ai.utils.random.ConstantDoubleDistribution;
import com.badlogic.gdx.ai.utils.random.ConstantFloatDistribution;
import com.badlogic.gdx.ai.utils.random.DoubleDistribution;
import com.badlogic.gdx.ai.utils.random.FloatDistribution;

/** @author implicit-invocation
 * @author davebaol */
public class MarkTask extends LeafTask<Dog> {

	@TaskAttribute
	public DoubleDistribution times = ConstantDoubleDistribution.ONE;

	@Override
	public void run () {
		Dog dog = getObject();
		if (dog.standBesideATree()) {
			dog.markATree();
			dog.setUrgent(false);
			success();
		} else if (times.nextDouble() < .5d){
			running();
		} else {
			fail();
		}
	}

	@Override
	protected Task<Dog> copyTo (Task<Dog> task) {
		return task;
	}

}
