# -*- coding: utf-8 -*-
"""
Created on Tue Mar  9 12:07:26 2021

@author: Team
"""

import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import linear_kernel
import mysql.connector
from flask import Flask, render_template, jsonify, request, make_response, send_from_directory, abort
from mysql.connector.constants import ClientFlag


#Google cloud configuration
config = {
    'user': 'root',
    'password': '',
    'host': '34.66.64.158',
    'database':'Food1Database'
}
# now we establish our connection
cnxn = mysql.connector.connect(**config)
cursor = cnxn.cursor()
cursor.execute("select * from Food_Table")
rows=cursor.fetchall()

dataset = pd.DataFrame(rows, columns=['Id', 'title', 'rating','calories','protien','fat','sodium'])


#making Tfidf matrix
#dataset= pd.read_csv("epi_r.csv")

tf=TfidfVectorizer(analyzer='word',ngram_range=(1,3),min_df=0,stop_words='english')
tfidf_matrix = tf.fit_transform(dataset['title'])
#print(tf.get_feature_names())
cosine_similarities = linear_kernel(tfidf_matrix, tfidf_matrix)
#cosine similarities
results = {}
similar_indi = cosine_similarities[0].argsort()
for idx, row in dataset.iterrows():
   similar_indices = cosine_similarities[idx].argsort()[:-100:-1]
   similar_items = [(cosine_similarities[idx][i], dataset['title'][i]) for i in similar_indices]
   results[row['title']] = similar_items[1:]


########
#recs = results['The Best Blts '][:5]
def  get_Rec(foodT):
    recs=foodT
    rec_list=[]
    for reccomend in recs:
        reccomendations = results[reccomend][:3]
        for s, rec in reccomendations:
            rec_list.append(rec)
            print("Recommended: " + (rec[1]) + " (score:" +      str(rec[0]) + ")")
    return rec_list
