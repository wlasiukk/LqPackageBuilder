//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package pl.wlasiukk.lqpackagebuilder;

import java.io.IOException;

public class OracleApexFileConverter {
    public static final String endDelimeter = "/";
    static final String eofl = "\n";

    public OracleApexFileConverter() {
    }

    public static void convertApexSqlFileToLiquibaseFormat(String filename) throws IOException {
        String src = FileUtils.readFile(filename);
    }

    public static String convertApexSqlToLiquibaseFormat(String fileSource) {
        String ret = fileSource.replaceAll("(?mi)^set.*", "\n");
        ret = ret.replaceAll("(?mi)^end.*(\\n)*/", "\nend;\n/\n");
        ret = ret.replaceAll("(\n|^)(ALTER SESSION.*);\n", "\n\\1 \\2\n/\n");
        ret = ret.replaceAll("(?mi)^prompt.*", "\n");
        ret = ret.replaceAll("(?mi)^--.*", "\n");
        ret = ret.replaceAll("(?mi)^WHENEVER.*", "\n");
        ret = ret.replaceAll("(\n|^)Content-type: application/text\n", "\n");
        ret = ret.replaceAll("(\n|^)X-DB-Content-length: .*\n", "\n");
        ret = ret.replaceAll("(?mi)^\n\n", "");
        return ret;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(convertApexSqlToLiquibaseFormat(FileUtils.readFile("c:\\tmp\\git\\repo_lcore\\test_apex\\workspace_100001\\f999\\application\\pages\\page_00002.sql")));
        System.out.println("X");
    }
}
