/** 
 * File contains code for the object that enables UDP communication
 */

var DBData = require('./data'); // used to create objects used by the database
var StringConst = require('./string_const').StringConst;

var PIT, FIB, CS, USER_CREDENTIALS;

var dgram = require("dgram"); // Node.js udp socket module
var socket = dgram.createSocket('udp4');
var utils = require('./utils.js').Utils;
var analytics = require('./analytics.js').analytics();

var NDN_SENSOR_NET_PORT = 50056; // same across all applications

var ndnjs_utils = require('./ndnjs_utils.js').ndn_utils;
var Data = require('./ndn-js/data.js').Data;
var Interest = require('./ndn-js/interest.js').Interest;
var Name = require('./ndn-js/name.js').Name;

var recentLoginValidations = []; // TODO - replace this temporary data structure
var recentRegisterValidations = []; // TODO - replace this temporary data structure

var rateLimitDictionary = {};

// chosen somewhat arbitrarily, is hopefully large enough to server many legitimate users with private IP
var MAX_HITS_PER_SECOND = 100;
var CLEAN_RATE_DICT_INTERVAL = 1000 * 60 * 60; // chosen somewhat arbitrarily

// TODO - improve upon this naive rate-limiting

/**
 * Enforces that each IP gets a maximum of MAX_HITS_PER_SECOND packets processed each second.
 * 
 * @param userIP of user requesting page
 * @return boolean regarding isRateLimit status of user's IP
 */
var isRateLimited = function (userIP) {

    var currentSecond = parseInt(new Date().getTime() / 1000);

    if (!rateLimitDictionary[userIP]) {
        rateLimitDictionary[userIP] = [currentSecond, 1];

        return false;
    } else {

        if (rateLimitDictionary[userIP][0] === currentSecond) {

            if(rateLimitDictionary[userIP][1] > MAX_HITS_PER_SECOND) {
                return true;
            } else {
                rateLimitDictionary[userIP][1] += 1;
                return false;
            }
        } else {
            rateLimitDictionary[userIP] = [currentSecond, 1];
            return false;
        }
    }

}

/**
 * Each CLEAN_RATE_DICT_INTERVAL we remove all entries that contain
 * invalid times (ie past times that could not cause a rate limit).
 */
setInterval(
    function() {
        var currentSecond = parseInt(new Date().getTime() / 1000);

        for (var key in rateLimitDictionary) {
            if (rateLimitDictionary[key][0] !== currentSecond) {
                delete rateLimitDictionary[key];
            }
        }

    }, CLEAN_RATE_DICT_INTERVAL
); // invoke every interval

/**
 * Returns object that handles majority of UDP communication. References to the core
 * NDN databases are passed to the module so that relevant queries can be performed.
 *
 * @param pitReference - reference to the PIT database
 * @param fibReference - reference to the FIB database
 * @param csReference - reference to the CS database
 * @param ucReference - reference to the USER_CREDENTIAL database
 */
