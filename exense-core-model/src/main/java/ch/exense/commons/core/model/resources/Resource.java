/*******************************************************************************
 * Copyright 2021 exense GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package ch.exense.commons.core.model.resources;

import org.bson.types.ObjectId;

import ch.exense.commons.core.model.accessors.AbstractOrganizableObject;

public class Resource extends AbstractOrganizableObject {

	protected ObjectId currentRevisionId;
	
	protected String resourceType;
	
	protected String resourceName;
	
	protected boolean ephemeral;
	
	public ObjectId getCurrentRevisionId() {
		return currentRevisionId;
	}

	public void setCurrentRevisionId(ObjectId currentRevisionId) {
		this.currentRevisionId = currentRevisionId;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public boolean isEphemeral() {
		return ephemeral;
	}

	public void setEphemeral(boolean ephemeral) {
		this.ephemeral = ephemeral;
	}
}
