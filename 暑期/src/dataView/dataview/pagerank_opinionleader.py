# -*- coding: UTF-8 -*-
#wenjing
#计算已知全网络的用户的page rank数值，计算每个群组的opinion leader是那些
import numpy as np
import matplotlib.pyplot as plt
import array as array
from scipy import optimize
from numpy import *
from numpy import linalg

#----PageRank------ 
a = array([[0,1,1,0],  
           [1,0,0,1],  
           [1,0,0,1],  
           [1,1,0,0]],dtype = float)  #dtype指定为float  
  
def graphMove(a):   #构造转移矩阵  
	b = transpose(a)  #b为a的转置矩阵  
	c = zeros((a.shape),dtype = float)  #构造和a大小一样的初始化为0的矩阵
	for i in range(a.shape[0]):  
		for j in range(a.shape[1]):  
			if(b[j].sum() == 0):
				c[i][j] = 0
			else:
				c[i][j] = a[i][j] / (b[j].sum())  #完成初始化分配  
	#print c,"\n===================================================="  
	return c  
  
def firstPr(c):   #pr值得初始化  
	pr = zeros((c.shape[0],1),dtype = float)  #构造一个存放pr值得矩阵  
	for i in range(c.shape[0]):  
		pr[i] = float(1)/c.shape[0]  
	#print pr,"\n==================================================="  
	return pr  
      
def pageRank(p,m,v):  #计算pageRank值  
	while((v == p*dot(m,v) + (1-p)*v).all()==False):  #判断pr矩阵是否收敛,(v == p*dot(m,v) + (1-p)*v).all()判断前后的pr矩阵是否相等，若相等则停止循环  
		#print v  
		v = p*dot(m,v) + (1-p)*v  
		#print (v == p*dot(m,v) + (1-p)*v).all()  
	return v  
''' 
if __name__=="__main__":  
    M = graphMove(a)  
    pr = firstPr(M)  
    p = 0.8           #引入浏览当前网页的概率为p,假设p=0.8  
    print pageRank(p,M,pr)  # 计算pr值    
'''
#计算联通度 
def getConnect():
	fr = open("input/groupUsersConnect.txt")
	fw = open("output/OLalluser/gConnect/groupConnect.txt", 'w')

	line = fr.readline()
	while line: #each group
		line = line.strip("\n")
		p = line.split(" ")
		value = []
		num = 0
		value.append(len(p)-1) #number of user
		for i in range(len(p)-1):
			line = fr.readline().strip("\n")
			pp = np.array([int(l) for l in line.split(" ")])
			num += sum(pp)
		value.append(num)
		value.append(num*1.0/((len(p)-1)*(len(p)-2)))
		groupConnect[p[0]]=value
		fw.write(str(p[0]) + " " + str(value[0]) + " " + str(num) + " " + str(value[2]) + "\n")
		line = fr.readline()
	print groupConnect['360134@N24']
	fr.close()
	fw.close()

#计算每个群组的聚集度，根据文章 A SURVEY OF MODELS AND ALGORITHMS FOR SOCIAL INFLUENCE ANALYSIS
def getClustering():
	fr = open("input/groupUsersConnect.txt")
	fw = open("output/OLalluser/gConnect/groupClustering.txt", 'w')
	line = fr.readline()
	while line: #each group
		line = line.strip("\n")
		p = line.split(" ")
		A = []
		value = []
		value.append(len(p)-1) #number of user in group
		for i in range(len(p)-1):
			line = fr.readline().strip("\n")
			pp = line.split(" ")
			A.append(pp)
		A = np.array(A, dtype=float)
		a,b = np.linalg.eig(A) #特征值，a是特征值，b是特征向量
		E = A.sum() #边数
		t = 0.0
		for i in range(len(a)):
			t = t + abs(a[i] * a[i] * a[i])
		value.append(E)
		if E == 0:
			value.append(0.0)
		else:
			value.append(2*t/E)  #directed
		value.append(t) #t是三角数目
		groupClustering[p[0]] = value
		fw.write(str(p[0]) + " " + str(value[0]) + " " + str(value[1]) + " " + str(value[2]) + " " + str(t) + "\n")
		line = fr.readline()
	print groupClustering['360134@N24']
	fr.close()
	fw.close()

