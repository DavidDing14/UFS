# -*- coding:utf-8 -*-  
import numpy as np
import csv
import math
import pandas as pd

#25类图片属性的差别阈值
diff = 0.1

def nodiff(train1, train2):
    if len(train1) != len(train2):
        print('training data length different!!!')
        return False
    sum = 0
    for i in range(len(train1)):
        if i==25 or i==29 or i==31:
            continue
        sum = sum + (train1[i]-train2[i])**2
        if sum>diff:
            return False
    return True

if __name__ == "__main__":
    fout = open('./sum0.1_similar_result.txt', 'w+')
    #loaddata
    dataframe = pd.read_csv("./group.csv", header=None)
    dateset = dataframe.values
    trainX = dateset[:, 1:36].astype(float)
    trainY = dateset[:, 0].astype(float)
    for i in range(len(dateset)):
        for j in range(i+1, len(dateset)):
            if ((nodiff(trainX[i], trainX[j]) and trainY[i] != trainY[j])):
                #print('picture i and j are similar but different in class, i is ' + trainY[i] + 'and j is ' + trainY[j])
                fout.write('picture ' + str(i) + ' and ' + str(j) + ' are similar but different in class, ' + str(i) + ' is ' + str(trainY[i]) + ' and ' + str(j) + ' is ' + str(trainY[j]) + '\n')

    fout.close()