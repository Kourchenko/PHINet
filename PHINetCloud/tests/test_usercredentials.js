/**
 * File provides test cases for the Pending Interest Table implemented in pit.js.
 */

var expect = require("chai").expect;
var StringConst = require('../string_const').StringConst;
var LoginDB = require('../usercredentials.js').LoginCredentials(StringConst.LOGIN_TEST_DB);
var UserClass = require('../user');

// --- test entries ---

var user1 = UserClass.User();
user1.user("uid111", "pw1", "email1",StringConst.DOCTOR_USER_TYPE);

var user2 = UserClass.User();
user2.user("uid222", "pw2", "email2",StringConst.PATIENT_USER_TYPE);

var user3 = UserClass.User();
user3.user("uid123", "pw3", "email1", StringConst.DOCTOR_USER_TYPE); // test that this is rejected (same email as user1)


// --- test entries ---

/**
 * Tests LoginCredentials.insertNewUser() functionality.
 */
describe('LoginCredentials', function(){
    describe('#insertNewUser()', function(){
        it('returns true of insertion was successful, otherwise returns false', function(done) {

            // delete first, before tests
            LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

            });

            LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

            });

            // test insert on bad data
            expect(LoginDB.insertNewUser(null, null, null, null)).to.equal(false); // rejected due to null params
            expect(LoginDB.insertNewUser(user2.getUserID(), user2.getPassword(), user2.getEmail(),
                                        "INVALID ENTITY TYPE: ERROR!", function(){})); // rejected due to invalid entity type

            // TODO - rework this horrible structure

            // test that insertion of user1 is valid
            LoginDB.insertNewUser(user1.getUserID(), user1.getPassword(), user1.getEmail(),
                user1.getEntityType(), function(rowsTouched){

                    expect(rowsTouched === 1).to.equal(true);

                    // test that insertion of user2 is valid
                    LoginDB.insertNewUser(user2.getUserID(), user2.getPassword(), user2.getEmail(),
                        user2.getEntityType(), function(rowsTouched){

                            expect(rowsTouched === 1).to.equal(true);

                            // test that redundant data (user2 inserted again) is rejected
                            LoginDB.insertNewUser(user2.getUserID(), user2.getPassword(), user2.getEmail(),
                                user2.getEntityType(), function(rowsTouched){

                                    expect(rowsTouched === 0).to.equal(true);

                                    // now, delete user2
                                    LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {
                                        expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

                                        // input should be rejected (the same email has been added previously by user1)
                                        LoginDB.insertNewUser(user3.getUserID(), user3.getPassword(), user3.getEmail(),
                                            user3.getEntityType(), function(rowsTouched) {

                                                expect(rowsTouched === 0).to.equal(true);

                                                // however, same input should be accepted if email is changed to the default NULL_FIELD
                                                LoginDB.insertNewUser(user3.getUserID(), user3.getPassword(), StringConst.NULL_FIELD,
                                                    user3.getEntityType(), function(rowsTouched) {

                                                        expect(rowsTouched === 1).to.equal(true);

                                                        // delete user3 before exiting tests
                                                        LoginDB.deleteUser(user3.getUserID(), function(rowsTouched) {
                                                            expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

                                                            // delete user1 before exiting tests
                                                            LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {

                                                                expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

                                                                done();
                                                            });

                                                        });
                                                    });
                                            });
                                    });

                                });
                        });

                });
        })
    });
});

/**
 * Tests LoginCredentials.getUserByID() functionality.
 */
describe('LoginCredentials', function(){
    describe('#getUserByEmail()', function(){
        it('returns true if get was valid, otherwise false', function(done) {

            // TODO - rework this horrible structure

            // delete first, before tests
            LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

                LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {
                    expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

                    // insert data before testing get
                    LoginDB.insertNewUser(user1.getUserID(), user1.getPassword(), user1.getEmail(),
                        user1.getEntityType(), function(rowsTouched) {

                            expect(rowsTouched === 1).to.equal(true);

                            LoginDB.insertNewUser(user2.getUserID(), user2.getPassword(), user2.getEmail(),
                                user2.getEntityType(), function(rowsTouched) {

                                    expect(rowsTouched === 1).to.equal(true);
                                });

                            // test get on bad data
                            expect(LoginDB.getUserByEmail(null,null)).to.equal(false);

                            // test get on good data
                            LoginDB.getUserByEmail(user1.getEmail(), function(rowsTouched, queryResult) {

                                expect(rowsTouched === 1).to.equal(true);

                                // an array is returned, only 1 row should have been touched, thus check index 0
                                expect(queryResult[0].getUserID() === user1.getUserID()).to.equal(true);
                                expect(queryResult[0].getPassword() === user1.getPassword()).to.equal(true);
                                expect(queryResult[0].getEmail() === user1.getEmail()).to.equal(true);

                                LoginDB.getUserByEmail(user2.getEmail(), function(rowsTouched, queryResult) {

                                    expect(rowsTouched === 1).to.equal(true);

                                    // an array is returned, only 1 row should have been touched, thus check index 0
                                    expect(queryResult[0].getUserID() === user2.getUserID()).to.equal(true);
                                    expect(queryResult[0].getPassword() === user2.getPassword()).to.equal(true);
                                    expect(queryResult[0].getEmail() === user2.getEmail()).to.equal(true);

                                    // delete before exiting tests

                                    LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {
                                        expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

                                        LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {
                                            expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

                                            done();
                                        });
                                    });
                                });

                            });

                        });

                });
            });
        })
    })
});

