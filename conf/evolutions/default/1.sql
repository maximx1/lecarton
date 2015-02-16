# --- !Ups
create sequence profiles_id_seq;
create table profiles(
	id int unsigned not null default nextval('profiles_id_seq'),
	username varchar(30) not null unique,
	password text unsigned not null,
	email text,
	primary key(id)
);

create sequence pastes_id_seq;
create table pastes(
	id int unsigned not null default nextval('pastes_id_seq'),
	pasteId varchar(30) not null unique,
	ownerId int unsigned not null,
	title text,
	content text,
	isPrivate bool not null default false,
	primary key(id),
	foreign key(ownerId) references profiles(id)
);

insert into profiles(id, username, password, email) values(default, 'anon', 'CHANGE_ME', 'anon@sample.com')

# --- !Downs
drop table if exists pastes;
drop table if exists profiles;
drop sequence if exists profiles_id_seq;
drop sequence if exists pastes_id_seq;
