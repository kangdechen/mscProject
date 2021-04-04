import mysql.connector
import pymysql
from mysql.connector.constants import ClientFlag
from flask import jsonify
import os
db_user = os.environ.get('CLOUD_SQL_USERNAME')
db_password = os.environ.get('CLOUD_SQL_PASSWORD')
db_name = os.environ.get('CLOUD_SQL_DATABASE_NAME')
db_connection_name = os.environ.get('CLOUD_SQL_CONNECTION_NAME')
class Connection:
    config = {
        'user': 'root',
        'password': '',
        'host': '34.66.64.158',
        'database':'Food1Database'
    }
    def Connection(self):
        config = Connection.config
        cnxn = mysql.connector.connect(**config)
        return cnxn
    # now we establish our connection
    def getData(self):

        connect=Connection.Connection()
        cursor = connect.cursor()
        cursor.execute("select * from Food_Table ")
        rows=cursor.fetchall()
        for row in rows:
            print(row)
        return  rows

    def getFood(self,rating):

        config=Connection.config
        cnxn = mysql.connector.connect(**config)
        cursor = cnxn.cursor()
        cursor.execute("select * from Food_Table where rating >"+rating)
        rows=cursor.fetchall()
        for row in rows:
            print(row)
        return  rows
    def save_image(self,picUrl,foodname):

        config=Connection.config
        cnxn = mysql.connector.connect(**config)
        cursor = cnxn.cursor()
        command="INSERT into Food_image(FoodName,picUrl) VALUES (%s, %s);"
        val=(foodname,picUrl)
        cursor.execute(command,val)
        cnxn.commit()
        print(cursor.rowcount, "record inserted.")







