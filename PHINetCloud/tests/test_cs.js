/**
 * File provides test cases for Content Store implemented in cs.js.
 */

var expect = require("chai").expect;
var StringConst = require('../string_const').StringConst;
var ContentStore = require('../cs.js').CS(StringConst.CS_TEST_DB);
var DBData = require('../data.js'); // used to create objects used by the database

// --- test entries ---

var entry1 = DBData.DATA();
entry1.csData("serverTestUser1", "serverTestSensor1", 
		StringConst.DATA_CACHE, StringConst.CURRENT_TIME, "10,11,12,13,14,15");

var entry2 = DBData.DATA();
entry2.csData("serverTestUser2", "serverTestSensor2", 
		StringConst.DATA_CACHE, StringConst.CURRENT_TIME, "10,11,12,13,14,15");

var entry3 = DBData.DATA();
entry3.csData("serverTestUser2", "serverTestSensor3",
            StringConst.DATA_CACHE, "yesterday", "55,66");

// --- test entries ---

/**
 * Tests ContentStore.insertCSData() functionality.
 */
describe('ContentStore', function(){
    describe('#insertCSData()', function(){
        it('should return true if entry has been made', function(done) {

            // first delete entries before testing addition
            ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString(), function (rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            ContentStore.deleteCSData(entry2.getUserID(), entry2.getTimeString(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            // test clearly bad input; should be rejected at start
            expect(ContentStore.insertCSData(null)).to.equal(false);
            expect(ContentStore.insertCSData(undefined)).to.equal(false);
            expect(ContentStore.insertCSData(DBData.DATA())).to.equal(false); // "empty" object should fail

            // test good input
            ContentStore.insertCSData(entry1, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            ContentStore.insertCSData(entry2, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            // test redundant input
            ContentStore.insertCSData(entry1, function(rowsTouched) {
                 expect(rowsTouched === 0).to.equal(true);
             });

            // finally, delete entries after testing addition
            ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true);
            });

            ContentStore.deleteCSData(entry2.getUserID(), entry2.getTimeString(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true);

                done(); // the invocation of done() tells testing framework that all tests are complete
            });
        })
    })
});

/**
 * Tests ContentStore.updateCSData() functionality.
 */
describe('ContentStore', function(){
    describe('#updateCSData()', function(){
        it('should return true if update has been made', function(done){

            // first delete (potentially stored) entries before testing update
            ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString(), function (rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });
            ContentStore.deleteCSData(entry2.getUserID(), entry2.getTimeString(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            // test rejection given bad input
            expect(ContentStore.updateCSData(null)).to.equal(false);
            expect(ContentStore.updateCSData(undefined)).to.equal(false);
            expect(ContentStore.updateCSData(DBData.DATA())).to.equal(false); // an "empty" object

            // now, place data into DB
            ContentStore.insertCSData(entry1, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            ContentStore.insertCSData(entry2, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            var ENTRY1_NEW_DATA = "100,101,102", ENTRY2_NEW_DATA = "9999";

            // modify data
            entry1.setDataFloat(ENTRY1_NEW_DATA);
            entry2.setDataFloat(ENTRY2_NEW_DATA);

            // test valid update given good input
            ContentStore.updateCSData(entry1, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was updated
            });

            ContentStore.updateCSData(entry2, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was updated
            });

            // test that update worked; check that new value is present

            ContentStore.getSpecificCSData(entry1.getUserID(), entry1.getTimeString(),

                function(rowsTouched, queryResult) {

                    // verify that only entry (of two by same user) was found
                    expect(rowsTouched === 1).to.equal(true);

                    // check that single entry was found and returned
                    expect(queryResult.getDataFloat() === ENTRY1_NEW_DATA).to.equal(true);
            });

            ContentStore.getSpecificCSData(entry2.getUserID(), entry2.getTimeString(),

                function(rowsTouched, queryResult) {

                    // verify that only entry (of two by same user) was found
                    expect(rowsTouched === 1).to.equal(true);

                    // check that single entry was found and returned
                    expect(queryResult.getDataFloat() === ENTRY2_NEW_DATA).to.equal(true);
            });

            // finally, delete entries now that test is over
            ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true);
            });

            ContentStore.deleteCSData(entry2.getUserID(), entry2.getTimeString(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true);

                done(); // the invocation of done() tells testing framework that all tests are complete
            });
        })
    })
});

