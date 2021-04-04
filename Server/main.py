from flask import Flask, render_template, jsonify, request, make_response, send_from_directory, abort, session

import os
from DataBase.Config import  Connection
from werkzeug.utils import secure_filename

import datetime
import random
from get_data import User

from google.cloud.storage import Client




app = Flask(__name__)


@app.route("/")
def homepage():
    return render_template("index.html", title="HOME PAGE")

@app.route('/login', methods=['GET','POST'])
def check_user():
   msg = ''
   u = request.form['username']
   p =request.form['password']
   print(f'user name is {u}  and password is {p}')
   account =User.UserLogin(u,p)
   if account:



    return render_template('upload.html', msg='')




@app.route('/register',methods=['GET','POST'])
def register():
    username=request.form['username']
    password=request.form['password']

    print('username:'+username)
    print('password:'+password)
    return '注册成功'

@app.route('/test/json/<r>')
def test_json(r):
    c = Connection

    list= c.getFood(c,r)

    return jsonify(results = list)


basedir = os.path.abspath(os.path.dirname(__file__))

ALLOWED_EXTENSIONS = set(['png', 'jpg', 'JPG', 'PNG', 'gif', 'GIF',"jfif"])


#https://blog.csdn.net/weixin_36380516/article/details/80347192
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
    return render_template('up.html',image=file_name)



@app.route('/upload', methods=['POST'])
def upload():
    if request.method == 'POST':
        storage_client = Client.from_service_account_json("Eat Diet App-1c6a0812e9a7.json", project='Eat Diet App')
        bucket = storage_client.get_bucket("eat-diet-app.appspot.com")

        file = request.files['photo']
        url=file.filename
        name=url.split('.')[0]
        ext = url.rsplit('.', 1)[1]
        new_url = name+Pic_str().create_uuid() + '.'+ext
        print (new_url)
        blob = bucket.blob("food/{}".format(new_url))
        blob.content_type = "image / png"
        if file and allowed_file(file.filename):

            blob.upload_from_file(file)
            print (jsonify({"success": True}))
            Connection.save_image(Connection,new_url,name)
            return render_template('up.html', image=new_url)
        else:
                return jsonify({"error": 1001, "msg": "failure"})




@app.route('/download/<string:filename>', methods=['GET'])
def down(filename):
    dir=os.getcwd()+os.sep+'static/photo/'
    print(dir+filename)

    return send_from_directory(dir,filename,as_attachment=True)


# show photo
@app.route('/shows/<string:filename>', methods=['GET'])
def show_photo(filename):
    file_dir =  basedir+"/static/photo/"
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




if __name__ == '__main__':
    app.run(debug=True ,host='0.0.0.0',port='8888')