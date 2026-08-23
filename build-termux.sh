#!/data/data/com.termux/files/usr/bin/bash
# ╔══════════════════════════════════════════════════╗
# ║   TurtleClient — Termux Build & Install Script   ║
# ╚══════════════════════════════════════════════════╝

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

info()    { echo -e "${CYAN}[*]${NC} $1"; }
success() { echo -e "${GREEN}[✓]${NC} $1"; }
warn()    { echo -e "${YELLOW}[!]${NC} $1"; }
error()   { echo -e "${RED}[✗]${NC} $1"; exit 1; }

echo ""
echo -e "${CYAN}  ████████╗██╗   ██╗██████╗ ████████╗██╗     ███████╗"
echo -e "     ██╔══╝██║   ██║██╔══██╗╚══██╔══╝██║     ██╔════╝"
echo -e "     ██║   ██║   ██║██████╔╝   ██║   ██║     █████╗  "
echo -e "     ██║   ██║   ██║██╔══██╗   ██║   ██║     ██╔══╝  "
echo -e "     ██║   ╚██████╔╝██║  ██║   ██║   ███████╗███████╗"
echo -e "     ╚═╝    ╚═════╝ ╚═╝  ╚═╝   ╚═╝   ╚══════╝╚══════╝${NC}"
echo -e "     ${YELLOW}Client Builder for Termux${NC}"
echo ""

# ── Step 1: Dependencies ─────────────────────────────────────────────
info "Updating packages..."
pkg update -y -q 2>/dev/null || warn "pkg update had warnings (continuing)"

info "Installing required packages..."
pkg install -y openjdk-21 wget unzip zip git 2>/dev/null || \
    error "Failed to install packages. Run 'pkg update' first."

success "Dependencies ready"

# ── Step 2: Verify Java ──────────────────────────────────────────────
JAVA_VER=$(java -version 2>&1 | head -1)
info "Java: $JAVA_VER"
if ! java -version 2>&1 | grep -q "21\|22\|23"; then
    error "Java 21+ required. Install with: pkg install openjdk-21"
fi
success "Java OK"

# ── Step 3: Locate project ───────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"

if [ ! -f "$PROJECT_DIR/build.gradle.kts" ]; then
    error "build.gradle.kts not found in $PROJECT_DIR\nMake sure this script is inside the turtle-client folder."
fi
info "Project: $PROJECT_DIR"

# ── Step 4: Make gradlew executable ─────────────────────────────────
chmod +x "$PROJECT_DIR/gradlew"
success "gradlew ready"

# ── Step 5: Build ────────────────────────────────────────────────────
# Turtle Client now targets multiple Minecraft versions via Stonecutter.
# Pass one as the first argument, e.g.: bash build-termux.sh 1.21.11
# Defaults to the 1.21.4 baseline. 26.2 needs a Java 25 JDK -- if Termux's
# repo doesn't have one yet, that node will fail to build here; build it via
# GitHub Actions CI instead (.github/workflows/build.yml already matrices
# across every version).
MC_VERSION="${1:-1.21.4}"
info "Target version: $MC_VERSION (pass a different one as \$1 to switch)"
info "Building TurtleClient (this takes 5–15 min on first run)..."
info "Gradle will download Minecraft and all dependencies automatically."
echo ""

cd "$PROJECT_DIR"

# Set JAVA_HOME for Termux
export JAVA_HOME="$PREFIX/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"

# Termux uses $HOME/tmp not /tmp
mkdir -p "$HOME/tmp"
BUILD_LOG="$HOME/tmp/turtle_build.log"

# Run build for the selected version node
./gradlew "${MC_VERSION}:buildAndCollect" --no-daemon 2>&1 | tee "$BUILD_LOG" | grep -E "BUILD|FAILED|error:|> Task|Exception"
echo ""

# ── Step 6: Check result ─────────────────────────────────────────────
JAR=$(find "$PROJECT_DIR/build/libs/${MC_VERSION}" -name "*.jar" ! -name "*-sources.jar" 2>/dev/null | head -1)

if [ -z "$JAR" ]; then
    echo ""
    error "Build FAILED. Check errors above or run:\n  cat ~/tmp/turtle_build.log"
fi

success "Built: $(basename $JAR)"

# ── Step 7: Copy to mods folder (optional) ───────────────────────────
echo ""
echo -e "${YELLOW}Where do you want to install the mod?${NC}"
echo "  1) Minecraft Java on this device (via PojavLauncher path)"
echo "  2) Copy to Downloads folder"
echo "  3) Skip (keep in build/libs)"
read -rp "Choice [1/2/3]: " CHOICE

case "$CHOICE" in
    1)
        # PojavLauncher default mods path
        POJAV_MODS="$HOME/.minecraft/mods"
        mkdir -p "$POJAV_MODS"
        cp "$JAR" "$POJAV_MODS/"
        success "Installed to $POJAV_MODS/$(basename $JAR)"
        warn "Make sure you have Fabric 1.21.1 + fabric-api installed in PojavLauncher!"
        ;;
    2)
        DEST="/sdcard/Download/$(basename $JAR)"
        cp "$JAR" "$DEST" 2>/dev/null || {
            warn "Could not copy to /sdcard/Download — trying storage/downloads"
            cp "$JAR" "$HOME/storage/downloads/$(basename $JAR)" 2>/dev/null || \
                warn "Run 'termux-setup-storage' first to enable SD card access."
        }
        success "Copied to Downloads: $(basename $JAR)"
        ;;
    3)
        info "JAR is at: $JAR"
        ;;
esac

# ── Done ─────────────────────────────────────────────────────────────
echo ""
echo -e "${GREEN}╔════════════════════════════════════════╗"
echo -e "║   TurtleClient build complete! 🎉      ║"
echo -e "╚════════════════════════════════════════╝${NC}"
echo ""
echo -e "  JAR: ${CYAN}$(basename $JAR)${NC}"
echo -e "  To install manually, drop it in your .minecraft/mods folder"
echo -e "  alongside fabric-api-*.jar"
echo ""
echo -e "  Required mods:"
echo -e "    • Fabric Loader 0.19.2+"
echo -e "    • Fabric API 0.116.12+1.21.1"
echo -e "    • Fabric Language Kotlin 1.13.11+"
echo ""
