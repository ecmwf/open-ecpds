ui = false
api_addr = "https://openbao:8200"
cluster_addr = "https://openbao:8201"

storage "raft" {
  path = "/openbao/file"
  node_id = "ecpds-openbao"
}

listener "tcp" {
  address = "0.0.0.0:8200"
  cluster_address = "0.0.0.0:8201"
  tls_cert_file = "/openbao/tls/tls.crt"
  tls_key_file = "/openbao/tls/tls.key"
}
