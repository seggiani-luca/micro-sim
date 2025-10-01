package microsim.simulation.type;

/**
 * 32 bit unsigned integer. Implemented as mutable, internally basically is an int.
 */
public class UInt32 {

  /**
   * Internal int.
   */
  private int val;

  /**
   * Creates a new UInt32 from an int.
   *
   * @param val initial value
   */
  public UInt32(int val) {
    this.val = val;
  }

  /**
   * Gets value as an int.
   *
   * @return value
   */
  public int get() {
    return val;
  }

  /**
   * Sets value from int.
   *
   * @param val value to set to
   */
  public void set(int val) {
    this.val = val;
  }

  /**
   * Adds an UInt32 to this one.
   *
   * @param other value to add
   */
  public void add(UInt32 other) {
    this.val = this.val + other.val;
  }

  /**
   * Subtracts an UInt32 from this one.
   *
   * @param other value to subtract
   */
  public void sub(UInt32 other) {
    this.val = this.val - other.val;
  }

  /**
   * Uses an UInt32 to shift this one left.
   *
   * @param other bits to shift
   */
  public void shl(UInt32 other) {
    this.val = this.val << other.val;
  }

  /**
   * Uses an UInt32 to shift this one right, logically (inserting zeroes).
   *
   * @param other bits to shift
   */
  public void shr(UInt32 other) {
    this.val = this.val >>> other.val;
  }

  /**
   * Uses an UInt32 to shift this one right, arithmetically (sign extending).
   *
   * @param other bits to shift
   */
  public void sar(UInt32 other) {
    this.val = this.val >> other.val;
  }

  /**
   * Converts to a string.
   *
   * @return converted string
   */
  public String toString() {
    return String.format("%08x", val);
  }
}
