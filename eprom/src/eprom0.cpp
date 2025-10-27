#include "lib/lib.h"
#include "lib/video/video.h"

/*
 * Characters.
 */
const char HORIZ      		= 0xc4;
const char VERT       		= 0xb3;
const char TOP_LEFT   		= 0xda;
const char TOP_RIGHT  		= 0xbf;
const char BOTTOM_LEFT		= 0xc0;
const char BOTTOM_RIGHT		= 0xd9;
const char T_LEFT     		= 0xb4;
const char T_RIGHT    		= 0xc3;
const char T_TOP      		= 0xc1;
const char T_BOTTOM   		= 0xc2;
const char CROSS      		= 0xc5;

const char HORIZ_D    		= 0xcd;
const char VERT_D     		= 0xba;
const char TOP_LEFT_D 		= 0xc9;
const char TOP_RIGHT_D		= 0xbb;
const char BOTTOM_LEFT_D	= 0xc8;
const char BOTTOM_RIGHT_D	= 0xbc;
const char T_LEFT_D   		= 0xb9;
const char T_RIGHT_D  		= 0xcc;
const char T_TOP_D    		= 0xca;
const char T_BOTTOM_D 		= 0xcb;
const char CROSS_D    		= 0xce;

const char PELLET					= 0x07;
const char SMILEY					= 0x02;
const char BAD_SMILEY			= 0x01;

/*
 * Map.
 */
const int MAP_SIZE = 30;
const int MAP_OFFSET = 25;

const char MAP[] =
"##############################"
"#                            #"
"# ######## ######## ######## #"
"# #@@@@@@# #@@@@@@# #@@@@@@# #"
"# ######## ###@@### ######## #"
"#            #@@#            #"
"# ########## #@@# ########## #"
"# #@@@@@@@@# #@@# #@@@@@@@@# #"
"# #####@@### #### ###@@##### #"
"#     #@@#          #@@#     #"
"# ### #@@# ######## #@@# ### #"
"# #@# #### #@@@@@@# #### #@# #"
"# #@#      #@@@@@@#      #@# #"
"# #@###### #@@@@@@# ######@# #"
"  #@@@@@@# #@@@@@@# #@@@@@@#  "
"  #@@@@@@# #@@@@@@# #@@@@@@#  "
"# #@###### #@@@@@@# ######@# #"
"# #@#      #@@@@@@#      #@# #"
"# #@# #### #@@@@@@# #### #@# #"
"# ### #@@# ######## #@@# ### #"
"#     #@@#          #@@#     #"
"# #####@@### #### ###@@##### #"
"# #@@@@@@@@# #@@# #@@@@@@@@# #"
"# ########## #@@# ########## #"
"#            #@@#            #"
"# ######## ###@@### ######## #"
"# #@@@@@@# #@@@@@@# #@@@@@@# #"
"# ######## ######## ######## #"
"#                            #"
"##############################";

const vid::coords START_POS = {1, 1};

unsigned int num_pellets;
bool pellets[MAP_SIZE * MAP_SIZE];

int get_map_idx(vid::coords coords) {
	return coords.col + coords.row * MAP_SIZE;
}

vid::coords map_to_vram(vid::coords in) {
	return {in.row, in.col + MAP_OFFSET};
}

char get_table_char(bool a, bool b, bool l, bool r) {
	int key = 0;
	key |= (a ? 1 : 0); // above
	key |= (b ? 2 : 0); // below
	key |= (l ? 4 : 0); // left
	key |= (r ? 8 : 0); // right

	switch(key) {
		case 0b0000: return '\0';   
		case 0b0001: return VERT;
		case 0b0010: return VERT;
		case 0b0011: return VERT;
		case 0b0100: return HORIZ;
		case 0b1000: return HORIZ;
		case 0b1100: return HORIZ;
		case 0b0101: return BOTTOM_RIGHT;
		case 0b1001: return BOTTOM_LEFT;
		case 0b0110: return TOP_RIGHT;
		case 0b1010: return TOP_LEFT;
		case 0b0111: return T_LEFT;
		case 0b1011: return T_RIGHT;
		case 0b1101: return T_TOP;
		case 0b1110: return T_BOTTOM;
		case 0b1111: return CROSS;
		default: return '\0';            
	}
}

