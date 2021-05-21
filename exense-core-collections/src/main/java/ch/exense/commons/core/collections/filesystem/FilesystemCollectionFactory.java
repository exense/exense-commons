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

import ch.exense.commons.app.Configuration;
import ch.exense.commons.core.collections.Collection;
import ch.exense.commons.core.collections.CollectionFactory;

import java.io.File;
import java.io.IOException;

public class FilesystemCollectionFactory implements CollectionFactory {

	public static final String DB_FILESYSTEM_PATH = "db.filesystem.path";
	private final File workspace;
	
	public FilesystemCollectionFactory(Configuration configuration) {
		super();
		this.workspace = configuration.getPropertyAsDirectory(DB_FILESYSTEM_PATH, new File("db"));
	}

	public FilesystemCollectionFactory(File workspace) {
		super();
		this.workspace = workspace;
	}

	@Override
	public void close() throws IOException {
		
	}

	@Override
	public <T> Collection<T> getCollection(String name, Class<T> entityClass) {
		return new FilesystemCollection<>(new File(workspace.getAbsolutePath()+"/"+name), entityClass);
	}

}
