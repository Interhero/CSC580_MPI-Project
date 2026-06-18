# 🔧 MS-MPI: Detailed Setup Guide for Firewall & SMPD Service

> **Context**: This guide elaborates on Steps 4 and 5 of the MS-MPI cluster setup for Windows 10/11 laptops.

---

## ✅ Pre-Requisite Check

Before starting, confirm MS-MPI is already installed:

1. Open **Command Prompt** (any user) and type:
   ```
   mpiexec --version
   ```
   You should see output like:
   ```
   Microsoft MPI Bootstrapper Version 10.x.x
   ```
   If you get `'mpiexec' is not recognized`, go back and install **msmpisetup.exe** first.

2. Confirm the installation path exists:
   ```
   dir "C:\Program Files\Microsoft MPI\Bin\"
   ```
   You should see `mpiexec.exe`, `smpd.exe`, and related files.

---

## 🔥 Step 4: Configure Windows Firewall

You need to create **two types of firewall rules**:
- Allow the **MPI application executable** through the firewall
- Open the **TCP/UDP port range 49152–65535** (Dynamic RPC ports used by MPI)

Do this on **ALL 4 laptops**.

---

### Method A: Using Windows Defender Firewall GUI (Recommended for Beginners)

#### Part 1 — Allow MPI Executables Through Firewall

1. Press `Windows key + S`, search for **"Windows Defender Firewall"**, and open it.

2. In the left panel, click **"Allow an app or feature through Windows Defender Firewall"**.

3. Click **"Change settings"** (requires Administrator).

4. Click **"Allow another app..."** at the bottom right.

5. Click **"Browse..."** and navigate to:
   ```
   C:\Program Files\Microsoft MPI\Bin\
   ```

6. Select **`mpiexec.exe`** → Click **Open** → Click **Add**.

7. Repeat steps 4–6 for:
   - `smpd.exe` (in the same Bin folder)

8. Make sure **both "Private" and "Public"** checkboxes are ticked for each.

9. Click **OK** to save.

---

#### Part 2 — Open Port Range 49152–65535

1. Go back to the main **Windows Defender Firewall** window.

2. In the left panel, click **"Advanced settings"**.
   > This opens the **Windows Defender Firewall with Advanced Security** window.

3. In the left tree, click **"Inbound Rules"**.

4. In the right panel, click **"New Rule..."**.

5. Select **"Port"** → Click **Next**.

6. Select **"TCP"** → Select **"Specific local ports"** → Type:
   ```
   49152-65535
   ```
   Click **Next**.

7. Select **"Allow the connection"** → Click **Next**.

8. Tick all three: **Domain**, **Private**, **Public** → Click **Next**.

9. Name the rule: `MPI TCP Dynamic Ports` → Click **Finish**.

10. Repeat steps 3–9 but in step 6 choose **"UDP"** instead of TCP.
    Name it: `MPI UDP Dynamic Ports`.

> You should now have **4 new inbound rules** (mpiexec, smpd, TCP ports, UDP ports).

---

### Method B: Using PowerShell (Faster — Run as Administrator)

Open **PowerShell as Administrator** (right-click Start → Windows PowerShell (Admin)) and run these commands one by one:

```powershell
# Rule 1: Allow mpiexec.exe
New-NetFirewallRule `
  -DisplayName "MS-MPI mpiexec" `
  -Direction Inbound `
  -Action Allow `
  -Program "C:\Program Files\Microsoft MPI\Bin\mpiexec.exe" `
  -Profile Any

# Rule 2: Allow smpd.exe
New-NetFirewallRule `
  -DisplayName "MS-MPI smpd" `
  -Direction Inbound `
  -Action Allow `
  -Program "C:\Program Files\Microsoft MPI\Bin\smpd.exe" `
  -Profile Any

# Rule 3: Open TCP port range
New-NetFirewallRule `
  -DisplayName "MPI TCP Dynamic Ports" `
  -Direction Inbound `
  -Action Allow `
  -Protocol TCP `
  -LocalPort 49152-65535 `
  -Profile Any

# Rule 4: Open UDP port range
New-NetFirewallRule `
  -DisplayName "MPI UDP Dynamic Ports" `
  -Direction Inbound `
  -Action Allow `
  -Protocol UDP `
  -LocalPort 49152-65535 `
  -Profile Any
```

