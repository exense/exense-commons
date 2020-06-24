package ch.exense.commons.core.model.utils;

import java.io.IOException;
import java.net.UnknownHostException;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class EmbeddedMongo{

	private MongodProcess mongod;
	private MongoClient client;
	private MongoDatabase database;

	private String bindIp;
	private int port;
	private String dbpath;

	public static EmbeddedMongo INSTANCE = new EmbeddedMongo();
	public static EmbeddedMongo getInstance() { return INSTANCE;}

	public void start(String dbpath, String bind, int port) throws UnknownHostException, IOException {
		this.bindIp = bind;
		this.port = port;
		this.dbpath = dbpath;

		MongodStarter starter = MongodStarter.getDefaultInstance();

		IMongodConfig mongodConfig = new MongodConfigBuilder()
				.version(Version.Main.PRODUCTION)
				.net(new Net(bindIp, this.port, Network.localhostIsIPv6()))
				.replication(new Storage(this.dbpath, null, 0))
				.build();

		MongodExecutable mongodExecutable = null;
		try {
			mongodExecutable = starter.prepare(mongodConfig);
			this.mongod = mongodExecutable.start();
			client = new MongoClient(bindIp, port);
			database = client.getDatabase("licensing");
		}catch(IOException e1) {
			System.out.println("Retrying start due to windows issue with mongo.lock file");
			try {
				mongodExecutable.stop();
				this.mongod = mongodExecutable.start();
			}catch(Exception e2) {
				System.out.println("Failed retry most likely due to mongo.lock locking issue");
				e1.printStackTrace();
				e2.printStackTrace();
			}
		}
	}

	public MongoCollection<Document> getHostsCollection() {
		return database.getCollection("hosts");
	}

	public void stop() {
		this.client.close();
		this.mongod.stop();
	}
}
