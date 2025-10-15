package microsim.elf;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.Arrays;
import microsim.ui.DebugShell;

/**
 * Reads and represents an ELF header. Only values significant to error checking and program header
 * access are kept.
 */
class ElfHeader {

  /**
   * Magic numbers for ELF file. Should be 0x7f, 0x45, 0x4c, 0x46.
   */
  byte[] e_ident_magic = new byte[4];

  /**
   * Specifies 32 or 64 bit target architecture (want 32 bit).
   * <ul>
   * <li>1: 32 bit</li>
   * <li>2: 64 bit</li>
   * </ul>
   */
  byte e_ident_class;

  /**
   * Specifies endianess (want little endian).
   * <ul>
   * <li>1: little endian</li>
   * <li>2: big endian</li>
   * </ul>
   */
  byte e_ident_data;

  // byte e_ident_version;
  // byte e_ident_osabi;
  // byte e_ident_abiversion;
  /**
   * Specifies ELF type (want executable).
   * <ul>
   * <li>0x01: relocatable</li>
   * <li>0x01: executable</li>
   * <li>0x02: executable</li>
   * <li>0x03: dynamic</li>
   * <li>0x04: core</li>
   * </ul>
   * otherwise unknown.
   */
  short e_type;

  /**
   * Specifies target architecture (want 0xf3, meaning RISC-V).
   */
  short e_machine;

  // int e_version;
  // int e_entry;
  /**
   * Program header table offset.
   */
  int e_phoff;

  // int e_shoff;
  // int e_flags;
  // short e_ehsize;
  /**
   * Program header size.
   */
  short e_phentsize;

  /**
   * Number of program headers.
   */
  short e_phnum;

  // short e_shentsize;
  // short e_shnum;
  // short e_shstrndx;
  /**
   * Creates an ELF header from a file channel. Channel is expected to be initialized to beginning
   * of file.
   *
   * @param channel ELF file channel
   * @throws IOException when failing to read from channel
   */
  public ElfHeader(FileChannel channel) throws IOException {
    // init header buffer
    ByteBuffer hBuffer = ByteBuffer.allocate(52);
    hBuffer.order(ByteOrder.LITTLE_ENDIAN);

    // read from channel into header buffer
    channel.read(hBuffer);
    hBuffer.flip();

    // read from buffer into header fields
    hBuffer.get(e_ident_magic);
    e_ident_class = hBuffer.get();
    e_ident_data = hBuffer.get();

    // elf.e_ident_version = hBuffer.get();
    // elf.e_ident_osabi = hBuffer.get();
    // elf.e_ident_abiversion = hBuffer.get();
    hBuffer.position(hBuffer.position() + 3);

    // skip padding
    hBuffer.position(hBuffer.position() + 7);

    e_type = hBuffer.getShort();
    e_machine = hBuffer.getShort();

    // elf.e_version = hBuffer.getInt();
    // elf.e_entry = hBuffer.getInt();
    hBuffer.position(hBuffer.position() + 8);

    e_phoff = hBuffer.getInt();

    // elf.e_shoff = hBuffer.getInt();
    // elf.e_flags = hBuffer.getInt();
    // elf.e_ehsize = hBuffer.getShort();
    hBuffer.position(hBuffer.position() + 10);

    e_phentsize = hBuffer.getShort();
    e_phnum = hBuffer.getShort();

    // elf.e_shentsize = hBuffer.getShort();
    // elf.e_shnum = hBuffer.getShort();
    // elf.e_shstrndx = hBuffer.getShort();
    hBuffer.position(hBuffer.position() + 6);
  }

  /**
   * Prints header information.
   */
  public void printHeader() {
    System.out.println("Parsed ELF header:");

    System.out.print("\tClass: ");
    switch (e_ident_class) {
      case 1 ->
        System.out.println("32 bit");
      case 2 ->
        System.out.println("64 bit");
      default ->
        System.out.println("unknown");
    }

    System.out.print("\tEndianess: ");
    switch (e_ident_data) {
      case 1 ->
        System.out.println("little-endian");
      case 2 ->
        System.out.println("big-endian");
      default ->
        System.out.println("unknown");
    }

    System.out.print("\tType: ");
    switch (e_type) {
      case 0x01 ->
        System.out.println("relocatable");
      case 0x02 ->
        System.out.println("executable");
      case 0x03 ->
        System.out.println("dynamic");
      case 0x04 ->
        System.out.println("core");
      default ->
        System.out.println("unknown");
    }

    System.out.print("\tMachine: ");
    switch (e_machine) {
      case 0xf3 ->
        System.out.println("RISC-V");
      default ->
        System.out.println("unknown");
    }

    System.out.println("\nProgram headers:");
  }

  /**
   * Checks if header values match expected ones.
   *
   * @throws IOException if a mismatch is found
   */
  void checkHeader() throws IOException {
    byte[] magic = {0x7f, 0x45, 0x4c, 0x46};
    if (!Arrays.equals(e_ident_magic, magic)) {
      throw new IOException("Not a valid ELF file");
    }

    if (e_ident_class != 1) {
      throw new IOException("Not 32 bit");
    }

    if (e_ident_data != 1) {
      throw new IOException("Not little-endian");
    }

    if (e_type != 0x02) {
      throw new IOException("Not an executable ELF");
    }

    if (e_machine != 0xf3) {
      throw new IOException("Not a RISC-V ELF");
    }
  }
}

