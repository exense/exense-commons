package ch.exense.commons.core.server;

import ch.exense.commons.core.accessors.CRUDAccessor;

public interface ControllerSettingAccessor extends CRUDAccessor<ControllerSetting> {

	public ControllerSetting getSettingByKey(String key);

}