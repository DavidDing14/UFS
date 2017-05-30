
I = imread('sun.bmp');%读取RGB格式的图像  
I = imcrop(I,[0,4,312,312]);
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

J = im2double(J);

x=0.1:0.1:2;

CanonPSNR = CanonPsnr(x, J);
NikonPSNR = NikonPsnr(x, J);
JpegPSNR = JpegPsnr(x, J);

subplot(3,3,3);
plot(x,CanonPSNR);
title('Canon psnr');
grid on;
subplot(3,3,4);
plot(x,NikonPSNR);
title('Nikon psnr');
grid on;
subplot(3,3,5);
plot(x,JpegPSNR);
title('Jpeg psnr');
grid on;
subplot(3,3,6);

plot(x,CanonPSNR, x, NikonPSNR, x, JpegPSNR);
title('all psnr');
grid on;
