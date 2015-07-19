package com.ndnhealthnet.androidudpclient.Utility;

import net.named_data.jndn.ContentType;
import net.named_data.jndn.Data;
import net.named_data.jndn.Exclude;
import net.named_data.jndn.Interest;
import net.named_data.jndn.KeyLocator;
import net.named_data.jndn.KeyLocatorType;
import net.named_data.jndn.MetaInfo;
import net.named_data.jndn.Name;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.util.Blob;

import java.nio.ByteBuffer;

/**
 * Uses jNDN library to create and manipulate NDN-compliant packets
 * Source: https://github.com/named-data/jndn
 */
public class JNDNUtils {

    static final double DEFAULT_FRESHNESS_PERIOD = 1000 * 60 * 60; // an hour

    /**
     * Returns decoded Interest packet
     *
     * @param byteBuffer - an encoded Interest packet
     * @return Interest packet if input valid; otherwise, false
     */
    public static Interest decodeInterest(ByteBuffer byteBuffer) {

        Interest interest = new Interest();

        try {

            interest.wireDecode(byteBuffer);

        } catch (EncodingException e) {

            interest = null; // failed to decode; assign null value
        }

        return interest;
    }

    /**
     * Returns decoded Interest packet
     *
     * @param blob - an encoded Interest Packet
     * @return Interest packet if input valid; otherwise, false
     */
    public static Interest decodeInterest(Blob blob) {
        return decodeInterest(blob.buf());
    }

    /**
     * Returns decoded Data packet
     *
     * @param byteBuffer - an encoded Data packet
     * @return Data packet if input valid; otherwise, false
     */
    public static Data decodeData(ByteBuffer byteBuffer) {

        Data data = new Data();

        try {
            data.wireDecode(byteBuffer);
        } catch (EncodingException e) {

            data = null; // failed to decode; assign null value
        }

        return data;
    }

    /**
     * Returns decoded Data packet
     *
     * @param blob - an encoded Data packet
     * @return Data packet if input valid; otherwise, false
     */
    public static Data decodeData(Blob blob) {
        return decodeData(blob.buf());
    }

    /**
     * Creates KeyLocator object as per NDN specification
     *
     * @param data component of KeyLocator
     * @param name component of KeyLocator
     * @param locatorType component of KeyLocator
     * @return KeyLocator object
     */
    public static KeyLocator createKeyLocator(Blob data, String name, KeyLocatorType locatorType) {
        KeyLocator keyLocator = new KeyLocator();

        keyLocator.setKeyData(data);
        keyLocator.setKeyName(new Name(name));
        keyLocator.setType(locatorType);

        return keyLocator;
    }

    /**
     * Creates MetaInfo object as per NDN specification
     *
     * @param finalBlockID component of MetaInfo
     * @param freshnessPeriod component of MetaInfo
     * @param cType component of MetaInfo
     * @return MetaInfo object
     */
    public static MetaInfo createMetaInfo(Name.Component finalBlockID, double freshnessPeriod, ContentType cType) {

        MetaInfo metaInfo = new MetaInfo();

        metaInfo.setFinalBlockId(finalBlockID);
        metaInfo.setFreshnessPeriod(freshnessPeriod);
        metaInfo.setType(cType);

        return metaInfo;
    }

    /**
     * Uses default values to create valid NDN MetaInfo object
     *
     * @return MetaInfo object
     */
    public static MetaInfo createMetaInfo() {
        MetaInfo metaInfo = new MetaInfo();

        // NOTE: we are currently not using metaInfo.setFinalBlockId

        metaInfo.setFreshnessPeriod(DEFAULT_FRESHNESS_PERIOD);
        metaInfo.setType(ContentType.BLOB); // blob is the default type

        return metaInfo;
    }

