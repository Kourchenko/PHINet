/** 
 * File contains code for that functions as "main"
 * segment of execution for this web application
 **/

var StringConst = require('./string_const').StringConst; // important Strings
var cookieParser = require('cookie-parser'); // allows integration of cookies
var express = require('express'); // Node.js web framework

var PIT = require('./pit').PIT(StringConst.PIT_DB); // PendingInterestTable database module
var FIB = require('./fib').FIB(StringConst.FIB_DB); // ForwardingInformationBase database module
var CS = require('./cs').CS(StringConst.CS_DB); // ContentStore database module
var LoginDB = require('./usercredentials.js').LoginCredentials(StringConst.LOGIN_DB); // user credential database
var utils = require('./utils').Utils; // useful utility methods

var udp_comm = require('./udp_comm').UDPComm(PIT, FIB, CS, LoginDB); // UDP communication module; requires reference to databases
var http = require('http'); // used to create the server
var ejs = require('ejs'); // enables use of embedded javascript
var fs = require('fs'); // enables easy file reading

var favicon = require('serve-favicon'); // allows use of a custom favicon
var bodyParser = require('body-parser'); // allows easy form submissions

udp_comm.initializeListener(); // begin listening for udp packets

var app = express();
app.use(bodyParser.json());
app.use(cookieParser());
app.use(bodyParser.urlencoded({
  extended: true
}));

app.set('port',  process.env.PORT || 3000);
app.use(express.static(__dirname));
app.use(favicon(__dirname + '/public/images/favicon.ico'));


var RATE_LIMIT_CODE = 429; // HTTP Code 429 is for rate-limits
var rateLimitDictionary = {};

// chosen somewhat arbitrarily, is hopefully large enough to server many legitimate users with private IP
var MAX_HITS_PER_SECOND = 50; 
var CLEAN_RATE_DICT_INTERVAL = 1000 * 60 * 60; // chosen somewhat arbitrarily

// TODO - improve upon this naive rate-limiting

/**
 * Enforces that each IP recieves a maximum of MAX_HITS_PER_SECOND pages each second.
 * 
 * @param userIP of user requesting page
 * @return boolean regarding isRateLimit status of user's IP
 */
var isRateLimited = function (userIP) {

    var currentSecond = parseInt(new Date().getTime() / 1000);

    if (!rateLimitDictionary[userIP]) {
        rateLimitDictionary[userIP] = [currentSecond, 1];

        return false;
    } else {

        if (rateLimitDictionary[userIP][0] === currentSecond) {

            if(rateLimitDictionary[userIP][1] > MAX_HITS_PER_SECOND) {
                return true;
            } else {
                rateLimitDictionary[userIP][1] += 1;
                return false;
            }
        } else {
            rateLimitDictionary[userIP] = [currentSecond, 1];
            return false;
        }
    }

}

/**
 * Each CLEAN_RATE_DICT_INTERVAL we remove all entries that contain
 * invalid times (ie past times that could not cause a rate limit).
 */
setInterval(
    function() {
        var currentSecond = parseInt(new Date().getTime() / 1000);

        for (var key in rateLimitDictionary) {
            if (rateLimitDictionary[key][0] !== currentSecond) {
                delete rateLimitDictionary[key];
            }
        }

    }, CLEAN_RATE_DICT_INTERVAL
); // invoke every interval

/**
 * Displays any webpage given input params
 *
 * @param httpStatusCode associated with page
 * @param res object used to send page to client
 * @param path of html file
 * @param log what to print if load fails
 * @param ejsParams used to populate page
 */
function displayPage(httpStatusCode, res, path, log, ejsParams) {

    fs.readFile(__dirname + path, 'utf-8', function(err, content) {
        if (err) {
            console.log(log + err);
        } else {

          res.status(httpStatusCode).send(ejs.render(content, ejsParams));
        }
    });
}

/**
 * Handles main web page
 */
app.get('/', function (req, res) {

    var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
        fs.readFile(__dirname + '/public/templates/index.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving index.html: " + err);
            } else {

    
            }

            if (req.cookies.user) {
                LoginDB.getUserByID(req.cookies.user, function(rowsTouched, queryResult) {

                    var ejsParams;

                    if (queryResult) {
                        var userIsPatient = queryResult.getEntityType() === StringConst.PATIENT_USER_TYPE;

                        ejsParams = {user: req.cookies.user, userIsPatient: userIsPatient};
                    }
                    else {
                        ejsParams = {user: "", userIsPatient: ""};
                    }

                    displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", ejsParams);
                });
            } else {

                var ejsParams = {user: "", userIsPatient: ""};

                displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", ejsParams);
            }
        });
    } else {
        displayPage(RATE_LIMIT_CODE, res, '/public/templates/rate_limit.html', "Error serving rate_limit.html: ", {});
    }
});

