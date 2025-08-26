package pl.wlasiukk.lqpackagebuilder;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.*;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class TemplateProcessor {
    public static String run(String templateFile, String sourceFile) throws IOException {
        return run(templateFile, sourceFile, 200);
    }

    public static String run(String templateFile, String sourceFile, int maxSize) throws IOException {
        Path sourcePath = Paths.get(sourceFile);
        byte[] sourceFileBytes = Files.readAllBytes(sourcePath);
        ByteArrayOutputStream compressedLZBytesOutputStream = new ByteArrayOutputStream(1024*10);

        String fileContentHex = toHex(sourceFileBytes);
        ArrayList<String> stringListHex = getStrings(maxSize, fileContentHex);

        byte[] compressedBytes = compress(sourceFileBytes);
        String compressedBytesStringHex = toHex(compressedBytes);
        ArrayList<String> stringListCompressedHex = getStrings(maxSize, compressedBytesStringHex);

        List<String> lines = Files.readAllLines(sourcePath);

        Context context = new Context();
        context.setVariable("sourceFileName", sourcePath.getFileName());
        context.setVariable("sourceFilePath", sourceFile);
        context.setVariable("sourceFileSizeBytes", sourceFileBytes.length);
        context.setVariable("stringListHex", stringListHex);
        context.setVariable("stringListCompressedHex", stringListCompressedHex);
        context.setVariable("linesList", lines);
        context.setVariable("wholeFile", new String(sourceFileBytes));

        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setTemplateMode(TemplateMode.TEXT);
        resolver.setCharacterEncoding("UTF-8");
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        String result = templateEngine.process(templateFile, context);
        return result;
    }

    private static ArrayList<String> getStrings(int maxSize, String inputString) {
        ArrayList<String> stringArrayList = new ArrayList<String>();
        for (int startPositon = 0; startPositon < inputString.length(); startPositon += maxSize) {
            int endPosition = (startPositon + maxSize < inputString.length() ? maxSize : inputString.length() - startPositon);
            stringArrayList.add(inputString.substring(startPositon, startPositon + endPosition));
        }
        return stringArrayList;
    }


    public static String toHex(byte[] b) {
        return String.format("%x", new BigInteger(1, b));
    }


    public static byte[] compress(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return bytes;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(bytes);
        gzip.close();
        return out.toByteArray();
    }
}
