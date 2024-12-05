# Setup Credential Issuance Configuration

To issue a Verifiable Credential, it is required to setup the **Issuance Configuration** on your project, where you select the **issuing wallet** and **supported schemas** to create a credential offer that the application issue

You can easily do this using the [Affinidi Portal](https://portal.affinidi.com)

1. Go to [Affinidi Portal](https://portal.affinidi.com).

2. Open `Wallets` menu under the `Tools` section and click on `Create Wallet` with any name (e.g. `MyWallet`) and DID method as `did:key`.
   ![alt text](./cis-image/wallet-create.png)

For more information, refer to the [Wallets documentation](https://docs.affinidi.com/dev-tools/wallets)

3. Go to `Credential Issuance Service` under `Services` section.

4. Click on `Create Configuration` and set the following fields:

   `Name of configuration`: Any name for configuration e.g. `Avvanz CIS Config`
   `Description - optional`: details description
   `Issuing Wallet`: Select Wallet Created previous step
   `Lifetime of Credential Offer` as `600`

5. Add schemas by clicking on "Add new item" under `Supported Schemas`

Schema 1 :

- _Schema_ as `Manual Input`,
- _Credential Type ID_ as `TPersonalInformationVerificationV1R0`
- _JSON Schema URL_ as `https://schema.affinidi.io/TPersonalInformationVerificationV1R0.json`
- _JSDON-LD Context URL_ = `https://schema.affinidi.io/TPersonalInformationVerificationV1R0.jsonld`

Schema 2 :

- _Schema_ as `Manual Input`,
- _Credential Type ID_ as `TAddressVerificationV1R0`
- _JSON Schema URL_ as `https://schema.affinidi.io/TAddressVerificationV1R0.json`
- _JSDON-LD Context URL_ = `https://schema.affinidi.io/TAddressVerificationV1R0.jsonld`

Schema 3 :

- _Schema_ as `Manual Input`,
- _Credential Type ID_ as `TEmploymentVerificationV1R1`
- _JSON Schema URL_ as `https://schema.affinidi.io/TEmploymentVerificationV1R1.json`
- _JSDON-LD Context URL_ = `https://schema.affinidi.io/TEmploymentVerificationV1R1.jsonld`

Schema 4 :

- _Schema_ as `Manual Input`,
- _Credential Type ID_ as `TEducationVerificationV1R0`
- _JSON Schema URL_ as `https://schema.affinidi.io/TEducationVerificationV1R0.json`
- _JSDON-LD Context URL_ = `https://schema.affinidi.io/TEducationVerificationV1R0.jsonld`

Sample Configuration
![alt text](./cis-image/cis-config.png)