exports.UDPComm = function(pitReference, fibReference, csReference, ucReference) {

    PIT = pitReference;
    FIB = fibReference;
    CS = csReference;
    USER_CREDENTIALS = ucReference;

	return {

        /**
         * Method initializes the UDP listener to specified PORT.
         */
		initializeListener : function () {

            // you must declare these here so that they will be in scope in the callback
            var handleInterestPacket = this.handleInterestPacket;
            var handleDataPacket = this.handleDataPacket;

            socket.bind(NDN_SENSOR_NET_PORT);
			socket.on('message', function(msg, rinfo) {

                if (!isRateLimited(rinfo.address)) {

                    console.log("message found: " + msg);

                    try {
                        // attempt to create both Interest and Data packet; only one should be valid
                        var interest = ndnjs_utils.decodeInterest(msg);
                        var data = ndnjs_utils.decodeData(msg);

                        if (interest && !data) {
                             console.log("interest packet found");

                            // Interest packet detected
                            handleInterestPacket(interest, rinfo.address, rinfo.port);
                        } else if (!interest && data) {
                            console.log("data packet found");

                            // Data packet detected
                            handleDataPacket(data, rinfo.address);
                        } else {

                            // unknown packet type; drop it
                        }
                    } catch (e) {
                        console.log("Something went wrong. Unable to parse packet. Error: " + e);
                    }
                }
			});
		},

        /**
         * Method sends message to specified ip/port combination. It's inclusion within the
         * module allows other modules to send messages - such as from within server.js
         *
         * @param message content sent to receiver
         * @param ip - receiver's ip
         * @param port - receiver's port
         */
        sendMessage: function (message, ip, port) {

            var buffer = message.buffer; // message param is either encoded Interest or Data object; get its buffer

            if (port == undefined || port === null) {
                port = NDN_SENSOR_NET_PORT;
            }

            socket.send(buffer, 0, buffer.length, port, ip,
                function(err) {

                    if (err) {
                        console.log("!!Error sending packet: " + err);
                    }
            });
        },

        /**
         * Handles INTEREST packet as per NDN specification
         *
         * Method parses packet then asks the following questions:
         * 1. Do I have the data? If so, I will reply with my own.
         * 2. Have I already sent an interest for this data? If so, I will add this request to PIT entry.
         *
         * @param interestPacket incoming ndn-js Interest packet after having been decoded
         * @param packetIP specifies IP that will receive reply if parse success
         * @param packetPort specifies IP that will receive reply if parse success
         */
        handleInterestPacket: function (interestPacket, packetIP, packetPort) {

            // decode paring characters "||" and then split into array for further parsing
            var nameComponent = interestPacket.getName().toUri().replace("%7C%7C", "||").split("/");

            console.log("interest name: " + nameComponent);

            // information extracted from our name format:
            // "/ndn/userID/sensorID/timeString/processID"
            // the indexes used are position + 1 (the addition 1 due to empty string in 0-index)
            var userID = nameComponent[2].trim();
            var sensorID = nameComponent[3].trim();
            var timeString = nameComponent[4].trim();
            var processID = nameComponent[5].trim();

            // check if packet is an INTEREST for CACHE data
            if (processID === StringConst.DATA_CACHE) {

                handleInterestCacheRequest(userID, sensorID, timeString, processID,
                    packetIP, packetPort);
            }
            // a client application requests login; initiate process now
            else if (processID === StringConst.LOGIN_REQUEST) {

                /**
                 * Here client userID is stored in sensorID position. We needed to send userID 
                 * but had no place to do so and sensorID would have otherwise been null. 
                 */

                handleInterestLoginRequest(sensorID, timeString, packetIP, packetPort);
            }
            // a client application requests register; initiate process now
            else if (processID === StringConst.REGISTER_REQUEST) {

                /**
                 * Here client userID is stored in sensorID position. We needed to send userID 
                 * but had no place to do so and sensorID would have otherwise been null. 
                 */

                handleInterestRegisterRequest(sensorID, timeString, packetIP, packetPort);
            }
            // a client requests result of login
            else if (processID === StringConst.LOGIN_RESULT) {

                handleLoginResultRequest(sensorID, timeString, packetIP, packetPort);
            }
            // a client requests the result of registration
            else if (processID === StringConst.REGISTER_RESULT) {

                handleRegisterResultRequest(sensorID, timeString, packetIP, packetPort);
            }
            // client requests mode analytic task on data
            else if (processID === StringConst.MODE_ANALYTIC) {

                handleModeAnalyticTask(userID, sensorID, timeString, packetIP, packetPort);
            }
            // client requests median analytic task on data
            else if (processID === StringConst.MEDIAN_ANALYTIC) {
                handleMedianAnalyticTask(userID, sensorID, timeString, packetIP, packetPort);
            }
            // client requests mean analytic task on data
            else if (processID === StringConst.MEAN_ANALYTIC) {
                handleMeanAnalyticTask(userID, sensorID, timeString, packetIP, packetPort);
            }
            // client requests initialization of sync request
            else if (processID === StringConst.INITIATE_SYNCH_REQUEST) {

                handleSynchRequest(sensorID, timeString, packetIP, packetPort);
            } else if (processID === StringConst.ADD_DOCTOR) {

                handleAddDoctor(sensorID, timeString, packetIP, packetPort);
            } else if (processID === StringConst.DOCTOR_LIST) {

                handleDoctorList(sensorID, timeString, packetIP, packetPort);
            } else if (processID === StringConst.ADD_PATIENT) {

                handleAddPatient(sensorID, timeString, packetIP, packetPort);
            } else if (processID === StringConst.PATIENT_LIST) {

                handlePatientList(sensorID, timeString, packetIP, packetPort);
            } else  {
                // unknown process id; drop packet
            }
        },

        /**
         * handles DATA packet as per NDN specification
         * Method parses packet then stores in cache if requested,
         * and sends out to satisfy any potential Interests.
         *
         * @param dataPacket incoming ndn-js Data packet after having been decoded
         * @param hostIP used to query PIT for previous entries
         */
        handleDataPacket: function (dataPacket, hostIP) {

            // decode paring characters "||" and then split into array for further parsing
            var nameComponent = dataPacket.getName().toUri().replace("%7C%7C", "||").split("/");
            var dataContents = dataPacket.getContent().toString();

            // information extracted from our name format:
            // "/ndn/userID/sensorID/timeString/processID"
            // the indexes used are position + 1 (the addition 1 due to empty string in 0-index)
            var userID = nameComponent[2].trim();
            var sensorID = nameComponent[3].trim();
            var timeString = nameComponent[4].trim();
            var processID = nameComponent[5].trim();

            console.log("handle data packet, name: " + nameComponent);

            // first, determine who wants the data
            PIT.getGeneralPITData(userID, hostIP, function(rowsTouched, allValidPITEntries) {

                // data was requested; handle it now
                if (allValidPITEntries !== null) {

                    // determine if data packet matches any requests (various criteria considered)
                    var requestFoundWithinInterval = false;
                    for (var i = 0; i < allValidPITEntries.length; i++) {

                        // must match time, processID, and userID
                        if ((utils.isValidForTimeInterval(allValidPITEntries[i].getTimeString(), timeString)
                                || allValidPITEntries[i].getTimeString() === timeString)
                                && allValidPITEntries[i].getProcessID() === processID
                                && allValidPITEntries[i].getUserID() === userID) {
                            requestFoundWithinInterval = true;
                            break;
                        }
                    }

                    if (requestFoundWithinInterval) { // positive request count, process packet now

                        // handle content store data
                        if (processID === StringConst.DATA_CACHE) {

                            handleCacheData(userID, sensorID, timeString,
                                processID, dataContents, allValidPITEntries);
                        }
                        // client has sent login credentials to server for validation
                        else if (processID === StringConst.LOGIN_CREDENTIAL_DATA) {

                            handleLoginData(dataContents);
                        }
                        // client has sent register credentials to server for validation
                        else if (processID === StringConst.REGISTER_CREDENTIAL_DATA) {

                            handleRegisterData(dataContents);
                        }
                        // client has responded to server's synchronization request
                        else if (processID === StringConst.SYNCH_DATA_REQUEST) {

                            handleSynchRequestData(userID, dataContents);
                        } else if (processID === StringConst.CLIENT_DOCTOR_SELECTION) {

                            handleClientDoctorSelection(userID, dataContents, timeString, hostIP);
                        } else {
                            // unknown process id; drop packet
                        }
                    }
                }
            });
        }
	};
};

