/**
 * File provides test cases for the Pending Interest Table implemented in pit.js.
 */

var expect = require("chai").expect;
var StringConst = require('../string_const').StringConst;
var PIT = require('../PIT.js').PIT(StringConst.PIT_TEST_DB);
var DBData = require('../data'); // used to create objects used by the database

// --- test entries ---

var entry1 = DBData.DATA();
entry1.pitData("serverTestUser1", "serverTestSensor1",
		StringConst.DATA_CACHE, StringConst.CURRENT_TIME, "10.12.13.14");

var entry2 = DBData.DATA();
entry2.pitData("serverTestUser2", "serverTestSensor2",
		StringConst.DATA_CACHE, StringConst.CURRENT_TIME, "10.88.11.22");

// --- test entries ---

/**
 * Tests PIT.insertPITData() functionality.
 */
describe('PIT', function(){
    describe('#insertPITData()', function(){
        it('should return true if entry has been made', function(done) {

            // TODO - rework this horrible structure

            // first delete entries before testing addition
            PIT.deletePITData(entry1.getUserID(), entry1.getTimeString(), entry1.getIpAddr(),
                function (rowsTouched) {
                    expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted

                    PIT.deletePITData(entry2.getUserID(), entry2.getTimeString(), entry2.getIpAddr(),
                        function(rowsTouched) {
                            expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted

                            // test clearly bad input; should be rejected at start
                            expect(PIT.insertPITData(null)).to.equal(false);
                            expect(PIT.insertPITData(undefined)).to.equal(false);
                            expect(PIT.insertPITData(DBData.DATA())).to.equal(false); // "empty" object should fail

                            // test good input
                            PIT.insertPITData(entry1, function(rowsTouched) {
                                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified

                                PIT.insertPITData(entry2, function(rowsTouched) {
                                    expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified

                                    // test redundant input
                                    PIT.insertPITData(entry1, function(rowsTouched) {
                                        expect(rowsTouched === 0).to.equal(true);

                                        // finally, delete entries after testing addition
                                        PIT.deletePITData(entry1.getUserID(), entry1.getTimeString(), entry1.getIpAddr(),
                                            function(rowsTouched) {
                                                expect(rowsTouched === 1).to.equal(true);

                                                PIT.deletePITData(entry2.getUserID(), entry2.getTimeString(), entry2.getIpAddr(),
                                                    function(rowsTouched) {
                                                        expect(rowsTouched === 1).to.equal(true);

                                                        done(); // the invocation of done() tells testing framework that all tests are complete
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
 * Tests PIT.updatePITData() functionality.
 */
describe('PIT', function(){
    describe('#updatePITData()', function(){
        it('should return true if update has been made', function(done){

            // TODO - rework this horrible structure

            // first delete (potentially stored) entries before testing update
            PIT.deletePITData(entry1.getUserID(), entry1.getTimeString(), entry1.getIpAddr(),
                function (rowsTouched) {
                    expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted

                    PIT.deletePITData(entry2.getUserID(), entry2.getTimeString(), entry2.getIpAddr(),
                        function(rowsTouched) {
                            expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted

                            // test rejection given bad input
                            expect(PIT.updatePITData(null)).to.equal(false);
                            expect(PIT.updatePITData(undefined)).to.equal(false);
                            expect(PIT.updatePITData(DBData.DATA())).to.equal(false); // an "empty" object

                            // now, place data into DB
                            PIT.insertPITData(entry1, function(rowsTouched) {
                                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified

                                PIT.insertPITData(entry2, function(rowsTouched) {
                                    expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified

                                    // update entry time
                                    entry1.setTimeString(StringConst.CURRENT_TIME);
                                    entry2.setTimeString(StringConst.CURRENT_TIME);

                                    // test valid update given good input
                                    PIT.updatePITData(entry1, function(rowsTouched) {
                                        expect(rowsTouched === 1).to.equal(true); // verify that 1 row was updated

                                        PIT.updatePITData(entry2, function(rowsTouched) {

                                            expect(rowsTouched === 1).to.equal(true); // verify that 1 row was updated

                                            // test that update worked; check that new value is present

                                            PIT.getSpecificPITData(entry1.getUserID(), entry1.getTimeString(), entry1.getIpAddr(),

                                                function(rowsTouched, queryResult) {

                                                    // verify that only entry (of two by same user) was found
                                                    expect(rowsTouched === 1).to.equal(true);

                                                    // check that single entry was found and returned
                                                    expect(queryResult.getUserID() === entry1.getUserID()).to.equal(true);

                                                    PIT.getSpecificPITData(entry2.getUserID(), entry2.getTimeString(), entry2.getIpAddr(),

                                                        function(rowsTouched, queryResult) {

                                                            // verify that only entry (of two by same user) was found
                                                            expect(rowsTouched === 1).to.equal(true);

                                                            // check that single entry was found and returned
                                                            expect(queryResult.getUserID() === entry2.getUserID()).to.equal(true);

                                                            // delete entries now that test is over
                                                            PIT.deletePITData(entry1.getUserID(), entry1.getTimeString(), entry1.getIpAddr(),
                                                                function (rowsTouched) {
                                                                    expect(rowsTouched === 1).to.equal(true); // verify that 1 or fewer rows were deleted

                                                                    PIT.deletePITData(entry2.getUserID(), entry2.getTimeString(), entry2.getIpAddr(),
                                                                        function(rowsTouched) {
                                                                            expect(rowsTouched === 1).to.equal(true); // verify that 1 or fewer rows were deleted

                                                                            done(); // the invocation of done() tells testing framework that all tests are complete
                                                                        });
                                                                });

                                                        });
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
 * Tests PIT.deletePITData() functionality.
 */
describe('PIT', function(){
    describe('#deletePITData()', function(){
        it('should return true if deletion has been made', function(done){

            // TODO - rework this horrible structure

            // before deletion test, attempt to input data into PIT
            PIT.insertPITData(entry1, function(rowsTouched){});
            PIT.insertPITData(entry2, function(rowsTouched){});

            // test deletion given good input
            PIT.deletePITData(entry1.getUserID(), entry1.getTimeString(), entry1.getIpAddr(),
                function(rowsTouched) {
                    expect(rowsTouched === 1).to.equal(true); // verify deletion of 1 row

                    PIT.deletePITData(entry2.getUserID(), entry2.getTimeString(), entry2.getIpAddr(),
                        function(rowsTouched) {
                            expect(rowsTouched === 1).to.equal(true); // verify deletion of 1 row

                            // test deletion given bad input
                            expect(PIT.deletePITData(undefined, undefined, undefined, undefined)).to.equal(false);

                            // object no longer exists in DB, verify that no rows were modified
                            PIT.deletePITData(entry1.getUserID(), entry1.getTimeString(), entry1.getIpAddr(),
                                function(rowsTouched) {
                                    expect(rowsTouched === 0).to.equal(true);

                                    done(); // the invocation of done() tells testing framework that all tests are complete
                                });
                        });
                });
        })
    })
});

/**
 * Tests PIT.getGeneralPITData() functionality.
 */
describe('PIT', function(){
    describe('#getGeneralPITData()', function(){
        it('should return true if data found, otherwise false', function(done){

            // TODO - rework this horrible structure

            // first delete (potentially stored) entries before testing get
            PIT.deletePITData(entry1.getUserID(), entry1.getTimeString(), entry1.getIpAddr(),
                function (rowsTouched) {
                    expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted

                    PIT.deletePITData(entry2.getUserID(), entry2.getTimeString(), entry2.getIpAddr(),
                        function(rowsTouched) {
                            expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted

                            // test method against bad data
                            expect(PIT.getGeneralPITData(null, null, null)).to.equal(false);

                            // insert good data
                            PIT.insertPITData(entry1, function(rowsTouched) {
                                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified

                                PIT.insertPITData(entry2, function(rowsTouched) {
                                    expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified

                                    PIT.getGeneralPITData(entry2.getUserID(), entry2.getIpAddr(), function(rowsTouched, queryResult) {

                                        // verify that both entries from serverTestUser2 were found
                                        expect(rowsTouched === 1).to.equal(true);

                                        // verify that both entries from serverTestUser2 were returned
                                        expect(queryResult.length === 1).to.equal(true);

                                        var entry2Found = false;

                                        for (var i = 0; i < queryResult.length; i++) {

                                            if (queryResult[i].getSensorID() === entry2.getSensorID()) {
                                                entry2Found = true;
                                            }
                                        }

                                        // check that both entries were found and returned
                                        expect(entry2Found).to.equal(true);

                                        // delete entries now that test is over
                                        PIT.deletePITData(entry1.getUserID(), entry1.getTimeString(), entry1.getIpAddr(),
                                            function (rowsTouched) {
                                                expect(rowsTouched === 1).to.equal(true); // verify that 1 or fewer rows were deleted

                                                PIT.deletePITData(entry2.getUserID(), entry2.getTimeString(), entry2.getIpAddr(),
                                                    function(rowsTouched) {
                                                        expect(rowsTouched === 1).to.equal(true); // verify that 1 or fewer rows were deleted

                                                        done(); // the invocation of done() tells testing framework that all tests are complete
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
 * Tests PIT.getSpecificPITData() functionality.
 */
describe('PIT', function(){
    describe('#getSpecificPITData()', function(){
        it('should return true if specific entry found, otherwise false', function(done){

            // TODO - rework this horrible structure

            // first delete (potentially stored) entries before testing get
            PIT.deletePITData(entry1.getUserID(), entry1.getTimeString(), entry1.getIpAddr(),
                function (rowsTouched) {
                    expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted

                    PIT.deletePITData(entry2.getUserID(), entry2.getTimeString(), entry2.getIpAddr(),
                        function(rowsTouched) {
                            expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted

                            // test method against bad data
                            expect(PIT.getSpecificPITData(null, null)).to.equal(false);

                            // insert good data
                            PIT.insertPITData(entry1, function(rowsTouched) {
                                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified

                                PIT.insertPITData(entry2, function(rowsTouched) {
                                    expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified

                                    PIT.getSpecificPITData(entry2.getUserID(), entry2.getTimeString(), entry2.getIpAddr(),

                                        function(rowsTouched, queryResult) {

                                            // verify that only entry (of two by same user) was found
                                            expect(rowsTouched === 1).to.equal(true);

                                            // check that single entry was found and returned
                                            expect(queryResult.getSensorID() === entry2.getSensorID()).to.equal(true);

                                            // delete entries now that test is over
                                            PIT.deletePITData(entry1.getUserID(), entry1.getTimeString(), entry1.getIpAddr(),
                                                function (rowsTouched) {
                                                    expect(rowsTouched === 1).to.equal(true); // verify that 1 or fewer rows were deleted

                                                    PIT.deletePITData(entry2.getUserID(), entry2.getTimeString(), entry2.getIpAddr(),
                                                        function(rowsTouched) {
                                                            expect(rowsTouched === 1).to.equal(true); // verify that 1 or fewer rows were deleted

                                                            done(); // the invocation of done() tells testing framework that all tests are complete
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
