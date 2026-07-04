#!/usr/bin/env bash
# Cuts a release: bumps the version, builds and verifies signed release
# artifacts, tags, pushes (which triggers the store-publishing workflow),
# and creates a GitHub release with the artifacts attached.
#
# Usage:
#   scripts/release.sh patch          2.6.5 -> 2.6.6
#   scripts/release.sh minor          2.6.5 -> 2.7.0
#   scripts/release.sh major          2.6.5 -> 3.0.0
#   scripts/release.sh 2.7.1          explicit version
#   scripts/release.sh -y patch       skip the confirmation prompt
#
# versionCode follows the existing scheme: versionName with the dots removed
# (2.6.5 -> 265). The script verifies it still increases monotonically.

set -Eeuo pipefail
cd "$(dirname "$0")/.."

GRADLE_FILE="app/build.gradle"

usage() {
    sed -n '2,12p' "$0" | sed 's/^# \{0,1\}//'
    exit 1
}

assume_yes=false
args=()
for arg in "$@"; do
    if [[ "$arg" == "-y" || "$arg" == "--yes" ]]; then
        assume_yes=true
    else
        args+=("$arg")
    fi
done
set -- "${args[@]}"

[[ $# -eq 1 ]] || usage

branch=$(git rev-parse --abbrev-ref HEAD)
if [[ "$branch" != "main" ]]; then
    echo "error: releases must be cut from main (currently on '$branch')" >&2
    exit 1
fi

if [[ -n "$(git status --porcelain)" ]]; then
    echo "error: working tree is not clean — commit or stash your changes first" >&2
    exit 1
fi

current=$(grep -oP "versionName '\K[^']+" "$GRADLE_FILE")
if [[ ! "$current" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "error: could not parse current versionName ('$current') from $GRADLE_FILE" >&2
    exit 1
fi
IFS=. read -r major minor patch <<< "$current"

case "$1" in
    major) new="$((major + 1)).0.0" ;;
    minor) new="$major.$((minor + 1)).0" ;;
    patch) new="$major.$minor.$((patch + 1))" ;;
    *)
        [[ "$1" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || usage
        new="$1"
        ;;
esac

old_code=$(grep -oP 'versionCode \K[0-9]+' "$GRADLE_FILE")
new_code=${new//./}
if (( new_code <= old_code )); then
    echo "error: new versionCode $new_code is not greater than current $old_code" >&2
    exit 1
fi

echo "Release v$current -> v$new (versionCode $old_code -> $new_code)"
if ! $assume_yes; then
    read -r -p "Proceed? [y/N] " reply
    [[ "$reply" =~ ^[Yy]$ ]] || exit 1
fi

sed -i -E "s/versionCode [0-9]+/versionCode $new_code/" "$GRADLE_FILE"
sed -i -E "s/versionName '[^']*'/versionName '$new'/" "$GRADLE_FILE"

# If anything fails from here until the commit, undo the version bump
revert_bump() {
    git checkout -- "$GRADLE_FILE"
    echo "error: build or tests failed — version bump reverted, nothing pushed" >&2
}
trap revert_bump ERR

echo "Running unit tests..."
./gradlew :app:testDebugUnitTest

echo "Building signed release artifacts..."
./gradlew bundleRelease assembleRelease

trap - ERR

git add "$GRADLE_FILE"
git commit -m "release: v$new"
git tag "v$new"
git push origin HEAD "v$new"

echo "Creating GitHub release..."
gh release create "v$new" \
    --title "v$new" \
    --generate-notes \
    app/build/outputs/bundle/release/app-release.aab \
    app/build/outputs/apk/release/app-release.apk

echo
echo "Done. The v$new tag also triggered the store-publishing workflow:"
echo "  gh run watch"
