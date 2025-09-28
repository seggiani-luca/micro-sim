import sys
import re


# instruction format:
# opcode[15]: has immediate operand
# opcode[14:6]: instruction type
# opcode[5:3]: source operand index
# opcode[2:0]: destination operand index
INST_DICT = {
    # movement
    "MOV": {
        "opcode": 0x0000,
        "immediate": "left",
        "operands": 2
    },
    "LOD": {
        "opcode": 0x0040,
        "immediate": "left",
        "operands": 2
    },
    "STO": {
        "opcode": 0x0080,
        "immediate": "right",
        "operands": 2
    },

    # arithmetic
    "ADD": {
        "opcode": 0x4000,
        "immediate": "left",
        "operands": 2
    },
    "SUB": {
        "opcode": 0x4040,
        "immediate": "left",
        "operands": 2
    },
    "CMP": {
        "opcode": 0x4080,
        "immediate": "left",
        "operands": 2
    },
    "INC": {
        "opcode": 0x40c0,
        "immediate": "none",
        "operands": 1
    },
    "DEC": {
        "opcode": 0x4100,
        "immediate": "none",
        "operands": 1
    },

    # logic
    "AND": {
        "opcode": 0x2000,
        "immediate": "left",
        "operands": 2
    },
    "OR": {
        "opcode": 0x2040,
        "immediate": "left",
        "operands": 2
    },
    "NOT": {
        "opcode": 0x2080,
        "immediate": "none",
        "operands": 1
    },

    # utility
    "SHL": {
        "opcode": 0x6000,
        "immediate": "left",
        "operands": 2
    },
    "SHR": {
        "opcode": 0x6040,
        "immediate": "left",
        "operands": 2
    },

    # stack
    "PUSH": {
        "opcode": 0x1000,
        "immediate": "none",
        "operands": 1
    },
    "POP": {
        "opcode": 0x1040,
        "immediate": "none",
        "operands": 1
    },
    "CALL": {
        "opcode": 0x9080,
        "immediate": "forced",
        "operands": 1
    },
    "RET": {
        "opcode": 0x10c0,
        "immediate": "none",
        "operands": 0
    },

    # jumps
    "JMP": {
        "opcode": 0x0800,
        "immediate": "yes",
        "operands": 1
    },
    "JO": {
        "opcode": 0x0840,
        "immediate": "yes",
        "operands": 1
    },
    "JS": {
        "opcode": 0x08c0,
        "immediate": "yes",
        "operands": 1
    },
    "JZ": {
        "opcode": 0x0940,
        "immediate": "yes",
        "operands": 1
    },
    "JC": {
        "opcode": 0x09c0,
        "immediate": "yes",
        "operands": 1
    },
}

# 0-3: general registers
# 4: ip
# 5: sp
REG_DICT = {
    "%A": {
        "index": 0
    },
    "%B": {
        "index": 1
    },
    "%C": {
        "index": 2
    },
    "%D": {
        "index": 3
    },
    "%IP": {
        "index": 4
    },
    "%SP": {
        "index": 5
    },
}


class AssemblerError(Exception):
    pass


def split_bytes(num):
    num = int(num)

    hi = (num >> 8) & 0xff
    lo = num & 0xff

    return f"{hi:02X} {lo:02X}"


def get_reg_index(token):
    mnem = token.upper()

    if mnem not in REG_DICT:
        raise AssemblerError(f"unkown register {mnem}")

    return int(REG_DICT[mnem]["index"])


def is_immediate(token):
    return token[0] == "$" or token[0] == "_"


