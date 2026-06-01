#include "../lib/lib.h"

/**
 * Namespace for PACMAN clone implementation. 
 */
namespace pac {

	/**
	 * Characters used to draw screen. 
	 */
	#define HORIZ          0xc4
	#define VERT           0xb3
	#define TOP_LEFT       0xda
	#define TOP_RIGHT      0xbf
	#define BOTTOM_LEFT    0xc0
	#define BOTTOM_RIGHT   0xd9
	#define T_LEFT         0xb4
	#define T_RIGHT        0xc3
	#define T_TOP          0xc1
	#define T_BOTTOM       0xc2
	#define CROSS          0xc5
	#define HORIZ_D        0xcd
	#define VERT_D         0xba
	#define TOP_LEFT_D     0xc9
	#define TOP_RIGHT_D    0xbb
	#define BOTTOM_LEFT_D  0xc8
	#define BOTTOM_RIGHT_D 0xbc
	#define T_LEFT_D       0xb9
	#define T_RIGHT_D      0xcc
	#define T_TOP_D        0xca
	#define T_BOTTOM_D     0xcb
	#define CROSS_D        0xce
	
	#define PELLET         0x07
	#define SMILEY         0x02
	#define BAD_SMILEY     0x01

	/**
	 * Size of game map (square). 
	 */
	const int map_size = 30;

	/**
	 * Vertical offset of game map. 
	 */
	const int map_offset = 25;

	/**
	 * Array of game map layout. # represents a wall and @ represents an 
	 * inaccessible (filled-in) region.
	 */
	const char map[] =
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

	/**
	 * Starting position of player.
	 */
	const vid::coords start_pos = {1, 1};

	/**
	 * Number of remaining pellets.
	 */
	unsigned int num_pellets;

	/**
	 * Array of pellet positions. 
	 */
	bool* pellets;

	/**
	 * Returns the index in the map array of a coordinate pair.
	 *
	 * @param coords map coordinates
	 * @return index in map array
	 */
	int get_map_idx(vid::coords coords) {
		return coords.col + coords.row * map_size;
	}

	/**
	 * Converts map coordinates to screen coordinates.
	 *
	 * @param in map coordinates 
	 * @return vram coordinates 
	 */
	vid::coords map_to_vram(vid::coords coords) {
		return {coords.row, coords.col + map_offset};
	}

	/**
	 * Gets character to print on screen for given tile and boundary tiles. Needed 
	 * because the map is stored as an array of # characters (representing walls), 
	 * and we want pretty printing of corners and intersections.
	 * 
	 * @param tile above?
	 * @param tile below?
	 * @param tile left?
	 * @param tile right?
	 */
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

	/**
	 * Draws the map.
	 */
	void draw_map() {
		for(int r = 0; r < map_size; r++) {
			for(int c = 0; c < map_size; c++) {
				bool present = map[get_map_idx({r, c})] == '#';
				if(!present) continue;

				char t = '\0';

				if(r == 0 || c == 0 || r == map_size - 1 || c == map_size - 1) {
					// borders
					if(r == 0 || r == map_size - 1) t = HORIZ_D;
					if(c == 0 || c == map_size - 1) t = VERT_D;
					if(r == 0 && c == 0) t = TOP_LEFT_D;
					if(r == 0 && c == map_size - 1) t = TOP_RIGHT_D;
					if(r == map_size - 1 && c == 0) t = BOTTOM_LEFT_D;
					if(r == map_size - 1 && c == map_size - 1) t = BOTTOM_RIGHT_D;
				} else {
					// inside
					bool above = map[get_map_idx({r - 1, c})] == '#';
					bool below = map[get_map_idx({r + 1, c})] == '#';
					bool left = map[get_map_idx({r, c - 1})] == '#';
					bool right = map[get_map_idx({r, c + 1})] == '#';
				
					t = get_table_char(above, below, left, right);
				}

				vid::put_char(map_to_vram({r, c}), t);
			}
		}
	}

	/**
	 * Fills the map with pellets.
	 */
	void fill_pellets() {
		mem::set((void*) pellets, 0, map_size * map_size * sizeof(bool));
		num_pellets = 0;

		for(int r = 1; r < map_size - 1; r++) {
			for(int c = 1 ; c < map_size - 1; c++) {
				char map_tile = map[get_map_idx({r, c})];
				bool present = map_tile == '#' || map_tile == '@';
				if(present) continue;
					
				// is pellet
				pellets[get_map_idx({r, c})] = true;
				num_pellets++;
				vid::put_char(map_to_vram({r, c}), PELLET);
			}
		}
		
		// clear pellet player starts on 
		bool& cur_pellet = pellets[get_map_idx(start_pos)];
		if(cur_pellet) {
			cur_pellet = false;
			num_pellets--;
		}
	}

