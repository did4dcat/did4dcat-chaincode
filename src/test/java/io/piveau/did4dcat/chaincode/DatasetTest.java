/*
 * SPDX-License-Identifier: Apache-2.0
 */

package io.piveau.did4dcat.chaincode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.cert.CertificateException;

public final class DatasetTest {

    @Nested
    class Equality {

        private final JSONObject didDocument = new JSONObject()
                .put("@context", new JSONArray().put("https://www.w3.org/ns/did/v1").put("https://did4dcat.org/context/v1"))
                .put("id", "did:dcat:dataset:123456")
                .put("controller", "did:dcat:provider:example-provider")
                .put("@url", new JSONObject().put("@id", "http://data.europa.eu/88u/dataset/europeana-aggregated-dataset.rdf"))
                .put("issued", "2022-09-19T18:05:20.997")
                .put("modified", "2022-09-20T20:05:20.997")
                .put("hash", new JSONObject().put("value", "f4389t356t7zw457zn547zw4").put("alg", "URDNA2015"));

        @Test
        public void isReflexive() throws CertificateException, IOException {
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(stub.getCreator()).thenReturn(CertificateUtil.getValidCreator());

            ClientIdentity clientIdentity = new ClientIdentity(stub);
            String userId = clientIdentity.getId();
            String mspId = clientIdentity.getMSPID();
            DatasetOwner owner = new DatasetOwner(userId, mspId);

            Dataset dataset = new Dataset("did:dcat:1234", didDocument.toString(), owner);

            assertThat(dataset).isEqualTo(dataset);
        }

        @Test
        public void isSymmetric() throws CertificateException, IOException {
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(stub.getCreator()).thenReturn(CertificateUtil.getValidCreator());

            ClientIdentity clientIdentity = new ClientIdentity(stub);
            String userId = clientIdentity.getId();
            String mspId = clientIdentity.getMSPID();
            DatasetOwner owner = new DatasetOwner(userId, mspId);

            Dataset datasetA = new Dataset("did:dcat:1234", didDocument.toString(), owner);
            Dataset datasetB = new Dataset("did:dcat:1234", didDocument.toString(), owner);

            assertThat(datasetA).isEqualTo(datasetB);
            assertThat(datasetB).isEqualTo(datasetA);
        }

        @Test
        public void isTransitive() throws CertificateException, IOException {
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(stub.getCreator()).thenReturn(CertificateUtil.getValidCreator());

            ClientIdentity clientIdentity = new ClientIdentity(stub);
            String userId = clientIdentity.getId();
            String mspId = clientIdentity.getMSPID();
            DatasetOwner owner = new DatasetOwner(userId, mspId);

            Dataset datasetA = new Dataset("did:dcat:1234", didDocument.toString(), owner);
            Dataset datasetB = new Dataset("did:dcat:1234", didDocument.toString(), owner);
            Dataset datasetC = new Dataset("did:dcat:1234", didDocument.toString(), owner);

            assertThat(datasetA).isEqualTo(datasetB);
            assertThat(datasetB).isEqualTo(datasetC);
            assertThat(datasetA).isEqualTo(datasetC);
        }

        @Test
        public void handlesInequality() throws CertificateException, IOException {
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(stub.getCreator()).thenReturn(CertificateUtil.getValidCreator());

            ClientIdentity clientIdentity = new ClientIdentity(stub);
            String userId = clientIdentity.getId();
            String mspId = clientIdentity.getMSPID();
            DatasetOwner owner = new DatasetOwner(userId, mspId);

            Dataset datasetA = new Dataset("did:dcat:1234", didDocument.toString(), owner);
            Dataset datasetB = new Dataset("did:dcat:3456", didDocument.toString(), owner);

            assertThat(datasetA).isNotEqualTo(datasetB);
        }

        @Test
        public void handlesOtherObjects() throws CertificateException, IOException {
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(stub.getCreator()).thenReturn(CertificateUtil.getValidCreator());

            ClientIdentity clientIdentity = new ClientIdentity(stub);
            String userId = clientIdentity.getId();
            String mspId = clientIdentity.getMSPID();
            DatasetOwner owner = new DatasetOwner(userId, mspId);

            Dataset datasetA = new Dataset("did:dcat:1234", didDocument.toString(), owner);
            String datasetB = "not a dataset";

            assertThat(datasetA).isNotEqualTo(datasetB);
        }

        @Test
        public void handlesNull() throws CertificateException, IOException {
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(stub.getCreator()).thenReturn(CertificateUtil.getValidCreator());

            ClientIdentity clientIdentity = new ClientIdentity(stub);
            String userId = clientIdentity.getId();
            String mspId = clientIdentity.getMSPID();
            DatasetOwner owner = new DatasetOwner(userId, mspId);

            Dataset dataset = new Dataset("did:dcat:1234", didDocument.toString(), owner);

            assertThat(dataset).isNotEqualTo(null);
        }
    }

}
