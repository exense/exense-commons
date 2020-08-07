package ch.exense.commons.core.model.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.mongo.config.ExtractedArtifactStoreBuilder;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.extract.UserTempNaming;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.Processors;
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


		IMongodConfig mongodConfig = new MongodConfigBuilder()
				.version(Version.Main.PRODUCTION)
				.net(new Net(bindIp, this.port, Network.localhostIsIPv6()))
				.replication(new Storage(this.dbpath, null, 0))
				.build();
		
		IStreamProcessor mongodOutput = Processors.named("[mongod>]",
				new FileStreamProcessor(new File("./mongod.log")));
		IStreamProcessor mongodError = new FileStreamProcessor(File.createTempFile("mongod-error", "log"));
		IStreamProcessor commandsOutput = Processors.namedConsole("[console>]");

		IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
		.defaults(Command.MongoD)
		.processOutput(new ProcessOutput(mongodOutput, mongodError, commandsOutput))
		.artifactStore(new ExtractedArtifactStoreBuilder()
				.defaults(Command.MongoD)
				.download(new DownloadConfigBuilder()
						.defaultsForCommand(Command.MongoD).build())
				.executableNaming(new UserTempNaming()))
		.build();
		MongodStarter starter = MongodStarter.getInstance(runtimeConfig);
		

		MongodExecutable mongodExecutable = null;
		try {
			mongodExecutable = starter.prepare(mongodConfig);
			this.mongod = mongodExecutable.start();
			client = new MongoClient(bindIp, port);
			database = client.getDatabase("licensing");
		}catch(IOException e1) {
			System.err.println("A problem occured at start of EmbeddedMongo (Check mongo.lock file?)");
			e1.printStackTrace();
			try {
				if(mongodExecutable != null) {
					mongodExecutable.stop();
				}
			}catch(Exception e2) {
				System.err.println("Could not stop executable.");
				e2.printStackTrace();
			}
		}
	}

	public void stop() {
		this.client.close();
		this.mongod.stop();
	}
	
	public class FileStreamProcessor implements IStreamProcessor {

		private FileOutputStream outputStream;

		public FileStreamProcessor(File file) throws FileNotFoundException {
			outputStream = new FileOutputStream(file);
		}

		@Override
		public void process(String block) {
			try {
				outputStream.write(block.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onProcessed() {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
