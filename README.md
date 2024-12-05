# Affinidi Crdential Issuance (CIS) using  kotlin 


This is a reference backend app created to demonstarte integration of Affinidi CIS to Kotlin and springboot develoment framework. 


## Prerequisite 

1. Make sure you have [JAVA 21](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html) installed on your machine. Run the following command in a terminal session to check the java version

```sh 
   java --version 
```
You should get an output as below. If not, please reinstall java21 using the link above
```sh
OpenJDK Runtime Environment Corretto-21.0.5.11.1 (build 21.0.5+11-LTS)
```
2. Ensure that your JAVA_HOME environment variable is set to the installation directory
```sh
export JAVA_HOME="/Library/Java/JavaVirtualMachines/amazon-corretto-21.jdk/Contents/Home"
```
3. Optionally you can add this entry to your respective profile (~/.bashrc or ~/.zshrc)


## Getting Started

Setting up the reference app is easy with [IntelliJ IDEA](https://www.jetbrains.com/idea/)
Just open the project `credential-issuance` from credential-issuance-kotlin/credential-issuance in INtelliJ Idea and you are ready to build and run.

just follow these steps for VS code:
Open folder `credential-issuance` from credential-issuance-kotlin/credential-issuance in VS code or terminal.

1.Update application environment variables 

1. Create a .env file for your application to hold environment variables

```sh
cp .env.example .env
```

2. Install the dependencies

```sh
./gradlew clean build  
```

3. Click here to [Set up your Personnel Access Token to interact with Affinidi services](#setup-personal-access-token)

4. Click here to [Set up your Credential Issuance Configuration](#setup-credential-issuance-configuration)



## Build and run the project:

```sh
./gradlew clean build 
./gradlew run
```

Then use any REST client to call API(POST): [http://localhost:8080/issuance](http://localhost:8080/issuance) to start issuing the credential

A very simple payload for POST 

```json
{
    "userDID": "did:key:<YOUR DID>"
}
```




## Setup Personal Access Token

Follow [this guide](./docs/create-pat.md) to set up your Personnel Access Token


# Setup Credential Issuance Configuration

To issue a Verifiable Credential, it is required to setup the **Issuance Configuration** on your project, where you select the **issuing wallet** and **supported schemas** to create a credential offer that the application issue

You can easily do this using the [Affinidi Portal](https://portal.affinidi.com)

1. Go to [Affinidi Portal](https://portal.affinidi.com).

2. Open `Wallets` menu under the `Tools` section and click on `Create Wallet` with any name (e.g. `MyWallet`) and DID method as `did:key`.
    ![alt text](./docs/cis-image/wallet-create.png)

For more information, refer to the [Wallets documentation](https://docs.affinidi.com/dev-tools/wallets)

3. Go to `Credential Issuance Service` under `Services` section.

4. Click on `Create Configuration` and set the following fields:

    `Issuing Wallet`: Select Wallet Created previous step
    `Lifetime of Credential Offer` as `600`
    
5. Add schemas by clicking on "Add new item" under `Supported Schemas`

Schema 1 : 
- *Schema* as `Manual Input`, 
- *Credential Type ID* as `healthProfile`
- *JSON Schema URL* as [https://schema.affinidi.io/TtestschemaIsusdfsfsfdV1R0.json](https://schema.affinidi.io/ThealthProfileV1R0.json)
- *JSDON-LD Context URL* = [https://schema.affinidi.io/TtestschemaIsusdfsfsfdV1R0.jsonld](https://schema.affinidi.io/ThealthProfileV1R0.jsonld)

Sample Configuration
![alt text](./docs/cis-image/cis-configuration.png)
