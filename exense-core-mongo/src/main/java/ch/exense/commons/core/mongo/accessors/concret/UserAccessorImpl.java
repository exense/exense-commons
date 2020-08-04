package ch.exense.commons.core.mongo.accessors.concret;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import ch.exense.commons.core.mongo.accessors.generic.AbstractCRUDAccessor;
import ch.exense.commons.core.mongo.accessors.generic.MongoClientSession;
import ch.exense.commons.core.user.User;
import ch.exense.commons.core.user.UserAccessor;

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
	
	public static String encryptPwd(String pwd) {
		return DigestUtils.sha512Hex(pwd);
	}

}
