fidin = fopen('AllImages.txt');
index = 1%数组下标
ar = [];%存储R值
ag = [];%存储G值
ab = [];%存储B值
afn = {};%存储图片名称
while ~feof(fidin)
    tline = fgetl(fidin);%读取AllImages.txt每一行
    str = deblank(tline);%去除后面的空格
    S = regexp(str, '\s+', 'split');%提取文件名
    s = char(S(1));
    if (s ~= ' ')
        f = fullfile('./DataSet',s);%链接出相对路径
        disp(f);
        I=imread(f);
        siz=size(I);
        I1=reshape(I,siz(1)*siz(2),siz(3));  % 每个颜色通道变为一列
        I1=double(I1);
        
        [N,X]=hist(I1, [0:1:255]);    % 如果需要小矩形宽一点，划分区域少点，可以把步长改大，比如0:5:255
        %bar(X,N(:,[3 2 1]));    % 柱形图，用N(:,[3 2 1])是因为默认绘图的时候采用的颜色顺序为b,g,r,c,m,y,k，跟图片的rgb顺序正好相反，所以把图片列的顺序倒过来，让图片颜色通道跟绘制时的颜色一致
        ar(:,index) = N(:,[1]);
        ag(:,index) = N(:,[2]);
        ab(:,index) = N(:,[3]);
        afn{index} = s;
        index = index + 1;
    end
end
fclose(fidin);

fidin = fopen('QueryImages.txt');
qindex = 1%数组下标
qar = [];%存储R值
qag = [];%存储G值
qab = [];%存储B值
qafn = {};%存储图片名称
while ~feof(fidin)
    tline = fgetl(fidin);%读取AllImages.txt每一行
    str = deblank(tline);%去除后面的空格
    S = regexp(str, '\s+', 'split');%提取文件名
    s = char(S(1));
    if (s ~= ' ')
        f = fullfile('./DataSet',s);%链接出相对路径
        disp(f);
        I=imread(f);
        siz=size(I);
        I1=reshape(I,siz(1)*siz(2),siz(3));  % 每个颜色通道变为一列
        I1=double(I1);
        [N,X]=hist(I1, [0:1:255]);    % 如果需要小矩形宽一点，划分区域少点，可以把步长改大，比如0:5:255
        %bar(X,N(:,[3 2 1]));    % 柱形图，用N(:,[3 2 1])是因为默认绘图的时候采用的颜色顺序为b,g,r,c,m,y,k，跟图片的rgb顺序正好相反，所以把图片列的顺序倒过来，让图片颜色通道跟绘制时的颜色一致
        qar(:,qindex) = N(:,[1]);
        qag(:,qindex) = N(:,[2]);
        qab(:,qindex) = N(:,[3]);
        qafn{qindex} = s;
        qindex = qindex + 1;    
    end
end
fclose(fidin);

%现在完成了数据的准备
%ar\ag\ab\afn分别存储了所有图片R\G\B数值和图片相对路径
%qar\qag\qab\qafn分别存储了Query图片R\G\B数值和图片相对路径

L2D16 = [];
L2D128 = [];
%下面实现L2 distance算法
for i=1:qindex-1
    for j=1:index-1
        L2D16(i,j) = L2(16,qar(:,i),qag(:,i),qab(:,i),ar(:,j),ag(:,j),ab(:,j));
        L2D128(i,j) = L2(128,qar(:,i),qag(:,i),qab(:,i),ar(:,j),ag(:,j),ab(:,j));
    end
end

%L2D16输出
for k=1:qindex-1
    index_order = [];
    for i=1:index-1
        index_order(i) = i;
    end
    for i=1:index-1
        for j=i+1:index-1
            if(L2D16(k,i)>L2D16(k,j))
                t = L2D16(k,i);
                L2D16(k,i) = L2D16(k,j);
                L2D16(k,j) = t;
                t = index_order(i);
                index_order(i) = index_order(j);
                index_order(j) = t;
            end
        end
    end
    str1 = char(qafn{k});
    for i=1:length(str1)
        if(str1(i) == '/')
            str1(i) = '_';
            break;
        end
    end
    name = ['res_', str1(1:end-4), '.txt'];
    filen = fullfile('./distance','L2D16',name);
    fid = fopen(filen, 'a+');
    for i=1:30
        fprintf(fid, '%s %f\n', char(afn{index_order(i)}), L2D16(k, i));
    end
    fclose(fid);
end

%L2D128输出
for k=1:qindex-1
    index_order = [];
    for i=1:index-1
        index_order(i) = i;
    end
    for i=1:index-1
        for j=i+1:index-1
            if(L2D128(k,i)>L2D128(k,j))
                t = L2D128(k,i);
                L2D128(k,i) = L2D128(k,j);
                L2D128(k,j) = t;
                t = index_order(i);
                index_order(i) = index_order(j);
                index_order(j) = t;
            end
        end
    end
    str1 = char(qafn{k});
    for i=1:length(str1)
        if(str1(i) == '/')
            str1(i) = '_';
            break;
        end
    end
    name = ['res_', str1(1:end-4), '.txt'];
    filen = fullfile('./distance','L2D128',name);
    fid = fopen(filen, 'a+');
    for i=1:30
        fprintf(fid, '%s %f\n', char(afn{index_order(i)}), L2D128(k, i));
    end
    fclose(fid);
end