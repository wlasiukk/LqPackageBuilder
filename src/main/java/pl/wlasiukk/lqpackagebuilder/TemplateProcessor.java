package pl.wlasiukk.lqpackagebuilder;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.*;


import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TemplateProcessor {
    public static String run(String templateFile, String sourceFile) throws IOException {
        return run(templateFile, sourceFile, 200);
    }

    public static String run(String templateFile, String sourceFile, int maxSize) throws IOException {
        Path sourcePath = Paths.get(sourceFile);
        byte[] sourceFileBytes = Files.readAllBytes(sourcePath);

        String fileContentHex = toHex(sourceFileBytes);
        ArrayList<String> stringListHex = new ArrayList<String>();
        for (int startPositon = 0; startPositon < fileContentHex.length(); startPositon += maxSize) {
            int endPosition = (startPositon + maxSize < fileContentHex.length() ? maxSize : fileContentHex.length() - startPositon);
            stringListHex.add(fileContentHex.substring(startPositon, startPositon + endPosition));
        }

        List<String> lines = Files.readAllLines(sourcePath);

        Context context = new Context();
        context.setVariable("sourceFileName", sourcePath.getFileName());
        context.setVariable("sourceFilePath", sourceFile);
        context.setVariable("sourceFileSizeBytes", sourceFileBytes.length);
        context.setVariable("stringListHex", stringListHex);
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


    public static String toHex(byte[] b) {
        return String.format("%x", new BigInteger(1, b));
    }
}