/**
 * Method sends message to specified ip/port combination. It's exclusion from the module ensures
 * that any function from within this file may call it without worrying about scope issues.
 *
 * @param message content sent to receiver
 * @param ip - receiver's ip
 * @param port - receiver's port
 */
function sendMessage (message, ip, port) {

    var buffer = message.buffer; // message param is either encoded Interest or Data object; get its buffer

    if (port == undefined || port === null) {
        port = NDN_SENSOR_NET_PORT;
    }

    console.log("sending: " + message);

    socket.send(buffer, 0, buffer.length, port, ip,
        function(err) {

            if (err) {
                console.log("!!Error sending packet: " + err);
            }
    });
}

/**
 * Method handles incoming Data
 *
 * @param userID userID associated with incoming Data packet
 * @param sensorID sensorID associated with incoming Data packet
 * @param timeString timeString associated with incoming Data packet
 * @param processID processID associated with incoming Data packet
 * @param packetFloatContent contents of incoming Data packet
 * @param allValidPITEntries ArrayList of all PIT entries requesting this data
 */
function handleCacheData(userID, sensorID, timeString,
                           processID, packetFloatContent, allValidPITEntries) {

    var data = DBData.DATA();
    data.csData(userID, sensorID, timeString, packetFloatContent);

    // if data exists in cache, just update; otherwise insert
    CS.getSpecificCSData(userID, timeString, function(rowsTouched, queryResult) {
        if (queryResult !== null) {

            CS.updateCSData(data, function(){});
        } else {

            // data not in cache, add now
            CS.insertCSData(data, function(){});
        }
    });

    require('dns').lookup(require('os').hostname(), function (err, myIPAddress, fam) {

        // now, send packets to each entity that requested the data
        for (var i = 0; i < allValidPITEntries.length; i++) {

            // data satisfies PIT entry; delete the entry
            PIT.deletePITData(allValidPITEntries[i].userID, allValidPITEntries[i].timeString,
                allValidPITEntries[i].ipAddr, function(){});

            // a device other than this requested the DATA
            if (allValidPITEntries[i].ipAddr !== myIPAddress) {

                // create and send NDN-compliant packet using ndn-js module
                var packetName = ndnjs_utils.createName(userID, sensorID, timeString, processID);
                var data = ndnjs_utils.createDataPacket(packetFloatContent, packetName);

                // send DATA packet to node that requested it
                sendMessage(data.wireEncode(), allValidPITEntries[i].ipAddr, NDN_SENSOR_NET_PORT);
            }
        }
    });
}

/**
 * Method handles login user-credential Data packet sent by client. If credentials
 * match user in database, then store success so that it can be queried.
 *
 * @param dataContents of user-credential Data packet
 */