/**
 * Tests LoginCredentials.getUserByID() functionality.
 */
describe('LoginCredentials', function(){
    describe('#getUserByID()', function(){
        it('returns true if get was valid, otherwise false', function(done) {

            // TODO - rework this horrible structure

            // delete first, before tests
            LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

                LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {
                    expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

                });
            });



            // insert data before testing get
            LoginDB.insertNewUser(user1.getUserID(), user1.getPassword(), user1.getEmail(),
                user1.getEntityType(), function(rowsTouched){

                    expect(rowsTouched === 1).to.equal(true);

                    LoginDB.insertNewUser(user2.getUserID(), user2.getPassword(), user2.getEmail(),
                        user2.getEntityType(), function(rowsTouched){

                            expect(rowsTouched === 1).to.equal(true);

                            // test get on bad data
                            expect(LoginDB.getUserByID(null,null)).to.equal(false);

                            // test get on good data
                            LoginDB.getUserByID(user1.getUserID(), function(rowsTouched, queryResult) {

                                expect(rowsTouched === 1).to.equal(true);

                                expect(queryResult.getUserID() === user1.getUserID()).to.equal(true);
                                expect(queryResult.getPassword() === user1.getPassword()).to.equal(true);

                                LoginDB.getUserByID(user2.getUserID(), function(rowsTouched, queryResult) {

                                    expect(rowsTouched === 1).to.equal(true);

                                    expect(queryResult.getUserID() === user2.getUserID()).to.equal(true);
                                    expect(queryResult.getPassword() === user2.getPassword()).to.equal(true);

                                    // delete before exiting tests

                                    LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {
                                        expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

                                        LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {
                                            expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

                                            done();
                                        });
                                    });
                                });
                            });
                        });
                });

        })
    })
});

/**
 * Tests LoginCredentials.updateUser() functionality.
 */
describe('LoginCredentials', function(){
    describe('#updateUser()', function(){
        it('returns true if update was successful, otherwise false', function(done) {


            // TODO - rework this horrible structure


            /// delete first, before tests
            LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

                LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {
                    expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

                });
            });



            // insert data before testing update
            LoginDB.insertNewUser(user1.getUserID(), user1.getPassword(), user1.getEmail(),
                user1.getEntityType(), function(rowsTouched){

                    expect(rowsTouched === 1).to.equal(true);

                    LoginDB.insertNewUser(user2.getUserID(), user2.getPassword(), user2.getEmail(),
                        user2.getEntityType(), function(rowsTouched){

                            expect(rowsTouched === 1).to.equal(true);

                            // update data locally
                            user1.setPassword("NEW_PASSWORD1");

                            user2.setPassword("NEW_PASSWORD2");
                            user2.setEntityType(StringConst.DOCTOR_ENTITY); // test valid entity update

                            // test update on bad data
                            expect(LoginDB.updateUser(null, null, null, null, null)).to.equal(false);

                            // test that bad entity update is rejected
                            expect(LoginDB.updateUser(user1.getUserID(), user1.getPassword(),
                                user1.getEmail(), "INVALID ENTITY TYPE: ERRROR!", function(){})).to.equal(false);

                            // test that valid update is accepted
                            LoginDB.updateUser(user2.getUserID(), user2.getPassword(), user2.getEmail(), user2.getEntityType(),

                                function(rowsTouched) {
                                    expect(rowsTouched === 1).to.equal(true); // verify that 1 row was updated

                                    // get user to verify update took place
                                    LoginDB.getUserByID(user2.getUserID(), function(rowsTouched, queryResult) {

                                        expect(rowsTouched === 1).to.equal(true);

                                        expect(queryResult.getUserID() === user2.getUserID()).to.equal(true);
                                        expect(queryResult.getPassword() === user2.getPassword()).to.equal(true);
                                        expect(queryResult.getEntityType() === user2.getEntityType()).to.equal(true);

                                        // delete before exiting tests
                                        LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {
                                            expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

                                            LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {
                                                expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

                                                done();
                                            });
                                        });
                                    });
                                });
                        });
                });
        })
    })
});

/**
 * Tests LoginCredentials.deleteUser() functionality.
 */
describe('LoginCredentials', function(){
    describe('#deleteUser()', function(){
        it('returns true if deletion was successful, otherwise false', function(done) {

            // TODO - rework this horrible structure

            // delete first, before tests
            LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

                LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {
                    expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

                    // insert data before testing deletion
                    LoginDB.insertNewUser(user1.getUserID(), user1.getPassword(), user1.getEmail(),
                        user1.getEntityType(), function(rowsTouched){

                            expect(rowsTouched === 1).to.equal(true);

                            LoginDB.insertNewUser(user2.getUserID(), user2.getPassword(), user2.getEmail(),
                                user2.getEntityType(), function(rowsTouched){

                                    expect(rowsTouched === 1).to.equal(true);

                                    // test deletion on bad data
                                    expect(LoginDB.deleteUser(null, null)).to.equal(false);

                                    // test deletion on good data
                                    LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {

                                        expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

                                        LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {

                                            expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

                                            done();
                                        });
                                    });
                                });

                        });
                });
            });
        })
    })
});

