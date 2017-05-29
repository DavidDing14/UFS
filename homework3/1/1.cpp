#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <fstream>
using namespace std;

#define window 5	//n 512λ������window ȡ5 

long long mv[16];	//�洢mv = m^(2*i+1) mod n 

int big_mod(int g, int n, int q){	//ʵ�� y = g^n mod q 
	if(n == 0){
		return 1;
	}else if(n == 1){
		return g % q;
	}else{
		int temp = 1;
		for(int i=0; i<n; ++i){
			temp *= g;
			temp %= q;
		}
		return temp;
	}
}

int e_gcd(int a, int b, int &x, int &y){	//��չŷ������㷨 
	if(b == 0){
		x = 1;
		y = 0;
		return a;
	}
	int ans = e_gcd(b, a%b, x, y);
	int temp = x;
	x = y;
	y = temp - a/b * y;
	return ans;
}

int cal(int a, int m, int &yy){	//��a����m�ĳ˷���Ԫ����a * k == 1 mod m �е�k ,˳����a*k - n*n/ = 1�е�n/ 
	int x, y;
	int gcd = e_gcd(a, m , x, y);
	printf("gcd = %d, x = %d, y = %d\n", gcd, x, y);
	yy = -y;	//yy==0 => wrong ; yy!=0 => n/ = yy;
	if(1%gcd != 0) return -1;
	x *= 1/gcd;
	m = abs(m);
	int ans = x % m;
	if(ans <= 0) ans += m;
	return ans;
}

int mont(int a, int b, int r, int nn, int n){
	int t = a*b;
	int m = ((t % r) * (nn % r)) % r;
	int u = (t + m*n) / r;
	if(u >= n)
		return u-n;
	else
		return u;
}

int mont_mulmod(int a, int b, int n){	//����Montgomeryʵ��ģ������,nΪ��������� 
	//��һ��������չ��ŷ����ö������nn 
	int r, nn;
	for(int i=0; ; ++i){
		if(pow(2,i)<=n && pow(2,i+1)>n){
			r = pow(2, i+1);
			break;
		}
	}
	int rr = cal(r, n, nn); 
	printf("r����n�ĳ˷���ԪΪrr = %d�� ���nn = %d\n", rr, nn);
	
	//�ڶ�������_a = a*r mod n
	int _a = ((a % n) * (r % n)) % n;
	printf("_a = %d\n", _a);
	
	//����������c = mont(_a, b)
	int c = mont(_a, b, r, nn, n);
	printf("return c = a^b mod n = %d\n", c);
	return c;
}

int mont_mulmod_jo(int a, int b, int n){	//����Montgomeryʵ��ģ������
	if(n % 2 == 1){	//nΪ���� 
		return mont_mulmod(a, b, n);
	}else{	//nΪż�� 
		int tm = 0;
		int dt = 1;
		int _n = n;
		while(_n % 2 == 0){
			tm++;
			dt *= 2;
			_n /= 2;
		}
		printf(" n = %d * 2 ^ %d , 2 ^ %d = %d\n", _n, tm, tm, dt);
		int v1 = mont_mulmod(a, b ,_n);
		int v2 = ((a % dt) * (b % dt)) % dt;
		printf("v1 = %d, v2 = %d\n", v1, v2);
		int _dt;
		int _n_ = cal(_n, dt, _dt);
		int t = abs(abs((v2 - v1) * _n_) % dt);
		int mm = v1 + _n * t;
		return mm;
	}
}

int main(){
	int m;
 	cout << "to calculate m^e mod n, please input small integer m : \n" << endl;
 	cin >> m;
 	int e[512], n[512];
 	char str[256];
 	cout << "then input e[512], n[512] from xxx, please input file name\n" << endl;
 	cin >> str;
 	ifstream fin(str);
 	FILE *fp;
 	char ch;
 	if((fp=fopen(str, "r"))==NULL){
		printf("file cannot be opened\n");
		exit(1);
 	}
 	int dd = 0;
 	while((ch=fgetc(fp))!=EOF){
		if(dd<512){
			e[dd] = ch-'0';
		}else{
			n[dd % 512] = ch - '0';
		}
		dd++;
 	}
 	fclose(fp);
 	//���ˣ���ʼ��m, e, n���
	
	int real_n = 0;
	for(int i=0; i<512; ++i){
		real_n = real_n*2 + n[i];
		if(real_n > INT_MAX){
			real_n = INT_MAX;
		}
	}
	
	int r = m;	
	int wstart;
	for(int i=511; i>=0; --i){
		if(e[i] == 1){
			wstart = i;
			break;
		}
	}	//wstartָ�����λ��1 
	
	for(int i=0; i<16; ++i){
		mv[i] = big_mod(m, 2*i+1, real_n);
	}
	for(int i=0; i<16; ++i){
		printf("mv[%d] = %ld\n", i, mv[i]);
	}	//��ʼ��mv[15]��ֵ
	/*
	while(wstart){
		wstart--;
		if(e[wstart] == 0){
			r = big_mod(r, 2, real_n);
		}else{
			//����λ�����������bit
			int w = 1;
			int wvalue = 1;
			wstart--;
			while(e[wstart] == 1 && w<=window){
				w++;
				wvalue = wvalue*2 + 1;
				wstart--;
			}
			r = big_mod(r, pow(2, window), real_n);
			r = mont_mulmod_jo(r, mv[wvalue>>1], real_n);
			if(wstart >= window){
			}else{
				int new_wv = 0;
				for(int i=wstart; i>=0; --i){
					new_wv = new_wv*2 + e[i];
				}
				r = big_mod(r, pow(2, new_wv), real_n);
				r = mont_mulmod_jo(r, mv[wvalue>>1], real_n);
				break;
			}
		}
	}
	*/
	printf("m ^ e mod n = %d\n", r);
	
	return 0;
}