function handleLoginData(dataContents) {

    if (dataContents) {

        // syntax is "userID,password"
        var loginCredentials = dataContents.split(",");

        var userID = loginCredentials[0].trim();
        var password = loginCredentials[1].trim();

        USER_CREDENTIALS.getUserByID(userID, function(rowCount, queryResult) {

            // only one row (user) should have been found and returned
            if (rowCount === 1 && queryResult) {

                utils.comparePassword(password, queryResult.getPassword(),
                    function (err, isPasswordMatch) {

                        // login successful
                        if (isPasswordMatch) {

                            // store result now so that user can query it
                            recentLoginValidations.push({"userID": userID, "password": password, 
                                                        "userType": queryResult.getEntityType()});
                        }
                        // login unsuccessful
                        else {
                            // store nothing; when user queries, they will get a null reply
                        }
                    });

            } else {
                // user was not found in database; login unsuccessful
            }
        });
    } else {
        // user-credential packet contained no content; do nothing
    }
}

/**
 * Method handles register user-credential Data packet sent by client. If
 * credentials match user in database, then store success so that it can be queried.
 *
 * If no user exists in database (a successful register), create entry for new user.
 *
 * @param dataContents
 */
function handleRegisterData(dataContents) {

    if (dataContents) {

        // syntax is "userID,password,email,userType"
        var registerCredentials = dataContents.split(",");

        var userID = registerCredentials[0].trim();
        var pw = registerCredentials[1].trim();
        var email = registerCredentials[2].trim();
        var userType = registerCredentials[3].trim();

        // hash password before storing
        utils.hashPassword(pw,  function(err, hashedPW){

            USER_CREDENTIALS.getUserByID(userID, function(rowCount, queryResult) {

                // register successful; this user was not found in database
                if (rowCount === 0 && !queryResult) {

                    // place user in database
                    USER_CREDENTIALS.insertNewUser(userID, hashedPW, email, userType,
                        function(rowsTouched) {

                            if (rowsTouched === 1) {
                                // insertion successful; store result now so that user can query it
                                recentRegisterValidations.push({"userID": userID, "password": hashedPW, "email": email});
                            } else {
                                // insert unsuccessful

                                // TODO - notify user; handle this error
                            }
                        });
                }
            });
        });
    } else {
        // user-credential packet contained no content; do nothing
    }
}

/**
 * Takes client-synchronization input (syntax below), parses it, and stores it in the ContentStore.
 *
 * Syntax: Sensor1--data1,time1;; ... ;;dataN,timeN:: ... ::SensorN--data1,time1;; ... ;;dataN,timeN
 *
 * @param userID of client sending synch data
 * @param dataContents client's synch data
 */
function handleSynchRequestData(userID, dataContents) {

    var parsedSynchData = utils.parseSynchData(userID, dataContents);

    if (parsedSynchData) {

        for (var i = 0; i < parsedSynchData.length; i++) {
            CS.insertCSData(parsedSynchData[i], function(){});
        }
    }
}

/**
 * If userID doesn't have doctorName as a doctor, add it now
 *
 * @param userID of user requesting new doctor
 * @param doctorName of new doctor requested by userID
 * @param timeString associated with packet
 * @param ipAddr of sender
 */
function handleClientDoctorSelection(userID, doctorName, timeString, ipAddr) {

    USER_CREDENTIALS.getUserByID(doctorName, function(rowCount, queryResult) {

        // data satisfies PIT entry; delete the entry
        PIT.deletePITData(userID, timeString, ipAddr, function(){});

        // check that doctorName is a valid doctor
        if (rowCount === 1 && queryResult
                        && queryResult.getEntityType() === StringConst.DOCTOR_USER_TYPE) {

            USER_CREDENTIALS.addDoctor(userID, doctorName, function(){});
        }

    });
}

/**
 * performs NDN logic on packet that requests data
 *
 * @param userID userID associated with requested data from cache
 * @param sensorID sensorID associated with requested data from cache
 * @param timeString timeString associated with requested data from cache
 * @param processID processID associated with requested data from cache
 * @param packetIP specifies IP that will receive reply if parse success
 * @param packetPort specifies IP that will receive reply if parse success
 * @return boolean - true if a packet sent, false otherwise (true indicates valid input; useful during testing)
 */
