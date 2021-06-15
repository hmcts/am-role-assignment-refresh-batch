locals {
  am_rg_name_prefix     = join("-", [var.raw_product, "shared-infrastructure"])
  am_key_vault_rg_name  = var.env == "preview" || var.env == "spreview" ? join("-", [local.am_rg_name_prefix, "aat"]) : join("-", [local.am_rg_name_prefix, var.env])
  am_key_vault_name     = var.env == "preview" || var.env == "spreview" ? join("-", [var.raw_product, "aat"]) : join("-", [var.raw_product, var.env])

  s2s_rg_prefix             = "rpe-service-auth-provider"
  s2s_vault_resource_group  = var.env == "preview" || var.env == "spreview" ? join("-", [local.s2s_rg_prefix, "aat"]) : join("-", [local.s2s_rg_prefix, var.env])
  s2s_key_vault_name        = var.env == "preview" || var.env == "spreview" ? join("-", ["s2s", "aat"]) : join("-", ["s2s", var.env])
}

data "azurerm_key_vault" "am_key_vault" {
  name                = local.key_vault_name
  resource_group_name = local.am_key_vault_rg_name
}

data "azurerm_key_vault" "s2s_key_vault" {
  name                = local.s2s_key_vault_name
  resource_group_name = local.s2s_vault_resource_group
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name          = "microservicekey-am-role-assignment-refresh-batch"
  key_vault_id  = data.azurerm_key_vault.s2s_key_vault.id
}

resource "azurerm_key_vault_secret" "ras_refresh_batch_s2s_secret" {
  name          = "am-role-assignment-refresh-batch-s2s-secret"
  value         = data.azurerm_key_vault_secret.s2s_secret.value
  key_vault_id  = data.azurerm_key_vault.am_key_vault.id
}