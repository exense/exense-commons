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
package ch.exense.commons.core.collections.filesystem;

import ch.exense.commons.core.model.accessors.AbstractIdentifiableObject;
import org.apache.commons.beanutils.PropertyUtils;
import org.bson.types.ObjectId;

import java.lang.reflect.InvocationTargetException;

public class AbstractCollection<T> {

	public AbstractCollection() {
		super();
	}

	protected ObjectId getId(T entity) {
		if (entity instanceof AbstractIdentifiableObject) {
			return ((AbstractIdentifiableObject) entity).getId();
		} else {
			Object idStr;
			try {
				idStr = PropertyUtils.getProperty(entity, AbstractIdentifiableObject.ID);
				return idStr != null ? new ObjectId(idStr.toString()) : null;
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected void setId(T entity, ObjectId id) {
		if (entity instanceof AbstractIdentifiableObject) {
			((AbstractIdentifiableObject) entity).setId(id);
		} else {
			try {
				PropertyUtils.setProperty(entity, AbstractIdentifiableObject.ID, id.toString());
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
	}

}