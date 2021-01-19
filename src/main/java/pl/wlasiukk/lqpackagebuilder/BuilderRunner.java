//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package pl.wlasiukk.lqpackagebuilder;

import org.apache.commons.cli.ParseException;

public class BuilderRunner {
    public BuilderRunner() {
    }

    public static void main(String[] args) {
        String logFile = System.getProperty("java.util.logging.config.file");
        if (logFile == null) {
            System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-6s %2$s %5$s%6$s%n");
        }

        BuilderContext builderContext;
        try {
            builderContext = new BuilderContext(args);
        } catch (ParseException var6) {
            var6.printStackTrace();
            return;
        }

        LqPackageBuilder lqPackageBuilder = new LqPackageBuilder(builderContext);

        try {
            lqPackageBuilder.buildPackage();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }
}