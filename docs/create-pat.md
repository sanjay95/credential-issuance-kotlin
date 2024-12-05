# Setup Personal Access Token (PAT)

To create a personal Access Token,use Affinidi CLI

## Install Affinidi CLI

Follow the guide below if you haven’t installed yet

1. Install Affinidi CLI using NPM

`npm install -g @affinidi/cli`

2. Verify that the installation is successful

`affinidi --version`

> Note that Affinidi CLI requires Node version 18 and above.

### Create Personal Access Token (PAT)

1. Log in to Affinidi CLI by running

```sh
affinidi start
```

2. Once logged in successfully, create token by running below command

```sh
affinidi token create-token
```

Follow the instruction

```
 ? Enter the value for name avvanzPAT
 ? Generate a new keypair for the token? yes
 ? Enter a passphrase to encrypt the private key. Leave it empty for no encryption ******
 ? Add token to active project and grant permissions? yes
 ? Enter the allowed resources, separated by spaces. Use * to allow access to all project resources *
 ? Enter the allowed actions, separated by spaces. Use * to allow all actions *

```

Sample response:

```json
 Creating Personal Access Token... Created successfully!
 Adding token to active project... Added successfully!
 Granting permissions to token... Granted successfully!
 {
   "id": "**********",
   "ari": "ari:iam:::token/**********",
   "ownerAri": "ari:iam:::user/**********",
   "name": "workshopPAT",
   "scopes": [
     "openid",
     "offline_access"
   ],
   "authenticationMethod": {
     "type": "PRIVATE_KEY",
     "signingAlgorithm": "RS256",
     "publicKeyInfo": {
       "jwks": {
         "keys": [
           {
             "use": "sig",
             "kty": "RSA",
             "kid": "**********",
             "alg": "RS256",
             "n": "**********",
             "e": "AQAB"
           }
         ]
       }
     }
   }
 }

 Use the projectId, tokenId, privateKey, and passphrase (if provided) to use this token with Affinidi TDK
 {
   "tokenId": "*******",
   "projectId": "*******",
   "privateKey": "*******",
   "passphrase": "******"
 }
 ›   Warning:
 ›   Please save the privateKey and passphrase (if provided) somewhere safe. You will not be able to view them again.
 ›

```

For more details on the command run below command

```sh
affinidi token create-token --help
```

3. Update `.env` file for the below variables with values obtained in above response

   ```
   PROJECT_ID=""
   KEY_ID="" # optional. required if different key_id is used or else Token_Id=Key_Id
   TOKEN_ID=""
   PASSPHRASE="" # Optional. Required if private key is encrypted
   PRIVATE_KEY=""
   ```

Learn more command from our [Documentation](https://docs.affinidi.com/dev-tools/affinidi-cli/manage-token)
