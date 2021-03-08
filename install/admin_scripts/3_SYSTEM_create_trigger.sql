-- run as SYSTEM

CREATE OR REPLACE TRIGGER system.dbarchitect_ddl_trigger
BEFORE DDL
ON  DBARCHITECT.SCHEMA
BEGIN
  IF ora_dict_obj_owner IN ('DBARCHITECT') and USER='DBARCHITECT' 
     and not (ora_dict_obj_name='UTILS' and ora_dict_obj_type in ('PACKAGE','PACKAGE BODY','OBJECT PRIVILEGE'))
  THEN
    raise_application_error(-20001,'Attempt to run script in schema dbarchitect instead of business one. ['||ora_dict_obj_type||' '||ora_dict_obj_name||']');
  END IF;
END dbarchitect_ddl_trigger;
/