/**
 * Tests ContentStore.deleteCSData() functionality.
 */
describe('ContentStore', function(){
    describe('#deleteCSData()', function(){
        it('should return true if deletion has been made', function(done){

            // before deletion test, attempt to input data into CS
            ContentStore.insertCSData(entry1, function(rowsTouched){});
            ContentStore.insertCSData(entry2, function(rowsTouched){});

            // test deletion given good input
            ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString(),
                function(rowsTouched) {
                    expect(rowsTouched === 1).to.equal(true); // verify deletion of 1 row
                });

            ContentStore.deleteCSData(entry2.getUserID(), entry2.getTimeString(),
                function(rowsTouched) {
                    expect(rowsTouched === 1).to.equal(true); // verify deletion of 1 row
                });

            // test deletion given bad input
            expect(ContentStore.deleteCSData(undefined, undefined)).to.equal(false);

            // object no longer exists in DB, verify that no rows were modified
            ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString(),
                function(rowsTouched) {
                    expect(rowsTouched === 0).to.equal(true);

                    done(); // the invocation of done() tells testing framework that all tests are complete
                });
        })
    })
});

/**
 * Tests ContentStore.getGeneralCSData() functionality.
 */
describe('ContentStore', function(){
    describe('#getGeneralCSData()', function(){
        it('should return true if data found, otherwise false', function(done){

            // first delete (potentially stored) entries before testing get
            ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString(), function (rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            ContentStore.deleteCSData(entry2.getUserID(), entry2.getTimeString(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            ContentStore.deleteCSData(entry3.getUserID(), entry3.getTimeString(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            // test method against bad input
            expect(ContentStore.getGeneralCSData(null, null)).to.equal(false);

            // insert good data
            ContentStore.insertCSData(entry1, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            ContentStore.insertCSData(entry2, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            ContentStore.insertCSData(entry3, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            ContentStore.getGeneralCSData(entry2.getUserID(), function(rowsTouched, queryResult) {


                // verify that both entries from serverTestUser2 were found
                expect(rowsTouched === 2).to.equal(true);

                // verify that both entries from serverTestUser2 were returned
                expect(queryResult.length === 2).to.equal(true);

                var entry2Found = false;
                var entry3Found = false;

                for (var i = 0; i < queryResult.length; i++) {

                    if (queryResult[i].getSensorID() === entry2.getSensorID()) {
                        entry2Found = true;
                    } else if (queryResult[i].getSensorID() === entry3.getSensorID()) {
                        entry3Found = true;
                    }
                }

                // check that both entries were found and returned
                expect(entry2Found && entry3Found).to.equal(true);
            });

            // finally, delete entries now that test is over
            ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true);
            });

            ContentStore.deleteCSData(entry2.getUserID(), entry2.getTimeString(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true);

                done(); // the invocation of done() tells testing framework that all tests are complete
            });
        })
    })
});

/**
 * Tests ContentStore.getSpecificCSData() functionality.
 */
describe('ContentStore', function(){
    describe('#getSpecificCSData()', function(){
        it('should return true if specific entry found, otherwise false', function(done){

            // first delete (potentially stored) entries before testing get
            ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString(), function (rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            ContentStore.deleteCSData(entry2.getUserID(), entry2.getTimeString(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            ContentStore.deleteCSData(entry3.getUserID(), entry3.getTimeString(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            // test method against bad data
            expect(ContentStore.getSpecificCSData(null, null, null)).to.equal(false);

            // insert good data
            ContentStore.insertCSData(entry1, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            ContentStore.insertCSData(entry2, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            ContentStore.insertCSData(entry3, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            ContentStore.getSpecificCSData(entry2.getUserID(), entry2.getTimeString(),

                function(rowsTouched, queryResult) {

                    // verify that only entry (of two by same user) was found
                    expect(rowsTouched === 1).to.equal(true);

                    // check that single entry was found and returned
                    expect(queryResult.getSensorID() === entry2.getSensorID()).to.equal(true);
           });

            // finally, delete entries now that test is over
            ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true);
            });

            ContentStore.deleteCSData(entry2.getUserID(), entry2.getTimeString(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true);

                done(); // the invocation of done() tells testing framework that all tests are complete
            });
        })
    })
});