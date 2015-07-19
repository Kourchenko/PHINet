/**
 * File contains code for miscellaneous functions.
 */

var bcrypt = require('bcrypt');
var DBData = require('./data'); // used to create objects used by the database
var StringConst = require('./string_const').StringConst;

exports.Utils = {

    /**
     * Used to parse (syntax below) synchronization data sent by clients into individual objects.
     *
     * Syntax: Sensor1--data1,time1;; ... ;;dataN,timeN:: ... ::SensorN--data1,time1;; ... ;;dataN,timeN
     *
     * @param userID of sender
     * @param dataContents for synchronization request
     * @returns {Array} of objects containing parsed data if input valid; otherwise returns empty array
     */
    parseSynchData: function(userID, dataContents) {

        if (dataContents && userID) {
            var parsedData = [];
            var splitBySensor = dataContents.split("::"); // '::' separates by sensor

            for (var i = 0; i < splitBySensor.length; i++) {
                var sensor = splitBySensor[i].split("--"); // '--' separates sensor's name from its data

                if (sensor.length > 1) {
                    var sensorName = sensor[0];
                    var sensorData = sensor[1].split(";;"); // ';;' separates sensor data pieces

                    for (var j = 0; j < sensorData.length; j++) {

                        var dataPiece = sensorData[j].split(","); // ',' separates (data,time) tuple

                        var sensorEntry = DBData.DATA();
                        sensorEntry.setDataPayload(dataPiece[0]);
                        sensorEntry.setSensorID(sensorName);
                        sensorEntry.setTimeString(dataPiece[1]);
                        sensorEntry.setUserID(userID);
                        sensorEntry.setProcessID(StringConst.NULL_FIELD);

                        parsedData.push(sensorEntry);
                    }
                } else {
                    // input was bad; do nothing
                }
            }

            return parsedData;
        } else {
            return [];
        }
    },

    /**
     * Returns true if password (3-15 alphanumerics plus underscore) valid.
     *
     * @param password input by user
     * @returns {boolean} determining validity of password
     */
    isValidPassword: function(password) {

        if (password) {
            var regex = /^[a-zA-Z0-9_]{3,15}$/;
            return regex.test(password);
        } else {
            return false;
        }
    },

    /**
     * Returns true if username (3-15 alphanumerics plus underscore) valid.
     *
     * @param username input by user
     * @returns {boolean} determining validity of username
     */
    isValidUserName: function(username) {

        // NOTE: keep username/password functions separate because syntax may change

        if (username) {
            var regex = /^[a-zA-Z0-9_]{3,15}$/;
            return regex.test(username);
        } else {
            return false;
        }
    },

    /**
     * Returns true if email valid.
     *
     * Code via http://stackoverflow.com/questions/46155/validate-email-address-in-javascript
     *
     * @param email input by user
     * @returns {boolean} determining validity of email
     */
    isValidEmail: function(email) {

        if (email) {
            var re = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;
            return re.test(email);
        } else {
            return false;
        }
    },

    /**
     * Date format is "YYYY-MM-DDTHH.mm.ss.SSS", where 'SSS' is milliseconds and 'T' is a parsing character
     *
     * @return String UTC-compliant current time
     */
	getCurrentTime: function () {

     	var date = new Date();
 		var utcString = date.toUTCString().split(" ");

 		var day = utcString[1]; // day is index 1
 		var month = date.getMonth() + 1; // month is 0-indexed, so add 1

 		var year = utcString[3]; // year is index 3

        var milliseconds = date.getMilliseconds();
        var seconds = date.getSeconds();
        var minutes = date.getMinutes();
        var hours = date.getHours();
 		
 		// if month is single digit, append 0 to front
 		if (month <= 9) { 
 			month = "0" + (month).toString()
 		}

       	return year + "-" + month + "-" + day + "T" + hours + "." + minutes + "." + seconds + "." + milliseconds;
	},

	/**
	 * Method hashes a password using bcrypt module.
     * source: https://github.com/ncb000gt/node.bcrypt.js/
	 *
	 * @param password to be hashed
	 * @param callback passes hashed pw back to caller
	 */
	hashPassword : function(password, callback) {

		if (!password || !callback) {
			throw "!!Error: invalid input to utils.hashPassword()!";
		} else {
            bcrypt.genSalt(10, function(err, salt) {
                if (err)
                    return callback(err, null);

                bcrypt.hash(password, salt, function(err, hash) {
                    return callback(err, hash);
                });

            })
        }
 	},

	/**
	 * Hashes a password and compares against another hash, returns true if hashes match.
	 *
	 * @param password - non-hashed user input
	 * @param hashedPassword - a hashed string (likely hashed pw found in db query)
	 * @param callback passes back true if hashes match, false otherwise
	 */
	comparePassword : function(password, hashedPassword, callback) {

		if (!password || !hashedPassword || !callback) {
			throw "!!Error: invalid input to utils.comparePassword()!";
		} else {
            bcrypt.compare(password, hashedPassword, function(err, isPasswordMatch) {
                if (err)
                    return callback(err, false);
                return callback(null, isPasswordMatch);
            });
        }
	},

    /**
     * Method returns true if the data interval is within request interval
     *
     * @param requestInterval a request interval; necessarily must contain two times (start and end)
     * @param dataInterval the time stamp on specific data
     * @return boolean - determination of whether dataInterval is within requestInterval
     */
    isValidForTimeInterval : function (requestInterval, dataInterval) {

        if (requestInterval === null || dataInterval === null) {
            return false;
        }

        var requestIntervals;

        // TIME_STRING FORMAT: "yyyy-MM-ddTHH.mm.ss.SSS||yyyy-MM-ddTHH.mm.ss.SSS"
        // the former is start interval, latter is end interval

        var beforeStartDate = false;
        var afterEndDate = false;

        var startDate, endDate, dataDate;

        try {
            requestIntervals = requestInterval.split("||"); // split interval into start/end

            // replace "T" with empty char "", so that comparison is easier
            // also, application-specific time syntax involves '.', but javascript only recognizes ':' - replace now
            requestIntervals[0] = requestIntervals[0].replace("T", " ").split(".").join(":");
            requestIntervals[1] = requestIntervals[1].replace("T", " ").split(".").join(":");
            dataInterval = dataInterval.replace("T", " ").split(".").join(":");

            startDate = new Date(Date.parse(requestIntervals[0]));
            endDate = new Date(Date.parse(requestIntervals[1]));

            if ( isNaN( startDate.getTime())  ||  isNaN( endDate.getTime()) ) {
                return false; // invalid date detected; return false
            }

            dataDate = new Date(dataInterval);

            beforeStartDate = dataDate < startDate; // test if dataDate is before startDate
            afterEndDate = dataDate > endDate; // test if dataDate is after endDate

        } catch  (e) {
            return false; // some problem occurred, default return is false
        }

        // if dataInterval is not before start and not after end, then its with interval
        return (!beforeStartDate && !afterEndDate) || requestIntervals[0] === dataInterval
            || requestIntervals[1] === dataInterval;
    }, 

    /**
     * Method is invoked and converts data into a string (syntax below) for easy placement in NDN Data packet.
     *
     * Syntax: Sensor1--data1,time1;; ... ;;dataN,timeN:: ... ::SensorN--data1,time1;; ... ;;dataN,timeN
     *
     * @param data to be converted
     * @param formatCallback that passes formatted data back to caller
     */
    formatCacheRequest : function(data, formatCallback) {

        try {

            var hashedBySensors = {};
            var formattedSyncData = "";

            // first separate data based upon sensor
            for (var i = 0; i < data.length; i++) {
                // sensor hasn't been stored yet, create array for its data and store now
                if (!hashedBySensors[data[i].getSensorID()]) {

                    hashedBySensors[data[i].getSensorID()] = [data[i]];
                }
                // sensor has been seen, append data to its Array now
                else {

                    hashedBySensors[data[i].getSensorID()].push(data[i]);
                }
            }

            var sensors = Object.keys(hashedBySensors);

            // now format data for each sensor
            for (var i = 0; i < sensors.length; i++) {

                formattedSyncData += sensors[i] + "--"; // '--' separates sensor's name from its data

                for (var j = 0; j < hashedBySensors[sensors[i]].length; j++) {
                    var sensorData = hashedBySensors[sensors[i]][j];

                    formattedSyncData += sensorData.getDataPayload() + "," + sensorData.getTimeString();
                    formattedSyncData += ";;"; // ';;' separates each data piece for sensor
                }

                // remove last two chars, ';;', because they proceed no data
                formattedSyncData = formattedSyncData.substr(0, formattedSyncData.length - 2);

                formattedSyncData += "::"; // '::' separates each sensor
            }

            // remove last two chars, '::', because they proceed no sensor
            formattedSyncData = formattedSyncData.substr(0, formattedSyncData.length - 2);

            formatCallback(formattedSyncData);
        } catch (e) {
            // TODO - handle better
            console.log("Error: exception in Utils.formatCacheRequest(): " + e);
        }
    }
};
