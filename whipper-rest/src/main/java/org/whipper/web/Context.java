package org.whipper.web;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class Context{

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

    @PostConstruct
    public void checkWorkingDir(){
        for (Directory d : Directory.values()) {
            check(d);
        }
    }

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

    private File getDir(Directory d, String id){
        return id == null ? d.f : new File(d.f, id);
    }

    public File getResultDir(String id){
        return getDir(Directory.RESULTS_DIR, id);
    }

    public File getArtifactDir(String id){
        return getDir(Directory.ARTIFACTS_DIR, id);
    }

    public File getScenarioDir(String id){
        return getDir(Directory.SCENARIOS_DIR, id);
    }

    public File getTmpDir(String id){
        return getDir(Directory.TMP_DIR, id);
    }
}
