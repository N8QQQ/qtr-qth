#!/bin/bash
# ---------------------------------------------------------
# WSL2 USB Serial Port Bridge Automation Tool
# ---------------------------------------------------------
set -eo pipefail

select_device() {
    local filter_pattern="$1"
    # Make arrays global so we can access them outside
    unset MATCHING_BUSIDS
    unset MATCHING_DESCRIPTIONS
    declare -g -a MATCHING_BUSIDS
    declare -g -a MATCHING_DESCRIPTIONS
    local idx=1
    
    while IFS= read -r line; do
        if [[ -z "$line" ]] || [[ "$line" == BUSID* ]] || [[ "$line" == Connected:* ]] || [[ "$line" == Persisted:* ]] || [[ "$line" == GUID* ]]; then
            continue
        fi

        if echo "$line" | grep -Eiq "(1546|067B|10C4|1A86|0403):[0-9A-Fa-f]{4}"; then
            if [ -n "$filter_pattern" ]; then
                if ! echo "$line" | grep -Eiq "$filter_pattern"; then
                    continue
                fi
            fi
            
            local busid
            busid=$(echo "$line" | awk '{print $1}')
            local vid_pid
            vid_pid=$(echo "$line" | awk '{print $2}')
            local remainder
            remainder=$(echo "$line" | sed -E "s/^${busid}[[:space:]]+${vid_pid}[[:space:]]+//" | sed 's/  */ /g')
            
            echo "[$idx] BUSID: $busid | VID:PID: $vid_pid | $remainder"
            MATCHING_BUSIDS[idx]=$busid
            MATCHING_DESCRIPTIONS[idx]=$remainder
            idx=$((idx+1))
        fi
    done < <(usbipd.exe list 2>/dev/null | tr -d '\r')

    if [ ${#MATCHING_BUSIDS[@]} -eq 0 ]; then
        echo "No matching GPS/Serial devices found matching criteria."
        exit 1
    fi
    
    echo ""
    read -r -p "Select a device number (or 'q' to quit): " sel
    if [[ "$sel" == "q" || "$sel" == "Q" ]]; then
        echo "Exiting..."
        exit 0
    fi
    if ! [[ "$sel" =~ ^[0-9]+$ ]] || [ -z "${MATCHING_BUSIDS[$sel]}" ]; then
        echo "⚠️ Invalid selection."
        exit 1
    fi
    
    SELECTED_BUSID="${MATCHING_BUSIDS[$sel]}"
    SELECTED_DESC="${MATCHING_DESCRIPTIONS[$sel]}"
}

# ---- SLICES 4 & 5: Release and Fault Injection Hooks ----
if [[ "$1" == "release" || "$1" == "--release" ]]; then
    BUSID="${2:---busid}"
    if [[ "$BUSID" == "--busid" ]]; then
        BUSID="$3"
    fi
    if [ -z "$BUSID" ]; then
        echo "🔍 Scanning for bound GPS devices..."
        # Find devices that do NOT have "Not shared"
        select_device "^.*(Shared|Attached).*$"
        BUSID="$SELECTED_BUSID"
    fi

    echo -e "\n✅ Selected: BUSID $BUSID"
    echo "🔌 Detaching BUSID $BUSID from WSL..."
    usbipd.exe detach --busid "$BUSID" || true
    echo "🔓 Unbinding BUSID $BUSID from usbipd host driver..."
    powershell.exe -Command "Start-Process usbipd.exe -ArgumentList 'unbind --busid $BUSID' -Verb RunAs -WindowStyle Hidden -Wait"
    echo "✅ Host release completed."
    exit 0
fi

if [[ "$1" == "simulate-disconnect" ]]; then
    BUSID="${2:---busid}"
    if [[ "$BUSID" == "--busid" ]]; then
        BUSID="$3"
    fi
    if [ -z "$BUSID" ]; then
        echo "🔍 Scanning for bound GPS devices..."
        select_device "^.*(Shared|Attached).*$"
        BUSID="$SELECTED_BUSID"
    fi

    echo -e "\n✅ Selected: BUSID $BUSID"
    echo "⚡ [FAULT INJECTION] Forcing immediate ungraceful detach of BUSID $BUSID..."
    usbipd.exe detach --busid "$BUSID"
    echo "✅ Detach signal sent. Watch Java Orchestrator logs for recovery telemetry."
    exit 0
fi

echo -e "--- 📡 WSL2 USB GPS Bridge Tool ---\n"
echo "Checking Windows host for serial devices..."

if ! command -v usbipd.exe &> /dev/null; then
    echo "⚠️ [ERROR] usbipd.exe not found on the host."
    echo "Please install usbipd-win on Windows: https://github.com/dorssel/usbipd-win/releases"
    exit 1
fi

select_device ""

echo -e "\n✅ Selected: BUSID $SELECTED_BUSID ($SELECTED_DESC)"

# ---- SLICE 2: UAC Elevation & WSL Attachment ----
echo "Elevating Windows privileges to bind device to usbipd (UAC prompt will appear)..."
if ! powershell.exe -Command "Start-Process usbipd.exe -ArgumentList 'bind --busid $SELECTED_BUSID' -Verb RunAs -WindowStyle Hidden -Wait"; then
    echo "⚠️ [ERROR] Host binding failed or was declined by user (UAC)."
    exit 1
fi

DISTRO=$WSL_DISTRO_NAME
if [ -z "$DISTRO" ]; then
    echo "⚠️ [ERROR] \$WSL_DISTRO_NAME is not set. Are you running inside WSL2?"
    exit 1
fi

echo "🔗 Attaching BUSID $SELECTED_BUSID to WSL2 distro '$DISTRO'..."
if ! usbipd.exe attach --wsl --busid "$SELECTED_BUSID"; then
    echo "⚠️ [ERROR] Failed to attach device. It may be in use by another driver."
    echo "Try running: ./scripts/wsl-usb-bridge.sh release --busid $SELECTED_BUSID"
    exit 1
fi

echo "🎉 Device attachment sequence completed."

# ---- SLICE 3: TTY Verification & Output Formatting ----
echo "⏳ Waiting for Linux block device enumeration..."
sleep 1.5

NEW_TTY=$(find /dev -maxdepth 1 \( -name "ttyUSB*" -o -name "ttyACM*" \) 2>/dev/null | head -n 1 || true)

if [ -n "$NEW_TTY" ]; then
    echo -e "\n✅ Success! Device is ready."
    echo "=================================="
    echo "serial.port=$NEW_TTY"
    echo "=================================="
else
    echo -e "\n⚠️ [WARNING] Device attached, but TTY node was not found in /dev/."
    echo "Check dmesg for USB driver enumeration errors."
fi
