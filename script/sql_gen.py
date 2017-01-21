
import os
import sys
import psycopg2

BASE_DIR = "data"
PERCENT_DIR = BASE_DIR + "/percent/"
REPORT_DIR = BASE_DIR + "/report/"
REL_DIR = BASE_DIR + "/relation/"

LOG_LEVEL_INFO = 0
LOG_LEVEL_WARN = 1
LOG_LEVEL_DEBUG = 2
LOG_LEVEL_ONLY_DEBUG = 3

LOG_LEVEL = LOG_LEVEL_INFO

def log(msg, log_level = LOG_LEVEL_WARN):
    decor = "[WARN]:"
    if log_level == LOG_LEVEL_DEBUG or log_level == LOG_LEVEL_ONLY_DEBUG:
        decor = "[DEBUG]:"
    elif log_level == LOG_LEVEL_INFO:
        decor = "[INFO]:"
    if log_level == LOG_LEVEL_ONLY_DEBUG or log_level <= LOG_LEVEL:
        print decor + str(msg)

def log_info(msg):
    log(msg, LOG_LEVEL_INFO)

def log_warn(msg):
    log(msg,LOG_LEVEL_WARN)

def log_debug(msg):
    log(msg,LOG_LEVEL_DEBUG)

def sq(s, lq = "'", rq = "'", comma=True):
    q = lq + s + rq
    return cm(q) if comma else q

def sq1(s, comma=True):
    return sq(s, "'", "'", comma)

def cm(s):
    return s + ","

def paren(s, comma=True):
    return sq(s, "(", ")", comma)



FETCH_ORGAN = """SELECT * from organ where name ="""
FETCH_LIPID_CLASS = """SELECT * from lipid_class where name ="""
FETCH_LIPID_MOLEC_QUERY = """SELECT * from lipid_molecule where lipid_molec ="""

FETCH_LIPID_MOLEC_PERCENT = """SELECT """

def is_row_exist(conn, q):
    log_debug("is_row_exist:query=" + q)
    cur = conn.cursor()
    cur.execute(q)
    return cur.fetchone()

def exe_then_commit(conn, q):
    log_debug("query=" + q)
    cur = conn.cursor()
    cur.execute(q)
    conn.commit()

def insert(conn, table, col_titles, cols):
    q = "INSERT INTO " + table + "(" + ",".join(col_titles) + ")" + " VALUES " + "(" + ",".join(cols) + ")"
    exe_then_commit(conn,q)

def set_columns(set_tuple_list):
    log_debug("set_columns::")
    return " SET " + ",".join( c + " = " + v for(c,v) in set_tuple_list)

def where_and(wheres):
    log_debug("where_and")
    return " WHERE " + " AND ".join( c + " = " + v for (c,v) in wheres)

def update(conn, table, wheres, sets):
    q = "UPDATE " + table + set_columns(sets) + where_and(wheres)
    exe_then_commit(conn,q)

def insert_organ(conn, name):
    log_debug("insert_organ::")
    q = FETCH_ORGAN + name
    r = is_row_exist(conn, q)
    if not r:
        log_debug("organ[" + name + "] not exist!!!")
        insert(conn, "organ", ["id,name"], ["default",name])
        r = is_row_exist(conn, q)
    log_debug("get organ_id:" + str(r[0]))
    return str(r[0])

def insert_lipid_class(conn, name):
    log_debug("insert_lipid_class:::")
    q = FETCH_LIPID_CLASS + name
    r = is_row_exist(conn, q)
    if not r:
        log_debug("lipid_class[" + name + "] not exist!!!")
        insert(conn, "lipid_class", ["id", "name"], ["default", name])
        r = is_row_exist(conn,q)
    log_debug("get lipid_class_id:" + str(r[0]))
    return str(r[0])

#return lipid_molec row id
def insert_lipid_molec(conn, lipid_molec_col_titles, lipid_molec_cols):
    log_debug("insert_lipid_molec::")
    log_debug("lipid_molec_col_titles:" + str(lipid_molec_col_titles))
    log_debug("lipid_molec_cols:" + str(lipid_molec_cols))
    q = FETCH_LIPID_MOLEC_QUERY  + lipid_molec_cols[0]
    r = is_row_exist(conn, q)
    if not r:
        log_debug("lipid_molec[" + lipid_molec_cols[0] + "] not exist!!!")
        insert(conn, "lipid_molecule", lipid_molec_col_titles, ["default"] + lipid_molec_cols)
        r = is_row_exist(conn, q)
    log_debug("get lipid_molec_id:" + str(r[0]))
    return str(r[0])

