ui = false
disable_mlock = true
api_addr = "https://127.0.0.1:8200"
cluster_addr = "https://127.0.0.1:8201"

storage "raft" {
  path = "/data/lib/openbao/raft"
  # Retain this node ID for existing standalone vaults.
  node_id = "standalone-demo"
}

listener "tcp" {
  address = "127.0.0.1:8200"
  cluster_address = "127.0.0.1:8201"
  tls_cert_file = "/data/lib/openbao/tls/server.crt"
  tls_key_file = "/data/lib/openbao/tls/server.key"
}
