/*
 * SPDX-License-Identifier: Apache-2.0
 */

package io.piveau.did4dcat.chaincode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class DatasetManagementTest {

    private final Genson genson = new Genson();

    private final JSONObject didDocument = new JSONObject()
            .put("@context", new JSONArray().put("https://www.w3.org/ns/did/v1").put("https://did4dcat.org/context/v1"))
            .put("id", "did:dcat:dataset:123456")
            .put("controller", "did:dcat:provider:example-provider")
            .put("@url", new JSONObject().put("@id", "http://data.europa.eu/88u/dataset/europeana-aggregated-dataset.rdf"))
            .put("issued", "2022-09-19T18:05:20.997")
            .put("modified", "2022-09-20T20:05:20.997")
            .put("hash", new JSONObject().put("value", "f4389t356t7zw457zn547zw4").put("alg", "URDNA2015"));


    private final class MockKeyValue implements KeyValue {

        private final String key;
        private final String value;

        MockKeyValue(final String key, final String value) {
            super();
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getStringValue() {
            return this.value;
        }

        @Override
        public byte[] getValue() {
            return this.value.getBytes();
        }

    }

    private final class MockAssetResultsIterator implements QueryResultsIterator<KeyValue> {

        private final List<KeyValue> assetList;

        MockAssetResultsIterator() {
            super();

            assetList = new ArrayList<KeyValue>();

            JSONObject did1 = new JSONObject(didDocument.toString());
            did1.put("id", "did:dcat:dataset:111111");
            JSONObject did2 = new JSONObject(didDocument.toString());
            did2.put("id", "did:dcat:dataset:222222");
            JSONObject did3 = new JSONObject(didDocument.toString());
            did3.put("id", "did:dcat:dataset:333333");
            JSONObject did4 = new JSONObject(didDocument.toString());
            did4.put("id", "did:dcat:dataset:444444");
            JSONObject did5 = new JSONObject(didDocument.toString());
            did5.put("id", "did:dcat:dataset:555555");

            assetList.add(new MockKeyValue("did:dcat:111111", new JSONObject()
                    .put("did", "did:dcat:dataset:111111")
                    .put("owner", new JSONObject().put("userId", "user1").put("mspId", "Glass01MSP"))
                    .put("didDocument", did1.toString()).toString()));
            assetList.add(new MockKeyValue("did:dcat:222222", new JSONObject()
                    .put("did", "did:dcat:dataset:222222")
                    .put("owner", new JSONObject().put("userId", "user2").put("mspId", "Glass01MSP"))
                    .put("didDocument", did2.toString()).toString()));
            assetList.add(new MockKeyValue("did:dcat:333333", new JSONObject()
                    .put("did", "did:dcat:dataset:333333")
                    .put("owner", new JSONObject().put("userId", "user3").put("mspId", "Glass01MSP"))
                    .put("didDocument", did3.toString()).toString()));
            assetList.add(new MockKeyValue("did:dcat:444444", new JSONObject()
                    .put("did", "did:dcat:dataset:444444")
                    .put("owner", new JSONObject().put("userId", "user4").put("mspId", "Glass01MSP"))
                    .put("didDocument", did4.toString()).toString()));
            assetList.add(new MockKeyValue("did:dcat:555555", new JSONObject()
                    .put("did", "did:dcat:dataset:555555")
                    .put("owner", new JSONObject().put("userId", "user").put("mspId", "Glass01MSP"))
                    .put("didDocument", did5.toString()).toString()));
        }

        @Override
        public Iterator<KeyValue> iterator() {
            return assetList.iterator();
        }

        @Override
        public void close() throws Exception {
            // do nothing
        }

    }

    @Test
    public void invokeUnknownTransaction() {
        DatasetManagement contract = new DatasetManagement();
        Context ctx = mock(Context.class);

        Throwable thrown = catchThrowable(() -> {
            contract.unknownTransaction(ctx);
        });

        assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                .hasMessage("Undefined contract method called");
        assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo(null);

        verifyZeroInteractions(ctx);
    }

    @Nested
    class InvokeGetAllDatasetsTransaction {

        @Test
        public void getAllDatasets() throws CertificateException, IOException {
            DatasetManagement contract = new DatasetManagement();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStateByRange("", "")).thenReturn(new MockAssetResultsIterator());

            String response = contract.GetAllDatasets(ctx);
            JSONArray result = new JSONArray(response);
            JSONObject did3 = result.getJSONObject(2);
            assertThat(did3.get("did")).isEqualTo("did:dcat:dataset:333333");
            JSONObject did3Document = new JSONObject(did3.get("didDocument").toString());
            assertThat(did3Document.get("id")).isEqualTo("did:dcat:dataset:333333");
            assertThat(did3Document.get("controller")).isEqualTo("did:dcat:provider:example-provider");

        }

        @Test
        public void getMyDatasets() throws CertificateException, IOException {
            DatasetManagement contract = new DatasetManagement();

            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);

            when(stub.getCreator()).thenReturn(CertificateUtil.getValidCreator());
            when(stub.getStateByRange("", "")).thenReturn(new MockAssetResultsIterator());
            ClientIdentity clientIdentity = new ClientIdentity(stub);
            DatasetOwner owner = DatasetManagement.convertClientIdentityToOwner(clientIdentity);
            when(ctx.getClientIdentity()).thenReturn(clientIdentity);
            when(ctx.getStub()).thenReturn(stub);

            JSONArray result = new JSONArray(contract.GetMyDatasets(ctx));
            assertThat(result.length()).isEqualTo(1);
            assertThat(result.getJSONObject(0).get("did")).isEqualTo("did:dcat:dataset:555555");
        }

    }

    @Nested
    class InvokeCreateDatasetTransaction {

        @Test
        public void createWhenDatasetExists() {
            DatasetManagement contract = new DatasetManagement();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            JSONObject returnValue = new JSONObject()
                    .put("did", "did:dcat:dataset:123456")
                    .put("didDocument", didDocument.toString())
                    .put("owner", new JSONObject().put("userId", "user").put("mspId", "Glass01MSP"));
            when(stub.getStringState("did:dcat:dataset:123456"))
                    .thenReturn(returnValue.toString());

            Throwable thrown = catchThrowable(() -> {
                contract.CreateDataset(ctx, didDocument.toString());
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Dataset did:dcat:dataset:123456 already exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("DATASET_ALREADY_EXISTS".getBytes());
        }

        @Test
        public void createWhenDatasetDoesNotExist() throws CertificateException, IOException {
            DatasetManagement contract = new DatasetManagement();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(stub.getCreator()).thenReturn(CertificateUtil.getValidCreator());
            ClientIdentity clientIdentity = new ClientIdentity(stub);
            DatasetOwner owner = DatasetManagement.convertClientIdentityToOwner(clientIdentity);
            when(ctx.getClientIdentity()).thenReturn(clientIdentity);

            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("did:dcat:dataset:123456")).thenReturn("");

            Dataset dataset = contract.CreateDataset(ctx, didDocument.toString());
            assertThat(dataset).isEqualTo(new Dataset("did:dcat:dataset:123456", didDocument.toString(), owner));
        }
    }

    @Nested
    class InvokeUpdateDatasetTransaction {

        @Test
        public void updateWhenDatasetExistsWithAuthorizedUser() throws CertificateException, IOException {
            DatasetManagement contract = new DatasetManagement();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(stub.getCreator()).thenReturn(CertificateUtil.getValidCreator());

            ClientIdentity clientIdentity = new ClientIdentity(stub);
            DatasetOwner owner = DatasetManagement.convertClientIdentityToOwner(clientIdentity);
            when(ctx.getClientIdentity()).thenReturn(clientIdentity);

            when(ctx.getStub()).thenReturn(stub);
            JSONObject returnValue = new JSONObject()
                    .put("did", "did:dcat:dataset:123456")
                    .put("didDocument", didDocument.toString())
                    .put("owner", new JSONObject().put("userId", "user").put("mspId", "Glass01MSP"));
            when(stub.getStringState("did:dcat:dataset:123456"))
                    .thenReturn(returnValue.toString());

            JSONObject newDidDocument = new JSONObject(didDocument.toString());

            newDidDocument.put("issued", "2022-09-22T18:05:20.997");

            Dataset dataset = contract.UpdateDataset(ctx, newDidDocument.toString());
            assertThat(dataset).isEqualTo(new Dataset("did:dcat:dataset:123456",
                    didDocument.toString(), owner));

            JSONObject updatedDidDocument = new JSONObject(dataset.getDidDocument());

            assertThat(updatedDidDocument.get("issued")).isEqualTo("2022-09-19T18:05:20.997");
        }

        @Test
        public void updateWhenDatasetExistsWithUnauthorizedUser() throws CertificateException, IOException {
            DatasetManagement contract = new DatasetManagement();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(stub.getCreator()).thenReturn(CertificateUtil.getInvalidCreator());

            ClientIdentity clientIdentity = new ClientIdentity(stub);
            when(ctx.getClientIdentity()).thenReturn(clientIdentity);

            when(ctx.getStub()).thenReturn(stub);
            JSONObject returnValue = new JSONObject()
                    .put("did", "did:dcat:dataset:123456")
                    .put("didDocument", didDocument.toString())
                    .put("owner", new JSONObject().put("userId", "user").put("mspId", "Glass01MSP"));
            when(stub.getStringState("did:dcat:dataset:123456"))
                    .thenReturn(returnValue.toString());

            Throwable thrown = catchThrowable(() -> {
                contract.UpdateDataset(ctx, didDocument.toString());
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("User is not authorized");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("USER_NOT_AUTHORIZED".getBytes());
        }

        @Test
        public void updateWhenDatasetDoesNotExist() {
            DatasetManagement contract = new DatasetManagement();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("did:dcat:dataset:123456")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.UpdateDataset(ctx, didDocument.toString());
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Dataset did:dcat:dataset:123456 does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("DATASET_NOT_FOUND".getBytes());
        }
}

    @Nested
    class InvokeReadDatasetTransaction {

        @Test
        public void readWhenDatasetExists() throws CertificateException, IOException {
            DatasetManagement contract = new DatasetManagement();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(stub.getCreator()).thenReturn(CertificateUtil.getValidCreator());

            ClientIdentity clientIdentity = new ClientIdentity(stub);
            DatasetOwner owner = DatasetManagement.convertClientIdentityToOwner(clientIdentity);
            when(ctx.getClientIdentity()).thenReturn(clientIdentity);

            when(ctx.getStub()).thenReturn(stub);
            JSONObject returnValue = new JSONObject()
                            .put("did", "did:dcat:dataset:123456")
                            .put("didDocument", didDocument.toString())
                            .put("owner", new JSONObject().put("userId", "user").put("mspId", "Glass01MSP"));

            when(stub.getStringState("did:dcat:dataset:123456"))
                    .thenReturn(returnValue.toString());

            Dataset dataset = contract.ReadDataset(ctx, "did:dcat:dataset:123456");
            assertThat(dataset).isEqualTo(new Dataset("did:dcat:dataset:123456", didDocument.toString(), owner));
        }

        @Test
        public void readWhenDatasetDoesNotExist() {
            DatasetManagement contract = new DatasetManagement();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("did:dcat:1111")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.ReadDataset(ctx, "did:dcat:1111");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Dataset did:dcat:1111 does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("DATASET_NOT_FOUND".getBytes());
        }
    }
}
