
/**
 * Returns object that holds/manipulates User Data.
 */
exports.User = function () {

    return {

        // --- member variables that may be manipulated ---
        userID: null,
        password: null,
        email: null,
        // --- member variables that may be manipulated ---

        /**
         * constructor for user
         *
         * @param userID associated with a given user
         * @param password associated with a given user
         * @param email associated with a given user
         */
        user: function (userID, password, email) {

            this.userID = userID;
            this.password = password;
            this.email = email;
        },

        getUserID : function() {
            return this.userID;
        },

        setUserID : function(userID) {
            this.userID = userID;
        },

        getPassword : function() {
            return this.password;
        },

        setPassword : function(password) {
            this.password = password;
        },

        getEmail : function () {
            return this.email;
        },

        setEmail: function(email) {
            this.email = email;
        }
    }
};