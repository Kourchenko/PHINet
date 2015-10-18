/** 
 * File contains code for that functions as "main"
 * segment of execution for this web application
 **/

var StringConst = require('./string_const').StringConst;
var cookieParser = require('cookie-parser');
var express = require('express');

var User = require('./user').User;
var UserDB = require('./usercredentials.js').UserCredentials(StringConst.USER_DB);
var utils = require('./utils').Utils;

var httpServer = require('http');
var embeddedJavaScript = require('ejs');
var fileSystem = require('fs'); // enables easy file reading

var favicon = require('serve-favicon'); // allows use of a custom favicon
var bodyParser = require('body-parser'); // allows easy form submissions
var postgresDB = require('pg');


var BAD_HTTP_REQUEST_CODE = 400;
var CREATED_HTTP_REQUEST_CODE = 201;
var INTERNAL_SERVER_ERROR_HTTP_REQUEST_CODE = 500;
var OK_HTTP_REQUEST_CODE = 200;
var RESOURCE_NOT_FOUND_HTTP_REQUEST_CODE = 404;
var NO_CONTENT_HTTP_REQUEST_CODE = 204;

var app = express();
app.use(bodyParser.json());
app.use(cookieParser());
app.use(bodyParser.urlencoded({
  extended: true
}));

var router = express.Router();

router.use(function(req, res, next) {
    next();
});

app.use('/api', router);
app.set('port',  process.env.PORT || 3000);
app.use(express.static(__dirname));
app.use(favicon(__dirname + '/public/images/favicon.ico'));


router.route('/users')
    // API to create user
    .post(function(req, res) {


        // TODO - ensure request is valid (rate limit, etc)

        if (utils.isValidSignupRequest(req.body)) {

            var user = User();

            user.userID = req.body.userID;
            user.email = req.body.email;
            user.password = req.body.password;

            // TODO - allow user to populate remaining fields via profile page

            UserDB.insertNewUser(user, function(rowsTouched) {

                if (rowsTouched == 1) {
                    res.statusCode = CREATED_HTTP_REQUEST_CODE;
                    res.json({message: "Successfully created user with userID: " + req.body.userID});
                } else {
                    res.statusCode = INTERNAL_SERVER_ERROR_HTTP_REQUEST_CODE;
                    res.json({message: "Sign up request for user with userID: " + req.body.userID
                            + " has passed validation but could not be stored in the database."})
                }
            });

        } else {
            res.statusCode = BAD_HTTP_REQUEST_CODE;
            res.json({message: "Invalid sign up request for user with userID: " + req.body.userID
                                + ". Please ensure username, password, and email are valid"});
        }
    });

router.route('/users/:userID')

    // API to get user by userID
    .get(function(req, res) {


        // TODO - validate userID && also take password to verify user is legitimate

        UserDB.getUserByID(req.params.userID, function(user) {

            if (user) {
                res.statusCode = OK_HTTP_REQUEST_CODE;
                res.json({message: "Successfully found user with userID: " + user.userID + " and email: " + user.email});
            } else {
                res.statusCode = NO_CONTENT_HTTP_REQUEST_CODE;
                res.json({message: "Failed to find user with userID: " + req.body.userID})
            }
        });

        // TODO - ensure request is valid (rate limit, etc)

        // TODO - validate input and return user
    })

    // API to update user by userID
    .put(function(req, res) {

        // TODO - ensure request is valid (rate limit, etc)

        // TODO - validate input and return SUCCESS/FAILURE
    })

    // API to delete user by userID
    .delete(function(req, res) {
        // TODO - validate input and delete user

        // TODO - ensure request is valid (rate limit, etc)

        // TODO - return SUCCESS/FAILURE accordingly
    });

function displayPage(httpStatusCode, res, path, log, embeddedJavaScriptParams) {

    fileSystem.readFile(__dirname + path, 'utf-8', function(err, content) {
        if (err) {
            console.log(log + err);
        } else {
          res.status(httpStatusCode).send(embeddedJavaScript.render(content, embeddedJavaScriptParams));
        }
    });
}

/**
 * Handles main web page
 */
app.get('/', function (req, res) {

    displayPage(OK_HTTP_REQUEST_CODE, res, '/public/templates/index.html', "Error serving index.html: ", {"user":""});
});

/**
 * Handles all other queries; responds with 404 page
 */
app.get('*', function(req, res) {

    displayPage(RESOURCE_NOT_FOUND_HTTP_REQUEST_CODE, res, '/public/templates/404.html', "Error serving 404.html: ", {user: ""});
});


var client = new postgresDB.Client(StringConst.DB_CONNECTION_STRING);
client.connect(function(err) {
  if(err) {
      console.error('could not connect to postgres', err);
  }
});

/**
 * Function creates DB table if it currently don't exist.
 *
 * @param dbName suspect table name
 * @param dbCreationQuery creation query to be invoked if table doesn't exist
 */
function ifNonexistentCreateDB(dbName, dbCreationQuery) {

    client.query( "SELECT COUNT(*) FROM " + dbName, function(err, result) {

        if (err) {

            // TODO - perform more sophisticated check

            var errWords = toString(err).split(" ");
            var naiveCheckPasses = true;

            // create table if naive check passes
            naiveCheckPasses &= errWords.indexOf("does") === -1;
            naiveCheckPasses &= errWords.indexOf("not") === -1;
            naiveCheckPasses &= errWords.indexOf("exist") === -1;

            if (naiveCheckPasses) {

                client.query(dbCreationQuery);
            }
        }
    });
}

function createUserDB() {

    ifNonexistentCreateDB(StringConst.USER_DB, StringConst.createUserDB(StringConst.USER_DB));
    ifNonexistentCreateDB(StringConst.USER_TEST_DB, StringConst.createUserDB(StringConst.USER_TEST_DB));
}

createUserDB();

httpServer.createServer(app).listen(app.get('port'), function() {
	console.log('Express server listening on port ' + app.get('port'));
});
