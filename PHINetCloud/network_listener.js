/** 
 * File contains code for the object that enables Network communication
 */

var StringConst = require('./string_const').StringConst;

var USER_CREDENTIALS;

var utils = require('./utils.js').Utils;
var analytics = require('./analytics.js').analytics();

var NET_PORT = 50056; // same across all applications

/**
 * Returns object that handles majority of Network communication. References to the core
 * databases are passed to the module so that relevant queries can be performed.
 *
 * @param ucReference - reference to the USER_CREDENTIAL database
 */
exports.NetworkListener = function(ucReference) {

    USER_CREDENTIALS = ucReference;

	return {

        /**
         * Method initializes the network listener to specified PORT.
         */
		initializeListener : function () {

            // you must declare these here so that they will be in scope in the callback
            var handlePacket = this.handlePacket;

           /*
                TODO - find appropriate networking library

           socket.bind(NDN_SENSOR_NET_PORT);
			socket.on('message', function(msg, rinfo) {

                if (!isRateLimited(rinfo.address)) {

                    console.log("message found: " + msg);

                    try {
                        handlePacket(msg);
                    } catch (e) {
                        console.log("Something went wrong. Unable to parse packet. Error: " + e);
                    }
                }
			});*/
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
         * TODO - doc
         *
         * @param packet incoming packet after having been decoded
         * @param packetIP specifies IP that will receive reply if parse success
         * @param packetPort specifies IP that will receive reply if parse success
         */
        handlePacket: function (packet, packetIP, packetPort) {
            // TODO
        }
    };
};