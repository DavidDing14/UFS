obj = VideoReader('cars.avi');%输入视频位置
numFrames = obj.NumberOfFrames;% 帧的总数
fprintf('frames = %d\n',numFrames);

referenceframe = read(obj,155);
referenceBlock = imcrop(referenceframe,[10,110,16,16]);
referenceBlock = rgb2gray(referenceBlock);
index_i = 10;
index_j = 110;

for i=1:16
    for j=1:16
        fprintf('%d ',referenceBlock(i,j));
    end
    fprintf('\n');
end

p = 32;

for k = 156 : numFrames% 读取数据
    frame = read(obj,k);
    frame = rgb2gray(frame);
    [rows, cols] = size(frame);
    if k==156
        fprintf('rows = %d, cols = %d\n', rows, cols);
    end
    %cutframe = imcrop(frame,[10,110,16,16]);
    %imshow(cutframe);%显示帧
    %imwrite(cutframe,strcat(num2str(k),'.jpg'),'jpg');% 保存帧
      
    %pixel domain block matching
    
    min_mse = double(100000);
    new_index_i = 0;
    new_index_j = 0;
    
    for i=index_i-p:index_i+p
        for j=index_j-p:index_j+p
            if(i<1 || j<1)
                continue;
            end
            mse = 0;
            for ik=0:15
                for jk=0:15
                    mse = mse + double((frame(i+ik, j+jk)-referenceBlock(1+ik, 1+jk))^2);
                    %fprintf('- = %d\n', (frame(i+ik,j+jk)-referenceBlock(1+ik,1+jk))^2);
                    %fprintf('mse = %d\n', mse);
                end
            end
            mse = mse / (16*16);
            if mse<min_mse
                min_mse = mse;
                new_index_i = i;
                new_index_j = j;
            end
        end
    end
    
    fprintf('new_index_i = %d, new_index_j = %d\n', new_index_i, new_index_j);
    index_i = new_index_i;
    index_j = new_index_j;
    referenceframe = read(obj,k);
    referenceBlock = imcrop(referenceframe,[new_index_i,new_index_j,16,16]);
    referenceBlock = rgb2gray(referenceBlock);
    imshow(referenceBlock);
    imwrite(referenceBlock,strcat(num2str(k),'.jpg'),'jpg');% 保存帧
end