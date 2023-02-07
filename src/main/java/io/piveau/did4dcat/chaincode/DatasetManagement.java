/*
 * SPDX-License-Identifier: Apache-2.0
 */

package io.piveau.did4dcat.chaincode;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;
import org.json.JSONObject;

@Contract(
        name = "did4dcat",
        info = @Info(
                title = "DID4DCAT Chaincode",
                description = "This is the DID4DCAT chaincode to manage persistent identifiers",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "fabian.kirstein@fokus.fraunhofer.de",
                        name = "Fabian Kirstein",
                        url = "https://fabiankirstein.de")))
@Default
public final class DatasetManagement implements ContractInterface {

    private final Genson genson = new Genson();

    private enum DatasetManagementError {
        DATASET_NOT_FOUND,
        DATASET_ALREADY_EXISTS,
        USER_NOT_AUTHORIZED
    }

    /**
     * Creates some initial datasets on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {

    }


    /**
     * Creates a new dataset on the ledger.
     *
     * @param ctx the transaction context
     * @param didDocument the DID document
     * @return the created dataset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Dataset CreateDataset(final Context ctx, final String didDocument) {
        JSONObject didDocumentJson = new JSONObject(didDocument);
        String did = didDocumentJson.getString("id");

        ChaincodeStub stub = ctx.getStub();
        String datasetJSON = stub.getStringState(did);

        if (!(datasetJSON == null || datasetJSON.isEmpty())) {
            String errorMessage = String.format("Dataset %s already exist", did);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, DatasetManagementError.DATASET_ALREADY_EXISTS.toString());
        }

        ClientIdentity clientIdentity = ctx.getClientIdentity();
        DatasetOwner owner = convertClientIdentityToOwner(clientIdentity);

        Dataset dataset = new Dataset(did, didDocument, owner);

        //Use Genson to convert the Asset into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(dataset);
        stub.putStringState(dataset.getDid(), sortedJson);
        return dataset;
    }

    /**
     * Updates an existing dataset on the ledger.
     *
     * @param ctx the transaction context
     * @param didDocument the DID document
     * @return the created dataset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Dataset UpdateDataset(final Context ctx, final String didDocument) {
        JSONObject didDocumentJson = new JSONObject(didDocument);
        String did = didDocumentJson.getString("id");

        ChaincodeStub stub = ctx.getStub();
        String datasetJSON = stub.getStringState(did);

        if (datasetJSON == null || datasetJSON.isEmpty()) {
            String errorMessage = String.format("Dataset %s does not exist", did);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, DatasetManagementError.DATASET_NOT_FOUND.toString());
        }

        Dataset oldDataset = genson.deserialize(datasetJSON, Dataset.class);

        ClientIdentity clientIdentity = ctx.getClientIdentity();
        DatasetOwner owner = convertClientIdentityToOwner(clientIdentity);

        if (!oldDataset.getOwner().equals(owner)) {
            String errorMessage = "User is not authorized";
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, DatasetManagementError.USER_NOT_AUTHORIZED.toString());
        }

        JSONObject oldDidDoc = new JSONObject(oldDataset.getDidDocument());
        String issued = oldDidDoc.getString("issued");

        JSONObject didDoc =  new JSONObject(didDocument);
        didDoc.put("issued", issued);

        Dataset dataset = new Dataset(did, didDoc.toString(), owner);

        //Use Genson to convert the Asset into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(dataset);
        stub.putStringState(dataset.getDid(), sortedJson);
        return dataset;
    }

    /**
     * Retrieves a dataset with the specified ID from the ledger.
     *
     * @param ctx the transaction context
     * @param did the did of the dataset
     * @return the dataset found on the ledger if there was one
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Dataset ReadDataset(final Context ctx, final String did) {
        ChaincodeStub stub = ctx.getStub();
        String datasetJSON = stub.getStringState(did);

        if (datasetJSON == null || datasetJSON.isEmpty()) {
            String errorMessage = String.format("Dataset %s does not exist", did);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, DatasetManagementError.DATASET_NOT_FOUND.toString());
        }

        Dataset dataset = genson.deserialize(datasetJSON, Dataset.class);
        return dataset;
    }

    /**
     * Retrieves all datasets of a user from the ledger.
     *
     * @param ctx the transaction context
     * @return array of assets found on the ledger
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetMyDatasets(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Dataset> queryResults = new ArrayList<Dataset>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        ClientIdentity clientIdentity = ctx.getClientIdentity();
        DatasetOwner owner = convertClientIdentityToOwner(clientIdentity);

        if (results != null) {
            for (KeyValue result: results) {
                Dataset dataset = genson.deserialize(result.getStringValue(), Dataset.class);
                if (dataset.getOwner().equals(owner)) {
                    System.out.println(dataset);
                    queryResults.add(dataset);
                }
            }
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

    /**
     * Retrieves all datasets from the ledger.
     *
     * @param ctx the transaction context
     * @return array of assets found on the ledger
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllDatasets(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Dataset> queryResults = new ArrayList<Dataset>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        if (results != null) {
            for (KeyValue result: results) {
                Dataset dataset = genson.deserialize(result.getStringValue(), Dataset.class);
                System.out.println(dataset);
                queryResults.add(dataset);
            }
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

    static DatasetOwner convertClientIdentityToOwner(final ClientIdentity clientIdentity) {
        Principal subjectDN = clientIdentity.getX509Certificate().getSubjectDN();
        String commonName = null;

        for (String each : subjectDN.getName().split(",\\s*")) {
            if (each.startsWith("CN=")) {
                commonName = each.substring(3);
            }
        }

        String userId = commonName;
        String mspId = clientIdentity.getMSPID();
        return new DatasetOwner(userId, mspId);
    }

}
