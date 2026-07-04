#!/bin/bash
# qtr-qth local CI/CD Replicator (Docker Security & Quality Scans)
# Curated for N8QQQ (Nicholas R. Ustick)

set -e

# Configuration
COMPOSE_FILE="docker/docker-compose.yml"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0;37m' # No Color

echo -e "${BLUE}==================================================${NC}"
echo -e "${BLUE}  🛰️ qtr-qth : Local Code Quality & Security Hub   ${NC}"
echo -e "${BLUE}==================================================${NC}"

# 1. Audit Docker Environment
if ! docker info >/dev/null 2>&1; then
    echo -e "${RED}[ERROR] Docker daemon is not running. Launch Docker to run scans.${NC}"
    exit 1
fi

# Helper functions
run_gitleaks() {
    echo -e "\n${CYAN}🔑 Running Secret Scanning (Gitleaks)...${NC}"
    docker compose -f "$COMPOSE_FILE" --profile quality run --rm gitleaks
}

run_trivy() {
    echo -e "\n${CYAN}📦 Running Dependency & Vulnerability Scan (Trivy)...${NC}"
    docker compose -f "$COMPOSE_FILE" --profile quality run --rm trivy
}

run_super_linter() {
    echo -e "\n${CYAN}🎨 Running Orchestrated Style/Syntax Linter (Super-Linter)...${NC}"
    echo -e "${YELLOW}[NOTE] Super-Linter image is heavy (~5GB). Pulling/running may take time.${NC}"
    docker compose -f "$COMPOSE_FILE" --profile quality run --rm super-linter
}

run_codeql() {
    echo -e "\n${CYAN}🧬 Running Semantic Security Analysis (CodeQL)...${NC}"
    echo -e "${YELLOW}[NOTE] CodeQL database creation requires compiling Java code.${NC}"
    docker compose -f "$COMPOSE_FILE" --profile quality run --rm codeql
}

show_help() {
    echo -e "Usage: $0 [options]"
    echo -e "Options:"
    echo -e "  --secrets   Run Gitleaks secret detection"
    echo -e "  --vuln      Run Trivy software composition analysis"
    echo -e "  --lint      Run Super-Linter syntax checks"
    echo -e "  --codeql    Run CodeQL static analysis"
    echo -e "  --all       Run all tests sequentially"
    echo -e "  --help      Show this help menu"
}

# Command dispatching
if [ $# -eq 0 ]; then
    show_help
    exit 0
fi

while [ $# -gt 0 ]; do
    case "$1" in
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
            run_gitleaks
            run_trivy
            run_super_linter
            run_codeql
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

echo -e "\n${GREEN}✅ Quality & Security Scans executed successfully.${NC}"
exit 0
