x=audioread('../data/exp1/guodegang.wav');%读取声音文件
g=cal_f0(x);
y=audioread('../data/exp1/guodegang_1.wav');
g1=cal_f0(y);
distance = dis(g,g1);
disp('guodegang.wav - guodegang_1.wav 基音周期 distance = ');
disp(distance);

y=audioread('../data/exp1/guodegang_2.wav');
g1=cal_f0(y);
distance = dis(g,g1);
disp('guodegang.wav - guodegang_2.wav 基音周期 distance = ');
disp(distance);

y=audioread('../data/exp1/guodegang_3.wav');
g1=cal_f0(y);
distance = dis(g,g1);
disp('guodegang.wav - guodegang_3.wav 基音周期 distance = ');
disp(distance);

x=audioread('../data/exp1/shantianfang.wav');%读取声音文件
g=cal_f0(x);
y=audioread('../data/exp1/shantianfang_1.wav');
g1=cal_f0(y);
distance = dis(g,g1);
disp('shantianfang.wav - shantianfang_1.wav 基音周期 distance = ');
disp(distance);

y=audioread('../data/exp1/shantianfang_2.wav');
g1=cal_f0(y);
distance = dis(g,g1);
disp('shantianfang.wav - shantianfang_2.wav 基音周期 distance = ');
disp(distance);

y=audioread('../data/exp1/shantianfang_3.wav');
g1=cal_f0(y);
distance = dis(g,g1);
disp('shantianfang.wav - shantianfang_3.wav 基音周期 distance = ');
disp(distance);

%下面进行exp2部分
%6000
x=audioread('../data/exp2/A/sen6000.wav');%读取声音文件
g=cal_f0(x);
y=audioread('../data/exp2/B/sen6000_b.wav');
g1=cal_f0(y);
distance = dis(g,g1);
disp('A sen6000.wav - B sen6000.wav 基音周期 distance = ');
disp(distance);

x=audioread('../data/exp2/AB/6000.wav');
g=cal_f0(x);
distance = dis(g,g1);
disp('AtoB sen6000.wav - B sen6000.wav 基音周期 distance = ');
disp(distance);

%6015
x=audioread('../data/exp2/A/sen6015.wav');%读取声音文件
g=cal_f0(x);
y=audioread('../data/exp2/B/sen6015_b.wav');
g1=cal_f0(y);
distance = dis(g,g1);
disp('A sen6015.wav - B sen6015.wav 基音周期 distance = ');
disp(distance);

x=audioread('../data/exp2/AB/6015.wav');
g=cal_f0(x);
distance = dis(g,g1);
disp('AtoB sen6015.wav - B sen6015.wav 基音周期 distance = ');
disp(distance);

%6028
x=audioread('../data/exp2/A/sen6028.wav');%读取声音文件
g=cal_f0(x);
y=audioread('../data/exp2/B/sen6028_b.wav');
g1=cal_f0(y);
distance = dis(g,g1);
disp('A sen6028.wav - B sen6028.wav 基音周期 distance = ');
disp(distance);

x=audioread('../data/exp2/AB/6028.wav');
g=cal_f0(x);
distance = dis(g,g1);
disp('AtoB sen6028.wav - B sen6028.wav 基音周期 distance = ');
disp(distance);

%6044
x=audioread('../data/exp2/A/sen6044.wav');%读取声音文件
g=cal_f0(x);
y=audioread('../data/exp2/B/sen6044_b.wav');
g1=cal_f0(y);
distance = dis(g,g1);
disp('A sen6044.wav - B sen6044.wav 基音周期 distance = ');
disp(distance);

x=audioread('../data/exp2/AB/6044.wav');
g=cal_f0(x);
distance = dis(g,g1);
disp('AtoB sen6044.wav - B sen6044.wav 基音周期 distance = ');
disp(distance);

%6147
x=audioread('../data/exp2/A/sen6147.wav');%读取声音文件
g=cal_f0(x);
y=audioread('../data/exp2/B/sen6147_b.wav');
g1=cal_f0(y);
distance = dis(g,g1);
disp('A sen6147.wav - B sen6147.wav 基音周期 distance = ');
disp(distance);

x=audioread('../data/exp2/AB/6147.wav');
g=cal_f0(x);
distance = dis(g,g1);
disp('AtoB sen6147.wav - B sen6147.wav 基音周期 distance = ');
disp(distance);