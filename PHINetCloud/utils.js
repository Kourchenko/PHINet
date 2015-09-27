/**
 * File contains code for miscellaneous functions.
 */

var bcrypt = require('bcrypt');

exports.Utils = {

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
	}
};
