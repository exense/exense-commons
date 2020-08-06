package ch.exense.commons.core.model.user;

import java.util.List;

import ch.exense.commons.core.model.accessors.CRUDAccessor;

public interface UserAccessor extends CRUDAccessor<User>{
	
	void remove(String username);

	List<User> getAllUsers();

	User getByUsername(String username);
}