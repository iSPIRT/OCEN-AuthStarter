# OCEN-AuthStarter
OCEN authentication project for signature verification 


<br>

## ⚙️ Configuration 

  
|   <div style="width:400px">Config</div> | Description |
| ----  |   ---   |  
| Lender Public and Private Keypair Set |  &#8226; Add Lender Public and Private Keypair Set in lender_private_public_keypair_set.json file located at resources folder <br>&#10; &#8226; Reference to create Public and Private Key Pair Set - https://mkjwk.org/|
| Lender Client Id | &#8226; Add your lender client id at lender.client.id in application.properties file located at resources folder |
| Lender Client Secret | &#8226; Add your lender client secret at lender.client.secret in application.properties file located at resources folder |
| LA Participant Id | &#8226; Add your LA participant id at la.participant.id in application.properties file located at resources folder |


<br>

## 🌐 API

|   <div style="width:400px">API</div> | Description |
| ----  |   ---   |  
| Health Test |  &#8226; To check whether your server is up and running|
| Generate Signature | &#8226; Utility API to generate signature <br>&#10; &#8226; Add LA Public and Private Keypair Set in the header with header name as **JWK_KEY_SET** <br>&#10; &#8226; Add Request Body for which you want to create signature |
| Create Loan Application | &#8226; Add signature as **x-jws-signature** header name which you have generated using Generate Signature API, make sure the body passed in the Generate Signature API and Create Loan Application is same otherwise due to body mismatch signature verification will fail|
