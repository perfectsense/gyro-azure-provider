/**
 * Azure
 * -----
 *
 * The Azure provider implements support for Azure cloud provider.
 *
 * Authentication
 * ++++++++++++++
 *
 * This provider expects credentials to be provided using a file containing the
 * credentials of a `Azure service principal <https://https://docs.microsoft.com/en-us/azure/active-directory/develop/app-objects-and-service-principals>`_ in the following format
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
