package ch.exense.commons.core.server;

import ch.exense.commons.core.accessors.AbstractIdentifiableObject;

public class ControllerSetting extends AbstractIdentifiableObject {

	protected String key;
	
	protected String value;

	public ControllerSetting(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public ControllerSetting() {
		super();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
