/**
 * Signed/unsigned multiplication.
 *
 * @param a first factor
 * @param b second factor
 * @return product
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

/**
 * Unsigned division.
 *
 * @param a dividend
 * @param b divisor 
 * @return quotient 
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

/**
 * Signed division.
 *
 * @param a dividend
 * @param b divisor 
 * @return quotient 
 */
extern "C" int __divsi3(int a, int b) {
	bool neg = false;

	if(a < 0) {
		a = -a;
		neg = !neg;
	}
	if(b < 0) {
		b = -b;
		neg = !neg;
	}

	unsigned int res = __udivsi3((unsigned int) a, (unsigned int) b);

	return neg ? - (int) res : (int) res;
}

/**
 * Unsigned modulus. 
 *
 * @param a dividend
 * @param b divisor 
 * @return modulus 
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

/**
 * Signed modulus. 
 *
 * @param a dividend
 * @param b divisor 
 * @return modulus 
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

	unsigned int res = __umodsi3((unsigned int) a, (unsigned int) b);

	return neg ? - (int) res : (int) res;
}
