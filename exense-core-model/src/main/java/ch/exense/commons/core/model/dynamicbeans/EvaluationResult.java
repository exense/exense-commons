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

public class EvaluationResult {

	Object resultValue;
	
	Exception evaluationException;

	public EvaluationResult() {
		super();
	}

	public EvaluationResult(Object resultValue) {
		super();
		this.resultValue = resultValue;
	}

	public Object getResultValue() {
		return resultValue;
	}

	public void setResultValue(Object resultValue) {
		this.resultValue = resultValue;
	}

	public Exception getEvaluationException() {
		return evaluationException;
	}

	public void setEvaluationException(Exception evaluationException) {
		this.evaluationException = evaluationException;
	}
}
