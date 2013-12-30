package com.github.jrh3k5.flume.mojo.internal;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * Skeleton definition of common unit test functions.
 * 
 * @author Joshua Hyde
 */

public abstract class AbstractUnitTest {
    /**
     * A {@link Rule} used to supply the current test name.
     */
    @Rule
    public TestName testName = new TestName();

    /**
     * Get the test directory.
     * 
     * @return A {@link File} referencing a temporary directory isolated to this test.
     * @throws IOException
     *             If any errors occur while creating (if needed) the test directory.
     */
    protected File getTestDirectory() throws IOException {
        final File classDirectory = new File("target", getClass().getSimpleName());
        FileUtils.forceMkdir(classDirectory);

        final File testDirectory = new File(classDirectory, getTestName());
        FileUtils.forceMkdir(testDirectory);
        return testDirectory;
    }

    /**
     * Get the name of the current test.
     * 
     * @return The name of the current test.
     */
    protected String getTestName() {
        return testName.getMethodName();
    }
}