#计算全网络中的用户的pagerank
def getPageRank():
	fr = open("input/nsidAlias.txt")
	fr2 = open("input/userContactList.txt")
	fr3 = open("input/userAlias.txt")

	useras = []#记录useralias里名字的顺序
	nsidAlias = {}
	userAlias = {}
	userContacts = {}
	line = fr.readline()#get nsidAlias
	while line:
		line = line.strip('\n')#去掉换行符！！！！
		p = line.split(" ")
		nsidAlias[p[0]] = p[1]
		line = fr.readline()
	print "after get nsidAlias !"
	line = fr2.readline()# get userContacts
	while line:
		line = line.strip('\n')
		p = line.split(" ")
		cont = []
		for i in range(1, len(p)):
			if(nsidAlias.has_key(p[i])):
				cont.append(nsidAlias[p[i]]) # cont alias
		userContacts[p[0]] = cont
		line = fr2.readline()
	print "len user contacts ",len(userContacts)
	print "after get user Contacts !"
	print userContacts['matthiaswerner1984']
	line = fr3.readline()
	index = 0
	while line:
		line = line.strip('\n')
		userAlias[line] = index
		useras.append(line)
		line = fr3.readline()
		index += 1
	print "after get user Alias !"
	print "num of user ", index

	fww = open("output/OLalluser/Aforpr.txt", 'w')
	num = 0
	A = [([0.0] * 2605) for i in range(2605)]
	for user in userAlias:# for each user
		if(userContacts.has_key(user)):
			#print "user ", user #lockheedmartin has not contacts
			cs = userContacts[user] # contact list of each user
			#print "cs ", cs
			for c in cs:
				A[userAlias[c]][userAlias[user]] = 1.0
				num += 1
	print "num ", num #37433
	#注意：A矩阵里用户的顺序是按userAlias里确定的其实就是文件userAlias.txt里的顺序
	for uas in useras:
		fww.write(uas + " ")
	fww.write("\n")
	for i in range(2605):
		for j in range(2605):
			fww.write(str(A[i][j]) + " ")
		fww.write("\n")
	fww.close()
	M = graphMove(array(A))  
	#M = graphMove(a)  
	pr = firstPr(M)
	p = 0.8           #引入浏览当前网页的概率为p,假设p=0.8  
	PR = pageRank(p,M,pr)  # 计算pr值 
	print PR.shape #2605 * 1
	file = "output/OLalluser/pagerank.txt"
	fw = open(file, 'w')
	for i in range(PR.shape[0]):
		fw.write(useras[i] + " " + str(PR[i][0]))
		fw.write("\n")
	fw.close()
	fr.close()
	fr2.close()
	fr3.close()

def dopr():
	file = "output/pagerank-new.txt"
	pr = []
	fp = open(file)
	line = fp.readline()
	while line:
		line = line.strip("\n")
		pr.append(float(line)*(1.0e+100)*(1.0e+100)*(1.0e+100))
		line = fp.readline()
	print len(pr)
	plt.figure(4)
	plt.plot(pr, '.')
	plt.show()