    /**
     * Creates application-specific name
     *
     * @param userID of associated packet
     * @param sensorID of associated packet
     * @param timeString of associated packet
     * @param processID of associated packet
     * @return jNDN Name object
     */
    public static Name createName(String userID, String sensorID, String timeString, String processID) {
       String name = "/ndn/" + userID + "/" + sensorID + "/" + timeString + "/" + processID;

        return new Name(name);
    }

    /**
     * Creates Data packet as per NDN specification
     *
     * @param content component of packet
     * @param name component of packet
     * @param metaInfo component of packet
     * @return jNDN Data packet
     */
    public static Data createDataPacket(String content, MetaInfo metaInfo, String name) {

        Data data = new Data();

        data.setContent(new Blob(content));
        data.setMetaInfo(metaInfo);
        data.setName(new Name(name));

        /* TODO - sign packet and validate on server
        try {
            // attempt to sign data
            KeyChain kc = new KeyChain();
            kc.signWithSha256(data);

        } catch (SecurityException e) {
            // sign failed

            // TODO - handle this error
        }*/

        return data;
    }

    /**
     * Creates Data packet as per NDN specification
     *
     * @param content component of packet
     * @param name component of packet
     * @param metaInfo component of packet
     * @return jNDN Data packet
     */
    public static Data createDataPacket(String content, MetaInfo metaInfo, Name name) {

        return createDataPacket(content, metaInfo, name.toUri());
    }

    /**
     * Creates Data packet as per NDN specification using the default MetaInfo configuration
     *
     * @param content component of packet
     * @param name component of packet
     * @return jNDN Data Packet
     */
    public static Data createDataPacket(String content, String name) {

        Data data = new Data();

        data.setContent(new Blob(content));
        data.setMetaInfo(createMetaInfo());
        data.setName(new Name(name));

        /* TODO - sign packet and validate on server
        try {
            // attempt to sign data
            KeyChain kc = new KeyChain();
            kc.signWithSha256(data);

        } catch (SecurityException e) {
            // sign failed

            // TODO - handle this error
        }*/

        return data;
    }

    /**
     * Creates Data packet as per NDN specification using the default MetaInfo configuration
     *
     * @param content component of packet
     * @param name component of packet
     * @return jNDN Data Packet
     */
    public static Data createDataPacket(String content, Name name) {

        return createDataPacket(content, name.toUri());
    }

    /**
     * Creates Interest packet as per NDN specification
     *
     * @param name name component of packet
     * @param childSelector name component of packet
     * @param interestLifetimeMillis name component of packet
     * @param keyLocator name component of packet
     * @param mustBeFresh name component of packet
     * @param maxSuffixComponents name component of packet
     * @param minSuffixComponents name component of packet
     * @param scope name component of packet
     * @return jNDN Interest Packet
     */
    public static Interest createInterestPacket(int childSelector, double interestLifetimeMillis,
                                                KeyLocator keyLocator, boolean mustBeFresh, int maxSuffixComponents,
                                                int minSuffixComponents, String name, int scope) {

        Exclude exclude = new Exclude(); // NOTE: we are not using exclude at this time

        Interest interest = new Interest();

        interest.setChildSelector(childSelector);
        interest.setExclude(exclude);
        interest.setInterestLifetimeMilliseconds(interestLifetimeMillis);
        interest.setKeyLocator(keyLocator);
        interest.setMustBeFresh(mustBeFresh);
        interest.setMaxSuffixComponents(maxSuffixComponents);
        interest.setMinSuffixComponents(minSuffixComponents);
        interest.setName(new Name(name));
        interest.setScope(scope);

        return interest;
    }

    /**
     * Creates and returns Interest packet with default configurations
     *
     * @param name component of packet
     * @return jNDN Interest Packet
     */
    public static Interest createInterestPacket(String name) {

        return new Interest(new Name(name));
    }

    /**
     * Creates and returns Interest packet with default configurations
     *
     * @param name component of packet
     * @return jNDN Interest Packet
     */
    public static Interest createInterestPacket(Name name) {

        return createInterestPacket(name.toUri());
    }
}