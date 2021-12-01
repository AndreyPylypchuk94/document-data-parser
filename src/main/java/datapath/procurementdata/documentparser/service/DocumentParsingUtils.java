package datapath.procurementdata.documentparser.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class DocumentParsingUtils {

    public static String toZonedDateTimeString(Date date) {
        if (date == null) return null;
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).format(ISO_OFFSET_DATE_TIME);
    }
}