/**
 * Handles login page
 */
app.get('/login', function (req, res) {

    var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {

        displayPage(200, res, '/public/templates/login.html', "Error serving login.html: ", {error:""});

    } else {
        displayPage(RATE_LIMIT_CODE, res, '/public/templates/rate_limit.html', "Error serving rate_limit.html: ", {});
    }
});

/**
 * Handles signup page
 */
app.get('/signup', function (req, res) {

    var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
       
        displayPage(200, res, '/public/templates/signup.html', "Error serving signup.html: ", {error:""});

    } else {
        displayPage(RATE_LIMIT_CODE, res, '/public/templates/rate_limit.html', "Error serving rate_limit.html: ", {});
    }
});

/**
 * Handles logout request by clearing the login cookie.
 */
app.get('/logout', function(req, res) {

    var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {

        res.clearCookie('user'); // user has logged out; clear the login cookie
        var ejsParams = {user: "", userIsPatient: "", error:""};
    
        displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", ejsParams);
    } else {
        displayPage(RATE_LIMIT_CODE, res, '/public/templates/rate_limit.html', "Error serving rate_limit.html: ", {});
    }
});

/**
 * Handles FAQ page
 */
app.get('/faq', function (req, res) {

    var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
        
            if (req.cookies.user) {
                LoginDB.getUserByID(req.cookies.user, function(rowsTouched, queryResult) {

                    var renderedHtml;
                    var ejsParams;
                    if (queryResult) {
                        var userIsPatient = queryResult.getEntityType() === StringConst.PATIENT_USER_TYPE;

                        ejsParams = {user: req.cookies.user, userIsPatient: userIsPatient};
                    }
                    else {
                        ejsParams = {user: "", userIsPatient: ""};
                    }

                    displayPage(200, res, '/public/templates/faq.html', "Error serving faq.html: ", ejsParams);
                });
            } else {
                var ejsParams = {user: "", userIsPatient: ""};
                displayPage(200, res, '/public/templates/faq.html', "Error serving faq.html: ", ejsParams);
            }
       
    } else {
        displayPage(RATE_LIMIT_CODE, res, '/public/templates/rate_limit.html', "Error serving rate_limit.html: ", {});
    }
});

/**
 * Handles profile page
 */
app.get('/profile', function (req, res) {

    var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
    
        // verify that user-login cookie exists before displaying profile page
        if (req.cookies && req.cookies.user) {

            // now, query database as second level of validation
            LoginDB.getUserByID(req.cookies.user, function(rowsTouched, queryResult) {

                var ejsParams;

                if (queryResult) {
                    var displayedEmail;
                    if (queryResult.getEmail() === StringConst.NULL_FIELD) {
                        displayedEmail = "none on record"
                    } else {
                        displayedEmail = queryResult.getEmail();
                    }

                    var userType;
                    var userIsPatient = queryResult.getEntityType() === StringConst.PATIENT_USER_TYPE;
                    if (queryResult.getEntityType() === StringConst.PATIENT_USER_TYPE) {
                        userType = "Patient";
                    } else {
                        userType = "Doctor";
                    }

                    ejsParams = {user: req.cookies.user, userIsPatient: userIsPatient,
                        email: displayedEmail, type: userType};

                    displayPage(200, res, '/public/templates/profile.html', "Error serving profile.html: ", ejsParams); 
                } else {
                    ejsParams = {user: "", userIsPatient:""};
                    displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", ejsParams);
                }
            });

        }
        // user doesn't exist, direct to main page
        else {

            ejsParams = {user: "", userIsPatient:""};
            displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", ejsParams);
        }     
    } else {
        displayPage(RATE_LIMIT_CODE, res, '/public/templates/rate_limit.html', "Error serving rate_limit.html: ", {});
    }
});

/**
 * Handles viewdata page
 */
