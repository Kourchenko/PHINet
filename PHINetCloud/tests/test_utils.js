/**
 * File provides test cases for Utility methods implemented in utils.js.
 */

var expect = require("chai").expect;
var utils = require('../utils').Utils;

/**
 * Tests Utils.comparePassword() functionality.
 */
describe('Utils', function() {
    describe('#comparePassword()', function () {
        it('method compares password against its encrypted version', function (done) {

            // --- test against bad input ---

            var exceptionThrown = false;

            try {
                utils.encryptPassword(null, null);
            } catch (e) {
                exceptionThrown = true;
            }

            expect(exceptionThrown).to.equal(true);
            exceptionThrown = false; // reset for future test

            try {
                utils.encryptPassword("one", null);
            } catch (e) {
                exceptionThrown = true;
            }

            expect(exceptionThrown).to.equal(true);
            exceptionThrown = false; // reset for future test

            try {
                utils.comparePassword(null);
            } catch (e) {
                exceptionThrown = true;
            }

            expect(exceptionThrown).to.equal(true);
            exceptionThrown = false; // reset for future test

            try {
                utils.comparePassword("null", "null", null);
            } catch (e) {
                exceptionThrown = true;
            }

            expect(exceptionThrown).to.equal(true);

            // --- test against bad input ---

            var password1 = "hunter2";

            utils.hashPassword(password1, function(err, hash) {

                var hashedPW1 = hash;

                expect(hashedPW1 === password1).to.equal(false);

                utils.comparePassword(password1, hashedPW1, function(err, passwordsMatch) {

                    expect(passwordsMatch).to.equal(true);
                    done(); // the invocation of done() tells testing framework that all tests are complete
                })
            });
        })
    })
});

/**
 * Tests Utils.isValidForTimeInterval() functionality.
 */
describe('Utils', function(){
    describe('#isValidForTimeInterval()', function(){
        it('returns true if valid time interval, otherwise false', function(done){
            var goodRequestInterval = "2012-05-04T08.08.08.888||2014-05-04T08.08.08.888";
            var badRequestInterval = "2012-ERROR:08.888||2014-ERROR8.08.888";

            var goodDataInterval1 = "2012-07-04T08.08.08.888"; // date is within goodRequestInterval
            var goodDataInterval2 = "2012-01-04T08.08.08.888"; // date is before goodRequestInterval
            var goodDataInterval3 = "2014-07-04T08.08.08.888"; // date is after goodRequestInterval

            var testInterval1 = "2015-02-22T00.00.00.000||2015-04-22T00.00.00.000";
            var dataTime1 = "2015-03-22T22.58.10.878";

            var testInterval2 = "2015-02-22T00.00.00.000||2015-04-22T00.00.00.000";
            var dataTime2 = "2015-02-22T04.04.04.004";

            // --- test bad input ---
            expect(utils.isValidForTimeInterval(null, null)).to.equal(false); // null entries

            // syntax error in request interval
            expect(utils.isValidForTimeInterval(badRequestInterval, goodDataInterval1)).to.equal(false);

            // two data intervals and no request interval
            expect(utils.isValidForTimeInterval(goodDataInterval1, goodDataInterval1)).to.equal(false);

            // --- test bad input ---

            // test input rejection if before interval
            expect(utils.isValidForTimeInterval(goodRequestInterval, goodDataInterval2)).to.equal(false);

            // test input rejection if after interval
            expect(utils.isValidForTimeInterval(goodRequestInterval, goodDataInterval3)).to.equal(false);

            // test input acceptance if during interval
            expect(utils.isValidForTimeInterval(goodRequestInterval, goodDataInterval1)).to.equal(true);
            expect(utils.isValidForTimeInterval(testInterval1, dataTime1)).to.equal(true);
            expect(utils.isValidForTimeInterval(testInterval2, dataTime2)).to.equal(true);

            done(); // the invocation of done() tells testing framework that all tests are complete
        })
    })
});

/**
 * Tests Utils.parseSynchData() functionality.
 */
