resource "hcloud_ssh_key" "deploy" {
  name       = "meterpoint"
  public_key = file(pathexpand(var.ssh_public_key_path))
}

resource "hcloud_firewall" "meterpoint" {
  name = "meterpoint"

  dynamic "rule" {
    for_each = ["22", "80", "443"]
    content {
      direction  = "in"
      protocol   = "tcp"
      port       = rule.value
      source_ips = ["0.0.0.0/0", "::/0"]
    }
  }
}

resource "hcloud_server" "app" {
  name         = "meterpoint"
  server_type  = var.server_type
  image        = "ubuntu-24.04"
  location     = var.location
  ssh_keys     = [hcloud_ssh_key.deploy.id]
  firewall_ids = [hcloud_firewall.meterpoint.id]

  user_data = templatefile("${path.module}/cloud-init.tftpl", {
    ssh_public_key = trimspace(file(pathexpand(var.ssh_public_key_path)))
    ci_public_key  = trimspace(file(pathexpand(var.ci_public_key_path)))
    volume_device  = hcloud_volume.data.linux_device
    mount_path     = var.data_mount_path
    postgres_uid   = var.postgres_uid
  })
}

resource "hcloud_volume" "data" {
  name     = "meterpoint-data"
  size     = 10
  location = var.location
  format   = "ext4"

  lifecycle {
    prevent_destroy = true
  }
}

resource "hcloud_volume_attachment" "data" {
  volume_id = hcloud_volume.data.id
  server_id = hcloud_server.app.id
  automount = false
}
