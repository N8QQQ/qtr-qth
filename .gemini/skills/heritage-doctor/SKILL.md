# Heritage Doctor

This skill acts as a configuration watchdog and repair utility for the `qtr-qth` workspace. It ensures that every development host (Mechanar, Gandalf, Athena, or Raspberry Pi) maintains the "Heritage" standard for identity and security.

## 🩺 Diagnostic Checklist

Before performing any commits or PR operations, ensure the following conditions are met:

### 1. Identity Verification
- **User Name:** `git config user.name` must be set.
- **User Email:** `git config user.email` must be set.

### 2. SSH Commit Signing (The Heritage Standard)
- **GPG Format:** `git config gpg.format` must be `ssh`.
- **Commit Signing:** `git config commit.gpgsign` must be `true`.
- **Signing Key:** `git config user.signingkey` must point to a valid SSH public key (e.g., `~/.ssh/id_ed25519.pub`).
- **Local Verification:** `git config gpg.ssh.allowedSignersFile` must point to an `allowed_signers` file containing your email and public key.
- **GitHub Sync:** The public key must be registered as a **Signing Key** in the user's GitHub account settings.

### 3. GitHub CLI Authentication
- **Status:** `gh auth status` must report a valid logged-in account with appropriate scopes.

## 🛠️ Repair Procedures

If diagnostics fail, apply the following repairs as needed:

### Enforce SSH Signing
```bash
git config --global gpg.format ssh
git config --global commit.gpgsign true
# Replace with the actual path to your public key
git config --global user.signingkey ~/.ssh/id_ed25519.pub

# Setup local verification
# 1. Create the allowed_signers file
echo "$(git config user.email) $(cat ~/.ssh/id_ed25519.pub)" > ~/.ssh/allowed_signers
# 2. Configure git to use it
git config --global gpg.ssh.allowedSignersFile ~/.ssh/allowed_signers
```

### Fix Identity
```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

### Authenticate GitHub CLI
```bash
gh auth login
```

## 🚀 Execution Guardrail
This skill must be invoked whenever a "Commit Verification Failed" or "Unsigned Commit" error is encountered. It serves as the prerequisite for all `heritage-committer` (standard commit) and `heritage-releaser` (release) operations.
