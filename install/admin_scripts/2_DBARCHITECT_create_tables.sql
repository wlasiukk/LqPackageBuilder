-- Skrypt powinien zostac uruchomiony w kontekscie uzytkownika DBARCHITECT

create table dbarchitect.DATABASECHANGELOG
(
  id            VARCHAR2(255) not null,
  author        VARCHAR2(255) not null,
  filename      VARCHAR2(255) not null,
  dateexecuted  TIMESTAMP(6) not null,
  orderexecuted NUMBER(10) not null,
  exectype      VARCHAR2(10) not null,
  md5sum        VARCHAR2(35),
  description   VARCHAR2(255),
  comments      VARCHAR2(255),
  tag           VARCHAR2(255),
  liquibase     VARCHAR2(20),
  contexts  VARCHAR2(255),
  labels  VARCHAR2(255),
  deployment_id  VARCHAR2(10)
);

create table dbarchitect.DATABASECHANGELOGLOCK
(
  id          NUMBER(10) not null,
  locked      NUMBER(1) not null,
  lockgranted TIMESTAMP(6),
  lockedby    VARCHAR2(255)
) ;

alter table dbarchitect.DATABASECHANGELOGLOCK
  add constraint PK_DATABASECHANGELOGLOCK primary key (ID)
  using index;
