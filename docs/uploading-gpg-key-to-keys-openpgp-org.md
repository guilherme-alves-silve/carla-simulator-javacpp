# Uploading a GPG Public Key to keys.openpgp.org

## Overview
`keys.openpgp.org` requires you to confirm ownership of the email address on your key via a confirmation link. If you don't click that link quickly enough, or you switch browsers/tabs mid-process, the upload session expires with an error like:

```
Error: Upload session expired. Please try again.
```

## Steps (command line — recommended)

1. **Export your public key** (use the long key ID or fingerprint):
   ```bash
   gpg --armor --export YOUR_KEY_ID
   ```

2. **Send the key directly to the keyserver**:
   ```bash
   gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
   ```

3. **Check your inbox immediately.** The keyserver emails a confirmation link to each UID (email address) on your key. Click it as soon as it arrives — the session has a short expiration window (roughly 30 minutes).

## Steps (web upload — alternative)

1. Go to https://keys.openpgp.org/upload
2. Paste the output of `gpg --armor --export YOUR_KEY_ID`
3. Once the "check your email" screen appears, **do not close or refresh the tab** — just wait for the email and click the confirmation link right away.

## Common causes of the "session expired" error

| Cause | Fix |
|---|---|
| Took too long to click the confirmation link | Restart the process and confirm immediately |
| Opened the link on a different browser/device than the one used to upload | Restart the process on a single browser/session |
| Multiple upload attempts | Only the most recent session is valid — ignore links from older emails |

## Verifying the upload
After confirmation, check that the key is live:
```bash
gpg --keyserver keys.openpgp.org --recv-keys YOUR_KEY_ID
```
