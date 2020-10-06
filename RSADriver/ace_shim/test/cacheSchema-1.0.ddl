drop table version if exists;
create table version (ver_major integer, ver_minor integer, constraint uq_version unique(ver_major,ver_minor));

drop table userfilter if exists;
create table userfilter (filterelement varchar(64));

drop table userextensions if exists;
create table userextensions (extensionelement varchar(64));

drop table tokenfilter if exists;
create table tokenfilter (filterelement varchar(64));

drop table tokenextensions if exists;
create table tokenextensions (extensionelement varchar(64));

drop table unprocessedusers if exists;
create table unprocessedusers (defaultlogin varchar(64));

drop table users if exists;
create table users (usernum varchar(64), defaultlogin varchar(64), fullname varchar(64), userdata blob);
#create unique index idx_uq_users_usernum on users (usernum);
#create unique index idx_uq_users_defaultlogin on users (defaultlogin);
#create index idx_users_fullname on users (fullname);

drop table unprocessedtokens if exists;
create table unprocessedtokens (tokenserial varchar(64));

drop table tokens if exists;
create table tokens (tokenserial varchar(64), tokendata blob);
#create unique index idx_uq_token_tokenserial on tokens (tokenserial);