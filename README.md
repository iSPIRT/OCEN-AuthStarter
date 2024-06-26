# OCEN-AuthStarter
Sample Loan Agent and Lender Application with signature and auth token verification and Async CreateLoanApplicationRequest and CreateLoanApplicationResponse
<br>
<br>

## Bootstrap Process
  1. Get clientId an clientSecret
  2. Get roleId from registry
  3. Get productId and productNetworkId from registry
  4. Update the base url and public key on the registry against the role
<br>
<br>

## Properties Configuration

### Common

```
// API ClientID and Secret for credential based Authorization Token Generation
client.id=f59adada-ea8d-4f8c-b511-4161fd216c49
client.secret=LGweLUAP7OEryjiQQK4byGppswSJYIph

// For generating token 
ocen.token.generation.url=https://auth.ocen.network/realms/dev/protocol/openid-connect/token

// For interacting with registry apis  
ocen.registry.base.url=https://dev.ocen.network/service

// For verifying the Auth Token
ocen.api.security.jwt.issuer=${API_TOKEN_ISSUER:https://auth.ocen.network/realms/dev}

// For updating the heartbeat events
ocen.heartbeat.event.url=http://analytics-dev.ocen.network/ocen/v4/event
```
### Loan Agent Application

`loan_agent_private_public_keypair_set.json` contains KeySet for signing request used by the LA Application

### Lender Application

`lender_private_public_keypair_set.json` contains KeySet for signing response used by the Lender Application

Reference to create Public and Private Key Pair Set - https://mkjwk.org/
<br>
<br>

## Sample Auth Flow 
![Auth - Create Loan Application Flow](https://github.com/iSPIRT/OCEN-AuthStarter/assets/40620782/9355d4c6-80ff-4782-830a-2df7734c6096)
<br>
<br>

## 🌐 API
|   <div style="width:400px">API</div> | Description |
| ----  |   ---   |  
| Health Test |  &#8226; To check whether your server is up and running|
| Generate Signature | &#8226; Utility API to generate signature <br>&#10; &#8226; Add Public and Private Keypair Set in the header with header name as **JWK_KEY_SET** <br>&#10; &#8226; Add Request Body for which you want to create signature |
| Trigger Create Loan Application | &#8226; This API triggers create loan application flow from Loan Agent Application |

<br>

## Instructions to Build/Run Applications

There are two Applications 
  - Lender (port :8084)
  - Loan Agent (port: 8085)

Minimum Java 11 Version Required

In main directory folder run below commands for running the applications
- `./mvnw clean install generate-resources` to build
- ` java -jar lender/target/lender-1.0-SNAPSHOT.jar` to run lender application
- ` java -jar loanagent/target/loanagent-1.0-SNAPSHOT.jar` to run loan agent application

Postman collection present with sample api calls 
