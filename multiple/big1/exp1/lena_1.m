I = imread('lena.bmp');%读取RGB格式的图像  
J = rgb2gray(I);%用已有的函数进行RGB到灰度图像的转换  
   
[rows , cols , colors] = size(I);%得到原来图像的矩阵的参数  
K = zeros(rows , cols);%用得到的参数创建一个全零的矩阵，这个矩阵用来存储用下面的方法产生的灰度图像  
K = uint8(K);%将创建的全零矩阵转化为uint8格式，因为用上面的语句创建之后图像是double型的  
   
for i = 1:rows  
    for j = 1:cols  
        sum = 0;  
        for k = 1:colors  
            sum = sum + I(i , j , k) / 3;%进行转化的关键公式，sum每次都因为后面的数字而不能超过255  
        end  
        K(i , j) = sum;  
    end  
end  
   
%显示原来的RGB图像  
figure(1);  
subplot(3,3,1);
imshow(I);  
title('original picture');
   
%显示经过系统函数运算过的灰度图像  
subplot(3,3,2);
imshow(J);  
title('rgb2gray picture');
   
%显示转化之后的灰度图像  
subplot(3,3,3);
imshow(K);
title('self-done picture');

%DCT2变换
dct2graypic = dct2(J);
subplot(3,3,4);
imshow(dct2graypic);
title('dct2处理之后的灰度图');

%对灰度矩阵进行量化??
dct2graypic(abs(dct2graypic)<0.1)=0;

%DCT2逆变换????
idct2graypic=idct2(dct2graypic)/255;
subplot(3,3,5),imshow(idct2graypic),title('idct2之后的灰度图');

%1-D DCT变换
dctgraypic = double(zeros(rows, cols));
for i=1:rows
    dctgraypic(i,:) = dct(double(J(i,:)));
end
for i=1:cols
    dctgraypic(:,i) = dct(dctgraypic(:,i));
end
subplot(3,3,6);
imshow(dctgraypic);
title('1D DCT处理后图像');

%1-D IDCT变换
idctgraypic=idct2(dctgraypic)/255;
subplot(3,3,7);
imshow(idctgraypic);
title('1D IDCT还原图像');

%8*8 block DCT2
dct2pic = zeros(rows, cols);
for i=1:rows/8
    for j=1:cols/8
        dct2pic(i*8-7:i*8,j*8-7:j*8) = dct2(J(i*8-7:i*8,j*8-7:j*8));
    end
end
subplot(3,3,8);
imshow(dct2pic);
title('8*8 blk dct2 pic');

%8*8 block IDCT2
idct2pic = zeros(rows, cols);
for i=1:rows/8
    for j=1:cols/8
        idct2pic(i*8-7:i*8,j*8-7:j*8) = idct2(dct2pic(i*8-7:i*8,j*8-7:j*8))/255;
    end
end
subplot(3,3,9);
imshow(idct2pic);
title('8*8 blk idct2 pic');

%计算三次trials的PSNR
mse1 = 0;
mse2 = 0;
mse3 = 0;
for i=1:rows
    for j=1:cols
        mse1 = mse1 + (dct2graypic(i,j)-idct2graypic(i,j))^2;
        mse2 = mse2 + (dctgraypic(i,j)-idctgraypic(i,j))^2;
        mse3 = mse3 + (dct2pic(i,j)-idct2pic(i,j))^2;
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