def insert_lipid_molec_percent_partial(conn, lipid_molec_percent_col_titles, lipid_molec_percent_cols):
    log_debug("insert_lipid_mlec_percent_partial:")
    q = """SELECT id, lipid_molec_id, organ_id from lipid_molecule_percent WHERE lipid_molec_id = """ + lipid_molec_percent_cols[-2] + """ AND organ_id = """ + lipid_molec_percent_cols[-1]
    log_debug("lipid_molec_percent_col_titles:" + str(lipid_molec_percent_col_titles))
    log_debug("lipid_molec_percent_cols:" + str(lipid_molec_percent_cols))
    r = is_row_exist(conn, q)
    if not r:
        log_debug("lipid_molec_percent does not exist!!!")
        insert(conn, "lipid_molecule_percent",  lipid_molec_percent_col_titles,["default"] + lipid_molec_percent_cols)
        r = is_row_exist(conn, q)
    log_debug("get back lipid_molec_percent_id:" + str(r[0]))
    return str(r[0])

def insert_lipid_molec_organ(conn, lipid_molec_organ_titles, lipid_molec_organ_cols):
    log_debug("insert_lipid_molec_organ:")
    q = """SELECT * from lipid_molecule_organ WHERE lipid_molec_id = """ + lipid_molec_organ_cols[-2] + """ AND organ_id = """ + lipid_molec_organ_cols[-1]
    log_debug("lipid_molec_organ_titles:" + str(lipid_molec_organ_titles))
    log_debug("lipid_molec_organ_cols:" + str(lipid_molec_organ_cols))
    r = is_row_exist(conn, q)
    if not r:
        log_debug("lipid_molec_organ does not exist!!!")
        insert(conn, "lipid_molecule_organ", lipid_molec_organ_titles,["default"] + lipid_molec_organ_cols)
        r = is_row_exist(conn, q)
    log_debug("get back lipid_molec_organ_id:" + str(r[0]))
    return str(r[0])

def read_report_conn(csv,conn):
    log_debug("read_report_conn::")
    p = csv[:csv.rindex("/")+1]
    f_name = csv[csv.rindex("/")+1:csv.rindex(".")]
    organ = f_name[:f_name.index("_lipids")] 
    organ = sq1(organ.replace("_", " "),False) #lowercase
    organ_id = insert_organ(conn, organ)
    log_debug("organ=" + organ)
    with open(csv,'r') as f:
        f.readline()
        for line in f:
            lipid_molec, lipidClass, fa, fa_group_key, calc_mass, formula, base_rt, main_ion, main_area_c = [e.rstrip() for e in line.split(",")]
            lipid_molec, lipidClass, fa, fa_group_key, calc_mass, formula, base_rt, main_ion, main_area_c = [sq1(lipid_molec,False), sq1(lipidClass,False), sq1(fa,False), sq1(fa_group_key,False), calc_mass, sq1(formula,False), base_rt, sq1(main_ion,False), sq1(main_area_c,False)]
            log_debug((lipid_molec, lipidClass, fa, fa_group_key, calc_mass, formula, base_rt, main_ion, main_area_c))
            lipid_class_id = insert_lipid_class(conn, lipidClass)
            lipid_molec_id = ""
            r = is_row_exist(conn, FETCH_LIPID_MOLEC_QUERY + lipid_molec)
            if not r:
                #it has not been used for percent-relation
                lipid_molec_id = insert_lipid_molec(conn, ["id","lipid_molec","fa","fa_group_key","calc_mass", "formula","main_ion", "lipid_class_id"], [lipid_molec, fa, fa_group_key, calc_mass, formula, main_ion, lipid_class_id])
            else:
                lipid_molec_id = str(r[0])
                update(conn, "lipid_molecule", [("id", lipid_molec_id)], [("fa", fa),("fa_group_key",fa_group_key),("calc_mass",str(calc_mass)),("formula",formula),("main_ion",main_ion),("lipid_class_id",lipid_class_id)])
            lipid_molec_organ_id = insert_lipid_molec_organ(conn, ["id","base_rt","main_area_c","lipid_molec_id","organ_id"],[base_rt, main_area_c, lipid_molec_id, organ_id])
            #lipid_molec_percent = insert_lipid_molec_percent_partial(conn,["id","base_rt","main_area_c","percent","lipid_molec_id","organ_id"],[base_rt, main_area_c, "0", lipid_molec_id, organ_id])
            #update(conn, "lipid_molecule", [("id", lipid_molec_id)], [("fa", fa),("fa_group_key",fa_group_key),("calc_mass",str(calc_mass)),("formula",formula),("main_ion",main_ion),("lipid_class_id",lipid_class_id)])
            #print("here???")
            #update(conn, "lipid_molecule_percent", [("lipid_molec_id", lipid_molec_id),("organ_id", organ_id)], [("base_rt", base_rt),("main_area_c",main_area_c)])



