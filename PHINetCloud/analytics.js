
var gauss = require('gauss'); // node module used for data analytics

/**
 * Returns data analytics module.
 */
exports.analytics = function() {

    // TODO - perform/test more complex analytics

    return {

        /**
         * Returns mean of input.
         *
         * @param data - array of values
         * @returns real valued mean
         */
        mean : function(data, callback) {

            if (data) {
                var vector = new gauss.Vector(data);

                callback(vector.mean());
            } else {

                throw "!!Error: analytics.mean() input is invalid.";
            }
        },

        /**
         * Returns mode of input.
         *
         * @param data - array of values
         * @returns real valued mode
         */
        mode : function(data, callback) {

            if (data) {
                var vector = new gauss.Vector(data);

                callback(vector.mode());

            } else {
                throw "!!Error: analytics.mode() input is invalid.";
            }
        },

        /**
         * Returns median of input.
         *
         * @param data - array of values
         * @returns real valued median
         */
        median: function(data, callback) {

            if (data) {
                var vector = new gauss.Vector(data);

                callback(vector.median());
            } else {
                throw "!!Error: analytics.median() input is invalid";
            }
        }
    }
};