import android,sqlite3

droid = android.Android()
def checktables():
  global con
  # Check to see if table exists
  if sqlvalue("select count(*) from sqlite_master where name='zzz'")==0:
    c=con.cursor()
    c.execute('create table zzz (id integer,info text)')
    c.execute("insert into zzz values (1,'Fred Smith')")
    c.execute("insert into zzz values (2,'John Smith')")
    c.close()
    con.commit()

def sqlvalue(query): # return a single value from a query
  global con
  row=con.execute(query).fetchone()
  if row==None:
    return None
  return row[0]

def dumpquery(query):
  global con
  for row in con.execute(query):
    print row

print "Connecting"
# NOTE: /sdcard/test.db must be writable. If not, you will need to find a writable location.
con = sqlite3.connect("/sdcard/test.db")
print "Checking tables"
checktables()
print "Dump table"
dumpquery("select * from zzz")
print "Done"