app.get('/viewdata', function (req, res) {

    var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
    
        // verify that user exists before displaying page
        if (req.cookies && req.cookies.user) {

            // query the database for the user's data
            CS.getGeneralCSData(req.cookies.user, function(rowsTouched, queryResults) {


                LoginDB.getUserByID(req.cookies.user, function(rowsTouched, queryResult) {

                    var ejsParams;

                    if (queryResult) {
                        var userIsPatient = queryResult.getEntityType() === StringConst.PATIENT_USER_TYPE;

                        ejsParams ={user: req.cookies.user, userIsPatient: userIsPatient,
                                                                        data:JSON.stringify(queryResults)};
                    }
                    else {
                        ejsParams = {user: "", userIsPatient: "", data:""};
                    }

                    displayPage(200, res, '/public/templates/viewdata.html', "Error serving viewdata.html: ", ejsParams);
                });
                // TODO - improve upon display (give patients or own data, etc)
            });

        }
        // user doesn't exist, direct to main page
        else {

            ejsParams = {user: "", userIsPatient:""};
            displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", ejsParams);
        }
    } else {
        displayRateLimitPage(res)
    }
});

/**
 * Handles test page
 */
 /*
app.get('/test', function (req, res) {

     var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
    
        if (req.cookies.user) {
            LoginDB.getUserByID(req.cookies.user, function(rowsTouched, queryResult) {

                var ejsParams;

                if (queryResult) {
                    var userIsPatient = queryResult.getEntityType() === StringConst.PATIENT_USER_TYPE;

                    ejsParams = {user: req.cookies.user, userIsPatient: userIsPatient};
                }
                else {
                    ejsParams = {user: "", userIsPatient: ""};
                }

                displayPage(200, res, '/public/templates/test.html', "Error serving test.html: ", ejsParams);
            });
        } else {
            var ejsParams = {user: "", userIsPatient: ""};
            displayPage(200, res, '/public/templates/test.html', "Error serving test.html: ", ejsParams);
        }
   
    } else {
        displayPage(RATE_LIMIT_CODE, res, '/public/templates/rate_limit.html', "Error serving rate_limit.html: ", {});
    }
});*/

/**
 * Handles doctors page
 */
app.get('/doctors', function (req, res) {

     var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
    
       
        if (req.cookies.user) {
            LoginDB.getUserByID(req.cookies.user, function(rowsTouched, queryResult) {

                var renderedHtml;

                if (queryResult) {
                    var userIsPatient = queryResult.getEntityType() === StringConst.PATIENT_USER_TYPE;

                    if (userIsPatient) {

                        LoginDB.getDoctors(req.cookies.user, function(rowCount, queryResult) {
                            
                            var ejsParams = {user: req.cookies.user, userIsPatient: true, doctors: queryResult, error: ""};
                            displayPage(200, res, '/public/templates/doctors.html', "Error serving doctors.html: ",ejsParams);
                        });
                    } else {
                        // only patients can view Doctors.html; redirect to main
                        var ejsParams = {user: req.cookies.user, userIsPatient: userIsPatient};
                        displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", ejsParams);
                    }

                }
                else {
                    // no user found in DB; display index page
                    var ejsParams = {user: "", userIsPatient: ""};
                    displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", ejsParams);
                }
            });
        } else {
            var ejsParams = {user: "", userIsPatient: ""};
            displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", ejsParams);
        }
    } else {
        displayPage(RATE_LIMIT_CODE, res, '/public/templates/rate_limit.html', "Error serving rate_limit.html: ", {});
    }
});

/**
 * Handles patients page
 */
app.get('/patients', function (req, res) {

     var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
    
        if (req.cookies.user) {
            LoginDB.getUserByID(req.cookies.user, function(rowsTouched, queryResult) {

                var renderedHtml;

                if (queryResult) {
                    var userIsPatient = queryResult.getEntityType() === StringConst.PATIENT_USER_TYPE;

                    if (!userIsPatient) {

                        LoginDB.getPatients(req.cookies.user, function(rowCount, queryResult) {
                            var ejsParams = {
                                            user: req.cookies.user,
                                            userIsPatient: false,
                                            patients: queryResult
                                        };

                            displayPage(200, res, '/public/templates/patients.html', "Error serving patients.html: ",ejsParams);
                        });
                    } else {
                        // only doctors can view Patients.html; redirect to main
                        var ejsParams = {user: req.cookies.user, userIsPatient: userIsPatient};
                        displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", ejsParams);
                    }
                }
                else {
                    var ejsParams = {user: "", userIsPatient: ""};  // user doesn't exist
                     displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", ejsParams);
                }
            });
        } else {
            var ejsParams = {user: "", userIsPatient: ""};  // user doesn't exist
            displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ", ejsParams);
        }
    } else {
        displayPage(RATE_LIMIT_CODE, res, '/public/templates/rate_limit.html', "Error serving rate_limit.html: ", {});
    }
});

