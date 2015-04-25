# --- !Ups
create sequence PROFILES_ID_SEQ;
create table PROFILES(
	ID integer not null default nextval('PROFILES_ID_SEQ'),
	USERNAME varchar(30) not null unique,
	PASSKEY text not null,
	EMAIL text,
	ISADMIN bool NOT NULL DEFAULT false,
	primary key(ID)
);

create sequence PASTES_ID_SEQ;
create table PASTES(
	ID integer not null default nextval('PASTES_ID_SEQ'),
	PASTEID varchar(30) not null unique,
	OWNERID int not null,
	TITLE text,
	CONTENT text,
	ISPRIVATE bool not null default false,
	primary key(ID),
	foreign key(OWNERID) references PROFILES(ID)
);

insert into profiles(ID, USERNAME, PASSKEY, EMAIL, ISADMIN) values(default, 'anon', 'CHANGE_ME', 'anon@sample.com', true)

# --- !Downs
drop table if exists PASTES;
drop table if exists PROFILES;
drop sequence if exists PROFILES_ID_SEQ;
drop sequence if exists PASTES_ID_SEQ;
