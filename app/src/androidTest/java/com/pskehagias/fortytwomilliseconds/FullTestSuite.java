package com.pskehagias.fortytwomilliseconds;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created by pkcyr on 3/18/2016.
 */
public class FullTestSuite extends TestSuite {
    public FullTestSuite(){
        super();
    }

    public Test suite(){
        return new TestSuiteBuilder(FullTestSuite.class)
                .includeAllPackagesUnderHere().build();
    }
}