/**
 * Handles all other queries; responds with 404 page
 */
app.get('*', function(req, res) {

     var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
    
        if (req.cookies.user) {
            LoginDB.getUserByID(req.cookies.user, function(rowsTouched, queryResult) {

                var ejsParams;

                if (queryResult) {
                    var userIsPatient = queryResult.getEntityType() === StringConst.PATIENT_USER_TYPE;

                    ejsParams = {user: req.cookies.user, userIsPatient: userIsPatient};
                }
                else {
                    ejsParams = {user: "", userIsPatient: ""};
                }

                displayPage(404, res, '/public/templates/404.html', "Error serving 404.html: ",ejsParams);
            });
        } else {
            var ejsParams = {user: "", userIsPatient: ""};
            displayPage(404, res, '/public/templates/404.html', "Error serving 404.html: ",ejsParams);
        }
    } else {
        displayPage(RATE_LIMIT_CODE, res, '/public/templates/rate_limit.html', "Error serving rate_limit.html: ", {});
    }
});

/**
 * Handles user login-attempt
 */
app.post('/loginAction', function(req, res) {

     var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
        
        // check that user entered both required params
        if (!req.body.user_name || !req.body.user_password) {

            // notify user of unsuccessful login
            var ejsParams = {error: "Login unsuccessful: provide all input.", user:""};
            displayPage(200, res, '/public/templates/login.html', "Error serving login.html: ",ejsParams);

        } else {

            var pw = req.body.user_password.trim();
            var userName = req.body.user_name.trim();

            // user entered both required params, now query databse to verify password
            LoginDB.getUserByID(userName, function(rowsTouched, queryResults){

                // only attempt to compare passwords if query was successful
                if (queryResults != null && rowsTouched == 1) {

                    utils.comparePassword(pw, queryResults.getPassword(),
                        function(err, isPasswordMatch) {

                            if (isPasswordMatch) {
                                // notify user of successful login
                                
                                // TODO - improve on cookie use
                                var userIsPatient = queryResults.getEntityType() === StringConst.PATIENT_USER_TYPE;

                                // store cookie for 1 day
                                res.cookie('user', userName, {maxAge: 1000 * 60 * 60 * 24, httpOnly:true});

                                var ejsParams = {user: userName, userIsPatient: userIsPatient};

                                displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ",ejsParams);       
                            } else {
                                // notify user of unsuccessful login
                                var ejsParams = {error: "Login unsuccessful: incorrect password.", user:""};
                                displayPage(200, res, '/public/templates/login.html', "Error serving login.html: ",ejsParams);
                            }
                    });
                } else {
                    // notify user of unsuccessful login (no user found)
                    var ejsParams = {error: "Login unsuccessful: user does not exist.", user:""};
                    displayPage(200, res, '/public/templates/login.html', "Error serving login.html: ",ejsParams);
                }
            });
        }
    } else {
        displayPage(RATE_LIMIT_CODE, res, '/public/templates/rate_limit.html', "Error serving rate_limit.html: ", {});
    }
});

/**
 * Handles user register-attempt
 */
