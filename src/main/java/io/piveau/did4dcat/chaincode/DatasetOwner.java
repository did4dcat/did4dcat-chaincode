package io.piveau.did4dcat.chaincode;

import java.util.Objects;

import com.owlike.genson.annotation.JsonProperty;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public final class DatasetOwner {

    @Property()
    private final String userId;

    @Property()
    private final String mspId;

    public String getUserId() {
        return userId;
    }

    public String getMspId() {
        return mspId;
    }

    public DatasetOwner(
            @JsonProperty("userId") final String userId,
            @JsonProperty("mspId") final String mspId
    ) {
        this.userId = userId;
        this.mspId = mspId;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        DatasetOwner other = (DatasetOwner) obj;

        if (getUserId().equals(other.getUserId()) && getMspId().equals(other.getMspId())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getMspId());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [userId=" + userId + ", mspId=" + mspId + "]";
    }
}
