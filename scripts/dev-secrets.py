#!/usr/bin/env python3
"""Prepare private, persistent credentials for the local Compose and IDE profiles."""

import argparse
import fcntl
import getpass
import json
import os
from pathlib import Path
import re
import secrets
import subprocess
import sys
import tempfile
import warnings


ROOT = Path(__file__).resolve().parents[1]
DIRECTORY = ROOT / "run/etc/ecpds/development-secrets"
NAMES = (
    "MYSQL_ROOT_PASSWORD", "KEYSTORE_PASSWORD", "RCC_SHARED_SECRET",
    "CLI_SHARED_SECRET", "JOLOKIA_PASSWORD", "HAWTIO_PASSWORD", "DEVELOPMENT_API_SECRET",
)
KEYSTORE_SERVICES = ("monitor", "mover")


def write_private(path, content):
    if path.is_symlink() or path.exists() and not path.is_file():
        raise ValueError(f"Expected a regular credential file: {path}")
    descriptor, temporary = tempfile.mkstemp(prefix=".credentials-", dir=DIRECTORY)
    try:
        with os.fdopen(descriptor, "w", encoding="utf-8") as output:
            output.write(content)
        os.replace(temporary, path)
    finally:
        if os.path.exists(temporary):
            os.unlink(temporary)


def validate_values(values):
    for name in (*NAMES, "JOLOKIA_USER", "HAWTIO_USER"):
        value = values.get(name)
        if not isinstance(value, str) or not re.fullmatch(r"[A-Za-z0-9._~!@%+/=-]+", value):
            raise ValueError(f"{name} must be nonempty and contain only letters, digits or ._~!@%+/=-")


def existing_keystores():
    paths = []
    for service in KEYSTORE_SERVICES:
        key = ROOT / f"run/etc/ecpds/{service}/ecpds-{service}.pfx"
        for path in (key, key.with_name(key.name + ".pending")):
            if path.is_symlink() or path.parent.is_symlink() or path.exists() and not path.is_file():
                raise ValueError(f"Expected a regular development TLS keystore: {path}")
            if path.exists():
                paths.append(path)
    return paths


def validate_keystores(paths, password):
    descriptor, temporary = tempfile.mkstemp(prefix=".keystore-password-", dir=DIRECTORY)
    try:
        with os.fdopen(descriptor, "w", encoding="utf-8") as output:
            output.write(password + "\n")
        environment = {name: value for name, value in os.environ.items()
                       if name in ("PATH", "HOME", "JAVA_HOME", "LANG", "LC_ALL")}
        for path in paths:
            try:
                result = subprocess.run(
                    ["keytool", "-J-Xmx128m", "-J-XX:ActiveProcessorCount=2", "-list",
                     "-keystore", str(path), "-storepass:file", temporary],
                    stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, env=environment, timeout=30)
            except FileNotFoundError:
                raise ValueError("keytool from a JDK is required to validate existing keystores") from None
            except subprocess.TimeoutExpired:
                raise ValueError(f"Timed out validating development TLS keystore: {path}") from None
            if result.returncode:
                raise ValueError(f"Cannot open development TLS keystore with the supplied password: {path}")
    finally:
        os.unlink(temporary)


def confirm_recreation(paths, acknowledged):
    print("Recreating development TLS keystores changes certificates and private keys.")
    print("Original keystores and any pending TLS keys will be backed up; no database or encryption keys are reset.")
    if not acknowledged:
        if not sys.stdin.isatty():
            raise ValueError("Stop the affected IDE/container services, then use "
                             "--recreate-keystores --services-stopped to acknowledge they are stopped")
        if input("Type RECREATE to confirm all affected IDE/container services are stopped: ") != "RECREATE":
            raise ValueError("Development keystore setup cancelled")
    for service in sorted({path.parent.name for path in paths}):
        pidfile = ROOT / f"run/var/tmp/ecpds/{service}/{service}.pid"
        if pidfile.exists():
            try:
                pid = int(pidfile.read_text().strip())
                if pid <= 0:
                    raise ValueError()
            except ValueError:
                raise ValueError(f"Cannot verify service PID file: {pidfile}") from None
            try:
                os.kill(pid, 0)
            except ProcessLookupError:
                continue
            except PermissionError:
                pass
            raise ValueError(f"Stop {service} before recreating its TLS keystore (PID {pid})")


def restore_keystores(moved):
    for original, backup, mode in reversed(moved):
        if original.exists() or original.is_symlink():
            raise ValueError(f"Cannot restore {original}: a new file exists; original retained at {backup}")
        backup.rename(original)
        original.chmod(mode)


def backup_keystores(paths):
    parent = DIRECTORY / "keystore-backups"
    if parent.is_symlink() or parent.exists() and not parent.is_dir():
        raise ValueError(f"Expected a private keystore backup directory: {parent}")
    parent.mkdir(mode=0o700, exist_ok=True)
    parent.chmod(0o700)
    backup = Path(tempfile.mkdtemp(prefix="keystores-", dir=parent))
    moved = []
    try:
        for path in paths:
            mode = path.stat().st_mode & 0o777
            target = backup / path.name
            path.rename(target)
            moved.append((path, target, mode))
            target.chmod(0o600)
    except BaseException:
        restore_keystores(moved)
        raise
    return backup, moved


