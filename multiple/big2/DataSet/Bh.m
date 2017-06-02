function [ D ] = Bh( bins, qr, qg, qb, r, g, b )
%Bh 此处显示有关此函数的摘要
%   bins=16\128
%qr\qg\qb为Query图片rgb数值，r\g\b为普通图片rgb数值
    [h, w] = size(qr);
    sum = 0;
    for i=1:h
        sum = sum + qr(i);
    end
    for i=1:h
        qr(i) = qr(i) / sum;
    end
    for i=1:h
        sum = sum + qg(i);
    end
    for i=1:h
        qg(i) = qg(i) / sum;
    end
    for i=1:h
        sum = sum + qb(i);
    end
    for i=1:h
        qb(i) = qb(i) / sum;
    end
    for i=1:h
        sum = sum + r(i);
    end
    for i=1:h
        r(i) = r(i) / sum;
    end
    for i=1:h
        sum = sum + g(i);
    end
    for i=1:h
        g(i) = g(i) / sum;
    end
    for i=1:h
        sum = sum + b(i);
    end
    for i=1:h
        b(i) = b(i) / sum;
    end
    if(bins == 16)
        temp = 1;        
        for i=1:h
            temp = temp - sqrt(qr(i)*r(i));
        end
        D = sqrt(temp) * 2/16;
        temp = 1;
        for i=1:h
            temp = temp - sqrt(qg(i)*g(i));
        end
        D = D + sqrt(temp) * 4/16;
        temp = 1;
        for i=1:h
            temp = temp - sqrt(qb(i)*b(i));
        end
        D = D + sqrt(temp) * 2/16;
    elseif(bins == 128)
        temp = 1;        
        for i=1:h
            temp = temp - sqrt(qr(i)*r(i));
        end
        D = sqrt(temp) * 4/128;
        temp = 1;
        for i=1:h
            temp = temp - sqrt(qg(i)*g(i));
        end
        D = D + sqrt(temp) * 8/128;
        temp = 1;
        for i=1:h
            temp = temp - sqrt(qb(i)*b(i));
        end
        D = D + sqrt(temp) * 4/128;
    end

end

