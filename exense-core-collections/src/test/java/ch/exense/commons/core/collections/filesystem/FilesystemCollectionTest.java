package ch.exense.commons.core.collections.filesystem;

import ch.exense.commons.app.Configuration;
import ch.exense.commons.core.collections.AbstractCollectionTest;
import ch.exense.commons.io.FileHelper;

import java.io.File;
import java.io.IOException;

public class FilesystemCollectionTest extends AbstractCollectionTest {

	public FilesystemCollectionTest() throws IOException {
		super(new FilesystemCollectionFactory(getConfiguration()));
	}
	
	private static Configuration getConfiguration() throws IOException {
		File folder = FileHelper.createTempFolder();
		Configuration configuration = new Configuration();
		configuration.putProperty(FilesystemCollectionFactory.DB_FILESYSTEM_PATH, folder.getAbsolutePath());
		return configuration;
	}
}
