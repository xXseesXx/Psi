#!/usr/bin/env python3

from pathlib import Path
import re
import sys


ROOT = Path("./src")


HEADER_RE = re.compile(r"^\s*(/\*.*?\*/)", re.DOTALL)


def get_path_variants(path: Path):
    """
    Generate strings which are deterministically derived from the file path.

    These are replaced with placeholders before comparing headers.
    """

    variants = set()

    # Path relative to ./src
    try:
        relative = path.relative_to(ROOT)
    except ValueError:
        relative = path

    relative_str = str(relative)
    relative_posix = relative.as_posix()

    # Full path variants
    variants.add(relative_str)
    variants.add(relative_posix)

    # Without extension
    relative_no_ext = relative.with_suffix("")
    variants.add(str(relative_no_ext))
    variants.add(relative_no_ext.as_posix())

    # Filename
    variants.add(path.name)
    variants.add(path.stem)

    # Filename with common Java notation
    variants.add(path.name.replace(".java", ""))

    # All individual path components
    parts = list(relative.parts)

    for part in parts:
        variants.add(part)

        if part.endswith(".java"):
            variants.add(part[:-5])

    # Java package notation
    #
    # src/foo/bar/baz/Thing.java
    # -> foo.bar.baz
    #
    if len(parts) > 1:
        package_parts = parts[:-1]

        package = ".".join(package_parts)
        package_slashes = "/".join(package_parts)
        package_backslashes = "\\".join(package_parts)

        variants.add(package)
        variants.add(package_slashes)
        variants.add(package_backslashes)

    # Remove empty / meaningless values
    variants = {
        value
        for value in variants
        if value and len(value) > 1
    }

    return variants


def normalize_header(header: str, path: Path) -> str:
    """
    Normalize a header so that differences caused purely by the file path
    don't cause it to be considered a different header.
    """

    header = (
        header
        .replace("\r\n", "\n")
        .replace("\r", "\n")
    )

    # Replace path-derived values with a generic placeholder.
    variants = get_path_variants(path)

    # Longest first is important.
    #
    # Otherwise, for example:
    #
    # foo/bar/Baz.java
    #
    # could have "foo" replaced before the complete path is considered.
    variants = sorted(
        variants,
        key=len,
        reverse=True
    )

    for value in variants:
        # Escape the value because it may contain regex characters.
        pattern = re.escape(value)

        header = re.sub(
            pattern,
            "<PATH>",
            header
        )

    # Normalize trailing whitespace on each line.
    lines = [
        line.rstrip()
        for line in header.splitlines()
    ]

    # Remove trailing empty lines.
    while lines and not lines[-1]:
        lines.pop()

    return "\n".join(lines)


def main():
    if not ROOT.exists():
        print(f"ERROR: {ROOT} does not exist.")
        sys.exit(1)

    # ONLY .java files, ONLY below ./src
    java_files = sorted(
        path
        for path in ROOT.rglob("*")
        if path.is_file() and path.suffix == ".java"
    )

    if not java_files:
        print("No .java files found in ./src")
        sys.exit(1)

    missing = []
    headers = {}

    for path in java_files:

        try:
            text = path.read_text(encoding="utf-8")

        except UnicodeDecodeError:
            text = path.read_text(encoding="latin-1")

        match = HEADER_RE.match(text)

        if not match:
            missing.append(path)
            continue

        raw_header = match.group(1)

        normalized = normalize_header(
            raw_header,
            path
        )

        headers.setdefault(normalized, []).append(path)

    # ------------------------------------------------------------
    # Summary
    # ------------------------------------------------------------

    print("=" * 70)
    print("JAVA HEADER CHECK")
    print("=" * 70)

    print(f"\nScanned: ./src")
    print(f"Java files: {len(java_files)}")
    print(
        f"Files with headers: "
        f"{sum(len(files) for files in headers.values())}"
    )
    print(f"Files without headers: {len(missing)}")
    print(f"Different header templates: {len(headers)}")

    # ------------------------------------------------------------
    # Missing
    # ------------------------------------------------------------

    if missing:
        print("\n" + "=" * 70)
        print("FILES WITHOUT HEADERS")
        print("=" * 70)

        for path in missing:
            print(f"  {path}")

    # ------------------------------------------------------------
    # Header groups
    # ------------------------------------------------------------

    if len(headers) > 1:
        print("\n" + "=" * 70)
        print("DIFFERENT HEADER TEMPLATES")
        print("=" * 70)

        for number, (header, files) in enumerate(
            headers.items(),
            start=1
        ):
            print(
                f"\n--- Header template {number} "
                f"({len(files)} file(s)) ---"
            )

            print("\nFiles:")

            for path in files:
                print(f"  {path}")

            print("\nNormalized header:")
            print(header)

    # ------------------------------------------------------------
    # Result
    # ------------------------------------------------------------

    print("\n" + "=" * 70)
    print("RESULT")
    print("=" * 70)

    if len(headers) == 1 and not missing:
        print(
            "✓ Every .java file has a header and all headers "
            "match after path-based normalization."
        )
        sys.exit(0)

    if missing:
        print(
            f"✗ {len(missing)} .java file(s) are missing a header."
        )

    if len(headers) > 1:
        print(
            f"✗ {len(headers)} genuinely different "
            f"header templates were found."
        )

    sys.exit(1)


if __name__ == "__main__":
    main()
