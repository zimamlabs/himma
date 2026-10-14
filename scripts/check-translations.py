#!/usr/bin/env python3
"""Fail when the Arabic strings drift from the English ones: same keys and the same placeholders."""
import re, sys
from xml.etree import ElementTree as ET
pairs = [("composeApp/src/commonMain/composeResources/values/strings.xml", "composeApp/src/commonMain/composeResources/values-ar/strings.xml"),
         ("composeApp/src/androidMain/res/values/strings.xml", "composeApp/src/androidMain/res/values-ar/strings.xml")]
def load(path):
    return {(el.tag, el.get("name")): "".join(el.itertext()) for el in ET.parse(path).getroot()}
ph = lambda t: sorted(re.findall(r"%\d\$s", t))
problems = []
for en_path, ar_path in pairs:
    en, ar = load(en_path), load(ar_path)
    problems += [f"missing in Arabic: {k[1]}" for k in en if k not in ar] + [f"only in Arabic: {k[1]}" for k in ar if k not in en]
    problems += [f"placeholders differ: {k[1]}" for k in en if k in ar and k[0] == "string" and ph(en[k]) != ph(ar[k])]
print("\n".join(problems) or "ok: translations match")
sys.exit(1 if problems else 0)
