import re

from flask import Flask, render_template, jsonify, request, make_response, send_from_directory, abort, session, \
    redirect, url_for

import os
from DataBase.Config import Connection
from werkzeug.utils import secure_filename

import datetime
import random
from get_data import User, recommendation
from Content_Based import get_Rec
from google.cloud.storage import Client

app = Flask(__name__)

app.secret_key = "super secret key"


@app.route("/")
def homepage():
    return render_template("index.html", title="HOME PAGE")


@app.route('/UserLoggin', methods=['GET', 'POST'])
def UserLoggin():
    msg = ''
    name = request.form['name']
    pwd = request.form['pwd']
    print(f'user name is {name}  and password is {pwd}')
    account = User.UserLogin(name, pwd)
    if account:
        session['loggedin'] = True
        session['id'] = int(account[0])
        session['username'] = account[1]
        # Redirect to home page
        return redirect(url_for('home'))
    else:
        msg = 'Incorrect username/password!'


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
        Connect = Connection.Connection(Connection)
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
    c = Connection

    list = c.getFood(c, r)

    return jsonify(results=list)


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
        storage_client = Client.from_service_account_json("Eat Diet App-1c6a0812e9a7.json", project='Eat Diet App')
        bucket = storage_client.get_bucket("eat-diet-app.appspot.com")

        file = request.files['photo']
        url = file.filename
        name = url.split('.')[0]
        ext = url.rsplit('.', 1)[1]
        new_url = name + Pic_str().create_uuid() + '.' + ext
        print(new_url)
        blob = bucket.blob("food/{}".format(new_url))
        blob.content_type = "image / png"
        if file and allowed_file(file.filename):

            blob.upload_from_file(file)
            print(jsonify({"success": True}))
            Connection.save_image(Connection, new_url, name)
            return render_template('up.html', image=new_url)
        else:
            return jsonify({"error": 1001, "msg": "failure"})


@app.route('/download/<string:filename>', methods=['GET'])
def down(filename):
    dir = os.getcwd() + os.sep + 'static/photo/'
    print(dir + filename)

    return send_from_directory(dir, filename, as_attachment=True)


# show photo
@app.route('/shows/<string:filename>', methods=['GET'])
def show_photo(filename):
    file_dir = basedir + "/static/photo/"
    if request.method == 'GET':
        if filename is None:
            pass
        else:
            storage_client = Client.from_service_account_json("Eat Diet App-1c6a0812e9a7.json", project='Eat Diet App')
            bucket = storage_client.get_bucket("eat-diet-app.appspot.com")
            name = filename
            blob = bucket.blob("food/{}".format(name))

            image_data = blob.download_as_bytes()
            response = make_response(image_data)
            print(response)
            response.headers['Content-Type'] = 'image/png'
            return response
    else:
        pass


@app.route('/showsRec/')
def foodRec():
    UserId = int(session['id'])
    foodList = recommendation.content_base(UserId)
    for f in foodList:
        print(f)
    reclist = get_Rec(foodList)
    return jsonify(reclist)


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port='8888')
