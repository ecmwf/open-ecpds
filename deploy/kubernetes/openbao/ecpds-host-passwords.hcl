path "ecpds/data/host-passwords/*" {
  capabilities = ["create", "read"]
}

path "ecpds/destroy/host-passwords/*" {
  capabilities = ["update"]
}

path "ecpds/metadata/host-passwords/*" {
  capabilities = ["read"]
}

path "auth/token/renew-self" {
  capabilities = ["update"]
}
