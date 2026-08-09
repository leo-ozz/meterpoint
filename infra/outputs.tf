output "server_ipv4" {
  value = hcloud_server.app.ipv4_address
}

output "data_mount_path" {
  value = "/mnt/HC_Volume_${hcloud_volume.data.id}"
}

output "data_device_path" {
  value = hcloud_volume.data.linux_device
}
