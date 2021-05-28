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

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DynamicValue<T> {
	
	boolean dynamic;
	
	T value;
	
	@JsonIgnore
	EvaluationResult evalutationResult;
	
	String expression;
	
	String expressionType;

	public DynamicValue() {
		super();
	}

	public DynamicValue(T value) {
		super();
		this.value = value;
		this.dynamic = false;
	}

	public DynamicValue(String expression, String expressionType) {
		super();
		this.dynamic = true;
		this.expression = expression;
		this.expressionType = expressionType;
	}

	@SuppressWarnings("unchecked")
	public T get() {
		if(!isDynamic()) {
			return value;
		} else {
			if(evalutationResult!=null) {
				if(evalutationResult.evaluationException!=null) {
					Throwable cause = evalutationResult.evaluationException.getCause();
					String errorMsg = evalutationResult.evaluationException.getMessage();
					if (cause != null) {
						errorMsg = errorMsg + ". Groovy error: >>> " + cause.getMessage() + " <<<";
					}
					throw new RuntimeException(errorMsg, evalutationResult.evaluationException);
				} else {
					Object result = evalutationResult.getResultValue();
					return (T) result;					
				}
			} else {
				throw new RuntimeException("Expression hasn't been evaluated.");
			}
		}
	}
	
	public DynamicValue<T> cloneValue() {
		DynamicValue<T> clone = new DynamicValue<>();
		clone.dynamic = dynamic;
		clone.evalutationResult = null;
		clone.expression = expression;
		clone.expressionType = expressionType;
		clone.value = value;
		return clone;
	}

	public boolean isDynamic() {
		return dynamic;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getExpressionType() {
		return expressionType;
	}

	public void setExpressionType(String expressionType) {
		this.expressionType = expressionType;
	}
	
	public String toString() {
		return get().toString();
	}
}
