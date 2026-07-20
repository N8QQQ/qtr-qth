#!/bin/bash
# qtr-qth : Linux Serial Permissions Helper
# Developed by Nicholas R. Ustick (N8QQQ)

echo "=========================================="
echo "  qtr-qth : Linux Setup Utility           "
echo "=========================================="

if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Check for common serial groups
    REQUIRED_GROUPS=("dialout" "uucp")
    USER_GROUPS=$(groups "$USER")
    
    NEEDS_FIX=true
    for group in "${REQUIRED_GROUPS[@]}"; do
        if [[ $USER_GROUPS == *"$group"* ]]; then
            echo "[OK] User '$USER' is already a member of the '$group' group."
            NEEDS_FIX=false
            break
        fi
    done

    if [ "$NEEDS_FIX" = true ]; then
        echo "[!] User '$USER' lacks serial port permissions."
        echo "This is required to read data from your USB GPS receiver."
        echo ""
        read -p "Would you like to add '$USER' to the 'dialout' group now? (y/n): " -n 1 -r
        echo ""
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            sudo usermod -a -G dialout "$USER"
            echo "------------------------------------------"
            echo "[SUCCESS] Permissions updated."
            echo "IMPORTANT: You MUST log out and log back in (or reboot) for this to take effect."
            echo "------------------------------------------"
        else
            echo "[INFO] Setup cancelled. You may need to run 'sudo usermod -a -G dialout $USER' manually."
        fi
    fi
else
    echo "[INFO] This system does not appear to be Linux. No setup required."
fi