app.post('/registerAction', function(req, res) {

     var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
    
        //check that user entered all required params
        if (!req.body.user_password || !req.body.user_name || !req.body.user_type) {
            // notify user of unsuccessful login
            var ejsParams = {error: "Register unsuccessful: provide all input.", user:""};
            displayPage(200, res, '/public/templates/signup.html', "Error serving signup.html: ",ejsParams);
        } else {

            var pw = req.body.user_password[1].trim();
            var verifyPW = req.body.user_password[1].trim();
            var userName = req.body.user_name.trim();
            var email = req.body.user_email.trim();
            
            // check that passwords match, enforce PW/Username syntax, and verify validity of email address
            if (pw === verifyPW
                && utils.isValidPassword(pw) && utils.isValidUserName(userName)
                && (!email || utils.isValidEmail(email))) {

                var userType = "";
                if (req.body.user_type === 'p') {

                    // user type of 'p' denotes patient
                    userType = StringConst.PATIENT_USER_TYPE;
                } else {

                    // only valid type remaining is DOCTOR
                    userType = StringConst.DOCTOR_USER_TYPE;
                }

                if (!email) {
                    email = StringConst.NULL_FIELD; // email not provided; list as null
                }

                // hash user's password then insert user in database
                utils.hashPassword(pw, function(err, hashedPW) {

                    // store hashed pw into DB
                    LoginDB.insertNewUser(userName, hashedPW, email, userType,

                        function(rowsTouched) {

                            // one row touched corresponds to successful insertion
                            if (rowsTouched === 1) {

                                // notify user of successful register
                      
                                // TODO - improve on cookie use

                                // store cookie for 1 day
                                res.cookie('user', userName, {maxAge: 1000 * 60 * 60 * 24, httpOnly:true});

                                var userIsPatient = userType === StringConst.PATIENT_USER_TYPE;

                                var ejsParams = {user: userName, userIsPatient: userIsPatient};
                                          
                                displayPage(200, res, '/public/templates/index.html', "Error serving index.html: ",ejsParams);
                            }
                            // an error occurred while inserting user into the database
                            else {

                                // notify user of bad input
                                var ejsParams = {user: "", error: "Register unsuccessful."
                                                        + "\nEither username or email already exists."};
                                displayPage(200, res, '/public/templates/signup.html', "Error serving signup.html: ",ejsParams);
                            }
                        });
                });

            } else {

                var ejsParams;

               if (pw !== verifyPW) {

                    // notify user of password mismatch
                    ejsParams = {user: "", error: "Passwords don't match."};
                }
                // since email is optional, validity only applies if it was entered
                else if (!utils.isValidEmail(email) && email) {

                    ejsParams = {user: "", error: "Invalid email."};
                } else if (!utils.isValidPassword(pw)) {

                    ejsParams = {user: "", error: "Invalid password. Must use 3-15 alpha-numerics."};
                } else if (!utils.isValidUserName(userName)) {

                    ejsParams = {user: "", error: "Invalid username. Must use 3-15 alpha-numerics."};
                }
                displayPage(200, res, '/public/templates/signup.html', "Error serving signup.html: ",ejsParams);
            }
        }
    } else {
        displayPage(RATE_LIMIT_CODE, res, '/public/templates/rate_limit.html', "Error serving rate_limit.html: ", {});
    }
});

/**
 * Handles user register-attempt
 */
app.post('/addDoctor', function(req, res) {

     var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {

        var doctorName = req.body.doctor_name.trim();

        //check that user entered all required params
        if (!doctorName) {
            // notify user of unsuccessful login
            
            LoginDB.getDoctors(req.cookies.user, function(rowCount, queryResult) {
                var ejsParams = {
                                user: req.cookies.user,
                                userIsPatient: true,
                                doctors: queryResult,
                                error: "You must enter the doctor's name first."
                            };

                displayPage(200, res, '/public/templates/doctors.html', "Error serving doctors.html: ",ejsParams);
            });
        } else if (utils.isValidUserName(doctorName)) {
         
                LoginDB.getUserByID(doctorName, function(rowCount, queryResult) {

                    if (rowCount === 1 && queryResult && queryResult.getEntityType() === StringConst.DOCTOR_USER_TYPE) {

                        LoginDB.addDoctor(req.cookies.user, doctorName, function(rowsTouched) {

                            LoginDB.getDoctors(req.cookies.user, function(rowCount, queryResultGetDr) {

                                var ejsParams = {
                                    user: req.cookies.user,
                                    userIsPatient: true,
                                    doctors: queryResultGetDr,
                                    error: ""
                                };

                                displayPage(200, res, '/public/templates/doctors.html', "Error serving doctors.html: ",ejsParams);
                            });

                        });

                    } else if (rowCount === 1 && queryResult) {

                        LoginDB.getDoctors(req.cookies.user, function(rowCount, queryResultGetDr) {
                             var ejsParams = {
                                user: req.cookies.user,
                                userIsPatient: true,
                                doctors: queryResultGetDr,
                                error: "User exists but is not a doctor."
                            };
                        displayPage(200, res, '/public/templates/doctors.html', "Error serving doctors.html: ",ejsParams);   
                       
                       });

                    } else {
              
                        LoginDB.getDoctors(req.cookies.user, function(rowCount, queryResultGetDr) {
                            var ejsParams =  {
                            user: req.cookies.user,
                            userIsPatient: true,
                            doctors: queryResultGetDr,
                            error: "User doesn't exist."
                        };

                        displayPage(200, res, '/public/templates/doctors.html', "Error serving doctors.html: ",ejsParams);
                        });
                    }
                });

        } else {
            LoginDB.getDoctors(req.cookies.user, function(rowCount, queryResult) {
                var ejsParams = {
                    user: req.cookies.user,
                    userIsPatient: true,
                    doctors: queryResult,
                    error: "Input wasn't syntactically valid."
                };

                displayPage(200, res, '/public/templates/doctors.html', "Error serving doctors.html: ",ejsParams);
            });
        }       
    } else {
        displayPage(RATE_LIMIT_CODE, res, '/public/templates/rate_limit.html', "Error serving rate_limit.html: ", {});
    }
});

