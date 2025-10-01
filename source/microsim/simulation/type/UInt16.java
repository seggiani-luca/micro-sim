package microsim.simulation.type;

/**
 * 16 bit unsigned integer. Implemented as mutable, internally keeps an int.
 */
public class UInt16 {

  /**
   * Internal int.
   */
  private int val;

  /**
   * Creates a new UInt16 from an int.
   *
   * @param val initial value
   */
  public UInt16(int val) {
    this.val = val & 0xffff;
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
    this.val = val & 0xffff;
  }

  /**
   * Adds an UInt16 to this one.
   *
   * @param other value to add
   */
  public void add(UInt16 other) {
    this.val = (this.val + other.val) & 0xffff;
  }

  /**
   * Subtracts an UInt16 from this one.
   *
   * @param other value to subtract
   */
  public void sub(UInt16 other) {
    this.val = (this.val - other.val) & 0xffff;
  }

  /**
   * Uses an UInt16 to shift this one left.
   *
   * @param other bits to shift
   */
  public void shl(UInt16 other) {
    this.val = (this.val << other.val) & 0xffff;
  }

  /**
   * Uses an UInt16 to shift this one right, logically (inserting zeroes).
   *
   * @param other bits to shift
   */
  public void shr(UInt16 other) {
    this.val = (this.val >>> other.val) & 0xffff;
  }

  /**
   * Uses an UInt16 to shift this one right, arithmetically (sign extending).
   *
   * @param other bits to shift
   */
  public void sar(UInt16 other) {
    this.val = (this.val >> other.val) & 0xffff;
  }

  /**
   * Converts to a string.
   *
   * @return converted string
   */
  public String toString() {
    return String.format("%04x", val);
  }
}
