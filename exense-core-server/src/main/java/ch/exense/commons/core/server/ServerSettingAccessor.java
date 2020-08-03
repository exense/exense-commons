package ch.exense.commons.core.server;

import ch.exense.commons.core.accessors.CRUDAccessor;

public interface ServerSettingAccessor extends CRUDAccessor<ServerSetting> {

	public ServerSetting getSettingByKey(String key);

}