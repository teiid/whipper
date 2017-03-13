package org.whipper.web;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * Application context.
 */
@Singleton
@Startup
public class Context{

    /**
     * Directories on file system.
     */
    private static enum Directory{
        BASE_DIR(new File(System.getProperty("user.home"), "whipper-web-ui")),
        RESULTS_DIR(new File(BASE_DIR.f, "results")),
        ARTIFACTS_DIR(new File(BASE_DIR.f, "artifacts")),
        SCENARIOS_DIR(new File(BASE_DIR.f, "scenarios")),
        TMP_DIR(new File(BASE_DIR.f, "tmp"));

        private final File f;

        private Directory(File f){
            this.f = f;
        }
    }

    /**
     * Check whether all required directories exist or can be created.
     */
    @PostConstruct
    public void checkWorkingDir(){
        for (Directory d : Directory.values()) {
            check(d);
        }
    }

    /**
     * Check single directory.
     *
     * @param d directory.
     */
    private void check(Directory d){
        if (!d.f.exists()) {
            if (!d.f.mkdirs()) {
                throw new IllegalStateException("Cannot create directory - " + d.f.getAbsolutePath());
            }
        } else {
            if (!d.f.isDirectory()) {
                throw new IllegalStateException("Not a directory - " + d.f.getAbsolutePath());
            }
        }
    }

    /**
     * Returns directory.
     *
     * @param d directory
     * @param id optional name of the file (or directory) in {@code d}.
     * @return required directory
     */
    private File getDir(Directory d, String id){
        return id == null ? d.f : new File(d.f, id);
    }

    /**
     * Returns directory where results are stored.
     *
     * @param id optional name of the file (or directory) in results directory
     * @return results directory or file/directory in it
     */
    public File getResultDir(String id){
        return getDir(Directory.RESULTS_DIR, id);
    }

    /**
     * Returns directory where artifacts are stored.
     *
     * @param id optional name of the file (or directory) in artifacts directory
     * @return artifacts directory or file/directory in it
     */
    public File getArtifactDir(String id){
        return getDir(Directory.ARTIFACTS_DIR, id);
    }

    /**
     * Returns directory where scenarios are stored.
     *
     * @param id optional name of the file (or directory) in scenarios directory
     * @return scenarios directory or file/directory in it
     */
    public File getScenarioDir(String id){
        return getDir(Directory.SCENARIOS_DIR, id);
    }

    /**
     * Returns directory where temporary files are stored.
     *
     * @param id optional name of the file (or directory) in temporary directory
     * @return temporary directory or file/directory in it
     */
    public File getTmpDir(String id){
        return getDir(Directory.TMP_DIR, id);
    }
}
