"""Enforce the coursework package layout for all project Java sources."""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
expected = {"builder", "factory", "strategy", "bridge", "observer", "decorator"}
base = root / "src/main/java/com/restaurant"
assert {p.name for p in base.iterdir() if p.is_dir()} == expected, "Exactly six pattern packages required"
assert {p.name for p in base.glob("*.java")} == {"Main.java"}, "Only Main may be outside the six packages"
for source_root in [root / "src/main/java", root / "src/test/java"]:
    for path in source_root.rglob("*.java"):
        package = re.search(r"^package\s+([\w.]+);", path.read_text(), re.M)
        assert package, f"Missing package: {path}"
        actual = ".".join(path.relative_to(source_root).parts[:-1])
        assert package.group(1) == actual, f"Package/path mismatch: {path}"
        if path.name != "Main.java":
            assert actual.split(".")[2] in expected, f"Unexpected package: {path}"
print("PASS: six pattern packages, matching subfolders, and one Main entry point")
