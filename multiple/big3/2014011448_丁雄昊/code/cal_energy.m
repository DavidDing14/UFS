function [ energy ] = cal_energy( signal )
%cal_energy ������Ƶ����
%   signal��ʾ��Ƶ����
    framelength = 200;%����֡��  
    framenumber = fix(length(signal)/framelength);%��ȡ�����ļ���Ӧ��֡��  
    for i = 1:framenumber;%��֡����  
        framesignal = signal((i-1)*framelength+1:i*framelength);%��ȡÿ֡������  
        E(i) = 0;%ÿ֡��������  
        for j = 1:framelength;%����ÿһ֡������  
            E(i) = E(i)+framesignal(j)^2;  
        end  
    end  
    energy = E;
end

