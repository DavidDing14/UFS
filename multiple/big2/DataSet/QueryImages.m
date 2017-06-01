fidin = fopen('QueryImages.txt');
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
%         h = figure;
%         set(h,'name',s,'Numbertitle','off');
        [N,X]=hist(I1, [0:1:255]);    % 如果需要小矩形宽一点，划分区域少点，可以把步长改大，比如0:5:255
        bar(X,N(:,[3 2 1]));    % 柱形图，用N(:,[3 2 1])是因为默认绘图的时候采用的颜色顺序为b,g,r,c,m,y,k，跟图片的rgb顺序正好相反，所以把图片列的顺序倒过来，让图片颜色通道跟绘制时的颜色一致
        xlim([0 255])
        hold on
        plot(X,N(:,[3 2 1]));    % 上边界轮廓
        ff = fullfile('./QueryHistogram', s);
        disp(ff);
        saveas(gcf,ff,'jpg');
        hold off
    end
end