/**
 * Reads and represents an ELF program header. Only values significant to error checking and segment
 * access are kept.
 */
class ProgramHeader {

  // int p_type;
  /**
   * Offset of segment in ELF.
   */
  int p_offset;

  // int p_vaddr;
  // int p_paddr;
  /**
   * Size of segment in ELF.
   */
  int p_filesz;

  // int p_memsz;
  /**
   * Flags of segment (want executable).
   * <ul>
   * <li>0x01: executable</li>
   * <li>0x02: writable</li>
   * <li>0x04: readable</li>
   * </ul>
   */
  int p_flags;

  // int p_align;
  /**
   * Creates an ELF program header from a file channel. Channel is expected to be initialized to
   * beginning of program header.
   *
   * @param channel ELF file channel
   * @param size size of program header (e_phentsize in ELF header)
   * @throws IOException when failing to read from channel
   */
  public ProgramHeader(FileChannel channel, int size) throws IOException {
    // init program header buffer
    ByteBuffer phBuffer = ByteBuffer.allocate(size);
    phBuffer.order(ByteOrder.LITTLE_ENDIAN);

    // read from channel into program header buffer
    channel.read(phBuffer);
    phBuffer.flip();

    // ph.p_type = phBuffer.getInt();
    phBuffer.position(phBuffer.position() + 4);

    p_offset = phBuffer.getInt();

    // ph.p_vaddr = phBuffer.getInt();
    // ph.p_paddr = phBuffer.getInt();
    phBuffer.position(phBuffer.position() + 8);

    p_filesz = phBuffer.getInt();

    // ph.p_memsz = phBuffer.getInt();
    phBuffer.position(phBuffer.position() + 4);

    p_flags = phBuffer.getInt();

    // ph.p_align = phBuffer.getInt();
    phBuffer.position(phBuffer.position() + 4);
  }

  /**
   * Prints program header information.
   */
  void printProgramHeader() {
    System.out.println("\tOffset:\t" + DebugShell.int32ToString(p_offset));
    System.out.println("\tSize:\t" + DebugShell.int32ToString(p_filesz));

    System.out.print("\tFlags:\t");
    if ((p_flags & 0x1) == 0x1) {
      System.out.print("E ");
    }
    if ((p_flags & 0x2) == 0x2) {
      System.out.print("W ");
    }
    if ((p_flags & 0x4) == 0x4) {
      System.out.print("R ");
    }
    System.out.println("\n");
  }
}

/**
 * Reads an ELF header and its program headers, used to load EPROM data for simulation.
 */
public class Elf {

  /**
   * ELF header.
   */
  private ElfHeader header;

  /**
   * Array of ELF program headers.
   */
  private ProgramHeader[] programHeaders;

  /**
   * EPROM data as byte array.
   */
  private byte[] eprom;

  /**
   * Returns EPROM data
   *
   * @return EPROM data byte array
   */
  public byte[] getEPROM() {
    return eprom;
  }

  /**
   * Parses ELF header and program headers, and sets up EPROM byte array.
   *
   * @param path path of the ELF file
   * @param debugMode should debug info get printed?
   * @throws IOException if headers are invalid or fails reading from file
   */
  public Elf(String path, boolean debugMode) throws IOException {
    try (FileChannel channel = FileChannel.open(Paths.get(path), StandardOpenOption.READ)) {
      // parse header
      header = new ElfHeader(channel);

      // check header
      if (debugMode) {
        header.printHeader();
      }
      header.checkHeader();

      // prepare to read program header array
      channel.position(header.e_phoff);
      programHeaders = new ProgramHeader[header.e_phnum];

      // iterate through program headers
      for (int i = 0; i < header.e_phnum; i++) {
        programHeaders[i] = new ProgramHeader(channel, header.e_phentsize);

        // print program header
        if (debugMode) {
          programHeaders[i].printProgramHeader();
        }
      }

      // expected program headers are:
      // 1) riscv attributes (ignored)
      // 2) text + rodata
      // 3) data (to be loaded in RAM by _start routine)
      // 4) bss (ignored)
      // 5) video (ignored)
      // 6) gnu stack (ignored)
      if (programHeaders.length < 3) {
        throw new IOException("Not enough program headers in ELF");
      }

      // get segment location
      int textOffset = programHeaders[1].p_offset;
      int textSize = programHeaders[1].p_filesz;
      int dataOffset = programHeaders[2].p_offset;
      int dataSize = programHeaders[2].p_filesz;

      // init EPROM aray
      eprom = new byte[textSize + dataSize];

      // read segment (2)
      channel.position(textOffset);
      ByteBuffer textBuffer = ByteBuffer.wrap(eprom, 0, textSize);
      channel.read(textBuffer);

      // read segment (3)
      channel.position(dataOffset);
      ByteBuffer dataBuffer = ByteBuffer.wrap(eprom, textSize, dataSize);
      channel.read(dataBuffer);

    } // file channel is closed here
  }
}
