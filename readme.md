GraalVM.JS Scripting Provider extension for Keycloak
----

This extension replaces the deprecated Nashorn based Scripting Provider extension in Keycloak. with a GraalVM.JS based one.

# Build
To build the extension, run the following command:
```
mvn clean package
```
This creates an `.ear` file which contains the required libraries.

# Deploy
```
cp graalvm-js-scripting-provider-bundle/target/graalvm-js-scripting-provider-bundle-*.ear /PATH/TO/KEYCLOAK/standalone/deployments/
```

# Enable Scripting

In order to enable scripting, you need to enable the `scripts` feature. One way to do this is via System Property:
```  
-Dkeycloak.profile.feature.scripts=enabled
```

If you want to create and edit script-based ProtocolMappers and Authenticators via the Admin-Console, you also need to enable 
the `upload_scripts` feature.
```
-Dkeycloak.profile.feature.upload_scripts=enabled
```

Note, that it is not recommended to enable the `upload_scripts` feature in production. 