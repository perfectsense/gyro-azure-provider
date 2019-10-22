/**
 * Azure
 * -----
 *
 * The Azure provider implements support for Azure cloud provider.
 *
 * Usage
 * +++++
 *
 * The Azure provider is implemented as a plugin. To use it add the plugin to your init file.
 * It uses the format ``@plugin: gyro:gyro-azure-provider:<version>``.
 *
 * .. code:: shell
 *
 *     {@literal @}repository: 'https://artifactory.psdops.com/gyro-releases'
 *     {@literal @}plugin: 'gyro:gyro-azure-provider:0.99.0'
 *
 * This lets Gyro load the Azure provider plugin and lets you start managing Azure resources using Gyro.
 *
 * Authentication
 * ++++++++++++++
 *
 * This provider expects credentials to be provided using a file containing the
 * credentials of a `Azure service principal <https://docs.microsoft.com/en-us/azure/active-directory/develop/app-objects-and-service-principals>`_ in the following format
 *
 * .. code:: shell
 *
 *      # sample management library properties file
 *      subscription=########-####-####-####-############
 *      client=########-####-####-####-############
 *      key=XXXXXXXXXXXXXXXX
 *      tenant=########-####-####-####-############
 *      managementURI=https\://management.core.windows.net/
 *      baseURL=https\://management.azure.com/
 *      authURL=https\://login.windows.net/
 *      graphURL=https\://graph.windows.net/
 *
 * The simplest way to create a *Service Principal* is using the azure cli. See `Azure CLI <https://docs.microsoft.com/en-us/cli/azure/get-started-with-azure-cli?view=azure-cli-latest>`_ for more info related to installing the cli and setting it up.
 *
 * Once set up, run `az login` to login to the Azure Portal and have an active session. If you have multiple subscriptions on your Azure Portal, use `az login --subscription <Subscription_ID>` to login to a specific subscription.
 *
 * Run `az ad sp create-for-rbac --name <Service_Principal_Name>` to create a *Service Principal* with the following output.
 *
 * .. code:: shell
 *
 *      {
 *          "appId": ########-####-####-####-############,
 *          "displayName": <Service_Principal_Name>,
 *          "name": "http://<Service_Principal_Name>",
 *          "password": ########-####-####-####-############,
 *          "tenant": ########-####-####-####-############
 *      }
 *
 * In the properties file, put the value of `appId` in the `client`, `password` in the `key` and `tenant` in the `tenant` field.
 *
 * Fill in the the `subscription` with the subscription value used during login. You can also run `az account show` to view this value displayed as `ID`.
 *
 * For more info refer `Azure file based authentication <https://docs.microsoft.com/en-us/azure/java/java-sdk-azure-authenticate#file-based-authentication-preview>`_
 *
 * Then use the credential file path in ``.gyro/init.gyro`` in your Gyro project along with
 * the region you want to use these credentials in and the log level.
 *
 * .. code:: shell
 *
 *     {@literal @}credentials 'azure::credentials'
 *         log-level: 'basic'
 *         region: 'westus'
 *         credential-file-path: '<azure_credentials_file_path>'
 *     {@literal @}end
 *
 * To use more than one region, provide a name for your credentials. When a name is not provided
 * then the credentials because the ``default``.
 *
 * .. code:: shell
 *
 *     {@literal @}credentials 'azure::credentials' eastus
 *         log-level: 'basic'
 *         region: 'eastus'
 *         credential-file-path: '<azure_credentials_file_path>'
 *     {@literal @}end
 *
 * To use a non-default set of credentials you must explicitly use them in your resource definitions:
 *
 * .. code:: shell
 *
 *     azure::virtual-machine web-server
 *         vm-size-type: 'STANDARD_G1'
 *
 *         {@literal @}uses-credentials: 'eastus'
 *     end
 *
 */
@DocNamespace("azure")
@Namespace("azure")
package gyro.azure;

import gyro.core.Namespace;
import gyro.core.resource.DocNamespace;
