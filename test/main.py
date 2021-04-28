import re

import flask
from flask import Flask, render_template, jsonify, request, make_response, send_from_directory, abort, session, \
    redirect, url_for, json

import os

from Collaborative_Filtering import  collaborativeBasedOnUID

from werkzeug.utils import secure_filename

import datetime
import random
from get_data import User, recommendation,Food
from DataBase.Config import GetConnection
from Content_Based import get_rec
from google.cloud.storage import Client
from Model import UserModel, FoodModel,FoodInfoModel
from FoodInfo import getFoodInfoByName,getfoodInfoById

app = Flask(__name__)

app.secret_key = "super secret key"


@app.route("/")
def homepage():
    return render_template("index.html", title="HOME PAGE")

@app.route('/Loggin', methods=['GET', 'POST'])
def Loggin():
    msg = ''
    c = GetConnection()
    name = request.form['name']
    pwd = request.form['pwd']
    print(f'user name is {name}  and password is {pwd}')
    account = User.UserLogin(c,name, pwd)
    if account:
        session['loggedin'] = True
        session['id'] = int(account[0])
        session['username'] = account[1]
        u =UserModel(account[0],account[1],account[2],account[3],account[4],account[5],account[6],account[7],account[8],account[9])
        user=u.__dict__
        print(user)
        print('id',session['id'])
        return render_template('Upload.html')

    else:
        msg = 'null'
        return msg


@app.route('/UserLoggin', methods=['GET', 'POST'])
def UserLoggin():
    msg = ''
    c = GetConnection()
    name = request.form['name']
    pwd = request.form['pwd']
    print(f'user name is {name}  and password is {pwd}')
    account = User.UserLogin(c,name, pwd)
    if account:
        session['loggedin'] = True
        session['id'] = int(account[0])
        session['username'] = account[1]
        gender=account[3]
        weight=account[4]
        u =UserModel(account[0],account[1],account[2],account[3],account[4],account[5],account[6],account[7],account[8],account[9])
        print(gender,weight)
        user=u.__dict__
        print(user)
        data = json.dumps(user)

        return data, 200, {"ContentType": "application/json"}

    else:
        msg = 'null'
        return msg



@app.route('/logout')
def logout():
    # Remove session data, this will log the user out
    session.pop('loggedin', None)
    session.pop('id', None)
    session.pop('username', None)
    # Redirect to login page

    return render_template('index.html')


@app.route('/home')
def home():
    # Check if user is loggedin
    if 'loggedin' in session:
        # User is loggedin show them the home page
        return render_template('upload.html', username=session['username'])
    # User is not loggedin redirect to login page
    return render_template('index.html')


@app.route('/register', methods=['GET', 'POST'])
def register():
    # Output message if something goes wrong...
    msg = ''
    # Check if "username", "password" and "email" POST requests exist (user submitted form)
    if request.method == 'POST' and 'username' in request.form and 'password' in request.form and 'email' in request.form:
        # Create variables for easy access
        username = request.form['username']
        password = request.form['password']
        email = request.form['email']
        Connect = GetConnection()
        cursor = Connect.cursor()
        cursor.execute('SELECT * FROM users WHERE userName = %s', (username,))
        account = cursor.fetchone()
        # If account exists show error and validation checks
        if account:
            msg = 'Account already exists!'
        elif not re.match(r'[^@]+@[^@]+\.[^@]+', email):
            msg = 'Invalid email address!'
        elif not re.match(r'[A-Za-z0-9]+', username):
            msg = 'Username must contain only characters and numbers!'
        elif not username or not password or not email:
            msg = 'Please fill out the form!'
        else:
            # Account doesnt exists and the form data is valid, now insert new account into accounts table
            cursor.execute('INSERT INTO users VALUES (null ,%s, %s, %s, %s)', (username, email, password, 'picurl'))
            Connect.commit()
            msg = 'You have successfully registered!'
    elif request.method == 'POST':
        # Form is empty... (no POST data)
        msg = 'Please fill out the form!'
    # Show registration form with message (if any)
    return render_template('register.html', msg=msg)


@app.route('/test/json/<r>')
def test_json(r):
    c =GetConnection()
    food_list=[]
    list = c.getFood(c, r)

    for food in list:
        f=FoodModel(food[0],food[1],food[2],food[3],food[4],food[5],food[6])
        food= f.__dict__
        food_list.append(food)
    #print(food_list)
    data = json.dumps(food_list)
    return data

def calculateAMR(id):
    c=GetConnection()
    BMR=0
    user=User.GetUser(c,id)
    #Calculate Your BMR
    f = UserModel(user[0],user[1],user[2],user[3],user[4],user[5],user[6],user[7],user[8],user[9])
    food = f.__dict__
    Dietplan=f.plan
    gender=f.gender
    weight=float(f.weight)
    age=float(f.age)
    height=float(f.height)

    if gender=='F':
        BMR=655.1 + (9.563 * weight) + (1.850*height) - (4.676 *age)
    if gender == 'M':
        BMR = 66.47 + (13.75 *weight) + (5.003 *height) - (6.755*age)
    #Calculate AMR
    AMR = BMR*2
    return AMR

