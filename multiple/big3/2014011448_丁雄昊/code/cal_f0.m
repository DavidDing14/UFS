function [ T1 ] = cal_f0( x )
%cal_f0 计算一段音频的基频
%   此处显示详细说明
    n=160;                         %取20ms的声音片段，即160个样点
    for m=1:length(x)/n;           %对每一帧求短时自相关函数
        for k=1:n;
             Rm(k)=0;
             for i=(k+1):n;
                 Rm(k)=Rm(k)+x(i+(m-1)*n)*x(i-k+(m-1)*n);
             end
        end
        p=Rm(10:n);                %防止误判，去掉前边10个数值较大的点        
        [Rmax,N(m)]=max(p);        %读取第一个自相关函数的最大点
    end                            %补回前边去掉的10个点
    N=N+10;
    T=N/8;
    T1= medfilt1(T,5);             %去除野点
end

