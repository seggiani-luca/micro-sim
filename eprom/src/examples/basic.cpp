#include "lib/lib.h"

#define VER "0.0"

/*
 * Tokens.
 */
enum op_type {
	ADD,
	SUB,
	GEQ,
	LEQ,
	G,
	L,
	EQ,
	NEQ,
	ASS
};

enum key_type {
	PRINT,
	IF,
	THEN,
	GOTO,
	INPUT,
	LET,
	CLEAR,
	LIST,
	RUN,
	END
};

enum tok_type {
	T_VAR,
	T_OP,
	T_NUM,
	T_KEY,
	T_STR,
	T_MARK	
};

#define MAX_STRLEN 20

struct token {
	tok_type type;

	union {
		char var;
		op_type op;
		int num;
		key_type key;
		char str[MAX_STRLEN];
	} payload;
	
	void print() {
		switch(type) {
			case T_VAR:
				vid::print_char(payload.var);
				break;
			case T_OP:
				switch(payload.op) {
					case ADD:	vid::print_str("+"); break;
					case SUB:	vid::print_str("-"); break;
					case GEQ:	vid::print_str(">="); break;
					case LEQ:	vid::print_str("<="); break;
					case G:		vid::print_str(">"); break;
					case L:		vid::print_str("<"); break;
					case NEQ:	vid::print_str("!="); break;
					case EQ:	vid::print_str("=="); break;
					case ASS:	vid::print_str("="); break;
				}
				break;
			case T_NUM:
				vid::print_int(payload.num);
				break;
			case T_KEY:
				switch(payload.key) {
					case PRINT:		vid::print_str("PRINT"); break;
					case IF:			vid::print_str("IF"); break;
					case THEN:		vid::print_str("THEN"); break;
					case GOTO:		vid::print_str("GOTO"); break;
					case INPUT:		vid::print_str("INPUT"); break;
					case LET:			vid::print_str("LET"); break;
					case CLEAR:		vid::print_str("CLEAR"); break;
					case LIST:		vid::print_str("LIST"); break;
					case RUN:			vid::print_str("RUN"); break;
					case END:			vid::print_str("END"); break;
				}
				break;
			case T_STR:
				vid::print_char('\"');
				vid::print_str(payload.str);
				vid::print_char('\"');
				break;
			case T_MARK:
				vid::print_str("MARK");
				break;
		}
	}
};

/*
 * Memory and limits.
 */
#define MAX_LEN 80

const int max_toks = 20;
const int max_lines = 100;
token lines[max_lines][max_toks];

/*
 * Tokenization.
 */
bool get_var(const char* wr, int len, token* tok) {
	if(len != 1) return false;

	if(*wr >= 'A' && *wr <= 'Z' || *wr >= 'a' && *wr <= 'z') {
		tok->type = T_VAR;
		tok->payload.var = *wr;
		return true;
	}

	return false;
}

bool get_op(const char* wr, int len, token* tok) {
	if(len > 2) return false;

	switch(*wr) {
		case '+':
			if(len == 2) return false;
			tok->payload.op = op_type::ADD;
			break;

		case '-':
			if(len == 2) return false;
			tok->payload.op = op_type::SUB;
			break;

		case '<':
			if(len == 1) {
				tok->payload.op = op_type::L;
			} else if(*(wr + 1) == '=') {
				tok->payload.op = op_type::LEQ;
			} else return false;
			break;

		case '>':
			if(len == 1) {
				tok->payload.op = op_type::G;
			} else if(*(wr + 1) == '=') {
				tok->payload.op = op_type::GEQ;
			} else return false;
			break;

		case '!':
			if(len == 2 && *(wr + 1) == '=') {
				tok->payload.op = op_type::NEQ;
			} else return false;
			break;

		case '=':
			if(len == 1) {
				tok->payload.op = op_type::ASS;
			} else if(*(wr + 1) == '=') {
				tok->payload.op = op_type::EQ;
			} else return false;
			break;
	
		default:
			return false;
	}

	tok->type = T_OP;
	return true;
}

bool get_num(const char* wr, int len, token* tok) {
	unsigned int res = 0;
	bool neg = false;

	if(*wr == '-') {
		wr++;
		neg = true;
	}

	// temp char
	char c;

	while(c = *wr++) {	
		if(c < '0' || c > '9') return false;

		res = res * 10 + c - '0';
	}

	tok->type = T_NUM;
	tok->payload.num = neg ? (int) -res : (int) res;
	return true;
}

