function [ D ] = L2( bins, qr, qg, qb, r, g, b )
%L2 此处显示有关此函数的摘要
%   bins=16\128
%qr\qg\qb为Query图片rgb数值，r\g\b为普通图片rgb数值
    if(bins == 16)
        temp = 0;
        [h, w] = size(qr);
        for i=1:h
            temp = temp + double(qr(i)-r(i))^2;
        end
        D = sqrt(temp * 2/16);
        temp = 0;
        for i=1:h
            temp = temp + double(qg(i)-g(i))^2;
        end
        D = D + sqrt(temp * 4/16);
        temp = 0;
        for i=1:h
            temp = temp + double(qb(i)-b(i))^2;
        end
        D = D + sqrt(temp * 2/16);
    elseif(bins == 128)
        temp = 0;
        [h, w] = size(qr);
        for i=1:h
            temp = temp + double(qr(i)-r(i))^2;
        end
        D = sqrt(temp * 4/128);
        temp = 0;
        for i=1:h
            temp = temp + double(qg(i)-g(i))^2;
        end
        D = D + sqrt(temp * 8/128);
        temp = 0;
        for i=1:h
            temp = temp + double(qb(i)-b(i))^2;
        end
        D = D + sqrt(temp * 4/128);
    end


end

