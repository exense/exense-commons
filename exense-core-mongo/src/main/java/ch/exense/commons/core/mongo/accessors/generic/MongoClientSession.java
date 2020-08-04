package ch.exense.commons.core.mongo.accessors.generic;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.JacksonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

public class MongoClientSession implements Closeable {

	protected MongoClient mongoClient;
	
	protected String db;
	
	private static final Logger logger = LoggerFactory.getLogger(MongoClientSession.class);
	
	public MongoClientSession(String host, int port, String user, String pwd, int maxConnections, String db) {

		ServerAddress address = new ServerAddress(host, port);
		List<MongoCredential> credentials = new ArrayList<>();
		if(user!=null) {
			MongoCredential credential = MongoCredential.createCredential(user, db, pwd.toCharArray());
			credentials.add(credential);
		}
		this.db = db;
		
		MongoClientOptions.Builder clientOptions = new MongoClientOptions.Builder();
		MongoClientOptions options = clientOptions.connectionsPerHost(maxConnections).build();
		mongoClient = new MongoClient(address, credentials,options);
		
		logger.info("mongo client initialized at: " + host +":" + port + " -- "+db);
		
	}
	
	public MongoDatabase getMongoDatabase() {
		return mongoClient.getDatabase(db);
	}
	
	public org.jongo.MongoCollection getJongoCollection(String collectionName) {
		@SuppressWarnings("deprecation")
		DB db = mongoClient.getDB(this.db);
		
		JacksonMapper.Builder builder = new JacksonMapper.Builder();
		AccessorLayerJacksonMapperProvider.getModules().forEach(m->builder.registerModule(m));
		
		Jongo jongo = new Jongo(db,builder.build());
		MongoCollection collection = jongo.getCollection(collectionName);
		
		return collection;
	}

	@Override
	public void close() throws IOException {
		mongoClient.close();
	}
	
}
