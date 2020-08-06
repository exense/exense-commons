package ch.exense.commons.core.web.container;

import ch.exense.commons.core.model.accessors.CRUDAccessor;

public interface ServerSettingAccessor extends CRUDAccessor<ServerSetting> {

	public ServerSetting getSettingByKey(String key);

}