from DataBase.Config import Connection
from flask import Flask, render_template, jsonify, request, make_response, send_from_directory, abort


cnxn= Connection.Connection(Connection)
cursor=cnxn.cursor()
class User:
    def UserLogin(UserName,pwd):

        cursor = cnxn.cursor()
        cursor.execute("select * from users where userName = %s and passwd =%s",(UserName,pwd))
        Users = cursor.fetchone()

        return  Users

class rating:
    def food_rating(self,userId,foodId,Rating):

        cursor = cnxn.cursor()
        cursor.execute(('insert into User_Rating (userId,FoodId,Rating) values (%s,%s,%s);'),(userId,foodId,Rating))
        cnxn.commit()
        return

class recommendation:
    def content_base(UserId):

        cursor = cnxn.cursor()
        command=('select distinct title FROM User_Rating as Ur LEFT JOIN Food_Table as Ft ON Ur.FoodId = Ft.Id where Ur.UserId= 1 and Ur.Rating>6')
        cursor.execute(command)

        lists=cursor.fetchall()

        return  lists
