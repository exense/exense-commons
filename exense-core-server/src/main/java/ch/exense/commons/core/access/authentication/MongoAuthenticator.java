package ch.exense.commons.core.access.authentication;

import ch.exense.commons.core.mongo.accessors.concrete.UserAccessorImpl;
import ch.exense.commons.core.mongo.accessors.generic.MongoClientSession;
import ch.exense.commons.core.user.UserAccessor;

public class MongoAuthenticator extends DirectoryComparisonAuthenticator{

	public MongoAuthenticator(UserAccessor userAccessor) {
		super(new UserAccessorDirectory(userAccessor));
	}
	
	public MongoAuthenticator(MongoClientSession clientSession) {
		super(new UserAccessorDirectory(new UserAccessorImpl(clientSession)));
	}

}
