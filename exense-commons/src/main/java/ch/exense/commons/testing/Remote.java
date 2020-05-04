/*******************************************************************************
 * (C) Copyright 2016 Dorian Cransac and Jerome Comte
 *  
 * This file is part of exense-common
 *  
 * exense-common is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * exense-common is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with exense-common.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package ch.exense.commons.testing;

/*
 * This annotation is required in JUnit 4 in order to tag tests.
 * In JUnit 5, the Tag annotation (with the category as a string argument) can be used instead.
 * There seems to be a bug in the runner in JUnit 4.9 - 4.12 preventing the use of the -Dgroups option
 */
public interface Remote {
}
