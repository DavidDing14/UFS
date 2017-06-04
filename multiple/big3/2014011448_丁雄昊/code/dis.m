function [ D ] = dis( x, y )
%dis 计算两个基音周期之间的距离
%   x，y分别为两个音频的基音周期向量
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

