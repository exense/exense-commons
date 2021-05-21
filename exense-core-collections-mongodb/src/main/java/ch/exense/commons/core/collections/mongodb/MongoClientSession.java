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
package ch.exense.commons.core.collections.mongodb;

import ch.exense.commons.app.Configuration;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientSettings.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import ch.exense.commons.core.collections.Collection;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MongoClientSession implements Closeable {

	protected final MongoClient mongoClient;
	protected final String db;
	
	public MongoClientSession(Configuration configuration) {
		super();

		String host = configuration.getProperty("db.host");
		Integer port = configuration.getPropertyAsInteger("db.port",27017);
		String user = configuration.getProperty("db.username");
		String pwd = configuration.getProperty("db.password");

		int maxConnections = configuration.getPropertyAsInteger("db.maxConnections", 200);
		Integer minConnections = configuration.getPropertyAsInteger("db.minConnections");
		Integer maxConnectionIdleTimeMs = configuration.getPropertyAsInteger("db.maxConnectionIdleTimeMs");
		Integer maintenanceFrequencyMs = configuration.getPropertyAsInteger("db.maintenanceFrequencyMs");
		Integer maxConnectionLifeTimeMs = configuration.getPropertyAsInteger("db.maxConnectionLifeTimeMs");
		Integer maxWaitTimeMs = configuration.getPropertyAsInteger("db.maxWaitTimeMs");

		db = configuration.getProperty("db.database","step");
		
		Builder builder = MongoClientSettings.builder();
		if(user!=null) {
			MongoCredential credential = MongoCredential.createCredential(user, db, pwd.toCharArray());
			builder.credential(credential);
		}
		builder.applyConnectionString(new ConnectionString("mongodb://"+host+":"+port));
		//ref https://mongodb.github.io/mongo-java-driver/4.0/apidocs/mongodb-driver-core/com/mongodb/connection/ConnectionPoolSettings.html
		builder.applyToConnectionPoolSettings(poolSettingBuilder -> {
			poolSettingBuilder.maxSize(maxConnections);
			if (minConnections != null)
				poolSettingBuilder.minSize(minConnections);
			if (maxConnectionIdleTimeMs != null)
				poolSettingBuilder.maxConnectionIdleTime(maxConnectionIdleTimeMs, TimeUnit.MILLISECONDS);
			if (maintenanceFrequencyMs != null)
				poolSettingBuilder.maintenanceFrequency(maintenanceFrequencyMs, TimeUnit.MILLISECONDS);
			if (maxConnectionLifeTimeMs != null)
				poolSettingBuilder.maxConnectionLifeTime(maxConnectionLifeTimeMs, TimeUnit.MILLISECONDS);
			if (maxWaitTimeMs != null)
				poolSettingBuilder.maxWaitTime(maxWaitTimeMs, TimeUnit.MILLISECONDS);
			poolSettingBuilder.build();
		});
		mongoClient = MongoClients.create(builder.build());
		
	}
	
	public MongoDatabase getMongoDatabase() {
		return mongoClient.getDatabase(db);
	}
	
	public MongoClient getMongoClient() {
		return mongoClient;
	}

	public <T> Collection<T> getEntityCollection(String name, Class<T> entityClass) {
		return new ch.exense.commons.core.collections.mongodb.MongoDBCollection<T>(this, name, entityClass);
	}

	@Override
	public void close() throws IOException {
		mongoClient.close();
	}
	
}
