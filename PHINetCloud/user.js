
var StringConst = require('./string_const').StringConst;

/**
 * Returns object that holds/manipulates User Data.
 */
exports.User = function () {

    return {

        // --- member variables that may be manipulated ---
        userID: null,
        firstName: null,
        lastName: null,
        gender: null,
        weight: null,
        height: null,
        DOB: null,
        entityType = null;

        password: null,
        email: null,
        lastloginType: null,


        // --- member variables that may be manipulated ---

        /**
         * constructor for user
         *
         * @param userID associated with a given user
         * @param entityType associated with a given user
         * @param firstName associated with a given user
         * @param lastName associated with a given user
         * @param gender associated with a given user
         * @param weight associated with a given user
         * @param height associated with a given user
         * @param DOB associated with a given user
         * @param password associated with a given user
         * @param email associated with a given user
         */
        user: function (userID,
                        firstName,
                        lastName,
                        DOB,
                        gender,
                        height,
                        weight,
                        password,
                        email,
                        lastloginType) {

            this.userID = userID;
            this.firstName = firstName;
            this.lastName = lastName;
            this.DOB = dateOfBirth;
            this.gender = gender;
            this.weight = weight;
            this.height = height;
            this.password = password;
            this.email = email;
            this.lastLoginTime = lastLoginTime;
            this.entityType = entityType;

        },

        getUserID : function() {
            return this.userID;
        },

        setUserID : function(userID) {
            this.userID = userID;
        },

        setfirstName: function(firstName) {
            this.firstName = firstName;
        },

        getfirstName: function(firstName) {
            return this.firstName;
        },

        setlastName: function(lastName) {
            this.lastName = lastName;
        },

        getLastName: function(lastName) {
            return this.lastName;
        },

        setDOB: function(dateOfBirth) {
            this.DOB = dateOfBirth;
        },

        getDOB: function() {
            return this.DOB;
        },

        setGender: function(gender) {
            this.gender = gender;
        },

        getGender: function() {
            return this.gender;
        },

        setWeight: function(weight) {
            this.weight = weight;
        },

        getWeight: function() {
            return this.weight;
        },

        setHeight: function(height) {
            this.height = height;
        },

        getHeight: function() {
            return this.height;
        },

        setPassword : function(password) {
            this.password = password;
        },

        getPassword : function() {
            return this.password;
        },

        setEmail: function(email) {
            this.email = email;
        },

        getEmail : function () {
            return this.email;
        },

        setlastLoginTime: function(lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
         },

        getlastLoginTime: function() {
            return this.lastLoginTime;
        },

        setEntityType: function(entityType) {
          this.entityType = entityType;
        },

        getEntityType: function() {
            return this.entityType;
        }

    }
};
