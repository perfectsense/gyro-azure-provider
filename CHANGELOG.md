## 0.99.3 (Unreleased)

## 0.99.2 (August 25th, 2020)

ENHANCEMENTS:

* [126](https://github.com/perfectsense/gyro-azure-provider/issues/126): Support subscription from auth properties file.
* [128](https://github.com/perfectsense/gyro-azure-provider/issues/128): Support remote file backend
* [130](https://github.com/perfectsense/gyro-azure-provider/issues/130): Add `exists(String file)` and `copy(String source, String dest)` methods to FileBackend.

## 0.99.1 (July 6th, 2020)

ENHANCEMENTS:

* [51](https://github.com/perfectsense/gyro-azure-provider/issues/51): Add support for Lifecycle for Azure Storage.
* [56](https://github.com/perfectsense/gyro-azure-provider/issues/56): Add support for Azure managed Identities.
* [59](https://github.com/perfectsense/gyro-azure-provider/issues/59): Add support for Role Assignment.
* [62](https://github.com/perfectsense/gyro-azure-provider/issues/62): Add support to manage System Managed Service Identity for VM.
* [66](https://github.com/perfectsense/gyro-azure-provider/issues/66): Add support for Virtual Machine Image.
* [87](https://github.com/perfectsense/gyro-azure-provider/issues/87): Add support for IP Forwarding for Network Interfaces
* [91](https://github.com/perfectsense/gyro-azure-provider/issues/91): Add support for Availability Zones for Application Gateway
* [92](https://github.com/perfectsense/gyro-azure-provider/issues/92): Add support for SKU's in Application Gateway
* [115](https://github.com/perfectsense/gyro-azure-provider/issues/115): Add support for Secrets and Keys in Vault.

ISSUES FIXED:

* [49](https://github.com/perfectsense/gyro-azure-provider/issues/49): Public Ip Address Not Set at Startup for Network Interface.
* [53](https://github.com/perfectsense/gyro-azure-provider/issues/53): Gyro does not clean up auto-created VM disks.
* [55](https://github.com/perfectsense/gyro-azure-provider/issues/55): Azure VirtualMachine should Implement GyroInstance.
* [58](https://github.com/perfectsense/gyro-azure-provider/issues/58): Differentiate between OS Disk and Data Disks in Azure VMs.
* [64](https://github.com/perfectsense/gyro-azure-provider/issues/64): Expose ID fields for CloudBlobContainerResource and CloudQueueResource.
* [72](https://github.com/perfectsense/gyro-azure-provider/issues/72): Expose object id for System Managed Identity on VM and Scale Set resources.
* [74](https://github.com/perfectsense/gyro-azure-provider/issues/70): Fixes storage account finder error.
* [80](https://github.com/perfectsense/gyro-azure-provider/issues/80): Fix NPE on VirtualMachineImageResource external query.
* [82](https://github.com/perfectsense/gyro-azure-provider/issues/82): AvailabilitySet is not applied to VM.
* [85](https://github.com/perfectsense/gyro-azure-provider/issues/85): Private DNS Zone does not associate Network
* [99](https://github.com/perfectsense/gyro-azure-provider/issues/99): VM Resource ID is not suitable for Gyro Instance ID.
* [101](https://github.com/perfectsense/gyro-azure-provider/issues/101): Match casing of AvailabilitySetResource ID on VM.
* [105](https://github.com/perfectsense/gyro-azure-provider/issues/105): Scale Set Custom Data needs to be encoded.
* [107](https://github.com/perfectsense/gyro-azure-provider/issues/107): Application Gateway Backend Health Needs to be accessible.
* [109](https://github.com/perfectsense/gyro-azure-provider/issues/110): VirtualMachine#getGyroInstancePublicIpAddress returns null.

MISC:

* [114](https://github.com/perfectsense/gyro-azure-provider/issues/114): Implement Gyro SSH/List for Scale Sets.