def prepare(options):
    state = DIRECTORY / "secrets.json"
    if state.is_symlink() or state.exists() and not state.is_file():
        raise ValueError("Development credential state must be a regular file")
    initialized = state.exists()
    if initialized:
        state.chmod(0o600)
        values = json.loads(state.read_text(encoding="utf-8"))
    else:
        data = ROOT / "run/var/lib/ecpds/database"
        if any((data / name).exists() for name in ("mysql", "ecpds")) and not os.getenv("MYSQL_ROOT_PASSWORD"):
            raise ValueError("Existing database: set MYSQL_ROOT_PASSWORD to its current root password before setup")
        values = {name: os.getenv(name) or secrets.token_hex(32) for name in NAMES}
        values["JOLOKIA_USER"] = os.getenv("JOLOKIA_USER") or "jolokia"
        values["HAWTIO_USER"] = os.getenv("HAWTIO_USER") or "hawtio"

    validate_values(values)
    recreate = options.recreate_keystores
    paths = existing_keystores() if not initialized or recreate else []
    if paths and not initialized and not recreate and not os.getenv("KEYSTORE_PASSWORD"):
        if not sys.stdin.isatty():
            raise ValueError("Existing keystore: set KEYSTORE_PASSWORD to its current password before setup, "
                             "or run make dev-secrets interactively")
        print("Existing development TLS keystores:\n" + "\n".join(f"  {path}" for path in paths))
        print("1. Keep keystores and enter their current password\n2. Back up and recreate keystores\n3. Cancel")
        choice = input("Choice [1]: ").strip()
        if choice in ("", "1"):
            with warnings.catch_warnings():
                warnings.simplefilter("error", getpass.GetPassWarning)
                try:
                    values["KEYSTORE_PASSWORD"] = getpass.getpass("Current keystore password: ")
                except getpass.GetPassWarning:
                    raise ValueError("Cannot read a password with terminal echo disabled") from None
            validate_values(values)
        elif choice == "2":
            recreate = True
        else:
            raise ValueError("Development keystore setup cancelled")
    if paths:
        if recreate:
            confirm_recreation(paths, options.services_stopped)
        elif not initialized:
            validate_keystores(paths, values["KEYSTORE_PASSWORD"])
    values = {name: values[name] for name in (*NAMES, "JOLOKIA_USER", "HAWTIO_USER")}
    outputs = {DIRECTORY / "compose.env": "".join(f"{name}='{value}'\n" for name, value in values.items())}
    for service, port in (("master", 2062), ("monitor", 3062), ("mover", 4062)):
        arguments = [
            f"-Dkeystore.password={values['KEYSTORE_PASSWORD']}",
            f"-Drccsharedsecret.value={values['RCC_SHARED_SECRET']}",
            f"-Dclisharedsecret.value={values['CLI_SHARED_SECRET']}",
            f"-Ddevelopment.api.secret={values['DEVELOPMENT_API_SECRET']}",
            "-javaagent:lib/agents/jolokia-agent-jvm-javaagent.jar="
            f"port={port},host=127.0.0.1,user={values['JOLOKIA_USER']},password={values['JOLOKIA_PASSWORD']}",
        ]
        outputs[DIRECTORY / f"{service}.args"] = "\n".join(arguments) + "\n"
    previous = {}
    for path in outputs:
        if path.is_symlink() or path.exists() and not path.is_file():
            raise ValueError(f"Expected a regular credential file: {path}")
        previous[path] = path.read_text(encoding="utf-8") if path.exists() else None
    moved, written, created_state = [], [], False
    backup = None
    try:
        if paths and recreate:
            backup, moved = backup_keystores(paths)
        if not initialized:
            descriptor = os.open(state, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
            created_state = True
            with os.fdopen(descriptor, "w", encoding="utf-8") as output:
                json.dump(values, output)
                output.write("\n")
        for path, content in outputs.items():
            write_private(path, content)
            written.append(path)
    except BaseException:
        try:
            for path in reversed(written):
                if previous[path] is None:
                    path.unlink()
                else:
                    write_private(path, previous[path])
        finally:
            try:
                if created_state:
                    state.unlink()
            finally:
                restore_keystores(moved)
        raise
    if backup is not None:
        print(f"Original development TLS material backed up in {backup} (private).")
        print("Replacement self-signed certificates will be generated on the next service start.")
    print(f"Development credentials ready in {DIRECTORY} (private; existing values preserved).")


def main():
    parser = argparse.ArgumentParser(description=__doc__, allow_abbrev=False)
    parser.add_argument("--recreate-keystores", action="store_true",
                        help="back up development TLS keystores for regeneration on the next service start")
    parser.add_argument("--services-stopped", action="store_true",
                        help="acknowledge that affected IDE/container services have been stopped")
    arguments = parser.parse_args()
    if arguments.services_stopped and not arguments.recreate_keystores:
        parser.error("--services-stopped requires --recreate-keystores")
    if DIRECTORY.is_symlink():
        raise ValueError("Development credential directory must not be a symbolic link")
    DIRECTORY.mkdir(parents=True, mode=0o700, exist_ok=True)
    DIRECTORY.chmod(0o700)
    lockpath = DIRECTORY / ".setup.lock"
    if lockpath.is_symlink() or lockpath.exists() and not lockpath.is_file():
        raise ValueError(f"Expected a regular setup lock: {lockpath}")
    descriptor = os.open(lockpath, os.O_CREAT | os.O_RDWR | getattr(os, "O_NOFOLLOW", 0), 0o600)
    with os.fdopen(descriptor, "r+") as lock:
        os.fchmod(lock.fileno(), 0o600)
        try:
            fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
        except BlockingIOError:
            raise ValueError("Development credential setup is already running") from None
        prepare(arguments)


if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, json.JSONDecodeError) as error:
        print(f"Cannot prepare development credentials: {error}", file=sys.stderr)
        sys.exit(1)
    except (EOFError, KeyboardInterrupt):
        print("Development credential setup cancelled.", file=sys.stderr)
        sys.exit(1)
