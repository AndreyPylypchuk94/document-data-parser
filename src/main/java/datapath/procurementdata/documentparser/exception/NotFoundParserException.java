package datapath.procurementdata.documentparser.exception;

public class NotFoundParserException extends RuntimeException {
    public NotFoundParserException() {
        super("Parser not found");
    }
}
