-- create own schema --
CREATE SCHEMA gvggroup
    AUTHORIZATION postgres;

COMMENT ON SCHEMA gvggroup
    IS 'test assessment ';

create table if not exists gvggroup.users (
                                              id integer not null,
                                              created date,
                                              email varchar(255) not null,
                                              enabled boolean,
                                              password varchar(255) not null,
                                              username varchar(255) not null,
                                              primary key (id)        );

alter table if exists gvggroup.users drop constraint if exists user_email_must_be_unique;
alter table if exists gvggroup.users add constraint user_email_must_be_unique unique (email);

alter table if exists gvggroup.users drop constraint if exists user_username_must_be_unique;
alter table if exists gvggroup.users add constraint user_username_must_be_unique unique (username);

create sequence users_seq start with 1 increment by 50;

create table if not exists gvggroup.user_roles (
                                                   id integer not null,
                                                   role varchar(255) check (role in ('USER','ADMIN','ROLE_ADMIN', 'ROLE_USER')), primary key (id));

truncate table gvggroup.user_roles;
create table if not exists gvggroup.users_user_roles (user_id integer not null, user_roles_id integer not null);

create sequence user_roles_seq start with 1 increment by 50;

alter table if exists gvggroup.users_user_roles
    add constraint FKdwlsjl9336fne9vntsddpf6xs foreign key (user_roles_id) references gvggroup.user_roles;
alter table if exists gvggroup.users_user_roles
    add constraint FKkfth240mxf8yd3ukhjmscs62w foreign key (user_id) references gvggroup.users;

insert into gvggroup.user_roles (id, role) values (1, 'ROLE_ADMIN'), (2, 'ROLE_USER');

insert into gvggroup.users(id, created, email, enabled, password, username) values
    (1, now(), 'admin@gmail.com', true, '$2a$10$VjLU7aHw4ksI9LmVARWEjejc2hMy2fdRZfP4NHGxDSoFJYT5mh39', 'admin'); -- password is '12345678'

alter table gvggroup.users_user_roles
    owner to postgres;

insert into gvggroup.users_user_roles(user_id, user_roles_id) values (1, 1);