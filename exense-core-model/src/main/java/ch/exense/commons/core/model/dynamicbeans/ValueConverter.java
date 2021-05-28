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
package ch.exense.commons.core.model.dynamicbeans;

public class ValueConverter {

	@SuppressWarnings("unchecked")
	public static <T> T convert(Object value, Class<T> classTo) {
		if(value != null) {
			if(String.class.isAssignableFrom(classTo)) {
				return (T) value.toString();
			} else if(Long.class.isAssignableFrom(classTo)) {
				if(value instanceof Number) {
					return (T)new Long(((Number)value).longValue());
				} else if(value instanceof String){
					return (T)new Long(Long.parseLong((String)value));
				} else {
					throw unsupportedConversionException(classTo, value);
				}
			} else if(Integer.class.isAssignableFrom(classTo)) {
				if(value instanceof Number) {
					return (T)new Integer(((Number)value).intValue());
				} else if(value instanceof String){
					return (T)new Integer(Integer.parseInt((String)value));
				} else {
					throw unsupportedConversionException(classTo, value);
				}
			} else {
				throw unsupportedConversionException(classTo, value);
			}
		} else {
			return null;
		}
	}
	
	protected static RuntimeException unsupportedConversionException(Class<?> class_, Object value) {
		return new RuntimeException("Unable to convert value of type "+value.getClass().getName()+" to "+class_.getName());
	}
}
