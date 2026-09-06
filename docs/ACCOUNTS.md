# Accounts

Open **the name/account button at the top right of the title screen**.

- **Launcher account:** kept available for switching back during this run.
- **Add offline:** creates a local profile with Minecraft's deterministic offline UUID. This is for singleplayer and offline-mode servers; it does not authenticate to online-mode servers, Realms, or grant a Minecraft license.
- **Microsoft:** uses Microsoft's device-code page in your browser. TurtleClient never asks for your Microsoft password. A Java entitlement and Minecraft profile must both be verified before the account is added.
- Select a profile, then **Use account**. You must be disconnected. No account is switched while a world is open, after the account screen closes, or after a cancelled request.
- **Remove:** forgets metadata and the in-memory sign-in. First switch away from an active account. The original launcher entry cannot be deleted.

## Microsoft application setup (required)

This repository does **not** ship another launcher's OAuth application ID. The client maintainer must register an appropriate **public-client application** and obtain any required Xbox/Minecraft API approval. Without that registration, Microsoft authentication cannot operate out of the box.

1. Register an application supporting personal Microsoft accounts in Microsoft Entra.
2. Enable the public-client/device-code flow. Do not create or enter a client secret.
3. Configure the required Xbox Live sign-in permission and obtain Minecraft Services API access/approval where required.
4. Enter its **Application (client) ID** in **Accounts → App setup**. This ID is public, not a password.
5. Choose Microsoft, open the official page, enter the displayed code, and approve the requested sign-in. Cancel/expiry/declined/family-restricted/unlicensed results leave the current account unchanged.

Registration guidance: <https://learn.microsoft.com/entra/identity-platform/quickstart-register-app>
Device flow: <https://learn.microsoft.com/entra/identity-platform/v2-oauth2-device-code>

A generic app registration alone does not guarantee Minecraft API access. An authorization error can indicate missing application approval, not an incorrect password.

## Storage and security

`config/turtle-client/accounts.json` stores only names, UUIDs, account kinds, and the public application ID. Access tokens stay **in memory only** and are discarded on exit/removal. No refresh tokens or passwords are stored. Saved Microsoft profiles must sign in again after restarting.

The flow uses HTTPS and an explicit service-host allowlist, rejects redirects/unexpected verification hosts, bounds responses, supports cancellation, checks Java ownership, and does not fall back to offline authentication on premium-login failure. Online account-property failures abort switching rather than using permissive offline properties. The title's Multiplayer button respects Minecraft's account/launcher permission check.

Session switching prepares account-scoped services first, then replaces the session, properties/profile futures, social, signing-key, reporting and telemetry services on the client thread. Newer versions also rebuild their Services/friends state. Discovery uses actual JVM types/signatures rather than Yarn field-name strings, so remapping does not break it. A failed field update is rolled back.

## Validation boundary

Automated tests use synthetic responses, metadata round trips and bytecode/API checks. They are **not** a real Microsoft-account test or a Minecraft multiplayer/session-switch test. Test an approved app and real accounts before distributing the sign-in feature as production-validated.
