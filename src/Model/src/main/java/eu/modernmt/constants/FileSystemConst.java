package eu.modernmt.constants;

import java.io.File;

/**
 * Created by davide on 24/02/16.
 */
public class FileSystemConst {

    public final File home;
    public final File engines;
    public final File runtime;
    public final File resources;
    public final File lib;

    private static final String SYSPROP_MMT_HOME = "mmt.home";

    FileSystemConst() {
        String home = System.getProperty(SYSPROP_MMT_HOME);
        if (home == null)
            throw new IllegalStateException("The system property '" + SYSPROP_MMT_HOME + "' must be initialized to the path of MMT installation.");

        this.home = new File(home).getAbsoluteFile();
        if (!this.home.isDirectory())
            throw new IllegalStateException("Invalid path for property '" + SYSPROP_MMT_HOME + "': " + this.home + " must be a valid directory.");

        this.engines = new File(this.home, "engines");
        if (!this.engines.isDirectory())
            throw new IllegalStateException("Invalid path for property '" + SYSPROP_MMT_HOME + "': " + this.engines + " must be a valid directory.");

        File build = new File(this.home, "build");

        this.lib = new File(build, "lib");
        if (!this.lib.isDirectory())
            throw new IllegalStateException("Invalid path for property '" + SYSPROP_MMT_HOME + "': " + this.lib + " must be a valid directory.");

        this.resources = new File(build, "res");
        if (!resources.isDirectory())
            throw new IllegalStateException("Invalid path for property '" + SYSPROP_MMT_HOME + "': " + this.resources + " must be a valid directory.");

        this.runtime = new File(this.home, "runtime");
        if (!this.runtime.isDirectory())
            throw new IllegalStateException("Invalid path for property '" + SYSPROP_MMT_HOME + "': " + this.runtime + " must be a valid directory.");
    }

}
