function [ D ] = dis( x, y )
%dis ����������������֮��ľ���
%   x��y�ֱ�Ϊ������Ƶ�Ļ�����������
min = length(x);
if length(y)<min
    min = length(y);
end
temp = 0;
for i=1:min
    temp = temp + (x(i)-y(i));
end
D = temp / min;
end

