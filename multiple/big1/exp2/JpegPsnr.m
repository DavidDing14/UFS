function [ s ] = JpegPsnr( a, J)
%UNTITLED6 此处显示有关此函数的摘要
%   a表示a * Jpeg的系数a
%   J表示原矩阵
%   返回值s 表示计算出来的psnr值
Jpeg = [16,11,10,16,24,40,51,61;
    12,12,14,19,26,58,60,55;
    14,13,16,24,40,57,59,56;
    14,17,22,19,51,87,80,62;
    18,22,37,56,68,109,103,77;
    24,35,55,64,81,104,113,92;
    49,64,78,87,103,121,120,101;
    72,92,95,98,112,100,103,99];

nums = length(a);
mse = zeros(nums);
psnr = zeros(nums);
[rows, cols, colors] = size(J);
T = dctmtx(8);
fun1 = @(block_struct) T * (block_struct.data) * T';
fun2 = @(block_struct) T' * (block_struct.data) * T;

for k=1:nums
    
    Jpegk = a(k) * Jpeg;
    blockpic = blockproc(J, [8,8], fun1);

    JpegPic = blockproc(blockpic, [8,8], @(block_struct)Jpegk .* block_struct.data);

    iJpegPic = blockproc(JpegPic, [8,8], fun2);

    for i=1:rows
        for j=1:cols
            mse(k) = mse(k) + (J(i,j)-iJpegPic(i,j))^2;
        end
    end

    mse(k) = mse(k) / (rows*cols);

    psnr(k) = 10*log10(255^2/mse(k));
end

s = psnr;

end

