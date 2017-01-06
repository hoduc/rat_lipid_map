# --- !Ups
create table "people" (
  "id" serial primary key,
  "name" varchar(50) not null,
  "age" int not null
);

# --- !Downs

drop table "people" if exists;
