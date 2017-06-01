fidin = fopen('QueryImages.txt');
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
%         h = figure;
%         set(h,'name',s,'Numbertitle','off');
        [N,X]=hist(I1, [0:1:255]);    % �����ҪС���ο�һ�㣬���������ٵ㣬���԰Ѳ����Ĵ󣬱���0:5:255
        bar(X,N(:,[3 2 1]));    % ����ͼ����N(:,[3 2 1])����ΪĬ�ϻ�ͼ��ʱ����õ���ɫ˳��Ϊb,g,r,c,m,y,k����ͼƬ��rgb˳�������෴�����԰�ͼƬ�е�˳�򵹹�������ͼƬ��ɫͨ��������ʱ����ɫһ��
        xlim([0 255])
        hold on
        plot(X,N(:,[3 2 1]));    % �ϱ߽�����
        ff = fullfile('./QueryHistogram', s);
        disp(ff);
        saveas(gcf,ff,'jpg');
        hold off
    end
end

