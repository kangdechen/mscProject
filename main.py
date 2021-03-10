from flask import Flask, request,render_template

app = Flask(__name__)

name = 'kangde'
pWord = '123456'


@app.route("/")
def homepage():
    return render_template("index.html", title="HOME PAGE")

@app.route('/user', methods=['POST'])
def check_user():

   u = request.form['username']
   p =request.form['password']
   print(f'user name is {u}  and password is {p}')

   if u!=name or p !=pWord:
        return 'login failed';
   else:
       return  'log in success';


@app.route('/demo', methods=['GET','POST'])
def demo():

    print(name)
    return name


@app.route('/register',methods=['GET','POST'])
def register():
    username=request.form['username']
    password=request.form['password']

    print('username:'+username)
    print('password:'+password)
    return '注册成功'

if __name__ == '__main__':
    app.run(debug=True ,host='0.0.0.0',port='8888')