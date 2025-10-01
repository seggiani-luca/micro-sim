package microsim.simulation.type;

/**
 * 8 bit unsigned integer. Implemented as mutable, internally keeps an int.
 */
public class UInt8 {

  /**
   * Internal int.
   */
  private int val;

  /**
   * Creates a new UInt8 from an int.
   *
   * @param val initial value
   */
  public UInt8(int val) {
    this.val = val & 0xff;
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
    this.val = val & 0xff;
  }

  /**
   * Adds an UInt8 to this one.
   *
   * @param other value to add
   */
  public void add(UInt8 other) {
    this.val = (this.val + other.val) & 0xff;
  }

  /**
   * Subtracts an UInt8 from this one.
   *
   * @param other value to subtract
   */
  public void sub(UInt8 other) {
    this.val = (this.val - other.val) & 0xff;
  }

  /**
   * Uses an UInt8 to shift this one left.
   *
   * @param other bits to shift
   */
  public void shl(UInt8 other) {
    this.val = (this.val << other.val) & 0xff;
  }

  /**
   * Uses an UInt8 to shift this one right, logically (inserting zeroes).
   *
   * @param other bits to shift
   */
  public void shr(UInt8 other) {
    this.val = (this.val >>> other.val) & 0xff;
  }

  /**
   * Uses an UInt8 to shift this one right, arithmetically (sign extending).
   *
   * @param other bits to shift
   */
  public void sar(UInt8 other) {
    this.val = (this.val >> other.val) & 0xff;
  }

  /**
   * Converts to a string.
   *
   * @return converted string
   */
  public String toString() {
    return String.format("%02x", val);
  }
}
