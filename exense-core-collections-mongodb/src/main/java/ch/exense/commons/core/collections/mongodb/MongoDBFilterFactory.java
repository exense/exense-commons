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
package ch.exense.commons.core.collections.mongodb;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import ch.exense.commons.core.collections.Filter;
import ch.exense.commons.core.collections.Filters.*;

import java.util.List;
import java.util.stream.Collectors;

public class MongoDBFilterFactory implements FilterFactory<Bson> {

	@Override
	public Bson buildFilter(ch.exense.commons.core.collections.Filter filter) {
		List<Bson> childerPojoFilters;
		List<Filter> children = filter.getChildren();
		if(children != null) {
			childerPojoFilters = filter.getChildren().stream().map(f -> this.buildFilter(f))
					.collect(Collectors.toList());
		} else {
			childerPojoFilters = null;
		}

		if (filter instanceof And) {
			return com.mongodb.client.model.Filters.and(childerPojoFilters);
		} else if (filter instanceof Or) {
			return com.mongodb.client.model.Filters.or(childerPojoFilters);
		} else if (filter instanceof Not) {
			return com.mongodb.client.model.Filters.not(childerPojoFilters.get(0));
		} else if (filter instanceof True) {
			return com.mongodb.client.model.Filters.expr("true");
		} else if (filter instanceof Equals) {
			Equals equalsFilter = (Equals) filter;
			String field = equalsFilter.getField();
			Object expectedValue = equalsFilter.getExpectedValue();
			if(field.equals("id")) {
				field = "_id";
				if (expectedValue instanceof String) {
					expectedValue = new ObjectId((String) expectedValue);
				}
			}
			return com.mongodb.client.model.Filters.eq(field, expectedValue);
		} else if (filter instanceof Regex) {
			Regex regexFilter = (Regex) filter;
			if(regexFilter.isCaseSensitive()) {
				return com.mongodb.client.model.Filters.regex(regexFilter.getField(), regexFilter.getExpression());
			} else {
				return com.mongodb.client.model.Filters.regex(regexFilter.getField(), regexFilter.getExpression(), "i");
			}
		} else if (filter instanceof Gt) {
			Gt gtFilter = (Gt) filter;
			return com.mongodb.client.model.Filters.gt(gtFilter.getField(), gtFilter.getValue());
		} else if (filter instanceof Gte) {
			Gte gteFilter = (Gte) filter;
			return com.mongodb.client.model.Filters.gte(gteFilter.getField(), gteFilter.getValue());
		} else if (filter instanceof Lt) {
			Lt ltFilter = (Lt) filter;
			return com.mongodb.client.model.Filters.lt(ltFilter.getField(), ltFilter.getValue());
		} else if (filter instanceof Lte) {
			Lte lteFilter = (Lte) filter;
			return com.mongodb.client.model.Filters.lte(lteFilter.getField(), lteFilter.getValue());
		} else {
			throw new IllegalArgumentException("Unsupported filter type " + filter.getClass());
		}
	}
}
