package ch.exense.commons.core.mongo.accessors.concrete;

import java.util.ArrayList;
import java.util.List;

import ch.exense.commons.core.model.user.User;
import ch.exense.commons.core.model.user.UserAccessor;
import ch.exense.commons.core.mongo.MongoClientSession;
import ch.exense.commons.core.mongo.accessors.generic.AbstractCRUDAccessor;

public class UserAccessorImpl extends AbstractCRUDAccessor<User> implements UserAccessor {

	public UserAccessorImpl(MongoClientSession clientSession) {
		super(clientSession, "users", User.class);
	}

	@Override
	public void remove(String username) {
		collection.remove("{'username':'"+username+"'}");
	}
	
	@Override
	public List<User> getAllUsers() {
		List<User> result = new ArrayList<>();
		collection.find().as(User.class).iterator().forEachRemaining(u->result.add(u));
		return result;
	}
	
	@Override
	public User getByUsername(String username) {
		assert username != null;
		return collection.findOne("{username: #}", username).as(User.class);
	}
}