def get_instruction(tokens):
    mnem = tokens[0].upper()

    if mnem not in INST_DICT:
        return None, None

    inst = INST_DICT[mnem]

    immediate = None

    opcode = inst["opcode"]

    if inst["operands"] == 0:
        # 0 operands
        pass

    elif inst["operands"] == 1:
        # 1 operand
        if inst["immediate"] == "forced":
            if not is_immediate(tokens[1]):
                raise AssemblerError(f"instruction {mnem} requires immediate operand")
            immediate = tokens[1][1:]
            reg_index = 0
        elif inst["immediate"] == "yes" and is_immediate(tokens[1]):
            immediate = tokens[1][1:]
            reg_index = 0
        else:
            reg_index = get_reg_index(tokens[1])

        opcode |= reg_index
        if immediate is not None:
            opcode |= 0x8000

    else:
        # 2 operands
        if inst["immediate"] == "left" and is_immediate(tokens[1]):
            immediate = tokens[1][1:]

            reg_index_l = 0
            reg_index_r = get_reg_index(tokens[2])

        elif inst["immediate"] == "right" and is_immediate(tokens[2]):
            immediate = tokens[2][1:]

            reg_index_l = get_reg_index(tokens[1])
            reg_index_r = 0
        else:
            reg_index_l = get_reg_index(tokens[1])
            reg_index_r = get_reg_index(tokens[2])

        opcode |= (reg_index_l << 3) | reg_index_r
        if immediate is not None:
            opcode |= 0x8000

    return opcode, immediate


def do_first_pass(lines):
    bytes = 0

    label_dict = {}

    for tokens in lines:
        if not tokens:
            continue

        inst, immediate = get_instruction(tokens)
        if inst is not None:
            # instruction
            bytes += 1
            if immediate is not None:
                bytes += 1

            continue

        if re.match(r"^_.*:$", tokens[0]):
            # label
            label = tokens[0][0:-1]

            if len(tokens) != 1:
                raise AssemblerError(f"trash after label {label}")

            bytes_hi = (bytes >> 8) & 0xff
            bytes_lo = bytes & 0xff

            resolved_label = f"${bytes_hi:02X}{bytes_lo:02X}"
            label_dict[label] = resolved_label

            continue

        raise AssemblerError(f"invalid token {tokens[0]}")

    return label_dict


def do_second_pass(lines, dest, label_dict, orig_lines):
    for ln, tokens in enumerate(lines):
        if not tokens:
            continue

        inst, immediate = get_instruction(tokens)
        if inst is not None:
            # instruction
            inst = split_bytes(inst)

            orig_line = orig_lines[ln]
            dest.write(f"{inst} // {orig_line.strip()}\n")

            if immediate is not None:
                # bit hacky, if this fails it didn't resolve to immediate
                try:
                    immediate = split_bytes(immediate)
                except ValueError:
                    raise AssemblerError(f"couldn't resolve label _{immediate}")

                dest.write(f"{immediate}\n")
            continue

        if re.match(r"^_.*:$", tokens[0]):
            # label
            label = tokens[0][0:-1]
            byte = label_dict[label]

            dest.write(f"// {label} ({byte})\n")
            continue

        raise AssemblerError(f"invalid token {tokens[0]}")


def get_lines(src):
    lines = []

    for line in src:
        # get rid of comments
        line = line.split("//", 1)[0]

        tokens = line.strip().replace(",", " ").split()
        lines.append(tokens)

    return lines


def resolve_labels(lines, label_dict):
    for tokens in lines:
        for i, token in enumerate(tokens):
            if token in label_dict:
                tokens[i] = label_dict[token]


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Please specify arguments as <source_file> <destination_file>")
        sys.exit()

    source_path = sys.argv[1]
    destination_path = sys.argv[2]

    with open(source_path, "r") as src, open(destination_path, "w") as dest:
        try:
            lines = get_lines(src)

            print("Got tokens:")
            print(lines)

            label_dict = do_first_pass(lines)
            print("Built label dictionary:")
            print(label_dict)

            resolve_labels(lines, label_dict)
            print("Got tokens after resolution:")
            print(lines)

            # for debug
            src.seek(0)
            orig_lines = src.read().splitlines()

            do_second_pass(lines, dest, label_dict, orig_lines)
            print("Written to file " + destination_path)

        except AssemblerError as e:
            print(f"Assembler error: {e}")
        except IndexError:
            print(f"Assembler error: ran out of tokens")
