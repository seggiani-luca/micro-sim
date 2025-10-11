// these are helpers needed by the compiler to implement functions not implemented by the ISA

/*
 * Signed/unsigned multiplication.
 */
extern "C" unsigned int __mulsi3(unsigned int a, unsigned int b) {
  unsigned int res = 0;

  while (a) {
  	if (a & 1) res += b;
		a >>= 1;
		b <<= 1;
  }
  
	return res;
}

/*
 * Unsigned division.
 */
extern "C" unsigned int __udivsi3(unsigned int a, unsigned int b) {
	if(b == 0) return 0;

	unsigned int q = 0;
	unsigned int r = 0;

	for (int i = 31; i >= 0; i--) {
		r = (r << 1) | ((a >> i) & 1);
		if (r >= b) {
			r -= b;
			q |= (1u << i);
		}
	}
	
	return q;
}

/*
 * Signed division.
 */
extern "C" int __divsi3(int a, int b) {
	bool neg = false;

	if(a < 0 && b > 0) {
		a = -a;
		neg = true;
	}
	if(a > 0 && b < 0) {
		b = -b;
		neg = true;
	}

	a = __udivsi3(a, b);

	if(neg) a = -a;
	return a;
}

/*
 * Unsigned modulus.
 */
extern "C" unsigned int __umodsi3(unsigned int a, unsigned int b) {
	if(b == 0) return 0;

	unsigned int q = 0;
	unsigned int r = 0;

	for (int i = 31; i >= 0; i--) {
		r = (r << 1) | ((a >> i) & 1);
		if (r >= b) {
			r -= b;
			q |= (1u << i);
		}
	}
	
	return r;
}

/*
 * Signed modulus.
 */
extern "C" int __modsi3(int a, int b) {
	bool neg = false;

	if(a < 0) {
		a = -a;
		neg = true;
	}
	if(b < 0) {
		b = -b;
	}

	a = __umodsi3(a, b);	

	if(neg) a = -a;
	return a;
}
