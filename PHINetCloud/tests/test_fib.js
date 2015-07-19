/**
 * File provides test cases for Forwarding Information Base implemented in fib.js.
 */

var expect = require("chai").expect;
var StringConst = require('../string_const').StringConst;
var FIB = require('../fib.js').FIB(StringConst.FIB_TEST_DB);
var DBData = require('../data'); // used to create objects used by the database

// --- test entries ---

var entry1 = DBData.DATA();
entry1.fibData("serverTestUser1", StringConst.CURRENT_TIME, "10.10.10.10");

var entry2 = DBData.DATA();
entry2.fibData("serverTestUser2", StringConst.CURRENT_TIME, "20.20.20.20");

// --- test entries ---

/**
 * Tests ForwardingInformationBase.insertFIBData() functionality.
 */
describe('FIB', function(){
    describe('#insertFIBData()', function(){
        it('should return true if entry has been made', function(done){

            // first delete entries before testing addition
            FIB.deleteFIBData(entry1.getUserID(), function (rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            FIB.deleteFIBData(entry2.getUserID(),  function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            // test clearly bad input; should be rejected at start
            expect(FIB.insertFIBData(null)).to.equal(false);
            expect(FIB.insertFIBData(undefined)).to.equal(false);
            expect(FIB.insertFIBData(DBData.DATA())).to.equal(false); // "empty" object should fail

            // test good input
            FIB.insertFIBData(entry1, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            FIB.insertFIBData(entry2, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            // test redundant input
            FIB.insertFIBData(entry1, function(rowsTouched) {
                expect(rowsTouched === 0).to.equal(true);
            });

            // finally, delete entries after testing addition
            FIB.deleteFIBData(entry1.getUserID(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true);
            });

            FIB.deleteFIBData(entry2.getUserID(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true);

                done(); // the invocation of done() tells testing framework that all tests are complete
            });
        })
    })
});

/**
 * Tests ForwardingInformationBase.updateFIBData() functionality.
 */
describe('FIB', function(){
    describe('#updateFIBData()', function(){
        it('should return true if update has been made', function(done){

            // first delete entries before testing update
            FIB.deleteFIBData(entry1.getUserID(), function (rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            FIB.deleteFIBData(entry2.getUserID(),  function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            // test clearly bad input; should be rejected at start
            expect(FIB.updateFIBData(null)).to.equal(false);
            expect(FIB.updateFIBData(undefined)).to.equal(false);
            expect(FIB.updateFIBData(DBData.DATA())).to.equal(false); // "empty" object should fail

            // test good input
            FIB.insertFIBData(entry1, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            FIB.insertFIBData(entry2, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            var ENTRY1_NEW_IP = "99.99.55.55", ENTRY2_NEW_IP = "55.66.77.88";

            // modify data
            entry1.setIpAddr(ENTRY1_NEW_IP);
            entry2.setIpAddr(ENTRY2_NEW_IP);

            // test valid update given good input
            FIB.updateFIBData(entry1, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was updated
            });

            FIB.updateFIBData(entry2, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was updated
            });

            // test that update worked; check that new value is present

            FIB.getSpecificFIBData(entry1.getUserID(), function(rowsTouched, queryResult) {

                    // verify that only entry (of two by same user) was found
                    expect(rowsTouched === 1).to.equal(true);

                    // check that single entry was found and returned
                    expect(queryResult.getIpAddr() === ENTRY1_NEW_IP).to.equal(true);
                });

            FIB.getSpecificFIBData(entry2.getUserID(), function(rowsTouched, queryResult) {

                    // verify that only entry (of two by same user) was found
                    expect(rowsTouched === 1).to.equal(true);

                    // check that single entry was found and returned
                    expect(queryResult.getIpAddr() === ENTRY2_NEW_IP).to.equal(true);

                });

            // delete entries now that test is over
            FIB.deleteFIBData(entry1.getUserID(), function (rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was deleted
            });

            FIB.deleteFIBData(entry2.getUserID(),  function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was deleted

                done(); // the invocation of done() tells testing framework that all tests are complete

            });
        })
    })
});

/** 
 * Tests ForwardingInformationBase.deleteFIBData() functionality.
 */
describe('FIB', function(){
    describe('#deleteFIBData()', function(){
        it('should return true if deletion has been made', function(done){

            // before deletion test, attempt to input data into FIB
            FIB.insertFIBData(entry1, function(rowsTouched){});
            FIB.insertFIBData(entry2, function(rowsTouched){});

            // test deletion given good input
            FIB.deleteFIBData(entry1.getUserID(), function(rowsTouched) {
                    expect(rowsTouched === 1).to.equal(true); // verify deletion of 1 row
                });

            FIB.deleteFIBData(entry2.getUserID(), function(rowsTouched) {
                    expect(rowsTouched === 1).to.equal(true); // verify deletion of 1 row
                });

            // test deletion given bad input
            expect(FIB.deleteFIBData(undefined, undefined)).to.equal(false);

            // object no longer exists in DB, verify that no rows were modified
            FIB.deleteFIBData(entry1.getUserID(), function(rowsTouched) {
                    expect(rowsTouched === 0).to.equal(true);

                    done(); // the invocation of done() tells testing framework that all tests are complete
                });
        })
    })
});

/**
 * Tests ForwardingInformationBase.getAllFIBData() functionality.
 */
describe('FIB', function(){
    describe('#getAllFIBData()', function(){
        it('should return true if data found, otherwise false', function(done){

            // first delete entries before testing update
            FIB.deleteFIBData(entry1.getUserID(), function (rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            FIB.deleteFIBData(entry2.getUserID(),  function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            // test method against bad input
            expect(FIB.getAllFIBData(null)).to.equal(false);

            // test good input
            FIB.insertFIBData(entry1, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            FIB.insertFIBData(entry2, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            FIB.getAllFIBData(function(rowsTouched, queryResults) {

                expect(rowsTouched === 2).to.equal(true); // verify that both rows were found

                // verify that both entries were returned
                expect(queryResults.length === 2).to.equal(true);

                var entry1Found = false;
                var entry2Found = false;

                for (var i = 0; i < queryResults.length; i++) {

                    if (queryResults[i].getUserID() === entry2.getUserID()) {
                        entry1Found = true;
                    } else if (queryResults[i].getUserID() === entry1.getUserID()) {
                        entry2Found = true;
                    }
                }

                expect(entry1Found && entry2Found).to.equal(true);

           });

            // delete entries now that test is over
            FIB.deleteFIBData(entry1.getUserID(), function (rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was deleted
            });

            FIB.deleteFIBData(entry2.getUserID(),  function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was deleted

                done(); // the invocation of done() tells testing framework that all tests are complete
            });
        })
    })
});

/**
 * Tests ForwardingInformationBase.getSpecificFIBData() functionality.
 */
describe('FIB', function(){
    describe('#getSpecificFIBData()', function(){
        it('should return true if specific entry found, otherwise false', function(done){

            // first delete entries before testing update
            FIB.deleteFIBData(entry1.getUserID(), function (rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            FIB.deleteFIBData(entry2.getUserID(),  function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            // test method against bad input
            expect(FIB.getSpecificFIBData(null, null)).to.equal(false);

            // test good input
            FIB.insertFIBData(entry1, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            FIB.insertFIBData(entry2, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            FIB.getSpecificFIBData(entry1.getUserID(), function(rowsTouched, queryResults) {

                expect(rowsTouched === 1).to.equal(true); // verify that one row was found

                expect(queryResults.getUserID() === entry1.getUserID()).to.equal(true);
                expect(queryResults.getIpAddr() === entry1.getIpAddr()).to.equal(true);

            });

            FIB.getSpecificFIBData(entry2.getUserID(), function(rowsTouched, queryResults) {

                expect(rowsTouched === 1).to.equal(true); // verify that one row was found

                expect(queryResults.getUserID() === entry2.getUserID()).to.equal(true);
                expect(queryResults.getIpAddr() === entry2.getIpAddr()).to.equal(true);

            });

            // delete entries now that test is over
            FIB.deleteFIBData(entry1.getUserID(), function (rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was deleted
            });

            FIB.deleteFIBData(entry2.getUserID(),  function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was deleted

                done(); // the invocation of done() tells testing framework that all tests are complete
            });
        })
    })
});