function handleInterestCacheRequest(userID, sensorID, timeString,
                                       processID, packetIP, packetPort) {

    // first, check CONTENT STORE (cache) for requested information
    CS.getGeneralCSData(userID, function(rowsTouched, csQueryResults) {

        // data was in cache; send to requester
        if (csQueryResults) {

            // format the data before sending it
            utils.formatCacheRequest(csQueryResults, function(formattedData) {

                // create and send packet with ndn-js module
                var packetName = ndnjs_utils.createName(userID ,
                    StringConst.NULL_FIELD, timeString, StringConst.DATA_CACHE);

                var data = ndnjs_utils.createDataPacket(formattedData, packetName);

                sendMessage(data.wireEncode(), packetIP, packetPort); // reply to interest with DATA from cache
            })
        }
        // data wasn't in cache; check PIT to see if an interest has already been sent for data
        else {

            // TODO - again, rework with specific date once TIME variable valid

            var newPITEntry;

            PIT.getGeneralPITData(userID, packetIP, function(rowsTouched, queryResults) {

                // query returned NULL: no INTEREST sent yet; do so now
                if (!rowsTouched || !queryResults) {

                    // add new request to PIT, then look into FIB before sending request
                    newPITEntry = DBData.DATA();
                    newPITEntry.pitData(userID, sensorID, processID, timeString, packetIP);
                    PIT.insertPITData(newPITEntry, function(){});

                    FIB.getAllFIBData(function(rowCount, queryResults) {

                        if (queryResults === null || rowCount === 0) {
                            // TODO - sophisticated way in which user deals with FIB

                            // shouldn't be empty; client connects to server (i.e., enter server's FIB) before requesting data
                            throw "!!Error: Cannot send message; FIB is empty.";
                        } else {

                            for (var i = 0; i < queryResults.length; i++) {

                                // don't send data to same node that requested; check first
                                if (queryResults[i].getIpAddr() !== packetIP && queryResults[i].getIpAddr() !== null) {

                                    // create and send packet with ndn-js module
                                    var packetName = ndnjs_utils.createName(userID, sensorID,
                                        timeString,  processID);
                                    var interest = ndnjs_utils.createInterestPacket(packetName);

                                    // ask for data from nodes in FIB via INTEREST packet
                                    sendMessage(interest.wireEncode(), queryResults[i].getIpAddr(), NDN_SENSOR_NET_PORT);
                                }
                            }
                        }
                    });
                    // query returned value: INTEREST has been sent; append current request to PIT entry
                } else {

                    // add new request to PIT and wait, request has already been sent
                    newPITEntry = DBData.DATA();
                    newPITEntry.pitData(userID, sensorID, processID, timeString, packetIP);

                    PIT.insertPITData(newPITEntry, function(){});
                }
            });
        }
    });
}

/**
 * Begins client-login procedure.
 *
 * @param clientID of client who requested login
 * @param timeString associated with packet
 * @param hostIP of client who requested login
 * @param hostPort of client who requested login
 */
function handleInterestLoginRequest(clientID, timeString, hostIP, hostPort) {

    /**
     * Due to the nature of NDN, the client must first send login Interest to
     * server (because the server is the only one who can validate such requests). The
     * server will then reply with a blank Data and, shortly, an Interest requesting
     * login credentials, and the client will then reply with a Data packet containing
     * them. The client then sends an Interest to the server querying for the result,
     * to which the server replies with a Data packet. If the results are positive,
     * the client has logged in; otherwise, login failed.
     */

    var packetName = ndnjs_utils.createName(clientID, StringConst.NULL_FIELD, timeString, StringConst.LOGIN_CREDENTIAL_DATA);
    var interest = ndnjs_utils.createInterestPacket(packetName);

    sendMessage(interest.wireEncode(), hostIP, hostPort);

    // add request to PIT
    var newPITEntry = DBData.DATA();
    newPITEntry.pitData(clientID, StringConst.NULL_FIELD, StringConst.LOGIN_CREDENTIAL_DATA, timeString, hostIP);

    PIT.insertPITData(newPITEntry, function(){});
}

/**
 * Begins client-register procedure.
 *
 * @param clientID of client who requested register
 * @param timeString associated with packet
 * @param hostIP of client who requested register
 * @param hostPort of client who requested register
 */
function handleInterestRegisterRequest(clientID, timeString, hostIP, hostPort) {

    /**
     * Due to the nature of NDN, the client must first send signup Interest to
     * server (because the server is the only one who can process such requests). The
     * server will then reply with a blank Data and, shortly, an Interest requesting
     * user credentials, and the client will then reply with a Data packet containing
     * them. The client then sends an Interest to the server querying for the result,
     * to which the server replies with a Data packet. If the results are positive,
     * the client has registered; otherwise, register failed.
     */

    var packetName = ndnjs_utils.createName(clientID, StringConst.NULL_FIELD, timeString, StringConst.REGISTER_CREDENTIAL_DATA);
    var interest = ndnjs_utils.createInterestPacket(packetName);

    sendMessage(interest.wireEncode(), hostIP, hostPort);

    // add request to PIT
    var newPITEntry = DBData.DATA();
    newPITEntry.pitData(clientID, StringConst.NULL_FIELD, StringConst.REGISTER_CREDENTIAL_DATA, timeString, hostIP);

    PIT.insertPITData(newPITEntry, function(){});
}

/**
 * Handles user's query as to whether login was success; if found,
 * replies with previously-stored login result then deletes it from array.
 *
 * @param hostIP of querying user
 * @param timeString associated with packet
 * @param sensorID of querying user
 * @param hostPort of querying user
 */
