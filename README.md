# LqPackageBuilder
Liquibase automation tool, focused on SQL and ORACLE APEX


---
To use templating when providing changeSet files there must be file named "template" in source file folder or any parent folder

When using template following variable are available :
 - sourceFileName
 - sourceFileSizeBytes
 - stringListHex - file changed to Hex and then divided to 200 length pieces - usefull when using wwv_flow_imp.g_varchar2_table in ORACLE  
 - linesList - file divided to lines 
 - wholeFile