bool get_key(const char* wr, int len, token* tok) {
	if(!str::cmp(wr, "PRINT")) {
		tok->payload.key = PRINT;
	} else if(!str::cmp(wr, "IF")) {
		tok->payload.key = IF;
	} else if(!str::cmp(wr, "THEN")) {
		tok->payload.key = THEN;
	} else if(!str::cmp(wr, "GOTO")) {
		tok->payload.key = GOTO;
	} else if(!str::cmp(wr, "INPUT")) {
		tok->payload.key = INPUT;
	} else if(!str::cmp(wr, "LET")) {
		tok->payload.key = LET;
	} else if(!str::cmp(wr, "CLEAR")) {
		tok->payload.key = CLEAR;
	} else if(!str::cmp(wr, "LIST")) {
		tok->payload.key = LIST;
	} else if(!str::cmp(wr, "RUN")) {
		tok->payload.key = RUN;
	} else if(!str::cmp(wr, "END")) {
		tok->payload.key = END;
	} else return false;

	tok->type = T_KEY;
	return true;
}

bool get_str(const char* wr, int len, token* tok) {
	if(*wr != '\"') return false;
	if(*(wr + len - 1) != '\"') return false;
	if(len - 2 >= MAX_STRLEN) return false;

	char* n_wr = tok->payload.str; 
	str::ncpy(n_wr, wr + 1, len - 2);
	n_wr[len - 2] = '\0';

	tok->type = T_STR;
	return true;
}

bool tokenize_wr(const char* wr, token* tok) {
	str::mset((void*) tok, 0, sizeof(token));

	int len = str::len(wr);

	if(get_var(wr, len, tok))	return true;
	if(get_op(wr, len, tok)) 	return true;
	if(get_num(wr, len, tok))	return true;
	if(get_key(wr, len, tok))	return true;
	if(get_str(wr, len, tok))	return true;

	// error
	vid::print_str("Token ignoto: ");
	vid::print_strln(wr);
	return false;
}

bool tokenize_ln(const char* ln, token* toks, int* num_toks) {
	*num_toks = 0;

	// for strings
	bool in_str = false;

	// temp token string
	char wr[MAX_LEN];
	int i = 0; // idx in string

	// temp char
	char c;

	while(c = *ln++) {
		if(c == ' ' && !in_str) {
			if(i == 0) continue;

			// terminate token
			wr[i] = '\0';

			// get token
			bool res = tokenize_wr(wr, &toks[(*num_toks)++]);
			if(*num_toks == max_toks || !res) return false;

			// reset temp token string
			i = 0;
		} else if(c =='\"'){
			wr[i++] = c;

			if(in_str) {
				// terminate token
				wr[i] = '\0';

				// get token
				bool res = tokenize_wr(wr, &toks[(*num_toks)++]);
				if(*num_toks == max_toks || !res) return false;
		
				// reset temp token string
				i = 0;
			}

			in_str = !in_str;	
		} else wr[i++] = c;
	}

	if(i != 0) {
		// terminate last token
		wr[i] = '\0';

		// get last token
		bool res = tokenize_wr(wr, &toks[(*num_toks)++]);
		if(*num_toks == max_toks || !res) return false;
	}

	toks[*num_toks].type = T_MARK;
	return true;
}

void insert_tokens(token* toks, int line_idx) {
	if(line_idx < 0 | line_idx >= max_lines) {
		vid::print_strln("Linea fuori campo");
		return;
	}

	int i = 0;
	while(toks[i].type != T_MARK) {
		lines[line_idx][i] = toks[i];
		i++;
	}

	lines[line_idx][i].type = T_MARK;
}

/*
 * Utils. 
 */
void print_tokens(token* toks) {
	while(toks->type != T_MARK) {
		toks->print();
		vid::print_char(' ');
	
		toks++;
	}
	vid::newline();
}


void list_tokens() {
	for(int i = 0; i < max_lines; i++) {
		if(lines[i][0].type != T_MARK) {
			vid::print_int(i);
			vid::print_char(' ');
			print_tokens(lines[i]);
		}
	}
}

