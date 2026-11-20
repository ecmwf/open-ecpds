#!/bin/bash
set -euo pipefail

allow_missing_ca=false
if [[ ${1:-} == --allow-missing-ca ]]; then
    allow_missing_ca=true
    shift
fi
if [[ $# -ne 2 ]]; then
    echo "Usage: $0 [--allow-missing-ca] TLS_DIRECTORY OFFLINE_OPERATOR_DIRECTORY" >&2
    exit 2
fi

tls_dir=$1
operator_dir=$2
if [[ -f "$tls_dir/tls.key" && -s "$tls_dir/tls.key" && -f "$tls_dir/tls.crt" && -s "$tls_dir/tls.crt" ]] &&
   { [[ -f "$tls_dir/ca.crt" && -s "$tls_dir/ca.crt" ]] ||
     [[ "$allow_missing_ca" == true && ! -e "$tls_dir/ca.crt" && ! -L "$tls_dir/ca.crt" ]]; }; then
    echo "Using existing OpenBao TLS files in $tls_dir."
    exit 0
fi

for file in "$tls_dir/tls.key" "$tls_dir/tls.crt" "$tls_dir/ca.crt" \
            "$operator_dir/ca.key" "$operator_dir/ca.crt"; do
    if [[ -e "$file" || -L "$file" ]]; then
        echo "Incomplete OpenBao TLS setup; refusing to overwrite $file." >&2
        exit 1
    fi
done

if ! command -v openssl >/dev/null; then
    echo "OpenSSL is required to generate local OpenBao certificates." >&2
    exit 1
fi

umask 077
mkdir -p "$tls_dir" "$operator_dir"
chmod 0755 "$tls_dir"
chmod 0700 "$operator_dir"
tls_work="$tls_dir/.generate"
if ! mkdir "$tls_work"; then
    echo "OpenBao TLS generation is already in progress or left $tls_work behind." >&2
    exit 1
fi
ca_work=
cleanup() {
    rm -f "$tls_work/tls.key" "$tls_work/tls.crt" "$tls_work/ca.crt" "$tls_work/server.csr" "$tls_work/server.ext"
    rmdir "$tls_work"
    if [[ -n "$ca_work" ]]; then
        rm -f "$ca_work/ca.key" "$ca_work/ca.crt" "$ca_work/ca.srl"
        rmdir "$ca_work"
    fi
}
trap cleanup EXIT
ca_work=$(mktemp -d "$operator_dir/.generate.XXXXXXXX")

openssl ecparam -name prime256v1 -genkey -noout -out "$ca_work/ca.key"
openssl req -new -x509 -key "$ca_work/ca.key" -sha256 -days 3650 \
    -subj '/CN=OpenECPDS standalone OpenBao CA' \
    -addext 'basicConstraints=critical,CA:TRUE' \
    -addext 'keyUsage=critical,keyCertSign,cRLSign' \
    -out "$ca_work/ca.crt"
openssl ecparam -name prime256v1 -genkey -noout -out "$tls_work/tls.key"
openssl req -new -key "$tls_work/tls.key" -subj '/CN=openbao' -out "$tls_work/server.csr"
printf 'subjectAltName=DNS:openbao,DNS:localhost,IP:127.0.0.1\nextendedKeyUsage=serverAuth\nkeyUsage=critical,digitalSignature\n' \
    > "$tls_work/server.ext"
openssl x509 -req -in "$tls_work/server.csr" -CA "$ca_work/ca.crt" \
    -CAkey "$ca_work/ca.key" -CAcreateserial -days 397 -sha256 \
    -extfile "$tls_work/server.ext" -out "$tls_work/tls.crt"
cp "$ca_work/ca.crt" "$tls_work/ca.crt"
chmod 0644 "$tls_work/ca.crt" "$tls_work/tls.crt"
chmod 0640 "$tls_work/tls.key"
if [[ ${OPENBAO_CONTAINER_ENGINE:-} == podman ]] && (( EUID != 0 )); then
    podman unshare chown 100:1000 "$tls_work/tls.key"
elif (( EUID == 0 )); then
    chown 100:1000 "$tls_work/tls.key"
else
    if ! command -v sudo >/dev/null; then
        echo "Install sudo or provision a TLS key readable by OpenBao's container identity." >&2
        exit 1
    fi
    sudo chown 100:1000 "$tls_work/tls.key"
fi

# Publish without replacing files even if an operator supplied them during generation.
ln "$ca_work/ca.key" "$operator_dir/ca.key"
ln "$ca_work/ca.crt" "$operator_dir/ca.crt"
ln "$tls_work/ca.crt" "$tls_dir/ca.crt"
ln "$tls_work/tls.crt" "$tls_dir/tls.crt"
mv -n "$tls_work/tls.key" "$tls_dir/tls.key"
if [[ -e "$tls_work/tls.key" ]]; then
    echo "Refusing to overwrite $tls_dir/tls.key." >&2
    exit 1
fi
echo "Created local OpenBao TLS files in $tls_dir. Keep the CA key in $operator_dir offline."
