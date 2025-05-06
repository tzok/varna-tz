#!/usr/bin/env python3

import argparse
import json
import sys


def parse_dot_bracket(sequence, dot_bracket_string):
    """
    Parses sequence and dot-bracket string to create a structure data dictionary.

    Args:
        sequence (str): The RNA sequence string.
        dot_bracket_string (str): The dot-bracket notation string.

    Returns:
        dict: A dictionary with "nucleotides" and "basePairs" lists.

    Raises:
        ValueError: If sequence and dot-bracket string have different lengths,
                    or if dot-bracket string is malformed.
    """
    if len(sequence) != len(dot_bracket_string):
        raise ValueError("Sequence and dot-bracket string must have the same length.")

    nucleotides = []
    for i, char_code in enumerate(sequence):
        nucleotides.append({"index": i + 1, "name": char_code})

    base_pairs = []
    stack = []
    opening_brackets = "([{<"
    closing_brackets = ")]}>"
    bracket_map = {")": "(", "]": "[", "}": "{", ">": "<"}

    for i, char_code in enumerate(dot_bracket_string):
        index = i + 1  # 1-based index
        if char_code in opening_brackets:
            stack.append((index, char_code))
        elif char_code in closing_brackets:
            if not stack:
                raise ValueError(f"Unmatched closing bracket '{char_code}' at position {index}.")
            nt1_index, open_bracket_char = stack.pop()
            if bracket_map[char_code] != open_bracket_char:
                raise ValueError(
                    f"Mismatched closing bracket '{char_code}' for opening bracket "
                    f"'{open_bracket_char}' at position {nt1_index} and {index}."
                )
            nt2_index = index
            # Ensure nt1 is always less than nt2, common convention
            base_pairs.append({
                "nt1": min(nt1_index, nt2_index),
                "nt2": max(nt1_index, nt2_index),
                "edge5": "WC",
                "edge3": "WC",
                "stericity": "CIS",
                "canonical": True,
            })
        elif char_code == ".":
            pass  # Unpaired nucleotide
        else:
            raise ValueError(
                f"Invalid character '{char_code}' in dot-bracket string at position {index}."
            )

    if stack:
        unmatched_positions = [item[0] for item in stack]
        raise ValueError(f"Unmatched opening brackets at positions: {unmatched_positions}.")

    # Sort base pairs by the first nucleotide index for consistent output
    base_pairs.sort(key=lambda bp: bp["nt1"])

    return {"nucleotides": nucleotides, "basePairs": base_pairs}


def main():
    """
    Main function to parse arguments and print structure JSON.
    """
    parser = argparse.ArgumentParser(
        description="Generate a JSON representation of RNA secondary structure from sequence and dot-bracket notation."
    )
    parser.add_argument(
        "sequence", type=str, help="The RNA sequence (e.g., 'GGAAACC')."
    )
    parser.add_argument(
        "dotbracket", type=str, help="The dot-bracket notation (e.g., '((...))')."
    )

    args = parser.parse_args()

    try:
        structure_data = parse_dot_bracket(args.sequence, args.dotbracket)
        print(json.dumps(structure_data, indent=2))
    except ValueError as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