function handleLoginResultRequest(sensorID, timeString, hostIP, hostPort) {

    /** 
     * Here userID is stored in sensorID position. We needed to send userID 
     * but had no place to do so and sensorID would have otherwise been null. 
     */

    handleResultRequest(sensorID, timeString, hostIP, hostPort, StringConst.LOGIN_REQUEST);
}

/**
 * Handles user's query as to whether register was success; if found,
 * replies with previously-stored register result then deletes it from array.
 *
 * @param sensorID of querying user
 * @param timeString associated with packet
 * @param hostIP of querying user
 * @param hostPort of querying user
 */
function handleRegisterResultRequest(sensorID, timeString, hostIP, hostPort) {

    /** 
     * Here userID is stored in sensorID position. We needed to send userID 
     * but had no place to do so and sensorID would have otherwise been null. 
     */

    handleResultRequest(sensorID, timeString, hostIP, hostPort, StringConst.REGISTER_REQUEST);
}

/**
 * Handles user's query as to whether register/login was success; if found,
 * replies with previously-stored register result then deletes it from array.
 *
 * @param userID of querying user
 * @param timeString associated with packet
 * @param hostIP of querying user
 * @param hostPort of querying user
 * @param requestType (i.e., either LOGIN_REQUEST or REGISTER_REQUEST)
 */
function handleResultRequest(userID, timeString, hostIP, hostPort, requestType) {
    var userValidationFound = false;
    var userType = null;
    var recentValidations;

    // set validation array based upon requestType (i.e., register or login)
    if (requestType === StringConst.REGISTER_REQUEST) {
        recentValidations = recentRegisterValidations;
    } else {
        // only requestType left is login
        recentValidations = recentLoginValidations;
    }

    for (var i = 0; i < recentValidations.length; i++) {

        if (recentValidations[i].userID === userID) {
            console.dir(recentValidations[i]);
            userType = recentValidations[i].userType;
            recentValidations.splice(i, 1); // remove element
            userValidationFound = true;

            break;
        }
    }

    var packetName;

    // client's login/register has been validated; reply with portion if FIB (refer to paper for more information)
    if (userValidationFound) {

        /** 
         * Here userID is stored in sensorID position. We needed to send userID 
         * but had no place to do so and sensorID would have otherwise been null. 
         */

        if (requestType === StringConst.REGISTER_REQUEST) {
            // set process id to REGISTER_RESULT
            packetName = ndnjs_utils.createName(StringConst.SERVER_ID, userID,
                timeString, StringConst.REGISTER_RESULT);
        } else {
            // only requestType left is login; set process id to LOGIN_RESULT
            packetName = ndnjs_utils.createName(StringConst.SERVER_ID, userID,
                timeString, StringConst.LOGIN_RESULT);
        }

        FIB.getAllFIBData(function(rowsTouched, queryResult) {

            var packetContent = "";
            if (requestType === StringConst.LOGIN_REQUEST) {
                // append user type on login
                packetContent = userType + ";;";
            }

            if (rowsTouched > 0 && queryResult) {

                // TODO - send randomization and localized IPs in proper proportions

                // send count / sqrt(count); as described in PHINET paper
                var fibEntriesToBeSent = Math.ceil(rowsTouched / Math.sqrt(rowsTouched));

                for (var i = 0; i < fibEntriesToBeSent; i++) {

                    // don't send a user their own information
                    if (queryResult[i].getUserID() !== userID) {

                        // syntax is "userid,ipaddr" and "||" separates each entry
                        packetContent += queryResult[i].getUserID() + "," + queryResult[i].getIpAddr() + "||";
                    }
                }

                packetContent = packetContent.substring(0, packetContent.length -2); // remove last 2 parsing characters

            } else {
                // Server-side FIB contains no entries; nothing to send here
            }

            var data = ndnjs_utils.createDataPacket(packetContent, packetName);
            sendMessage(data.wireEncode(), hostIP, hostPort);

            FIB.getSpecificFIBData(userID, function(rowsTouched, queryResult) {

                // now, place newly registered/logged-in user into server-side FIB
                var fibEntry = DBData.DATA();
                fibEntry.fibData(userID, timeString, hostIP);

                if (rowsTouched === 0 || !queryResult) {
                    FIB.insertFIBData(fibEntry, function(){}); // user wasn't in FIB; insert now
                } else {
                    FIB.updateFIBData(fibEntry, function(){}); // user was in FIB; update now
                }
            });
        });
    }
    // client's login/register has not been validated; reply with an empty data packet
    else {

        /** 
         * Here userID is stored in sensorID position. We needed to send userID 
         * but had no place to do so and sensorID would have otherwise been null. 
         */

        var data;
        if (requestType === StringConst.REGISTER_REQUEST) {

            // set process id to REGISTER_RESULT
            packetName = ndnjs_utils.createName(StringConst.SERVER_ID, userID,
                timeString, StringConst.REGISTER_RESULT);

            data = ndnjs_utils.createDataPacket(StringConst.REGISTER_FAILED, packetName);
        } else {

            // only requestType left is login; set process id to LOGIN_RESULT
            packetName = ndnjs_utils.createName(StringConst.SERVER_ID, userID,
                timeString, StringConst.LOGIN_RESULT);

            data = ndnjs_utils.createDataPacket(StringConst.LOGIN_FAILED, packetName);
        }

        sendMessage(data.wireEncode(), hostIP, hostPort);
    }
}

