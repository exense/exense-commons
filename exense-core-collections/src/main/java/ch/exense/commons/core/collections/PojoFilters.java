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
package ch.exense.commons.core.collections;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;

import ch.exense.commons.core.collections.Filters.And;
import ch.exense.commons.core.collections.Filters.Equals;
import ch.exense.commons.core.collections.Filters.FilterFactory;
import ch.exense.commons.core.collections.Filters.Gt;
import ch.exense.commons.core.collections.Filters.Gte;
import ch.exense.commons.core.collections.Filters.Lt;
import ch.exense.commons.core.collections.Filters.Lte;
import ch.exense.commons.core.collections.Filters.Not;
import ch.exense.commons.core.collections.Filters.Or;
import ch.exense.commons.core.collections.Filters.Regex;
import ch.exense.commons.core.collections.Filters.True;
import ch.exense.commons.core.model.accessors.AbstractIdentifiableObject;

public class PojoFilters {

	public static class PojoFilterFactory<POJO> implements FilterFactory<PojoFilter<?>> {

		@Override
		public PojoFilter<POJO> buildFilter(Filter filter) {

			List<PojoFilter<POJO>> childerPojoFilters;
			List<Filter> children = filter.getChildren();
			if(children != null) {
				childerPojoFilters = children.stream().map(f -> this.buildFilter(f))
						.collect(Collectors.toList());
			} else {
				childerPojoFilters = null;
			}

			if (filter instanceof And) {
				return new AndPojoFilter<POJO>(childerPojoFilters);
			} else if (filter instanceof Or) {
				return new OrPojoFilter<POJO>(childerPojoFilters);
			} else if (filter instanceof Not) {
				return new NotPojoFilter<POJO>(childerPojoFilters.get(0));
			} else if (filter instanceof Or) {
				return new OrPojoFilter<POJO>(childerPojoFilters);
			} else if (filter instanceof Equals) {
				return new EqualsPojoFilter<POJO>((Equals) filter);
			} else if (filter instanceof Regex) {
				return new RegexPojoFilter<POJO>((Regex) filter);
			} else if (filter instanceof True) {
				return new TruePojoFilter<POJO>();
			} else if (filter instanceof Lt) {
				return new LtPojoFilter<POJO>((Lt) filter);
			} else if (filter instanceof Lte) {
				return new LtePojoFilter<POJO>((Lte) filter);
			} else if (filter instanceof Gt) {
				return new GtPojoFilter<POJO>((Gt) filter);
			}else if (filter instanceof Gte) {
				return new GtePojoFilter<POJO>((Gte) filter);
			} else {
				throw new IllegalArgumentException("Unsupported filter type " + filter.getClass());
			}
		}
	}

	public static class AndPojoFilter<T> implements PojoFilter<T> {

		private final List<PojoFilter<T>> pojoFilters;

		public AndPojoFilter(List<PojoFilter<T>> PojoFilters) {
			super();
			this.pojoFilters = PojoFilters;
		}

		@Override
		public boolean test(T t) {
			return pojoFilters.stream().allMatch(PojoFilter -> PojoFilter.test(t));
		}
	}

	public static class OrPojoFilter<T> implements PojoFilter<T> {

		private final List<PojoFilter<T>> pojoFilters;

		public OrPojoFilter(List<PojoFilter<T>> PojoFilters) {
			super();
			this.pojoFilters = PojoFilters;
		}

		@Override
		public boolean test(T t) {
			return pojoFilters.stream().anyMatch(PojoFilter -> PojoFilter.test(t));
		}
	}

	public static class NotPojoFilter<T> implements PojoFilter<T> {

		private final PojoFilter<T> pojoFilter;

		public NotPojoFilter(PojoFilter<T> PojoFilter) {
			super();
			this.pojoFilter = PojoFilter;
		}

		@Override
		public boolean test(T t) {
			return !pojoFilter.test(t);
		}
	}
	
	public static class TruePojoFilter<T> implements PojoFilter<T> {

		public TruePojoFilter() {
			super();
		}

		@Override
		public boolean test(T t) {
			return true;
		}
	}

	public static class EqualsPojoFilter<T> implements PojoFilter<T> {

		private final Equals equalsFilter;

		public EqualsPojoFilter(Equals equalsFilter) {
			super();
			this.equalsFilter = equalsFilter;
		}

