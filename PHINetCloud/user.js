
var StringConst = require('./string_const').StringConst;

/**
 * Returns object that holds/manipulates User Data.
 */
exports.User = function () {

    return {

        // --- member variables that may be manipulated ---
        userID: null,
        password: null,
        email: null,
        entityType: null,
        doctorList: null,
        patientList: null,
        // --- member variables that may be manipulated ---

        /**
         * constructor for user
         *
         * @param userID associated with a given user
         * @param password associated with a given user
         * @param email associated with a given user
         * @param entityType associated with a given user
         */
        user: function (userID, password, email, entityType) {

            if (entityType !== StringConst.DOCTOR_USER_TYPE
                            && entityType !== StringConst.PATIENT_USER_TYPE) {
                throw "!! Error in User constructor: entity is of invalid type \'" + entityType + "\' .";
            }

            this.userID = userID;
            this.password = password;
            this.email = email;
            this.entityType = entityType;

            // set to empty string now; add doctors/patients (if any) later
            this.doctorList = ""; 
            this.patientList = "";
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
        },

        getEntityType : function() {
            return this.entityType;
        },

        setEntityType : function(entityType) {
            this.entityType = entityType;
        },

        getDoctorList : function() {
            // Syntax for Doctor list is "doctor_1,...,doctor_n"

            if (!this.doctorList) {
                return [];
            } else {
                return this.doctorList.split(",");
            }
        }, 

        setDoctorList : function(doctorList) {
            this.doctorList = doctorList;
        },

        getPatientList: function() {

            if (!this.patientList) {
                return [];
            } else {
                // Syntax for Patient list is "patient_1,...,patient_n"
                return this.patientList.split(",");
            }
        }, 

        setPatientList : function(patientList) {
            return this.patientList = patientList;
        }
    }
};