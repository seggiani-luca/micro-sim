package microsim.component;

public class TSLineBool extends TSLine<Boolean> {
	@Override
	public Boolean read() {
		return data == null ? false : data; 
	}
}