void draw_map() {
	for(int r = 0; r < MAP_SIZE; r++) {
		for(int c = 0; c < MAP_SIZE; c++) {
			bool present = MAP[get_map_idx({r, c})] == '#';
			if(!present) continue;

			char t = '\0';

			if(r == 0 || c == 0 || r == MAP_SIZE - 1 || c == MAP_SIZE - 1) {
				// borders
				if(r == 0 || r == MAP_SIZE - 1) t = HORIZ_D;
				if(c == 0 || c == MAP_SIZE - 1) t = VERT_D;
				if(r == 0 && c == 0) t = TOP_LEFT_D;
				if(r == 0 && c == MAP_SIZE - 1) t = TOP_RIGHT_D;
				if(r == MAP_SIZE - 1 && c == 0) t = BOTTOM_LEFT_D;
				if(r == MAP_SIZE - 1 && c == MAP_SIZE - 1) t = BOTTOM_RIGHT_D;
			} else {
				// inside
				bool above = MAP[get_map_idx({r - 1, c})] == '#';
				bool below = MAP[get_map_idx({r + 1, c})] == '#';
				bool left = MAP[get_map_idx({r, c - 1})] == '#';
				bool right = MAP[get_map_idx({r, c + 1})] == '#';
			
				t = get_table_char(above, below, left, right);
			}

			vid::put_char(map_to_vram({r, c}), t);
		}
	}
}

void fill_pellets() {
	str::mset((void*) pellets, 0, MAP_SIZE * MAP_SIZE * sizeof(bool));
	num_pellets = 0;

	for(int r = 1; r < MAP_SIZE - 1; r++) {
		for(int c = 1 ; c < MAP_SIZE - 1; c++) {
			char map_tile = MAP[get_map_idx({r, c})];
			bool present = map_tile == '#' || map_tile == '@';
			if(present) continue;
				
			// is pellet
			pellets[get_map_idx({r, c})] = true;
			num_pellets++;
			vid::put_char(map_to_vram({r, c}), PELLET);
		}
	}
	
	// clear pellet player starts on 
	bool& cur_pellet = pellets[get_map_idx(START_POS)];
	if(cur_pellet) {
		cur_pellet = false;
		num_pellets--;
	}
}

/*
 * Player.
 */
const vid::coords NORTH	= {-1, 0};
const vid::coords SOUTH	= {1,  0};
const vid::coords WEST 	= {0, -1};
const vid::coords EAST 	= {0,  1};

struct {
	vid::coords pos = START_POS;
	vid::coords dir = EAST;
} player;

vid::coords wanted_dir = EAST;

void draw_player() {
	vid::put_char(map_to_vram(player.pos), SMILEY);
}

vid::coords wrap(vid::coords coords) {
	if(coords.row < 0) coords.row = MAP_SIZE - 1;
	if(coords.row >= MAP_SIZE) coords.row = 0;
	if(coords.col < 0) coords.col = MAP_SIZE - 1;
	if(coords.col >= MAP_SIZE) coords.col = 0;

	return coords;
}

void update_player() {	
	// get input
	char k = kyb::poll_char();
	switch(k) {
		case 'W': case 'w': wanted_dir = NORTH; break;
		case 'A': case 'a': wanted_dir = WEST; break;
		case 'S': case 's': wanted_dir = SOUTH; break;
		case 'D': case 'd': wanted_dir = EAST; break;
	}

	vid::coords wanted_pos = wrap(player.pos + wanted_dir);
	
	// try moving
	vid::coords new_pos = wanted_pos;

	if(MAP[get_map_idx(wanted_pos)] != '#') { 
		// can move in wanted direction
		player.dir = wanted_dir;
	} else {
		// can't move in wanted direction, hold previous and go on
		new_pos = wrap(player.pos + player.dir);
	}

	if(MAP[get_map_idx(new_pos)] != '#') { 
		// actually move
		vid::put_char(map_to_vram(player.pos), '\0');
		player.pos = new_pos;
		vid::put_char(map_to_vram(player.pos), SMILEY);

		// eat pellet
		bool& cur_pellet = pellets[get_map_idx(player.pos)];
		if(cur_pellet) {
			cur_pellet = false;
			num_pellets--;
		}
	}
}

/*
 * Ghosts.
 */
unsigned int seed = 12345;
unsigned int rand() {
	seed = seed * 1664525 + 1013904223;
	return seed;
}

const vid::coords GHOST0_POS = {28, 28};
const vid::coords GHOST0_DIR = WEST;
const vid::coords GHOST1_POS = {28, 1};
const vid::coords GHOST1_DIR = NORTH;
const vid::coords GHOST2_POS = {20, 19};
const vid::coords GHOST2_DIR = NORTH;

struct ghost {
	vid::coords pos;
	vid::coords dir;
};

