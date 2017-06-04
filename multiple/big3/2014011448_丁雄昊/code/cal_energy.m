function [ energy ] = cal_energy( signal )
%cal_energy 计算音频能量
%   signal表示音频向量
    framelength = 200;%设置帧长  
    framenumber = fix(length(signal)/framelength);%读取语音文件对应的帧数  
    for i = 1:framenumber;%分帧处理  
        framesignal = signal((i-1)*framelength+1:i*framelength);%获取每帧的数据  
        E(i) = 0;%每帧能量置零  
        for j = 1:framelength;%计算每一帧的能量  
            E(i) = E(i)+framesignal(j)^2;  
        end  
    end  
    energy = E;
end

