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
git commit -m "backup before package-structure refactor (to src/il/ac/hit/project/{main,test})" >/dev/null 2>&1 || true

echo "===> Step 2: Create new directories"
mkdir -p src/il/ac/hit/project/main
mkdir -p src/il/ac/hit/project/test

echo "===> Step 3: Move Java sources into src/.../main"
shopt -s nullglob
for p in src/main/java/il/ac/hit/project/*; do
  git mv "$p" src/il/ac/hit/project/main/ 2>/dev/null || mv "$p" src/il/ac/hit/project/main/
done

echo "===> Step 4: Move test sources into src/.../test"
for p in src/test/java/il/ac/hit/project/*; do
  git mv "$p" src/il/ac/hit/project/test/ 2>/dev/null || mv "$p" src/il/ac/hit/project/test/
done

echo "===> Step 5: Clean up old src/main/java and src/test/java"
rm -rf src/main/java src/test/java

echo "===> Step 6: Update package declarations in main sources"
find src/il/ac/hit/project/main -type f -name "*.java" -print0 | \
  xargs -0 perl -pi -e 's/\bil\.ac\.hit\.project\b/il.ac.hit.project.main/g'

echo "===> Step 7: Update package declarations in test sources"
find src/il/ac/hit/project/test -type f -name "*.java" -print0 | \
  xargs -0 perl -pi -e 's/\bil\.ac\.hit\.project\b/il.ac.hit.project.test/g'

echo "===> Step 8: Update pom.xml to use src as source root and new Main class"
# Add custom build section if not present
if ! grep -q "<sourceDirectory>src</sourceDirectory>" pom.xml; then
  perl -0777 -pi -e 's#</build>#  <sourceDirectory>src</sourceDirectory>\n    <testSourceDirectory>src</testSourceDirectory>\n  </build>#s' pom.xml
fi

# Update main class
perl -pi -e 's#<main\.class>.*</main\.class>#<main.class>il.ac.hit.project.main.Main</main.class>#g' pom.xml

echo "===> Step 9: Commit refactor"
git add -A
git commit -m "refactor: move to src/il/ac/hit/project/{main,test} package structure" >/dev/null 2>&1 || true

echo "===> Done!"
echo "✅ Project is now in the new structure:"
echo "   src/il/ac/hit/project/main/... for production code"
echo "   src/il/ac/hit/project/test/... for test code"
