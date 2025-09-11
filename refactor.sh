#!/usr/bin/env bash
set -euo pipefail

echo "===> Step 0: Check current working directory"
if [ ! -f "pom.xml" ]; then
  echo "ERROR: Please run this script from the project root (where pom.xml is located)."
  exit 1
fi

echo "===> Step 1: Make a git backup (if repo exists)"
if [ ! -d .git ]; then
  git init >/dev/null 2>&1 || true
fi
git add -A
git commit -m "backup before package-structure refactor" >/dev/null 2>&1 || true

echo "===> Step 2: Move Java sources up one level"
mkdir -p src/main/java/il/ac/hit/project
mkdir -p src/test/java/il/ac/hit/project

shopt -s nullglob
for p in src/main/java/il/ac/hit/project/main/*; do
  git mv "$p" src/main/java/il/ac/hit/project/ 2>/dev/null || mv "$p" src/main/java/il/ac/hit/project/
done

for p in src/test/java/il/ac/hit/project/main/*; do
  git mv "$p" src/test/java/il/ac/hit/project/ 2>/dev/null || mv "$p" src/test/java/il/ac/hit/project/
done

rmdir --ignore-fail-on-non-empty src/main/java/il/ac/hit/project/main || true
rmdir --ignore-fail-on-non-empty src/test/java/il/ac/hit/project/main || true

echo "===> Step 3: Update package declarations and imports"
find src -type f -name "*.java" -print0 | xargs -0 perl -pi -e 's/\bil\.ac\.hit\.project\.main\b/il.ac.hit.project/g'

echo "===> Step 4: Fix other files (pom.xml, configs)"
grep -R --line-number --files-with-matches "il.ac.hit.project.main" . | sort -u > files_to_fix.txt || true
if [ -s files_to_fix.txt ]; then
  while IFS= read -r f; do
    perl -pi -e 's/il\.ac\.hit\.project\.main/il.ac.hit.project/g' "$f"
  done < files_to_fix.txt
fi
rm -f files_to_fix.txt

echo "===> Step 5: Update main.class in pom.xml (if present)"
perl -pi -e 's#<main\.class>\s*il\.ac\.hit\.project\.main\.Main\s*</main\.class>#<main.class>il.ac.hit.project.Main</main.class>#g' pom.xml

echo "===> Step 6: Clean and rebuild"
mvn clean package -DskipTests

echo "===> Step 7: Run tests"
mvn test

echo "===> Step 8: Commit changes"
git add -A
git commit -m "refactor: package il.ac.hit.project.main -> il.ac.hit.project" >/dev/null 2>&1 || true

echo "===> Done!"
echo "Project structure has been refactored. Main class should now be il.ac.hit.project.Main"
