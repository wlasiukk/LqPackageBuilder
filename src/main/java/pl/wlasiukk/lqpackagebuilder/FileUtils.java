//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package pl.wlasiukk.lqpackagebuilder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
    private static final Logger LOGGER = Logger.getLogger(FileUtils.class.getName());
    private static String lastFoundFile;

    public FileUtils() {
    }

    public static String buildPath(String[] paths) {
        String dirSeparator = File.separator;
        String outStr = "";
        String lastPath = "";
        int pathNr = 0;
        String[] paths_arr = paths;
        int paths_arr_length = paths_arr.length;

        for(int i = 0; i < paths_arr_length; ++i) {
            String path = paths_arr[i];
            ++pathNr;
            if (pathNr > 1 && !path.startsWith(dirSeparator) && !lastPath.endsWith(dirSeparator)) {
                outStr = outStr + dirSeparator;
            }

            outStr = outStr + path;
            lastPath = path;
        }

        return outStr;
    }

    public static void createDirectory(String directoryPath, String directoryName) {
        if (!Files.exists(Paths.get(directoryPath), new LinkOption[0])) {
            boolean dirCreated = (new File(directoryPath)).mkdirs();
            if (dirCreated) {
                LOGGER.log(Level.FINEST, "{0} created : {1} ", new Object[]{directoryName, directoryPath});
            }
        }

    }

    public static String findOneFileByName(String directoryName, String fileNameRegexp) {
        if (directoryName == null) {
            throw new NoSuchElementException("File " + fileNameRegexp + " not found, empty directory");
        } else {
            File dir = new File(directoryName);
            File[] matches = dir.listFiles(new FileUtilsFilter(fileNameRegexp));
            if (matches != null && matches.length > 0) {
                return matches[0].getAbsolutePath();
            } else {
                throw new NoSuchElementException("File " + fileNameRegexp + " not found in directory " + directoryName);
            }
        }
    }

    public static boolean dirHaveFile(String directoryName, String fileNameRegexp) {
        boolean foundFile = false;

        try {
            lastFoundFile = findOneFileByName(directoryName, fileNameRegexp);
            foundFile = true;
        } catch (NoSuchElementException var4) {
            foundFile = false;
        }

        return foundFile;
    }

    public static String findFileRecurseUp(String directoryName, String fileNameRegexp) {
        if (directoryName == null) {
            throw new NoSuchElementException("File " + fileNameRegexp + " not found, empty directory ");
        } else {
            String currentDir = directoryName;
            new File(directoryName);

            while(currentDir != null && !dirHaveFile(currentDir, fileNameRegexp)) {
                File dir = new File(currentDir);
                currentDir = dir.getParent();
            }

            if (currentDir == null) {
                throw new NoSuchElementException("File " + fileNameRegexp + " not found in directory " + directoryName);
            } else {
                return currentDir;
            }
        }
    }

    public static List<String> findFileRecurseDown(String directoryName, String fileName) {
        List<String> files_list = new ArrayList<String>();
        Path startPath = Paths.get(directoryName);
        try (Stream<Path> stream = Files.walk(startPath, Integer.MAX_VALUE)) {
            List<String> collect = stream
                    .map(String::valueOf)
                    .sorted()
                    .filter(filename -> filename.endsWith(File.separator+fileName))
                    .collect(Collectors.toList())
                    ;
            files_list.addAll(collect);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return files_list;
    }

    public static boolean isThereFileInParentDirectories(String directoryName, String fileNameRegexp) {
        lastFoundFile = null;
        try {
            findFileRecurseUp(directoryName, fileNameRegexp);
            return true;
        } catch (NoSuchElementException var3) {
            return false;
        }
    }

    static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    static boolean isApexSource(BuilderContext builderContext, String filename) {
        String fullInputFilePath = buildPath(new String[]{builderContext.getSourceDirectory(), filename});
        return isThereFileInParentDirectories((new File(fullInputFilePath)).getParent(), "apex_source");
    }

    static boolean isTemplateProvided(BuilderContext builderContext, String filename) {
        String fullInputFilePath = buildPath(new String[]{builderContext.getSourceDirectory(), filename});
        return isThereFileInParentDirectories((new File(fullInputFilePath)).getParent(), "template");
    }

    static String getLastFoundFile(){
        return lastFoundFile;
    }

    static String getFileExtension(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf(46);
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }

        return extension;
    }

    static String getFileNameWithoutExtension(String fileName) {
        String extension = getFileExtension(fileName);
        return extension != null && extension.length() > 0 ? fileName.substring(0, fileName.length() - extension.length()) : fileName;
    }



    final static class FileUtilsFilter implements FilenameFilter {
        private String fileNameRegexp;

        FileUtilsFilter(String var1) {
            this.fileNameRegexp = var1;
        }

        public boolean accept(File dir, String name) {
            return name.matches(this.fileNameRegexp);
        }
    }

}