Expected output for each rule:
```
Name                  : {XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX}
DisplayName           : MS-MPI mpiexec
Description           :
...
Action                : Allow
```

---

### ✅ Verify Firewall Rules Were Created

```powershell
Get-NetFirewallRule | Where-Object { $_.DisplayName -like "MS-MPI*" -or $_.DisplayName -like "MPI*" } | Select-Object DisplayName, Enabled, Action
```

Expected output:
```
DisplayName              Enabled  Action
-----------              -------  ------
MS-MPI mpiexec           True     Allow
MS-MPI smpd              True     Allow
MPI TCP Dynamic Ports    True     Allow
MPI UDP Dynamic Ports    True     Allow
```

---

### ⚠️ Common Firewall Issues

| Problem | Likely Cause | Fix |
|---------|-------------|-----|
| Rule created but MPI still blocked | Third-party antivirus firewall (e.g., Kaspersky, McAfee) | Temporarily disable antivirus firewall OR add MPI rules in antivirus software |
| "Access Denied" when creating rules | Not running as Administrator | Right-click PowerShell → "Run as administrator" |
| Rules exist but nodes can't communicate | Network profile set to "Public" | Change network profile to "Private" (see below) |

#### Changing Network Profile to Private (Important for Wi-Fi/LAN)

```powershell
# Check current network profile
Get-NetConnectionProfile

# Change to Private (replace "Wi-Fi" with your actual interface name)
Set-NetConnectionProfile -InterfaceAlias "Wi-Fi" -NetworkCategory Private
```

---

---

## ⚙️ Step 5: Enable the SMPD Service

`smpd` (Simple Message Passing Daemon) is the **background service** that allows MPI to launch processes on remote machines. It must be installed and running on **ALL 4 laptops**.

---

### Method A: Command Prompt (Classic Way)

> ⚠️ **MUST run Command Prompt as Administrator**

1. Press `Windows key + S`, search **"cmd"**.
2. Right-click **Command Prompt** → Select **"Run as administrator"**.
3. A UAC prompt will appear — click **Yes**.

#### Sub-step 5.1 — Install the SMPD Service

```cmd
smpd -install
```

**Expected output:**
```
SMPD installation succeeded.
```

**If you see an error like `smpd is not recognized`:**
- The MPI Bin folder is not in your PATH.
- Fix: Use the full path:
  ```cmd
  "C:\Program Files\Microsoft MPI\Bin\smpd.exe" -install
  ```

**If you see `The service already exists`:**
- That is fine! It means smpd was already installed. Skip to Sub-step 5.2.

---

#### Sub-step 5.2 — Start the SMPD Service

```cmd
net start smpd
```

**Expected output:**
```
The MSMPI Launch Service service is starting.
The MSMPI Launch Service service was started successfully.
```

**If you see `The service name is invalid`:**
- Try the full service name:
  ```cmd
  net start "MSMPI Launch Service"
  ```

**If you see `Access is denied`:**
- You are NOT running as Administrator. Close and reopen CMD as Admin.

**If you see `The service is already running`:**
- That is fine! Proceed to verification.

---

### Method B: Using PowerShell as Administrator

```powershell
# Install smpd service
& "C:\Program Files\Microsoft MPI\Bin\smpd.exe" -install

# Start the service
Start-Service -Name "MSMPI Launch Service"

# Set service to start automatically on boot (optional but recommended)
Set-Service -Name "MSMPI Launch Service" -StartupType Automatic
```

---

### Method C: Using Windows Services Manager (GUI)

1. Press `Windows key + R`, type `services.msc`, press Enter.
2. Scroll down to find **"MSMPI Launch Service"** (or "smpd").
3. Right-click → **Properties**.
4. Set **Startup type** to **"Automatic"**.
5. Click **"Start"** if status shows "Stopped".
6. Click **OK**.

