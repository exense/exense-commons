package ch.exense.commons.core.access.authentication;

import ch.exense.commons.core.model.user.UserAccessor;
import ch.exense.commons.core.mongo.MongoClientSession;
import ch.exense.commons.core.mongo.accessors.concrete.UserAccessorImpl;

public class MongoAuthenticator extends DirectoryComparisonAuthenticator{

	public MongoAuthenticator(UserAccessor userAccessor) {
		super(new UserAccessorDirectory(userAccessor));
	}
	
	public MongoAuthenticator(MongoClientSession clientSession) {
		super(new UserAccessorDirectory(new UserAccessorImpl(clientSession)));
	}

}
