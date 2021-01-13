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
package ch.exense.commons.core.model.accessors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>
 * With this annotation you can define attributes ({@link AbstractOrganizableObject#attributes})
 * to be added to any kind of object that ends up in a an {@link AbstractOrganizableObject}
 * 
 * This is for instance the case with Keywords:
 * </p>
 * <p>
 * {@literal @}Attribute("name"="project", "value"="@system" <br>
 * public class MyKeywordLibrary extends AbstractKeyword {<br>
 * <br>
 * }
 * </p>
 * <p>
 * This annotation can be added to types or methods
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Attribute {

	public String key();

	public String value();

}