/**
 * Performs requested mode analytic task and sends result to requestor.
 *
 * @param userID of data set on which to perform mode
 * @param sensorID of data set on which to perform mode
 * @param timeString of data set on which to perform mode
 * @param hostIP of user sending request
 * @param hostPort of user sending request
 */
function handleModeAnalyticTask(userID, sensorID, timeString, hostIP, hostPort) {

    getRequestedData(userID, sensorID, timeString, function(requestedData) {

        // data exists, perform mode
        if (requestedData && requestedData.length > 0) {

            var packetName = ndnjs_utils.createName(userID, sensorID, timeString, StringConst.MODE_ANALYTIC);

            var modeValue = analytics.mode(requestedData).toString();

            var data = ndnjs_utils.createDataPacket(modeValue, packetName);

            sendMessage(data.wireEncode(), hostIP, hostPort);
        }
    });
}

/**
 * Performs requested median analytic task and sends result to requestor.
 *
 * @param userID of data set on which to perform median
 * @param sensorID of data set on which to perform median
 * @param timeString of data set on which to perform media
 * @param hostIP of user sending request
 * @param hostPort of user sending request
 */
function handleMedianAnalyticTask(userID, sensorID, timeString, hostIP, hostPort) {

    getRequestedData(userID, sensorID, timeString, function(requestedData) {

        // data exists, perform median
        if (requestedData && requestedData.length > 0) {

            var packetName = ndnjs_utils.createName(userID, sensorID, timeString, StringConst.MEDIAN_ANALYTIC);

            var medianValue = analytics.median(requestedData).toString();

            var data = ndnjs_utils.createDataPacket(medianValue, packetName);

            sendMessage(data.wireEncode(), hostIP, hostPort);
        }
    });
}

/**
 * Performs requested mean analytic task and sends result to requestor.
 *
 * @param userID of data set on which to perform mean
 * @param sensorID of data set on which to perform mean
 * @param timeString of data set on which to perform mean
 * @param hostIP of user sending request
 * @param hostPort of user sending request
 */
function handleMeanAnalyticTask(userID, sensorID, timeString, hostIP, hostPort) {

    getRequestedData(userID, sensorID, timeString, function(requestedData) {

        // data exists, perform mean
        if (requestedData && requestedData.length > 0) {

            var packetName = ndnjs_utils.createName(userID, sensorID, timeString, StringConst.MEAN_ANALYTIC);

            var meanValue = analytics.mean(requestedData).toString();

            var data = ndnjs_utils.createDataPacket(meanValue, packetName);

            sendMessage(data.wireEncode(), hostIP, hostPort);
        }
    });
}

/**
 * Returns any data found given input params.
 *
 * @param userID of requested data
 * @param sensorID of requested data
 * @param timeString of requested data
 * @param callback used to pass back requested data
 */
function getRequestedData(userID, sensorID, timeString, callback) {

    CS.getGeneralCSData(userID, function(rowsTouched, queryResults) {

        if (rowsTouched > 0 && queryResults) {
            // TODO - improve upon this naive data-matching
            var matchingData = [];

            for (var i = 0; i < queryResults.length; i++) {

                if (queryResults[i].getSensorID() === sensorID
                    && utils.isValidForTimeInterval(timeString, queryResults[i].getTimeString())) {

                    // match found, place in array to be returned to caller function
                    matchingData.push(parseInt(queryResults[i].getDataPayload()));
                }
            }

            callback(matchingData);
        } else {

            callback(null); // no data found; drop
        }
    });
}

/**
 * Initiates synchronization request by sending Interest
 * to client requesting data within timeString interval.
 *
 * Here the userID was placed in sensorID position; the sensorID is null
 * (because no sensorID is used when we send a synch) and we need to include
 * the userID - so we place the userID as a sensorID.
 *
 * @param sensorID here is actually the userID
 * @param timeString of data that should be synched
 * @param hostIP of client requesting synch request
 * @param hostPort of client requesting synch request
 */
function handleSynchRequest(sensorID, timeString, hostIP, hostPort) {

    // create and send packet with ndn-js module
    var packetName = ndnjs_utils.createName(sensorID, StringConst.NULL_FIELD, timeString, StringConst.SYNCH_DATA_REQUEST);
    var interest = ndnjs_utils.createInterestPacket(packetName);

    // ask for synch data from requestor via INTEREST packet
    sendMessage(interest.wireEncode(), hostIP, hostPort);

    // add request to PIT
    var newPITEntry = DBData.DATA();

    newPITEntry.pitData(sensorID, StringConst.NULL_FIELD, StringConst.SYNCH_DATA_REQUEST, timeString, hostIP);
    PIT.insertPITData(newPITEntry, function(){});
}

