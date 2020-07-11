/*==============================================================*/
/* DBMS name:      Sybase SQL Anywhere 12                       */
/* Created on:     2015/9/26 22:22:03                           */
/*==============================================================*/

create tablespace MBMS datafile 'c:\MBMS.dbf' size 50m autoextend on next
50m maxsize unlimited;
create user hyb identified by hyb default tablespace MBMS;
grant connect,resource to hyb;
conn hyb/hyb;

--删除外键约束
alter table MBMS_bill DROP CONSTRAINT FK_BILL_REF_SUPPLIER;

--删除序列
DROP SEQUENCE seq_bill;
DROP SEQUENCE seq_supplier;
DROP SEQUENCE seq_user;

--删除表
DROP TABLE MBMS_bill;
DROP TABLE MBMS_user;
DROP TABLE MBMS_supplier;

/*==============================================================*/
/* Table: Bill                                                  */
/*==============================================================*/
create table MBMS_bill
(
   billid               NUMBER(10)                 not null,
   name                 varchar(20)                    null,
   amount               NUMBER(10)                        null,
   money                NUMBER(10)                        null,
   supplier             NUMBER(10)                        null,
   description          varchar(500)                   null,
   createDate           date                           null,
   payOrNot             NUMBER(1)                        null,
   constraint PK_BILLID primary KEY (billid)
);

/*==============================================================*/
/* Table: supplier                                              */
/*==============================================================*/
create table MBMS_supplier 
(
   supplierid           NUMBER(10)                 not null,
   name                 varchar(20)                    null,
   description          varchar(500)                   null,
   contact              varchar(20)                    null,
   phone                NUMBER(20)                    null,
   fax                  NUMBER(20)                    null,
   address              varchar(100)                   null,
   constraint PK_SUPPLIER primary KEY (supplierid)
);

/*==============================================================*/
/* Table: "user"                                                */
/*==============================================================*/
create table MBMS_user
(
   userid               NUMBER(4)                   NOT NULL,
   loginname            varchar(16)                 not null,
   password             varchar(16)                    null,
   name                 varchar(20)                    null,
   sex                  NUMBER(1)                     null,
   age                  NUMBER(3)                      null,
   phone                NUMBER(11)                    null,
   address              varchar(50)                    null,
   authority            NUMBER(4)                        null,
   constraint PK_USERid primary KEY (userid)
);

alter table MBMS_bill
   add constraint FK_BILL_REF_SUPPLIER foreign key (supplier)
      references MBMS_supplier (supplierid);

/*==============================================================*/
/*插入数据                                                      */
/*==============================================================*/


--插入供应商数据
CREATE SEQUENCE seq_supplier;
BEGIN
  FOR I IN 1 .. 22 LOOP
    INSERT INTO Mbms_Supplier
    VALUES(
           seq_supplier.nextval,
           DBMS_RANDOM.STRING('l', 10),
           DBMS_RANDOM.STRING('l', 50),
           DBMS_RANDOM.STRING('l', 20),
           TRUNC(DBMS_RANDOM.VALUE(10000000000, 19999999999)),
           TRUNC(DBMS_RANDOM.VALUE(10000000000, 19999999999)),
           DBMS_RANDOM.STRING('l', 50)
    );
    END LOOP;
END;

--插入账单数据
CREATE SEQUENCE seq_bill;
BEGIN
  FOR I IN 1 .. 32 LOOP
      INSERT INTO MBMS_BILL
      VALUES
        (SEQ_BILL.NEXTVAL,
         DBMS_RANDOM.STRING('l', 8),
         TRUNC(DBMS_RANDOM.VALUE(1, 100)),
         TRUNC(DBMS_RANDOM.VALUE(1, 100)),
         --随机选择一个supplierid插入
         (
         select supplierid from
            (
              select * from MBMS_supplier
              order by dbms_random.value
            )
         where rownum <= 1
          ),
       --随机选择一个supplierid插入
         DBMS_RANDOM.STRING('l', 50),
         SYSDATE,
         SIGN(SIGN(dbms_random.random)+1));     --随机插入0或1
    END LOOP;
END;
--插入user用户和admin用户
CREATE SEQUENCE seq_user;
INSERT INTO mbms_user VALUES(
           seq_user.nextval,
           'user',
           'user',
           DBMS_RANDOM.STRING('l', 6),
           SIGN(SIGN(dbms_random.random)+1),
           TRUNC(DBMS_RANDOM.VALUE(10, 80)),
           TRUNC(DBMS_RANDOM.VALUE(10000000000, 19999999999)),
           DBMS_RANDOM.STRING('l', 50),
           0
    );
INSERT INTO mbms_user VALUES(
           seq_user.nextval,
           'admin',
           'admin',
           DBMS_RANDOM.STRING('l', 6),
           SIGN(SIGN(dbms_random.random)+1),
           TRUNC(DBMS_RANDOM.VALUE(10, 80)),
           TRUNC(DBMS_RANDOM.VALUE(10000000000, 19999999999)),
           DBMS_RANDOM.STRING('l', 50),
           1
    );
--随机插入多组用户数据

BEGIN
  FOR I IN 1 .. 12 LOOP
    INSERT INTO mbms_user
    VALUES(
           seq_user.nextval,
           DBMS_RANDOM.STRING('l', 6),
           DBMS_RANDOM.STRING('l', 6),
           DBMS_RANDOM.STRING('l', 6),
           SIGN(SIGN(dbms_random.random)+1),
           TRUNC(DBMS_RANDOM.VALUE(10, 80)),
           TRUNC(DBMS_RANDOM.VALUE(10000000000, 19999999999)),
           DBMS_RANDOM.STRING('l', 50),
           SIGN(SIGN(dbms_random.random)+1)
    );
    END LOOP;
END;

commit;

SELECT * FROM MBMS_bill;
SELECT * FROM MBMS_user;
SELECT * FROM MBMS_supplier;

















