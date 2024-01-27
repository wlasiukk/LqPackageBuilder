//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package pl.wlasiukk.lqpackagebuilder;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LqPackageBuilder {
    private static final Logger LOGGER = Logger.getLogger(LqPackageBuilder.class.getName());
    private BuilderContext builderContext;

    public LqPackageBuilder(BuilderContext builderContext) {
        this.builderContext = builderContext;
    }

    public void buildPackage() throws Exception {
        OneFileProcessor oneFileProcessor = new OneFileProcessor(this.builderContext);
        Set changedFiles;
        if (this.builderContext.getFileName() != null && !this.builderContext.getFileName().isEmpty()) {
            changedFiles = new HashSet();
            changedFiles.add(this.builderContext.getFileName());
            RepositoryHelper.setOneChangedFile(this.builderContext.getFileName(), "M");
        } else {
            changedFiles = RepositoryHelper.getChangedFiles(this.builderContext);
        }

        Iterator changedFilesIterator = ((Set)changedFiles).iterator();

        while(changedFilesIterator.hasNext()) {
            String changedFile = (String)changedFilesIterator.next();
            LOGGER.log(Level.INFO, "processing {0}", changedFile);
            oneFileProcessor.processFile(changedFile);
        }

    }
}
