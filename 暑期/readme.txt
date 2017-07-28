先进行contactRatio实验，4best
之后percent实验，18best
之后minRatio实验，20best
最后shRatio实验,12best
实验最好结果为contactRatio=4.0, percent=18, minRatio=20, shRatio=12

caseStudy/	--存放案例分析的结果
	picture/	--存放主要特征相同，群组特征不同的图片组
		1/	--social role信息、连通度信息均不同的图片
		2-groupType/	--仅groupType不同的图片
		3/	--同1
		4-groupConnectType/	--仅groupConnectType不同的图片
	single_similar_25_each0.01.txt	--25维基本特征每一维特征都相差小于0.01的两张相似图片
	sum_1_similar_result.txt	--32维（除群组特征）差值平方和小于1的相似图片
	sum_0.1_similar_result.txt	--32维（除群组特征）差值平方和小于0.1的相似图片

dataRatio/	--存放4种参数调节的结果
	ol_sh/	--包含ol\sh信息的模型
		DavidDing/
			group_1_8_..	--connectRatio变化时得到的样本文件

		factorAnalyse_dxh/
			2017.7.11/
				****davidding_groupConnect.txt	--去掉groupConnectType之后的样本文件，实际上都是一样的，只需要一个即可
				answer_davidding.txt	--存储因子分析实验结果

		connectRatio_/
			answer.txt	--存储正常实验结果
			answer_order.txt	--记录实验结果的顺序


	delete_ol_sh/	--去除ol\sh的模型
		DavidDing/
			delete_ol_sh/
				group_1_8_groupemotion_minpicture6_delete_ol_sh...	--connectRatio变化时得到的样本文件
		factorAnalyse_dxh/
			2017.7.12_delete_ol_sh/
				28188010davidding-groupConnect.txt	--去掉groupConnectType之后的样本文件
				answer_davidding.txt	--最后一组是因子分析实验结果
		connectRatio/
			answer_davidding.txt	--12组实验结果
			answer_order.txt	--12组实验顺序

	percent/	--percent参数调节
		group1_8...	--不同参数的文件
		answer_davidding.txt	--实验结果
		order.txt	--结果顺序

	minRatio/	--minRation参数调节
		group1_8...	--不同参数的文件
		answer_davidding.txt	--实验结果
		order.txt	--结果顺序

	shRatio/	--shRatio参数调节
		group1_8...	--不同参数的文件
		answer_davidding.txt	--实验结果
		order.txt	--结果顺序

dataViewResult/	--连通度与群组情感的相关程度
	20120601.png	--时间节点为20120601的实验结果
	20130111.png	--时间节点为20130111的实验结果

factorAnalyse/	--存放各种因子分析的实验结果和参数顺序
	18204.012davidding-socialrole**.txt	--存放因子分析实验数据
	answer_davidding.txt	--按字典序存放上述各数据的实验结果
	canshu.txt	--解释每组数据的含义

lstmResult/	--存放lstm对比试验的实验结果
	result.png	--存放25维特征的实验结果
	35result.png	--存放35维特征（除edge特征）的实验结果

modelRatio/	--存放模型调参的结果（没有效果提升，仅供参考）（所有数据建立在原模型100、0.1的基础上）
	0.2/	--存放下降梯度0.2的实验结果
	110_0.09/	--存放110次迭代，0.09下降梯度的实验结果
	120_0.09/	--存放120次迭代，0.08下降梯度的实验结果
	sh_min/	--存放50次迭代的实验结果

src/	--实验源代码及相关操作数据
	dataOutput/	--模型训练文件的输出文件（新加代码都在文件最下方）
		ColorThemeExtractor.java
	dataView/
		dataview/	--数据观测相关代码
			connectivity.py	--连通度与情感相关性代码
	lstm/	--实验环境(windows) python3.5 keras pandas
		lstm/	--lstm对比试验代码
			case_study_similar.py	--casestudy提取两张相似图片的代码
			get_csv.py	--将group**.txt转换为的csv文件
			group.csv 	--存放group**.txt转换为的数据
			group**.txt --原始数据
			lstm_single.py	--主程序
			newTrainX.csv 	--训练特征格式改变结果
			trainX.csv 	--训练特征
			trainY.csv 	--classification

usedData/	--存放实验开始时提取的信息
	Groups_dxh/
		groupConnect.txt	--存放每个群组连通度计算结果

	getImageStatics_dxh/
		qualifiedImagesId&..	--存放每张图片的id，情感类型，用户，发布时间以及所属群组们
		Intimacy.txt	--存储所有用户之间的交互次数
		groupIntimacy.txt	--存储每个群组内部用户的关系权值矩阵