bool update_ghost(ghost& gh) {
	vid::coords new_pos = wrap(gh.pos + gh.dir);

	if(MAP[get_map_idx(new_pos)] == '#') { 
		// collided, check for other direction
		vid::coords options[4];
		int count = 0;

		if(MAP[get_map_idx(gh.pos + NORTH)] != '#') options[count++] = NORTH;
		if(MAP[get_map_idx(gh.pos + SOUTH)] != '#') options[count++] = SOUTH;
		if(MAP[get_map_idx(gh.pos + WEST)] != '#') options[count++] = WEST;
		if(MAP[get_map_idx(gh.pos + EAST)] != '#') options[count++] = EAST;

		if(count == 0) new_pos = gh.pos; // stuck
		else {
			vid::coords new_dir = options[rand() % count];
			new_pos = gh.pos + new_dir;
			gh.dir = new_dir;
		}
	}

	bool collided = false;
	collided |= gh.pos == player.pos;

	vid::put_char(map_to_vram(gh.pos), pellets[get_map_idx(gh.pos)] ? PELLET : '\0'); // put pellet
	
	gh.pos = new_pos;
	collided |= gh.pos == player.pos;
	
	vid::put_char(map_to_vram(gh.pos), BAD_SMILEY);
	
	return collided ;
}

/*
 * Ui.
 */
void print_heading(vid::coords pos, vid::coords dir) {
	if(dir == NORTH) vid::put_str(pos, "NORTH");
	if(dir == SOUTH) vid::put_str(pos, "SOUTH");
	if(dir == WEST) vid::put_str(pos, "WEST ");
	if(dir == EAST) vid::put_str(pos, "EAST ");
}

void draw_ui(ghost* ghosts) {
	vid::put_str({1, 1}, "Risc-man v0.0");
	
	vid::put_str({1, 56}, "Pellets: ");
	vid::put_uint({1, 66}, num_pellets);
	
	vid::put_str({2, 1}, "Player X: ");
	vid::put_int({2, 11}, player.pos.row);
	vid::put_str({3, 1}, "Player Y: ");
	vid::put_int({3, 11}, player.pos.col);
	vid::put_str({4, 1}, "Player heading: ");
	print_heading({4, 17}, player.dir);
	
	vid::put_str({2, 56}, "Ghost 0 X: ");
	vid::put_int({2, 67}, ghosts[0].pos.row);
	vid::put_str({3, 56}, "Ghost 0 Y: ");
	vid::put_int({3, 67}, ghosts[0].pos.col);
	vid::put_str({4, 56}, "Ghost 0 heading: ");
	print_heading({4, 73}, ghosts[0].dir);
	
	vid::put_str({26, 1}, "Ghost 1 X: ");
	vid::put_int({26, 12}, ghosts[1].pos.row);
	vid::put_str({27, 1}, "Ghost 1 Y: ");
	vid::put_int({27, 12}, ghosts[1].pos.col);
	vid::put_str({28, 1}, "Ghost 1 heading: ");
	print_heading({28, 18}, ghosts[1].dir);
	
	vid::put_str({26, 56}, "Ghost 2 X: ");
	vid::put_int({26, 67}, ghosts[2].pos.row);
	vid::put_str({27, 56}, "Ghost 2 Y: ");
	vid::put_int({27, 67}, ghosts[2].pos.col);
	vid::put_str({28, 56}, "Ghost 2 heading: ");
	print_heading({28, 73}, ghosts[2].dir);
}

void main() {
	start:
	vid::clear();
	vid::set_cursor({-1, -1}); // hide cursor
	
	// init map
	draw_map();
	fill_pellets();

	// init ghosts
	ghost ghosts[] = {
		{ GHOST0_POS, GHOST0_DIR },
		{ GHOST1_POS, GHOST1_DIR },
		{ GHOST2_POS, GHOST2_DIR }
	};
	int num_ghosts = 3;

	// init player
	player.pos = START_POS;
	player.dir = EAST;

	// game loop
	while(true) {
		update_player();

		bool gameover = false;
		for(int i = 0; i < num_ghosts; i++) {
			gameover |= update_ghost(ghosts[i]);
		}

		if(gameover) {
			vid::put_str({15, 35}, "Game over!");
			utl::wait();
			goto start;
		}

		if(num_pellets == 0) {
			vid::put_str({15, 35}, "Hai vinto!");
			utl::wait();
			goto start;
		}	
		
		draw_ui(ghosts);

		tim::wait_ticks(200);
	}
}