/*
 * Symbols.
 */
int vars[26 * 2];
bool vars_set[26 * 2];

bool var_idx(char& name) {
	if(name >= 'A' && name <= 'Z') {
		name -= 'A';
		return true; 
	}	
	if(name >= 'a' && name <= 'z') {
		name -= 'a';
		return true;
	}

	return false;
}

int& var(char name) {
	if(var_idx(name)) return vars[name];

	utl::panic("Variabile inesistente in var()");
}

void define_var(char name) {
	if(var_idx(name)) {
		vars_set[name] = true;
		return;
	} 
	
	utl::panic("Variabile inesistente in define_var()");
}

bool var_defined(char name) {
	if(var_idx(name)) return vars_set[name];

	utl::panic("Variabile inesistente in var_defined()");
}

/*
 * Execution.
 */
bool exec_statement(token* toks, int* line = nullptr);

bool get_tok_val(token tok, int& res) {
	if(tok.type == T_NUM) {
		res = tok.payload.num;
		return true;
	}

	if(tok.type == T_VAR) {
		if(!var_defined(tok.payload.var)) {
			vid::print_str("Variabile ");
			vid::print_char(tok.payload.var);
			vid::print_strln(" non dichiarata");
			return false;
		}
		
		res = var(tok.payload.var); 
		return true;
	}

	vid::print_str("Espressione non puo' contenere ");
	tok.print();
	vid::newline();
	return false;
}

bool is_aritmop(token tok) {
	if(tok.type != T_OP) return false;

	switch(tok.payload.op) {
		case ADD: return true;
		case SUB: return true;
	}

	return false;
}

bool is_relop(token tok) {
	if(tok.type != T_OP) return false;

	switch(tok.payload.op) {
		case GEQ: return true;
		case LEQ: return true;
		case G: return true;
		case L: return true;
		case NEQ: return true;
		case EQ: return true;
	}

	return false;
}

void apply_aritmop(int& res, int arg, op_type type) {
	switch(type) {
		case ADD: res += arg; break;
		case SUB: res -= arg; break;
	}
}

bool apply_relop(int arg1, int arg2, op_type type) {
	switch(type) {
		case GEQ: return arg1 >= arg2;
		case LEQ: return arg1 <= arg2;
		case G: return arg1 > arg2;
		case L: return arg1 < arg2;
		case NEQ: return arg1 != arg2;
		case EQ: return arg1 == arg2;
	}

	return false; // mai raggiunto, meglio aver paura
}

bool eval_expr(token*& toks, int& res, bool continues = true) {
	if(!get_tok_val(*toks, res)) return false;

	toks++;

	while(toks->type != T_MARK) {
		if(!is_aritmop(*toks)) {
			if(continues) {
				// c'è qualcosa dopo
				toks--;
				return true;
			} else {
				vid::print_strln("Spazzatura dopo espressione");
				return false;
			}
		} 

		op_type type = toks->payload.op;

		toks++;

		if(toks->type == T_MARK) {
			vid::print_strln("Nessun termine dopo operatore");
			return false;
		}
		
		int temp_res;
		if(!get_tok_val(*toks, temp_res)) return false;

		apply_aritmop(res, temp_res, type);
		
		toks++;
	}

	return true;
}

bool exec_print(token* toks) {
	if(toks->type == T_MARK) {
		vid::print_strln("Nulla da stampare");
		return false;
	}

	if(toks->type == T_STR) {
		if(toks[1].type != T_MARK) {
			vid::print_strln("Spazzatura dopo PRINT");
			return false;
		}

		vid::print_strln(toks->payload.str);
		return true;
	}
	
	int res;
	if(eval_expr(toks, res, false)) {
		vid::print_int(res);
		vid::newline();
		return true;
	}

	return false;
}

bool exec_if(token* toks, int* line) {
	int res1;
	if(!eval_expr(toks, res1)) return false;

	toks++;

	if(!is_relop(*toks)) {
		vid::print_strln("Operatore relazionale invalido: ");
		toks->print();
		vid::newline();
		return false;
	}

	op_type type = toks->payload.op;

	toks++;

	int res2;
	if(!eval_expr(toks, res2)) return false;

	bool through = apply_relop(res1, res2, type);

	toks++;

	if(toks->type != T_KEY || toks->payload.key != THEN) {
		vid::print_strln("Nessun THEN dopo IF");
		return false;
	}

	if(through) {
		exec_statement(toks + 1, line);
	}

	return true;	
}

