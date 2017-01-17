# --- !Ups
create table lipid_class(
  id serial primary key,
  name varchar(50) not null
);

create table organ(
  id serial primary key,
  name varchar(100) not null
);

create table percentage(
  id serial primary key,
  lipid_class_id int references lipid_class (id),
  organ_id int references organ (id),
  n_species int not null,
  percent double precision not null
);

create table report(
  id serial primary key,
  lipid_molec varchar(100) not null,
  fa varchar(100) not null,
  fa_group_key varchar(100) not null,
  calc_mass double precision not null,
  formula varchar(100) not null,
  base_rt double precision not null,
  main_ion varchar(100) not null,
  main_area_c varchar(100) not null,
  lipid_class_id int references lipid_class (id),
  organ_id int references organ (id)
);

create table lipid_molecule(
  id serial primary key,
  lipid_molec varchar(100) not null,
  fa varchar(100) not null,
  fa_group_key varchar(100) not null,
  calc_mass double precision not null,
  formula varchar(100) not null,
  main_ion varchar(100) not null,
  lipid_class_id int references lipid_class (id)
);

create table lipid_molecule_percent(
  id serial primary key,
  base_rt double precision not null,
  main_area_c varchar(100) not null,
  percent double precision,
  lipid_molec_id int references lipid_molecule (id),
  organ_id int references organ (id)
);

create table lipid_class_percent(
  id serial primary key,
  lipid_class_id int references lipid_class (id),
  organ_id int references organ (id),
  percent double precision not null
);


# --- !Downs
drop table lipid_class if exists;
drop table organ if exists;
drop table percentage if exists;
drop table report if exists;
drop table lipid_molecule if exists;
drop table lipid_molecule_percent if exists;
drop table lipid_class_percent if exists;