describe('Utils', function(){
    describe('#parseSynchData()', function(){
        it('after parse, returns list of parsed data', function(done){

            // this input contains two data points from sensorID1 and one data point from sensorID2
            var goodInput = "sensorID2--100,1990-11-09T00.00.00.000::sensorID1--10,1990-11-06T00.00.00.000;;"
                                        + "15,1990-11-07T00.00.00.000;;99,1990-11-08T00.00.00.000";
            var badInput = "i1sdfpijajjsdf";

            var goodParsedInput = utils.parseSynchData("userID", goodInput);
            var badParsedOutput1 = utils.parseSynchData("userID", badInput);
            var badParsedOutput2 = utils.parseSynchData("", badInput);

            var foundSensorID1Count = 0;
            var foundSensorID2Count = 0;

            // check to see that both sensors were found
            for (var i = 0; i < goodParsedInput.length; i++) {
                if (goodParsedInput[i].getSensorID() === "sensorID2") {
                    foundSensorID2Count += 1;
                } else if (goodParsedInput[i].getSensorID() === "sensorID1") {
                    foundSensorID1Count += 1;
                }
            }

            // test good output found in right proportions
            expect(foundSensorID1Count === 3 && foundSensorID2Count === 1).to.equal(true);

            // test bad output
            expect(badParsedOutput1.length == 0).to.equal(true);
            expect(badParsedOutput2.length == 0).to.equal(true);

            done(); // the invocation of done() tells testing framework that all tests are complete
        })
    })
});

/**
 * Tests Utils.isValidPassword() functionality.
 */
describe('Utils', function(){
    describe('#isValidPassword()', function(){
        it('returns true if password is syntactically valid', function(done){

            var badPassword1 = "aias;;;asd_"; // bad characters
            var badPassword2 = "ds"; // too short
            var badPassword3 = "ajsdpfioajsdpifjasdpifjasdifj"; // too long

            var goodPassword1 = "hunter2";
            var goodPassword2 = "hunter_2";

            // test bad input
            expect(utils.isValidPassword(badPassword1)).to.equal(false);
            expect(utils.isValidPassword(badPassword2)).to.equal(false);
            expect(utils.isValidPassword(badPassword3)).to.equal(false);
            expect(utils.isValidPassword(null)).to.equal(false);

            // test good input
            expect(utils.isValidPassword(goodPassword1)).to.equal(true);
            expect(utils.isValidPassword(goodPassword2)).to.equal(true);

            done(); // the invocation of done() tells testing framework that all tests are complete
        })
    })
});

/**
 * Tests Utils.isValidUserName() functionality.
 */
describe('Utils', function(){
    describe('#isValidUserName()', function(){
        it('returns true if username is syntactically valid', function(done){

            var badUserName1 = "aias;;;asd_"; // bad characters
            var badUserName2 = "ds"; // too short
            var badUserName3 = "ajsdpfioajsdpifjasdpifjasdifj"; // too long

            var goodUserName1 = "hunter2";
            var goodUserName2 = "hunter_2";

            // test bad input
            expect(utils.isValidUserName(badUserName1)).to.equal(false);
            expect(utils.isValidUserName(badUserName2)).to.equal(false);
            expect(utils.isValidUserName(badUserName3)).to.equal(false);
            expect(utils.isValidUserName(null)).to.equal(false);

            // test good input
            expect(utils.isValidUserName(goodUserName1)).to.equal(true);
            expect(utils.isValidUserName(goodUserName2)).to.equal(true);

            done(); // the invocation of done() tells testing framework that all tests are complete
        })
    })
});

/**
 * Tests Utils.isValidEmail() functionality.
 */
describe('Utils', function(){
    describe('#isValidEmail()', function(){
        it('returns true if email is syntactically valid', function(done){

            var badEmail1 = "hunter2";
            var badEmail2 = "hunter2@.gmail.com";
            var badEmail3 = "hunter2@gmail";

            var goodEmail1 = "hunter2@gmail.com";
            var goodEmail2 = "hunter2@hunter2.abc.edu";

            // test bad input
            expect(utils.isValidEmail(badEmail1)).to.equal(false);
            expect(utils.isValidEmail(badEmail2)).to.equal(false);
            expect(utils.isValidEmail(badEmail3)).to.equal(false);
            expect(utils.isValidEmail(null)).to.equal(false);

            // test good input
            expect(utils.isValidEmail(goodEmail1)).to.equal(true);
            expect(utils.isValidEmail(goodEmail2)).to.equal(true);

            done(); // the invocation of done() tells testing framework that all tests are complete
        })
    })
});


