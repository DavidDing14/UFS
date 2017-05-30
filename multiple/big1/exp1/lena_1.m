I = imread('lena.bmp');%��ȡRGB��ʽ��ͼ��  
J = rgb2gray(I);%�����еĺ�������RGB���Ҷ�ͼ���ת��  
   
[rows , cols , colors] = size(I);%�õ�ԭ��ͼ��ľ���Ĳ���  
K = zeros(rows , cols);%�õõ��Ĳ�������һ��ȫ��ľ���������������洢������ķ��������ĻҶ�ͼ��  
K = uint8(K);%��������ȫ�����ת��Ϊuint8��ʽ����Ϊ���������䴴��֮��ͼ����double�͵�  
   
for i = 1:rows  
    for j = 1:cols  
        sum = 0;  
        for k = 1:colors  
            sum = sum + I(i , j , k) / 3;%����ת���Ĺؼ���ʽ��sumÿ�ζ���Ϊ��������ֶ����ܳ���255  
        end  
        K(i , j) = sum;  
    end  
end  
   
%��ʾԭ����RGBͼ��  
figure(1);  
subplot(3,3,1);
imshow(I);  
title('original picture');
   
%��ʾ����ϵͳ����������ĻҶ�ͼ��  
subplot(3,3,2);
imshow(J);  
title('rgb2gray picture');
   
%��ʾת��֮��ĻҶ�ͼ��  
subplot(3,3,3);
imshow(K);
title('self-done picture');

%DCT2�任
dct2graypic = dct2(J);
subplot(3,3,4);
imshow(dct2graypic);
title('dct2����֮��ĻҶ�ͼ');

%�ԻҶȾ����������??
dct2graypic(abs(dct2graypic)<0.1)=0;

%DCT2��任????
idct2graypic=idct2(dct2graypic)/255;
subplot(3,3,5),imshow(idct2graypic),title('idct2֮��ĻҶ�ͼ');

%1-D DCT�任
dctgraypic = double(zeros(rows, cols));
for i=1:rows
    dctgraypic(i,:) = dct(double(J(i,:)));
end
for i=1:cols
    dctgraypic(:,i) = dct(dctgraypic(:,i));
end
subplot(3,3,6);
imshow(dctgraypic);
title('1D DCT�����ͼ��');

%1-D IDCT�任
idctgraypic=idct2(dctgraypic)/255;
subplot(3,3,7);
imshow(idctgraypic);
title('1D IDCT��ԭͼ��');

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

%��������trials��PSNR
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
