/** 
 * File contains code for that functions as "main"
 * segment of execution for this web application
 **/

var StringConst = require('./string_const').StringConst;
var cookieParser = require('cookie-parser');
var express = require('express');

var UserCredentialDB = require('./usercredentials.js').UserCredentials(StringConst.LOGIN_DB);
var utils = require('./utils').Utils;

var networkListener = require('./network_listener').NetworkListener(UserCredentialDB);
var httpServer = require('http');
var embeddedJavaScript = require('ejs');
var fileSystem = require('fs'); // enables easy file reading

var favicon = require('serve-favicon'); // allows use of a custom favicon
var bodyParser = require('body-parser'); // allows easy form submissions

networkListener.initializeListener(); // begin listening for udp packets

var app = express();
app.use(bodyParser.json());
app.use(cookieParser());
app.use(bodyParser.urlencoded({
  extended: true
}));

app.set('port',  process.env.PORT || 3000);
app.use(express.static(__dirname));
app.use(favicon(__dirname + '/public/images/favicon.ico'));

/**
 * Displays any webpage given input params
 *
 * @param httpStatusCode associated with page
 * @param res object used to send page to client
 * @param path of html file
 * @param log what to print if load fails
 * @param embeddedJavaScriptParams used to populate page
 */
function displayPage(httpStatusCode, res, path, log, embeddedJavaScriptParams) {

    fileSystem.readFile(__dirname + path, 'utf-8', function(err, content) {
        if (err) {
            console.log(log + err);
        } else {

          res.status(httpStatusCode).send(embeddedJavaScript.render(content, embeddedJavaScriptParams));
        }

    });
}

var displayPageRateLimited = function(res){
    // HTTP Code 429 is for rate-limits
    displayPage(429, res, '/public/templates/rate_limit.html', "Error serving rate_limit.html: ", {});
} ;

/**
 * Handles main web page
 */
app.get('/', function (req, res) {

    if (req.cookies.user) {
        UserCredentialDB.getUserByID(req.cookies.user, function(rowsTouched, queryResult) {

            var embeddedJavaScriptParams = queryResult ? {"user": req.cookies.user} : {"user":""};

            displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", embeddedJavaScriptParams);
        });
    } else {

        displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", {user: ""});
    }

});

/**
 * Handles login page
 */
app.get('/login', function (req, res) {

    displayPage(200, res, '/public/templates/login.html', "Error serving login.html: ", {error:""});

});

/**
 * Handles signup page
 */
app.get('/signup', function (req, res) {
    displayPage(200, res, '/public/templates/signup.html', "Error serving signup.html: ", {error:""});
});

/**
 * Handles logout request by clearing the login cookie.
 */
app.get('/logout', function(req, res) {

    res.clearCookie('user'); // user has logged out; clear the login cookie
    var embeddedJavaScriptParams = {user: "", error:""};

    displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", embeddedJavaScriptParams);

});

/**
 * Handles FAQ page
 */
app.get('/faq', function (req, res) {
    if (req.cookies.user) {
        UserCredentialDB.getUserByID(req.cookies.user, function(rowsTouched, queryResult) {

            var embeddedJavaScriptParams = queryResult ? req.cookies.user : "";

            displayPage(200, res, '/public/templates/faq.html', "Error serving faq.html: ", embeddedJavaScriptParams);
        });
    } else {
        var embeddedJavaScriptParams = {user: ""};
        displayPage(200, res, '/public/templates/faq.html', "Error serving faq.html: ", embeddedJavaScriptParams);
    }

});

/**
 * Handles profile page
 */
app.get('/profile', function (req, res) {
    // verify that user-login cookie exists before displaying profile page
    if (req.cookies && req.cookies.user) {

        // now, query database as second level of validation
        UserCredentialDB.getUserByID(req.cookies.user, function(rowsTouched, queryResult) {

            var embeddedJavaScriptParams;


            if (queryResult) {
                var displayedEmail;
                if (queryResult.getEmail() === StringConst.NULL_FIELD) {
                    displayedEmail = "none on record"

                } else {
                    displayedEmail = queryResult.getEmail();
                }

                embeddedJavaScriptParams = {user: req.cookies.user,  email: displayedEmail};

                displayPage(200, res, '/public/templates/profile.html', "Error serving profile.html: ", embeddedJavaScriptParams);
            } else {
                embeddedJavaScriptParams = {user: ""};
                displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", embeddedJavaScriptParams);
            }
        });

    }
    // user doesn't exist, direct to main page
    else {
        displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", {user: ""});

    }
});

/**
 * Handles viewdata page
 */
app.get('/viewdata', function (req, res) {
    // verify that user exists before displaying page
    if (req.cookies && req.cookies.user) {

       // TODO - display data

    }
    // user doesn't exist, direct to main page
    else {

        displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", {user: ""});
    }
});

/**
 * Handles all other queries; responds with 404 page
 */
app.get('*', function(req, res) {
    if (req.cookies.user) {
        UserCredentialDB.getUserByID(req.cookies.user, function(rowsTouched, queryResult) {

            // TODO - rethink why/if this query is necessary

            var embeddedJavaScriptParams = {user: req.cookies.user};

            displayPage(404, res, '/public/templates/404.html', "Error serving 404.html: ",embeddedJavaScriptParams);
        });
    } else {
        var embeddedJavaScriptParams = {user: ""};
        displayPage(404, res, '/public/templates/404.html', "Error serving 404.html: ",embeddedJavaScriptParams);

    }
});

