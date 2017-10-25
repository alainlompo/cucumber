package gherkin.exceptions;

public class DialectProviderException extends RuntimeException {

	private static final long serialVersionUID = -8939219073513372503L;

	public DialectProviderException(Exception ex) {
		super(ex);
	}
}
