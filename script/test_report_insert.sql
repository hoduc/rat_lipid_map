WITH ins(lipid_molec, fa, fa_group_key, calc_mass, formula, base_rt, main_ion, main_area_c, lipidClass, organ ) AS
( VALUES
  ('CL(18:2/18:1/18:2/18:2)','18:2/18:1/18:2/18:2', '72:07:00', 1450.988, 'C81 H144 O17 P2', 18.753, '-H', 9.24e+05, 'CL', 'Subcutaneous fat'),
  ('CL(18:2/16:1/18:2/18:2)','18:2/16:1/18:2/18:2', '70:07:00', 1422.957, 'C79 H140 017 P2', 18.305, '-H', 2.97e+05, 'CL', 'Spleen')
)
INSERT INTO report
   (lipid_molec, fa, fa_group_key, calc_mass, formula, base_rt, main_ion, main_area_c, lipid_class_id, organ_id )
SELECT
   ins.lipid_molec, ins.fa, ins.fa_group_key, ins.calc_mass, ins.formula, ins.base_rt, ins.main_ion, ins.main_area_c, lipid_class.id, organ.id
FROM
   ins, lipid_class, organ
WHERE
lipid_class.name = ins.lipidClass AND organ.name = ins.organ
