//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package pl.wlasiukk.lqpackagebuilder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class BuilderContext {
    private static Logger LOGGER = Logger.getLogger(BuilderContext.class.getName());
    private static final String VERSION = "0.1";
    private static final String DEFAULT_INSTALL_DIRECTORY = "install";
    private static final String DEFAULT_VERSION_CHANGELOG = "versionchangelog.xml";
    private static final String DEFAULT_GIT_LISTING = "X";
    private static final String DEFAULT_SPLIT_EXTENTIONS = ".sql,.tab";
    private String installDirectory;
    private String versionChangeLogName;
    private boolean testOnly;
    private String gitList;
    private String logLevel;
    private String[] splitExtentions;
    private String packageName;
    private String sourceDirectory;
    private String outputDirectory;
    private String fileName;
    private String taskDescription;
    private boolean renumerate;
    private boolean newVersions;

    public boolean isNewVersions() {
        return this.newVersions;
    }

    public void setNewVersions(boolean newVersions) {
        this.newVersions = newVersions;
    }

    public boolean isRenumerate() {
        return this.renumerate;
    }

    public void setRenumerate(boolean renumerate) {
        this.renumerate = renumerate;
    }

    public String[] getSplitExtentions() {
        return this.splitExtentions;
    }

    public void setSplitExtentions(String[] splitExtentions) {
        this.splitExtentions = splitExtentions;
    }

    public boolean isTestOnly() {
        return this.testOnly;
    }

    public void setTestOnly(boolean testOnly) {
        this.testOnly = testOnly;
    }

    public String getGitList() {
        return this.gitList;
    }

    public void setGitList(String gitList) {
        this.gitList = gitList;
    }

    public String getVersionChangeLogName() {
        return this.versionChangeLogName;
    }

    public void setVersionChangeLogName(String versionChangeLogName) {
        this.versionChangeLogName = versionChangeLogName;
    }

    public String getInstallDirectory() {
        return this.installDirectory;
    }

    public void setInstallDirectory(String installDirectory) {
        this.installDirectory = installDirectory;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getSourceDirectory() {
        return this.sourceDirectory;
    }

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public String getOutputDirectory() {
        return this.outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public String getTaskDescription4ID() {
        if (taskDescription == null) return "";
        return taskDescription.split(" ")[0];
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public BuilderContext(String[] args) throws ParseException {
        Options options = new Options();
        Option input = new Option("s", "sourceDirectory", true, "[required] base repository directory");
        input.setRequired(true);
        options.addOption(input);
        Option output = new Option("o", "outputDirectory", true, "[required] output directory (version directory) - with version changelog. Must be relative to <sourceDirectory>");
        output.setRequired(true);
        options.addOption(output);
        Option pname = new Option("p", "packageName", true, "name of package witch changes - ie. branch name [can not be HEAD or MASTER], JIRA etc ");
        options.addOption(pname);
        Option filename = new Option("f", "fileName", true, "changed file name - will be copied to output directory [relative to <sourceDirectory> OR full path OR unique filename with or without parent directory]");
        options.addOption(filename);
        Option gitlist = new Option("g", "gitList", true, "process files reportd by git as M=modified; C=changed; A=added; U=untracked; X=all previous one [default=X]");
        options.addOption(gitlist);
        Option instdir = new Option("i", "installDirectory", true, "folder where install scripts will be generated [default=install]");
        options.addOption(instdir);
        Option vchl = new Option("vc", "versionChangeLogName", true, "name of version change log file - must be in outputDirectory. \nIf not set first *.xml will be used \nIf not found then new versionchangelog.xml will beused");
        options.addOption(vchl);
        Option splitopt = new Option("sp", "splitExtentions", true, "comma delimited file extentions where splitStatements change option will be set to true [.sql,.tab]");
        splitopt.setValueSeparator(',');
        options.addOption(splitopt);
        Option testopt = new Option("t", "testOnly", false, "no changes made, only - printing files to be processed");
        options.addOption(testopt);
        Option renumerateopt = new Option("r", "renumerate", false, "renumerate entries in version change log file");
        options.addOption(renumerateopt);
        Option newVersionopt = new Option("n", "newVersions", false, "if file is already in change, new version will ba added instead of overwritting old one");
        options.addOption(newVersionopt);
        Option loglvl = new Option("l", "logLevel", true, "log level [FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE, ALL, OFF]");
        options.addOption(loglvl);
        Option taskDescription = new Option("td", "taskDescription", true, "task description (ie JIRA number with title)");
        options.addOption(taskDescription);
        if (args.length == 0 || "help".equals(args[0])) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setOptionComparator((Comparator)null);
            formatter.setWidth(160);
            formatter.printHelp("LqPackageBuilder", "Liquibase package creator version " + getVersion(), options, "options f and g are exclusive, f override g", true);
            System.exit(0);
        }

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (Exception var20) {
            LOGGER.log(Level.SEVERE, "{0}", var20.getMessage());
            System.exit(1);
        }

        this.installDirectory = cmd.getOptionValue("installDirectory", "install");
        this.gitList = cmd.getOptionValue("gitList", "X");
        this.versionChangeLogName = cmd.getOptionValue("versionChangeLogName", "versionchangelog.xml");
        this.sourceDirectory = cmd.getOptionValue("sourceDirectory");
        this.outputDirectory = cmd.getOptionValue("outputDirectory");
        this.fileName = cmd.getOptionValue("fileName");
        this.testOnly = cmd.hasOption("testOnly");
        this.setRenumerate(cmd.hasOption("renumerate"));
        this.setNewVersions(cmd.hasOption("newVersions"));
        this.logLevel = cmd.getOptionValue("logLevel");
        this.taskDescription = cmd.getOptionValue("taskDescription");
        if (cmd.hasOption("splitExtentions")) {
            this.splitExtentions = cmd.getOptionValues("splitExtentions");
        } else {
            this.splitExtentions = ".sql,.tab".split(",");
        }

        if (this.logLevel != null) {
            try {
                setLevel(Level.parse(this.logLevel));
                LOGGER = Logger.getLogger(BuilderContext.class.getName());
                LOGGER.log(Level.CONFIG, "set log level to {0}", this.logLevel);
            } catch (IllegalArgumentException var19) {
                LOGGER.log(Level.WARNING, "not recognized log level {0}, using default {1}", new String[]{this.logLevel, LOGGER.getLevel().getName()});
            }
        }

        this.packageName = cmd.getOptionValue("packageName");
        if (!cmd.hasOption("packageName")) {
            String currentBranch = RepositoryHelper.getCurrentBranchName(this);
            if (!currentBranch.equalsIgnoreCase("HEAD") && !currentBranch.equalsIgnoreCase("MASTER")) {
                LOGGER.log(Level.INFO, "package name from branch name will be : {0}", currentBranch);
            } else {
                int nojiraNumber;
                for(nojiraNumber = 1; Files.exists(Paths.get(FileUtils.buildPath(new String[]{this.getFullOutputDirectoryPath(), "NOJIRA_" + String.format("%07d", nojiraNumber)})), new LinkOption[0]); ++nojiraNumber) {
                }

                currentBranch = "NOJIRA_" + String.format("%07d", nojiraNumber);
                LOGGER.log(Level.INFO, "new package auto name will be : {0}", currentBranch);
            }

            this.packageName = currentBranch;
        }

        this.validate();
    }

    private void validate() {
        boolean valid = true;
        if (!Files.exists(Paths.get(this.sourceDirectory), new LinkOption[0])) {
            LOGGER.log(Level.SEVERE, "sourceDirectory {0} does not exists", this.sourceDirectory);
            valid = false;
        }

        if (Files.exists(Paths.get(this.fileName), new LinkOption[0])) {
            // full path
            this.fileName = Paths.get(this.sourceDirectory).toAbsolutePath().relativize(Paths.get(this.fileName)).toString();
            LOGGER.log(Level.INFO, "fileName set to : {0}", this.fileName);
        } else if (this.fileName != null && !Files.exists(Paths.get(this.sourceDirectory + this.fileName), new LinkOption[0])) {
            if (Files.exists(Paths.get(this.sourceDirectory + File.separator + this.fileName), new LinkOption[0])) {
                // path relative to sourceDirectory
                this.fileName = File.separator + this.fileName;
            } else if (this.fileName.startsWith("..") && Files.exists(Paths.get(this.sourceDirectory + File.separator + this.fileName.substring(3)), new LinkOption[0])) {
                // path relative to sourceDirectory with "../" as prefix
                this.fileName = this.fileName.substring(3);
            } else {
                /* trying to find file */
                List<String> files_list = FileUtils.findFileRecurseDown(this.sourceDirectory, this.fileName);
                List<String> files_list2 = files_list.stream().filter(fileName -> !fileName.startsWith( this.sourceDirectory + File.separator + this.outputDirectory)).collect(Collectors.toList());
                if (files_list2.size()==1 && Files.exists(Paths.get(files_list2.get(0)))) {
                    // found 1 file - processing it !
                    LOGGER.log(Level.INFO, "input file {0} found here : {1}", new Object[]{this.fileName, files_list.get(0)});
                    String newFileName = Paths.get(this.sourceDirectory).toAbsolutePath().relativize(Paths.get(files_list2.get(0))).toString();
                    LOGGER.log(Level.INFO, "fileName set to : {0}", newFileName);
                    this.fileName = newFileName;
                } else if (files_list2.size()>0){
                    // found multiple files - error !
                    int maxPrintSize=5;
                    LOGGER.log(Level.SEVERE, "file {0} found in multiple places : \n{1} {2}", new Object[]{this.fileName, String.join("\n", files_list2.stream().limit(maxPrintSize).collect(Collectors.toList())), (files_list2.size()>maxPrintSize?"... of all "+files_list2.size():"")});
                    valid = false;
                } else {
                    LOGGER.log(Level.SEVERE, "file {0} does not exists", this.sourceDirectory + this.fileName);
                    valid = false;
                }
            }
        }

        if ("HEAD".equalsIgnoreCase(this.packageName)) {
            LOGGER.log(Level.SEVERE, "package name can not be HEAD");
            valid = false;
        }

        if ("MASTER".equalsIgnoreCase(this.packageName)) {
            LOGGER.log(Level.SEVERE, "package name can not be MASTER");
            valid = false;
        }

        String fullOutputPath = FileUtils.buildPath(new String[]{this.sourceDirectory, this.outputDirectory});
        if (!Files.exists(Paths.get(fullOutputPath), new LinkOption[0])) {
            LOGGER.log(Level.SEVERE, "outputDirectory {0} does not exists", fullOutputPath);
            valid = false;
        }

        if (!valid) {
            throw new IllegalArgumentException("There was errors in arguments validation");
        }
    }

    public static String getVersion() {
        return "0.1.3";
    }

    public String getFullOutputDirectoryPath() {
        return FileUtils.buildPath(new String[]{this.sourceDirectory, this.outputDirectory});
    }

    public String getChangesetFileName() {
        return this.packageName.replaceAll("\\\\", "_").replaceAll("/", "_") + ".xml";
    }

    private static void setLevel(Level targetLevel) {
        Logger root = Logger.getLogger("");
        root.setLevel(targetLevel);
        Handler[] var2 = root.getHandlers();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Handler handler = var2[var4];
            handler.setLevel(targetLevel);
        }

    }
}
