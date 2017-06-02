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
qpre = [];%every query image precision
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
    fid = fopen(filen, 'w');
    t_pre = 0;
    for i=1:30
        t_pre = t_pre + L2D16(k,i);
        fprintf(fid, '%s %f\n', char(afn{index_order(i)}), L2D16(k, i));
    end
    t_pre = t_pre / 30;
    qpre(k) = t_pre;
    fclose(fid);
end

fid = fopen('./distance/L2D16/res_overall.txt', 'w');
overall = 0;
for i=1:qindex-1
    overall = overall + qpre(i);
    fprintf(fid, '%s %f\n', char(qafn{i}), qpre(i));
end
overall = overall / (qindex-1);
fprintf(fid, '%f\n', overall);
fclose(fid);

%L2D128输出
qpre = [];%every query image precision
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
    fid = fopen(filen, 'w');
    t_pre = 0;
    for i=1:30
        t_pre = t_pre + L2D128(k,i);
        fprintf(fid, '%s %f\n', char(afn{index_order(i)}), L2D128(k, i));
    end
    t_pre = t_pre / 30;
    qpre(k) = t_pre;
    fclose(fid);
end

fid = fopen('./distance/L2D128/res_overall.txt', 'w');
overall = 0;
for i=1:qindex-1
    overall = overall + qpre(i);
    fprintf(fid, '%s %f\n', char(qafn{i}), qpre(i));
end
overall = overall / (qindex-1);
fprintf(fid, '%f\n', overall);
fclose(fid);

HID16 = [];
HID128 = [];
%下面实现HI distance算法
for i=1:qindex-1
    for j=1:index-1
        HID16(i,j) = HI(16,qar(:,i),qag(:,i),qab(:,i),ar(:,j),ag(:,j),ab(:,j));
        HID128(i,j) = HI(128,qar(:,i),qag(:,i),qab(:,i),ar(:,j),ag(:,j),ab(:,j));
    end
end

%HID16输出
qpre = [];%every query image precision
for k=1:qindex-1
    index_order = [];
    for i=1:index-1
        index_order(i) = i;
    end
    for i=1:index-1
        for j=i+1:index-1
            if(HID16(k,i)>HID16(k,j))
                t = HID16(k,i);
                HID16(k,i) = HID16(k,j);
                HID16(k,j) = t;
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
    filen = fullfile('./distance','HID16',name);
    fid = fopen(filen, 'w');
    t_pre = 0;
    for i=1:30
        t_pre = t_pre + HID16(k,i);
        fprintf(fid, '%s %f\n', char(afn{index_order(i)}), HID16(k, i));
    end
    t_pre = t_pre / 30;
    qpre(k) = t_pre;
    fclose(fid);
end

fid = fopen('./distance/HID16/res_overall.txt', 'w');
overall = 0;
for i=1:qindex-1
    overall = overall + qpre(i);
    fprintf(fid, '%s %f\n', char(qafn{i}), qpre(i));
end
overall = overall / (qindex-1);
fprintf(fid, '%f\n', overall);
fclose(fid);

%HID128输出
qpre = [];%every query image precision
for k=1:qindex-1
    index_order = [];
    for i=1:index-1
        index_order(i) = i;
    end
    for i=1:index-1
        for j=i+1:index-1
            if(HID128(k,i)>HID128(k,j))
                t = HID128(k,i);
                HID128(k,i) = HID128(k,j);
                HID128(k,j) = t;
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
    filen = fullfile('./distance','HID128',name);
    fid = fopen(filen, 'w');
    t_pre = 0;
    for i=1:30
        t_pre = t_pre + HID128(k,i);
        fprintf(fid, '%s %f\n', char(afn{index_order(i)}), HID128(k, i));
    end
    t_pre = t_pre / 30;
    qpre(k) = t_pre;
    fclose(fid);
end

fid = fopen('./distance/HID128/res_overall.txt', 'w');
overall = 0;
for i=1:qindex-1
    overall = overall + qpre(i);
    fprintf(fid, '%s %f\n', char(qafn{i}), qpre(i));
end
overall = overall / (qindex-1);
fprintf(fid, '%f\n', overall);
fclose(fid);

HID16 = [];
HID128 = [];
%下面实现Bh distance算法
for i=1:qindex-1
    for j=1:index-1
        BhD16(i,j) = Bh(16,qar(:,i),qag(:,i),qab(:,i),ar(:,j),ag(:,j),ab(:,j));
        BhD128(i,j) = Bh(128,qar(:,i),qag(:,i),qab(:,i),ar(:,j),ag(:,j),ab(:,j));
    end
end

%BhD16输出
qpre = [];%every query image precision
for k=1:qindex-1
    index_order = [];
    for i=1:index-1
        index_order(i) = i;
    end
    for i=1:index-1
        for j=i+1:index-1
            if(BhD16(k,i)>BhD16(k,j))
                t = BhD16(k,i);
                BhD16(k,i) = BhD16(k,j);
                BhD16(k,j) = t;
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
    filen = fullfile('./distance','BhD16',name);
    fid = fopen(filen, 'w');
    t_pre = 0;
    for i=1:30
        t_pre = t_pre + BhD16(k,i);
        fprintf(fid, '%s %f\n', char(afn{index_order(i)}), BhD16(k, i));
    end
    t_pre = t_pre / 30;
    qpre(k) = t_pre;
    fclose(fid);
end

fid = fopen('./distance/BhD16/res_overall.txt', 'w');
overall = 0;
for i=1:qindex-1
    overall = overall + qpre(i);
    fprintf(fid, '%s %f\n', char(qafn{i}), qpre(i));
end
overall = overall / (qindex-1);
fprintf(fid, '%f\n', overall);
fclose(fid);

%BhD128输出
qpre = [];%every query image precision
for k=1:qindex-1
    index_order = [];
    for i=1:index-1
        index_order(i) = i;
    end
    for i=1:index-1
        for j=i+1:index-1
            if(BhD128(k,i)>BhD128(k,j))
                t = BhD128(k,i);
                BhD128(k,i) = BhD128(k,j);
                BhD128(k,j) = t;
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
    filen = fullfile('./distance','BhD128',name);
    fid = fopen(filen, 'w');
    t_pre = 0;
    for i=1:30
        t_pre = t_pre + BhD128(k,i);
        fprintf(fid, '%s %f\n', char(afn{index_order(i)}), BhD128(k, i));
    end
    t_pre = t_pre / 30;
    qpre(k) = t_pre;
    fclose(fid);
end

fid = fopen('./distance/BhD128/res_overall.txt', 'w');
overall = 0;
for i=1:qindex-1
    overall = overall + qpre(i);
    fprintf(fid, '%s %f\n', char(qafn{i}), qpre(i));
end
overall = overall / (qindex-1);
fprintf(fid, '%f\n', overall);
fclose(fid);