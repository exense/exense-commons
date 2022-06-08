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
package ch.exense.commons.core.access;

import java.util.List;

import jakarta.inject.Singleton;

import ch.exense.commons.core.access.authentication.AuthenticationManager;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.exense.commons.core.model.user.Preferences;
import ch.exense.commons.core.model.user.User;
import ch.exense.commons.core.model.user.UserAccessor;
import ch.exense.commons.core.web.container.ServerSetting;
import ch.exense.commons.core.web.container.ServerSettingAccessor;
import ch.exense.commons.core.web.services.AbstractServices;

@Singleton
@Path("admin")
public class AdminServices extends AbstractServices {
	
	protected ServerSettingAccessor serverSettingsAccessor;

	private static final String MAINTENANCE_MESSAGE_KEY = "maintenance_message";
	private static final String MAINTENANCE_TOGGLE_KEY = "maintenance_message_enabled";
	
	private static final Logger logger = LoggerFactory.getLogger(AdminServices.class);
	
	@PostConstruct
	public void init() throws Exception {
		// TODO: move to "defaultly" registered classes in server main instead of instanciating accessor ourselves?
		serverSettingsAccessor = (ServerSettingAccessor) getContext().get(ServerSettingAccessor.class.toString());
		
		logger.info("AdminServices post constructed successfully.");
	}

	@POST
	@Secured(right="user-write")
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/user")
	public void save(User user) {
		UserAccessor userAccessor = getUserAccessor();

		User previousUser = userAccessor.get(user.getId());
		if(previousUser == null) {
			// previousUser is null => we're creating a new user
			// initializing password
			resetPwd(user);
		}
		
		userAccessor.save(user);
	}

	@DELETE
	@Secured(right="user-write")
	@Path("/user/{id}")
	public void remove(@PathParam("id") String username) {
		getUserAccessor().remove(username);
	}
	
	@GET
	@Secured(right="user-read")
	@Path("/user/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser(@PathParam("id") String username) {
		return getUserAccessor().getByUsername(username);
	}
	
	@GET
	@Secured(right="user-read")
	@Path("/users")
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> getUserList() {
		return getUserAccessor().getAllUsers();
	}
	
	private static String INITIAL_PWD = "init";
	
	public static class ChangePwdRequest {
		
		private String oldPwd;
		
		private String newPwd;

		public ChangePwdRequest() {
			super();
		}

		public String getOldPwd() {
			return oldPwd;
		}

		public void setOldPwd(String oldPwd) {
			this.oldPwd = oldPwd;
		}

		public String getNewPwd() {
			return newPwd;
		}

		public void setNewPwd(String newPwd) {
			this.newPwd = newPwd;
		}
	}
	
	@GET
	@Path("/maintenance/message")
	public String getMaintenanceMessage() {
		ServerSetting setting = serverSettingsAccessor.getSettingByKey(MAINTENANCE_MESSAGE_KEY);
		return setting!=null?setting.getValue():null;
	}
	
	@POST
	@Secured(right="admin")
	@Path("/maintenance/message")
	public void setMaintenanceMessage(String message) {
		ServerSetting setting = serverSettingsAccessor.getSettingByKey(MAINTENANCE_MESSAGE_KEY);
		if(setting == null) {
			setting = new ServerSetting();
			setting.setKey(MAINTENANCE_MESSAGE_KEY);
		}
		setting.setValue(message);
		serverSettingsAccessor.save(setting);
	}
	
	@GET
	@Path("/maintenance/message/toggle")
	public boolean getMaintenanceMessageToggle() {
		ServerSetting setting = serverSettingsAccessor.getSettingByKey(MAINTENANCE_TOGGLE_KEY);
		return setting!=null?Boolean.parseBoolean(setting.getValue()):false;
	}
	
	@POST
	@Secured(right="admin")
	@Path("/maintenance/message/toggle")
	public void setMaintenanceMessageToggle(boolean enabled) {
		ServerSetting setting = serverSettingsAccessor.getSettingByKey(MAINTENANCE_TOGGLE_KEY);
		if(setting == null) {
			setting = new ServerSetting();
			setting.setKey(MAINTENANCE_TOGGLE_KEY);
		}
		setting.setValue(Boolean.toString(enabled));
		serverSettingsAccessor.save(setting);
	}
	
	@POST
	@Secured
	@Path("/myaccount/changepwd")
	public void resetMyPassword(ChangePwdRequest request) {
		User user = getCurrentUser();
		if(user!=null) {
			user.setPassword(AuthenticationManager.hashPassword(request.getNewPwd()));
			getUserAccessor().save(user);			
		}
	}

	protected User getCurrentUser() {
		return getUserAccessor().get(getSession().getUser().getId());
	}
	
	@GET
	@Secured
	@Path("/myaccount")
	@Produces(MediaType.APPLICATION_JSON)
	public User getMyUser() {
		User user = getCurrentUser();
		return user;
	}
		
	@GET
	@Secured
	@Path("/myaccount/preferences")
	public Preferences getPreferences() {
		User user = getCurrentUser();
		if(user!=null) {
			return user.getPreferences();
		} else {
			return null;
		}
	}
	
	@POST
	@Secured
	@Path("/myaccount/preferences/{id}")
	public void putPreference(@PathParam("id") String preferenceName, Object value) {
		User user = getCurrentUser();
		if(user!=null) {
			if(user.getPreferences()==null) {
				Preferences prefs = new Preferences();
				user.setPreferences(prefs);
			}
			user.getPreferences().put(preferenceName, value);
			getUserAccessor().save(user);			
		}
	}
	
	@POST
	@Secured
	@Path("/myaccount/preferences")
	public void putPreference( Preferences preferences) {
		User user = getCurrentUser();
		if(user!=null) {
			user.setPreferences(preferences);
			getUserAccessor().save(user);			
		}
	}
	
	@POST
	@Secured(right="user-write")
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/user/{id}/resetpwd")
	public void resetPassword(@PathParam("id") String username) {
		User user = getUserAccessor().getByUsername(username);
		resetPwd(user);
		getUserAccessor().save(user);
	}

	private UserAccessor getUserAccessor() {
		return (UserAccessor) getContext().get(User.class.getName());
	}

	private void resetPwd(User user) {
		user.setPassword(AuthenticationManager.hashPassword(INITIAL_PWD));
	}
}
