import os

BASE_DIR = "data"
PERCENT_DIR = BASE_DIR + "/percent/"
REPORT_DIR = BASE_DIR + "/report/"

def sq(s, lq = "'", rq = "'", comma=True):
    q = lq + s + rq
    return cm(q) if comma else q

def sq1(s, comma=True):
    return sq(s, "'", "'", comma)

def cm(s):
    return s + ","

def paren(s, comma=True):
    return sq(s, "(", ")", comma)

def read_report(csv):
    values = []
    p = csv[:csv.rindex("/")+1]
    #print("p:"+p)
    f_name = csv[csv.rindex("/")+1:csv.rindex(".")]
    organ = f_name[:f_name.index("_lipids")]
    #print("pre:organ=",organ)
    organ = organ.replace("_", " ")
    print("organ=",organ)
    with open(csv,'r') as f:
        f.readline()
        for line in f:
            lipid_molec, lipidClass, fa, fa_group_key, calc_mass, formula, base_rt, main_ion, main_area_c = line.split(",")
            s = paren(sq(lipid_molec.rstrip()) + sq(fa.rstrip()) + sq(fa_group_key.rstrip()) + cm(calc_mass.rstrip()) + sq(formula.rstrip())  + cm(base_rt.rstrip()) + sq(main_ion.rstrip()) + sq(main_area_c.rstrip()) + sq(lipidClass.rstrip()) + sq1(organ.rstrip(),False))
            values.append(s)
        values[-1] = values[-1][:-1]

    header = """WITH ins(lipid_molec, fa, fa_group_key, calc_mass, formula, base_rt, main_ion, main_area_c, lipidClass, organ ) AS
( VALUES
"""
    footer = """
INSERT INTO report
   (lipid_molec, fa, fa_group_key, calc_mass, formula, base_rt, main_ion, main_area_c, lipid_class_id, organ_id )
SELECT
   ins.lipid_molec, ins.fa, ins.fa_group_key, ins.calc_mass, ins.formula, ins.base_rt, ins.main_ion, ins.main_area_c, lipid_class.id, organ.id
FROM
   ins, lipid_class, organ
WHERE
lipid_class.name = ins.lipidClass AND organ.name = ins.organ"""
    with open(p + f_name + ".sql", 'w') as f:
        for v in values:
            header += v
        header += ")" + footer
        f.write(header)

def read_percent(csv):
    values = []
    p = csv[:csv.rindex("/")+1]
    #print("p:"+p)
    f_name = csv[csv.rindex("/")+1:csv.rindex(".")]
    organ = f_name[:f_name.index("_lipids")]
    #print("pre:organ=",organ)
    organ = organ.replace("_", " ")
    print("organ=",organ)
    with open(csv,'r') as f:
        f.readline() #skip the title
        for line in f:
            lipidClass, n_species, percent = [e.rstrip() for e in line.split(",")]
            if lipidClass == "Total":
                continue
            #print(sq(lipidClass))
            #print(sq(organ))
            #print(cm(n_species))
            #print(sq1(percent,False))
            s = paren( sq(lipidClass) + sq(organ) + cm(n_species) + percent)
            values.append(s)
        values[-1] = values[-1][:-1]

    header = """WITH ins(lipidClass, organ, n_species, percent) AS
( VALUES
"""

    footer = """
INSERT INTO percentage
   (lipid_class_id, organ_id, n_species, percent)
SELECT
   lipid_class.id, organ.id, ins.n_species, ins.percent
FROM
   ins, lipid_class, organ
WHERE
lipid_class.name = ins.lipidClass AND organ.name = ins.organ"""
    with open(p + f_name + ".sql", 'w') as f:
        for v in values:
            header += v
        header += ")" + footer
        f.write(header)

if __name__ == "__main__":
    #print(os.listdir(REPORT_DIR))
    print("::REPORT::")
    for filename in os.listdir(REPORT_DIR):
        #print("path:" + os.path.abspath(filename))
        if filename.endswith(".csv"):
            read_report(os.path.abspath(REPORT_DIR) + "/" + filename)

    print("::PERCENT::")
    for filename in os.listdir(PERCENT_DIR):
        if filename.endswith(".csv"):
            read_percent(os.path.abspath(PERCENT_DIR) + "/" + filename)
