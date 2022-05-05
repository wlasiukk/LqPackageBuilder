//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package pl.wlasiukk.lqpackagebuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

public class RepositoryHelper {
    private static final Logger LOGGER = Logger.getLogger(RepositoryHelper.class.getName());
    private static HashMap<String, String> fileList;

    public RepositoryHelper() {
    }

    public static String getFileStatus(String filename) throws Exception {
        String status = "";
        if (fileList == null) {
            throw new Exception("fileList not populated !");
        } else {
            status = (String)fileList.get(filename);
            return status;
        }
    }

    private static String getGitListing(BuilderContext builderContext) {
        String gitListing = builderContext.getGitList();
        if ("X".equalsIgnoreCase(gitListing)) {
            gitListing = "MCAU";
        }

        return gitListing;
    }

    private static Set<String> filterFiles(Set<String> files, String outputDirectory) {
        String outputDirectoryPath = Paths.get(outputDirectory).toString();
        Set<String> retSet = new HashSet();
        LOGGER.log(Level.FINEST, "filter condition : startsWith({0})", outputDirectoryPath);
        Iterator var4 = files.iterator();

        while(var4.hasNext()) {
            String file = (String)var4.next();
            if (!Paths.get(file).toString().startsWith(outputDirectoryPath)) {
                retSet.add(file);
                LOGGER.log(Level.FINEST, "included {0}", Paths.get(file).toString());
            } else {
                LOGGER.log(Level.FINEST, "excluded {0}", Paths.get(file).toString());
            }
        }

        return retSet;
    }

    private static Repository getRepository(BuilderContext builderContext) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        String repoPath = FileUtils.findFileRecurseUp(builderContext.getSourceDirectory(), ".git") + File.separator + ".git";
        Repository repo = ((FileRepositoryBuilder)((FileRepositoryBuilder)builder.setGitDir(new File(repoPath))).setMustExist(true)).build();
        return repo;
    }

    public static String getCurrentBranchName(BuilderContext builderContext) {
        String repoName = "";

        try {
            Repository repo = getRepository(builderContext);
            repoName = repo.getBranch();
        } catch (IOException var3) {
            var3.printStackTrace();
        }

        return repoName;
    }

    private static void copyToHashMap(Set<String> keys, String value, HashMap<String, String> hm) {
        Iterator var3 = keys.iterator();

        while(var3.hasNext()) {
            String key = (String)var3.next();
            hm.put(key, value);
        }

    }

    public static void setOneChangedFile(String filename, String changeType) {
        fileList = new HashMap();
        fileList.put(filename, changeType);
    }

    public static Set<String> getChangedFiles(BuilderContext builderContext) {
        Set<String> retSet = new HashSet();
        fileList = new HashMap();

        try {
            Repository repo = getRepository(builderContext);
            Git git = new Git(repo);
            Status status = git.status().call();
            String gitListing = getGitListing(builderContext);
            Set list;
            if (gitListing.contains("M")) {
                list = status.getModified();
                copyToHashMap(list, "M", fileList);
                retSet.addAll(list);
            }

            if (gitListing.contains("C")) {
                list = status.getChanged();
                copyToHashMap(list, "C", fileList);
                retSet.addAll(list);
            }

            if (gitListing.contains("A")) {
                list = status.getAdded();
                copyToHashMap(list, "A", fileList);
                retSet.addAll(list);
            }

            if (gitListing.contains("U")) {
                list = status.getUntracked();
                copyToHashMap(list, "U", fileList);
                retSet.addAll(list);
            }
        } catch (IOException var7) {
            var7.printStackTrace();
        } catch (NoWorkTreeException var8) {
            var8.printStackTrace();
        } catch (GitAPIException var9) {
            var9.printStackTrace();
        }

        return filterFiles(retSet, builderContext.getOutputDirectory());
    }

    public static String getLastHeadVersion(BuilderContext builderContext, String file) throws IOException {
        Repository localRepository = getRepository(builderContext);
        Ref head = localRepository.exactRef("refs/heads/master");
        if (head == null){
            return ""; // not using master ?? TODO
        }
        String s = fetchBlob(localRepository, head.getName(), file);
        return s;
    }

    private static String fetchBlob(Repository repository, String revSpec, String path) throws MissingObjectException, IncorrectObjectTypeException, IOException {
        ObjectId id = repository.resolve(revSpec);
        ObjectReader reader = repository.newObjectReader();
        RevWalk walk = null;

        String var9;
        try {
            walk = new RevWalk(reader);
            RevCommit commit = walk.parseCommit(id);
            RevTree tree = commit.getTree();
            TreeWalk treewalk = TreeWalk.forPath(reader, path, new AnyObjectId[]{tree});
            if (treewalk != null) {
                byte[] data = reader.open(treewalk.getObjectId(0)).getBytes();
                String var10 = new String(data, "utf-8");
                return var10;
            }

            var9 = "";
        } finally {
            walk.dispose();
            reader.close();
        }

        return var9;
    }
}
