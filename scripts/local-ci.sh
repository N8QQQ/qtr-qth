#!/bin/bash
# qtr-qth local CI/CD Replicator (Docker Security & Quality Scans)
# Curated for N8QQQ (Nicholas R. Ustick)

set -e

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="docker/docker-compose.yml"
cd "$PROJECT_DIR"

AGENT_MODE=0
REPORTS_DIR=".agents/reports"

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0;37m' # No Color

log_info() {
	if [ "$AGENT_MODE" = "0" ]; then echo -e "${CYAN}$1${NC}"; fi
}
log_warn() {
	if [ "$AGENT_MODE" = "0" ]; then echo -e "${YELLOW}$1${NC}"; fi
}

if [ "$AGENT_MODE" = "0" ]; then
	echo -e "${BLUE}==================================================${NC}"
	echo -e "${BLUE}  🛰️ qtr-qth : Local Code Quality & Security Hub   ${NC}"
	echo -e "${BLUE}==================================================${NC}"
fi

# 1. Audit Docker Environment
if ! docker info >/dev/null 2>&1; then
	echo -e "${RED}[ERROR] Docker daemon is not running. Launch Docker to run scans.${NC}"
	exit 1
fi

# Helper functions
run_gitleaks() {
	log_info "\n🔑 Running Secret Scanning (Gitleaks)..."
	if [ "$AGENT_MODE" = "1" ]; then
		docker compose -f "$COMPOSE_FILE" --profile quality run --rm gitleaks detect --source=/path --report-format json --report-path /path/$REPORTS_DIR/gitleaks.json
	else
		docker compose -f "$COMPOSE_FILE" --profile quality run --rm gitleaks
	fi
}

run_trivy() {
	log_info "\n📦 Running Dependency & Vulnerability Scan (Trivy)..."
	if [ "$AGENT_MODE" = "1" ]; then
		docker compose -f "$COMPOSE_FILE" --profile quality run --rm trivy fs --scanners vuln,secret --skip-dirs docker/codeql-results --skip-dirs .agents --format json --output /rootfs/$REPORTS_DIR/trivy.json /rootfs
	else
		docker compose -f "$COMPOSE_FILE" --profile quality run --rm trivy fs --scanners vuln,secret --skip-dirs docker/codeql-results --skip-dirs .agents /rootfs
	fi
}

run_super_linter() {
	log_info "\n🎨 Running Orchestrated Style/Syntax Linter (Super-Linter)..."
	log_warn "[NOTE] Super-Linter image is heavy (~5GB). Pulling/running may take time."
	if [ "$AGENT_MODE" = "1" ]; then
		docker compose -f "$COMPOSE_FILE" --profile quality run --rm super-linter >"$REPORTS_DIR/super-linter.log" 2>&1
	else
		docker compose -f "$COMPOSE_FILE" --profile quality run --rm super-linter
	fi
}

run_codeql() {
	log_info "\n🧬 Running Semantic Security Analysis (CodeQL)..."
	log_warn "[NOTE] CodeQL database creation requires compiling Java code."
	if [ "$AGENT_MODE" = "1" ]; then
		docker compose -f "$COMPOSE_FILE" --profile quality run --rm codeql >"$REPORTS_DIR/codeql.log" 2>&1
		cp docker/codeql-results/scan.sarif "$REPORTS_DIR/codeql.sarif" 2>/dev/null || true
	else
		docker compose -f "$COMPOSE_FILE" --profile quality run --rm codeql
	fi
}

show_help() {
	echo -e "Usage: $0 [options]"
	echo -e "Options:"
	echo -e "  --secrets      Run Gitleaks secret detection"
	echo -e "  --vuln         Run Trivy software composition analysis"
	echo -e "  --lint         Run Super-Linter syntax checks"
	echo -e "  --codeql       Run CodeQL static analysis"
	echo -e "  --all          Run all tests sequentially"
	echo -e "  --agent-mode   Output pure JSON artifacts to .agents/reports/"
	echo -e "  --help         Show this help menu"
}

# Pre-parse for agent mode
for arg in "$@"; do
	if [ "$arg" = "--agent-mode" ]; then
		AGENT_MODE=1
		mkdir -p "$REPORTS_DIR"
	fi
done

# Command dispatching
if [ $# -eq 0 ]; then
	show_help
	exit 0
fi

while [ $# -gt 0 ]; do
	case "$1" in
	--agent-mode)
		shift
		;;
	--secrets)
		run_gitleaks
		shift
		;;
	--vuln)
		run_trivy
		shift
		;;
	--lint)
		run_super_linter
		shift
		;;
	--codeql)
		run_codeql
		shift
		;;
	--all)
		log_info "\n🚀 Launching ALL checks in parallel..."
		run_gitleaks &
		PID_GL=$!
		run_trivy &
		PID_TR=$!
		run_super_linter &
		PID_SL=$!
		run_codeql &
		PID_CQ=$!

		FAIL=0
		wait $PID_GL || FAIL=1
		wait $PID_TR || FAIL=1
		wait $PID_SL || FAIL=1
		wait $PID_CQ || FAIL=1

		if [ $FAIL -ne 0 ]; then
			echo -e "\n${RED}❌ One or more tests failed.${NC}"
			exit 1
		fi
		shift
		;;
	--help)
		show_help
		exit 0
		;;
	*)
		echo -e "${RED}Unknown option: $1${NC}"
		show_help
		exit 1
		;;
	esac
done

if [ "$AGENT_MODE" = "0" ]; then echo -e "\n${GREEN}✅ Quality & Security Scans executed successfully.${NC}"; fi
exit 0
