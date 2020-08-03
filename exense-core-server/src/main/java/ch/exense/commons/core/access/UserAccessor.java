package ch.exense.commons.core.access;

import java.util.List;

import ch.exense.commons.core.accessors.CRUDAccessor;

public interface UserAccessor extends CRUDAccessor<User>{
	
	void remove(String username);

	List<User> getAllUsers();

	User getByUsername(String username);
}