# Rat Lipid Map

Display Lipid data of Rat

## Description

Visualize rat's organ research lipid data.

__A picture is worth a thousand words__: 

![design](https://raw.githubusercontent.com/hoduc/rat_lipid_map/master/demo/design_sketch_annotated.png)



Techs used: __HTML/CSS/Jquery/Bootstrap/CanvasJS/Dynatable,PostgreSQL,Scala,Python__

Website bundle consists of two main layers:


* __Front-end__: Set up interface and send request to back end for data

    * __HTML/CSS/Jquery/Bootstrap__: web layout.

    * __CanvasJS__: percents in chart(pie,bar).

    * __Dynatable__: display lipid molecule data as table.

* __Back-end__: Process request from front end [Play framework]

    * __PostgreSQL__ (v.9.5): Database- Store lipid data.
    * __Scala__: interact with the database(Postgre) and return data to front end.

* __No-end__: Scripts and tool 

    * __Python script__[sql_gen.py]: to automatically insert data( from csv ) into the database.
    
    * __GIMP__: generate click area for each rat organ.






## Wiki : https://github.com/hoduc/rat_lipid_map/wiki/Rat-Lipid-Map-Wiki

## Overview:

![demo1](https://raw.githubusercontent.com/hoduc/rat_lipid_map/master/demo/demo1.png)

![demo2](https://raw.githubusercontent.com/hoduc/rat_lipid_map/master/demo/demo2.png)