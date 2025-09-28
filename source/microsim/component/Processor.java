package microsim.component;

import java.util.Map;

enum ProcessorState {
	FETCH, // read instruction
	DECODE, // decode instruction

	// execution states
	// movement
	MOV,
	MOV_IMMEDIATE,
	LOAD,
	LOAD_IMMEDIATE,
	STORE,
	STORE_IMMEDIATE,
	// arithmetic
	ADD,
	ADD_IMMEDIATE,
	SUB,
	SUB_IMMEDIATE,
	CMP,
	CMP_IMMEDIATE,
	INC,
	DEC,
	// logic
	AND,
	AND_IMMEDIATE,
	OR,
	OR_IMMEDIATE,
	NOT,
	// utility
	SHL,
	SHL_IMMEDIATE,
	SHR,
	SHR_IMMEDIATE,
	// stack
	PUSH,
	POP,
	CALL,
	RET,
	// jumps
	JMP,
	JO,
	JNO,
	JS,
	JNS,
	JZ,
	JNZ,
	JC,
	JNC,
	// other
	NOP,
	HLT,

	MEM_READ0, // memory read routine
	MEM_READ1,
	MEM_READ2,

	MEM_WRITE0, // memory write routine

}

public class Processor implements RunnableComponent {
	// register constants
	private static final int GENERAL_REGISTERS = 4;
	
	private static final char RESET_INSTRUCTION_ADDRESS = 0x9400; // set to EPROM_BEG 

	// instruction maps
	Map<Character, ProcessorState> opcodeStrings = Map.ofEntries(
		// movement
		Map.entry((char)0x0000, ProcessorState.MOV),
		Map.entry((char)0x8000, ProcessorState.MOV_IMMEDIATE),
		Map.entry((char)0x0040, ProcessorState.LOAD),
		Map.entry((char)0x8040, ProcessorState.LOAD_IMMEDIATE),
		Map.entry((char)0x0080, ProcessorState.STORE),
		Map.entry((char)0x8080, ProcessorState.STORE_IMMEDIATE),
		// arithmetic
		Map.entry((char)0x4000, ProcessorState.ADD),
		Map.entry((char)0xc000, ProcessorState.ADD_IMMEDIATE),
		Map.entry((char)0x4040, ProcessorState.SUB),
		Map.entry((char)0xc040, ProcessorState.SUB_IMMEDIATE),
		Map.entry((char)0x4080, ProcessorState.CMP),
		Map.entry((char)0xc080, ProcessorState.CMP_IMMEDIATE),
		Map.entry((char)0x40c0, ProcessorState.INC),
		Map.entry((char)0x4100, ProcessorState.DEC),
		// logic
		Map.entry((char)0x2000, ProcessorState.AND),
		Map.entry((char)0xa000, ProcessorState.AND_IMMEDIATE),
		Map.entry((char)0x2040, ProcessorState.OR),
		Map.entry((char)0xa040, ProcessorState.OR_IMMEDIATE),
		Map.entry((char)0x2080, ProcessorState.NOT),
		// utility
		Map.entry((char)0x6000, ProcessorState.SHL),
		Map.entry((char)0xe000, ProcessorState.SHL_IMMEDIATE),
		Map.entry((char)0x6040, ProcessorState.SHR),
		Map.entry((char)0xe040, ProcessorState.SHR_IMMEDIATE),
		// stack
		Map.entry((char)0x1000, ProcessorState.PUSH),
		Map.entry((char)0x1040, ProcessorState.POP),
		Map.entry((char)0x9080, ProcessorState.CALL),
		Map.entry((char)0x10c0, ProcessorState.RET),
		// jumps
		Map.entry((char)0x8800, ProcessorState.JMP),
		Map.entry((char)0x8840, ProcessorState.JO),
		Map.entry((char)0x8880, ProcessorState.JNO),
		Map.entry((char)0x88c0, ProcessorState.JS),
		Map.entry((char)0x8900, ProcessorState.JNS),
		Map.entry((char)0x8940, ProcessorState.JZ),
		Map.entry((char)0x8980, ProcessorState.JNZ),
		Map.entry((char)0x89c0, ProcessorState.JC),
		Map.entry((char)0x8a00, ProcessorState.JNC),
		// other
		Map.entry((char)0x4800, ProcessorState.HLT),
		Map.entry((char)0x4840, ProcessorState.NOP)
	);

	private final Bus bus;

	// general purpose registers
	private char[] registers = new char[GENERAL_REGISTERS];
		
	// control registers
	private char ip;
	private char sp;

	// flag register
	private boolean of;
	private boolean sf;
	private boolean zf;
	private boolean cf;

	// utilities 
	private int sourceIndex; // source register index
													 // 0-3: general registers
													 // 4: ip
													 // 5: sp
	private int destIndex; // destination register index
	
	private char temp; // read utility register

	// state machine handling
	ProcessorState state = ProcessorState.FETCH;
	ProcessorState returnState; // aka multiway jump register

	public Processor(Bus bus) {
		this.bus = bus;

		// processor instantly takes control of all lines but data
		bus.addressLine.drive(this, (char)0);
	
		bus.readEnable.drive(this, false);
		bus.writeEnable.drive(this, false);
	
		bus.targetSpace.drive(this, false);

		// reset instruction pointer
		ip = RESET_INSTRUCTION_ADDRESS;
	}

	@Override
	public void step() {
		System.out.println("Processor has state " + state.name());

		switch(state) {
			case FETCH: {
				// read next instruction at ip
				bus.addressLine.drive(this, ip);

				// return to decode after read
				returnState = ProcessorState.DECODE;
				
				// init read
				state = ProcessorState.MEM_READ0;
				
				break;
			}
			case DECODE: {
				char opcode = temp;

				System.out.println(String.format("%04X", opcode & 0xFFFF));

				// bit 15 of opcode calls for another read
				boolean hasImmediate = (opcode & 0x8000) != 0;

				// get execution state from opcode 
				// might set register indexees for opcode found
				ProcessorState execState = getExecutionState(temp);

				if(hasImmediate) {
					// return to execution after read
					returnState = execState;
					
					// read another word
					state = ProcessorState.MEM_READ0;
				} else {
					// proceed to execution
					state = execState;
				}

				System.out.println("Got state: " + execState.name());

				break;
			}
			case HLT: {
				System.out.println("Halting...");
				System.exit(0);
			}
			case MEM_READ0: {
				// expect bus address line to be driven by prior state
				bus.readEnable.drive(this, true);

				state = ProcessorState.MEM_READ1;
				
				break;
			}
			case MEM_READ1: {
				bus.readEnable.drive(this, false);

				state = ProcessorState.MEM_READ2;
				
				break;
			}
			case MEM_READ2: {
				temp = bus.dataLine.read();

				// return
				state = returnState;
				
				break;
			}
			default: {
				throw new RuntimeException("Unknown processor state");
			}
		}
	}

	// gets execution state from opcode 
	// might set register indexes for opcode found
	private ProcessorState getExecutionState(char opcode) {
		// instruction format:
		// opcode[15]: has immediate operand
		// opcode[14:6]: instruction type
		// opcode[5:3]: source operand index
		// opcode[2:0]: destination operand index
	
		// isolate opcode string
		char opcodeString = (char) (opcode & 0xFFC0);
		
		// get execution state from map
		ProcessorState executionState = opcodeStrings.get(opcodeString);
		if(executionState == null) {
			throw new RuntimeException("Unkown opcode");
		}

		// get register indexes
		destIndex = opcode & 0x07;
		sourceIndex = (opcode >> 3) & 0x07;

		return executionState;
	}
}
