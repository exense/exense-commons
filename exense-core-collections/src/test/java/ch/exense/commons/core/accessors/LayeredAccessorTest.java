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

import ch.exense.commons.core.model.accessors.AbstractOrganizableObject;
import com.google.common.collect.Iterators;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class LayeredAccessorTest {

	private ObjectId entity1_1;
	private ObjectId entity2_1;
	private ObjectId entity2_2;
	private InMemoryAccessor<AbstractOrganizableObject> accessor1;
	private InMemoryAccessor<AbstractOrganizableObject> accessor2;

	public LayeredAccessorTest() {
		super();
		accessor1 = new InMemoryAccessor<>();
		accessor2 = new InMemoryAccessor<>();
		
		entity1_1 = addEntity(accessor1, "entity 1");
		
		entity2_1 = addEntity(accessor2, "entity 1");
		entity2_2 = addEntity(accessor2, "entity 2");
	}

	@Test
	public void test() {
		LayeredAccessor<AbstractOrganizableObject> accessor = new LayeredAccessor<>();
		accessor.pushAccessor(accessor2);
		accessor.pushAccessor(accessor1);
		
		performTests(accessor);
	}
	
	@Test
	public void test2() {
		LayeredAccessor<AbstractOrganizableObject> accessor = new LayeredAccessor<>();
		accessor.addAccessor(accessor1);
		accessor.addAccessor(accessor2);
		
		performTests(accessor);
	}
	
	@Test
	public void test3() {
		ArrayList<InMemoryAccessor<AbstractOrganizableObject>> list = new ArrayList<InMemoryAccessor<AbstractOrganizableObject>>();
		list.add(accessor1);
		list.add(accessor2);
		LayeredAccessor<AbstractOrganizableObject> accessor = new LayeredAccessor<>(list);
		
		performTests(accessor);
	}

	protected void performTests(LayeredAccessor<AbstractOrganizableObject> accessor) {
		testGet(entity1_1, accessor);
		testGet(entity2_1, accessor);
		testGet(entity2_2, accessor);
		
		AbstractOrganizableObject[] all = Iterators.toArray(accessor.getAll(), AbstractOrganizableObject.class);
		assertEquals(3, all.length);
		
		AbstractOrganizableObject entity = findByName("entity 1", accessor);
		assertEquals(entity1_1, entity.getId());
		
		entity = findByName("entity 2", accessor);
		assertEquals(entity2_2, entity.getId());
		
		entity = accessor.findByAttributes(newAttributes("entity 1"), "attributes");
		assertEquals(entity1_1, entity.getId());
		
		List<AbstractOrganizableObject> entities = new ArrayList<>();
		accessor.findManyByAttributes(newAttributes("entity 1")).forEachRemaining(entities::add);
		assertEquals(2, entities.size());
		assertEquals(entity1_1, entities.get(0).getId());
		assertEquals(entity2_1, entities.get(1).getId());
		
		entities = new ArrayList<>();
		accessor.findManyByAttributes(newAttributes("entity 1"), "attributes").forEachRemaining(entities::add);
		assertEquals(2, entities.size());
		assertEquals(entity1_1, entities.get(0).getId());
		assertEquals(entity2_1, entities.get(1).getId());
		
		AbstractOrganizableObject entity1_3 = newEntity("entity 3");
		accessor.save(entity1_3);
		entity = accessor1.get(entity1_3.getId());
		assertNotNull(entity);
		
		entity = accessor2.get(entity1_3.getId());
		assertNull(entity);
		
		accessor.remove(entity1_3.getId());
		entity = accessor1.get(entity1_3.getId());
		assertNull(entity);
		
		entity = accessor.get(new ObjectId());
		assertNull(entity);
		
		List<AbstractOrganizableObject> range = accessor.getRange(0, 1);
		assertEquals(entity1_1, range.get(0).getId());;
	}

	protected AbstractOrganizableObject findByName(String name, LayeredAccessor<AbstractOrganizableObject> accessor) {
		HashMap<String, String> attributes = newAttributes(name);
		AbstractOrganizableObject entity = accessor.findByAttributes(attributes);
		return entity;
	}

	protected HashMap<String, String> newAttributes(String name) {
		HashMap<String, String> attributes = new HashMap<>();
		attributes.put(AbstractOrganizableObject.NAME, name);
		return attributes;
	}

	protected void testGet(ObjectId entity1_1, LayeredAccessor<AbstractOrganizableObject> accessor) {
		AbstractOrganizableObject result = accessor.get(entity1_1);
		assertEquals(entity1_1, result.getId());
		
		accessor.get(entity1_1.toString());
		assertEquals(entity1_1, result.getId());
	}

	protected ObjectId addEntity(InMemoryAccessor<AbstractOrganizableObject> accessor1, String name) {
		AbstractOrganizableObject entity = newEntity(name);
		accessor1.save(entity);
		return entity.getId();
	}

	protected AbstractOrganizableObject newEntity(String name) {
		AbstractOrganizableObject entity = new AbstractOrganizableObject();
		entity.setAttributes(new HashMap<>());
		entity.getAttributes().put(AbstractOrganizableObject.NAME, name);
		return entity;
	}

}
