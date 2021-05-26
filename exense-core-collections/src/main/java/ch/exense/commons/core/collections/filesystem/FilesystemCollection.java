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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import ch.exense.commons.core.accessors.DefaultJacksonMapperProvider;
import ch.exense.commons.core.collections.Collection;
import ch.exense.commons.core.collections.Filter;
import ch.exense.commons.core.collections.PojoFilter;
import ch.exense.commons.core.collections.PojoFilters.PojoFilterFactory;
import ch.exense.commons.core.collections.PojoUtils;
import ch.exense.commons.core.collections.SearchOrder;

public class FilesystemCollection<T> extends AbstractCollection<T> implements Collection<T> {

	private static final String FILE_EXTENSION = ".entity";
	private final ObjectMapper mapper;
	private final File repository;
	private final Class<T> entityClass;

	public FilesystemCollection(File repository, Class<T> entityClass) {
		super();
		this.repository = repository;
		this.entityClass = entityClass;
		YAMLFactory factory = new YAMLFactory();
		// Disable native type id to enable conversion to generic Documents
		factory.disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID);
		this.mapper = DefaultJacksonMapperProvider.getObjectMapper(factory);
		if (!repository.exists()) {
			repository.mkdirs();
		}
	}

	@Override
	public List<String> distinct(String columnName, Filter filter) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static class FileAndEntity<T> {
		
		private final File file;
		private final T entity;
		
		public FileAndEntity(File file, T entity) {
			super();
			this.file = file;
			this.entity = entity;
		}

		public File getFile() {
			return file;
		}

		public T getEntity() {
			return entity;
		}
	}
	
	private Stream<FileAndEntity<T>> entityStream() {
		return Arrays.asList(repository.listFiles(f -> f.getName().endsWith(FILE_EXTENSION))).stream().map(f->new FileAndEntity<>(f, readFile(f)));
	}
	
	@Override
	public Stream<T> find(Filter filter, SearchOrder order, Integer skip, Integer limit, int maxTime) {
		Stream<T> stream = filteredStream(filter).map(FileAndEntity::getEntity).sorted(new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return getId(o1).compareTo(getId(o2));
			}
		});
		if(order != null) {
			@SuppressWarnings("unchecked")
			Comparator<T> comparing = (Comparator<T>) PojoUtils.comparator(order.getAttributeName());
			if(order.getOrder()<0) {
				comparing = comparing.reversed();
			}
			stream = stream.sorted(comparing);
		}
		if(skip != null) {
			stream = stream.skip(skip);
		}
		if(limit != null) {
			stream = stream.limit(limit);
		}
		return stream;
	}

	private Stream<FileAndEntity<T>> filteredStream(Filter filter) {
		PojoFilter<T> pojoFilter = new PojoFilterFactory<T>().buildFilter(filter);
		Iterator<FileAndEntity<T>> it = entityStream().iterator();
		Spliterator<FileAndEntity<T>> spliterator = Spliterators.spliteratorUnknownSize(it, 0);
		Stream<FileAndEntity<T>> filter2 = StreamSupport.stream(spliterator, false).filter(f-> {
			return pojoFilter.test(f.entity);
		});
		return filter2;
	}

	@Override
	public void remove(Filter filter) {
		filteredStream(filter).forEach(f->{
			f.getFile().delete();
		});
	}

	@Override
	public T save(T entity) {
		if (getId(entity) == null) {
			setId(entity, new ObjectId());
		}
		File file = getFile(entity);
		writeEntity(entity, file);
		return entity;
	}

	@Override
	public void save(Iterable<T> entities) {
		if (entities != null) {
			entities.forEach(e -> save(e));
		}
	}
	
	private T readFile(File file) {
		try {
			return mapper.readValue(file, entityClass);
		} catch (IOException e) {
			throw new FilesystemCollectionException(e);
		}
	}
	
	private void writeEntity(T entity, File file) {
		try {
			mapper.writeValue(file, entity);
		} catch (IOException e) {
			throw new FilesystemCollectionException(e);
		}
	}

	private File getFile(T entity) {
		ObjectId id = getId(entity);
		File file = getFileById(id);
		return file;
	}

	private File getFileById(ObjectId id) {
		String filename = id.toString() + FILE_EXTENSION;
		File file = new File(repository.getAbsolutePath() + "/" + filename);
		return file;
	}

	@Override
	public void createOrUpdateIndex(String field) {
		// not supported
	}

	@Override
	public void createOrUpdateCompoundIndex(String... fields) {
		// not supported
	}
	
	@Override
	public void rename(String newName) {
		// TODO Auto-generated method stub
	}

	@Override
	public void drop() {
		// TODO Auto-generated method stub
		
	}
}