// ---- Code Tests UDP Functionality ---

// TODO - implement the testing portion of site

//var DataPacketClass = require('./datapacket');
//var InterestPacketClass = require('./interestpacket');

// method allows user to test networking functionality
/*app.post('/submitIP', function(req, res) {

 if (req.body.user.ipAddrPing !== undefined) {
 // user requested ping

 var sys = require('sys')
 var exec = require('child_process').exec;

 function puts(error, stdout, stderr) {
 console.log(stdout);
 }

 exec("ping -c 3 " + req.body.user.ipAddrPing, puts);

 } else if (req.body.user.ipAddrTrace !== undefined) {
 // user requested traceroute

 var traceroute = require('traceroute');

 traceroute.trace(req.body.user.ipAddrTrace,
 function (err,hops) {
 if (!err) {
 console.log(hops);
 } else {
 console.log("error: " + err);
 }
 });

 } else {
 // user requested fake packets sent to them

 var dataPacket = new DataPacketClass.DataPacket();
 dataPacket.DataPacket("CLOUD-SERVER", StringConst.NULL_FIELD, StringConst.CURRENT_TIME,
 StringConst.DATA_CACHE, "0,99,100,101,102");

 var interestPacket = new InterestPacketClass.InterestPacket();
 interestPacket.InterestPacket("CLOUD-SERVER", StringConst.NULL_FIELD,
 StringConst.CURRENT_TIME, StringConst.INTEREST_CACHE_DATA, "0,99,100,101,102");

 udp_comm.sendMessage(dataPacket.createDATA(), req.body.user.ipAddr);
 udp_comm.sendMessage(interestPacket.createINTEREST(), req.body.user.ipAddr);
 }

 });*/
// ---- Code Tests UDP Functionality ---

// --- Below Code Handles DB Creation ---

var postgresDB = require('pg'); // the postgres Node.js module

var client = new postgresDB.Client(StringConst.DB_CONNECTION_STRING);
client.connect(function(err) {
  if(err) {
    return console.error('could not connect to postgres', err);
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

function createPIT() {

    ifNonexistentCreateDB(StringConst.PIT_DB, StringConst.createPITQuery(StringConst.PIT_DB));
    ifNonexistentCreateDB(StringConst.PIT_TEST_DB, StringConst.createPITQuery(StringConst.PIT_TEST_DB));
}

function createCS() {
  
    ifNonexistentCreateDB(StringConst.CS_DB, StringConst.createCSQuery(StringConst.CS_DB));
    ifNonexistentCreateDB(StringConst.CS_TEST_DB, StringConst.createCSQuery(StringConst.CS_TEST_DB));
}

function createFIB() {

    ifNonexistentCreateDB(StringConst.FIB_DB, StringConst.createFIBQuery(StringConst.FIB_DB));
    ifNonexistentCreateDB(StringConst.FIB_TEST_DB, StringConst.createFIBQuery(StringConst.FIB_TEST_DB));
}

function createLoginDB() {

    ifNonexistentCreateDB(StringConst.LOGIN_DB, StringConst.createLoginDBQuery(StringConst.LOGIN_DB));
    ifNonexistentCreateDB(StringConst.LOGIN_TEST_DB, StringConst.createLoginDBQuery(StringConst.LOGIN_TEST_DB));
}

createFIB();
createCS();
createPIT();
createLoginDB();

// --- Above Code Handles DB Creation ---

http.createServer(app).listen(app.get('port'), function() {
	console.log('Express server listening on port ' + app.get('port'));
});
