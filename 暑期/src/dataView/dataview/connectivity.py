# -*- coding: UTF-8 -*-
# DavidDing
# 群组连通度与群组情感之间的数据观测
import numpy as np
import matplotlib.pyplot as plt
import array as array
from scipy import optimize
from numpy import *

def f_1(x, A, B):
    return A*x + B

def getRatioEmotion(time, minp):
    file = "input/groupConnect.txt"
    fr = open(file)
    groupConnectratio = {}
    line = fr.readline()
    while line:
        line = line.strip("\n")
        p = line.split(" ")
        groupConnectratio[p[0]] = float(p[3])
        line = fr.readline()
    fr.close()
    file2 = "input/groupsEmotionRatioDate.txt"
    fr2 = open(file2)
    groupDateRatio = {} #<group, <date, ratio>> --- dict<dict>
    line = fr2.readline()
    while line:
        line = line.strip("\n")
        p = line.split(" ")
        if(len(p) > 4 and int(p[3]) > minp):
            value = {}
            if(groupDateRatio.has_key(p[0])):
                value = groupDateRatio[p[0]]
                value[p[1]] = float(p[4])
            else:
                value[p[1]] = float(p[4])
            groupDateRatio[p[0]] = value #字典初始化
        line = fr2.readline()
    print len(groupDateRatio)#8335
    #print groupDateRatio['360134@N24']#ok!!
    fr2.close()

    #for group emotion ratio and sh ratio
    x=[]
    y=[]
    for g in groupDateRatio:
        if(groupDateRatio[g].has_key(time) and groupConnectratio.has_key(g)):
            x.append(groupDateRatio[g][time])
            y.append(groupConnectratio[g])
    print "x size is %d", len(x)
    print "y size is %d", len(y)

    plt.figure()

    A1, B1 = optimize.curve_fit(f_1, x, y)[0]
    x1 = np.arange(0, 1, 0.01)
    y1 = A1*x1 + B1
    plt.plot(x1, y1, 'g--')

    #write x and y to a file for plot in excel
    fw = open("output/emotionRatio&ConnectRatio_" + str(time) + "_" + str(minp) + ".txt", "w")
    for i in range(len(x)):
        fw.write(str(x[i]) + " " + str(y[i]) + "\n")
    fw.close()
    plt.plot(x, y, '.')
    plt.title("relation between emotion ration and Connect ratio")
    plt.xlabel("maxEmotionRatio")
    plt.ylabel("Connect Ratio")
    #计算相关系数
    xy = np.array([x,y])
    corr = np.corrcoef(xy)
    print corr #线性相关性都挺差,但是在emotion ratio所在的时间比较靠后时，还是有相关性的
    plt.text(0.8, 0.8*max(y), "time: "+str(time)+" minp: "+str(minp)+"\n"+"correlation: "+str(corr[0][1]))
    plt.show()

if __name__ == '__main__':
    getRatioEmotion("20120601", 4)  #20120601 20130111