function [ D ] = HI( bins, qr, qg, qb, r, g, b )
%HI 此处显示有关此函数的摘要
%   bins=16\128
%qr\qg\qb为Query图片rgb数值，r\g\b为普通图片rgb数值
    if(bins == 16)
        temp = 0;
        temp_m = 0;
        [h, w] = size(qr);
        for i=1:h
            temp = temp + min(qr(i),r(i));
            temp_m = temp_m + qr(i);
        end
        D = temp / temp_m * 2/16;
        temp = 0;
        temp_m = 0;
        for i=1:h
            temp = temp + min(qg(i),g(i));
            temp_m = temp_m + qg(i);
        end
        D = D + temp / temp_m * 4/16;
        temp = 0;
        temp_m = 0;
        for i=1:h
            temp = temp + min(qb(i),b(i));
            temp_m = temp_m + qb(i);
        end
        D = D + temp / temp_m * 2/16;
    elseif(bins == 128)
        temp = 0;
        temp_m = 0;
        [h, w] = size(qr);
        for i=1:h
            temp = temp + min(qr(i),r(i));
            temp_m = temp_m + qr(i);
        end
        D = temp / temp_m * 4/128;
        temp = 0;
        temp_m = 0;
        for i=1:h
            temp = temp + min(qg(i),g(i));
            temp_m = temp_m + qg(i);
        end
        D = D + temp / temp_m * 8/128;
        temp = 0;
        temp_m = 0;
        for i=1:h
            temp = temp + min(qb(i),b(i));
            temp_m = temp_m + qb(i);
        end
        D = D + temp/temp_m * 4/128;
    end

end

