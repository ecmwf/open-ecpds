vault {
  address = "https://openbao:8200"
  ca_cert = "/etc/openbao-ca/ca.crt"
}

auto_auth {
  method "kubernetes" {
    mount_path = "auth/kubernetes"
    config = {
      role       = "ecpds-master"
      token_path = "/var/run/openbao-identity/token"
    }
  }

  sink "file" {
    config = {
      path = "/run/ecpds/openbao/token"
      mode = 384 # 0600; the sink expects an integer.
    }
  }
}