bool exec_goto(token* toks, int* line) {
	int res;
	if(!get_tok_val(*toks, res)) {
		return false;
	}

	if(res < 0 | res >= max_lines) {
		vid::print_strln("GOTO fuori campo");
		return false;
	}

	if(line) *line = res - 1;

	return true;
}

bool exec_input(token* toks) {	
	if(toks->type != T_VAR) {
		vid::print_strln("Nessuna variabile dopo INPUT");
		return false;
	}
	
	if(toks[1].type != T_MARK) {
		vid::print_strln("Spazzatura dopo INPUT");
		return false;
	}

	char name = toks->payload.var;
	
	int val = kyb::read_int();

	define_var(name);
	var(name) = val;

	return true;	
}

bool exec_let(token* toks) {
	if(toks->type != T_VAR) {
		vid::print_strln("Nessuna variabile dopo LET");
		return false;
	}

	char name = toks->payload.var;

	toks++;

	if(toks->type != T_OP || toks->payload.op != ASS) {
		vid::print_strln("Nessun operatore di assegnamento dopo LET");
	}

	toks++;

	int res;
	if(!eval_expr(toks, res, false)) return false;

	define_var(name);
	var(name) = res;

	return true;	
}

bool exec_clear(token* toks) {
	if(toks->type != T_MARK) {
		vid::print_strln("Spazzatura dopo CLEAR");
		return false;
	}
	
	for(int i = 0; i < max_lines; i++) {
		lines[i][0].type = T_MARK;
	}
	
	return true;	
}

bool exec_list(token* toks) {
	if(toks->type != T_MARK) {
		vid::print_strln("Spazzatura dopo LIST");
		return false;
	}

	list_tokens();
	
	return true;	
}

bool exec_run(token* toks) {
	if(toks->type != T_MARK) {
		vid::print_strln("Spazzatura dopo RUN");
		return false;
	}

	// reset vars
	for(int i = 0; i < 26 * 2; i++) {
		vars_set[i] = false;
	}

	// run
	for(int i = 0; i < max_lines; i++) {
		if(!exec_statement(lines[i], &i)) return false;
	}

	return true;
}

bool exec_end(token* toks) {
	if(toks->type != T_MARK) {
		vid::print_strln("Spazzatura dopo END");
		return false;
	}

	return true;
}

bool exec_statement(token* toks, int* line) {
	if(toks[0].type == T_MARK) return true;

	if(toks[0].type != T_KEY || toks[0].payload.key == THEN) {
		vid::print_str("Istruzione non puo' iniziare con ");
		toks[0].print();
		vid::newline();
		return false;
	}

	switch(toks[0].payload.key) {
		case(PRINT): 	return exec_print(toks + 1);
		case(IF): 		return exec_if(toks + 1, line);
		case(GOTO): 	return exec_goto(toks + 1, line);
		case(INPUT):	return exec_input(toks + 1);
		case(LET): 		return exec_let(toks + 1);
		case(CLEAR):	return exec_clear(toks + 1);
		case(LIST):		return exec_list(toks + 1);
		case(RUN):		return exec_run(toks + 1);
		case(END):		return exec_end(toks + 1);
		default: return false;
	}
}

void greet() {
	vid::print_str("micro-sim BASIC ");
	vid::print_strln(VER);
	vid::put_str({0, 60}, "2025 - Luca Seggiani");
}

void main() {
	greet();

	// init lines
	for(int i = 0; i < max_lines; i++) {
		lines[i][0].type = T_MARK;
	}

	while(true) {
		char line[MAX_LEN];	
		vid::print_str("$ ");
		kyb::read_str(line, MAX_LEN);
	
		int num_toks;
		token toks[max_toks];
		if(!tokenize_ln(line, toks, &num_toks)) {
			if(num_toks == max_toks) vid::print_strln("Troppi token");
			continue;
		}

		if(num_toks == 0) continue;

		if(toks[0].type == T_NUM) {
			int line_idx = toks[0].payload.num;
			insert_tokens(toks + 1, line_idx);
		} else {
			exec_statement(toks);
		}

	}
}
