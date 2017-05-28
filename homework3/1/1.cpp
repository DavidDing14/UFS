#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
using namespace std;

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
 	cout << "please input a, b, n(a ^ b mod n) : \n" << endl;
 	int a, b, n;
 	cin >> a >> b >> n;
 	cout << mont_mulmod_jo(a, b, n) << endl;
	return 0;
}
