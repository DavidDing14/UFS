function [ s ] = CanonPsnr( a, J)
%UNTITLED6 �˴���ʾ�йش˺�����ժҪ
%   a��ʾa * Canon��ϵ��a
%   J��ʾԭ����
%   ����ֵs ��ʾ���������psnrֵ
Canon = [1,1,1,2,3,6,8,10;
    1,1,2,3,4,8,9,8;
    2,2,2,3,6,8,10,8;
    2,2,3,4,7,12,11,9;
    3,3,8,11,10,16,15,11;
    3,5,8,10,12,15,16,13;
    7,10,11,12,15,17,17,14;
    14,13,13,15,15,14,14,14];

nums = length(a);
mse = zeros(nums);
psnr = zeros(nums);
[rows, cols, colors] = size(J);
T = dctmtx(8);
fun1 = @(block_struct) T * (block_struct.data) * T';
fun2 = @(block_struct) T' * (block_struct.data) * T;

for k=1:nums
    
    Canonk = a(k) * Canon;
    
    blockpic = blockproc(J, [8,8], fun1);

    CanonPic = blockproc(blockpic, [8,8], @(block_struct)Canonk .* block_struct.data);

    iCanonPic = blockproc(CanonPic, [8,8], fun2);

    for i=1:rows
        for j=1:cols
            mse(k) = mse(k) + (J(i,j)-iCanonPic(i,j))^2;
        end
    end

    mse(k) = mse(k) / (rows*cols);

    psnr(k) = 10*log10(255^2/mse(k));

end

s = psnr;

end