def loaddata(value):
	file1 = "output/OLalluser/pagerank.txt" #this file has user and it's pagerank python ragerank
	fp = open(file1)
	users = []
	alldata = []
	line = fp.readline()
	while line:
		line = line.strip("\n")
		p = line.split(" ")
		userPR[p[0]] = float(p[1])
		userPRjava[p[0]] = 0.0
		alldata.append(float(p[1]))
		users.append(p[0])
		line = fp.readline()
	print "用户数目 %d", len(users)#
	print userPR["applesticker"]
	print len(alldata) #alldata是所有的pagerank值
	fp.close()
	fp = open("output/OLalluser/prOpinionleader.txt")#该文件是从Java版page rank算法计算出的page rank数值，就一行，按上述user顺序的page rank数值，以空格隔开
	line = fp.readline()
	line = line.strip("\n")
	p = line.split(" ")
	index = 0
	for uu in users:
		userPRjava[uu] = float(p[index])
		#alldata.append(float(p[index]))
		index += 1
	print userPRjava['bringo']
	print userPRjava["b0"]
	print "after get page rank !"
	sortdata = sorted(alldata)
	num = int(len(alldata) * value)#则sortdata的后Num个pagerank值是在opinion leader范围之内的 sortdata[len(alldata)-num, len(alldata) - 1]
	print "minPR is ", sortdata[len(alldata)-num]
	file = "output/OLalluser/OL/userOpinionleadersJava.txt";#输出文件，每行一个<useralias '1'/'0'>
	fw = open(file, 'w')
	for i in userPRjava:#for each user
		rank = userPRjava[i]
		#print rank
		fw.write(i + " ")
		if(rank >= sortdata[len(alldata)-num]):
			fw.write('1' + "\n")
		else:
			fw.write('0' + "\n")
	fw.close()
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

	#fw = open("output/OLalluser/OL/groupOLratiojava.txt", 'w')
	fw = open("output/OLalluser/OL/groupOLratio.txt", 'w')
	for g in groupUser:
		us = groupUser[g]
		op = 0
		for u in us:
			if(userPR[u] >= sortdata[len(alldata)-num]):
				op += 1

		groupOLRatio[g] = op * 1.0 / len(us)
		fw.write(g + " " + str(op) + " " + str(len(us)) + " " + str(op * 1.0 / len(us)) + "\n")
	plt.figure(5)
	#plt.plot(groupOLRatio.values(), '.')
	plt.hist(groupOLRatio.values())
	plt.title("Frequency of Opinion Leader Ratio -- python")
	plt.text(0.8, 0.8*plt.ylim()[1], "threshold " + str(value))
	#print plt.ylim()
	plt.show()
	fp.close()

#拟合函数
def fmax(x, a, b):
	return a*x+b
