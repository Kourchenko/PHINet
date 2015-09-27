
/**
 * Returns object that holds/manipulates User Data.
 */
exports.User = function () {

    return {

        // --- member variables that may be manipulated ---
        userID: null,
        firstName: null,
        lastName: null,
        email: null,
        password: null,
        gender: null,
        weightInPounds: null,
        heightInInches: null,
        dateOfBirth: null,
        lastLoginTime: null,
        lastLoginType: null,
        // --- member variables that may be manipulated ---

        /**
         * constructor for user
         */
        user: function (userID,
                        firstName,
                        lastName,
                        email,
                        password,
                        gender,
                        weightInPounds,
                        heightInInches,
                        dateOfBirth,
                        lastLoginTime,
                        lastLoginType) {

            this.userID = userID;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.password = password;
            this.gender = gender;
            this.weightInPounds = weightInPounds;
            this.heightInInches = heightInInches;
            this.dateOfBirth = dateOfBirth;
            this.lastLoginTime = lastLoginTime;
            this.lastLoginType = lastLoginType;
        }
    }
};