def insert_percentage(conn, percentage_col_title, percentage_cols):
    log_debug("insert_percentage::")
    log_debug("percentage_col_title:" + str(percentage_col_title))
    log_debug("percentage_cols:" + str(percentage_cols))
    q = """SELECT id, lipid_class_id, organ_id from percentage WHERE lipid_class_id =""" + percentage_cols[0] + """ AND organ_id = """ + percentage_cols[1]
    r = is_row_exist(conn, q)
    if not r:
        log_debug("pecentage does not exist!!!")
        insert(conn, "percentage", percentage_col_title, ["default"] + percentage_cols)
        r = is_row_exist(conn,q)    
    log_debug("get back percentage_id:" + str(r[0]))
    return str(r[0])

def read_percent_conn(csv,conn):
    log_debug("read_percent_conn::")
    p = csv[:csv.rindex("/")+1]
    f_name = csv[csv.rindex("/")+1:csv.rindex(".")]
    organ = f_name[:f_name.index("_lipids")]
    organ = sq1(organ.replace("_", " "),False)
    organ_id = insert_organ(conn, organ)
    log_debug("organ=" + organ)
    with open(csv,'r') as f:
        f.readline() #skip the title
        for line in f:
            lipidClass, n_species, percent = [e.rstrip() for e in line.split(",")]
            if lipidClass == "Total":
                continue
            lipidClass = sq1(lipidClass, False)
            lipid_class_id = insert_lipid_class(conn, lipidClass)
            percentage_id = insert_percentage(conn,["id","lipid_class_id","organ_id","n_species","percent"], [lipid_class_id,organ_id,n_species,percent])


def insert_lipid_class_percent(conn, cols, col_values):
    log_debug("insert_lipid_class_percent::")
    log_debug("col:" + str(cols))
    log_debug("col_values:" + str(col_values))
    q = "SELECT id, lipid_class_id, organ_id from lipid_class_percent WHERE lipid_class_id = " + col_values[0] + " AND organ_id = " + col_values[1]
    r = is_row_exist(conn,q)
    if not r:
        log_debug("lipid_class percentage does not exist!!!")
        insert(conn, "lipid_class_percent", cols, ["default"] + col_values)
        r = is_row_exist(conn,q)
    log_debug("get back lipid_class_percent_id:" + str(r[0]))
    return str(r[0])
              

def read_lipid_class_percent(csv, conn, lipid_class_id):
    log_debug("read_lipid_class_percent::")
    with open(csv, 'r') as f:
        f.readline()
        f.readline()
        for line in f:
            organ,percent = [e.rstrip() for e in line.split(",")]
            organ = sq1(organ, False)
            organ_id = insert_organ(conn,organ)
            lipid_class_percent_id = insert_lipid_class_percent(conn, ["id","lipid_class_id", "organ_id", "percent"], [lipid_class_id, organ_id, percent])


def update_lipid_molecule_percent(csv, conn, lipid_class_id):
    log_debug("update_lipid_molecule_percent::")
    with open(csv, 'r') as f:
        l = [sq1(e.rstrip(),False) for e in f.readline().split(",")]
        log_debug("l=" + str(l))
        organs = l[1:]
        log_debug("organs:" + str(organs))
        organ_ids = [ insert_organ(conn,organ) for organ in organs ]
        log_debug("organ_ids:" + str(organ_ids))
        for line in f:
            line = [e.rstrip() for e in line.split(",")]
            lipid_molecule = sq1(line[0],False)
            percents = line[1:]
            log_debug("lipid_molecule:" + lipid_molecule)
            log_debug("percent:" + str(percents))
            r = is_row_exist(conn,FETCH_LIPID_MOLEC_QUERY + lipid_molecule)
            if not r:
                log_warn("lipid_molecule does not exist!!! Exiting the program!!!")
                exit(-1)
            lipid_molec_id = str(r[0])
            if len(percents) != len(organs):
                log_warn("len(organ) != len(percents)")
                log_warn("organs=" + str(organs))
                log_warn("percent=" + str(percents))
                log_warn("Stop the program!!!")
                exit(-1)
            log_debug("preparing to update lipid_molecule_percent:")
            for (organ_id,percent) in zip(organ_ids, percents):
                update(conn, "lipid_molecule_percent", [("organ_id", organ_id)] + [("lipid_molec_id", lipid_molec_id)], [("percent", percent)])


