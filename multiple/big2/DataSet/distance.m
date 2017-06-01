fidin = fopen('AllImages.txt');
index = 1%�����±�
ar = [];%�洢Rֵ
ag = [];%�洢Gֵ
ab = [];%�洢Bֵ
afn = {};%�洢ͼƬ����
while ~feof(fidin)
    tline = fgetl(fidin);%��ȡAllImages.txtÿһ��
    str = deblank(tline);%ȥ������Ŀո�
    S = regexp(str, '\s+', 'split');%��ȡ�ļ���
    s = char(S(1));
    if (s ~= ' ')
        f = fullfile('./DataSet',s);%���ӳ����·��
        disp(f);
        I=imread(f);
        siz=size(I);
        I1=reshape(I,siz(1)*siz(2),siz(3));  % ÿ����ɫͨ����Ϊһ��
        I1=double(I1);
        
        [N,X]=hist(I1, [0:1:255]);    % �����ҪС���ο�һ�㣬���������ٵ㣬���԰Ѳ����Ĵ󣬱���0:5:255
        %bar(X,N(:,[3 2 1]));    % ����ͼ����N(:,[3 2 1])����ΪĬ�ϻ�ͼ��ʱ����õ���ɫ˳��Ϊb,g,r,c,m,y,k����ͼƬ��rgb˳�������෴�����԰�ͼƬ�е�˳�򵹹�������ͼƬ��ɫͨ��������ʱ����ɫһ��
        ar(:,index) = N(:,[1]);
        ag(:,index) = N(:,[2]);
        ab(:,index) = N(:,[3]);
        afn{index} = s;
        index = index + 1;
    end
end
fclose(fidin);

fidin = fopen('QueryImages.txt');
qindex = 1%�����±�
qar = [];%�洢Rֵ
qag = [];%�洢Gֵ
qab = [];%�洢Bֵ
qafn = {};%�洢ͼƬ����
while ~feof(fidin)
    tline = fgetl(fidin);%��ȡAllImages.txtÿһ��
    str = deblank(tline);%ȥ������Ŀո�
    S = regexp(str, '\s+', 'split');%��ȡ�ļ���
    s = char(S(1));
    if (s ~= ' ')
        f = fullfile('./DataSet',s);%���ӳ����·��
        disp(f);
        I=imread(f);
        siz=size(I);
        I1=reshape(I,siz(1)*siz(2),siz(3));  % ÿ����ɫͨ����Ϊһ��
        I1=double(I1);
        [N,X]=hist(I1, [0:1:255]);    % �����ҪС���ο�һ�㣬���������ٵ㣬���԰Ѳ����Ĵ󣬱���0:5:255
        %bar(X,N(:,[3 2 1]));    % ����ͼ����N(:,[3 2 1])����ΪĬ�ϻ�ͼ��ʱ����õ���ɫ˳��Ϊb,g,r,c,m,y,k����ͼƬ��rgb˳�������෴�����԰�ͼƬ�е�˳�򵹹�������ͼƬ��ɫͨ��������ʱ����ɫһ��
        qar(:,qindex) = N(:,[1]);
        qag(:,qindex) = N(:,[2]);
        qab(:,qindex) = N(:,[3]);
        qafn{qindex} = s;
        qindex = qindex + 1;    
    end
end
fclose(fidin);

%������������ݵ�׼��
%ar\ag\ab\afn�ֱ�洢������ͼƬR\G\B��ֵ��ͼƬ���·��
%qar\qag\qab\qafn�ֱ�洢��QueryͼƬR\G\B��ֵ��ͼƬ���·��

L2D16 = [];
L2D128 = [];
%����ʵ��L2 distance�㷨
for i=1:qindex-1
    for j=1:index-1
        L2D16(i,j) = L2(16,qar(:,i),qag(:,i),qab(:,i),ar(:,j),ag(:,j),ab(:,j));
        L2D128(i,j) = L2(128,qar(:,i),qag(:,i),qab(:,i),ar(:,j),ag(:,j),ab(:,j));
    end
end

%L2D16���
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

%L2D128���
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