/**
 * Invoked when client sends Interest to request the addition of a new
 * doctor. Here we send an Interest requesting the name of that doctor.
 *
 * TODO - note user of userID v. sensorID 
 *
 * @param userID of user requesting initiation of doctor-addition process
 * @param timeString associated with requested
 * @param hostIP associated with requested
 * @param hostPort associated with requested
 */
function handleAddDoctor(userID, timeString, hostIP, hostPort) {

    // create and send packet with ndn-js module
    var packetName = ndnjs_utils.createName(userID, StringConst.NULL_FIELD, timeString, StringConst.CLIENT_DOCTOR_SELECTION);
    var interest = ndnjs_utils.createInterestPacket(packetName);

    // ask for new doctor from requestor via INTEREST packet
    sendMessage(interest.wireEncode(), hostIP, hostPort);

    // add request to PIT
    var newPITEntry = DBData.DATA();

    newPITEntry.pitData(userID, StringConst.NULL_FIELD, StringConst.CLIENT_DOCTOR_SELECTION, timeString, hostIP);
    PIT.insertPITData(newPITEntry, function(){});
}
 
/**
 * Satisfies client's Interest packet requesting a list of the client's doctors.
 *
 * TODO -  && note use of userID v. sensorID
 * 
 * @param userID requesting a list of their doctors
 * @param timeString associated with request
 * @param hostIP associated with request
 * @param hostPort associated with request
 */          
function handleDoctorList(userID, timeString, hostIP, hostPort) {

    // TODO - look into database for doctor list and return

    USER_CREDENTIALS.getUserByID(userID, function(rowCount, queryResult) {

        // verify that user exists and is a valid patient
        if (rowCount === 1 && queryResult 
            && queryResult.getEntityType() === StringConst.PATIENT_USER_TYPE) {

            var doctorList = queryResult.getDoctorList();

            if (doctorList.indexOf("") !== -1) {
                doctorList.splice(doctorList.indexOf(""), 1); // remove empty string
            }

            doctorList = doctorList.join(); // join array into string

            // TODO - note use of placing into X

            var packetName = ndnjs_utils.createName(StringConst.SERVER_ID, userID, timeString, StringConst.DOCTOR_LIST);

            var data = ndnjs_utils.createDataPacket(doctorList, packetName);

            sendMessage(data.wireEncode(), hostIP, hostPort);
        }
    });
}

/**
 * TODO - do we even need this method?
 *
 * TODO - note user of userID v. sensorID
 *
 * @param userID of user requesting initiation of patient-addition process
 * @param timeString associated with requested
 * @param hostIP associated with requested
 * @param hostPort associated with requested
 */
function handleAddPatient(userID, timeString, hostIP, hostPort) {

    // create and send packet with ndn-js module
    var packetName = ndnjs_utils.createName(userID, StringConst.NULL_FIELD, timeString, StringConst.CLIENT_PATIENT_SELECTION);
    var interest = ndnjs_utils.createInterestPacket(packetName);

    // ask for new doctor from requestor via INTEREST packet
    sendMessage(interest.wireEncode(), hostIP, hostPort);

    // add request to PIT
    var newPITEntry = DBData.DATA();

    newPITEntry.pitData(userID, StringConst.NULL_FIELD, StringConst.CLIENT_PATIENT_SELECTION, timeString, hostIP);
    PIT.insertPITData(newPITEntry, function(){});
}

/**
 * Satisfies client's Interest packet requesting a list of the client's patients.
 *
 * TODO -  && note use of userID v. sensorID
 *
 * @param userID requesting a list of their patients
 * @param timeString associated with request
 * @param hostIP associated with request
 * @param hostPort associated with request
 */
function handlePatientList(userID, timeString, hostIP, hostPort) {

    // TODO - look into database for doctor list and return

    USER_CREDENTIALS.getUserByID(userID, function(rowCount, queryResult) {

        // verify that user exists and is a valid patient
        if (rowCount === 1 && queryResult 
            && queryResult.getEntityType() === StringConst.DOCTOR_USER_TYPE) {

            var patientList = queryResult.getPatientList();

            if (patientList.indexOf("") !== -1) {
                patientList.splice(patientList.indexOf(""), 1); // remove empty string
            }

            patientList = patientList.join(); // join array into string

            // TODO - note use of placing into X

            var packetName = ndnjs_utils.createName(StringConst.SERVER_ID, userID, timeString, StringConst.PATIENT_LIST);

            var data = ndnjs_utils.createDataPacket(patientList, packetName);

            sendMessage(data.wireEncode(), hostIP, hostPort);
        }
    });
}