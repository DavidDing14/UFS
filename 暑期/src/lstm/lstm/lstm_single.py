from __future__ import print_function
import keras
from keras.models import Sequential
import keras.utils.np_utils as kutils
from keras.layers import *
from keras import backend as K
import scipy.io as sio
import numpy as np
import pandas as pd
from keras.models import load_model
from keras.models import Sequential
from keras.layers import Dense, Dropout
from keras.wrappers.scikit_learn import KerasClassifier
from keras.utils import np_utils
from sklearn.model_selection import train_test_split, KFold, cross_val_score
from sklearn.preprocessing import LabelEncoder
import os
import random
import csv
import time
from keras.callbacks import ModelCheckpoint
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'


batch_size = 32
num_classes = 6
epochs = 100
size = 35089

time_steps = 35
length = 1


def index(predict):
    # return L.index(max(L))
    flag = 0
    max_m = predict[0]
    for i in range(0,6):
        if predict[i] > max_m:
            flag = i
            max_m = predict[i]
    return flag


def div(x, y):
    if y == 0:
        return 0
    else:
        return x*1.0/y


def f1_score(model, testX, testY):
    c1 = [0] * 6
    c2 = [0] * 6
    c3 = [0] * 6
    c4 = [([0] * 6) for i in range(6)]
    f1 = 0
    predict = model.predict(testX, batch_size=batch_size)
    for j in range(0, len(testY)):
        c4[index(testY[j])][index(predict[j])] += 1;
        if (index(predict[j]) == index(testY[j])):
            c1[index(predict[j])] += 1
        c2[index(testY[j])] += 1
        c3[index(predict[j])] += 1
    iprecision = [([0]) for i in range(6)]
    irecall = [([0]) for i in range(6)]
    if1 = [([0]) for i in range(6)]
    for i in range(0,6):
        print(c4[i])
    for i in range(0,6):
        iprecision[i] = div(c4[i][i], sum(c4[i]));
        irecall[i] = div(c4[i][i], sum(sum(c4,[])[i::6]));
        if1[i] = div(2*iprecision[i]*irecall[i],(iprecision[i]+irecall[i]));
        f1 += if1[i];
        print(i + 1, ': ', '\t precision: ', '%.3f'%iprecision[i], ' \t recall: ', '%.3f'%irecall[i], '\t f1-score: ', '%.3f'%if1[i])
    print(div(f1,6))
    #model.save('tmp/model_single_64_' + time.strftime('%m-%d-%X',time.localtime(time.time())) + '.h5')
    return 0

def cal_model(trainX, trainY, testX, testY):
    input_shape = (time_steps, length)

    # model = Sequential()
    # model.add(Masking(mask_value=-20., input_shape=input_shape))
    # model.add(TimeDistributed(Dense(256, activation='linear',input_shape=input_shape)))
    # model.add(TimeDistributed(Dense(256, activation='tanh', input_shape=input_shape)))
    # model.add(LSTM(256, return_sequences=True, input_shape=input_shape))
    # model.add(LSTM(256, input_shape=input_shape))
    # model.add(Dense(6, activation='softmax'))
    model = Sequential()
    model.add(Masking(mask_value=-20, input_shape=input_shape))
    model.add(Dense(128, activation='relu', input_shape=input_shape))
    model.add(LSTM(100, return_sequences=True, activation='relu', input_shape=input_shape))
    model.add(LSTM(100, activation ='relu',input_shape=input_shape))
    model.add(Dense(6, activation='softmax'))

    model.summary()
    model.compile(loss=keras.losses.categorical_crossentropy, optimizer = keras.optimizers.Adam(lr=0.0001, beta_1=0.9, beta_2=0.999, epsilon=1e-08), metrics=['accuracy'])
    for i in range(epochs):
        model.fit(trainX, trainY, batch_size=batch_size, epochs=1, verbose=1)
        f1_score(model, testX, testY)
    return model

if __name__ == "__main__":
    #fy = open('./trainY.csv', 'w', newline='')
    #fx = open('./trainX.csv', 'w', newline='')
    #fn = open('./newTrainX.csv', 'w', newline='')
    #csvw = csv.writer(fn)
    #loaddata
    dataframe = pd.read_csv("./group.csv", header=None)
    dateset = dataframe.values
    trainX = dateset[:, 1:36].astype(float)
    trainY = dateset[:, 0]

    newtrainX = []
    for i in range(len(trainX)):
        newtrainX.extend(trainX[i])
    trainX = np.array(newtrainX)
    #csvw.writerow(newtrainX)

    # encode class values as integers
    encoder = LabelEncoder()
    encoded_Y = encoder.fit_transform(trainY)
    # convert integers to dummy variables (one hot encoding)
    dummy_y = np_utils.to_categorical(encoded_Y)

    #输出每张图片的情感list，例如[1,0,0,0,0,0]表示happy
    #for i in range(len(dummy_y)):
    #    csvw.writerow(dummy_y[i])

    #输出每张图片的属性
    #for i in range(len(trainX)):
    #    csvw.writerow(trainX[i])

    trainX1 = trainX.reshape(size, time_steps, length)
    #trainY1 = trainY.reshape(size, num_classes)

    trainX = trainX1[:20000]
    trainY = dummy_y[:20000]

    testX = trainX1[20000:]
    testY = dummy_y[20000:]
    cal_model(trainX, trainY, testX, testY)

    #fn.close()
    #fx.close()
    #fy.close()