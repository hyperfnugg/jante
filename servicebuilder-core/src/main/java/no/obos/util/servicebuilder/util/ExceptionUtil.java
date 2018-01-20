package no.obos.util.servicebuilder.util;

public class ExceptionUtil {
    public static void wrapCheckedExceptionsVoid(VoidThrower thrower) {
        try {
            thrower.doStuff();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <T> T wrapCheckedExceptions(Thrower<T> thrower) {
        try {
            return thrower.doStuff();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public interface VoidThrower {
        void doStuff() throws Exception;
    }

    public interface Thrower<T> {
        T doStuff() throws Exception;
    }
}
