from DataBase.Config import Connection
from flask import Flask, render_template, jsonify, request, make_response, send_from_directory, abort



class User:
    def UserLogin(UserName):
        cnxn= Connection.Connection()
        cursor = cnxn.cursor()
        cursor.execute("select * from users where userName =" + UserName)
        Users = cursor.fetchone()

        return  Users