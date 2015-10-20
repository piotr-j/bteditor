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

/** @author implicit-invocation
 * @author davebaol */
public class Dog {

	public String name;
	public String brainLog;

	public Dog (String name) {
		this.name = name;
		this.brainLog = name + " brain";
	}

	public void bark () {
		log("Bow wow!!!");
	}

	public void startWalking () {
		log("Dog starts walking");
	}

	public void randomlyWalk () {
		log("Dog walks randomly around!");
	}

	public void stopWalking () {
		log("Dog stops walking");
	}

	public boolean standBesideATree () {
		if (Math.random() < 0.5) {
			log("No tree found :(");
			return false;
		}
		return true;
	}

	public void markATree () {
		log("Dog lifts a leg and pee!");
	}

	private boolean urgent = false;

	public boolean isUrgent () {
		return urgent;
	}

	public void setUrgent (boolean urgent) {
		this.urgent = urgent;
	}

	public void log (String msg) {
		System.out.println(name + " : " + msg);
	}

	public void brainLog (String msg) {
		System.out.println(brainLog + " : " + msg);
	}

}
