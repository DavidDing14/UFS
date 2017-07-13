先进行contactRatio实验，4best
之后percent实验，18best
之后minRatio实验，20best
最后shRatio实验,

Groups_dxh/
	groupConnect.txt	--存放每个群组连通度计算结果

getImageStatics_dxh/
	qualifiedImagesId&..	--存放每张图片的id，情感类型，用户，发布时间以及所属群组们
	Intimacy.txt	--存储所有用户之间的交互次数
	groupIntimacy.txt	--存储每个群组内部用户的关系权值矩阵

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