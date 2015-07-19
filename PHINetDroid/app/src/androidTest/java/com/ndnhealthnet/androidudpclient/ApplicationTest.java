package com.ndnhealthnet.androidudpclient;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;

/**
 * Class handles all test cases for entire application.
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();

        Context context = getContext();

        DatabaseHandlerTest dbHandlerTest = new DatabaseHandlerTest(context);
        dbHandlerTest.runTests();

        UtilsTest utilsTest = new UtilsTest(context);
        utilsTest.runTests();
    }
}