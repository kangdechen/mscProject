# -*- coding: utf-8 -*-
"""
Created on Mon Mar 29 13:57:41 2021

@author: Acer
"""

import numpy as np
import pandas as pd
from collections import defaultdict

from surprise import Reader, Dataset, SVD, BaselineOnly, NMF, accuracy
from surprise.model_selection import train_test_split
import mysql.connector
#from mysql.connector.constants import ClientFlag

class DatasetBuilder():
    def __init__(self):
        self.config = {
                'user': 'root',
                'password': 'root',
                'host': '35.242.133.12',
                'database':'Food1Database'
                 }
        
        # now we establish our connection and get User_Rating
        self.cnxn = mysql.connector.connect(**self.config)
        self.cursor = self.cnxn.cursor()
        self.cursor.execute("Select UserId,FoodId,Rating from User_Rating")
        self.ratings=self.cursor.fetchall()
        #Surprise only take three columns
        
        self.ratings=pd.DataFrame(self.ratings,columns=['UserId','FoodId','Rating'])
        self.cursor.execute("select * from Food_Table")
        self.Fooddata=self.cursor.fetchall()
        self.Fooddata = pd.DataFrame(self.Fooddata, columns=['Id', 'title', 'rating','calories','protien','fat','sodium'])
        
        reader=Reader()
        self.dataset=Dataset.load_from_df(self.ratings[['UserId','FoodId','Rating']],reader)
        self.train_dataset,self.test_dataset=train_test_split(self.dataset, test_size=0.2)
    
    
    def get_Food_title(self,Food_id):
        self.cnxn = mysql.connector.connect(**self.config)
        self.cursor = self.cnxn.cursor()
        self.cursor.execute("Select title from Food_Table where Id ="+str(Food_id)+";")
        self.title=self.cursor.fetchall()
        str1 = ""
        # traverse in the string
        for ele in self.title[0]:
            str1 += ele
        return str(str1);
        #return self.Fooddata[self.Fooddata['Id'] == Food_id].title;


class AlgosImplemnt():
    def __init__(self, dataset):
        self.algos = []
        self.dataset = dataset
        
    def addAlgorithm(self, algo):
        self.algos.append(algo)


    def get_top_n(self,predictions, n):
            """Return the top-N recommendation for each user from a set of predictions.
        
            Args:
                predictions(list of Prediction objects): The list of predictions, as
                    returned by the test method of an algorithm.
                n(int): The number of recommendation to output for each user. Default
                    is 10.
        
            Returns:
            A dict where keys are user (raw) ids and values are lists of tuples:
                [(raw item id, rating estimation), ...] of size n.
            """
        
            # First map the predictions to each user.
            top_n = defaultdict(list)
            for uid, iid, true_r, est, _ in predictions:
                top_n[uid].append((iid, est))
        
            # Then sort the predictions for each user and retrieve the k highest ones.
            for uid, user_ratings in top_n.items():
                user_ratings.sort(key=lambda x: x[1], reverse=True)
                top_n[uid] = user_ratings[:n]
        
            return top_n 
        
    def train_and_evaluate(self):
        for algo in self.algos:
            algo.fit(self.dataset.train_dataset)
            self.predictions = algo.test(self.dataset.test_dataset)
            rmse = accuracy.rmse(self.predictions)
            mae = accuracy.mae(self.predictions)
            #fcp = accuracy.fcp(predictions)
            print('-----------')
            print(f'{algo.__class__.__name__}') 
            print('-----------')
            print(f'      Metrics - RMSE: {rmse}, MAE: {mae}')
            print('-----------')






def collaborativeBasedOnUID(UserId):
    dataset = DatasetBuilder()
    Algo = AlgosImplemnt(dataset)

    svd = SVD()
    Algo.addAlgorithm(svd)

    nmf = NMF()
    Algo.addAlgorithm(nmf)

    bsl_options = {'method': 'als',
                   'n_epochs': 5,
                   'reg_u': 12,
                   'reg_i': 5
                   }
    als = BaselineOnly(bsl_options=bsl_options)
    Algo.addAlgorithm(als)

    Algo.train_and_evaluate()
    top_n = Algo.get_top_n(Algo.predictions, n=10)
    reccomendationList=[]
    for uid, user_ratings in top_n.items():
        if(uid==UserId):
             for (iid, _) in user_ratings:
                 print(dataset.get_Food_title(iid));
                 reccomendationList.append(dataset.get_Food_title(iid));

    return reccomendationList;
            