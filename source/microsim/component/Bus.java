package microsim.component;

public class Bus implements RunnableComponent {
	// address and data lines
	public TSLine<Character> addressLine; // 16 bit addressing
																				// (operations must be aligned when using 16 bit registers)
	public TSLine<Character> dataLine; // 16 bit data line

	// control lines
	public TSLineBool readEnable; // high active
	public TSLineBool writeEnable;

	public TSLineBool targetSpace; // low is memory 

	public Bus() {
		addressLine = new TSLine<Character>();
		dataLine = new TSLine<Character>();
		
		readEnable = new TSLineBool();
		writeEnable = new TSLineBool();
		
		targetSpace = new TSLineBool();
	}

	public void step() {
		// stepping bus means stepping lines
		addressLine.step();
		dataLine.step();
		
		readEnable.step();
		writeEnable.step();
		
		targetSpace.step();
	}
}
