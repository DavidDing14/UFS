# -*- coding:utf-8 -*-  
import numpy as np
import csv


#文件一共有35089个训练数据
f = open('./group1_8_groupemotion_minpicture6_shRatio_opinion01_shspanner01_18_minRatio_20_contactRatio_4.0_shRatio_12_6000_less10_withoutE.txt')
cs = open('./group.csv', 'w', newline='')
csvw = csv.writer(cs)

for i in range(35089):
    x = f.readline()
    str = x.split()
    #分割了之后，对每一行进行处理
    classid = str[0]
    if classid[0] == '?':
        str[0] = classid[1:]
    for j in range(1, 26):
        l = str[j].split(':')
        if len(l) == 2:
            str[j] = l[1]
        else:
            str[j] = 0
            print('maybe something error in ' + i + ' ' + j)
    #gender, marital, occupation, friendSize, friendEmotion, groupSize, groupEmotion, groupType, groupSHtype, groupConnectType
    dict = {'gender':-1, 'marital':-1, 'occupation':-1, 'friendSize':-1, 'friendEmotion':-1, 'groupSize':-1, 'groupEmotion':-1, 'groupType':-1, 'groupSHType':-1, 'groupConnectType':-1}
    for j in range(26, len(str)):
        l = str[j].split(':')
        if len(l) == 2:
            if l[0] in dict:
                dict[l[0]] = l[1]
        else:
            print('maybe something error in ' + i + ' ' + j)
    str1 = []
    for key in dict:
        str1.append(dict[key])
    csvw.writerow(str[:26]+str1)


cs.close()
f.close()