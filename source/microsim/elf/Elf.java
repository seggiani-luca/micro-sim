package microsim.elf;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.Arrays;
import microsim.ui.DebugShell;

class ProgramHeader {

  // int p_type;
  int p_offset;
  // int p_vaddr;
  // int p_paddr;
  int p_filesz;
  // int p_memsz;
  int p_flags;
  // int p_align;

  static ProgramHeader parseProgramHeader(Elf elf, FileChannel channel, int i)
    throws IOException {
    ProgramHeader ph = new ProgramHeader();

    // init program header buffer
    ByteBuffer phBuffer = ByteBuffer.allocate(elf.e_phentsize);
    phBuffer.order(ByteOrder.LITTLE_ENDIAN);

    // read from channel into program header buffer
    channel.read(phBuffer);
    phBuffer.flip();

    // ph.p_type = phBuffer.getInt();
    phBuffer.position(phBuffer.position() + 4);

    ph.p_offset = phBuffer.getInt();

    // ph.p_vaddr = phBuffer.getInt();
    // ph.p_paddr = phBuffer.getInt();
    phBuffer.position(phBuffer.position() + 8);

    ph.p_filesz = phBuffer.getInt();

    // ph.p_memsz = phBuffer.getInt();
    phBuffer.position(phBuffer.position() + 4);

    ph.p_flags = phBuffer.getInt();

    // ph.p_align = phBuffer.getInt();
    phBuffer.position(phBuffer.position() + 4);

    return ph;
  }

  boolean isExecutable() {
    return (p_flags & 0x1) == 0x1;
  }

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

  private ProgramHeader() {
  }
}

public class Elf {

  private byte[] e_ident_magic = new byte[4];
  private byte e_ident_class;
  private byte e_ident_data;
  // byte e_ident_version;
  // byte e_ident_osabi;
  // byte e_ident_abiversion;
  private short e_type;
  private short e_machine;
  // int e_version;
  // int e_entry;
  private int e_phoff;
  // int e_shoff;
  // int e_flags;
  // short e_ehsize;
  short e_phentsize;
  private short e_phnum;
  // short e_shentsize;
  // short e_shnum;
  // short e_shstrndx;

  ProgramHeader[] programHeaders;

  private byte[] eprom;

  public byte[] getEPROM() {
    return eprom;
  }

  public static Elf readELF(String path, boolean debugMode) throws IOException {
    Elf elf = new Elf();

    // open file channel
    FileChannel channel = FileChannel.open(Paths.get(path), StandardOpenOption.READ);

    // parse header
    parseHeader(elf, channel);

    // check header
    if (debugMode) {
      elf.printHeader();
    }
    elf.checkHeader();

    // prepare to read program header array
    channel.position(elf.e_phoff);
    elf.programHeaders = new ProgramHeader[elf.e_phnum];

    // iterate through program headers
    for (int i = 0; i < elf.e_phnum; i++) {
      elf.programHeaders[i] = ProgramHeader.parseProgramHeader(elf, channel, i);

      // print program header
      if (debugMode) {
        elf.programHeaders[i].printProgramHeader();
      }
    }

    // find first executable program header
    int execIndex = -1;

    // iterate for index
    for (int i = 0; i < elf.e_phnum; i++) {
      // if it's executable, flag and break
      if (elf.programHeaders[i].isExecutable()) {
        execIndex = i;
        break;
      }
    }

    // if no executable program header is found quit
    if (execIndex == -1) {
      throw new IOException("ELF doesn't have an executable program header");
    }

    // get executable segment location
    int epromOffset = elf.programHeaders[execIndex].p_offset;
    int epromSize = elf.programHeaders[execIndex].p_filesz;

    // move channel to segment
    channel.position(epromOffset);

    // use ByteBuffer wrapping EPROM array
    elf.eprom = new byte[epromSize];
    ByteBuffer segmentBuffer = ByteBuffer.wrap(elf.eprom);

    // read into buffer
    channel.read(segmentBuffer);

    // check
    return elf;
  }

  private static void parseHeader(Elf elf, FileChannel channel) throws IOException {
    // init header buffer
    ByteBuffer hBuffer = ByteBuffer.allocate(52);
    hBuffer.order(ByteOrder.LITTLE_ENDIAN);

    // read from channel into header buffer
    channel.read(hBuffer);
    hBuffer.flip();

    // read from buffer into header fields
    hBuffer.get(elf.e_ident_magic);
    elf.e_ident_class = hBuffer.get();
    elf.e_ident_data = hBuffer.get();

    // elf.e_ident_version = hBuffer.get();
    // elf.e_ident_osabi = hBuffer.get();
    // elf.e_ident_abiversion = hBuffer.get();
    hBuffer.position(hBuffer.position() + 3);

    // skip padding
    hBuffer.position(hBuffer.position() + 7);

    elf.e_type = hBuffer.getShort();
    elf.e_machine = hBuffer.getShort();

    // elf.e_version = hBuffer.getInt();
    // elf.e_entry = hBuffer.getInt();
    hBuffer.position(hBuffer.position() + 8);

    elf.e_phoff = hBuffer.getInt();

    // elf.e_shoff = hBuffer.getInt();
    // elf.e_flags = hBuffer.getInt();
    // elf.e_ehsize = hBuffer.getShort();
    hBuffer.position(hBuffer.position() + 10);

    elf.e_phentsize = hBuffer.getShort();
    elf.e_phnum = hBuffer.getShort();

    // elf.e_shentsize = hBuffer.getShort();
    // elf.e_shnum = hBuffer.getShort();
    // elf.e_shstrndx = hBuffer.getShort();
    hBuffer.position(hBuffer.position() + 6);
  }

  private void printHeader() {
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

  private void checkHeader() throws IOException {
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

  private Elf() {
  }
}
