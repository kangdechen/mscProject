from DataBase.Config import Connection,GetConnection
from flask import Flask, render_template, jsonify, request, make_response, send_from_directory, abort



class User:
    def UserLogin(self,UserName,pwd):
        cnxn = self
        cursor = cnxn.cursor()
        cursor.execute("select * from users where userName = %s and passwd =%s",(UserName,pwd))
        Users = cursor.fetchone()
        cursor.close()
        print(Users[0])
        return  Users
    def GetUser(self,id):
        cnxn = self
        cursor = cnxn.cursor()
        cursor.execute("select * from users where userId ="+str(id))
        Users = cursor.fetchone()
        cursor.close()
        print(Users[0])
        return  Users

class rating:
    def food_rating(self,userId,foodId,Rating):
        cnxn = self
        cursor = cnxn.cursor()

        cursor.execute(('insert into User_Rating (userId,FoodId,Rating) values (%s,%s,%s);'),(userId,foodId,Rating))
        cnxn.commit()
        cursor.close()
        return

class recommendation:
    def content_base(self,UserId):
        cnxn = self

        cursor = cnxn.cursor()
        command=("select distinct title FROM User_like as Ur LEFT JOIN Food_Table as Ft ON Ur.FoodId = Ft.Id where Ur.UserId='"+str(UserId)+" 'and Ur.User_like>0")
        cursor.execute(command,(UserId))

        lists=cursor.fetchall()
        cursor.close()

        return  lists

class Food:
    def getData(self):
        cnxn = self
        cursor = cnxn.cursor()
        cursor.execute("select * from Food_Table limit 10")
        rows = cursor.fetchall()

        for row in rows:
            print(row)
        cnxn.close()
        return rows
    def getFood(self,foodlist):
        cnxn = self
        cursor = cnxn.cursor()
        rows=[]
        for food in foodlist:

            food='"'+food+'"'

            #to avoid single quate in comand
            c=("select * from Food_Table where title = "+food+";")
           # print(c)
            cursor.execute(c)
            row = cursor.fetchall()
            rows.append(row)
        for row in rows:
            print(row)
        cnxn.close()
        return rows
    def getRecFood(self,UserId):
        cnxn = self
        cursor = cnxn.cursor()
        command = ("select  DISTINCT * from Food_Table as ft inner JOIN Food_rec on ft.title= Food_rec.FoodTitle where Food_rec.userId="+str(UserId)+" group by ft.title order by ft.rating;")
        cursor.execute("SET sql_mode = '' ;")
        cursor.execute(command, (UserId))

        rows = cursor.fetchall()
        for row in rows:
            print(row)
        cursor.close()

        return rows
    def saveFoodrec(self,Userid,foodlist):
        cnxn = self
        cursor = cnxn.cursor()
        rows=[]
        for food in foodlist:
            cursor.execute(('insert into Food_rec (userId,FoodTitle) values (%s,%s);'), (Userid, food))
            cnxn.commit()
            print("save rec",food)
        cnxn.close()
        return


    def getFoodData(self,id):
        cnxn = self
        cursor = cnxn.cursor()
        print(id)
        cursor.execute("select * from Food_info where FoodId ="+str(id))
        rows = cursor.fetchall()

        for row in rows:
            print(row)
        cnxn.close()
        return rows
    def saveFood_info(self,foodid,ingredient):
        cnxn = self
        cursor = cnxn.cursor()
        for food in ingredient:
            cursor.execute(('insert into Food_info (FoodId,ingredient) values (%s,%s);'), (foodid,food))
            cnxn.commit()
        return
    def getFoodImage(self,filename):

        cursor =self.cursor()


        food = '"' + filename + '"'

        # to avoid single quate in comand
        c = ("select * from Food_image where foodname = " + food + ";")
        lists = cursor.fetchone()
        cursor.close()
        print(filename)

        print(lists)

        return lists

    def save_image(self, picUrl, foodname):

        cnxn = self
        cursor = cnxn.cursor()
        command = "INSERT into Food_image(FoodName,picUrl) VALUES (%s, %s);"
        val = (foodname, picUrl)
        cursor.execute(command, val)
        cnxn.commit()
        cursor.close()
        print(cursor.rowcount, "record inserted.")
