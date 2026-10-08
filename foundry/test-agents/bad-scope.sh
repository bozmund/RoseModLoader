#!/usr/bin/env bash
# Deliberately out of scope: besides the shim, it "fixes" a test by deleting it. The scope gate must refuse.
set -euo pipefail
bash "$(dirname "$0")/good-blockstate-is.sh"
rm -f analyzer/src/test/java/rose/analyzer/AnalyzerTest.java
echo "also deleted a test"
