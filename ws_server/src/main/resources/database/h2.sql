create table domainevententry (
   globalIndex bigint not null AUTO_INCREMENT,

   aggregateIdentifier varchar(30) not null,
   eventIdentifier varchar(40) not null,
   sequenceNumber bigint not null,
   timestamp bigint default 0 not null,
   templateId int default 0 not null,

   payload BLOB not null,
   primary key(globalIndex)
);

create table snapshotevententry (

	 aggregateIdentifier varchar(30) not null,
   sequenceNumber bigint not null,
   timestamp bigint default 0 not null,
   templateId int default 0 not null,

   payload BLOB not null,

   primary key(aggregateIdentifier, sequenceNumber)

);