def insert_lipid_molecule_percent_rel(csv, conn, lipid_class_id):
    log_debug("insert_lipid_molecule_percent_rel::")
    with open(csv, 'r') as f:
        l = [sq1(e.rstrip(),False) for e in f.readline().split(",")]
        log_debug("l=" + str(l))
        organs = l[1:]
        log_debug("organs:" + str(organs))
        organ_ids = [ insert_organ(conn,organ) for organ in organs ]
        log_debug("organ_ids:" + str(organ_ids))
        for line in f:
            line = [e.rstrip() for e in line.split(",")]
            lipid_molecule = sq1(line[0],False)
            percents = line[1:]
            log_debug("lipid_molecule:" + lipid_molecule)
            log_debug("percent:" + str(percents))
            lipid_molec_id = insert_lipid_molec(conn, ["id","lipid_molec","fa","fa_group_key","calc_mass", "formula","main_ion", "lipid_class_id"], [lipid_molecule,sq1("null",False), sq1("null",False), "0", sq1("null",False), sq1("null",False), lipid_class_id])
            if len(percents) != len(organs):
                log_warn("len(organ) != len(percents)")
                log_warn("organs=" + str(organs))
                log_warn("percent=" + str(percents))
                log_warn("Stop the program!!!")
                exit(-1)
            log_debug("preparing to update lipid_molecule_percent:")
            for (organ_id,percent) in zip(organ_ids, percents):
                lipid_molec_percent = insert_lipid_molec_percent_partial(conn,["id","percent","lipid_molec_id","organ_id"],[percent, lipid_molec_id, organ_id])
                
def read_rel_conn(csv, conn):
    log_debug("read_rel_conn::")
    f_name = csv[csv.rindex("/")+1:csv.rindex(".")]
    pre_lipids = f_name[:f_name.index("lipids")]
    log_debug("pre_lipids:" + pre_lipids)
    lipid_class = sq1(pre_lipids[:pre_lipids.index("_")],False)
    lipid_class_id = insert_lipid_class(conn, lipid_class)
    log_debug("lipid_class=" + lipid_class)
    if "lipidmolec" not in pre_lipids:
        read_lipid_class_percent(csv,conn, lipid_class_id)
    else:
        #update_lipid_molecule_percent(csv,conn, lipid_class_id)
        insert_lipid_molecule_percent_rel(csv,conn,lipid_class_id)

def read_csv(log_msg,directory,read_fn,conn):
    log_info(log_msg)
    for filename in os.listdir(directory):
        if filename.endswith(".csv"):
            log_info("reading file:" + filename)
            read_fn(os.path.abspath(directory) + "/" + filename, conn)

def truncate_tables(conn):
    exe_then_commit(conn, "TRUNCATE percentage, lipid_molecule, lipid_molecule_percent, lipid_class_percent , lipid_class, organ CASCADE")

if __name__ == "__main__":
    conn = None
    truncate = False
    if len(sys.argv) >1:
        for arg in sys.argv:
            if arg in ["debug", "info", "warn"]:
                if arg == "debug":
                    LOG_LEVEL = LOG_LEVEL_DEBUG
                elif arg == "warn":
                    LOG_LEVEL = LOG_LEVEL_WARN
            elif arg == "truncate-first":
                truncate = True
    try:
        conn = psycopg2.connect("dbname='test' user='tester1' host='localhost' password='123'")
        if truncate:
            log_debug("truncate table")
            truncate_tables(conn)
        read_csv("::RELATION::", REL_DIR, read_rel_conn, conn)
        read_csv("::REPORT::", REPORT_DIR, read_report_conn, conn)
        read_csv("::PERCENT::", PERCENT_DIR, read_percent_conn, conn)
    except Exception as e:
        print(type(e))
        print(e)
    finally:
        conn.close()
    
