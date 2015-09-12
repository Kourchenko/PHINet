/**
 * File provides test cases for Data Analytics implemented in analytics.js.
 */

var expect = require("chai").expect;
var analytics = require('../analytics').analytics();

/**
 * Tests UDPComm.handleFIBData() functionality.
 */
describe('Analytics', function() {
    describe('#mean()', function () {
        it('method returns the mean value', function (done) {

            // test simple mean value cases
            expect(analytics.mean([10, 10, 10, 10])).to.equal(10);
            expect(analytics.mean([1,3,29])).to.equal(11);

            var exceptionRaised = false;

            try {
                // bad input should raise exception
                expect(analytics.mean(null)).to.equal(null);

            } catch (e) {
                exceptionRaised = true;
            }

            expect(exceptionRaised).to.equal(true);

            exceptionRaised = false; // reset variable for next test

            try {
                // bad input should raise exception
                expect(analytics.mean([10, 10, "string"])).to.equal(null);

            } catch (e) {
                exceptionRaised = true;
            }

            expect(exceptionRaised).to.equal(true);

            done(); // the invocation of done() tells testing framework that all tests are complete
        })
    })
});

/**
 * Tests UDPComm.handleFIBData() functionality.
 */
describe('Analytics', function() {
    describe('#mode()', function () {
        it('method returns the mode value', function (done) {

            // test simple mode value cases
            expect(analytics.mode([10, 10, 10, 10])).to.equal(10);
            expect(analytics.mode([1,3,29, 3, 50])).to.equal(3);

            var exceptionRaised = false;

            try {
                // bad input should raise exception
                expect(analytics.mode(null)).to.equal(null);

            } catch (e) {
                exceptionRaised = true;
            }

            expect(exceptionRaised).to.equal(true);

            exceptionRaised = false; // reset variable for next test

            try {
                // bad input should raise exception
                expect(analytics.mode([10, 10, "string"])).to.equal(null);

            } catch (e) {
                exceptionRaised = true;
            }

            expect(exceptionRaised).to.equal(true);

            done(); // the invocation of done() tells testing framework that all tests are complete
        })
    })
});

/**
 * Tests UDPComm.handleFIBData() functionality.
 */
describe('Analytics', function() {
    describe('#median()', function () {
        it('method returns the median value', function (done) {

            // test simple median value cases
            expect(analytics.median([10, 10, 10, 10, 40])).to.equal(10);
            expect(analytics.median([1,3, 7, 99])).to.equal(5);

            var exceptionRaised = false;

            try {
                expect(analytics.median(null)).to.equal(null);

            } catch (e) {
                exceptionRaised = true;
            }

            expect(exceptionRaised).to.equal(true);

            exceptionRaised = false; // reset variable for next test

            try {
                expect(analytics.median([10, 10, "string"])).to.equal(null);

            } catch (e) {
                exceptionRaised = true;
            }

            expect(exceptionRaised).to.equal(true);

            done(); // the invocation of done() tells testing framework that all tests are complete
        })
    })
});