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
