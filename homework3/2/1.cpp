#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
using namespace std;

/******************************************
这段代码旨在解决Diffie-Hellman密钥交换过程中
中间人的问题  
******************************************/

int q, g, XA, XB;	//全局参数，A和B在D-H算法中交换的内容 

int ea = 5;
int da;
int eb = 7;
int db;
int na;
int nb;

int big_mod(int g, int n, int q){	//实现 y = g^n mod q 
	printf("%d ^ %d mod %d\n", g, n, q);
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

void RSA_A(){		//用RSA算法计算A的私钥和公钥
	int pa = 7, qa = 17;
	na = pa * qa;
	printf("\nA RSA : p = %d, q = %d, n = %d\n", pa, qa, na);
	int fi = (pa-1)*(qa-1);
	printf("欧拉函数fi(n) = %d\n", fi);

	for(int i=0; i<fi; ++i){
		if((i*fi+1) % ea == 0){
			da = (i*fi+1) / ea;
			break;	
		}
	} 
	printf("A的私钥为{%d, %d}, 公钥为{%d, %d}\n", ea, na, da, na);
}

void RSA_B(){		//用RSA算法计算B的私钥和公钥 
	int pb = 11, qb = 13;
	nb = pb*qb;
	printf("\nB RSA : p = %d, q = %d, n = %d\n", pb, qb, nb);
	int fi = (pb-1)*(qb-1);
	printf("欧拉函数fi(n) = %d\n", fi);

	for(int i=0; i<fi; ++i){
		if((i*fi+1) % eb == 0){
			db = (i*fi+1) / eb;
			break;	
		}
	} 
	printf("B的私钥为{%d, %d}, 公钥为{%d, %d}\n\n", eb, nb, db, nb);
}

void recieve_A(long long YB){
	printf("A recieved YB with B's sign\n");
	int RK = big_mod(YB, db, nb);
	printf("A recieved YB fromd B and calculate RK = %ld\n\n", RK);
	printf("A use Real YB to calculate K:\n");
	int K = big_mod(RK, XA, q);
	printf("A recieved YB from B and calculete K = %ld\n\n", K);
}

void behavior_B(long long YA){
	XB = 11;
	printf("B use D-H to encrypy XB:\n");
	int YB = big_mod(g, XB, q);
	printf("after D-H YB = %ld\n\n", YB);
	printf("B sign his name on YB:\n");
	YB = big_mod(YB, eb, nb);
	printf("B send YB to A, YB = %ld\n\n", YB);
	recieve_A(YB);
	printf("B recieved YA with A's sign\n");
	int RK = big_mod(YA, da, na);
	printf("B recieved YA fromd A and calculate RK = %ld\n\n", RK);
	printf("B use Real YA to calculate K:\n");
	int K = big_mod(RK, XB, q);
	printf("B recieved YA from A and calculete K = %ld\n", K);
}

void behavior_A(){
	q = 19;
	g = 2;
	XA = 10;
	printf("A use D-H to encrypy XA:\n");
	int YA = big_mod(g, XA, q);
	printf("after D-H YA = %ld\n\n", YA);
	printf("A sign his name on YA:\n");
	YA = big_mod(YA, ea, na);
	printf("A send YA to B, YA = %ld\n\n", YA);
	behavior_B(YA);
}

int main(){
	RSA_A();
	RSA_B();
	behavior_A();
	return 0;
} 