> 💡 Setting startup type to Automatic ensures smpd starts automatically every time the laptop boots — very useful during lab sessions.

---

### ✅ Verify SMPD is Running

**Option 1 — Command Prompt:**
```cmd
sc query smpd
```
Expected output:
```
SERVICE_NAME: smpd
        TYPE               : 10  WIN32_OWN_PROCESS
        STATE              : 4  RUNNING
                                (STOPPABLE, NOT_PAUSABLE, ACCEPTS_SHUTDOWN)
        WIN32_EXIT_CODE    : 0  (0x0)
        SERVICE_EXIT_CODE  : 0  (0x0)
        CHECKPOINT         : 0x0
        WAIT_HINT          : 0x0
```
The key line is: **`STATE : 4 RUNNING`** ✅

**Option 2 — PowerShell:**
```powershell
Get-Service -Name "MSMPI Launch Service" | Select-Object Name, Status, StartType
```
Expected output:
```
Name                 Status  StartType
----                 ------  ---------
MSMPI Launch Service Running Automatic
```

**Option 3 — Task Manager:**
- Open Task Manager (`Ctrl + Shift + Esc`)
- Go to **Services** tab
- Find `smpd` — it should show **Running**

---

### ⚠️ Common SMPD Issues

| Error Message | Cause | Fix |
|---------------|-------|-----|
| `smpd is not recognized` | PATH not set | Use full path: `"C:\Program Files\Microsoft MPI\Bin\smpd.exe" -install` |
| `Access is denied` | Not Administrator | Run CMD/PowerShell as Administrator |
| `The service already exists` | Previously installed | Just run `net start smpd` — OK to skip install |
| `The service name is invalid` | Service name mismatch | Use `net start "MSMPI Launch Service"` |
| `Error 1079` (account mismatch) | Service account issue | Set smpd to run as Local System (see below) |
| `The service did not start due to logon failure` | Credentials issue | See SMPD credential fix below |

---

### 🔧 Fix: SMPD Credential / Logon Issue (Error 1079 or Logon Failure)

This is a common problem on fresh Windows installs:

1. Press `Windows key + R` → type `services.msc` → Enter.
2. Find **"MSMPI Launch Service"** → right-click → **Properties**.
3. Go to the **"Log On"** tab.
4. Select **"Local System account"**.
5. Check **"Allow service to interact with desktop"**.
6. Click **OK**.
7. Right-click the service again → **Restart**.

---

## 🌐 Step 6 (Bonus): Verify Full Cluster Communication

After Steps 4 and 5 are complete on **all 4 laptops**, run this from the **Master Node (Member 1's laptop)** in an Administrator Command Prompt:

```cmd
mpiexec -hosts 4 192.168.x.1 1 192.168.x.2 1 192.168.x.3 1 192.168.x.4 1 hostname
```

Replace `192.168.x.1` etc. with actual IP addresses (find with `ipconfig` on each laptop).

**Expected output (one hostname per node):**
```
LAPTOP-MEMBER1
LAPTOP-MEMBER2
LAPTOP-MEMBER3
LAPTOP-MEMBER4
```

If all 4 hostnames appear → **Your cluster is ready!** 🎉

---

### ⚠️ If mpiexec Hangs or Times Out

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| Command hangs (no output) | smpd not running on remote node | Run `net start smpd` on remote machines |
| `Connect to smpd on ... failed` | Firewall blocking port | Re-check firewall rules on the failing node |
| `Could not find host` | Wrong IP or hostname | Verify IPs with `ipconfig`, try pinging each laptop |
| `Unable to connect to ... authentication` | smpd credential mismatch | All machines should use same local user credentials, or set smpd to Local System account |

---

### 🔁 Useful Quick-Reference Commands

```cmd
:: Check smpd status
sc query smpd

:: Start smpd
net start smpd

:: Stop smpd
net stop smpd

:: Restart smpd
net stop smpd && net start smpd

:: Uninstall smpd (if you need to reinstall cleanly)
net stop smpd
smpd -uninstall

:: Reinstall and start fresh
smpd -install
net start smpd
```

---

*Guide prepared for MS-MPI Group Assignment | June 2026*
