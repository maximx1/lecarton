# --- !Ups
create sequence profiles_id_seq;
create table profiles(
	id integer not null default nextval('profiles_id_seq'),
	username varchar(30) not null unique,
	passkey text not null,
	email text,
	isAdmin bool NOT NULL DEFAULT false,
	primary key(id)
);

create sequence pastes_id_seq;
create table pastes(
	id integer not null default nextval('pastes_id_seq'),
	pasteid varchar(30) not null unique,
	ownerid int not null,
	title text,
	content text,
	isprivate bool not null default false,
	primary key(id),
	foreign key(ownerid) references profiles(id)
);

insert into profiles(id, username, passkey, email, isadmin) values(default, 'anon', '$2a$04$kdRLRHJXp8T6tgfSdVIzIOZxFCgCzlx4pXs0vKNcDHNfhRgd4vQXK', 'anon@sample.com', true)

# --- !Downs
drop table if exists pastes;
drop table if exists profiles;
drop sequence if exists profiles_id_seq;
drop sequence if exists pastes_id_seq;
