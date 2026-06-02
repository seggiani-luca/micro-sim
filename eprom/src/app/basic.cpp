#include "../lib/lib.h"

#define VER "0.0"

/**
 * Implementation of a simple TinyBASIC interpreter. 
 */
namespace bas {
	/*
	 * Array of strings. 
	 */
	#define MAX_STRLEN 32
	#define MAX_STRINGS 128
	char (*strings)[MAX_STRLEN];

	/**
	 * Next string to allocate.
	 */
	int cur_string = 0;

	/*
	 * Arena allocator for strings.
	 */
	char* alloc_string() {
		if(cur_string == MAX_STRINGS) return NULL;
		return strings[cur_string++];
	}

	/*
	 * Operation token types.
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

	/*
	 * Keyword token types.
	 */
	enum key_type {
		PRINT,
		IF,
		THEN,
		GOTO,
		INPUT,
		LET,
		END
	};

	/**
	 * Token types.
	 */
	enum tok_type {
		T_VAR,
		T_OP,
		T_NUM,
		T_KEY,
		T_STR,
		T_NOP,
		T_MARK
	};

	/**
	 * Represents a single token.
	 */
	struct token {
		/**
		 * Type of token.
		 */
		tok_type type;

		/**
		 * Token specific information, changes based on type.
		 */
		union {
			/**
			 * Variable name if variable.
			 */
			char var;

			/**
			 * Operation type if operation token.
			 */
			op_type op;

			/**
			 * Value if number literal.
			 */
			int num;

			/**
			 * Keyword type if keyword token.
			 */
			key_type key;
			
			/**
			 * Pointer to value if string literal.
			 */
			char* str;
		} payload;

		/**
		 * Prints this token.
		 */
		void print() {
			switch(type) {
				case T_VAR:
					vid::print_char(payload.var);
					break;
				
				case T_OP:
					switch(payload.op) {
						case ADD: vid::print_str("+"); break;
						case SUB: vid::print_str("-"); break;
						case GEQ: vid::print_str(">="); break;
						case LEQ: vid::print_str("<="); break;
						case G:   vid::print_str(">"); break;
						case L:   vid::print_str("<"); break;
						case NEQ: vid::print_str("!="); break;
						case EQ:  vid::print_str("=="); break;
						case ASS: vid::print_str("="); break;
					}
					break;
				
				case T_NUM:
					vid::print_int(payload.num);
					break;
				
				case T_KEY:
					switch(payload.key) {
						case PRINT: vid::print_str("PRINT"); break;
						case IF:    vid::print_str("IF"); break;
						case THEN:  vid::print_str("THEN"); break;
						case GOTO:  vid::print_str("GOTO"); break;
						case INPUT: vid::print_str("INPUT"); break;
						case LET:   vid::print_str("LET"); break;
						case END:   vid::print_str("END"); break;
					}
					break;
				
				case T_STR:
					vid::print_char('\"');
					vid::print_str(payload.str);
					vid::print_char('\"');
					break;

				case T_NOP:
					vid::print_strln("NOP");
					break;

				case T_MARK:
					vid::print_str("MARK");
					break;
			}
		}
	};

	/**
	 * File sizing constants.
	 */
	#define FILE_BUF_SIZE 4096

	/*
	 * Token sizing constants. 
	 */
	#define MAX_TOKS 20
	#define MAX_LINES 100

	// program code buffer 
	token (*lines)[MAX_TOKS];

	/**
	 * Represents a label. 
	 */
	struct label {
		/**
		 * Name of label.
		 */
		char* str;

		/**
		 * Line of label.
		 */
		int line;
	};

	/*
	 * Array of strings. 
	 */
	#define MAX_LABELS 128
	label* labels;
	
	/**
	 * Next label to allocate.
	 */
	int cur_label = 0;

	/**
	 * Arena allocator for labels.
	 */
	label* alloc_label() {
		if(cur_label == MAX_LABELS) return NULL;
		return &labels[cur_label++];
	}
	
	/**
	 * Resolves label with given name.
	 *
	 * @param name name of label
	 * @return label, if found
	 */
	label* resolve_label(const char* name) {
		for(int i = 0; i < cur_label; i++) {
			label* lab = &labels[i];
			
			// match
			if(str::cmp(name, lab->str) == 0) return lab;
		}

		// not found
		return NULL;
	}

