# DID4DCAT Chaincode

## Build
```
$ gradle build
```

## Deploy to Fabric
```
./network.sh deployCC -ccn did4dcat -ccp [path] -ccl java
```

```
$ peer chaincode invoke -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --tls --cafile ${PWD}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n did4dcat --peerAddresses localhost:7051 --tlsRootCertFiles ${PWD}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt --peerAddresses localhost:9051 --tlsRootCertFiles ${PWD}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt -c '{"function":"InitLedger","Args":[]}'
```

```
$ peer chaincode query -C mychannel -n did4dcat -c '{"Args":["GetAllAssets"]}'
```

## How to prepare/package for network

Build

```
$ ./gradlew installDist
```

Package

```
$ mkdir fabric
$ cd ./fabric
$ wget https://github.com/hyperledger/fabric/releases/download/v2.4.7/hyperledger-fabric-linux-amd64-2.4.7.tar.gz
$ tar -zxzf hyperledger-fabric-linux-amd64-2.4.7.tar.gz 
$ export PATH=$PATH:$(realpath ./bin)
$ cd ./config
$ peer lifecycle chaincode package ../../did4dcat.tar.gz --path ../../build/install/did4dcat --label did4dcat_1.0.0 --lang java
$ peer lifecycle chaincode calculatepackageid did4dcat.tar.gz
```
