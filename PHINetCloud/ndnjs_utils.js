/**
 * Uses NDN-js library to create and manipulate NDN-compliant packets
 * Source: https://github.com/named-data/ndn-js
 */

var Data = require('./ndn-js/data.js').Data;
var Exclude = require('./ndn-js/exclude.js').Exclude;
var Interest = require('./ndn-js/interest.js').Interest;
var KeyLocator = require('./ndn-js/key-locator.js').KeyLocator;
var MetaInfo = require('./ndn-js/meta-info.js').MetaInfo;
var Name = require('./ndn-js/name.js').Name;
var KeyChain = require('./ndn-js/security/key-chain.js').KeyChain;
var Sha256WithRsaSignature = require('./ndn-js/sha256-with-rsa-signature.js').Sha256WithRsaSignature;

exports.ndn_utils = {

    DEFAULT_FRESHNESS_PERIOD : 1000 * 60 * 60, // an hour

    /**
     * Returns decoded Interest packet
     *
     * @param encodedInterest - an encoded Interest packet
     * @return Interest packet if input valid; otherwise, false
     */
    decodeInterest: function decodeInterest(encodedInterest) {
        var interest = new Interest();

        try {
            interest.wireDecode(encodedInterest);
        } catch (e) {
            interest = null; // failed to decode; set interest to null
        }

        return interest;
    },

    /**
     * Returns decoded Data packet
     *
     * @param encodedData - an encoded Data packet
     * @return Data packet if input valid; otherwise, false
     */
    decodeData: function(encodedData) {
        var data = new Data();

        try {
            data.wireDecode(encodedData);
        } catch (e) {
            data = null; // failed to decode; set interest to null
        }

        return data;
    },

    /**
     * Creates KeyLocator object as per NDN specification
     *
     * @param data component of KeyLocator
     * @param name component of KeyLocator
     * @param locatorType component of KeyLocator
     * @return KeyLocator object
     */
    createKeyLocator : function(data, name, locatorType) {
        var keyLocator = new KeyLocator();

        keyLocator.setKeyData(data);
        keyLocator.setKeyName(name);
        keyLocator.setType(locatorType);

        return keyLocator;
    },

    /**
     * Creates MetaInfo object as per NDN specification
     *
     * @param finalBlockID component of MetaInfo
     * @param freshnessPeriod component of MetaInfo
     * @param cType component of MetaInfo
     * @return MetaInfo object
     */
    createMetaInfo: function(finalBlockID, freshnessPeriod, cType) {

        var metaInfo = new MetaInfo();

        metaInfo.setFreshnessPeriod(freshnessPeriod);
        metaInfo.setFinalBlockId(finalBlockID);
        metaInfo.setType(cType);

        return metaInfo;
    },

    /**
     * Uses default values to create valid NDN MetaInfo object
     *
     * @return MetaInfo object
     */
    createMetaInfo: function() {

        var metaInfo = new MetaInfo();

        // NOTE: we are currently not using metaInfo.setFinalBlockId

        metaInfo.setType(0);// TODO - 0 is default, but set correctly with MetaInfo.ContentType.BLOB
        metaInfo.setFreshnessPeriod(this.DEFAULT_FRESHNESS_PERIOD);

        return metaInfo;
    },

    /**
     * Creates application-specific name
     *
     * @param userID of associated packet
     * @param sensorID of associated packet
     * @param timeString of associated packet
     * @param processID of associated packet
     * @return ndn-js Name object
     */
    createName: function(userID, sensorID, timeString, processID) {

        var name = "/ndn/" + userID + "/" + sensorID + "/" + timeString + "/" + processID;

        return new Name(name);
    },

    /**
     * Creates Data packet as per NDN specification
     * If metaInfo wasn't passed to function, use default settings
     *
     * @param content component of packet
     * @param name component of packet
     * @param metaInfo component of packet
     * @return ndn-js Data packet
     */
    createDataPacket: function(content, name, metaInfo) {

        if (!metaInfo) {
            metaInfo = this.createMetaInfo();
        }

        var data = new Data();
        var signature = new Sha256WithRsaSignature();

        data.setMetaInfo(metaInfo);
        data.setName(new Name(name));
        data.setContent(content);
        data.setSignature(signature);

        // TODO - correctly sign data KeyChain.signWithSha256(data); // sign the data packet

        return data;
    },

    /**
     * Creates Interest packet as per NDN specification
     * If params weren't passed to function, we don't set them
     *
     * @param name name component of packet
     * @param childSelector name component of packet
     * @param interestLifetimeMillis name component of packet
     * @param keyLocator name component of packet
     * @param mustBeFresh name component of packet
     * @param maxSuffixComponents name component of packet
     * @param minSuffixComponents name component of packet
     * @param scope name component of packet
     * @return ndn-js Interest Packet
     */
    createInterestPacket: function(name, childSelector, interestLifetimeMillis, keyLocator, mustBeFresh, maxSuffixComponents,
                minSuffixComponents, scope) {

        var exclude = new Exclude(); // NOTE: we are not using exclude at this time

        var interest = new Interest();

        interest.setExclude(exclude);
        interest.setName(new Name(name));

        if (childSelector)
            interest.setChildSelector(childSelector);

        if (interestLifetimeMillis)
            interest.setInterestLifetimeMilliseconds(interestLifetimeMillis);

        if (keyLocator)
            interest.setKeyLocator(keyLocator);

        if (mustBeFresh)
            interest.setMustBeFresh(mustBeFresh);

        if (maxSuffixComponents)
            interest.setMaxSuffixComponents(maxSuffixComponents);

        if (minSuffixComponents)
            interest.setMinSuffixComponents(minSuffixComponents);

        if (scope)
            interest.setScope(scope);

        return interest;
    }
};