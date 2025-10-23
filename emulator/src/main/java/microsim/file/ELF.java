package microsim.file;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.*;
import net.fornwall.jelf.*;

/**
 * Uses the jelf library to parse an ELF and read the correct segments into an EPROM array.
 */
public class ELF {

  /**
   * Checks if the ELF header is valid, otherwise throws an exception.
   *
   * @param elf ELF header
   * @throws IOException if header is invalid
   */
  private static void checkElf(ElfFile elf) throws IOException {
    if (elf.ei_class != 1) {
      throw new IOException("Not 32 bit");
    }

    if (elf.ei_data != 1) {
      throw new IOException("Not little-endian");
    }

    if (elf.e_type != 0x02) {
      throw new IOException("Not an executable ELF");
    }

    if (elf.e_machine != 0xf3) {
      throw new IOException("Not a RISC-V ELF");
    }

    if (elf.e_phnum < 2) {
      throw new IOException("Not enough ELF program headers (expected at least 3)");
    }
  }

  /**
   * Checks if the rodata ELF segment is valid, otherwise throws an exception.
   *
   * @param data rodata ELF segment
   * @throws IOException if segment is invalid
   */
  private static void checkRodata(ElfSegment rodata) throws IOException {
    if (!rodata.isExecutable()) {
      throw new IOException("Expected rodata section (1) is not executable");
    }
    if (!rodata.isReadable()) {
      throw new IOException("Expected rodata section (1) is not readable");
    }
  }

  /**
   * Checks if the data ELF segment is valid, otherwise throws an exception.
   *
   * @param data data ELF segment
   * @throws IOException if segment is invalid
   */
  private static void checkData(ElfSegment data) throws IOException {
    if (!data.isWriteable()) {
      throw new IOException("Expected data section (2) is not writable");
    }
    if (!data.isReadable()) {
      throw new IOException("Expected rodata section (2) is not readable");
    }
  }

  /**
   * Takes a path string and returns the corresponding EPROM byte array.
   *
   * @param path path of EPROM data
   * @return EPROM data array
   * @throws IOException if fails to open file or parse ELF headers
   */
  public static byte[] readEPROM(String path) throws IOException {
    // open ELF
    File file = new File(path);

    ElfFile elf = null;
    try {
      elf = ElfFile.from(file);
    } catch (ElfException e) {
      throw new IOException("Error loading ELF. " + e.getMessage());
    }

    // check if header is as expected
    checkElf(elf);

    // expected program headers are:
    // 0) riscv attributes (ignored)
    // 1) text + rodata
    // 2) data (to be loaded in RAM by _start routine)
    // 3) bss (ignored)
    // 4) video (ignored)
    // 5) gnu stack (ignored)
    ElfSegment rodata = elf.getProgramHeader(1);
    ElfSegment data = elf.getProgramHeader(2);

    // check if program headers are as expected
    checkRodata(rodata);
    checkData(data);

    // get rodata segment info
    int rodataBeg = (int) rodata.p_offset;
    int rodataSize = (int) rodata.p_filesz;

    // get data segment info
    int dataBeg = (int) data.p_offset;
    int dataSize = (int) data.p_filesz;

    byte[] eprom = new byte[rodataSize + dataSize];
    // actually get segments
    try (FileChannel channel = FileChannel.open(Paths.get(path), StandardOpenOption.READ)) {
      // read rodata segment
      channel.position(rodataBeg);
      ByteBuffer dataBuffer = ByteBuffer.wrap(eprom, 0, rodataSize);
      channel.read(dataBuffer);

      // read data segment
      channel.position(dataBeg);
      ByteBuffer rodataBuffer = ByteBuffer.wrap(eprom, rodataSize, dataSize);
      channel.read(rodataBuffer);
    }

    return eprom;
  }
}