/**
 * Handles user login-attempt
 */
app.post('/loginAction', function(req, res) {

    var embeddedJavaScriptParams;

    // check that user entered both required params
    if (!req.body.user_name || !req.body.user_password) {

        // notify user of unsuccessful login
        embeddedJavaScriptParams = {error: "Login unsuccessful: provide all input.", user:""};
        displayPage(200, res, '/public/templates/login.html', "Error serving login.html: ",embeddedJavaScriptParams);


    } else {

        var pw = req.body.user_password.trim();
        var userName = req.body.user_name.trim();

        // user entered both required params, now query database to verify password
        UserCredentialDB.getUserByID(userName, function(rowsTouched, queryResults){

            // only attempt to compare passwords if query was successful
            if (queryResults != null && rowsTouched == 1) {

                utils.comparePassword(pw, queryResults.getPassword(),
                    function(err, isPasswordMatch) {

                        if (isPasswordMatch) {
                            // notify user of successful login

                            // TODO - improve on cookie use
                            // store cookie for 1 day
                            res.cookie('user', userName, {maxAge: 1000 * 60 * 60 * 24, httpOnly:true});

                            embeddedJavaScriptParams = {user: userName};

                            displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ",embeddedJavaScriptParams);
                        } else {
                            // notify user of unsuccessful login
                            embeddedJavaScriptParams = {error: "Login unsuccessful: incorrect password.", user:""};
                            displayPage(200, res, '/public/templates/login.html', "Error serving login.html: ",embeddedJavaScriptParams);
                        }
                });
            } else {
                // notify user of unsuccessful login (no user found)
                var embeddedJavaScriptParams = {error: "Login unsuccessful: user does not exist.", user:""};
                displayPage(200, res, '/public/templates/login.html', "Error serving login.html: ",embeddedJavaScriptParams);

            }
        });
    }
});

/**
 * Handles user register-attempt
 */
app.post('/registerAction', function(req, res) {

    var embeddedJavaScriptParams;

    //check that user entered all required params
    if (!req.body.user_password || !req.body.user_name || !req.body.user_type) {
        // notify user of unsuccessful login
        embeddedJavaScriptParams = {error: "Register unsuccessful: provide all input.", user:""};
        displayPage(200, res, '/public/templates/signup.html', "Error serving signup.html: ",embeddedJavaScriptParams);
    } else {

        var pw = req.body.user_password[1].trim();
        var verifyPW = req.body.user_password[1].trim();
        var userName = req.body.user_name.trim();
        var email = req.body.user_email.trim();

        // check that passwords match, enforce PW/Username syntax, and verify validity of email address
        if (pw === verifyPW
            && utils.isValidPassword(pw) && utils.isValidUserName(userName)
            && (!email || utils.isValidEmail(email))) {

            if (!email) {
                email = StringConst.NULL_FIELD; // email not provided; list as null
            }

            // hash user's password then insert user in database
            utils.hashPassword(pw, function(err, hashedPW) {

                // store hashed pw into DB
                UserCredentialDB.insertNewUser(userName, hashedPW, email,
                    function(rowsTouched) {

                        // one row touched corresponds to successful insertion
                        if (rowsTouched === 1) {

                            // notify user of successful register
                            // TODO - improve on cookie use

                            // store cookie for 1 day
                            res.cookie('user', userName, {maxAge: 1000 * 60 * 60 * 24, httpOnly:true});

                            embeddedJavaScriptParams = {user: userName};

                            displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ",embeddedJavaScriptParams);
                        }
                        // an error occurred while inserting user into the database
                        else {

                            // notify user of bad input
                            embeddedJavaScriptParams = {user: "", error: "Register unsuccessful."
                                                    + "\nEither username or email already exists."};
                            displayPage(200, res, '/public/templates/signup.html', "Error serving signup.html: ",embeddedJavaScriptParams);
                        }
                    });
            });

        } else {

            if (pw !== verifyPW) {

                // notify user of password mismatch
                embeddedJavaScriptParams = {user: "", error: "Passwords don't match."};
            }
            // since email is optional, validity only applies if it was entered
            else if (!utils.isValidEmail(email) && email) {

                embeddedJavaScriptParams = {user: "", error: "Invalid email."};
            } else if (!utils.isValidPassword(pw)) {

                embeddedJavaScriptParams = {user: "", error: "Invalid password. Must use 3-15 alpha-numerics."};
            } else if (!utils.isValidUserName(userName)) {

                embeddedJavaScriptParams = {user: "", error: "Invalid username. Must use 3-15 alpha-numerics."};
            }
            displayPage(200, res, '/public/templates/signup.html', "Error serving signup.html: ", embeddedJavaScriptParams);
        }
    }
});

var postgresDB = require('pg');


var client = new postgresDB.Client(StringConst.DB_CONNECTION_STRING);
client.connect(function(err) {
  if(err) {
    return console.error('could not connect to postgres', err);
  }
});

httpServer.createServer(app).listen(app.get('port'), function() {
	console.log('Express server listening on port ' + app.get('port'));
});
