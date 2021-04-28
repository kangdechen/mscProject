import mysql.connector

import pandas as pd
import numpy as np



def getFoodInfoByName(FoodName):
        ContentInfo=[]
        counter=1
        dataset = pd.read_csv("Dataset/epi_r.csv")
        dataset=dataset.drop_duplicates(keep="first")
        data = dataset[dataset["title"] == FoodName]
        for columns in data:
            if (counter <= 6):
                # ContentInfo += "\n"+str(columns)+":"+str(data[columns].values)+"\n"
                pass
            elif (data[columns].values > 0):

                ContentInfo.append(str(columns))

            counter = counter + 1

        return (ContentInfo)




def getfoodInfoById(FoodId):
        dataset = pd.read_csv("Dataset/epi_r.csv")

        #indexing in sql is with 1 so used -1
        foodTitle=dataset.iloc[FoodId-1]['title']
        #print(foodTitle)
        InfoList=getFoodInfoByName(foodTitle)
        return (InfoList)






#b= fd.getFoodInfoByName("Lentil, Apple, and Turkey Wrap ")
#a= fd.getfoodInfoById(799)
#print(a)
#print(b)

