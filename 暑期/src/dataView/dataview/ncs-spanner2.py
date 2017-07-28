# -*- coding: UTF-8 -*-
#wenjing
#根据结构局限算法，计算用户是否structural hole spanner
#参见 https://wenku.baidu.com/view/1998162ae009581b6ad9eba5.html
import numpy as np
import matplotlib.pyplot as plt
import array as array
from scipy import optimize
from numpy import *

# users = []

def loaddata(): #可以直接根据Aforpr.txt得到A
    file = 'output/Aforpr.txt'
    fr = open(file)
    A = [([0.0] * 2605) for i in range(2605)]
    line = fr.readline()#get the first line: user alias
    line = line.strip('\n')
    p = line.split(" ")
    # users = []
    for user in p:
        users.append(user)
    print "get all user aslias \n", len(users)
    line = fr.readline()
    row = 0 # row index in A 
    while line:
        line = line.strip('\n')#去掉换行符！！！！
        p = line.split(" ")
        for col in range(0, len(p)-1):#每行最后有个空格
            A[row][col] = p[col]
        row = row + 1
        line = fr.readline()
    print "after get A !"
    print "A[0] \n", A[0][2600:]
    fr.close()
    return A

def getP(a):
    a = np.array(a, dtype = float)
    P = [([0.0] * a.shape[0]) for i in range(a.shape[1])]
    for i in range(0, a.shape[0]):    
        #if a[i].sum() > 0: #this is for undirect 无向图
            #P[i] = a[i] * 1.0 / a[i].sum()
        #for j in range(0, a.shape[0]):
        tmp = a.sum(axis=1)[i] + a.sum(axis=0)[i]
            #for k in range(0, a.shape[0]):
                #tmp = tmp + a[i][k] + a[k][i]
        if tmp != 0:
            P[i] = (a[i] + a[:,i]) * 1.0 / tmp
        #print P[i][0]
    print "get P"
    #print P[0].sum()
    print a.sum()
    return P

def getC(p):
    p = np.array(p, dtype = float)
    #print p
    C = {}
    for i in range(p.shape[0]):
        ci = 0.0
        for j in range(p.shape[1]):
            if p[i][j] != 0 and p[j][i] != 0:
                tmp = p[i][j]
                for q in range(p.shape[1]):
                    if q != i and q != j:
                        tmp = tmp + p[i][q] * p[q][j]
                ci = ci + tmp * tmp
        C[users[i]] = ci
    print "getC"
    print len(C)
    return C

#根据yuzhi和每个用户的network constraint score 判断用户是否是sh spanner
def getSH(yuzhi):
    print "in getSH"
    file = "output/SHspanner/ncs2.txt"
    fr = open(file)
    usernsc = {}
    shdata = []
    users = []
    line = fr.readline()
    while line:
        line = line.strip('\n')#去掉换行符！！！！
        p = line.split(":")
        usernsc[p[0]] = float(p[1])
        if float(p[1]) != 0:
            shdata.append(float(p[1]))
        users.append(p[0])
        line = fr.readline()
    print len(shdata)
    print usernsc[users[10]]
    fr.close()
    sortdata = sorted(shdata) #从小到大排序,已去掉=0的
    num = int(len(users) * yuzhi)#则sortdata的前Num个scores值是在sh spanner范围之内的 sortdata[0, num-1]
    print "max score is ", sortdata[num-1]
    file = "output/SHspanner/userSHspanner_" + str(int(yuzhi*100)) + ".txt";#输出文件，每行一个<useralias '1'/'0'>
    fw = open(file, 'w')
    for u in users:
        score = usernsc[u]
        #print score
        fw.write(u + " ")
        if(score <= sortdata[num-1] and score != 0):
            fw.write('1' + "\n")
        else:
            fw.write('0' + "\n")
    fw.close()

