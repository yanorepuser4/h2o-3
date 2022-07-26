package water.persist;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.fs.s3a.S3AFileSystem;
import org.apache.hadoop.util.Progressable;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.*;

public class S3AClientFactoryTest {

    @Test
    public void getOrMakeClient_noBucket() {
        TestedS3AClientFactory factory = new TestedS3AClientFactory();
        assertNull(factory.getOrMakeClient(null));
        assertNotNull(factory._fs);
        assertEquals(URI.create("s3a://www.h2o.ai/"), factory._fs.getUri());
    }

    @Test
    public void getOrMakeClient_customDefaultBucket() {
        TestedS3AClientFactory factory = new TestedS3AClientFactory();
        assertNull(factory.getOrMakeClient(null));
        assertNotNull(factory._fs);
        assertEquals(URI.create("s3a://www.h2o.ai/"), factory._fs.getUri());
    }

    @Test
    public void getOrMakeClient() {
        TestedS3AClientFactory factory = new TestedS3AClientFactory();
        assertNull(factory.getOrMakeClient("mybucket"));
        assertNotNull(factory._fs);
        assertEquals(URI.create("s3a://mybucket/"), factory._fs.getUri());
    }

    @Ignore
    private static class TestedS3AClientFactory extends S3AClientFactory {
        private FileSystem _fs;
        @Override
        protected FileSystem getFileSystem(URI uri) {
            _fs = new DummyFileSystem(uri);
            return _fs;
        }
    }

    private static class DummyFileSystem extends S3AFileSystem {
        private final URI _uri;

        DummyFileSystem(URI uri) {
            _uri = uri;
        }

        @Override
        public URI getUri() {
            return _uri;
        }

        @Override
        public FSDataInputStream open(Path path, int i) throws IOException {
            return null;
        }

        @Override
        public FSDataOutputStream create(Path path, FsPermission fsPermission, boolean b, int i, short i1, long l, Progressable progressable) throws IOException {
            return null;
        }

        @Override
        public FSDataOutputStream append(Path path, int i, Progressable progressable) throws IOException {
            return null;
        }

        @Override
        public boolean rename(Path path, Path path1) throws IOException {
            return false;
        }

        @Override
        public boolean delete(Path path, boolean b) throws IOException {
            return false;
        }

        @Override
        public FileStatus[] listStatus(Path path) throws FileNotFoundException, IOException {
            return new FileStatus[0];
        }

        @Override
        public void setWorkingDirectory(Path path) {

        }

        @Override
        public Path getWorkingDirectory() {
            return null;
        }

        @Override
        public boolean mkdirs(Path path, FsPermission fsPermission) throws IOException {
            return false;
        }

    }
    
}