package io.piveau.did4dcat.chaincode;

import java.util.Objects;

import com.owlike.genson.annotation.JsonProperty;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public final class Dataset {

    @Property()
    private final String did;

    @Property()
    private final String didDocument;

    @Property()
    private final DatasetOwner owner;

    public String getDid() {
        return did;
    }

    public String getDidDocument() {
        return didDocument;
    }

    public DatasetOwner getOwner() {
        return owner;
    }

    public Dataset(
            @JsonProperty("did") final String did,
            @JsonProperty("didDocument") final String didDocument,
            @JsonProperty("owner") final DatasetOwner owner
    ) {
        this.did = did;
        this.didDocument = didDocument;
        this.owner = owner;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Dataset other = (Dataset) obj;

        // ToDo Implement better equals check
        if (getDid().equals(other.getDid())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDid());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [did=" + did + ", owner=" + owner + "]";
    }
}
