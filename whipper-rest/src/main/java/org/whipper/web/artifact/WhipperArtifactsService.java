package org.whipper.web.artifact;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.whipper.web.Context;
import org.whipper.web.Utils;

/**
 * Whipper artifacts service.
 * <p>
 * This service manages all artifacts loaded to the system. Structure of the artifacts is
 * <pre>
 *     &lt;artifact-name>
 *         |_queries
 *           |_...
 *         |_expected_results
 *           |_...
 * </pre>
 */
@Singleton
@Startup
public class WhipperArtifactsService{

    @Inject
    private Context context;

    /**
     * Deletes artifacts.
     *
     * @param name artifacts name
     */
    @Lock(LockType.WRITE)
    public void deleteArtifacts(String name){
        if(name == null || name.isEmpty()){
            return;
        }
        Utils.delete(context.getArtifactDir(name));
    }

    /**
     * Creates artifacts from a ZIP archive.
     * <p>
     * The entries in archive must have structure defined in documentation of this class.
     *
     * @param name artifacts name
     * @param zip path to ZIP archive
     */
    @Lock(LockType.WRITE)
    public void createArtifacts(String name, File zip){
        // TODO
    }

    /**
     * Lists suites defined in artifacts.
     *
     * @param name artifacts name
     *
     * @return list of suites
     */
    @Lock(LockType.READ)
    public List<String> getArtifactsQueries(String name){
        if(name == null || name.isEmpty()){
            return null;
        }
        File queries = new File(context.getArtifactDir(name), "queries");
        if(!queries.exists()){
            return null;
        }
        return Arrays.stream(queries.listFiles(f -> f.getName().endsWith(".xml")))
                .map(File::getName).collect(Collectors.toList());
    }

    /**
     * Lists all artifacts names in the system.
     *
     * @return list of artifacts names
     */
    @Lock(LockType.READ)
    public List<String> listArtifacts(){
        return Arrays.stream(context.getArtifactDir(null).listFiles(f -> f.isDirectory() && !f.isHidden()))
                .map(File::getName).collect(Collectors.toList());
    }
}
