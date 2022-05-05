//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package pl.wlasiukk.lqpackagebuilder;

import com.ximpleware.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChangeLogManager {
    private static final Logger LOGGER = Logger.getLogger(ChangeLogManager.class.getName());
    private static final String FILENAME_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public ChangeLogManager() {
    }

    public static String getCurrentDateAsString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    public static String getOsUser() {
        return System.getProperty("user.name");
    }

    public static String getHostName() {
        try {
            InetAddress localMachine = InetAddress.getLocalHost();
            return localMachine.getHostName();
        } catch (UnknownHostException var2) {
            LOGGER.log(Level.SEVERE, "getHostName encountered problem : {0}", var2.getMessage());
            return "";
        }
    }

    public static String getSplitStatements(BuilderContext builderContext, String filename) {
        String[] extentions = builderContext.getSplitExtentions();
        String[] var3 = extentions;
        int var4 = extentions.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String extention = var3[var5];
            if (filename.endsWith(extention)) {
                return "true";
            }
        }

        return "false";
    }

    public static String getSplitDelimiter(BuilderContext builderContext, String filename) {
        return FileUtils.isApexSource(builderContext, filename) ? "\\n/\\s*\\n|\\n/\\s*$" : ";";
    }

    public static String getRollbackForFile(String fileName, BuilderContext builderContext) throws Exception {
        String rollback = "";
        String status = RepositoryHelper.getFileStatus(fileName);
        if ("M".equals(status) || "C".equals(status)) {
            String rollbackFileName = FileUtils.buildPath(new String[]{builderContext.getFullOutputDirectoryPath(), builderContext.getPackageName(), "rollback", fileName});
            String absoluteRollbackfileDir = Paths.get(rollbackFileName).getParent().toAbsolutePath().toString();
            String lastMainBranchVersion = RepositoryHelper.getLastHeadVersion(builderContext, fileName);
            if (lastMainBranchVersion.length() > 0) {
                FileUtils.createDirectory(absoluteRollbackfileDir, "Rollback directory");
                Files.write(Paths.get(rollbackFileName), lastMainBranchVersion.getBytes(), new OpenOption[]{StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE});
                rollback = "<sqlFile path=\"rollback/" + fileName + "\" relativeToChangelogFile=\"true\" endDelimiter=\"" + getSplitDelimiter(builderContext, fileName) + "\" splitStatements=\"" + getSplitStatements(builderContext, fileName) + "\" />";
            }
        }

        return rollback;
    }

    public static String buildSqlFileChangeSet(String fileName, BuilderContext builderContext, VTDNav vn, String rollback) throws NavException {
        String changeSet = "\n\t<changeSet id=\"" + getNextChangeSetId(builderContext, vn) + "\" author=\"" + getOsUser() + "\" failOnError=\"true\">\n\t\t<!-- " + getCurrentDateAsString() + " " + getOsUser() + "@" + getHostName() + " : LqPackageBuilder:" + BuilderContext.getVersion() + " -->\n\t\t<comment>" + fileName + "</comment>\n\t\t<sqlFile path=\"" + (builderContext.getInstallDirectory() + File.separator + fileName).replace('\\', '/') + "\"  stripComments=\"false\" endDelimiter=\"" + getSplitDelimiter(builderContext, fileName) + "\"  splitStatements=\"" + getSplitStatements(builderContext, fileName) + "\" relativeToChangelogFile=\"true\"/>\n\t\t<rollback>" + rollback + "</rollback>\n\t</changeSet>";
        return changeSet;
    }

    private static String getNextChangeSetId(BuilderContext builderContext, VTDNav vn) throws NavException {
        int newChangeSetIdNumber = 1;

        String changeSetId;
/*
        for(changeSetId = builderContext.getPackageName() + "." + newChangeSetIdNumber; isXpathPresent("//changeSet[@id=\"" + changeSetId + "\"]", vn); changeSetId = builderContext.getPackageName() + "." + newChangeSetIdNumber) {
            ++newChangeSetIdNumber;
        }

 */
        AutoPilot autoPilotChangesets = new AutoPilot(vn);

        try {
            autoPilotChangesets.selectXPath("changeSet");
            int currentId = newChangeSetIdNumber;
            while(autoPilotChangesets.evalXPath() != -1) {
                int attrId = vn.getAttrVal("id");
                String attrIdString = vn.toNormalizedString(attrId);
                String[] attrIdStringSplitted = attrIdString.split("\\.");
                if (attrIdStringSplitted.length>0) {
                    try {
                        currentId = Integer.parseInt(attrIdStringSplitted[attrIdStringSplitted.length - 1]);
                        if (currentId>=newChangeSetIdNumber) {
                            newChangeSetIdNumber=currentId+1;
                        }
                        LOGGER.finer("found currentId="+currentId+"; newChangeSetIdNumber="+newChangeSetIdNumber);
                    } catch (NumberFormatException nfe){
                        // doing nothing, changeset id was nat ended by number - manual modified ?
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("computing NextChangeSetId in safer way, but it is finding first gap in numeration ...");
            for(changeSetId = builderContext.getPackageName() + "." + newChangeSetIdNumber; isXpathPresent("//changeSet[@id=\"" + changeSetId + "\"]", vn); changeSetId = builderContext.getPackageName() + "." + newChangeSetIdNumber) {
                ++newChangeSetIdNumber;
            }
        }
        String newId = builderContext.getPackageName() + "." + newChangeSetIdNumber;
        LOGGER.finer("new id = "+newId);
        return newId;
    }

    public static void addTag(BuilderContext builderContext, String tagName, boolean afterHead) throws Exception {
        String tag = "\n\t<changeSet id=\"" + tagName + "\" author=\"" + getOsUser() + "\">\n\t\t<tagDatabase tag=\"" + tagName + "\" />\n\t</changeSet>\n";
        String changeLogFileName = getPackageChangeLogFileName(builderContext);
        VTDGen vg = new VTDGen();
        XMLModifier xm = new XMLModifier();
        if (!vg.parseFile(changeLogFileName, false)) {
            throw new Exception("XML file parsing problem !");
        } else {
            VTDNav vn = vg.getNav();
            xm.bind(vn);
            if (!isXpathPresent("//tagDatabase[@tag='" + tagName + "']", vn)) {
                if (afterHead) {
                    vn.toElement(0);
                    xm.insertAfterHead(tag);
                } else {
                    vn.toElement(3, "changeSet");
                    xm.insertBeforeElement(tag);
                }
            }

            if (!builderContext.isTestOnly()) {
                xm.output(new FileOutputStream(changeLogFileName));
            }

            vg.clear();
        }
    }

    public static void addSqlFileChangeset(BuilderContext builderContext, String fileName, String originalFileName) throws Exception {
        String changeLogFileName = getPackageChangeLogFileName(builderContext);
        VTDGen vg = new VTDGen();
        XMLModifier xm = new XMLModifier();
        if (!vg.parseFile(changeLogFileName, false)) {
            throw new Exception("XML file parsing problem !");
        } else {
            VTDNav vn = vg.getNav();
            xm.bind(vn);
            String changeSet = buildSqlFileChangeSet(fileName, builderContext, vn, getRollbackForFile(originalFileName, builderContext));
            if (isXpathPresent("//sqlFile[@path='" + builderContext.getInstallDirectory() + "/" + fileName + "']", vn)) {
                LOGGER.log(Level.FINER, "there is changeset with that sqlfile : {0}", fileName);
            } else {
                vn.toElement(0);
                if (vn.toElement(3, "changeSet")) {
                    xm.insertAfterElement(changeSet);
                } else {
                    xm.insertAfterHead(changeSet);
                }

                if (!builderContext.isTestOnly()) {
                    xm.output(new FileOutputStream(changeLogFileName));
                }

            }
        }
    }

    private static String getPackageChangeLogFileName(BuilderContext builderContext) {
        return FileUtils.buildPath(new String[]{builderContext.getSourceDirectory(), builderContext.getOutputDirectory(), builderContext.getPackageName(), builderContext.getChangesetFileName()});
    }

    public static void createPackageChangeLogFile(BuilderContext builderContext) throws IOException, URISyntaxException {
        if (!builderContext.isTestOnly()) {
            String changesetFileName = getPackageChangeLogFileName(builderContext);
            Path changesetPath = Paths.get(changesetFileName);
            if (!Files.exists(changesetPath, new LinkOption[0])) {
                createFileFromResource(changesetPath);
            }
        }
    }

    private static void createFileFromResource(Path changesetPath) throws IOException, URISyntaxException {
        createFileFromResource(changesetPath, "resources/changelog.xml");
    }

    private static void createFileFromResource(Path changesetPath, String resourceName) throws IOException, URISyntaxException {
        ResourceManager rm = new ResourceManager();
        String changelogString = rm.getResourceAsString(resourceName);
        Files.write(changesetPath, changelogString.getBytes(), new OpenOption[]{StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE});
    }

    public static String findVersionChangeLog(BuilderContext builderContext) throws IOException, URISyntaxException {
        String fn;
        try {
            fn = FileUtils.findOneFileByName(FileUtils.buildPath(new String[]{builderContext.getSourceDirectory(), builderContext.getOutputDirectory()}), "(.+).xml");
        } catch (NoSuchElementException var3) {
            fn = builderContext.getVersionChangeLogName();
            createFileFromResource(Paths.get(FileUtils.buildPath(new String[]{builderContext.getOutputDirectory(), fn})));
        }

        return fn;
    }

    public static String buildInclude(String versionchangeset) throws IOException, URISyntaxException {
        String includeSet = "\n\t<include file=\"" + versionchangeset + "\" relativeToChangelogFile=\"true\"/>";
        return includeSet;
    }

    public static void addVersionInclude(BuilderContext builderContext) throws Exception {
        String versionchangeset = FileUtils.buildPath(new String[]{builderContext.getPackageName(), builderContext.getChangesetFileName()});
        String includeSet = buildInclude(versionchangeset);
        String changeLogFileName = findVersionChangeLog(builderContext);
        VTDGen vg = new VTDGen();
        XMLModifier xm = new XMLModifier();
        if (!vg.parseFile(changeLogFileName, false)) {
            throw new Exception("XML file parsing problem !");
        } else {
            VTDNav vn = vg.getNav();
            xm.bind(vn);
            if (isXpathPresent("//include[@file='" + versionchangeset + "']", vn)) {
                LOGGER.log(Level.FINER, "that change file is already included in version file [{0}]", versionchangeset);
            } else {
                vn.toElement(0);
                if (vn.toElement(3, "include")) {
                    xm.insertAfterElement(includeSet);
                } else {
                    xm.insertAfterHead(includeSet);
                }

                if (!builderContext.isTestOnly()) {
                    xm.output(new FileOutputStream(changeLogFileName));
                }

            }
        }
    }

    private static boolean isXpathPresent(String xpath, VTDNav vn) throws NavException {
        AutoPilot ap = new AutoPilot(vn);
        boolean pathPresent = false;

        try {
            ap.selectXPath(xpath);
            pathPresent = ap.evalXPath() != -1;
        } catch (Exception var5) {
            LOGGER.log(Level.SEVERE, "xpath={0}", xpath);
            var5.printStackTrace();
        }

        return pathPresent;
    }

    public static void renumerate(BuilderContext builderContext) throws Exception {
        String changeLogFileName = getPackageChangeLogFileName(builderContext);
        String renumeratedXml = renumerate(FileUtils.readFile(changeLogFileName));
        if (!builderContext.isTestOnly()) {
            Files.write(Paths.get(changeLogFileName), renumeratedXml.getBytes(), new OpenOption[]{StandardOpenOption.TRUNCATE_EXISTING});
        }

    }

    public static String renumerate(String xmlSource) throws Exception {
        new String(xmlSource);
        VTDGen vg = new VTDGen();
        XMLModifier xm = new XMLModifier();
        vg.setDoc(xmlSource.getBytes());
        vg.parse(false);
        VTDNav vn = vg.getNav();
        xm.bind(vn);
        AutoPilot autoPilotChangesets = new AutoPilot(vn);
        autoPilotChangesets.selectXPath("changeSet");
        int idCounter = 0;

        while(autoPilotChangesets.evalXPath() != -1) {
            int attrId = vn.getAttrVal("id");
            String attrIdString = vn.toNormalizedString(attrId);
            int dotposition = attrIdString.lastIndexOf(46);
            if (dotposition >= 0) {
                ++idCounter;
                String newAttrIdValue = attrIdString.substring(0, attrIdString.lastIndexOf(46));
                newAttrIdValue = newAttrIdValue + "." + idCounter;
                if (!attrIdString.equals(newAttrIdValue)) {
                    xm.updateToken(attrId, newAttrIdValue);
                    LOGGER.log(Level.FINE, "updated id from {0} to {1}", new String[]{attrIdString, newAttrIdValue});
                } else {
                    LOGGER.log(Level.FINEST, "leaving the same id {0}", attrIdString);
                }
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        xm.output(baos);
        String retXml = baos.toString();
        vg.clear();
        return retXml;
    }
}