	/*
	 * Tries to get a variable token, expecting [A:Z] or [a:z].
	 *
	 * @param wr word to tokenize
	 * @param len length of word
	 * @param tok resulting token, if got 
	 * @return was the token got? 
	 */
	bool get_var(const char* wr, int len, token* tok) {
		// only single line
		if(len != 1) return false;

		// get if inside range of allowed variables
		if(*wr >= 'A' && *wr <= 'Z' || *wr >= 'a' && *wr <= 'z') {
			tok->type = T_VAR;
			tok->payload.var = *wr;
			return true;
		}

		return false;
	}

	/*
	 * Tries to get an operation token, expecting + | - | >= | <= | > | < | != 
	 * | == | =.
	 *
	 * @param wr word to tokenize
	 * @param len length of word
	 * @param tok resulting token, if got 
	 * @return was the token got? 
	 */
	bool get_op(const char* wr, int len, token* tok) {
		if(len > 2) return false;

		// check if known operation
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
				// disamiguate between < and <=
				if(len == 1) {
					tok->payload.op = op_type::L;
				} else if(*(wr + 1) == '=') {
					tok->payload.op = op_type::LEQ;
				} else return false;
				break;

			case '>':
				// disamiguate between > and >=
				if(len == 1) {
					tok->payload.op = op_type::G;
				} else if(*(wr + 1) == '=') {
					tok->payload.op = op_type::GEQ;
				} else return false;
				break;

			case '!':
				// check it ends in = 
				if(len == 2 && *(wr + 1) == '=') {
					tok->payload.op = op_type::NEQ;
				} else return false;
				break;

			case '=':
				// disambiguate between = and ==
				if(len == 1) {
					tok->payload.op = op_type::ASS;
				} else if(*(wr + 1) == '=') {
					tok->payload.op = op_type::EQ;
				} else return false;
				break;
		
			default:
				return false;
		}