		@Override
		public boolean test(T t) {
			try {
				String field = equalsFilter.getField();
				Object beanProperty = getBeanProperty(t, field);
				Object expectedValue = equalsFilter.getExpectedValue();
				if(expectedValue != null) {
					if (field.equals(AbstractIdentifiableObject.ID) && expectedValue instanceof String){
						expectedValue = new ObjectId((String) expectedValue);
					}
					if(expectedValue instanceof Number) {
						if(beanProperty != null) {
							return new BigDecimal(expectedValue.toString()).compareTo(new BigDecimal(beanProperty.toString()))==0;
						} else {
							return false;
						}
					} else {
						return expectedValue.equals(beanProperty);
					}
				} else {
					return beanProperty == null; 
				}
			} catch (NoSuchMethodException e) {
				return (equalsFilter.getExpectedValue() == null);
			} catch (IllegalAccessException | InvocationTargetException e) {
				return false;
			}
		}
	}

	public static class RegexPojoFilter<T> implements PojoFilter<T> {

		private final Regex regexFilter;
		private final Pattern pattern;

		public RegexPojoFilter(Regex regexFilter) {
			super();
			this.regexFilter = regexFilter;
			String expression = "";
			if(!regexFilter.isCaseSensitive()) {
				expression += "(?i)";
			}
			expression += regexFilter.getExpression();
			pattern = Pattern.compile(expression);
		}

		@Override
		public boolean test(T t) {
			try {
				Object beanProperty = getBeanProperty(t, regexFilter.getField());
				if(beanProperty != null) {
			Matcher matcher = pattern.matcher(beanProperty.toString());
			return matcher.find();
		} else {
			return false;
		}
	} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
		return false;
	}
}
	}

	public static class LtPojoFilter<T> implements PojoFilter<T> {

		private final Lt ltFilter;

		public LtPojoFilter(Lt ltFilter) {
			super();
			this.ltFilter = ltFilter;
		}

		@Override
		public boolean test(T t) {
			try {
				String field = ltFilter.getField();
				Object beanProperty = getBeanProperty(t, field);
				long value = ltFilter.getValue();
				if(beanProperty instanceof Number) {
					Number fieldValue = (Number) beanProperty;
					return ( fieldValue.longValue() < value);
				} else {
					throw new RuntimeException("Gt,Gte,Lt and Lte filters only support numbers, provided field is not compatible: " + field);
				}
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				return false;
			}
		}
	}

	public static class LtePojoFilter<T> implements PojoFilter<T> {

		private final Lte lteFilter;

		public LtePojoFilter(Lte lteFilter) {
			super();
			this.lteFilter = lteFilter;
		}

		@Override
		public boolean test(T t) {
			try {
				String field = lteFilter.getField();
				Object beanProperty = getBeanProperty(t, field);
				long value = lteFilter.getValue();
				if(beanProperty instanceof Number) {
					Number fieldValue = (Number) beanProperty;
					return ( fieldValue.longValue() <= value);
				} else {
					throw new RuntimeException("Gt,Gte,Lt and Lte filters only support numbers, provided field is not compatible: " + field);
				}
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				return false;
			}
		}
	}

	public static class GtPojoFilter<T> implements PojoFilter<T> {

		private final Gt gtFilter;

		public GtPojoFilter(Gt gtFilter) {
			super();
			this.gtFilter = gtFilter;
		}

		@Override
		public boolean test(T t) {
			try {
				String field = gtFilter.getField();
				Object beanProperty = getBeanProperty(t, field);
				long value = gtFilter.getValue();
				if(beanProperty instanceof Number) {
					Number fieldValue = (Number) beanProperty;
					return ( fieldValue.longValue() > value);
				} else {
					throw new RuntimeException("Gt,Gte,Lt and Lte filters only support numbers, provided field is not compatible: " + field);
				}
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				return false;
			}
		}
	}

	public static class GtePojoFilter<T> implements PojoFilter<T> {

		private final Gte gteFilter;

		public GtePojoFilter(Gte gteFilter) {
			super();
			this.gteFilter = gteFilter;
		}

		@Override
		public boolean test(T t) {
			try {
				String field = gteFilter.getField();
				Object beanProperty = getBeanProperty(t, field);
				long value = gteFilter.getValue();
				if (beanProperty instanceof Number) {
					Number fieldValue = (Number) beanProperty;
					return (fieldValue.longValue() >= value);
				} else {
					throw new RuntimeException("Gt,Gte,Lt and Lte filters only support numbers, provided field is not compatible: " + field);
				}
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				return false;
			}
		}
	}
	
	private static Object getBeanProperty(Object t, String fieldName)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return PojoUtils.getProperty(t, fieldName);
	}
}
