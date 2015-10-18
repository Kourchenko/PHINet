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

        var regex = /^[a-zA-Z0-9_]{3,15}$/;
        return password && regex.test(password);
    },

    /**
     * Returns true if username (3-15 alphanumerics plus underscore) valid.
     *
     * @param username input by user
     * @returns {boolean} determining validity of username
     */
    isValidUserName: function(username) {

        // NOTE: keep username/password functions separate because syntax may change

        var regex = /^[a-zA-Z0-9_]{3,15}$/;
        return username && regex.test(username);
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

        var re = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;
        return email && re.test(email);
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
                    callback(err, null);

                bcrypt.hash(password, salt, function(err, hash) {
                    callback(err, hash);
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
                    callback(err, false);
                callback(null, isPasswordMatch);
            });
        }
	},

    /**
     * Expects and validates
     *  - userID
     *  - password
     *  - email
     */
    isValidSignupRequest : function(request) {

        // TODO - improve validations on password hash

        return request && this.isValidEmail(request.email)
                    && this.isValidUserName(request.userID)
                    && request.password;
    }
};
