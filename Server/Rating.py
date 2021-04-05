
from flask import Flask, render_template, jsonify, request, make_response, send_from_directory, abort, session, \
    redirect, url_for


from DataBase.Config import  Connection

from get_data import User

app = Flask(__name__)

@app.route('/rating', methods=['GET','POST'])
def Food_rating():
    UserId=session['id']


