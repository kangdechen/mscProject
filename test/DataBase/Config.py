import mysql.connector
import pymysql
from mysql import connector
from mysql.connector import errorcode
from mysql.connector.constants import ClientFlag
from flask import jsonify
import os
db_user = os.environ.get('root')
db_password = os.environ.get('root')
db_name = os.environ.get('Food1Database')
db_connection_name = os.environ.get('eat-diet-app-310718:europe-west2:food')


def GetConnection():
    config = Connection.config

    try:
        cnxn = mysql.connector.connect(**config)
    except connector.Error as err:
        if err.errno == connector.errorcode.ER_ACCESS_DENIED_ERROR:
            print("Something is wrong with your user name or password")
        elif err.errno == errorcode.ER_BAD_DV_ERROR:
            print("Database does not exist")
        else:
            print(err)
    # the else will happen if there was no error!

    return cnxn

class Connection:
    config = {
        'user': 'root',
        'password': 'root',
        'host': '35.242.133.12',
        'database':'Food1Database'
    }







