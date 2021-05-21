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
package ch.exense.commons.core.accessors;

import ch.exense.commons.core.collections.filesystem.FilesystemCollection;
import ch.exense.commons.core.model.accessors.AbstractIdentifiableObject;
import ch.exense.commons.core.model.accessors.AbstractOrganizableObject;
import ch.exense.commons.io.FileHelper;
import org.junit.Before;


import java.io.File;
import java.io.IOException;

public class FilesystemAccessorTest extends AbstractAccessorTest {

	@Before
	public void before() {
		File repository;
		try {
			repository = FileHelper.createTempFolder();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		accessor = new AbstractAccessor<AbstractIdentifiableObject>(
				new FilesystemCollection<>(repository, AbstractIdentifiableObject.class));
		organizableObjectAccessor = new AbstractAccessor<AbstractOrganizableObject>(
				new FilesystemCollection<>(repository, AbstractOrganizableObject.class));
	}
}
