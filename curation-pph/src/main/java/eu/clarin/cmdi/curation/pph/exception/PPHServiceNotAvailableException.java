package eu.clarin.cmdi.curation.pph.exception;

/**
 * The type PPHServiceNotAvailableException
 */
public class PPHServiceNotAvailableException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Pph service not available exception.
    *
    * @param cause the cause
    */
   public PPHServiceNotAvailableException(Throwable cause) {
      super(cause);
   }
}
