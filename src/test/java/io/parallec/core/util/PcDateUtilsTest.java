package io.parallec.core.util;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class PcDateUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSuits() {
        new PcDateUtils();

        PcDateUtils.getDateTimeStrConcise(new Date());
        PcDateUtils.getDateTimeStrConcise(null);

        PcDateUtils.getNowDateTimeStrConcise();
        PcDateUtils.getNowDateTimeStrStandard();

        PcDateUtils.getDateTimeStrStandard(new Date());
        PcDateUtils.getDateTimeStrStandard(new Date(0L));
        PcDateUtils.getDateTimeStrStandard(null);

        PcDateUtils.getDateTimeStr(new Date());
        PcDateUtils.getDateTimeStr(null);

        PcDateUtils.getDateFromConciseStr("");
        PcDateUtils.getDateFromConciseStr(null);
        PcDateUtils.getDateFromConciseStr("20150913104745849-0700");
        PcDateUtils.getDateFromConciseStr("20150913104745849aa-0700");

        PcDateUtils.getDateTimeStrConciseNoZone(new Date());
        PcDateUtils.getDateTimeStrConciseNoZone(null);

        PcDateUtils.getNowDateTimeStrConciseNoZone();

        PcDateUtils.getNowDateTimeStr();

    }

}
