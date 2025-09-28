package microsim.component;

public class TSLine<T> implements RunnableComponent {
	private RunnableComponent driver;

	// data is buffered until update
	private T data;
	private T bufferedData;

	@Override
	public void step() {
		data = bufferedData;
	}

	public void drive(RunnableComponent driver, T data) {
		if(driver == null) {
			throw new RuntimeException("Null driver cannot drive TSLine");
		}

		// trying to drive free line
		if(this.driver == null) {
			this.driver = driver;
			this.bufferedData = data;
			
			return;
		}

		// trying to drive already driven line
		if(driver != this.driver) {
			throw new RuntimeException(
				driver.getClass().getName() + 
				" trying to drive TSLine already driven by " + 
				this.driver.getClass().getName()
			);
		}

		// already driving line
		this.bufferedData = data;
	}

	public void release(RunnableComponent driver) {
		if(driver == null) {
			throw new RuntimeException("Null driver cannot release TSLine");
		}

		// trying to release line not owned
		if(driver != this.driver) {
			throw new RuntimeException(
				driver.getClass().getName() + 
				" trying to release TSLine already driven by " + 
				this.driver.getClass().getName()
			);
		}

		// release line
		this.driver = null;
		this.bufferedData = null;
	}

	public T read() {
		return data;
	}
}
