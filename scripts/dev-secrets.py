#!/usr/bin/env python3
"""Prepare private, persistent credentials for the local Compose and IDE profiles."""

import json
import os
from pathlib import Path
import re
import secrets
import sys
import tempfile


ROOT = Path(__file__).resolve().parents[1]
DIRECTORY = ROOT / "run/etc/ecpds/development-secrets"
NAMES = (
    "MYSQL_ROOT_PASSWORD", "KEYSTORE_PASSWORD", "RCC_SHARED_SECRET",
    "CLI_SHARED_SECRET", "JOLOKIA_PASSWORD", "HAWTIO_PASSWORD", "DEVELOPMENT_API_SECRET",
)


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


def main():
    if DIRECTORY.is_symlink():
        raise ValueError("Development credential directory must not be a symbolic link")
    DIRECTORY.mkdir(parents=True, mode=0o700, exist_ok=True)
    DIRECTORY.chmod(0o700)
    state = DIRECTORY / "secrets.json"
    if state.is_symlink() or state.exists() and not state.is_file():
        raise ValueError("Development credential state must be a regular file")
    if state.exists():
        state.chmod(0o600)
        values = json.loads(state.read_text(encoding="utf-8"))
    else:
        data = ROOT / "run/var/lib/ecpds/database"
        if any((data / name).exists() for name in ("mysql", "ecpds")) and not os.getenv("MYSQL_ROOT_PASSWORD"):
            raise ValueError("Existing database: set MYSQL_ROOT_PASSWORD to its current root password before setup")
        if any((ROOT / f"run/etc/ecpds/{service}/ecpds-{service}.pfx").exists()
               for service in ("monitor", "mover")) and not os.getenv("KEYSTORE_PASSWORD"):
            raise ValueError("Existing keystore: set KEYSTORE_PASSWORD to its current password before setup")
        values = {name: os.getenv(name) or secrets.token_hex(32) for name in NAMES}
        values["JOLOKIA_USER"] = os.getenv("JOLOKIA_USER") or "jolokia"
        values["HAWTIO_USER"] = os.getenv("HAWTIO_USER") or "hawtio"

    for name in (*NAMES, "JOLOKIA_USER", "HAWTIO_USER"):
        value = values.get(name)
        if not isinstance(value, str) or not re.fullmatch(r"[A-Za-z0-9._~!@%+/=-]+", value):
            raise ValueError(f"{name} must be nonempty and contain only letters, digits or ._~!@%+/=-")
    values = {name: values[name] for name in (*NAMES, "JOLOKIA_USER", "HAWTIO_USER")}
    if not state.exists():
        # Exclusive creation keeps concurrent setup processes from choosing different secrets.
        descriptor = os.open(state, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
        with os.fdopen(descriptor, "w", encoding="utf-8") as output:
            json.dump(values, output)
            output.write("\n")
    write_private(DIRECTORY / "compose.env", "".join(f"{name}='{value}'\n" for name, value in values.items()))
    for service, port in (("master", 2062), ("monitor", 3062), ("mover", 4062)):
        arguments = [
            f"-Dkeystore.password={values['KEYSTORE_PASSWORD']}",
            f"-Drccsharedsecret.value={values['RCC_SHARED_SECRET']}",
            f"-Dclisharedsecret.value={values['CLI_SHARED_SECRET']}",
            f"-Ddevelopment.api.secret={values['DEVELOPMENT_API_SECRET']}",
            "-javaagent:lib/agents/jolokia-agent-jvm-javaagent.jar="
            f"port={port},host=127.0.0.1,user={values['JOLOKIA_USER']},password={values['JOLOKIA_PASSWORD']}",
        ]
        write_private(DIRECTORY / f"{service}.args", "\n".join(arguments) + "\n")
    print(f"Development credentials ready in {DIRECTORY} (private; existing values preserved).")


if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, json.JSONDecodeError) as error:
        print(f"Cannot prepare development credentials: {error}", file=sys.stderr)
        sys.exit(1)
