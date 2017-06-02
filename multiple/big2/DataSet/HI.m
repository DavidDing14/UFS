function [ D ] = HI( bins, qr, qg, qb, r, g, b )
%HI �˴���ʾ�йش˺�����ժҪ
%   bins=16\128
%qr\qg\qbΪQueryͼƬrgb��ֵ��r\g\bΪ��ͨͼƬrgb��ֵ
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

