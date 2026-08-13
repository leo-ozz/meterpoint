variable "server_type" {
  description = "cx33 for x86"
  type        = string
  default     = "cx33"
}

variable "location" {
  type    = string
  default = "nbg1"
}

variable "ssh_public_key_path" {
  type    = string
  default = "~/.ssh/meterpoint.pub"
}

variable "ci_public_key_path" {
  description = "Public half of the CI deploy keypair."
  type        = string
  default     = "~/.ssh/meterpoint_ci.pub"
}

variable "data_mount_path" {
  description = "Mount point for the data volume. Must match MOUNT_PATH in the server env file."
  type        = string
  default     = "/mnt/meterpoint"
}

variable "postgres_uid" {
  description = "uid of the postgres user inside the Postgres image. Bind-mounted host directories keep host ownership, so the data directory must be chowned to this."
  type        = number
  default     = 999
}
