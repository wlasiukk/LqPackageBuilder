//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package pl.wlasiukk.lqpackagebuilder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

public class OneFileProcessor {
    private static final Logger LOGGER = Logger.getLogger(OneFileProcessor.class.getName());
    private BuilderContext builderContext;

    public OneFileProcessor(BuilderContext builderContext) {
        this.builderContext = builderContext;
    }

    public void processFile(String fileName) throws Exception {
        String originalFileName = fileName;
        String fullInputFilePath = FileUtils.buildPath(new String[]{this.builderContext.getSourceDirectory(), fileName});
        String fullOutputFilePath = FileUtils.buildPath(new String[]{this.builderContext.getSourceDirectory(), this.builderContext.getOutputDirectory(), this.builderContext.getPackageName(), this.builderContext.getInstallDirectory(), fileName});
        if (!this.builderContext.isTestOnly()) {
            this.createOutputDirectory(fullOutputFilePath);
            String initFolder;
            if (this.builderContext.isNewVersions() && Files.exists(Paths.get(fullOutputFilePath), new LinkOption[0])) {
                LOGGER.fine("file " + fileName + " already exists, creating new version in change");
                int fileVersion = 1;
                initFolder = FileUtils.getFileExtension(fileName);
                String fileNameWithoutExtension = FileUtils.getFileNameWithoutExtension(fileName);

                while (Files.exists(Paths.get(fullOutputFilePath), new LinkOption[0])) {
                    ++fileVersion;
                    fileName = fileNameWithoutExtension + "v" + fileVersion + "." + initFolder;
                    fullOutputFilePath = FileUtils.buildPath(new String[]{this.builderContext.getSourceDirectory(), this.builderContext.getOutputDirectory(), this.builderContext.getPackageName(), this.builderContext.getInstallDirectory(), fileName});
                    LOGGER.finest("trying new version of file " + fileName);
                }

                Files.copy(Paths.get(fullInputFilePath), Paths.get(fullOutputFilePath));
            } else {
                Files.copy(Paths.get(fullInputFilePath), Paths.get(fullOutputFilePath), StandardCopyOption.REPLACE_EXISTING);
            }

            if (FileUtils.isApexSource(this.builderContext, fileName)) {
                LOGGER.info("APEX source detected - applying converter");
                String initSource = "";

                try {
                    initFolder = FileUtils.findFileRecurseUp(fullInputFilePath, "init.sql");
                    byte[] initFileBytes = Files.readAllBytes(Paths.get(FileUtils.buildPath(new String[]{initFolder, "init.sql"})));
                    initSource = new String(initFileBytes);
                } catch (NoSuchElementException var9) {
                    LOGGER.info("init.sql file not found");
                }

                Files.write(
                        Paths.get(fullOutputFilePath),
                        (OracleApexFileConverter.convertApexSqlToLiquibaseFormat(initSource) +
                                "\n\n\nbegin\n  wwv_flow_api.g_mode := 'REPLACE';\nend;\n/\n\n\n" +
                                OracleApexFileConverter.convertApexSqlToLiquibaseFormat(FileUtils.readFile(fullOutputFilePath))
                        ).getBytes(),
                        new OpenOption[]{StandardOpenOption.TRUNCATE_EXISTING});
            }
            if (FileUtils.isTemplateProvided(this.builderContext, fileName)) {
                String templateFileName = FileUtils.getLastFoundFile();
                LOGGER.info("TEMPLATE source detected - applying converter");
                Files.write(
                        Paths.get(fullOutputFilePath),
                        TemplateProcessor.run(templateFileName, fullOutputFilePath).getBytes(StandardCharsets.UTF_8),
                        new OpenOption[]{StandardOpenOption.TRUNCATE_EXISTING}
                );
            }
        }

        ChangeLogManager.createPackageChangeLogFile(this.builderContext);
        ChangeLogManager.addTag(this.builderContext, this.builderContext.getPackageName() + "_pre", true);
        ChangeLogManager.addSqlFileChangeset(this.builderContext, fileName, originalFileName);
        ChangeLogManager.addVersionInclude(this.builderContext);
        if (this.builderContext.isRenumerate()) {
            ChangeLogManager.renumerate(this.builderContext);
        }

    }

    private void createOutputDirectory(String fileName) {
        if (!this.builderContext.isTestOnly()) {
            FileUtils.createDirectory((new File(fileName)).getParentFile().getAbsolutePath(), "Output Directory");
        }
    }
}
