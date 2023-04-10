-- Initializes tables for the NoteCloser app
create database if not exists noteclose;
use noteclose;

create table if not exists user_token(
	id int primary key, -- OSM user id
    token text not null, -- OSM access token
    auth_date datetime not null default now()
);

create table if not exists note(
	note int primary key, -- OSM note id
    osm_user int not null, -- OSM user id
    schedule_date datetime not null default now(), -- The time the note was scheduled to close
    close_date datetime not null, -- The time the note is scheduled to close
    close_message text,
    info enum("scheduled", "executed", "cancelled") default "scheduled"
);