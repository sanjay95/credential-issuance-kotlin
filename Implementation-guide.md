# **Implementation Guide for Affinidi Credential Issuance**

## **1. Overview**

Credential Issuance Service provides applications with secure methods of issuing and claiming credentials. It implements the **OID4VCI** [OpenID for Verifiable Credential Issuance](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html) protocol, which provides the mechanism for Issuers to issue Verifiable Credentials to Affinidi Vault users and obtain the credentials using the OAuth 2.0 authorisation flow.

The Credential Issuance Service follows a pre-defined Schema to issue Verifiable Credentials to users and cryptographically signs the credentials using wallets. The verifiers/requesters can later use the Issuer’s DID value to verify the credentials.

- **Objective**: Issue Credential Using Affinidi Credential Issuance Service.
- **Stakeholders**: Candidate (who is doing self-service on Avvanz portal), Avvanz web app, Affinidi Services for Credentials Issuance.
- **Scope**: for self-service candidate, issuance to be done from Avvanz.

## **2. System Architecture**

### **Key Components**

1. **Frontend**: Interface for users to request issuance.
2. **Backend (PHP)**:
   - REST APIs to handle requests.
   - Controller for business logic.
   - Database to store issuance records, if required.
3. **Notification System**: Send email/SMS notifications if applicable.

### **Flow Diagram**

When issuing a Verifiable Credential, three main flows happen within the whole process:

- Issuance Configuration

![Issuance Configuration](./cis-image/cis-config1.png)

- Credential Issuance Flow [ claimMode: **TX_CODE** (Default)]

![Credential Issuance Flow](./cis-image/cis-flow.png)

- Credential Offer Claim [ claimMode : **FIXED_HOLDER**]

![Credential Offer Claim](./cis-image/cis-claim.png)

Refer More details on [Affinidi Credential Issuance here](https://docs.affinidi.com/docs/affinidi-elements/credential-issuance/)

## **3. Implementation Steps**

### **Step 1: Affinidi Onboarding**

- Onbording Steps includes

  - Create [Affinidi Vault](https://vault.affinidi.com)
  - Create Developer Account on [Affinidi Portal](https://portal.affinidi.com)
  - Install & Initiatlise Dev Tools - [Affinidi CLI](https://docs.affinidi.com/dev-tools/affinidi-cli/)
  - Create [Schemas](https://docs.affinidi.com/docs/affinidi-elements/schema-builder/#how-to-use-the-schema-builder) for Education, Employment, Address & criminality
  - Create [Wallets](https://docs.affinidi.com/docs/affinidi-elements/wallets/#how-to-create-a-wallet)(PKI for Signing the Credentials)
  - Create [Affinidi Credentials Issuance](./cis-configuration.md) configuration.
  - Create [Machine User](./create-pat.md) for server to server communication.

### **Step 2: Update environment variables**

to enable service to service level communication below backend variables are required.

- **PROJECT_ID**
- **KEY_ID** #Optional, Required if Private/Public key is created from other library except Affinidi dev tools.
- **TOKEN_ID**
- **PASSPHRASE** # Optional. Required if private key is encrypted
- **PRIVATE_KEY**

### **Step 3: Install Dependencies**

Install dependencies for using Affinidi Services

```php
composer require affinidi/affinidi-php-tdk
```

For more details on usages of this library refer [documentation here](https://packagist.org/packages/affinidi/affinidi-php-tdk)

### **Step 4: Refer Env variables in services**

In `service.php` create config for env variables

```php
    'affinidi_tdk' => [
        'api_gateway_url' => env('API_GATEWAY_URL'),
        'token_endpoint' => env('TOKEN_ENDPOINT'),
        'project_Id' => env('PROJECT_ID'),
        'private_key' => env('PRIVATE_KEY'),
        'token_id' => env('TOKEN_ID'),
        'passphrase' => env('PASSPHRASE'),
        'key_id' => env('KEY_ID'),
        'vault_url' => env('VAULT_URL'),
    ]
```

### **Step 5: Create APIs**

- **Controller Layer**: Create Controller in php

```php
php artisan make:controller CredentialController
```

create public function for Issuance using Affinidi Library. Sample example with `credentialTypeId`=`AnyTcourseCertificateV1R0`

```php
use Affinidi\AffinidiTDK;

    public function issueCredential(Request $request)
    {
        try {
            $credentials_request =
                [
                    [
                        "credentialTypeId" => config('services.affinidiCIS.courseCredentialTypeId'),
                        "credentialData" => [
                            "courseID" => "EMP-IT-AUTOMATION-2939302",
                            "course" => [
                                "name" => "IT Automation with Python",
                                "type" => "Professional Certificate",
                                "url" => "",
                                "courseDuration" => "45 Days"
                            ],
                            "learner" => [
                                "name" => "",
                                "email" => "grajesh.c@affinidi.com",
                                "phone" => ""
                            ],
                            "achievement" => [
                                "score" => "100",
                                "grade" => "A"
                            ],
                            "courseMode" => "online",
                            "completionDate" => "08/09/2024"
                        ]
                    ]
                ];


            $apiMethod = '/cis/v1/' . config('services.affinidi_tdk.project_Id') . '/issuance/start';

            $tdk = new AffinidiTDK(
                config('services.affinidi_tdk')
            );

            $data = $tdk->InvokeAPI($apiMethod, [
                'data' => $credentials_request,
                'claimMode' => "TX_CODE"
            ]);

            $json_response = [
                "credentialOfferUri" => $data["credentialOfferUri"],
                "txCode" => $data["txCode"],
                "issuanceId" => $data["issuanceId"],
                "expiresIn" => $data["expiresIn"],
                "vaultLink" => config('services.affinidi_tdk.vault_url') . '/claim?credential_offer_uri=' . $data["credentialOfferUri"],
            ];

            return response()->json($json_response);
        } catch (Exception $e) {
            // Handle or log the error
            error_log('JSON error: ' . $e->getMessage());

            return response()->json(["error" => $e->getMessage()]);
        }
    }
```

### **Step 6: Create Routes**

Create api routes

```php
use App\Http\Controllers\CredentialController;

Route::post('/issue-credential', [CredentialController::class, 'issueCredential']);

```

In case your api.php doesn't exsit, create using below command.

```php
php artisan install:api
```

### **Step 7: Security**

- Affinidi API is protected with Project Level Access Token which is handled by Affinidi Library.
- Project Level Token is `bearer` Token

## **4. Reference Implementation**

You can refer to the [Github](https://github.com/Grajesh-Chandra/php-cis/) repo for running this Application.
