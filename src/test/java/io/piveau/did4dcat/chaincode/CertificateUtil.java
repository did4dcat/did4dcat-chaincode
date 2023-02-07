package io.piveau.did4dcat.chaincode;

import com.google.protobuf.ByteString;
import org.hyperledger.fabric.protos.msp.SerializedIdentity;

import java.util.Base64;

final class CertificateUtil {

    private CertificateUtil() { }

    private static final String VALID_CERTIFICATE =
        "MIICizCCAjKgAwIBAgIUJUr9aEvJYlzs5QKVTalSUorYLm8wCgYIKoZIzj0EAwIw"
        + "fjELMAkGA1UEBhMCREUxDzANBgNVBAgTBkJlcmxpbjEPMA0GA1UEBxMGQmVybGlu"
        + "MSQwIgYDVQQKExtnbGFzczAxLmZva3VzLmZyYXVuaG9mZXIuZGUxJzAlBgNVBAMT"
        + "HmNhLWdsYXNzMDEuZm9rdXMuZnJhdW5ob2Zlci5kZTAeFw0yMjA3MjcxNTA4MDBa"
        + "Fw0yMzA3MjcxNTE4MDBaMEExMDALBgNVBAsTBG9yZzEwDQYDVQQLEwZjbGllbnQw"
        + "EgYDVQQLEwtkZXBhcnRtZW50MTENMAsGA1UEAxMEdXNlcjBZMBMGByqGSM49AgEG"
        + "CCqGSM49AwEHA0IABA/52Bo6RWHsl+nlVTFDmhjK8DL7DVPXdnt+cFkJMwH+f+gA"
        + "AJkwH+tHIvRzQeB2bmMJk0P1KG3qE+nQorE5C7ajgcowgccwDgYDVR0PAQH/BAQD"
        + "AgeAMAwGA1UdEwEB/wQCMAAwHQYDVR0OBBYEFO+kQcKUOxxfuCTnXB1hSTIuSRYg"
        + "MB8GA1UdIwQYMBaAFHmqqeSyg1wYm8llRyXcT8r0w1o4MGcGCCoDBAUGBwgBBFt7"
        + "ImF0dHJzIjp7ImhmLkFmZmlsaWF0aW9uIjoib3JnMS5kZXBhcnRtZW50MSIsImhm"
        + "LkVucm9sbG1lbnRJRCI6InVzZXIiLCJoZi5UeXBlIjoiY2xpZW50In19MAoGCCqG"
        + "SM49BAMCA0cAMEQCIE6xL79nNBjUqX6ij6yaYmkPlKob6r9UPMR4BaAqM/4LAiBo"
        + "9/iOa4ybG/YEGpdYu0TsjDSVi5u9BIw3QSoQRYwLkg==";

    private static final String INVALID_CERTIFICATE =
        "MIICODCCAd6gAwIBAgIUNi4fyaKvYIy+EqV0uOv83led2RowCgYIKoZIzj0EAwIw"
        + "fjELMAkGA1UEBhMCREUxDzANBgNVBAgTBkJlcmxpbjEPMA0GA1UEBxMGQmVybGlu"
        + "MSQwIgYDVQQKExtnbGFzczAxLmZva3VzLmZyYXVuaG9mZXIuZGUxJzAlBgNVBAMT"
        + "HmNhLWdsYXNzMDEuZm9rdXMuZnJhdW5ob2Zlci5kZTAeFw0yMjA3MjcxNTA4MDBa"
        + "Fw0yMzA3MjcxNTE4MDBaMCExDzANBgNVBAsTBmNsaWVudDEOMAwGA1UEAxMFYWRt"
        + "aW4wWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAASJwuXc2AO287vOEPZAdG0C4Ym5"
        + "W34v7GBtiVq80o2ZMzs70Z2UEApwJLJsKjg5IZRg6RE6h27m5Ru5Vc6Wi33Ho4GW"
        + "MIGTMA4GA1UdDwEB/wQEAwIDqDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUH"
        + "AwIwDAYDVR0TAQH/BAIwADAdBgNVHQ4EFgQU26eGuLCBFQOcZyicRGcBJlI0bLkw"
        + "HwYDVR0jBBgwFoAUeaqp5LKDXBibyWVHJdxPyvTDWjgwFAYDVR0RBA0wC4IJbG9j"
        + "YWxob3N0MAoGCCqGSM49BAMCA0gAMEUCIQC9K2817CKFDjMacV5f7Pvlbk81c6Z6"
        + "a+laHQnVbClkTAIgMYpxNsBi6Ho1IC6lXxK7mYLL8yx1uo0fXp8bm1rxp1I=";

    static byte[] buildSerializedIdentity(final String certificate) {
        final SerializedIdentity.Builder identity = SerializedIdentity.newBuilder();
        identity.setMspid("Glass01MSP");
        final byte[] decodedCert = Base64.getDecoder().decode(certificate);
        identity.setIdBytes(ByteString.copyFrom(decodedCert));
        final SerializedIdentity builtIdentity = identity.build();
        return builtIdentity.toByteArray();
    }

    static byte[] getValidCreator() {
        return buildSerializedIdentity(VALID_CERTIFICATE);
    }

    static byte[] getInvalidCreator() {
        return buildSerializedIdentity(INVALID_CERTIFICATE);
    }
}
