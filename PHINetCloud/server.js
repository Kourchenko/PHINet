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
        var user = User();

        // TODO - validate input

        user.userID = req.body.userID;
        // TODO - ensure request is valid (rate limit, etc)

        // TODO - populate remaining fields
        // TODO - return success/failure accordingly
        res.json({message: "SUCCESS"});
    });

router.route('/users/:userID')

    // API to get user by userID
    .get(function(req, res) {

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

    displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", {"user":""});
});

/**
 * Handles all other queries; responds with 404 page
 */
app.get('*', function(req, res) {

    displayPage(404, res, '/public/templates/404.html', "Error serving 404.html: ", {user: ""});
});


var client = new postgresDB.Client(StringConst.DB_CONNECTION_STRING);
client.connect(function(err) {
  if(err) {
      console.error('could not connect to postgres', err);
  }
});

httpServer.createServer(app).listen(app.get('port'), function() {
	console.log('Express server listening on port ' + app.get('port'));
});