	/**
	 * Cardinal directions.
	 */
	 #define NORTH {-1, 0}
	 #define SOUTH {1,  0}
	 #define WEST  {0, -1}
	 #define EAST  {0,  1}

	/**
	 * Struct for player data.
	 */
	struct {
		/**
		 * Position of player.
		 */
		vid::coords pos = start_pos;

		/**
		 * Direction player is heading in.
		 */
		vid::coords dir = EAST;
	} player;

	/**
	 * Wanted direction of player.
	 */
	vid::coords wanted_dir = EAST;

	/**
	 * Draws the player.
	 */
	void draw_player() {
		vid::put_char(map_to_vram(player.pos), SMILEY);
	}

	/**
	 * Wraps coordinates inside playable region.
	 *
	 * @param coords coordinates to wrap
	 * @return wrapped coordinates
	 */
	vid::coords wrap(vid::coords coords) {
		if(coords.row < 0) coords.row = map_size - 1;
		if(coords.row >= map_size) coords.row = 0;
		if(coords.col < 0) coords.col = map_size - 1;
		if(coords.col >= map_size) coords.col = 0;

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

		if(map[get_map_idx(wanted_pos)] != '#') { 
			// can move in wanted direction
			player.dir = wanted_dir;
		} else {
			// can't move in wanted direction, hold previous and go on
			new_pos = wrap(player.pos + player.dir);
		}

		if(map[get_map_idx(new_pos)] != '#') { 
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

	/**
	 * Seed for random number generation.
	 */
	unsigned int seed = 12345;

	/**
	 * Generates a random number.
	 *
	 * @return the random number
	 */
	unsigned int rand() {
		seed = seed * 1664525 + 1013904223;
		return seed;
	}

	/**
	 * Init position of ghost 0.
	 */
	const vid::coords ghost0_pos = {28, 28};

	/**
	 * Init direction of ghost 0.
	 */
	const vid::coords ghost0_dir = WEST;

	/**
	 * Init position of ghost 1.
	 */
	const vid::coords ghost1_pos = {28, 1};

	/**
	 * Init direction of ghost 1.
	 */
	const vid::coords ghost1_dir = NORTH;

	/**
	 * Init position of ghost 2.
	 */
	const vid::coords ghost2_pos = {20, 19};

	/**
	 * Init direction of ghost 2.
	 */
	const vid::coords ghost2_dir = NORTH;

	/**
	 * Represents a ghost.
	 */
	struct ghost {
		/**
		 * Position of ghost.
		 */
		vid::coords pos;

		/**
		 * Heading direction of ghost.
		 */
		vid::coords dir;
	};

	/**
	 * Array of ghosts.
	 */
	ghost ghosts[] = {
		{ ghost0_pos, ghost0_dir },
		{ ghost1_pos, ghost1_dir },
		{ ghost2_pos, ghost2_dir }
	};

	/**
	 * Number of ghosts.
	 */
	int num_ghosts = 3;


	/**
	 * Updates a ghost.
	 *
	 * @param gh reference to the ghost
	 * @return did the ghost collide with the player
	 */
	bool update_ghost(ghost& gh) {
		vid::coords new_pos = wrap(gh.pos + gh.dir);

		if(map[get_map_idx(new_pos)] == '#') { 
			// collided, check for other direction
			vid::coords options[4];
			int count = 0;

			if(map[get_map_idx(gh.pos + (vid::coords)NORTH)] != '#') 
				options[count++] = NORTH;
			if(map[get_map_idx(gh.pos + (vid::coords)SOUTH)] != '#') 
				options[count++] = SOUTH;
			if(map[get_map_idx(gh.pos + (vid::coords)WEST)] != '#') 
				options[count++] = WEST;
			if(map[get_map_idx(gh.pos + (vid::coords)EAST)] != '#') 
				options[count++] = EAST;

			if(count == 0) new_pos = gh.pos; // stuck
			else {
				vid::coords new_dir = options[rand() % count];
				new_pos = gh.pos + new_dir;
				gh.dir = new_dir;
			}
		}

		// check if we are colliding before moving
		bool collided = false;
		collided |= gh.pos == player.pos;
		vid::put_char(map_to_vram(gh.pos), 
				pellets[get_map_idx(gh.pos)] ? PELLET : '\0'); // put pellet

		// check if we are colliding after moving
		gh.pos = new_pos;
		collided |= gh.pos == player.pos;
		vid::put_char(map_to_vram(gh.pos), BAD_SMILEY);
		
		return collided;
	}

	/*
	 * Print position and heading direction of player or ghost.
	 *
	 * @param pos position to print
	 * @param dir direction to print
	 */
	void print_heading(vid::coords pos, vid::coords dir) {
		if(dir == (vid::coords)NORTH) vid::put_str(pos, "nord ");
		if(dir == (vid::coords)SOUTH) vid::put_str(pos, "sud  ");
		if(dir == (vid::coords)WEST) vid::put_str(pos, "ovest");
		if(dir == (vid::coords)EAST) vid::put_str(pos, "est  ");
	}

	/**
	 * Draw UI on screen.
	 */
	void draw_ui() {
		vid::put_str({1, 1}, "Risc-man");
		
		vid::put_str({1, 56}, "Pellet: ");
		vid::put_uint({1, 66}, num_pellets);
		
		vid::put_str({2, 1}, "X giocatore: ");
		vid::put_int({2, 14}, player.pos.row);
		vid::put_str({3, 1}, "Y Giocatore: ");
		vid::put_int({3, 14}, player.pos.col);
		vid::put_str({4, 1}, "Riscman diretto a ");
		print_heading({4, 19}, player.dir);
		
		vid::put_str({2, 56}, "X Fantasma 0: ");
		vid::put_int({2, 70}, ghosts[0].pos.row);
		vid::put_str({3, 56}, "Y Fantasma 0: ");
		vid::put_int({3, 70}, ghosts[0].pos.col);
		vid::put_str({4, 56}, "Fant. 0 diretto a ");
		print_heading({4, 74}, ghosts[0].dir);
		
		vid::put_str({26, 1}, "X Fantasma 1: ");
		vid::put_int({26, 14}, ghosts[1].pos.row);
		vid::put_str({27, 1}, "Y Fantasma 1: ");
		vid::put_int({27, 14}, ghosts[1].pos.col);
		vid::put_str({28, 1}, "Fant. 1 diretto a ");
		print_heading({28, 19}, ghosts[1].dir);
		
		vid::put_str({26, 56}, "X Fantasma 2: ");
		vid::put_int({26, 70}, ghosts[2].pos.row);
		vid::put_str({27, 56}, "Y Fantasma 2: ");
		vid::put_int({27, 70}, ghosts[2].pos.col);
		vid::put_str({28, 56}, "Fant. 2 diretto a ");
		print_heading({28, 74}, ghosts[2].dir);
	}

	/**
	 * Prompt the user for exit.
	 */
	bool prompt_exit() {
		vid::put_str({16, 31}, "Vuoi uscire? (s/n)");

		char c = kyb::get_char();
		return c == 's';
	}

} // pac::

using namespace pac;
namespace app {
	ENTRY(pacman) {
		// grab memory
		bool _pellets[map_size * map_size];
		pellets = _pellets;
		
		start:
		vid::clear();
		vid::set_cursor({-1, -1}); // hide cursor
			
		// init ghosts
		ghost ghosts[] = {
			{ ghost0_pos, ghost0_dir },
			{ ghost1_pos, ghost1_dir },
			{ ghost2_pos, ghost2_dir }
		};
		int num_ghosts = 3;

		// init player
		player.pos = start_pos;
		player.dir = EAST;
		
		// init map
		draw_map();
		fill_pellets();

		// game loop
		while(true) {
			update_player();

			bool gameover = false;
			for(int i = 0; i < num_ghosts; i++) {
				gameover |= update_ghost(ghosts[i]);
			}

			if(gameover) {
				vid::put_str({15, 35}, "Game over!");
				if(prompt_exit()) break;
				goto start;
			}

			if(num_pellets == 0) {
				vid::put_str({15, 35}, "Hai vinto!");
				if(prompt_exit()) break;
				goto start;
			}	
			
			draw_ui();

			tim::sleep(200);
		}

		vid::clear();
		return 0;
	}
} // app::