#计算每个群组的sh ratio
def getGroupSHratio(yuzhi):
    file = "output/SHspanner/userSHspanner_" + str(int(yuzhi*100)) + ".txt";#输出文件，每行一个<useralias '1'/'0'>
    fr = open(file)
    line = fr.readline()
    userSH = {}
    while line:
        line = line.strip("\n")
        p = line.split(" ")
        userSH[p[0]] = p[1] # user and sh spanner yes(1) or no(0)
        line = fr.readline()
    fr.close()

    groupUser = {}
    n = 0
    file2 = "input/groupUsersConnect.txt"
    fp = open(file2)
    line = fp.readline()
    while line:
        line = line.strip("\n")
        p = line.split(" ")
        groupUser[p[0]] = p[1:]
        for i in range(0, len(p)-1):
            line = fp.readline()
            n += 1
        line = fp.readline()
    print "群组数目 %d ", len(groupUser)#9094
    print "n is %d " , n #178676 right!
    fp.close()

    fw = open("output/SHspanner/groupSHratio_" + str(int(yuzhi*100)) + ".txt", 'w')
    for g in groupUser:
        us = groupUser[g]
        op = 0
        for u in us:
            if(userSH[u] == "1"):
                op += 1

        #groupOLRatio[g] = op * 1.0 / len(us)
        fw.write(g + " " + str(op) + " " + str(len(us)) + " " + str(op * 1.0 / len(us)) + "\n")
    fw.close()

def getRatioEmotion(yuzhi, time, minp):
    file = "output/SHspanner/groupSHratio_" + str(int(yuzhi*100)) + ".txt"
    fr = open(file)
    groupSHratio = {}
    line = fr.readline()
    while line:
        line = line.strip("\n")
        p = line.split(" ")
        groupSHratio[p[0]] = float(p[3])
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
        if(groupDateRatio[g].has_key(time) and groupSHratio.has_key(g)):
            x.append(groupDateRatio[g][time])
            y.append(groupSHratio[g])
    print "x size is %d", len(x)
    print "y size is %d", len(y)

    #write x and y to a file for plot in excel
    fw = open("output/emotionRatio&shRatio_" + str(time) + "_" + str(yuzhi) + "_" + str(minp) + ".txt", "w")
    for i in range(len(x)):
        fw.write(str(x[i]) + " " + str(y[i]) + "\n")
    fw.close()
    plt.figure()
    plt.plot(x, y, '.')
    plt.title("relation between emotion ration and SH spanner ratio")
    plt.xlabel("maxEmotionRatio")
    plt.ylabel("SH spanner Ratio")
    #计算相关系数
    xy = np.array([x,y])
    corr = np.corrcoef(xy)
    print corr #线性相关性都挺差,但是在emotion ratio所在的时间比较靠后时，还是有相关性的
    plt.text(0.8, 0.8*max(y), "pr yuzhi: " + str(yuzhi) + "\n"+"time: "+str(time)+" minp: "+str(minp)+"\n"+"correlation: "+str(corr[0][1]))
    plt.show()

if __name__ == '__main__':
    '''
    users = []
    A = loaddata() #得到邻接矩阵
    P = getP(A) #
    C = {}
    #C['bringo'] = 1
    C = getC(P)
    file = "output/OLalluser/SHspanner/ncs2.txt"
    fw = open(file, 'w')
    for i in range(2605):
        fw.write(users[i] + ":" + str(C[users[i]]) + "\n")
    fw.close()
    '''
    yuzhi = 0.28 # 0.06 0.1 0.14 0.18 0.2 0.24 0.28 0.3
    #getSH(yuzhi)
    #getGroupSHratio(yuzhi)
    getRatioEmotion(yuzhi, "20120601", 5)  #20120601 20130111
    '''
    users = ['a', 'b', 'c', 'd']
    A = np.array([[0, 1, 1, 0], [1, 0, 1, 0], [1, 1, 0, 1], [0, 0, 1, 0]], dtype = float)
    P = getP(A)
    print P
    C = getC(P)
    print C
    '''