def MealCalories(AMR,Meal):
    Calories=0
    if Meal==1:
        Calories=AMR*0.35
    if Meal == 2:
        Calories = AMR * 0.4
    if Meal == 3:
        Calories = AMR * 0.35
    return  Calories

@app.route('/GetFood/<id>/<meal>')
def get_Food(id,meal):
    c = GetConnection()
    print(id ,meal)
    rec_list=[]
  #  UserId = flask.request.args.get()
    lists = Food.getData(c)
    print("List from data",lists)
    c2 = GetConnection()
    recFood_list=Food.getRecFood(c2,id)
    AMR=calculateAMR(id)
    Calories=MealCalories(AMR,meal)
    for food in recFood_list:
        if food[3]>Calories:
            print("too high calories",food[1])
        else:
            f = FoodModel(food[0], food[1], food[2], food[3], food[4], food[5], food[6])
            food = f.__dict__
            rec_list.append(food)
    #print(list)
    for food in lists:
        f=FoodModel(food[0],food[1],food[2],food[3],food[4],food[5],food[6])
        food= f.__dict__
        rec_list.append(food)

    data = json.dumps(rec_list)
    return data


basedir = os.path.abspath(os.path.dirname(__file__))

ALLOWED_EXTENSIONS = set(['png', 'jpg', 'JPG', 'PNG', 'gif', 'GIF', "jfif"])


# https://blog.csdn.net/weixin_36380516/article/details/80347192
def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS


class Pic_str:
    def create_uuid(self):
        nowTime = datetime.datetime.now().strftime("%Y%m%d%H%M%S");
        randomNum = random.randint(0, 100);
        if randomNum <= 10:
            randomNum = str(0) + str(randomNum);
        uniqueNum = str(nowTime) + str(randomNum);
        return uniqueNum;


@app.route('/display/<string:file_name>')
def upload_test(file_name):
    return render_template('up.html', image=file_name)


@app.route('/upload', methods=['POST'])
def upload():




    if request.method == 'POST':
        storage_client = Client.from_service_account_json("eat-diet-app-310718-8d524e6b2ecd.json", project='eat-diet-app-310718')
        bucket = storage_client.get_bucket("ait2021")

        file = request.files['photo']
        url = file.filename
        name = url.split('.')[0]
        ext = url.rsplit('.', 1)[1]
        new_url = name + Pic_str().create_uuid()
        print(new_url)
        blob = bucket.blob("food/{}".format(new_url))
        blob.content_type = "image / png"
        if file and allowed_file(file.filename):

            blob.upload_from_file(file)
            print(jsonify({"success": True}))
            Food.save_image(GetConnection(), new_url, name)
            return render_template('up.html', image=new_url)
        else:
            return jsonify({"error": 1001, "msg": "failure"})


@app.route('/download', methods=['GET'])
def down():
    foodiamge = Food.getFoodImage(Food)
    return jsonify(foodiamge)

def selectPic():
    foodiamge = Food.getFoodImage(Food)
    return
# show photo
@app.route('/shows/<string:Foodname>', methods=['GET'])
def show_photo(Foodname):
    c=GetConnection()
    foodiamge=Food.getFoodImage(c,Foodname)
    if foodiamge:
        filename=foodiamge[2]
    else:
        filename='NotFound2021041912174835'
    if request.method == 'GET':
        if filename is None:
            filename='NotFound2021041912174835'
        else:
            storage_client = Client.from_service_account_json("eat-diet-app-310718-8d524e6b2ecd.json",project='eat-diet-app-310718')
            bucket = storage_client.get_bucket("ait2021")
            name = filename
            blob = bucket.blob("food/{}".format(name))

            image_data = blob.download_as_bytes()
            response = make_response(image_data)
            print(response)
            response.headers['Content-Type'] = 'image/png'
            return response
    else:
        pass


@app.route('/showsRec/<UserId>')
def foodRec(UserId):
    c = GetConnection()

    print( UserId)
    foodList = recommendation.content_base(c,UserId)
    if not foodList:
        print("List is empty")
        return "empty list"
    else:

        #for f in foodList:
            #print(f)
        reclist = get_rec(foodList)
        FoodDlist=[]
        collaborativeListBasedOnUserId=collaborativeBasedOnUID(UserId);
        for val in collaborativeListBasedOnUserId:
            reclist.append(val)
        FoodDlist = Food.saveFoodrec(c,UserId,reclist)

    return "success"

@app.route('/foodInfo/<int:foodId>')
def foodInfo(foodId):
    print(foodId)
    infoList=getfoodInfoById(foodId)

    return  jsonify(infoList)

@app.route('/saveFood')
def create_ingredient(foodId,infoList):

    Food.saveFood_info(foodId, infoList)

    return 'sucess'
@app.route('/GetIngre/<int:foodId>')
def get_ingredient(foodId):
        c=GetConnection()
        lists=Food.getFoodData(c,foodId)
        ingre_list=[]




        for ingre in lists:
            f=FoodInfoModel(ingre[0],ingre[1],ingre[2])
            ingre=f.__dict__
            ingre_list.append(ingre)

        print(ingre_list)
        data = json.dumps(ingre_list)
        return data
if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port='8888')
