I = imread('lena.bmp');%读取RGB格式的图像  
J = rgb2gray(I);%用已有的函数进行RGB到灰度图像的转换  
[rows, cols, colors] = size(I);

fprintf('rows = %d, cols = %d\n', rows, cols);

figure(1);
subplot(3,3,1);
imshow(I);
title('原始图像');

subplot(3,3,2);
imshow(J);
title('灰化图像');

Canon = [1,1,1,2,3,6,8,10;
    1,1,2,3,4,8,9,8;
    2,2,2,3,6,8,10,8;
    2,2,3,4,7,12,11,9;
    3,3,8,11,10,16,15,11;
    3,5,8,10,12,15,16,13;
    7,10,11,12,15,17,17,14;
    14,13,13,15,15,14,14,14];

Nikon = [2,1,1,2,3,5,6,7;
    1,1,2,2,3,7,7,7;
    2,2,2,3,5,7,8,7;
    2,2,3,3,6,10,10,7;
    2,3,4,7,8,13,12,9;
    3,4,7,8,10,12,14,11;
    6,8,9,10,12,15,14,12;
    9,11,11,12,13,12,12,12];

Jpeg = [16,11,10,16,24,40,51,61;
    12,12,14,19,26,58,60,55;
    14,13,16,24,40,57,59,56;
    14,17,22,19,51,87,80,62;
    18,22,37,56,68,109,103,77;
    24,35,55,64,81,104,113,92;
    49,64,78,87,103,121,120,101;
    72,92,95,98,112,100,103,99];



J = im2double(J);
T = dctmtx(8);

fun1 = @(block_struct) T * (block_struct.data) * T';

blockpic = blockproc(J,[8,8],fun1);
subplot(3,3,3);
imshow(blockpic);
title('分块并dct2处理后图像');

CanonPic = blockproc(blockpic, [8,8], @(block_struct)Canon .* block_struct.data);
subplot(3,3,4);
imshow(CanonPic);
title('Canon Pic');


fun2 = @(block_struct) T' * (block_struct.data) * T;

iblockpic = blockproc(CanonPic,[8,8],fun2);
subplot(3,3,5);
imshow(iblockpic);
title('Canon idct2');

NikonPic = blockproc(blockpic, [8,8], @(block_struct)Nikon .* block_struct.data);
subplot(3,3,6);
imshow(NikonPic);
title('Nikon Pic');

iNikonPic = blockproc(NikonPic,[8,8],fun2);
subplot(3,3,7);
imshow(iNikonPic);
title('Nikon idct2');

JpegPic = blockproc(blockpic, [8,8], @(block_struct)Jpeg .* block_struct.data);
subplot(3,3,8);
imshow(JpegPic);
title('Jpeg Pic');

iJpegPic = blockproc(JpegPic,[8,8],fun2);
subplot(3,3,9);
imshow(iJpegPic);
title('Jpeg idct2');

%计算三次trials的PSNR
mse1 = 0;
mse2 = 0;
mse3 = 0;
for i=1:rows
    for j=1:cols
        mse1 = mse1 + (J(i,j)-iblockpic(i,j))^2;
        mse2 = mse2 + (J(i,j)-iNikonPic(i,j))^2;
        mse3 = mse3 + (J(i,j)-iJpegPic(i,j))^2;
    end
end
mse1 = mse1/(rows*cols);
mse2 = mse2/(rows*cols);
mse3 = mse3/(rows*cols);
fprintf('mse1 = %d, mse2 = %d, mse3 = %d\n', mse1, mse2, mse3);
psnr1 = 10*log10(255^2/mse1);
psnr2 = 10*log10(255^2/mse2);
psnr3 = 10*log10(255^2/mse3);
fprintf('psnr1 = %d, psnr2 = %d, psnr3 = %d\n', psnr1, psnr2, psnr3);