		// set type last
		tok->type = T_OP;
		return true;
	}

	/*
	 * Tries to get a number literal token, expecting -? [0:9]+.
	 *
	 * @param wr word to tokenize
	 * @param len length of word
	 * @param tok resulting token, if got 
	 * @return was the token got? 
	 */
	bool get_num(const char* wr, int len, token* tok) {
		unsigned int res = 0;
		bool neg = false;

		// get if negative
		if(*wr == '-') {
			wr++;
			neg = true;
		}

		// temp char
		char c;

		// scroll chars to get number
		while((c = *wr++)) {	
			if(c < '0' || c > '9') return false;
			res = res * 10 + c - '0';
		}

		// set type last
		tok->type = T_NUM;
		tok->payload.num = neg ? (int) -res : (int) res;
		return true;
	}

	/*
	 * Tries to get a keyword token, expecting PRINT | IF | THEN | GOTO | INPUT
	 * | LET | END.
	 *
	 * @param wr word to tokenize
	 * @param len length of word
	 * @param tok resulting token, if got 
	 * @return was the token got? 
	 */
	bool get_key(const char* wr, int len, token* tok) {
		// check if known keyword
		if(!str::cmp(wr, "PRINT")) tok->payload.key = PRINT;
		else if(!str::cmp(wr, "IF")) tok->payload.key = IF;
		else if(!str::cmp(wr, "THEN")) tok->payload.key = THEN;
		else if(!str::cmp(wr, "GOTO")) tok->payload.key = GOTO;
		else if(!str::cmp(wr, "INPUT")) tok->payload.key = INPUT;
		else if(!str::cmp(wr, "LET")) tok->payload.key = LET;
		else if(!str::cmp(wr, "END")) tok->payload.key = END;
		else return false;

		// set type last
		tok->type = T_KEY;
		return true;
	}

	/*
	 * Tries to get a string literal token, expecting <max_str> chars.
	 *
	 * @param wr word to tokenize
	 * @param len length of word
	 * @param tok resulting token, if got 
	 * @return was the token got? 
	 */
	bool get_str(const char* wr, int len, token* tok) {
		// check edge commas and size
		if(*wr != '\"') return false;
		if(*(wr + len - 1) != '\"') return false;
		if(len - 2 >= MAX_STRLEN) return false;

		// allocate string
		char* n_wr = alloc_string();
		if(n_wr == NULL) {
			vid::print_strln("Spazio stringhe esaurito");
			return false;
		}

		// copy string into allocated one
		tok->payload.str = n_wr;
		str::ncpy(n_wr, wr + 1, len - 2);
		n_wr[len - 2] = '\0';

		// set type last
		tok->type = T_STR;
		return true;
	}

	/*
	 * Tries to get a label token, expecting <max_str> chars :, or just the
	 * label name. Automatically populates the label table.
	 *
	 * @param wr word to tokenize
	 * @param len length of word
	 * @param tok resulting token, if got 
	 * @param line the current line
	 * @return was the token got? 
	 */
	bool get_label(const char* wr, int len, token* tok, int line) {
		// check if it's a label definition 
		if(*(wr + len - 1) == ':') {
			// allocate string
			if(len - 1 >= MAX_STRLEN) return false;
			char* n_wr = alloc_string();
			if(n_wr == NULL) {
				vid::print_strln("Spazio stringhe esaurito");
				return false;
			}

			// populate label
			label* lab = alloc_label();
			lab->str = n_wr;
			str::ncpy(n_wr, wr, len - 1);
			n_wr[len - 1] = '\0';
			lab->line = line;
			
			// set type to nop
			tok->type = T_NOP;
			return true;
		}
			
		// get label name 
		if(len >= MAX_STRLEN) return false;
		char temp[MAX_STRLEN];
		mem::cpy(temp, wr, len);
		temp[len] = '\0';

		// resolve label
		label* lab = resolve_label(temp);
		if(lab == NULL) {
			vid::print_str("Etichetta sconosciuta ");
			vid::print_strln(temp);
		}
		tok->payload.num = lab->line;

		// set type last
		tok->type = T_NUM;
		return true;
	}

	/**
	 * Tries to tokenize a word.
	 *
	 * @param wr word to tokenize
	 * @param tok resulting token, if got
	 * @param line the current line
	 * @return was the token got? 
	 */
	bool tokenize_wr(const char* wr, token* tok, int line) {
		// clear token
		mem::set((void*) tok, 0, sizeof(token));

		// get word length
		int len = str::len(wr);

		// try to tokenize
		if(get_var(wr, len, tok))         return true;
		if(get_op(wr, len, tok))          return true;
		if(get_num(wr, len, tok))         return true;
		if(get_key(wr, len, tok))         return true;
		if(get_str(wr, len, tok))         return true;
		if(get_label(wr, len, tok, line)) return true;

		// error
		vid::print_str("Token ignoto: ");
		vid::print_strln(wr);
		return false;
	}

	/**
	 * Tries to tokenize a line. Doesn't use str::tok to keep track of quotes.
	 *
	 * @param ln the line to tokenize
	 * @param toks the resulting array of tokens
	 * @param line the current line
	 * @return bool was the tokenization succesful?
	 */
	#define WORD_SIZE 64
	bool tokenize_ln(const char* ln, token* toks, int line) {
		// don't return anything for empty lines
		if(*ln == '\n' || *ln == '\0' || *ln == '#') return false;	

		// keep track of tokens
		int num_toks = 0;

		// for strings
		bool in_str = false;
		int i = 0; // idx in string
		
		// current word
		char wr[WORD_SIZE];

		// go through chars and get tokens
		while(true) {
			char c = *(ln++);
			
			// next line
			if(c == '\n' || c == '\0') break; 

			if(c == ' ' && !in_str) {
				if(i == 0) continue;

				// terminate token
				wr[i] = '\0';

				// get token
				bool res = tokenize_wr(wr, &toks[num_toks++], line);
				if(num_toks == MAX_TOKS || !res) return false;

				// reset temp token string
				i = 0;
			} else if(c =='\"'){
				wr[i++] = c;

				if(in_str) {
					// terminate token
					wr[i] = '\0';

					// get token
					bool res = tokenize_wr(wr, &toks[num_toks++], line);
					if(num_toks == MAX_TOKS || !res) return false;
			
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
			bool res = tokenize_wr(wr, &toks[num_toks++], line);
			if(num_toks == MAX_TOKS || !res) return false;
		}

		toks[num_toks].type = T_MARK;
		return true;
	}

	/*
	 * Array of variables. 
	 */
	#define NUM_VARS (26 * 2)
	int* vars;

	/**
	 * Array of "is variable set?" flags.
	 */
	bool* vars_set;

	/**
	 * Gets if the given name is a valid variable name, and converts it to a
	 * variable index if positive.
	 *
	 * @param name name of variable
	 * @return is the name valid?
	 */
	bool var_idx(char& name) {
		// get if var name is allowed
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

	/**
	 * Returns a reference to the variable with the given name.
	 *
	 * @param name name of variable
	 * @return reference to variable
	 */
	int& var(char name) {
		// return var reference
		if(var_idx(name)) return vars[name];
		utl::panic("Variabile inesistente richiesta");
	}

	/**
	 * Defines a variable if not defined yet.
	 *
	 * @param name name of variable
	 */
	void define_var(char name) {
		// define the var if it exists
		if(var_idx(name)) {
			vars_set[name] = true;
			return;
		} 
		utl::panic("Cercato di definire una variabile inesistente");
	}

	/**
	 * Gets if a variable is defined.
	 *
	 * @param name name of variable
	 * @return is the variable defined?
	 */
	bool var_defined(char name) {
		// check if var is defined
		if(var_idx(name)) return vars_set[name];
		utl::panic("Cercata definizione di variabile inesistente");
	}

	/**
	 * Gets the value of a token if possible (token is variable o literal).
	 *
	 * @param tok the token to take the value of
	 * @param res the resulting value
	 * @return did the token contain a value?
	 */
	bool get_tok_val(token tok, int& res) {
		// if it's a number, take it
		if(tok.type == T_NUM) {
			res = tok.payload.num;
			return true;
		}

		// if it's a variable, check if defined and take it
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

		vid::print_str("Espressione non puo' contenere token ");
		tok.print();
		vid::newline();
		return false;
	}

	/**
	 * Checks if token represents an arithmetic operator.
	 *
	 * @param tok the token to evaluate
	 * @return does the token represent an arithmetic operator?
	 */
	bool is_aritmop(token tok) {
		if(tok.type != T_OP) return false;

		switch(tok.payload.op) {
			case ADD: return true;
			case SUB: return true;
			default: return false;
		}
	}

	/**
	 * Checks if token represents a relational operator.
	 *
	 * @param tok the token to evaluate
	 * @return does the token represent a relational operator?
	 */
	bool is_relop(token tok) {
		if(tok.type != T_OP) return false;

		switch(tok.payload.op) {
			case GEQ: return true;
			case LEQ: return true;
			case G: return true;
			case L: return true;
			case NEQ: return true;
			case EQ: return true;
			default: return false;
		}
	}

	/**
	 * Performs an arithmetic operation.
	 * 
	 * @param res result (and first operand) of operation
	 * @param arg second operand of operation
	 * @param type type of operation
	 */
	void apply_aritmop(int& res, int arg, op_type type) {
		switch(type) {
			case ADD: res += arg; break;
			case SUB: res -= arg; break;
			default: break; // if no such operation type do nothing
		}
	}

	/**
	 * Performs a relational arithmetic operation.
	 * 
	 * @param res result (and first operand) of operation
	 * @param arg second operand of operation
	 * @param type type of operation
	 */
	bool apply_relop(int arg1, int arg2, op_type type) {
		switch(type) {
			case GEQ: return arg1 >= arg2;
			case LEQ: return arg1 <= arg2;
			case G: return arg1 > arg2;
			case L: return arg1 < arg2;
			case NEQ: return arg1 != arg2;
			case EQ: return arg1 == arg2;
			default: return false;
		}
	}

	/**
	 * Evaluates an expression.
	 *
	 * @param toks tokens that make up the expression
	 * @param res result of expression, if found valid
	 * @param continues does the expression sit in a statement?
	 * @return is the expression valid?
	 */
	bool eval_expr(token*& toks, int& res, bool continues = true) {
		if(!continues && toks->type == T_MARK) {
			vid::print_strln("Espressione vuota");
			return false;
		}

		// get first expression token
		if(!get_tok_val(*toks, res)) return false;

		// move to first operator
		toks++;

		// go through operator, value pairs
		while(toks->type != T_MARK) {
			if(!is_aritmop(*toks)) {
				if(continues) {
					// might continue, move to previous token and return
					toks--;
					return true;
				} else {
					// should end with an expression, meaning error
					vid::print_strln("Spazzatura dopo espressione");
					return false;
				}
			} 

			// get operation type
			op_type type = toks->payload.op;
			
			// move to next token (value)
			toks++;
			if(toks->type == T_MARK) {
				vid::print_strln("Nessun termine dopo operatore");
				return false;
			}
			
			// get value
			int temp_res;
			if(!get_tok_val(*toks, temp_res)) return false;

			// apply operation, value pair
			apply_aritmop(res, temp_res, type);
			
			// move to next token (operator or end)
			toks++;
		}

		return true;
	}

	/**
	 * Executes a print statement.
	 *
	 * @param toks tokens that make up the statement
	 * @return should execution continue?
	 */
	bool exec_print(token* toks) {
		// if string print it
		if(toks->type == T_STR) {
			if(toks[1].type != T_MARK) {
				vid::print_strln("Spazzatura dopo PRINT");
				return false;
			}

			vid::print_strln(toks->payload.str);
			return true;
		}
		
		// expect non continuing expression and print it 
		int res;
		if(eval_expr(toks, res, false)) {
			vid::print_int(res);
			vid::newline();
			return true;
		}

		return false;
	}

	// forward declaration for if statements 
	bool exec_statement(token* toks, int* line = NULL);
	
	/**
	 * Executes an if statement.
	 *
	 * @param toks tokens that make up the statement
	 * @param line reference to current line, which the THEN clause can change
	 * @return should execution continue?
	 */
	bool exec_if(token* toks, int* line) {
		// expect first expression 
		int res1;
		if(!eval_expr(toks, res1)) return false;

		// move to next token (relational op)
		toks++;

		// expect relational op
		if(!is_relop(*toks)) {
			vid::print_strln("Operatore relazionale invalido: ");
			toks->print();
			vid::newline();
			return false;
		}

		// get operation type
		op_type type = toks->payload.op;

		// move to next token (expression)
		toks++;

		// expect second expression
		int res2;
		if(!eval_expr(toks, res2)) return false;

		// check here (early) if it goes through
		bool through = apply_relop(res1, res2, type);

		// move to next token (THEN)
		toks++;

		// expect THEN keyword
		if(toks->type != T_KEY || toks->payload.key != THEN) {
			vid::print_strln("Nessun THEN dopo IF");
			return false;
		}

		// if through, expect a statement to follow and execute it
		if(through) {
			exec_statement(toks + 1, line);
		}

		// otherwise move on
		return true;	
	}

	/**
	 * Executes a goto statement.
	 *
	 * @param toks tokens that make up the statement
	 * @param line reference to current line, which the statement will change
	 * @return should execution continue?
	 */
	bool exec_goto(token* toks, int* line) {
		// expect expression and get line number
		int res;
		if(!eval_expr(toks, res, false)) {
			return false;
		}

		if(res < 0 | res >= MAX_LINES) {
			vid::print_strln("GOTO fuori campo");
			return false;
		}

		// jump to line number
		if(line) *line = res - 1;

		return true;
	}

	/**
	 * Executes an input statement.
	 *
	 * @param toks tokens that make up the statement
	 * @return should execution continue?
	 */
	bool exec_input(token* toks) {
		// expect var
		if(toks->type != T_VAR) {
			vid::print_strln("Nessuna variabile dopo INPUT");
			return false;
		}
		
		if(toks[1].type != T_MARK) {
			vid::print_strln("Spazzatura dopo INPUT");
			return false;
		}

		// get var name
		char name = toks->payload.var;
		
		// get value
		int val = kyb::read_int();

		// set var to got value
		define_var(name);
		var(name) = val;

		return true;	
	}

	/**
	 * Executes a let variable statement.
	 *
	 * @param toks tokens that make up the statement
	 * @return should execution continue?
	 */
	bool exec_let(token* toks) {
		// expect var
		if(toks->type != T_VAR) {
			vid::print_strln("Nessuna variabile dopo LET");
			return false;
		}

		// get var name
		char name = toks->payload.var;

		toks++;

		// expect assignment operator
		if(toks->type != T_OP || toks->payload.op != ASS) {
			vid::print_strln("Nessun operatore di assegnamento dopo LET");
		}

		toks++;

		// expect non continuing expression and get value
		int res;
		if(!eval_expr(toks, res, false)) return false;

		// set var
		define_var(name);
		var(name) = res;

		return true;	
	}

	/**
	 * Executes an end variable statement.
	 *
	 * @param toks tokens that make up the statement
	 * @return should execution continue?
	 */
	bool exec_end(token* toks) {
		if(toks->type != T_MARK) {
			vid::print_strln("Spazzatura dopo END");
			return false;
		}

		// just return false (halts execution)
		return false;
	}

	/**
	 * Executes a statement.
	 *
	 * @param toks tokens that make up the statement
	 * @param line reference to current line, which the statement can change
	 * @return should execution continue?
	 */
	bool exec_statement(token* toks, int* line) {
		// empty statements and NOPSalways run
		if(toks[0].type == T_MARK || toks[0].type == T_NOP) return true;

		// expect all keywords but THEN
		if(toks[0].type != T_KEY || toks[0].payload.key == THEN) {
			vid::print_str("Istruzione non puo' iniziare con token ");
			toks[0].print();
			vid::newline();
			return false;
		}

		switch(toks[0].payload.key) {
			case(PRINT): 	return exec_print(toks + 1);
			// these need a reference to line number as they might change it
			case(IF): 		return exec_if(toks + 1, line);
			case(GOTO): 	return exec_goto(toks + 1, line);
			case(INPUT):	return exec_input(toks + 1);
			case(LET): 		return exec_let(toks + 1);
			case(END):		return exec_end(toks + 1);
			default: return false;
		}
	}
	
	/**
	 * Path of current file.
	 */
	char* file;

	/**
	 * Opens the current file.
	 *
	 * @return was the operation succesful?
	 */
	bool open_file() {
		// show intent
		vid::print_str("Leggo file ");
		vid::print_strln(file);
		tim::sleep(500);

		// initialize file buffer 
		char fbuf[FILE_BUF_SIZE];

		// read file from disk
		int fsiz = blk::dir::read_file(file, fbuf, FILE_BUF_SIZE, 
				blk::dir::cur);
		if(fsiz == -1) {
			vid::print_strln("Errore lettura file");
			return 0;
		}

		int i = 0; // index in buffer
		int j = 0; // current line

		// tokenize lines
		while (i < fsiz && j < MAX_LINES) {
			if(tokenize_ln(fbuf + i, lines[j], j)) j++;
			
			// advance to next line
			while (i < fsiz && fbuf[i] != '\n') i++;
			i++; // skip '\\n'
		}

		return 1;
	}

} // bas::

using namespace bas;
namespace app {
	ENTRY(basic) {
		// grab memory
		char _strings[MAX_STRINGS][MAX_STRLEN];
		strings = _strings;
		int _vars[NUM_VARS];
		vars = _vars;
		bool _vars_set[NUM_VARS];
		vars_set = _vars_set;
		token _lines[MAX_LINES][MAX_TOKS];
		lines = _lines;
		label _labels[MAX_LABELS];
		labels = _labels;

		// init lines
		for(int i = 0; i < MAX_LINES; i++) {
			lines[i][0].type = T_MARK;
		}
		
		// reset vars
		for(int i = 0; i < 26 * 2; i++) {
			vars_set[i] = false;
		}
		
		// get requested file path
		if(argc < 2) {
			vid::print_strln("Nome file?");
			return 1;
		}
		file = argv[1];
		
		// try opening file (containing program)
		if(!open_file()) return 2;

		// execute progam
		for(int i = 0; i < MAX_LINES; i++) {
			if(!exec_statement(lines[i], &i)) break; 
		}

		return 0;
	}
} // app::
