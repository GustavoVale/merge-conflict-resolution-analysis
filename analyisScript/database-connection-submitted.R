#OPENING AND CLOSING DATABASE CONNECTION -------------------------------------------------------------

#db Connection
get.database.connection.server = function(){
  mycnf= "./cnf.file"
  dbConnect(dbDriver("MySQL"), group="serverDB", dbname="4cnetwork", 
            default.file="./cnf.file", unix.socket="/var/run/mysqld/mysqld.sock") 
  #Create a cnf.file with further information or add it here like:
  #user="root", password="root", dbname="test-DB", host="127.0.0.1", port=3306) 
  
}

#db Disconnection
close.db.connection = function(mydb){
  dbDisconnect(mydb)  
}
