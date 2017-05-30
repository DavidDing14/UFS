function [ s ] = NikonPsnr( a, J)
%UNTITLED6 此处显示有关此函数的摘要
%   a表示a * Nikon的系数a
%   J表示原矩阵
%   返回值s 表示计算出来的psnr值
Nikon = [2,1,1,2,3,5,6,7;
    1,1,2,2,3,7,7,7;
    2,2,2,3,5,7,8,7;
    2,2,3,3,6,10,10,7;
    2,3,4,7,8,13,12,9;
    3,4,7,8,10,12,14,11;
    6,8,9,10,12,15,14,12;
    9,11,11,12,13,12,12,12];

nums = length(a);
mse = zeros(nums);
psnr = zeros(nums);
[rows, cols, colors] = size(J);
T = dctmtx(8);
fun1 = @(block_struct) T * (block_struct.data) * T';
fun2 = @(block_struct) T' * (block_struct.data) * T;

for k=1:nums
    
    Nikonk = a(k) * Nikon;
    
    blockpic = blockproc(J, [8,8], fun1);

    NikonPic = blockproc(blockpic, [8,8], @(block_struct)Nikonk .* block_struct.data);

    iNikonPic = blockproc(NikonPic, [8,8], fun2);

    for i=1:rows
        for j=1:cols
            mse(k) = mse(k) + (J(i,j)-iNikonPic(i,j))^2;
        end
    end

    mse(k) = mse(k) / (rows*cols);

    psnr(k) = 10*log10(255^2/mse(k));

end

s = psnr;

end

