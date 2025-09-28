package microsim.component;

public class Bus implements RunnableComponent {
	// address and data lines
	public TSLine<Character> addressLine; // 16 bit addressing
	public TSLine<Character> dataLine; // 16 bit data line

	// control lines
	public TSLine<Boolean> readEnable; // high active
	public TSLine<Boolean> writeEnable;

	public TSLine<Boolean> targetSpace; // low is memory 

	public Bus() {
		addressLine = new TSLine<Character>();
		dataLine = new TSLine<Character>();
		
		readEnable = new TSLine<Boolean>();
		writeEnable = new TSLine<Boolean>();
		
		targetSpace = new TSLine<Boolean>();
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
