# PHINet

PHINet is a testbed for Health-IoT over the Content Centric Network (CCN) known as Named Data Networking (NDN). Its creation was primarily motivated by the gap in what was possible and what was desired in terms of studying the implications Health-IoT over a CCN.

This project is described in a paper accepted by the IEEE Mobile Services conference (link will be provided soon).

# Install

[Our client application is available for android devices.](https://play.google.com/store/apps/details?id=com.ndnhealthnet.androidudpclient)

[Visit our website for additional information/functionality.](http://phinet.elasticbeanstalk.com/)

# Contributing

All documented and tested contributions that further the aim of this project are welcome. Consult the following list for ideas.

###GENERAL
- REQUIRED: test multiple clients interacting
- REQUIRED: improve docs (for both website and wiki)
- REQUIRED: forward Packets to appropriate FIB entries (store where data comes from, etc)
- REQUIRED: modify/use all NDN packet components
- REQUIRED: increase code clarity / handle all TODOs
- REQUIRED: more rigorous testing
- REQUIRED: add to the "supported senors" list
- REQUIRED: view doctor profile (contact information, etc)
- REQUIRED: crisis management and detection
- DESIRED: doctor name suggestions when typing 
- DESIRED: allow more fine tuning in regards to requesting patient data

###CLIENT
- REQUIRED: handle case of if user already sent interest and its still in pit (not satisfied)
- REQUIRED: connect to sensors, send data requests at given intervals, retrieve and store data
- REQUIRED: keep FIB fresh
- DESIRED: larger options of analytic/sensor selection within ViewDataActivity.java
- DESIRED: multi-sensor graph
- DESIRED: improve sensor axes
- DESIRED: improve layout
- DESIRED: allow interval-selection in PacketListActvity
- DESIRED: assess memory usage

###WEB
- REQUIRED: server incorrectly assumes all data is for itself; fix (it's functional now, however)
- REQUIRED: get rid of temporary data structures that store login/signup result
- REQUIRED: view your own data
- REQUIRED: provide link for doctor's to view patient's data
- DESIRED: allow for editing of profile data
- DESIRED: improve naive rate-limiting
- DESIRED: improve DB schemas; they don't scale well
- DESIRED: improve layout
- DESIRED: basic testing page (to test with device)
- DESIRED: perform more complex analytics
- DESIRED: make mobile friendly

# Tests

- To run the PHINetCloud tests, you must install mocha (preferably globally): "npm install -g mocha". Then, within the tests directory, run "mocha test_x.js" where "x" is the test you'd like to run. Moreover, all tests can be run given the following command: "mocha test_*.js".

- To run PHINetDroid tests, right click on the ApplicationTest file and click the android icon (if your configrations are different, you must delete them and then click).

# Running Server Code Locally

If you want an android device to interact with PHINetCloud running locally, simply change the android's SERVER_IP (from within ConstVar) to the IP of your machine.

# Support 

Send all questions to cseales6@gmail.com.

# License

[MIT License.](https://github.com/seales/PHINet/blob/master/LICENSE.md)