#time是指哪个时间片,minp是指获取groupdateRatio时该时间段的图片最小数
def maxEmotionRatioOpinionLeaderRatio(time, minp):#画图计算最大情感比例（群组情感聚集度）和群组opinion leader比例关系
	print "in function maxEmotionRatioOpinionLeaderRatio !"
	print len(groupOLRatio)
	print len(groupConnect)
	file = "input/groupsEmotionRatioDate.txt"
	fp = open(file)
	groupDateRatio = {} #<group, <date, ratio>> --- dict<dict>
	line = fp.readline()
	while line:
		p = line.split(" ")
		if(len(p) > 4 and int(p[3]) > minp):
			value = {}
			if(groupDateRatio.has_key(p[0])):
				value = groupDateRatio[p[0]]
				value[p[1]] = float(p[4])
			else:
				value[p[1]] = float(p[4])
			groupDateRatio[p[0]] = value #字典初始化
		line = fp.readline()
	print len(groupDateRatio)#8335
	#print groupDateRatio['360134@N24']#ok!!
	fp.close()

	#plot groupDateRatio and groupClustering
	'''
	xx = []
	yy = []
	zz = []
	for g in groupDateRatio:
		if(groupDateRatio[g].has_key(time) and groupClustering.has_key(g)):
			xx.append(groupDateRatio[g][time])
			yy.append(groupClustering[g][2])
			zz.append(groupClustering[g][3])
	print "xx size is %d", len(xx)
	print "yy size is %d", len(yy)
	ff = "output/OLalluser/emotionRatio_clustering" + "_"+ str(time) + ".txt"
	fw = open(ff, 'w')
	for i in range(0, len(xx)):
		fw.write(str(xx[i]) + " " + str(yy[i]) + "\n")
	fw.close()
	plt.figure(1)
	plt.plot(xx, yy, '.')
	plt.title("groupMaxEmotionRatio&&groupClustering")
	plt.xlabel("maxEmotionRatio")
	plt.ylabel("groupClustering")
	#计算相关系数
	xy = np.array([xx,yy])
	corr = np.corrcoef(xy)
	print corr #线性相关性还好
	xz = np.array([xx, zz])
	corr = np.corrcoef(xz)
	print "xz corr", corr
	plt.text(0.8, 0.8*max(yy), "correction: "+str(corr[0][1]) + "\n"+"time: "+str(time)+" minp: "+str(minp))
	plt.show()
	'''
	#plot groupDateRatio and groupConnect
	'''
	xc = []
	yc = []
	for g in groupDateRatio:
		if(groupDateRatio[g].has_key(time) and groupConnect.has_key(g)):
			xc.append(groupDateRatio[g][time])
			yc.append(groupConnect[g][2])
	print "xc size is %d", len(xc)
	print "yc size is %d", len(yc)
	ff = "output/OLalluser/emotionRatio_connectRatio" + "_"+ str(time) + ".txt"
	fw = open(ff, 'w')
	for i in range(0, len(xc)):
		fw.write(str(xc[i]) + " " + str(yc[i]) + "\n")
	fw.close()
	plt.figure(3)
	plt.plot(xc, yc, '.')
	plt.title("groupConnect&&groupMaxEmotionRatio")
	plt.xlabel("maxEmotionRatio")
	plt.ylabel("groupConnect")
	#计算相关系数
	xy = np.array([xc,yc])
	corr = np.corrcoef(xy)
	print corr #线性相关性还好
	plt.text(0.8, 0.8*max(yc), "correction: "+str(corr[0][1]) + "\n"+"time: "+str(time)+" minp: "+str(minp))
	plt.show()
	'''

	#for group emotion ratio and ol ratio
	
	x=[]
	y=[]
	for g in groupDateRatio:
		if(groupDateRatio[g].has_key(time) and groupOLRatio.has_key(g)):
			x.append(groupDateRatio[g][time])
			y.append(groupOLRatio[g])
	print "x size is %d", len(x)
	print "y size is %d", len(y)
	#ff = "output/"+str(int(100*yuzhi))+time+"-new.txt"
	ff = "output/OLalluser/emotionRatio_OLratio" + str(yuzhi) + "_"+ str(time) + ".txt"
	fw = open(ff, 'w')
	for i in range(0, len(x)):
		fw.write(str(x[i]) + " " + str(y[i]) + "\n")
	fw.close()
	plt.figure(3)
	plt.plot(x, y, '.')
	plt.title("relation between emotion ratio and opinionLeader ratio")
	plt.xlabel("maxEmotionRatio")
	plt.ylabel("opinionLeader Ratio")
	#计算相关系数
	xy = np.array([x,y])
	corr = np.corrcoef(xy)
	print corr #线性相关性都挺差,但是在emotion ratio所在的时间比较靠后时，还是有相关性的
	plt.text(0.8, 0.8*max(y), "pr yuzhi: " + str(yuzhi) + "\n"+"time: "+str(time)+" minp: "+str(minp)+"\n"+"correlation: "+str(corr[0][1]))
	#曲线拟合
	#fita,fitb=optimize.curve_fit(fmax,x,y)
	#plt.plot(x, fmax(x, fita[0], fita[1]))
	#plt.text(0.85, 0.7, "thresold 0.2 \ntime "+time)
	#print fita
	#print fitb
	plt.show()
	


if __name__ == '__main__':
	userPR = {} #每个用户的page rank值
	userPRjava = {}#每个用户的rage rank值，但是这个值是java版rage rank算法计算的, -------- 现在该算法不如python版效果好
	#groupRank = {} #读取groupOpinionleader.txt文件得到的每个群组的pagerank值。
	#alldata = []#groupRank中所有的pagerank数值, array
	groupUser = {} #读取文件groupUsersConnect.txt得到的group user对应值
	groupOLRatio = {} #每个群组的opinion leader 所占的比例
	groupConnect = {}
	groupClustering = {}
	#getPageRank()
	#dopr()
	#getConnect()
	#getClustering()
	#yuzhi=0.06
	yuzhi = 0.28 #模型效果最好的比例
	loaddata(yuzhi)
	#dataPlot("test-OpinionLeader.txt")
	#dataPlot()
	#print len(alldata)#178676
	#getOpinionLeader(yuzhi) #注意计算ol ratio 和emotion ratio关系时Minp=5,否则都不太好
	maxEmotionRatioOpinionLeaderRatio("20120601", 5) #20120406(0.03312497) 20120420(-0.0036142) 20120504(-0.03597512) 20120518(0.10270972) 20120601 之间没有 20130111

